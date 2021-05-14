package br.com.qrdapio.repository;

import br.com.qrdapio.domain.Cardapio;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Cardapio entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CardapioRepository extends R2dbcRepository<Cardapio, Long>, CardapioRepositoryInternal {
    @Query("SELECT * FROM cardapio entity WHERE entity.restaurante_id = :id")
    Flux<Cardapio> findByRestaurante(Long id);

    @Query("SELECT * FROM cardapio entity WHERE entity.restaurante_id IS NULL")
    Flux<Cardapio> findAllWhereRestauranteIsNull();

    // just to avoid having unambigous methods
    @Override
    Flux<Cardapio> findAll();

    @Override
    Mono<Cardapio> findById(Long id);

    @Override
    <S extends Cardapio> Mono<S> save(S entity);
}

interface CardapioRepositoryInternal {
    <S extends Cardapio> Mono<S> insert(S entity);
    <S extends Cardapio> Mono<S> save(S entity);
    Mono<Integer> update(Cardapio entity);

    Flux<Cardapio> findAll();
    Mono<Cardapio> findById(Long id);
    Flux<Cardapio> findAllBy(Pageable pageable);
    Flux<Cardapio> findAllBy(Pageable pageable, Criteria criteria);
}
