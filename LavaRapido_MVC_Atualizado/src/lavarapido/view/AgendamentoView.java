package lavarapido.view;

import lavarapido.model.Agendamento;
import lavarapido.model.Cliente;
import lavarapido.model.TipoServico;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Tela da agenda: formulário de agendamento, filtro (dia/semana/todos) e tabela. */
public class AgendamentoView extends PainelBase {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final JComboBox<Cliente> cmbCliente = new JComboBox<>();
    private final JComboBox<TipoServico> cmbTipo = new JComboBox<>();
    private final JTextField txtData = new JTextField(10);
    private final JTextField txtHora = new JTextField(6);
    private final JButton btnSalvar = new JButton("Agendar");
    private final JButton btnNovo = new JButton("Novo");
    private final JButton btnCancelar = new JButton("Cancelar agendamento");
    private final JButton btnConcluir = new JButton("Concluir");

    private final JTextField txtFiltroData = new JTextField(10);
    private final JRadioButton rdoDia = new JRadioButton("Dia");
    private final JRadioButton rdoSemana = new JRadioButton("Semana", true);
    private final JButton btnFiltrar = new JButton("Filtrar");
    private final JButton btnTodos = new JButton("Ver todos");

    private final DefaultTableModel modelo = modeloSomenteLeitura(
            "ID", "Início", "Término", "Cliente", "Placa", "Serviço", "Preço", "Duração (min)", "Status");
    private final JTable tabela = criarTabela(modelo);
    private long idEmEdicao = -1;

    public AgendamentoView() {
        addCampo("Cliente:", cmbCliente);
        addCampo("Serviço:", cmbTipo);
        JPanel dh = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        dh.add(txtData);
        dh.add(new JLabel("às"));
        dh.add(txtHora);
        dh.add(new JLabel("(dd/mm/aaaa  hh:mm)"));
        addCampo("Data e hora:", dh);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botoes.add(btnSalvar);
        botoes.add(btnNovo);
        botoes.add(btnConcluir);
        botoes.add(btnCancelar);

        JPanel topo = new JPanel(new BorderLayout());
        topo.setBorder(BorderFactory.createTitledBorder("Agendamento"));
        topo.add(form, BorderLayout.CENTER);
        topo.add(botoes, BorderLayout.SOUTH);

        ButtonGroup g = new ButtonGroup();
        g.add(rdoDia);
        g.add(rdoSemana);
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtro.setBorder(BorderFactory.createTitledBorder("Agenda"));
        filtro.add(new JLabel("Data de referência:"));
        filtro.add(txtFiltroData);
        filtro.add(rdoDia);
        filtro.add(rdoSemana);
        filtro.add(btnFiltrar);
        filtro.add(btnTodos);

        JPanel centro = new JPanel(new BorderLayout(0, 4));
        centro.add(filtro, BorderLayout.NORTH);
        centro.add(new JScrollPane(tabela), BorderLayout.CENTER);

        add(topo, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        tabela.getColumnModel().getColumn(0).setMaxWidth(60);
        definirModoEdicao(false);
    }

    // Eventos usados pelo Controller.
    public void aoSalvar(Runnable r) { btnSalvar.addActionListener(e -> r.run()); }
    public void aoNovo(Runnable r) { btnNovo.addActionListener(e -> r.run()); }
    public void aoCancelarAgendamento(Runnable r) { btnCancelar.addActionListener(e -> r.run()); }
    public void aoConcluir(Runnable r) { btnConcluir.addActionListener(e -> r.run()); }
    public void aoFiltrar(Runnable r) { btnFiltrar.addActionListener(e -> r.run()); }
    public void aoVerTodos(Runnable r) { btnTodos.addActionListener(e -> r.run()); }
    public void aoSelecionar(Runnable r) {
        tabela.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) r.run(); });
    }

    public Cliente getCliente() { return (Cliente) cmbCliente.getSelectedItem(); }
    public TipoServico getTipo() { return (TipoServico) cmbTipo.getSelectedItem(); }
    public String getData() { return txtData.getText(); }
    public String getHora() { return txtHora.getText(); }
    public String getFiltroData() { return txtFiltroData.getText(); }
    public boolean isFiltroSemana() { return rdoSemana.isSelected(); }
    public long getIdEmEdicao() { return idEmEdicao; }
    public long getIdSelecionado() { return idSelecionado(tabela); }

    public void setFiltroData(java.time.LocalDate d) { txtFiltroData.setText(d.format(DATA)); }

    public void setClientes(List<Cliente> clientes) {
        Object sel = cmbCliente.getSelectedItem();
        cmbCliente.removeAllItems();
        clientes.forEach(cmbCliente::addItem);
        if (sel != null) cmbCliente.setSelectedItem(sel);
    }

    public void setTipos(List<TipoServico> tipos) {
        Object sel = cmbTipo.getSelectedItem();
        cmbTipo.removeAllItems();
        tipos.forEach(cmbTipo::addItem);
        if (sel != null) cmbTipo.setSelectedItem(sel);
    }

    /**
     * Carrega o agendamento no formulário; se não estiver ativo, os botões de alteração ficam desabilitados.
     */
    public void preencher(Agendamento a) {
        idEmEdicao = a.getId();
        cmbCliente.setSelectedItem(a.getCliente());
        cmbTipo.setSelectedItem(a.getTipoServico());
        txtData.setText(a.getDataHora().format(DATA));
        txtHora.setText(a.getDataHora().format(HORA));
        definirModoEdicao(true);
        boolean ativo = a.isAtivo();
        btnSalvar.setEnabled(ativo);
        btnConcluir.setEnabled(ativo);
        btnCancelar.setEnabled(ativo);
    }

    public void limparFormulario() {
        idEmEdicao = -1;
        txtData.setText("");
        txtHora.setText("");
        tabela.clearSelection();
        definirModoEdicao(false);
    }

    /**
     * Alterna os botões entre "novo agendamento" e "editando um existente".
     */
    private void definirModoEdicao(boolean editando) {
        btnSalvar.setText(editando ? "Salvar alterações" : "Agendar");
        btnSalvar.setEnabled(true);
        btnConcluir.setEnabled(editando);
        btnCancelar.setEnabled(editando);
    }

    public void mostrarAgendamentos(List<Agendamento> lista) {
        modelo.setRowCount(0);
        for (Agendamento a : lista) {
            modelo.addRow(new Object[]{a.getId(), a.getDataHora().format(DATA_HORA),
                    a.getFim().format(DATA_HORA), a.getCliente().getNome(),
                    a.getCliente().getPlacaVeiculo(), a.getTipoServico().getNome(),
                    TipoServicoView.MOEDA.format(a.getPreco()), a.getDuracaoMinutos(), a.getStatus()});
        }
    }
}
