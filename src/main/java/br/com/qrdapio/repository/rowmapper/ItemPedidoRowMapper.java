package br.com.qrdapio.repository.rowmapper;

import br.com.qrdapio.domain.ItemPedido;
import br.com.qrdapio.service.ColumnConverter;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link ItemPedido}, with proper type conversions.
 */
@Service
public class ItemPedidoRowMapper implements BiFunction<Row, String, ItemPedido> {

    private final ColumnConverter converter;

    public ItemPedidoRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link ItemPedido} stored in the database.
     */
    @Override
    public ItemPedido apply(Row row, String prefix) {
        ItemPedido entity = new ItemPedido();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setQuantidade(converter.fromRow(row, prefix + "_quantidade", Integer.class));
        entity.setItemId(converter.fromRow(row, prefix + "_item_id", Long.class));
        entity.setPedidoId(converter.fromRow(row, prefix + "_pedido_id", Long.class));
        return entity;
    }
}
