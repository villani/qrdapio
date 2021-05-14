package br.com.qrdapio.web.rest;

import static br.com.qrdapio.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import br.com.qrdapio.IntegrationTest;
import br.com.qrdapio.domain.Pedido;
import br.com.qrdapio.domain.Restaurante;
import br.com.qrdapio.domain.enumeration.FormaPagamento;
import br.com.qrdapio.repository.PedidoRepository;
import br.com.qrdapio.service.EntityManager;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link PedidoResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient
@WithMockUser
class PedidoResourceIT {

    private static final FormaPagamento DEFAULT_FORMA_PAGAMENTO = FormaPagamento.CREDITO;
    private static final FormaPagamento UPDATED_FORMA_PAGAMENTO = FormaPagamento.DEBITO;

    private static final ZonedDateTime DEFAULT_DATA_HORA = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_DATA_HORA = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final Integer DEFAULT_SENHA = 1;
    private static final Integer UPDATED_SENHA = 2;

    private static final String ENTITY_API_URL = "/api/pedidos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Pedido pedido;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Pedido createEntity(EntityManager em) {
        Pedido pedido = new Pedido().formaPagamento(DEFAULT_FORMA_PAGAMENTO).dataHora(DEFAULT_DATA_HORA).senha(DEFAULT_SENHA);
        // Add required entity
        Restaurante restaurante;
        restaurante = em.insert(RestauranteResourceIT.createEntity(em)).block();
        pedido.setRestaurante(restaurante);
        return pedido;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Pedido createUpdatedEntity(EntityManager em) {
        Pedido pedido = new Pedido().formaPagamento(UPDATED_FORMA_PAGAMENTO).dataHora(UPDATED_DATA_HORA).senha(UPDATED_SENHA);
        // Add required entity
        Restaurante restaurante;
        restaurante = em.insert(RestauranteResourceIT.createUpdatedEntity(em)).block();
        pedido.setRestaurante(restaurante);
        return pedido;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Pedido.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
        RestauranteResourceIT.deleteEntities(em);
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @BeforeEach
    public void setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        pedido = createEntity(em);
    }

