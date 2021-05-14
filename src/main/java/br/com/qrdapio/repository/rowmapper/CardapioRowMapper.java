package br.com.qrdapio.repository.rowmapper;

import br.com.qrdapio.domain.Cardapio;
import br.com.qrdapio.service.ColumnConverter;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Cardapio}, with proper type conversions.
 */
@Service
public class CardapioRowMapper implements BiFunction<Row, String, Cardapio> {

    private final ColumnConverter converter;

    public CardapioRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Cardapio} stored in the database.
     */
    @Override
    public Cardapio apply(Row row, String prefix) {
        Cardapio entity = new Cardapio();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setNome(converter.fromRow(row, prefix + "_nome", String.class));
        entity.setRestauranteId(converter.fromRow(row, prefix + "_restaurante_id", Long.class));
        return entity;
    }
}
