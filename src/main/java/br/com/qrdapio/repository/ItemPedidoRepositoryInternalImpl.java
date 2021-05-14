package br.com.qrdapio.repository;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import br.com.qrdapio.domain.ItemPedido;
import br.com.qrdapio.repository.rowmapper.ItemCardapioRowMapper;
import br.com.qrdapio.repository.rowmapper.ItemPedidoRowMapper;
import br.com.qrdapio.repository.rowmapper.PedidoRowMapper;
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
 * Spring Data SQL reactive custom repository implementation for the ItemPedido entity.
 */
@SuppressWarnings("unused")
class ItemPedidoRepositoryInternalImpl implements ItemPedidoRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final ItemCardapioRowMapper itemcardapioMapper;
    private final PedidoRowMapper pedidoMapper;
    private final ItemPedidoRowMapper itempedidoMapper;

    private static final Table entityTable = Table.aliased("item_pedido", EntityManager.ENTITY_ALIAS);
    private static final Table itemTable = Table.aliased("item_cardapio", "item");
    private static final Table pedidoTable = Table.aliased("pedido", "pedido");

    public ItemPedidoRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        ItemCardapioRowMapper itemcardapioMapper,
        PedidoRowMapper pedidoMapper,
        ItemPedidoRowMapper itempedidoMapper
    ) {
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.itemcardapioMapper = itemcardapioMapper;
        this.pedidoMapper = pedidoMapper;
        this.itempedidoMapper = itempedidoMapper;
    }

    @Override
    public Flux<ItemPedido> findAllBy(Pageable pageable) {
        return findAllBy(pageable, null);
    }

    @Override
    public Flux<ItemPedido> findAllBy(Pageable pageable, Criteria criteria) {
        return createQuery(pageable, criteria).all();
    }

    RowsFetchSpec<ItemPedido> createQuery(Pageable pageable, Criteria criteria) {
        List<Expression> columns = ItemPedidoSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(ItemCardapioSqlHelper.getColumns(itemTable, "item"));
        columns.addAll(PedidoSqlHelper.getColumns(pedidoTable, "pedido"));
        SelectFromAndJoinCondition selectFrom = Select
            .builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(itemTable)
            .on(Column.create("item_id", entityTable))
            .equals(Column.create("id", itemTable))
            .leftOuterJoin(pedidoTable)
            .on(Column.create("pedido_id", entityTable))
            .equals(Column.create("id", pedidoTable));

        String select = entityManager.createSelect(selectFrom, ItemPedido.class, pageable, criteria);
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
    public Flux<ItemPedido> findAll() {
        return findAllBy(null, null);
    }

    @Override
    public Mono<ItemPedido> findById(Long id) {
        return createQuery(null, where("id").is(id)).one();
    }

    private ItemPedido process(Row row, RowMetadata metadata) {
        ItemPedido entity = itempedidoMapper.apply(row, "e");
        entity.setItem(itemcardapioMapper.apply(row, "item"));
        entity.setPedido(pedidoMapper.apply(row, "pedido"));
        return entity;
    }

    @Override
    public <S extends ItemPedido> Mono<S> insert(S entity) {
        return entityManager.insert(entity);
    }

    @Override
    public <S extends ItemPedido> Mono<S> save(S entity) {
        if (entity.getId() == null) {
            return insert(entity);
        } else {
            return update(entity)
                .map(
                    numberOfUpdates -> {
                        if (numberOfUpdates.intValue() <= 0) {
                            throw new IllegalStateException("Unable to update ItemPedido with id = " + entity.getId());
                        }
                        return entity;
                    }
                );
        }
    }

    @Override
    public Mono<Integer> update(ItemPedido entity) {
        //fixme is this the proper way?
        return r2dbcEntityTemplate.update(entity).thenReturn(1);
    }
}

class ItemPedidoSqlHelper {

    static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("quantidade", table, columnPrefix + "_quantidade"));

        columns.add(Column.aliased("item_id", table, columnPrefix + "_item_id"));
        columns.add(Column.aliased("pedido_id", table, columnPrefix + "_pedido_id"));
        return columns;
    }
}
