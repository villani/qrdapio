package br.com.qrdapio.repository;

import br.com.qrdapio.domain.ItemPedido;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the ItemPedido entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ItemPedidoRepository extends R2dbcRepository<ItemPedido, Long>, ItemPedidoRepositoryInternal {
    @Query("SELECT * FROM item_pedido entity WHERE entity.item_id = :id")
    Flux<ItemPedido> findByItem(Long id);

    @Query("SELECT * FROM item_pedido entity WHERE entity.item_id IS NULL")
    Flux<ItemPedido> findAllWhereItemIsNull();

    @Query("SELECT * FROM item_pedido entity WHERE entity.pedido_id = :id")
    Flux<ItemPedido> findByPedido(Long id);

    @Query("SELECT * FROM item_pedido entity WHERE entity.pedido_id IS NULL")
    Flux<ItemPedido> findAllWherePedidoIsNull();

    // just to avoid having unambigous methods
    @Override
    Flux<ItemPedido> findAll();

    @Override
    Mono<ItemPedido> findById(Long id);

    @Override
    <S extends ItemPedido> Mono<S> save(S entity);
}

interface ItemPedidoRepositoryInternal {
    <S extends ItemPedido> Mono<S> insert(S entity);
    <S extends ItemPedido> Mono<S> save(S entity);
    Mono<Integer> update(ItemPedido entity);

    Flux<ItemPedido> findAll();
    Mono<ItemPedido> findById(Long id);
    Flux<ItemPedido> findAllBy(Pageable pageable);
    Flux<ItemPedido> findAllBy(Pageable pageable, Criteria criteria);
}
