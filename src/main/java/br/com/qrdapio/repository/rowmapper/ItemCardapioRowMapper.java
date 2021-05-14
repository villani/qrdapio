package br.com.qrdapio.repository.rowmapper;

import br.com.qrdapio.domain.ItemCardapio;
import br.com.qrdapio.domain.enumeration.Categoria;
import br.com.qrdapio.service.ColumnConverter;
import io.r2dbc.spi.Row;
import java.math.BigDecimal;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link ItemCardapio}, with proper type conversions.
 */
@Service
public class ItemCardapioRowMapper implements BiFunction<Row, String, ItemCardapio> {

    private final ColumnConverter converter;

    public ItemCardapioRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link ItemCardapio} stored in the database.
     */
    @Override
    public ItemCardapio apply(Row row, String prefix) {
        ItemCardapio entity = new ItemCardapio();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setCategoria(converter.fromRow(row, prefix + "_categoria", Categoria.class));
        entity.setNome(converter.fromRow(row, prefix + "_nome", String.class));
        entity.setDescricao(converter.fromRow(row, prefix + "_descricao", String.class));
        entity.setValor(converter.fromRow(row, prefix + "_valor", BigDecimal.class));
        entity.setCardapioId(converter.fromRow(row, prefix + "_cardapio_id", Long.class));
        return entity;
    }
}
