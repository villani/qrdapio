package br.com.qrdapio.web.rest;

import static br.com.qrdapio.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import br.com.qrdapio.IntegrationTest;
import br.com.qrdapio.domain.Cardapio;
import br.com.qrdapio.domain.ItemCardapio;
import br.com.qrdapio.domain.enumeration.Categoria;
import br.com.qrdapio.repository.ItemCardapioRepository;
import br.com.qrdapio.service.EntityManager;
import java.math.BigDecimal;
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
 * Integration tests for the {@link ItemCardapioResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient
@WithMockUser
class ItemCardapioResourceIT {

    private static final Categoria DEFAULT_CATEGORIA = Categoria.PRATO;
    private static final Categoria UPDATED_CATEGORIA = Categoria.BEBIDA;

    private static final String DEFAULT_NOME = "AAAAAAAAAA";
    private static final String UPDATED_NOME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRICAO = "AAAAAAAAAA";
    private static final String UPDATED_DESCRICAO = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_VALOR = new BigDecimal(1);
    private static final BigDecimal UPDATED_VALOR = new BigDecimal(2);

    private static final String ENTITY_API_URL = "/api/item-cardapios";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ItemCardapioRepository itemCardapioRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private ItemCardapio itemCardapio;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ItemCardapio createEntity(EntityManager em) {
        ItemCardapio itemCardapio = new ItemCardapio()
            .categoria(DEFAULT_CATEGORIA)
            .nome(DEFAULT_NOME)
            .descricao(DEFAULT_DESCRICAO)
            .valor(DEFAULT_VALOR);
        // Add required entity
        Cardapio cardapio;
        cardapio = em.insert(CardapioResourceIT.createEntity(em)).block();
        itemCardapio.setCardapio(cardapio);
        return itemCardapio;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ItemCardapio createUpdatedEntity(EntityManager em) {
        ItemCardapio itemCardapio = new ItemCardapio()
            .categoria(UPDATED_CATEGORIA)
            .nome(UPDATED_NOME)
            .descricao(UPDATED_DESCRICAO)
            .valor(UPDATED_VALOR);
        // Add required entity
        Cardapio cardapio;
        cardapio = em.insert(CardapioResourceIT.createUpdatedEntity(em)).block();
        itemCardapio.setCardapio(cardapio);
        return itemCardapio;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(ItemCardapio.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
        CardapioResourceIT.deleteEntities(em);
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
        itemCardapio = createEntity(em);
    }

    @Test
    void createItemCardapio() throws Exception {
        int databaseSizeBeforeCreate = itemCardapioRepository.findAll().collectList().block().size();
        // Create the ItemCardapio
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(itemCardapio))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the ItemCardapio in the database
        List<ItemCardapio> itemCardapioList = itemCardapioRepository.findAll().collectList().block();
        assertThat(itemCardapioList).hasSize(databaseSizeBeforeCreate + 1);
        ItemCardapio testItemCardapio = itemCardapioList.get(itemCardapioList.size() - 1);
        assertThat(testItemCardapio.getCategoria()).isEqualTo(DEFAULT_CATEGORIA);
        assertThat(testItemCardapio.getNome()).isEqualTo(DEFAULT_NOME);
        assertThat(testItemCardapio.getDescricao()).isEqualTo(DEFAULT_DESCRICAO);
        assertThat(testItemCardapio.getValor()).isEqualByComparingTo(DEFAULT_VALOR);
    }

    @Test
    void createItemCardapioWithExistingId() throws Exception {
        // Create the ItemCardapio with an existing ID
        itemCardapio.setId(1L);

        int databaseSizeBeforeCreate = itemCardapioRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(itemCardapio))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ItemCardapio in the database
        List<ItemCardapio> itemCardapioList = itemCardapioRepository.findAll().collectList().block();
        assertThat(itemCardapioList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkCategoriaIsRequired() throws Exception {
        int databaseSizeBeforeTest = itemCardapioRepository.findAll().collectList().block().size();
        // set the field null
        itemCardapio.setCategoria(null);

        // Create the ItemCardapio, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(itemCardapio))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<ItemCardapio> itemCardapioList = itemCardapioRepository.findAll().collectList().block();
        assertThat(itemCardapioList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkNomeIsRequired() throws Exception {
        int databaseSizeBeforeTest = itemCardapioRepository.findAll().collectList().block().size();
        // set the field null
        itemCardapio.setNome(null);

        // Create the ItemCardapio, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(itemCardapio))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<ItemCardapio> itemCardapioList = itemCardapioRepository.findAll().collectList().block();
        assertThat(itemCardapioList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkDescricaoIsRequired() throws Exception {
        int databaseSizeBeforeTest = itemCardapioRepository.findAll().collectList().block().size();
        // set the field null
        itemCardapio.setDescricao(null);

        // Create the ItemCardapio, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(itemCardapio))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<ItemCardapio> itemCardapioList = itemCardapioRepository.findAll().collectList().block();
        assertThat(itemCardapioList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllItemCardapiosAsStream() {
        // Initialize the database
        itemCardapioRepository.save(itemCardapio).block();

        List<ItemCardapio> itemCardapioList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(ItemCardapio.class)
            .getResponseBody()
            .filter(itemCardapio::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(itemCardapioList).isNotNull();
        assertThat(itemCardapioList).hasSize(1);
        ItemCardapio testItemCardapio = itemCardapioList.get(0);
        assertThat(testItemCardapio.getCategoria()).isEqualTo(DEFAULT_CATEGORIA);
        assertThat(testItemCardapio.getNome()).isEqualTo(DEFAULT_NOME);
        assertThat(testItemCardapio.getDescricao()).isEqualTo(DEFAULT_DESCRICAO);
        assertThat(testItemCardapio.getValor()).isEqualByComparingTo(DEFAULT_VALOR);
    }

    @Test
    void getAllItemCardapios() {
        // Initialize the database
        itemCardapioRepository.save(itemCardapio).block();

        // Get all the itemCardapioList
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
            .value(hasItem(itemCardapio.getId().intValue()))
            .jsonPath("$.[*].categoria")
            .value(hasItem(DEFAULT_CATEGORIA.toString()))
            .jsonPath("$.[*].nome")
            .value(hasItem(DEFAULT_NOME))
            .jsonPath("$.[*].descricao")
            .value(hasItem(DEFAULT_DESCRICAO))
            .jsonPath("$.[*].valor")
            .value(hasItem(sameNumber(DEFAULT_VALOR)));
    }

    @Test
    void getItemCardapio() {
        // Initialize the database
        itemCardapioRepository.save(itemCardapio).block();

        // Get the itemCardapio
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, itemCardapio.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(itemCardapio.getId().intValue()))
            .jsonPath("$.categoria")
            .value(is(DEFAULT_CATEGORIA.toString()))
            .jsonPath("$.nome")
            .value(is(DEFAULT_NOME))
            .jsonPath("$.descricao")
            .value(is(DEFAULT_DESCRICAO))
            .jsonPath("$.valor")
            .value(is(sameNumber(DEFAULT_VALOR)));
    }

    @Test
    void getNonExistingItemCardapio() {
        // Get the itemCardapio
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewItemCardapio() throws Exception {
        // Initialize the database
        itemCardapioRepository.save(itemCardapio).block();

        int databaseSizeBeforeUpdate = itemCardapioRepository.findAll().collectList().block().size();

        // Update the itemCardapio
        ItemCardapio updatedItemCardapio = itemCardapioRepository.findById(itemCardapio.getId()).block();
        updatedItemCardapio.categoria(UPDATED_CATEGORIA).nome(UPDATED_NOME).descricao(UPDATED_DESCRICAO).valor(UPDATED_VALOR);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedItemCardapio.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedItemCardapio))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ItemCardapio in the database
        List<ItemCardapio> itemCardapioList = itemCardapioRepository.findAll().collectList().block();
        assertThat(itemCardapioList).hasSize(databaseSizeBeforeUpdate);
        ItemCardapio testItemCardapio = itemCardapioList.get(itemCardapioList.size() - 1);
        assertThat(testItemCardapio.getCategoria()).isEqualTo(UPDATED_CATEGORIA);
        assertThat(testItemCardapio.getNome()).isEqualTo(UPDATED_NOME);
        assertThat(testItemCardapio.getDescricao()).isEqualTo(UPDATED_DESCRICAO);
        assertThat(testItemCardapio.getValor()).isEqualTo(UPDATED_VALOR);
    }

    @Test
    void putNonExistingItemCardapio() throws Exception {
        int databaseSizeBeforeUpdate = itemCardapioRepository.findAll().collectList().block().size();
        itemCardapio.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, itemCardapio.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(itemCardapio))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ItemCardapio in the database
        List<ItemCardapio> itemCardapioList = itemCardapioRepository.findAll().collectList().block();
        assertThat(itemCardapioList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchItemCardapio() throws Exception {
        int databaseSizeBeforeUpdate = itemCardapioRepository.findAll().collectList().block().size();
        itemCardapio.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(itemCardapio))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ItemCardapio in the database
        List<ItemCardapio> itemCardapioList = itemCardapioRepository.findAll().collectList().block();
        assertThat(itemCardapioList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamItemCardapio() throws Exception {
        int databaseSizeBeforeUpdate = itemCardapioRepository.findAll().collectList().block().size();
        itemCardapio.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(itemCardapio))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the ItemCardapio in the database
        List<ItemCardapio> itemCardapioList = itemCardapioRepository.findAll().collectList().block();
        assertThat(itemCardapioList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateItemCardapioWithPatch() throws Exception {
        // Initialize the database
        itemCardapioRepository.save(itemCardapio).block();

        int databaseSizeBeforeUpdate = itemCardapioRepository.findAll().collectList().block().size();

        // Update the itemCardapio using partial update
        ItemCardapio partialUpdatedItemCardapio = new ItemCardapio();
        partialUpdatedItemCardapio.setId(itemCardapio.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedItemCardapio.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedItemCardapio))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ItemCardapio in the database
        List<ItemCardapio> itemCardapioList = itemCardapioRepository.findAll().collectList().block();
        assertThat(itemCardapioList).hasSize(databaseSizeBeforeUpdate);
        ItemCardapio testItemCardapio = itemCardapioList.get(itemCardapioList.size() - 1);
        assertThat(testItemCardapio.getCategoria()).isEqualTo(DEFAULT_CATEGORIA);
        assertThat(testItemCardapio.getNome()).isEqualTo(DEFAULT_NOME);
        assertThat(testItemCardapio.getDescricao()).isEqualTo(DEFAULT_DESCRICAO);
        assertThat(testItemCardapio.getValor()).isEqualByComparingTo(DEFAULT_VALOR);
    }

    @Test
    void fullUpdateItemCardapioWithPatch() throws Exception {
        // Initialize the database
        itemCardapioRepository.save(itemCardapio).block();

        int databaseSizeBeforeUpdate = itemCardapioRepository.findAll().collectList().block().size();

        // Update the itemCardapio using partial update
        ItemCardapio partialUpdatedItemCardapio = new ItemCardapio();
        partialUpdatedItemCardapio.setId(itemCardapio.getId());

        partialUpdatedItemCardapio.categoria(UPDATED_CATEGORIA).nome(UPDATED_NOME).descricao(UPDATED_DESCRICAO).valor(UPDATED_VALOR);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedItemCardapio.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedItemCardapio))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ItemCardapio in the database
        List<ItemCardapio> itemCardapioList = itemCardapioRepository.findAll().collectList().block();
        assertThat(itemCardapioList).hasSize(databaseSizeBeforeUpdate);
        ItemCardapio testItemCardapio = itemCardapioList.get(itemCardapioList.size() - 1);
        assertThat(testItemCardapio.getCategoria()).isEqualTo(UPDATED_CATEGORIA);
        assertThat(testItemCardapio.getNome()).isEqualTo(UPDATED_NOME);
        assertThat(testItemCardapio.getDescricao()).isEqualTo(UPDATED_DESCRICAO);
        assertThat(testItemCardapio.getValor()).isEqualByComparingTo(UPDATED_VALOR);
    }

    @Test
    void patchNonExistingItemCardapio() throws Exception {
        int databaseSizeBeforeUpdate = itemCardapioRepository.findAll().collectList().block().size();
        itemCardapio.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, itemCardapio.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(itemCardapio))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ItemCardapio in the database
        List<ItemCardapio> itemCardapioList = itemCardapioRepository.findAll().collectList().block();
        assertThat(itemCardapioList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchItemCardapio() throws Exception {
        int databaseSizeBeforeUpdate = itemCardapioRepository.findAll().collectList().block().size();
        itemCardapio.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(itemCardapio))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ItemCardapio in the database
        List<ItemCardapio> itemCardapioList = itemCardapioRepository.findAll().collectList().block();
        assertThat(itemCardapioList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamItemCardapio() throws Exception {
        int databaseSizeBeforeUpdate = itemCardapioRepository.findAll().collectList().block().size();
        itemCardapio.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(itemCardapio))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the ItemCardapio in the database
        List<ItemCardapio> itemCardapioList = itemCardapioRepository.findAll().collectList().block();
        assertThat(itemCardapioList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteItemCardapio() {
        // Initialize the database
        itemCardapioRepository.save(itemCardapio).block();

        int databaseSizeBeforeDelete = itemCardapioRepository.findAll().collectList().block().size();

        // Delete the itemCardapio
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, itemCardapio.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<ItemCardapio> itemCardapioList = itemCardapioRepository.findAll().collectList().block();
        assertThat(itemCardapioList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
