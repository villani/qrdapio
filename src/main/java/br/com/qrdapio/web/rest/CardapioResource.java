package br.com.qrdapio.web.rest;

import br.com.qrdapio.domain.Cardapio;
import br.com.qrdapio.repository.CardapioRepository;
import br.com.qrdapio.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link br.com.qrdapio.domain.Cardapio}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class CardapioResource {

    private final Logger log = LoggerFactory.getLogger(CardapioResource.class);

    private static final String ENTITY_NAME = "cardapio";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CardapioRepository cardapioRepository;

    public CardapioResource(CardapioRepository cardapioRepository) {
        this.cardapioRepository = cardapioRepository;
    }

    /**
     * {@code POST  /cardapios} : Create a new cardapio.
     *
     * @param cardapio the cardapio to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new cardapio, or with status {@code 400 (Bad Request)} if the cardapio has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/cardapios")
    public Mono<ResponseEntity<Cardapio>> createCardapio(@Valid @RequestBody Cardapio cardapio) throws URISyntaxException {
        log.debug("REST request to save Cardapio : {}", cardapio);
        if (cardapio.getId() != null) {
            throw new BadRequestAlertException("A new cardapio cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return cardapioRepository
            .save(cardapio)
            .map(
                result -> {
                    try {
                        return ResponseEntity
                            .created(new URI("/api/cardapios/" + result.getId()))
                            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                            .body(result);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }
            );
    }

    /**
     * {@code PUT  /cardapios/:id} : Updates an existing cardapio.
     *
     * @param id the id of the cardapio to save.
     * @param cardapio the cardapio to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated cardapio,
     * or with status {@code 400 (Bad Request)} if the cardapio is not valid,
     * or with status {@code 500 (Internal Server Error)} if the cardapio couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/cardapios/{id}")
    public Mono<ResponseEntity<Cardapio>> updateCardapio(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Cardapio cardapio
    ) throws URISyntaxException {
        log.debug("REST request to update Cardapio : {}, {}", id, cardapio);
        if (cardapio.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, cardapio.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return cardapioRepository
            .existsById(id)
            .flatMap(
                exists -> {
                    if (!exists) {
                        return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                    }

                    return cardapioRepository
                        .save(cardapio)
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                        .map(
                            result ->
                                ResponseEntity
                                    .ok()
                                    .headers(
                                        HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString())
                                    )
                                    .body(result)
                        );
                }
            );
    }

    /**
     * {@code PATCH  /cardapios/:id} : Partial updates given fields of an existing cardapio, field will ignore if it is null
     *
     * @param id the id of the cardapio to save.
     * @param cardapio the cardapio to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated cardapio,
     * or with status {@code 400 (Bad Request)} if the cardapio is not valid,
     * or with status {@code 404 (Not Found)} if the cardapio is not found,
     * or with status {@code 500 (Internal Server Error)} if the cardapio couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/cardapios/{id}", consumes = "application/merge-patch+json")
    public Mono<ResponseEntity<Cardapio>> partialUpdateCardapio(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Cardapio cardapio
    ) throws URISyntaxException {
        log.debug("REST request to partial update Cardapio partially : {}, {}", id, cardapio);
        if (cardapio.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, cardapio.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return cardapioRepository
            .existsById(id)
            .flatMap(
                exists -> {
                    if (!exists) {
                        return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                    }

                    Mono<Cardapio> result = cardapioRepository
                        .findById(cardapio.getId())
                        .map(
                            existingCardapio -> {
                                if (cardapio.getNome() != null) {
                                    existingCardapio.setNome(cardapio.getNome());
                                }

                                return existingCardapio;
                            }
                        )
                        .flatMap(cardapioRepository::save);

                    return result
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                        .map(
                            res ->
                                ResponseEntity
                                    .ok()
                                    .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                                    .body(res)
                        );
                }
            );
    }

    /**
     * {@code GET  /cardapios} : get all the cardapios.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of cardapios in body.
     */
    @GetMapping("/cardapios")
    public Mono<List<Cardapio>> getAllCardapios() {
        log.debug("REST request to get all Cardapios");
        return cardapioRepository.findAll().collectList();
    }

    /**
     * {@code GET  /cardapios} : get all the cardapios as a stream.
     * @return the {@link Flux} of cardapios.
     */
    @GetMapping(value = "/cardapios", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Cardapio> getAllCardapiosAsStream() {
        log.debug("REST request to get all Cardapios as a stream");
        return cardapioRepository.findAll();
    }

    /**
     * {@code GET  /cardapios/:id} : get the "id" cardapio.
     *
     * @param id the id of the cardapio to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the cardapio, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/cardapios/{id}")
    public Mono<ResponseEntity<Cardapio>> getCardapio(@PathVariable Long id) {
        log.debug("REST request to get Cardapio : {}", id);
        Mono<Cardapio> cardapio = cardapioRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(cardapio);
    }

    /**
     * {@code DELETE  /cardapios/:id} : delete the "id" cardapio.
     *
     * @param id the id of the cardapio to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/cardapios/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteCardapio(@PathVariable Long id) {
        log.debug("REST request to delete Cardapio : {}", id);
        return cardapioRepository
            .deleteById(id)
            .map(
                result ->
                    ResponseEntity
                        .noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                        .build()
            );
    }
}
