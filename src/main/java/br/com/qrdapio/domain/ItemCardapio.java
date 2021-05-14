package br.com.qrdapio.domain;

import br.com.qrdapio.domain.enumeration.Categoria;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A ItemCardapio.
 */
@Table("item_cardapio")
public class ItemCardapio implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @NotNull(message = "must not be null")
    @Column("categoria")
    private Categoria categoria;

    @NotNull(message = "must not be null")
    @Column("nome")
    private String nome;

    @NotNull(message = "must not be null")
    @Column("descricao")
    private String descricao;

    @Column("valor")
    private BigDecimal valor;

    @JsonIgnoreProperties(value = { "itemCardapios", "restaurante" }, allowSetters = true)
    @Transient
    private Cardapio cardapio;

    @Column("cardapio_id")
    private Long cardapioId;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ItemCardapio id(Long id) {
        this.id = id;
        return this;
    }

    public Categoria getCategoria() {
        return this.categoria;
    }

    public ItemCardapio categoria(Categoria categoria) {
        this.categoria = categoria;
        return this;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getNome() {
        return this.nome;
    }

    public ItemCardapio nome(String nome) {
        this.nome = nome;
        return this;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return this.descricao;
    }

    public ItemCardapio descricao(String descricao) {
        this.descricao = descricao;
        return this;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return this.valor;
    }

    public ItemCardapio valor(BigDecimal valor) {
        this.valor = valor != null ? valor.stripTrailingZeros() : null;
        return this;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor != null ? valor.stripTrailingZeros() : null;
    }

    public Cardapio getCardapio() {
        return this.cardapio;
    }

    public ItemCardapio cardapio(Cardapio cardapio) {
        this.setCardapio(cardapio);
        this.cardapioId = cardapio != null ? cardapio.getId() : null;
        return this;
    }

    public void setCardapio(Cardapio cardapio) {
        this.cardapio = cardapio;
        this.cardapioId = cardapio != null ? cardapio.getId() : null;
    }

    public Long getCardapioId() {
        return this.cardapioId;
    }

    public void setCardapioId(Long cardapio) {
        this.cardapioId = cardapio;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemCardapio)) {
            return false;
        }
        return id != null && id.equals(((ItemCardapio) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ItemCardapio{" +
            "id=" + getId() +
            ", categoria='" + getCategoria() + "'" +
            ", nome='" + getNome() + "'" +
            ", descricao='" + getDescricao() + "'" +
            ", valor=" + getValor() +
            "}";
    }
}
