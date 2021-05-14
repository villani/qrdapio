package br.com.qrdapio.repository;

import br.com.qrdapio.domain.Restaurante;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Restaurante entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RestauranteRepository extends R2dbcRepository<Restaurante, Long>, RestauranteRepositoryInternal {
    // just to avoid having unambigous methods
    @Override
    Flux<Restaurante> findAll();

    @Override
    Mono<Restaurante> findById(Long id);

    @Override
    <S extends Restaurante> Mono<S> save(S entity);
}

interface RestauranteRepositoryInternal {
    <S extends Restaurante> Mono<S> insert(S entity);
    <S extends Restaurante> Mono<S> save(S entity);
    Mono<Integer> update(Restaurante entity);

    Flux<Restaurante> findAll();
    Mono<Restaurante> findById(Long id);
    Flux<Restaurante> findAllBy(Pageable pageable);
    Flux<Restaurante> findAllBy(Pageable pageable, Criteria criteria);
}
