package lavarapido;

import lavarapido.controller.AgendamentoController;
import lavarapido.controller.ClienteController;
import lavarapido.controller.TipoServicoController;
import lavarapido.service.*;
import lavarapido.view.*;

import javax.swing.*;
import java.nio.file.Path;

/** Inicia o sistema e cria as telas e os controles. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception ignorado) { /* usa o padrão */ }

            // Banco compartilhado pelos serviços.
            Banco banco = new Banco(Path.of("dados", "lavarapido.dat"));
            ClienteService clienteService = new ClienteService(banco);
            TipoServicoService tipoService = new TipoServicoService(banco);
            AgendamentoService agendamentoService = new AgendamentoService(banco);
            tipoService.semearSeVazio();

            // Criação das telas.
            ClienteView clienteView = new ClienteView();
            TipoServicoView tipoView = new TipoServicoView();
            AgendamentoView agendamentoView = new AgendamentoView();

            // Ligação entre telas e regras do sistema.
            new ClienteController(clienteView, clienteService);
            new TipoServicoController(tipoView, tipoService);
            AgendamentoController agenda = new AgendamentoController(agendamentoView, agendamentoService, clienteService, tipoService);

            // Atualiza as opções da agenda ao trocar de aba.
            MainView janela = new MainView(clienteView, tipoView, agendamentoView);
            janela.aoTrocarAba(() -> { agenda.atualizarCombos(); agenda.atualizarTabela(); });
            janela.setVisible(true);
        });
    }
}
