package br.com.qrdapio.repository.rowmapper;

import br.com.qrdapio.domain.Pedido;
import br.com.qrdapio.domain.enumeration.FormaPagamento;
import br.com.qrdapio.service.ColumnConverter;
import io.r2dbc.spi.Row;
import java.time.ZonedDateTime;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Pedido}, with proper type conversions.
 */
@Service
public class PedidoRowMapper implements BiFunction<Row, String, Pedido> {

    private final ColumnConverter converter;

    public PedidoRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Pedido} stored in the database.
     */
    @Override
    public Pedido apply(Row row, String prefix) {
        Pedido entity = new Pedido();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setFormaPagamento(converter.fromRow(row, prefix + "_forma_pagamento", FormaPagamento.class));
        entity.setDataHora(converter.fromRow(row, prefix + "_data_hora", ZonedDateTime.class));
        entity.setSenha(converter.fromRow(row, prefix + "_senha", Integer.class));
        entity.setRestauranteId(converter.fromRow(row, prefix + "_restaurante_id", Long.class));
        return entity;
    }
}
