package lavarapido.view;

import lavarapido.model.TipoServico;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/** Tela de tipos de serviço: formulário + tabela. Não contém regra de negócio. */
public class TipoServicoView extends PainelBase {
    private static final long serialVersionUID = 1L;
    static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    private final JTextField txtNome = new JTextField(28);
    private final JTextField txtDescricao = new JTextField(40);
    private final JTextField txtPreco = new JTextField(10);
    private final JTextField txtDuracao = new JTextField(6);
    private final JButton btnSalvar = new JButton("Cadastrar");
    private final JButton btnNovo = new JButton("Novo");
    private final JButton btnExcluir = new JButton("Excluir");
    private final DefaultTableModel modelo = modeloSomenteLeitura("ID", "Nome", "Descrição", "Preço", "Duração (min)");
    private final JTable tabela = criarTabela(modelo);
    private long idEmEdicao = -1;

    public TipoServicoView() {
        addCampo("Nome:", txtNome);
        addCampo("Descrição:", txtDescricao);
        addCampo("Preço (R$):", txtPreco);
        addCampo("Duração (minutos):", txtDuracao);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botoes.add(btnSalvar);
        botoes.add(btnNovo);
        botoes.add(btnExcluir);

        JPanel topo = new JPanel(new BorderLayout());
        topo.setBorder(BorderFactory.createTitledBorder("Tipo de serviço"));
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

    public String getNome() { return txtNome.getText(); }
    public String getDescricao() { return txtDescricao.getText(); }
    public String getPreco() { return txtPreco.getText(); }
    public String getDuracao() { return txtDuracao.getText(); }
    public long getIdEmEdicao() { return idEmEdicao; }
    public long getIdSelecionado() { return idSelecionado(tabela); }

    /**
     * Coloca o serviço nos campos e muda o botão para "Atualizar".
     */
    public void preencher(TipoServico t) {
        idEmEdicao = t.getId();
        txtNome.setText(t.getNome());
        txtDescricao.setText(t.getDescricao());
        txtPreco.setText(t.getPreco().toPlainString().replace('.', ','));
        txtDuracao.setText(String.valueOf(t.getDuracaoMinutos()));
        btnSalvar.setText("Atualizar");
        btnExcluir.setEnabled(true);
    }

    public void limparFormulario() {
        idEmEdicao = -1;
        txtNome.setText("");
        txtDescricao.setText("");
        txtPreco.setText("");
        txtDuracao.setText("");
        tabela.clearSelection();
        btnSalvar.setText("Cadastrar");
        btnExcluir.setEnabled(false);
        txtNome.requestFocusInWindow();
    }

    /**
     * Substitui o conteúdo da tabela (preço formatado em R$).
     */
    public void mostrarTipos(List<TipoServico> tipos) {
        modelo.setRowCount(0);
        for (TipoServico t : tipos) {
            modelo.addRow(new Object[]{t.getId(), t.getNome(), t.getDescricao(), MOEDA.format(t.getPreco()), t.getDuracaoMinutos()});
        }
    }
}
