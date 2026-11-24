package lavarapido.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/** Tipo de serviço oferecido pelo lava-rápido. */
public class TipoServico implements Serializable {
    private static final long serialVersionUID = 1L;
    private long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private int duracaoMinutos;

    public TipoServico() { }
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public int getDuracaoMinutos() { return duracaoMinutos; }
    public void setDuracaoMinutos(int duracaoMinutos) { this.duracaoMinutos = duracaoMinutos; }

    @Override public boolean equals(Object o) {
        return o instanceof TipoServico t && t.id == id;
    }
    @Override public int hashCode() { return Objects.hash(id); }
    @Override public String toString() { return nome + " (" + duracaoMinutos + " min)"; }
}
