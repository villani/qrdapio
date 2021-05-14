package br.com.qrdapio.repository;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import br.com.qrdapio.domain.ItemCardapio;
import br.com.qrdapio.domain.enumeration.Categoria;
import br.com.qrdapio.repository.rowmapper.CardapioRowMapper;
import br.com.qrdapio.repository.rowmapper.ItemCardapioRowMapper;
import br.com.qrdapio.service.EntityManager;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.math.BigDecimal;
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
 * Spring Data SQL reactive custom repository implementation for the ItemCardapio entity.
 */
@SuppressWarnings("unused")
class ItemCardapioRepositoryInternalImpl implements ItemCardapioRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final CardapioRowMapper cardapioMapper;
    private final ItemCardapioRowMapper itemcardapioMapper;

    private static final Table entityTable = Table.aliased("item_cardapio", EntityManager.ENTITY_ALIAS);
    private static final Table cardapioTable = Table.aliased("cardapio", "cardapio");

    public ItemCardapioRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        CardapioRowMapper cardapioMapper,
        ItemCardapioRowMapper itemcardapioMapper
    ) {
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.cardapioMapper = cardapioMapper;
        this.itemcardapioMapper = itemcardapioMapper;
    }

    @Override
    public Flux<ItemCardapio> findAllBy(Pageable pageable) {
        return findAllBy(pageable, null);
    }

    @Override
    public Flux<ItemCardapio> findAllBy(Pageable pageable, Criteria criteria) {
        return createQuery(pageable, criteria).all();
    }

    RowsFetchSpec<ItemCardapio> createQuery(Pageable pageable, Criteria criteria) {
        List<Expression> columns = ItemCardapioSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(CardapioSqlHelper.getColumns(cardapioTable, "cardapio"));
        SelectFromAndJoinCondition selectFrom = Select
            .builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(cardapioTable)
            .on(Column.create("cardapio_id", entityTable))
            .equals(Column.create("id", cardapioTable));

        String select = entityManager.createSelect(selectFrom, ItemCardapio.class, pageable, criteria);
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
    public Flux<ItemCardapio> findAll() {
        return findAllBy(null, null);
    }

    @Override
    public Mono<ItemCardapio> findById(Long id) {
        return createQuery(null, where("id").is(id)).one();
    }

    private ItemCardapio process(Row row, RowMetadata metadata) {
        ItemCardapio entity = itemcardapioMapper.apply(row, "e");
        entity.setCardapio(cardapioMapper.apply(row, "cardapio"));
        return entity;
    }

    @Override
    public <S extends ItemCardapio> Mono<S> insert(S entity) {
        return entityManager.insert(entity);
    }

    @Override
    public <S extends ItemCardapio> Mono<S> save(S entity) {
        if (entity.getId() == null) {
            return insert(entity);
        } else {
            return update(entity)
                .map(
                    numberOfUpdates -> {
                        if (numberOfUpdates.intValue() <= 0) {
                            throw new IllegalStateException("Unable to update ItemCardapio with id = " + entity.getId());
                        }
                        return entity;
                    }
                );
        }
    }

    @Override
    public Mono<Integer> update(ItemCardapio entity) {
        //fixme is this the proper way?
        return r2dbcEntityTemplate.update(entity).thenReturn(1);
    }
}

class ItemCardapioSqlHelper {

    static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("categoria", table, columnPrefix + "_categoria"));
        columns.add(Column.aliased("nome", table, columnPrefix + "_nome"));
        columns.add(Column.aliased("descricao", table, columnPrefix + "_descricao"));
        columns.add(Column.aliased("valor", table, columnPrefix + "_valor"));

        columns.add(Column.aliased("cardapio_id", table, columnPrefix + "_cardapio_id"));
        return columns;
    }
}
