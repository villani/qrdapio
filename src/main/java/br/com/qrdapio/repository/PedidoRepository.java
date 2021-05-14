package br.com.qrdapio.repository;

import br.com.qrdapio.domain.Pedido;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Pedido entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PedidoRepository extends R2dbcRepository<Pedido, Long>, PedidoRepositoryInternal {
    @Query("SELECT * FROM pedido entity WHERE entity.restaurante_id = :id")
    Flux<Pedido> findByRestaurante(Long id);

    @Query("SELECT * FROM pedido entity WHERE entity.restaurante_id IS NULL")
    Flux<Pedido> findAllWhereRestauranteIsNull();

    // just to avoid having unambigous methods
    @Override
    Flux<Pedido> findAll();

    @Override
    Mono<Pedido> findById(Long id);

    @Override
    <S extends Pedido> Mono<S> save(S entity);
}

interface PedidoRepositoryInternal {
    <S extends Pedido> Mono<S> insert(S entity);
    <S extends Pedido> Mono<S> save(S entity);
    Mono<Integer> update(Pedido entity);

    Flux<Pedido> findAll();
    Mono<Pedido> findById(Long id);
    Flux<Pedido> findAllBy(Pageable pageable);
    Flux<Pedido> findAllBy(Pageable pageable, Criteria criteria);
}
