package br.com.qrdapio.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import br.com.qrdapio.IntegrationTest;
import br.com.qrdapio.domain.Restaurante;
import br.com.qrdapio.repository.RestauranteRepository;
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
 * Integration tests for the {@link RestauranteResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient
@WithMockUser
class RestauranteResourceIT {

    private static final String DEFAULT_NOME = "AAAAAAAAAA";
    private static final String UPDATED_NOME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/restaurantes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Restaurante restaurante;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Restaurante createEntity(EntityManager em) {
        Restaurante restaurante = new Restaurante().nome(DEFAULT_NOME);
        return restaurante;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Restaurante createUpdatedEntity(EntityManager em) {
        Restaurante restaurante = new Restaurante().nome(UPDATED_NOME);
        return restaurante;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Restaurante.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
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
        restaurante = createEntity(em);
    }

    @Test
    void createRestaurante() throws Exception {
        int databaseSizeBeforeCreate = restauranteRepository.findAll().collectList().block().size();
        // Create the Restaurante
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(restaurante))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Restaurante in the database
        List<Restaurante> restauranteList = restauranteRepository.findAll().collectList().block();
        assertThat(restauranteList).hasSize(databaseSizeBeforeCreate + 1);
        Restaurante testRestaurante = restauranteList.get(restauranteList.size() - 1);
        assertThat(testRestaurante.getNome()).isEqualTo(DEFAULT_NOME);
    }

    @Test
    void createRestauranteWithExistingId() throws Exception {
        // Create the Restaurante with an existing ID
        restaurante.setId(1L);

        int databaseSizeBeforeCreate = restauranteRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(restaurante))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Restaurante in the database
        List<Restaurante> restauranteList = restauranteRepository.findAll().collectList().block();
        assertThat(restauranteList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkNomeIsRequired() throws Exception {
        int databaseSizeBeforeTest = restauranteRepository.findAll().collectList().block().size();
        // set the field null
        restaurante.setNome(null);

        // Create the Restaurante, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(restaurante))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Restaurante> restauranteList = restauranteRepository.findAll().collectList().block();
        assertThat(restauranteList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllRestaurantesAsStream() {
        // Initialize the database
        restauranteRepository.save(restaurante).block();

        List<Restaurante> restauranteList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Restaurante.class)
            .getResponseBody()
            .filter(restaurante::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(restauranteList).isNotNull();
        assertThat(restauranteList).hasSize(1);
        Restaurante testRestaurante = restauranteList.get(0);
        assertThat(testRestaurante.getNome()).isEqualTo(DEFAULT_NOME);
    }

    @Test
    void getAllRestaurantes() {
        // Initialize the database
        restauranteRepository.save(restaurante).block();

        // Get all the restauranteList
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
            .value(hasItem(restaurante.getId().intValue()))
            .jsonPath("$.[*].nome")
            .value(hasItem(DEFAULT_NOME));
    }

    @Test
    void getRestaurante() {
        // Initialize the database
        restauranteRepository.save(restaurante).block();

        // Get the restaurante
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, restaurante.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(restaurante.getId().intValue()))
            .jsonPath("$.nome")
            .value(is(DEFAULT_NOME));
    }

    @Test
    void getNonExistingRestaurante() {
        // Get the restaurante
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewRestaurante() throws Exception {
        // Initialize the database
        restauranteRepository.save(restaurante).block();

        int databaseSizeBeforeUpdate = restauranteRepository.findAll().collectList().block().size();

        // Update the restaurante
        Restaurante updatedRestaurante = restauranteRepository.findById(restaurante.getId()).block();
        updatedRestaurante.nome(UPDATED_NOME);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedRestaurante.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedRestaurante))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Restaurante in the database
        List<Restaurante> restauranteList = restauranteRepository.findAll().collectList().block();
        assertThat(restauranteList).hasSize(databaseSizeBeforeUpdate);
        Restaurante testRestaurante = restauranteList.get(restauranteList.size() - 1);
        assertThat(testRestaurante.getNome()).isEqualTo(UPDATED_NOME);
    }

    @Test
    void putNonExistingRestaurante() throws Exception {
        int databaseSizeBeforeUpdate = restauranteRepository.findAll().collectList().block().size();
        restaurante.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, restaurante.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(restaurante))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Restaurante in the database
        List<Restaurante> restauranteList = restauranteRepository.findAll().collectList().block();
        assertThat(restauranteList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchRestaurante() throws Exception {
        int databaseSizeBeforeUpdate = restauranteRepository.findAll().collectList().block().size();
        restaurante.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(restaurante))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Restaurante in the database
        List<Restaurante> restauranteList = restauranteRepository.findAll().collectList().block();
        assertThat(restauranteList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamRestaurante() throws Exception {
        int databaseSizeBeforeUpdate = restauranteRepository.findAll().collectList().block().size();
        restaurante.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(restaurante))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Restaurante in the database
        List<Restaurante> restauranteList = restauranteRepository.findAll().collectList().block();
        assertThat(restauranteList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateRestauranteWithPatch() throws Exception {
        // Initialize the database
        restauranteRepository.save(restaurante).block();

        int databaseSizeBeforeUpdate = restauranteRepository.findAll().collectList().block().size();

        // Update the restaurante using partial update
        Restaurante partialUpdatedRestaurante = new Restaurante();
        partialUpdatedRestaurante.setId(restaurante.getId());

        partialUpdatedRestaurante.nome(UPDATED_NOME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedRestaurante.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedRestaurante))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Restaurante in the database
        List<Restaurante> restauranteList = restauranteRepository.findAll().collectList().block();
        assertThat(restauranteList).hasSize(databaseSizeBeforeUpdate);
        Restaurante testRestaurante = restauranteList.get(restauranteList.size() - 1);
        assertThat(testRestaurante.getNome()).isEqualTo(UPDATED_NOME);
    }

    @Test
    void fullUpdateRestauranteWithPatch() throws Exception {
        // Initialize the database
        restauranteRepository.save(restaurante).block();

        int databaseSizeBeforeUpdate = restauranteRepository.findAll().collectList().block().size();

        // Update the restaurante using partial update
        Restaurante partialUpdatedRestaurante = new Restaurante();
        partialUpdatedRestaurante.setId(restaurante.getId());

        partialUpdatedRestaurante.nome(UPDATED_NOME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedRestaurante.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedRestaurante))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Restaurante in the database
        List<Restaurante> restauranteList = restauranteRepository.findAll().collectList().block();
        assertThat(restauranteList).hasSize(databaseSizeBeforeUpdate);
        Restaurante testRestaurante = restauranteList.get(restauranteList.size() - 1);
        assertThat(testRestaurante.getNome()).isEqualTo(UPDATED_NOME);
    }

    @Test
    void patchNonExistingRestaurante() throws Exception {
        int databaseSizeBeforeUpdate = restauranteRepository.findAll().collectList().block().size();
        restaurante.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, restaurante.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(restaurante))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Restaurante in the database
        List<Restaurante> restauranteList = restauranteRepository.findAll().collectList().block();
        assertThat(restauranteList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchRestaurante() throws Exception {
        int databaseSizeBeforeUpdate = restauranteRepository.findAll().collectList().block().size();
        restaurante.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(restaurante))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Restaurante in the database
        List<Restaurante> restauranteList = restauranteRepository.findAll().collectList().block();
        assertThat(restauranteList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamRestaurante() throws Exception {
        int databaseSizeBeforeUpdate = restauranteRepository.findAll().collectList().block().size();
        restaurante.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(restaurante))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Restaurante in the database
        List<Restaurante> restauranteList = restauranteRepository.findAll().collectList().block();
        assertThat(restauranteList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteRestaurante() {
        // Initialize the database
        restauranteRepository.save(restaurante).block();

        int databaseSizeBeforeDelete = restauranteRepository.findAll().collectList().block().size();

        // Delete the restaurante
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, restaurante.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Restaurante> restauranteList = restauranteRepository.findAll().collectList().block();
        assertThat(restauranteList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
