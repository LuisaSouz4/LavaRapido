package lavarapido.controller;

import lavarapido.model.Cliente;
import lavarapido.service.ClienteService;
import lavarapido.service.ValidacaoException;
import lavarapido.view.ClienteView;

/** Liga a ClienteView ao ClienteService: trata os eventos da tela e mostra os erros de validação. */
public class ClienteController {
    private final ClienteView view;
    private final ClienteService service;

    /**
     * Registra os eventos dos botões e da tabela e carrega a lista inicial.
     */
    public ClienteController(ClienteView view, ClienteService service) {
        this.view = view;
        this.service = service;
        view.aoSalvar(this::salvar);
        view.aoNovo(view::limparFormulario);
        view.aoExcluir(this::excluir);
        view.aoSelecionar(this::selecionar);
        atualizarTabela();
    }

    public void atualizarTabela() { view.mostrarClientes(service.listar()); }

    /**
     * Botão Cadastrar/Atualizar: decide entre criar e editar conforme haja um cliente selecionado.
     */
    private void salvar() {
        try {
            if (view.getIdEmEdicao() < 0) {
                service.cadastrar(view.getNome(), view.getCpf(), view.getTelefone(), view.getEmail(), view.getPlaca());
            } else {
                service.editar(view.getIdEmEdicao(), view.getNome(), view.getCpf(), view.getTelefone(), view.getEmail(), view.getPlaca());
            }
            view.limparFormulario();
            atualizarTabela();
        } catch (ValidacaoException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    /**
     * Botão Excluir: pede confirmação e remove o cliente em edição.
     */
    private void excluir() {
        long id = view.getIdEmEdicao();
        if (id < 0 || !view.confirmar("Excluir o cliente selecionado?")) return;
        try {
            service.excluir(id);
            view.limparFormulario();
            atualizarTabela();
        } catch (ValidacaoException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    /**
     * Ao clicar numa linha da tabela, carrega o cliente no formulário para edição.
     */
    private void selecionar() {
        long id = view.getIdSelecionado();
        if (id < 0) return;
        Cliente c = service.buscar(id).orElse(null);
        if (c != null) view.preencher(c);
    }
}
