package br.com.qrdapio.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import br.com.qrdapio.IntegrationTest;
import br.com.qrdapio.domain.Cardapio;
import br.com.qrdapio.domain.Restaurante;
import br.com.qrdapio.repository.CardapioRepository;
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
 * Integration tests for the {@link CardapioResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient
@WithMockUser
class CardapioResourceIT {

    private static final String DEFAULT_NOME = "AAAAAAAAAA";
    private static final String UPDATED_NOME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/cardapios";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private CardapioRepository cardapioRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Cardapio cardapio;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Cardapio createEntity(EntityManager em) {
        Cardapio cardapio = new Cardapio().nome(DEFAULT_NOME);
        // Add required entity
        Restaurante restaurante;
        restaurante = em.insert(RestauranteResourceIT.createEntity(em)).block();
        cardapio.setRestaurante(restaurante);
        return cardapio;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Cardapio createUpdatedEntity(EntityManager em) {
        Cardapio cardapio = new Cardapio().nome(UPDATED_NOME);
        // Add required entity
        Restaurante restaurante;
        restaurante = em.insert(RestauranteResourceIT.createUpdatedEntity(em)).block();
        cardapio.setRestaurante(restaurante);
        return cardapio;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Cardapio.class).block();
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
        cardapio = createEntity(em);
    }

    @Test
    void createCardapio() throws Exception {
        int databaseSizeBeforeCreate = cardapioRepository.findAll().collectList().block().size();
        // Create the Cardapio
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(cardapio))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Cardapio in the database
        List<Cardapio> cardapioList = cardapioRepository.findAll().collectList().block();
        assertThat(cardapioList).hasSize(databaseSizeBeforeCreate + 1);
        Cardapio testCardapio = cardapioList.get(cardapioList.size() - 1);
        assertThat(testCardapio.getNome()).isEqualTo(DEFAULT_NOME);
    }

    @Test
    void createCardapioWithExistingId() throws Exception {
        // Create the Cardapio with an existing ID
        cardapio.setId(1L);

        int databaseSizeBeforeCreate = cardapioRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(cardapio))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Cardapio in the database
        List<Cardapio> cardapioList = cardapioRepository.findAll().collectList().block();
        assertThat(cardapioList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkNomeIsRequired() throws Exception {
        int databaseSizeBeforeTest = cardapioRepository.findAll().collectList().block().size();
        // set the field null
        cardapio.setNome(null);

        // Create the Cardapio, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(cardapio))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Cardapio> cardapioList = cardapioRepository.findAll().collectList().block();
        assertThat(cardapioList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllCardapiosAsStream() {
        // Initialize the database
        cardapioRepository.save(cardapio).block();

        List<Cardapio> cardapioList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Cardapio.class)
            .getResponseBody()
            .filter(cardapio::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(cardapioList).isNotNull();
        assertThat(cardapioList).hasSize(1);
        Cardapio testCardapio = cardapioList.get(0);
        assertThat(testCardapio.getNome()).isEqualTo(DEFAULT_NOME);
    }

    @Test
    void getAllCardapios() {
        // Initialize the database
        cardapioRepository.save(cardapio).block();

        // Get all the cardapioList
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
            .value(hasItem(cardapio.getId().intValue()))
            .jsonPath("$.[*].nome")
            .value(hasItem(DEFAULT_NOME));
    }

    @Test
    void getCardapio() {
        // Initialize the database
        cardapioRepository.save(cardapio).block();

        // Get the cardapio
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, cardapio.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(cardapio.getId().intValue()))
            .jsonPath("$.nome")
            .value(is(DEFAULT_NOME));
    }

    @Test
    void getNonExistingCardapio() {
        // Get the cardapio
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewCardapio() throws Exception {
        // Initialize the database
        cardapioRepository.save(cardapio).block();

        int databaseSizeBeforeUpdate = cardapioRepository.findAll().collectList().block().size();

        // Update the cardapio
        Cardapio updatedCardapio = cardapioRepository.findById(cardapio.getId()).block();
        updatedCardapio.nome(UPDATED_NOME);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedCardapio.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedCardapio))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Cardapio in the database
        List<Cardapio> cardapioList = cardapioRepository.findAll().collectList().block();
        assertThat(cardapioList).hasSize(databaseSizeBeforeUpdate);
        Cardapio testCardapio = cardapioList.get(cardapioList.size() - 1);
        assertThat(testCardapio.getNome()).isEqualTo(UPDATED_NOME);
    }

    @Test
    void putNonExistingCardapio() throws Exception {
        int databaseSizeBeforeUpdate = cardapioRepository.findAll().collectList().block().size();
        cardapio.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, cardapio.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(cardapio))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Cardapio in the database
        List<Cardapio> cardapioList = cardapioRepository.findAll().collectList().block();
        assertThat(cardapioList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchCardapio() throws Exception {
        int databaseSizeBeforeUpdate = cardapioRepository.findAll().collectList().block().size();
        cardapio.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(cardapio))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Cardapio in the database
        List<Cardapio> cardapioList = cardapioRepository.findAll().collectList().block();
        assertThat(cardapioList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamCardapio() throws Exception {
        int databaseSizeBeforeUpdate = cardapioRepository.findAll().collectList().block().size();
        cardapio.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(cardapio))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Cardapio in the database
        List<Cardapio> cardapioList = cardapioRepository.findAll().collectList().block();
        assertThat(cardapioList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateCardapioWithPatch() throws Exception {
        // Initialize the database
        cardapioRepository.save(cardapio).block();

        int databaseSizeBeforeUpdate = cardapioRepository.findAll().collectList().block().size();

        // Update the cardapio using partial update
        Cardapio partialUpdatedCardapio = new Cardapio();
        partialUpdatedCardapio.setId(cardapio.getId());

        partialUpdatedCardapio.nome(UPDATED_NOME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedCardapio.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedCardapio))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Cardapio in the database
        List<Cardapio> cardapioList = cardapioRepository.findAll().collectList().block();
        assertThat(cardapioList).hasSize(databaseSizeBeforeUpdate);
        Cardapio testCardapio = cardapioList.get(cardapioList.size() - 1);
        assertThat(testCardapio.getNome()).isEqualTo(UPDATED_NOME);
    }

    @Test
    void fullUpdateCardapioWithPatch() throws Exception {
        // Initialize the database
        cardapioRepository.save(cardapio).block();

        int databaseSizeBeforeUpdate = cardapioRepository.findAll().collectList().block().size();

        // Update the cardapio using partial update
        Cardapio partialUpdatedCardapio = new Cardapio();
        partialUpdatedCardapio.setId(cardapio.getId());

        partialUpdatedCardapio.nome(UPDATED_NOME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedCardapio.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedCardapio))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Cardapio in the database
        List<Cardapio> cardapioList = cardapioRepository.findAll().collectList().block();
        assertThat(cardapioList).hasSize(databaseSizeBeforeUpdate);
        Cardapio testCardapio = cardapioList.get(cardapioList.size() - 1);
        assertThat(testCardapio.getNome()).isEqualTo(UPDATED_NOME);
    }

    @Test
    void patchNonExistingCardapio() throws Exception {
        int databaseSizeBeforeUpdate = cardapioRepository.findAll().collectList().block().size();
        cardapio.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, cardapio.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(cardapio))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Cardapio in the database
        List<Cardapio> cardapioList = cardapioRepository.findAll().collectList().block();
        assertThat(cardapioList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchCardapio() throws Exception {
        int databaseSizeBeforeUpdate = cardapioRepository.findAll().collectList().block().size();
        cardapio.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(cardapio))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Cardapio in the database
        List<Cardapio> cardapioList = cardapioRepository.findAll().collectList().block();
        assertThat(cardapioList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamCardapio() throws Exception {
        int databaseSizeBeforeUpdate = cardapioRepository.findAll().collectList().block().size();
        cardapio.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(cardapio))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Cardapio in the database
        List<Cardapio> cardapioList = cardapioRepository.findAll().collectList().block();
        assertThat(cardapioList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteCardapio() {
        // Initialize the database
        cardapioRepository.save(cardapio).block();

        int databaseSizeBeforeDelete = cardapioRepository.findAll().collectList().block().size();

        // Delete the cardapio
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, cardapio.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Cardapio> cardapioList = cardapioRepository.findAll().collectList().block();
        assertThat(cardapioList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
