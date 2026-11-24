package lavarapido.controller;

import lavarapido.model.TipoServico;
import lavarapido.service.TipoServicoService;
import lavarapido.service.ValidacaoException;
import lavarapido.view.TipoServicoView;

/** Liga a TipoServicoView ao TipoServicoService (mesmo fluxo do ClienteController). */
public class TipoServicoController {
    private final TipoServicoView view;
    private final TipoServicoService service;

    public TipoServicoController(TipoServicoView view, TipoServicoService service) {
        this.view = view;
        this.service = service;
        view.aoSalvar(this::salvar);
        view.aoNovo(view::limparFormulario);
        view.aoExcluir(this::excluir);
        view.aoSelecionar(this::selecionar);
        atualizarTabela();
    }

    public void atualizarTabela() { view.mostrarTipos(service.listar()); }

    /**
     * Botão Cadastrar/Atualizar.
     */
    private void salvar() {
        try {
            if (view.getIdEmEdicao() < 0) {
                service.cadastrar(view.getNome(), view.getDescricao(), view.getPreco(), view.getDuracao());
            } else {
                service.editar(view.getIdEmEdicao(), view.getNome(), view.getDescricao(), view.getPreco(), view.getDuracao());
            }
            view.limparFormulario();
            atualizarTabela();
        } catch (ValidacaoException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    /**
     * Botão Excluir (com confirmação).
     */
    private void excluir() {
        long id = view.getIdEmEdicao();
        if (id < 0 || !view.confirmar("Excluir o tipo de serviço selecionado?")) return;
        try {
            service.excluir(id);
            view.limparFormulario();
            atualizarTabela();
        } catch (ValidacaoException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    /**
     * Carrega a linha selecionada no formulário.
     */
    private void selecionar() {
        long id = view.getIdSelecionado();
        if (id < 0) return;
        TipoServico t = service.buscar(id).orElse(null);
        if (t != null) view.preencher(t);
    }
}
