package br.com.qrdapio.repository;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import br.com.qrdapio.domain.Cardapio;
import br.com.qrdapio.repository.rowmapper.CardapioRowMapper;
import br.com.qrdapio.repository.rowmapper.RestauranteRowMapper;
import br.com.qrdapio.service.EntityManager;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive custom repository implementation for the Cardapio entity.
 */
@SuppressWarnings("unused")
class CardapioRepositoryInternalImpl implements CardapioRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final RestauranteRowMapper restauranteMapper;
    private final CardapioRowMapper cardapioMapper;

    private static final Table entityTable = Table.aliased("cardapio", EntityManager.ENTITY_ALIAS);
    private static final Table restauranteTable = Table.aliased("restaurante", "restaurante");

    public CardapioRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        RestauranteRowMapper restauranteMapper,
        CardapioRowMapper cardapioMapper
    ) {
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.restauranteMapper = restauranteMapper;
        this.cardapioMapper = cardapioMapper;
    }

    @Override
    public Flux<Cardapio> findAllBy(Pageable pageable) {
        return findAllBy(pageable, null);
    }

    @Override
    public Flux<Cardapio> findAllBy(Pageable pageable, Criteria criteria) {
        return createQuery(pageable, criteria).all();
    }

    RowsFetchSpec<Cardapio> createQuery(Pageable pageable, Criteria criteria) {
        List<Expression> columns = CardapioSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(RestauranteSqlHelper.getColumns(restauranteTable, "restaurante"));
        SelectFromAndJoinCondition selectFrom = Select
            .builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(restauranteTable)
            .on(Column.create("restaurante_id", entityTable))
            .equals(Column.create("id", restauranteTable));

        String select = entityManager.createSelect(selectFrom, Cardapio.class, pageable, criteria);
        String alias = entityTable.getReferenceName().getReference();
        String selectWhere = Optional
            .ofNullable(criteria)
            .map(
                crit ->
                    new StringBuilder(select)
                        .append(" ")
                        .append("WHERE")
                        .append(" ")
                        .append(alias)
                        .append(".")
                        .append(crit.toString())
                        .toString()
            )
            .orElse(select); // TODO remove once https://github.com/spring-projects/spring-data-jdbc/issues/907 will be fixed
        return db.sql(selectWhere).map(this::process);
    }

    @Override
    public Flux<Cardapio> findAll() {
        return findAllBy(null, null);
    }

    @Override
    public Mono<Cardapio> findById(Long id) {
        return createQuery(null, where("id").is(id)).one();
    }

    private Cardapio process(Row row, RowMetadata metadata) {
        Cardapio entity = cardapioMapper.apply(row, "e");
        entity.setRestaurante(restauranteMapper.apply(row, "restaurante"));
        return entity;
    }

    @Override
    public <S extends Cardapio> Mono<S> insert(S entity) {
        return entityManager.insert(entity);
    }

    @Override
    public <S extends Cardapio> Mono<S> save(S entity) {
        if (entity.getId() == null) {
            return insert(entity);
        } else {
            return update(entity)
                .map(
                    numberOfUpdates -> {
                        if (numberOfUpdates.intValue() <= 0) {
                            throw new IllegalStateException("Unable to update Cardapio with id = " + entity.getId());
                        }
                        return entity;
                    }
                );
        }
    }

    @Override
    public Mono<Integer> update(Cardapio entity) {
        //fixme is this the proper way?
        return r2dbcEntityTemplate.update(entity).thenReturn(1);
    }
}

class CardapioSqlHelper {

    static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("nome", table, columnPrefix + "_nome"));

        columns.add(Column.aliased("restaurante_id", table, columnPrefix + "_restaurante_id"));
        return columns;
    }
}
