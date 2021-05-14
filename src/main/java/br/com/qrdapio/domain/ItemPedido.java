package br.com.qrdapio.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A ItemPedido.
 */
@Table("item_pedido")
public class ItemPedido implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Min(value = 1)
    @Column("quantidade")
    private Integer quantidade;

    private Long itemId;

    @Transient
    private ItemCardapio item;

    @JsonIgnoreProperties(value = { "itemPedidos", "restaurante" }, allowSetters = true)
    @Transient
    private Pedido pedido;

    @Column("pedido_id")
    private Long pedidoId;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ItemPedido id(Long id) {
        this.id = id;
        return this;
    }

    public Integer getQuantidade() {
        return this.quantidade;
    }

    public ItemPedido quantidade(Integer quantidade) {
        this.quantidade = quantidade;
        return this;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public ItemCardapio getItem() {
        return this.item;
    }

    public ItemPedido item(ItemCardapio itemCardapio) {
        this.setItem(itemCardapio);
        this.itemId = itemCardapio != null ? itemCardapio.getId() : null;
        return this;
    }

    public void setItem(ItemCardapio itemCardapio) {
        this.item = itemCardapio;
        this.itemId = itemCardapio != null ? itemCardapio.getId() : null;
    }

    public Long getItemId() {
        return this.itemId;
    }

    public void setItemId(Long itemCardapio) {
        this.itemId = itemCardapio;
    }

    public Pedido getPedido() {
        return this.pedido;
    }

    public ItemPedido pedido(Pedido pedido) {
        this.setPedido(pedido);
        this.pedidoId = pedido != null ? pedido.getId() : null;
        return this;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
        this.pedidoId = pedido != null ? pedido.getId() : null;
    }

    public Long getPedidoId() {
        return this.pedidoId;
    }

    public void setPedidoId(Long pedido) {
        this.pedidoId = pedido;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemPedido)) {
            return false;
        }
        return id != null && id.equals(((ItemPedido) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ItemPedido{" +
            "id=" + getId() +
            ", quantidade=" + getQuantidade() +
            "}";
    }
}
