package lavarapido.controller;

import lavarapido.model.Agendamento;
import lavarapido.service.AgendamentoService;
import lavarapido.service.ClienteService;
import lavarapido.service.TipoServicoService;
import lavarapido.service.ValidacaoException;
import lavarapido.view.AgendamentoView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;

/**
 * Controla as ações da tela de agendamentos.
 */
public class AgendamentoController {
    private static final DateTimeFormatter DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter HORA =
            DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private final AgendamentoView view;
    private final AgendamentoService service;
    private final ClienteService clienteService;
    private final TipoServicoService tipoService;
    /** true quando o usuário clicou em "Ver todos"; false usa a data e o modo (dia/semana) do filtro. */
    private boolean verTodos = false;

    public AgendamentoController(AgendamentoView view, AgendamentoService service,
                                 ClienteService clienteService, TipoServicoService tipoService) {
        this.view = view;
        this.service = service;
        this.clienteService = clienteService;
        this.tipoService = tipoService;
        view.setFiltroData(LocalDate.now());
        view.aoSalvar(this::salvar);
        view.aoNovo(view::limparFormulario);
        view.aoCancelarAgendamento(this::cancelar);
        view.aoConcluir(this::concluir);
        view.aoFiltrar(this::filtrar);
        view.aoVerTodos(() -> { verTodos = true; atualizarTabela(); });
        view.aoSelecionar(this::selecionar);
        atualizarCombos();
        atualizarTabela();
    }

    /** Recarrega as listas de clientes e serviços (chamado ao voltar para a aba da agenda). */
    public void atualizarCombos() {
        view.setClientes(clienteService.listar());
        view.setTipos(tipoService.listar());
    }

    /**
     * Recarrega a tabela da agenda conforme o filtro atual.
     */
    public void atualizarTabela() {
        try {
            List<Agendamento> lista;
            if (verTodos) {
                lista = service.listarTodos();
            } else {
                LocalDate ref = parseData(view.getFiltroData());
                lista = view.isFiltroSemana() ? service.listarPorSemana(ref) : service.listarPorDia(ref);
            }
            view.mostrarAgendamentos(lista);
        } catch (ValidacaoException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    private void filtrar() { verTodos = false; atualizarTabela(); }

    /**
     * Botão Agendar/Salvar alterações: monta a data/hora e chama o serviço (cria ou edita).
     */
    private void salvar() {
        try {
            LocalDateTime dh = LocalDateTime.of(parseData(view.getData()), parseHora(view.getHora()));
            if (view.getIdEmEdicao() < 0) {
                service.agendar(view.getCliente(), view.getTipo(), dh);
            } else {
                service.editar(view.getIdEmEdicao(), view.getCliente(), view.getTipo(), dh);
            }
            view.limparFormulario();
            atualizarTabela();
        } catch (ValidacaoException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    /** Botão Cancelar agendamento: pede confirmação e muda o status para CANCELADO. */
    private void cancelar() {
        long id = view.getIdEmEdicao();
        if (id < 0 || !view.confirmar("Cancelar o agendamento selecionado?")) return;
        try {
            service.cancelar(id);
            view.limparFormulario();
            atualizarTabela();
        } catch (ValidacaoException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    /** Botão Concluir: muda o status para CONCLUIDO. */
    private void concluir() {
        long id = view.getIdEmEdicao();
        if (id < 0) return;
        try {
            service.concluir(id);
            view.limparFormulario();
            atualizarTabela();
        } catch (ValidacaoException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    /**
     * Ao clicar numa linha, carrega o agendamento no formulário.
     */
    private void selecionar() {
        long id = view.getIdSelecionado();
        if (id < 0) return;
        service.buscar(id).ifPresent(view::preencher);
    }

    /**
     * Converte "dd/mm/aaaa" (modo estrito: 31/02 é recusado).
     */
    private static LocalDate parseData(String t) {
        try {
            return LocalDate.parse(t.trim(), DATA);
        } catch (DateTimeParseException e) {
            throw new ValidacaoException("Data inválida. Use o formato dd/mm/aaaa.");
        }
    }

    /**
     * Converte "hh:mm" em 24 horas.
     */
    private static LocalTime parseHora(String t) {
        try {
            return LocalTime.parse(t.trim(), HORA);
        } catch (DateTimeParseException e) {
            throw new ValidacaoException("Hora inválida. Use o formato hh:mm (24 horas).");
        }
    }
}
