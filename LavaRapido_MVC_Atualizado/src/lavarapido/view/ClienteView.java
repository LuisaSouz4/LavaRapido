package lavarapido.view;

import lavarapido.model.Cliente;
import lavarapido.service.Validador;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Tela de clientes: formulário + tabela. Não contém regra de negócio. */
public class ClienteView extends PainelBase {
    private static final long serialVersionUID = 1L;

    private final JTextField txtNome = new JTextField(28);
    private final JTextField txtCpf = new JTextField(14);
    private final JTextField txtTelefone = new JTextField(14);
    private final JTextField txtEmail = new JTextField(28);
    private final JTextField txtPlaca = new JTextField(9);
    private final JButton btnSalvar = new JButton("Cadastrar");
    private final JButton btnNovo = new JButton("Novo");
    private final JButton btnExcluir = new JButton("Excluir");
    private final DefaultTableModel modelo = modeloSomenteLeitura("ID", "Nome", "CPF", "Telefone", "E-mail", "Placa");
    private final JTable tabela = criarTabela(modelo);
    private long idEmEdicao = -1;

    public ClienteView() {
        addCampo("Nome:", txtNome);
        addCampo("CPF:", txtCpf);
        addCampo("Telefone:", txtTelefone);
        addCampo("E-mail:", txtEmail);
        addCampo("Placa do veículo:", txtPlaca);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botoes.add(btnSalvar);
        botoes.add(btnNovo);
        botoes.add(btnExcluir);

        JPanel topo = new JPanel(new BorderLayout());
        topo.setBorder(BorderFactory.createTitledBorder("Dados do cliente"));
        topo.add(form, BorderLayout.CENTER);
        topo.add(botoes, BorderLayout.SOUTH);

        add(topo, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        btnExcluir.setEnabled(false);
        tabela.getColumnModel().getColumn(0).setMaxWidth(60);
    }

    
    // Eventos usados pelo Controller.
    public void aoSalvar(Runnable r) { btnSalvar.addActionListener(e -> r.run()); }
    public void aoNovo(Runnable r) { btnNovo.addActionListener(e -> r.run()); }
    public void aoExcluir(Runnable r) { btnExcluir.addActionListener(e -> r.run()); }
    public void aoSelecionar(Runnable r) {
        tabela.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) r.run(); });
    }

    // --- leitura/escrita dos campos ---
    public String getNome() { return txtNome.getText(); }
    public String getCpf() { return txtCpf.getText(); }
    public String getTelefone() { return txtTelefone.getText(); }
    public String getEmail() { return txtEmail.getText(); }
    public String getPlaca() { return txtPlaca.getText(); }
    public long getIdEmEdicao() { return idEmEdicao; }
    public long getIdSelecionado() { return idSelecionado(tabela); }

    /**
     * Coloca o cliente nos campos e muda o botão para "Atualizar".
     */
    public void preencher(Cliente c) {
        idEmEdicao = c.getId();
        txtNome.setText(c.getNome());
        txtCpf.setText(Validador.formatarCpf(c.getCpf()));
        txtTelefone.setText(Validador.formatarTelefone(c.getTelefone()));
        txtEmail.setText(c.getEmail());
        txtPlaca.setText(c.getPlacaVeiculo());
        btnSalvar.setText("Atualizar");
        btnExcluir.setEnabled(true);
    }

    /**
     * Volta ao modo "novo cadastro".
     */
    public void limparFormulario() {
        idEmEdicao = -1;
        for (JTextField f : new JTextField[]{txtNome, txtCpf, txtTelefone, txtEmail, txtPlaca}) f.setText("");
        tabela.clearSelection();
        btnSalvar.setText("Cadastrar");
        btnExcluir.setEnabled(false);
        txtNome.requestFocusInWindow();
    }

    /**
     * Substitui o conteúdo da tabela.
     */
    public void mostrarClientes(List<Cliente> clientes) {
        modelo.setRowCount(0);
        for (Cliente c : clientes) {
            modelo.addRow(new Object[]{c.getId(), c.getNome(), Validador.formatarCpf(c.getCpf()),
                    Validador.formatarTelefone(c.getTelefone()), c.getEmail(), c.getPlacaVeiculo()});
        }
    }
}
