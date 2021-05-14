package br.com.qrdapio.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A Restaurante.
 */
@Table("restaurante")
public class Restaurante implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @NotNull(message = "must not be null")
    @Column("nome")
    private String nome;

    @Transient
    @JsonIgnoreProperties(value = { "itemCardapios", "restaurante" }, allowSetters = true)
    private Set<Cardapio> cardapios = new HashSet<>();

    @Transient
    @JsonIgnoreProperties(value = { "itemPedidos", "restaurante" }, allowSetters = true)
    private Set<Pedido> pedidos = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Restaurante id(Long id) {
        this.id = id;
        return this;
    }

    public String getNome() {
        return this.nome;
    }

    public Restaurante nome(String nome) {
        this.nome = nome;
        return this;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Set<Cardapio> getCardapios() {
        return this.cardapios;
    }

    public Restaurante cardapios(Set<Cardapio> cardapios) {
        this.setCardapios(cardapios);
        return this;
    }

    public Restaurante addCardapio(Cardapio cardapio) {
        this.cardapios.add(cardapio);
        cardapio.setRestaurante(this);
        return this;
    }

    public Restaurante removeCardapio(Cardapio cardapio) {
        this.cardapios.remove(cardapio);
        cardapio.setRestaurante(null);
        return this;
    }

    public void setCardapios(Set<Cardapio> cardapios) {
        if (this.cardapios != null) {
            this.cardapios.forEach(i -> i.setRestaurante(null));
        }
        if (cardapios != null) {
            cardapios.forEach(i -> i.setRestaurante(this));
        }
        this.cardapios = cardapios;
    }

    public Set<Pedido> getPedidos() {
        return this.pedidos;
    }

    public Restaurante pedidos(Set<Pedido> pedidos) {
        this.setPedidos(pedidos);
        return this;
    }

    public Restaurante addPedido(Pedido pedido) {
        this.pedidos.add(pedido);
        pedido.setRestaurante(this);
        return this;
    }

    public Restaurante removePedido(Pedido pedido) {
        this.pedidos.remove(pedido);
        pedido.setRestaurante(null);
        return this;
    }

    public void setPedidos(Set<Pedido> pedidos) {
        if (this.pedidos != null) {
            this.pedidos.forEach(i -> i.setRestaurante(null));
        }
        if (pedidos != null) {
            pedidos.forEach(i -> i.setRestaurante(this));
        }
        this.pedidos = pedidos;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Restaurante)) {
            return false;
        }
        return id != null && id.equals(((Restaurante) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Restaurante{" +
            "id=" + getId() +
            ", nome='" + getNome() + "'" +
            "}";
    }
}
