package lavarapido.service;

import lavarapido.model.Agendamento;
import lavarapido.model.Cliente;
import lavarapido.model.StatusAgendamento;
import lavarapido.model.TipoServico;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Regras para criar, editar e consultar os agendamentos.
 */
public class AgendamentoService {
    private final Banco banco;

    public AgendamentoService(Banco banco) { this.banco = banco; }

    public Optional<Agendamento> buscar(long id) {
        return banco.dados().agendamentos.stream().filter(a -> a.getId() == id).findFirst();
    }

    public List<Agendamento> listarTodos() { return filtrar(a -> true); }

    public List<Agendamento> listarPorDia(LocalDate dia) {
        return filtrar(a -> a.getDataHora().toLocalDate().equals(dia));
    }

    /** Semana de segunda a domingo que contém o dia informado. */
    public List<Agendamento> listarPorSemana(LocalDate dia) {
        LocalDate ini = dia.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate fim = ini.plusDays(6);
        return filtrar(a -> {
            LocalDate d = a.getDataHora().toLocalDate();
            return !d.isBefore(ini) && !d.isAfter(fim);
        });
    }

    /**
     * Cria um agendamento depois de conferir os dados.
     */
    public Agendamento agendar(Cliente cliente, TipoServico tipo, LocalDateTime dataHora) {
        validar(cliente, tipo, dataHora, null);
        Agendamento a = new Agendamento();
        a.setId(banco.proximoId());
        a.setCliente(cliente);
        a.setTipoServico(tipo);
        a.setDataHora(dataHora);
        copiarDadosDoServico(a, tipo);
        banco.dados().agendamentos.add(a);
        banco.salvar();
        return a;
    }

    /**
     * Altera os dados de um agendamento ainda ativo.
     */
    public Agendamento editar(long id, Cliente cliente, TipoServico tipo, LocalDateTime dataHora) {
        Agendamento a = buscar(id).orElseThrow(() -> new ValidacaoException("Agendamento não encontrado."));
        if (!a.isAtivo()) throw new ValidacaoException("Somente agendamentos com status Agendado podem ser editados.");
        validar(cliente, tipo, dataHora, a);
        a.setCliente(cliente);
        a.setTipoServico(tipo);
        a.setDataHora(dataHora);
        copiarDadosDoServico(a, tipo);
        banco.salvar();
        return a;
    }

    /**
     * Cancela o agendamento.
     */
    public void cancelar(long id) {
        Agendamento a = buscar(id).orElseThrow(() -> new ValidacaoException("Agendamento não encontrado."));
        if (!a.isAtivo()) throw new ValidacaoException("Este agendamento já está " + a.getStatus().toString().toLowerCase() + ".");
        a.setStatus(StatusAgendamento.CANCELADO);
        banco.salvar();
    }

    /**
     * Conclui o agendamento.
     */
    public void concluir(long id) {
        Agendamento a = buscar(id).orElseThrow(() -> new ValidacaoException("Agendamento não encontrado."));
        if (!a.isAtivo()) throw new ValidacaoException("Este agendamento já está " + a.getStatus().toString().toLowerCase() + ".");
        a.setStatus(StatusAgendamento.CONCLUIDO);
        banco.salvar();
    }

    /**
     * Regras comuns ao agendar e ao editar. O parâmetro 'atual' é ignorado na checagem de conflito,
     * para que editar um agendamento não conflite com ele mesmo.
     */
    private void validar(Cliente cliente, TipoServico tipo, LocalDateTime dataHora, Agendamento atual) {
        if (cliente == null) throw new ValidacaoException("Selecione um cliente.");
        if (tipo == null) throw new ValidacaoException("Selecione o tipo de serviço.");
        if (dataHora == null) throw new ValidacaoException("Informe a data e a hora.");
        if (dataHora.isBefore(LocalDateTime.now())) throw new ValidacaoException("Não é possível agendar em data/hora passada.");
        if (tipo.getDuracaoMinutos() < 5 || tipo.getDuracaoMinutos() > 480)
            throw new ValidacaoException("O serviço possui duração inválida.");
        LocalDateTime fim = dataHora.plusMinutes(tipo.getDuracaoMinutos());
        boolean conflito = banco.dados().agendamentos.stream()
                .anyMatch(o -> o != atual && o.isAtivo() && o.sobrepoe(dataHora, fim));
        if (conflito) throw new ValidacaoException("O box estará ocupado nesse intervalo. Escolha outro horário.");
    }

    /** Copia preço e duração para manter o histórico mesmo se o tipo de serviço mudar. */
    private void copiarDadosDoServico(Agendamento a, TipoServico tipo) {
        a.setPreco(tipo.getPreco());
        a.setDuracaoMinutos(tipo.getDuracaoMinutos());
    }

    private List<Agendamento> filtrar(Predicate<Agendamento> p) {
        return banco.dados().agendamentos.stream().filter(p)
                .sorted(Comparator.comparing(Agendamento::getDataHora)).toList();
    }
}
