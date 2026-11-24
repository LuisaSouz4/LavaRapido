package lavarapido.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Agendamento guarda também o preço e a duração vigentes no momento da reserva. */
public class Agendamento implements Serializable {
    private static final long serialVersionUID = 1L;
    private long id;
    private Cliente cliente;
    private TipoServico tipoServico;
    private LocalDateTime dataHora;
    private BigDecimal preco;
    private int duracaoMinutos;
    private StatusAgendamento status = StatusAgendamento.AGENDADO;

    public Agendamento() { }
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public TipoServico getTipoServico() { return tipoServico; }
    public void setTipoServico(TipoServico tipoServico) { this.tipoServico = tipoServico; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public int getDuracaoMinutos() { return duracaoMinutos; }
    public void setDuracaoMinutos(int duracaoMinutos) { this.duracaoMinutos = duracaoMinutos; }
    public StatusAgendamento getStatus() { return status; }
    public void setStatus(StatusAgendamento status) { this.status = status; }
    public boolean isAtivo() { return status == StatusAgendamento.AGENDADO; }
    public LocalDateTime getFim() { return dataHora.plusMinutes(duracaoMinutos); }

    /** Verifica se este atendimento se sobrepõe ao intervalo informado. */
    public boolean sobrepoe(LocalDateTime inicio, LocalDateTime fim) {
        return dataHora.isBefore(fim) && inicio.isBefore(getFim());
    }
}
