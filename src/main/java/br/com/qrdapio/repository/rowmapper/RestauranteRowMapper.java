package br.com.qrdapio.repository.rowmapper;

import br.com.qrdapio.domain.Restaurante;
import br.com.qrdapio.service.ColumnConverter;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Restaurante}, with proper type conversions.
 */
@Service
public class RestauranteRowMapper implements BiFunction<Row, String, Restaurante> {

    private final ColumnConverter converter;

    public RestauranteRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Restaurante} stored in the database.
     */
    @Override
    public Restaurante apply(Row row, String prefix) {
        Restaurante entity = new Restaurante();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setNome(converter.fromRow(row, prefix + "_nome", String.class));
        return entity;
    }
}