    @Test
    void createPedido() throws Exception {
        int databaseSizeBeforeCreate = pedidoRepository.findAll().collectList().block().size();
        // Create the Pedido
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(pedido))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Pedido in the database
        List<Pedido> pedidoList = pedidoRepository.findAll().collectList().block();
        assertThat(pedidoList).hasSize(databaseSizeBeforeCreate + 1);
        Pedido testPedido = pedidoList.get(pedidoList.size() - 1);
        assertThat(testPedido.getFormaPagamento()).isEqualTo(DEFAULT_FORMA_PAGAMENTO);
        assertThat(testPedido.getDataHora()).isEqualTo(DEFAULT_DATA_HORA);
        assertThat(testPedido.getSenha()).isEqualTo(DEFAULT_SENHA);
    }

    @Test
    void createPedidoWithExistingId() throws Exception {
        // Create the Pedido with an existing ID
        pedido.setId(1L);

        int databaseSizeBeforeCreate = pedidoRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(pedido))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Pedido in the database
        List<Pedido> pedidoList = pedidoRepository.findAll().collectList().block();
        assertThat(pedidoList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkFormaPagamentoIsRequired() throws Exception {
        int databaseSizeBeforeTest = pedidoRepository.findAll().collectList().block().size();
        // set the field null
        pedido.setFormaPagamento(null);

        // Create the Pedido, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(pedido))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Pedido> pedidoList = pedidoRepository.findAll().collectList().block();
        assertThat(pedidoList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkDataHoraIsRequired() throws Exception {
        int databaseSizeBeforeTest = pedidoRepository.findAll().collectList().block().size();
        // set the field null
        pedido.setDataHora(null);

        // Create the Pedido, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(pedido))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Pedido> pedidoList = pedidoRepository.findAll().collectList().block();
        assertThat(pedidoList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllPedidosAsStream() {
        // Initialize the database
        pedidoRepository.save(pedido).block();

        List<Pedido> pedidoList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Pedido.class)
            .getResponseBody()
            .filter(pedido::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(pedidoList).isNotNull();
        assertThat(pedidoList).hasSize(1);
        Pedido testPedido = pedidoList.get(0);
        assertThat(testPedido.getFormaPagamento()).isEqualTo(DEFAULT_FORMA_PAGAMENTO);
        assertThat(testPedido.getDataHora()).isEqualTo(DEFAULT_DATA_HORA);
        assertThat(testPedido.getSenha()).isEqualTo(DEFAULT_SENHA);
    }

    @Test
    void getAllPedidos() {
        // Initialize the database
        pedidoRepository.save(pedido).block();

        // Get all the pedidoList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(pedido.getId().intValue()))
            .jsonPath("$.[*].formaPagamento")
            .value(hasItem(DEFAULT_FORMA_PAGAMENTO.toString()))
            .jsonPath("$.[*].dataHora")
            .value(hasItem(sameInstant(DEFAULT_DATA_HORA)))
            .jsonPath("$.[*].senha")
            .value(hasItem(DEFAULT_SENHA));
    }

    @Test
    void getPedido() {
        // Initialize the database
        pedidoRepository.save(pedido).block();

        // Get the pedido
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, pedido.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(pedido.getId().intValue()))
            .jsonPath("$.formaPagamento")
            .value(is(DEFAULT_FORMA_PAGAMENTO.toString()))
            .jsonPath("$.dataHora")
            .value(is(sameInstant(DEFAULT_DATA_HORA)))
            .jsonPath("$.senha")
            .value(is(DEFAULT_SENHA));
    }

    @Test
    void getNonExistingPedido() {
        // Get the pedido
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewPedido() throws Exception {
        // Initialize the database
        pedidoRepository.save(pedido).block();

        int databaseSizeBeforeUpdate = pedidoRepository.findAll().collectList().block().size();

        // Update the pedido
        Pedido updatedPedido = pedidoRepository.findById(pedido.getId()).block();
        updatedPedido.formaPagamento(UPDATED_FORMA_PAGAMENTO).dataHora(UPDATED_DATA_HORA).senha(UPDATED_SENHA);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedPedido.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedPedido))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Pedido in the database
        List<Pedido> pedidoList = pedidoRepository.findAll().collectList().block();
        assertThat(pedidoList).hasSize(databaseSizeBeforeUpdate);
        Pedido testPedido = pedidoList.get(pedidoList.size() - 1);
        assertThat(testPedido.getFormaPagamento()).isEqualTo(UPDATED_FORMA_PAGAMENTO);
        assertThat(testPedido.getDataHora()).isEqualTo(UPDATED_DATA_HORA);
        assertThat(testPedido.getSenha()).isEqualTo(UPDATED_SENHA);
    }

    @Test
    void putNonExistingPedido() throws Exception {
        int databaseSizeBeforeUpdate = pedidoRepository.findAll().collectList().block().size();
        pedido.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, pedido.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(pedido))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Pedido in the database
        List<Pedido> pedidoList = pedidoRepository.findAll().collectList().block();
        assertThat(pedidoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchPedido() throws Exception {
        int databaseSizeBeforeUpdate = pedidoRepository.findAll().collectList().block().size();
        pedido.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(pedido))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Pedido in the database
        List<Pedido> pedidoList = pedidoRepository.findAll().collectList().block();
        assertThat(pedidoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamPedido() throws Exception {
        int databaseSizeBeforeUpdate = pedidoRepository.findAll().collectList().block().size();
        pedido.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(pedido))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Pedido in the database
        List<Pedido> pedidoList = pedidoRepository.findAll().collectList().block();
        assertThat(pedidoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdatePedidoWithPatch() throws Exception {
        // Initialize the database
        pedidoRepository.save(pedido).block();

        int databaseSizeBeforeUpdate = pedidoRepository.findAll().collectList().block().size();

        // Update the pedido using partial update
        Pedido partialUpdatedPedido = new Pedido();
        partialUpdatedPedido.setId(pedido.getId());

        partialUpdatedPedido.dataHora(UPDATED_DATA_HORA).senha(UPDATED_SENHA);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedPedido.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedPedido))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Pedido in the database
        List<Pedido> pedidoList = pedidoRepository.findAll().collectList().block();
        assertThat(pedidoList).hasSize(databaseSizeBeforeUpdate);
        Pedido testPedido = pedidoList.get(pedidoList.size() - 1);
        assertThat(testPedido.getFormaPagamento()).isEqualTo(DEFAULT_FORMA_PAGAMENTO);
        assertThat(testPedido.getDataHora()).isEqualTo(UPDATED_DATA_HORA);
        assertThat(testPedido.getSenha()).isEqualTo(UPDATED_SENHA);
    }

    @Test
    void fullUpdatePedidoWithPatch() throws Exception {
        // Initialize the database
        pedidoRepository.save(pedido).block();

        int databaseSizeBeforeUpdate = pedidoRepository.findAll().collectList().block().size();

        // Update the pedido using partial update
        Pedido partialUpdatedPedido = new Pedido();
        partialUpdatedPedido.setId(pedido.getId());

        partialUpdatedPedido.formaPagamento(UPDATED_FORMA_PAGAMENTO).dataHora(UPDATED_DATA_HORA).senha(UPDATED_SENHA);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedPedido.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedPedido))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Pedido in the database
        List<Pedido> pedidoList = pedidoRepository.findAll().collectList().block();
        assertThat(pedidoList).hasSize(databaseSizeBeforeUpdate);
        Pedido testPedido = pedidoList.get(pedidoList.size() - 1);
        assertThat(testPedido.getFormaPagamento()).isEqualTo(UPDATED_FORMA_PAGAMENTO);
        assertThat(testPedido.getDataHora()).isEqualTo(UPDATED_DATA_HORA);
        assertThat(testPedido.getSenha()).isEqualTo(UPDATED_SENHA);
    }

    @Test
    void patchNonExistingPedido() throws Exception {
        int databaseSizeBeforeUpdate = pedidoRepository.findAll().collectList().block().size();
        pedido.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, pedido.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(pedido))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Pedido in the database
        List<Pedido> pedidoList = pedidoRepository.findAll().collectList().block();
        assertThat(pedidoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchPedido() throws Exception {
        int databaseSizeBeforeUpdate = pedidoRepository.findAll().collectList().block().size();
        pedido.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(pedido))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Pedido in the database
        List<Pedido> pedidoList = pedidoRepository.findAll().collectList().block();
        assertThat(pedidoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamPedido() throws Exception {
        int databaseSizeBeforeUpdate = pedidoRepository.findAll().collectList().block().size();
        pedido.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(pedido))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Pedido in the database
        List<Pedido> pedidoList = pedidoRepository.findAll().collectList().block();
        assertThat(pedidoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deletePedido() {
        // Initialize the database
        pedidoRepository.save(pedido).block();

        int databaseSizeBeforeDelete = pedidoRepository.findAll().collectList().block().size();

        // Delete the pedido
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, pedido.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Pedido> pedidoList = pedidoRepository.findAll().collectList().block();
        assertThat(pedidoList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
