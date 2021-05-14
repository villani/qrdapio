package br.com.qrdapio.repository;

import br.com.qrdapio.domain.ItemCardapio;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the ItemCardapio entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ItemCardapioRepository extends R2dbcRepository<ItemCardapio, Long>, ItemCardapioRepositoryInternal {
    @Query("SELECT * FROM item_cardapio entity WHERE entity.cardapio_id = :id")
    Flux<ItemCardapio> findByCardapio(Long id);

    @Query("SELECT * FROM item_cardapio entity WHERE entity.cardapio_id IS NULL")
    Flux<ItemCardapio> findAllWhereCardapioIsNull();

    // just to avoid having unambigous methods
    @Override
    Flux<ItemCardapio> findAll();

    @Override
    Mono<ItemCardapio> findById(Long id);

    @Override
    <S extends ItemCardapio> Mono<S> save(S entity);
}

interface ItemCardapioRepositoryInternal {
    <S extends ItemCardapio> Mono<S> insert(S entity);
    <S extends ItemCardapio> Mono<S> save(S entity);
    Mono<Integer> update(ItemCardapio entity);

    Flux<ItemCardapio> findAll();
    Mono<ItemCardapio> findById(Long id);
    Flux<ItemCardapio> findAllBy(Pageable pageable);
    Flux<ItemCardapio> findAllBy(Pageable pageable, Criteria criteria);
}
