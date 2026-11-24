package lavarapido.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Utilidades comuns às telas (formulário em grade, mensagens, tabela somente leitura). */
public abstract class PainelBase extends JPanel {
    protected final JPanel form = new JPanel(new GridBagLayout());
    private int linha = 0;

    protected PainelBase() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    protected void addCampo(String rotulo, JComponent c) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = linha++;
        g.insets = new Insets(3, 5, 3, 5);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        form.add(new JLabel(rotulo), g);
        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        form.add(c, g);
    }

    protected static DefaultTableModel modeloSomenteLeitura(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    protected static JTable criarTabela(DefaultTableModel modelo) {
        JTable t = new JTable(modelo);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoCreateRowSorter(false);
        t.setRowHeight(24);
        return t;
    }

    /** Lê o id (primeira coluna) da linha selecionada, ou -1. */
    protected static long idSelecionado(JTable t) {
        int l = t.getSelectedRow();
        return l < 0 ? -1 : (Long) t.getModel().getValueAt(l, 0);
    }

    public void mostrarErro(String msg) { JOptionPane.showMessageDialog(this, msg, "Atenção", JOptionPane.WARNING_MESSAGE); }
    public void mostrarInfo(String msg) { JOptionPane.showMessageDialog(this, msg, "Informação", JOptionPane.INFORMATION_MESSAGE); }
    public boolean confirmar(String msg) {
        return JOptionPane.showConfirmDialog(this, msg, "Confirmação", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
