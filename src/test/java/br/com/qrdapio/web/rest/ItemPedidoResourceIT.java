package br.com.qrdapio.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import br.com.qrdapio.IntegrationTest;
import br.com.qrdapio.domain.ItemCardapio;
import br.com.qrdapio.domain.ItemPedido;
import br.com.qrdapio.domain.Pedido;
import br.com.qrdapio.repository.ItemPedidoRepository;
import br.com.qrdapio.service.EntityManager;
import java.time.Duration;
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
 * Integration tests for the {@link ItemPedidoResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient
@WithMockUser
class ItemPedidoResourceIT {

    private static final Integer DEFAULT_QUANTIDADE = 1;
    private static final Integer UPDATED_QUANTIDADE = 2;

    private static final String ENTITY_API_URL = "/api/item-pedidos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ItemPedidoRepository itemPedidoRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private ItemPedido itemPedido;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ItemPedido createEntity(EntityManager em) {
        ItemPedido itemPedido = new ItemPedido().quantidade(DEFAULT_QUANTIDADE);
        // Add required entity
        ItemCardapio itemCardapio;
        itemCardapio = em.insert(ItemCardapioResourceIT.createEntity(em)).block();
        itemPedido.setItem(itemCardapio);
        // Add required entity
        Pedido pedido;
        pedido = em.insert(PedidoResourceIT.createEntity(em)).block();
        itemPedido.setPedido(pedido);
        return itemPedido;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ItemPedido createUpdatedEntity(EntityManager em) {
        ItemPedido itemPedido = new ItemPedido().quantidade(UPDATED_QUANTIDADE);
        // Add required entity
        ItemCardapio itemCardapio;
        itemCardapio = em.insert(ItemCardapioResourceIT.createUpdatedEntity(em)).block();
        itemPedido.setItem(itemCardapio);
        // Add required entity
        Pedido pedido;
        pedido = em.insert(PedidoResourceIT.createUpdatedEntity(em)).block();
        itemPedido.setPedido(pedido);
        return itemPedido;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(ItemPedido.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
        ItemCardapioResourceIT.deleteEntities(em);
        PedidoResourceIT.deleteEntities(em);
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
        itemPedido = createEntity(em);
    }

    @Test
    void createItemPedido() throws Exception {
        int databaseSizeBeforeCreate = itemPedidoRepository.findAll().collectList().block().size();
        // Create the ItemPedido
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(itemPedido))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the ItemPedido in the database
        List<ItemPedido> itemPedidoList = itemPedidoRepository.findAll().collectList().block();
        assertThat(itemPedidoList).hasSize(databaseSizeBeforeCreate + 1);
        ItemPedido testItemPedido = itemPedidoList.get(itemPedidoList.size() - 1);
        assertThat(testItemPedido.getQuantidade()).isEqualTo(DEFAULT_QUANTIDADE);
    }

    @Test
    void createItemPedidoWithExistingId() throws Exception {
        // Create the ItemPedido with an existing ID
        itemPedido.setId(1L);

        int databaseSizeBeforeCreate = itemPedidoRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(itemPedido))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ItemPedido in the database
        List<ItemPedido> itemPedidoList = itemPedidoRepository.findAll().collectList().block();
        assertThat(itemPedidoList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void getAllItemPedidosAsStream() {
        // Initialize the database
        itemPedidoRepository.save(itemPedido).block();

        List<ItemPedido> itemPedidoList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(ItemPedido.class)
            .getResponseBody()
            .filter(itemPedido::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(itemPedidoList).isNotNull();
        assertThat(itemPedidoList).hasSize(1);
        ItemPedido testItemPedido = itemPedidoList.get(0);
        assertThat(testItemPedido.getQuantidade()).isEqualTo(DEFAULT_QUANTIDADE);
    }

    @Test
    void getAllItemPedidos() {
        // Initialize the database
        itemPedidoRepository.save(itemPedido).block();

        // Get all the itemPedidoList
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
            .value(hasItem(itemPedido.getId().intValue()))
            .jsonPath("$.[*].quantidade")
            .value(hasItem(DEFAULT_QUANTIDADE));
    }

    @Test
    void getItemPedido() {
        // Initialize the database
        itemPedidoRepository.save(itemPedido).block();

        // Get the itemPedido
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, itemPedido.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(itemPedido.getId().intValue()))
            .jsonPath("$.quantidade")
            .value(is(DEFAULT_QUANTIDADE));
    }

    @Test
    void getNonExistingItemPedido() {
        // Get the itemPedido
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewItemPedido() throws Exception {
        // Initialize the database
        itemPedidoRepository.save(itemPedido).block();

        int databaseSizeBeforeUpdate = itemPedidoRepository.findAll().collectList().block().size();

        // Update the itemPedido
        ItemPedido updatedItemPedido = itemPedidoRepository.findById(itemPedido.getId()).block();
        updatedItemPedido.quantidade(UPDATED_QUANTIDADE);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedItemPedido.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedItemPedido))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ItemPedido in the database
        List<ItemPedido> itemPedidoList = itemPedidoRepository.findAll().collectList().block();
        assertThat(itemPedidoList).hasSize(databaseSizeBeforeUpdate);
        ItemPedido testItemPedido = itemPedidoList.get(itemPedidoList.size() - 1);
        assertThat(testItemPedido.getQuantidade()).isEqualTo(UPDATED_QUANTIDADE);
    }

    @Test
    void putNonExistingItemPedido() throws Exception {
        int databaseSizeBeforeUpdate = itemPedidoRepository.findAll().collectList().block().size();
        itemPedido.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, itemPedido.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(itemPedido))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ItemPedido in the database
        List<ItemPedido> itemPedidoList = itemPedidoRepository.findAll().collectList().block();
        assertThat(itemPedidoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchItemPedido() throws Exception {
        int databaseSizeBeforeUpdate = itemPedidoRepository.findAll().collectList().block().size();
        itemPedido.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(itemPedido))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ItemPedido in the database
        List<ItemPedido> itemPedidoList = itemPedidoRepository.findAll().collectList().block();
        assertThat(itemPedidoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamItemPedido() throws Exception {
        int databaseSizeBeforeUpdate = itemPedidoRepository.findAll().collectList().block().size();
        itemPedido.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(itemPedido))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the ItemPedido in the database
        List<ItemPedido> itemPedidoList = itemPedidoRepository.findAll().collectList().block();
        assertThat(itemPedidoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateItemPedidoWithPatch() throws Exception {
        // Initialize the database
        itemPedidoRepository.save(itemPedido).block();

        int databaseSizeBeforeUpdate = itemPedidoRepository.findAll().collectList().block().size();

        // Update the itemPedido using partial update
        ItemPedido partialUpdatedItemPedido = new ItemPedido();
        partialUpdatedItemPedido.setId(itemPedido.getId());

        partialUpdatedItemPedido.quantidade(UPDATED_QUANTIDADE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedItemPedido.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedItemPedido))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ItemPedido in the database
        List<ItemPedido> itemPedidoList = itemPedidoRepository.findAll().collectList().block();
        assertThat(itemPedidoList).hasSize(databaseSizeBeforeUpdate);
        ItemPedido testItemPedido = itemPedidoList.get(itemPedidoList.size() - 1);
        assertThat(testItemPedido.getQuantidade()).isEqualTo(UPDATED_QUANTIDADE);
    }

    @Test
    void fullUpdateItemPedidoWithPatch() throws Exception {
        // Initialize the database
        itemPedidoRepository.save(itemPedido).block();

        int databaseSizeBeforeUpdate = itemPedidoRepository.findAll().collectList().block().size();

        // Update the itemPedido using partial update
        ItemPedido partialUpdatedItemPedido = new ItemPedido();
        partialUpdatedItemPedido.setId(itemPedido.getId());

        partialUpdatedItemPedido.quantidade(UPDATED_QUANTIDADE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedItemPedido.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedItemPedido))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ItemPedido in the database
        List<ItemPedido> itemPedidoList = itemPedidoRepository.findAll().collectList().block();
        assertThat(itemPedidoList).hasSize(databaseSizeBeforeUpdate);
        ItemPedido testItemPedido = itemPedidoList.get(itemPedidoList.size() - 1);
        assertThat(testItemPedido.getQuantidade()).isEqualTo(UPDATED_QUANTIDADE);
    }

    @Test
    void patchNonExistingItemPedido() throws Exception {
        int databaseSizeBeforeUpdate = itemPedidoRepository.findAll().collectList().block().size();
        itemPedido.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, itemPedido.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(itemPedido))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ItemPedido in the database
        List<ItemPedido> itemPedidoList = itemPedidoRepository.findAll().collectList().block();
        assertThat(itemPedidoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchItemPedido() throws Exception {
        int databaseSizeBeforeUpdate = itemPedidoRepository.findAll().collectList().block().size();
        itemPedido.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(itemPedido))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ItemPedido in the database
        List<ItemPedido> itemPedidoList = itemPedidoRepository.findAll().collectList().block();
        assertThat(itemPedidoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamItemPedido() throws Exception {
        int databaseSizeBeforeUpdate = itemPedidoRepository.findAll().collectList().block().size();
        itemPedido.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(itemPedido))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the ItemPedido in the database
        List<ItemPedido> itemPedidoList = itemPedidoRepository.findAll().collectList().block();
        assertThat(itemPedidoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteItemPedido() {
        // Initialize the database
        itemPedidoRepository.save(itemPedido).block();

        int databaseSizeBeforeDelete = itemPedidoRepository.findAll().collectList().block().size();

        // Delete the itemPedido
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, itemPedido.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<ItemPedido> itemPedidoList = itemPedidoRepository.findAll().collectList().block();
        assertThat(itemPedidoList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
