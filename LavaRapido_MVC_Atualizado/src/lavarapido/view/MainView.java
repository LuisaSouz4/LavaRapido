package lavarapido.view;

import javax.swing.*;
import java.awt.*;

/** Janela principal com uma aba por módulo. */
public class MainView extends JFrame {
    private static final long serialVersionUID = 1L;

    private final JTabbedPane abas = new JTabbedPane();

    public MainView(ClienteView clientes, TipoServicoView servicos, AgendamentoView agenda) {
        super("Lava Rápido - Controle de Clientes e Agenda");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        abas.addTab("Agenda", agenda);
        abas.addTab("Clientes", clientes);
        abas.addTab("Tipos de serviço", servicos);
        add(abas, BorderLayout.CENTER);
        setSize(1100, 650);
        setLocationRelativeTo(null);
    }

    public void aoTrocarAba(Runnable r) { abas.addChangeListener(e -> r.run()); }
}
