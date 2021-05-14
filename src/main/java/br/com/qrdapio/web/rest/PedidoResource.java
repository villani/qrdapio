package br.com.qrdapio.web.rest;

import br.com.qrdapio.domain.Pedido;
import br.com.qrdapio.repository.PedidoRepository;
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
 * REST controller for managing {@link br.com.qrdapio.domain.Pedido}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class PedidoResource {

    private final Logger log = LoggerFactory.getLogger(PedidoResource.class);

    private static final String ENTITY_NAME = "pedido";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PedidoRepository pedidoRepository;

    public PedidoResource(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * {@code POST  /pedidos} : Create a new pedido.
     *
     * @param pedido the pedido to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new pedido, or with status {@code 400 (Bad Request)} if the pedido has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/pedidos")
    public Mono<ResponseEntity<Pedido>> createPedido(@Valid @RequestBody Pedido pedido) throws URISyntaxException {
        log.debug("REST request to save Pedido : {}", pedido);
        if (pedido.getId() != null) {
            throw new BadRequestAlertException("A new pedido cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return pedidoRepository
            .save(pedido)
            .map(
                result -> {
                    try {
                        return ResponseEntity
                            .created(new URI("/api/pedidos/" + result.getId()))
                            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                            .body(result);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }
            );
    }

    /**
     * {@code PUT  /pedidos/:id} : Updates an existing pedido.
     *
     * @param id the id of the pedido to save.
     * @param pedido the pedido to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated pedido,
     * or with status {@code 400 (Bad Request)} if the pedido is not valid,
     * or with status {@code 500 (Internal Server Error)} if the pedido couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/pedidos/{id}")
    public Mono<ResponseEntity<Pedido>> updatePedido(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Pedido pedido
    ) throws URISyntaxException {
        log.debug("REST request to update Pedido : {}, {}", id, pedido);
        if (pedido.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, pedido.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return pedidoRepository
            .existsById(id)
            .flatMap(
                exists -> {
                    if (!exists) {
                        return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                    }

                    return pedidoRepository
                        .save(pedido)
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
     * {@code PATCH  /pedidos/:id} : Partial updates given fields of an existing pedido, field will ignore if it is null
     *
     * @param id the id of the pedido to save.
     * @param pedido the pedido to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated pedido,
     * or with status {@code 400 (Bad Request)} if the pedido is not valid,
     * or with status {@code 404 (Not Found)} if the pedido is not found,
     * or with status {@code 500 (Internal Server Error)} if the pedido couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/pedidos/{id}", consumes = "application/merge-patch+json")
    public Mono<ResponseEntity<Pedido>> partialUpdatePedido(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Pedido pedido
    ) throws URISyntaxException {
        log.debug("REST request to partial update Pedido partially : {}, {}", id, pedido);
        if (pedido.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, pedido.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return pedidoRepository
            .existsById(id)
            .flatMap(
                exists -> {
                    if (!exists) {
                        return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                    }

                    Mono<Pedido> result = pedidoRepository
                        .findById(pedido.getId())
                        .map(
                            existingPedido -> {
                                if (pedido.getFormaPagamento() != null) {
                                    existingPedido.setFormaPagamento(pedido.getFormaPagamento());
                                }
                                if (pedido.getDataHora() != null) {
                                    existingPedido.setDataHora(pedido.getDataHora());
                                }
                                if (pedido.getSenha() != null) {
                                    existingPedido.setSenha(pedido.getSenha());
                                }

                                return existingPedido;
                            }
                        )
                        .flatMap(pedidoRepository::save);

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
     * {@code GET  /pedidos} : get all the pedidos.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of pedidos in body.
     */
    @GetMapping("/pedidos")
    public Mono<List<Pedido>> getAllPedidos() {
        log.debug("REST request to get all Pedidos");
        return pedidoRepository.findAll().collectList();
    }

    /**
     * {@code GET  /pedidos} : get all the pedidos as a stream.
     * @return the {@link Flux} of pedidos.
     */
    @GetMapping(value = "/pedidos", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Pedido> getAllPedidosAsStream() {
        log.debug("REST request to get all Pedidos as a stream");
        return pedidoRepository.findAll();
    }

    /**
     * {@code GET  /pedidos/:id} : get the "id" pedido.
     *
     * @param id the id of the pedido to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the pedido, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/pedidos/{id}")
    public Mono<ResponseEntity<Pedido>> getPedido(@PathVariable Long id) {
        log.debug("REST request to get Pedido : {}", id);
        Mono<Pedido> pedido = pedidoRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(pedido);
    }

    /**
     * {@code DELETE  /pedidos/:id} : delete the "id" pedido.
     *
     * @param id the id of the pedido to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/pedidos/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deletePedido(@PathVariable Long id) {
        log.debug("REST request to delete Pedido : {}", id);
        return pedidoRepository
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
