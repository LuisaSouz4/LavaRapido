package lavarapido.model;

import java.io.Serializable;
import java.util.Objects;

/** Cliente do lava rápido. */
public class Cliente implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String nome;
    private String cpf;          // somente dígitos
    private String telefone;     // somente dígitos
    private String email;
    private String placaVeiculo; // maiúscula, sem hífen

    public Cliente() { }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPlacaVeiculo() { return placaVeiculo; }
    public void setPlacaVeiculo(String placaVeiculo) { this.placaVeiculo = placaVeiculo; }

    /** Dois clientes são o mesmo se tiverem o mesmo id (necessário para o JComboBox achar o item). */
    @Override public boolean equals(Object o) {
        return o instanceof Cliente c && c.id == id;
    }
    @Override public int hashCode() { return Objects.hash(id); }
    /** Texto exibido na lista suspensa da agenda. */
    @Override public String toString() { return nome + " (" + placaVeiculo + ")"; }
}
