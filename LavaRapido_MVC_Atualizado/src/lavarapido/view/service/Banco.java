package lavarapido.service;

import lavarapido.model.Agendamento;
import lavarapido.model.Cliente;
import lavarapido.model.TipoServico;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Salva e carrega os dados do sistema em arquivo.
 */
public class Banco {
    public static class Dados implements Serializable {
        private static final long serialVersionUID = 1L;
        public final List<Cliente> clientes = new ArrayList<>();
        public final List<TipoServico> tipos = new ArrayList<>();
        public final List<Agendamento> agendamentos = new ArrayList<>();
        public long proximoId = 1;
    }

    private final Path arquivo;
    private Dados dados;

    public Banco(Path arquivo) {
        this.arquivo = arquivo;
        this.dados = carregar();
    }

    public Dados dados() { return dados; }

    /**
     * Completa os novos campos quando o usuário abre um arquivo criado por uma
     * versão anterior do projeto, na qual ainda não existia duração.
     */
    private void normalizarDadosAntigos(Dados dados) {
        for (TipoServico tipo : dados.tipos) {
            if (tipo.getDuracaoMinutos() < 5) {
                String nome = tipo.getNome() == null ? "" : tipo.getNome().toLowerCase();
                if (nome.contains("detalhada")) tipo.setDuracaoMinutos(180);
                else if (nome.contains("cera")) tipo.setDuracaoMinutos(90);
                else if (nome.contains("completa")) tipo.setDuracaoMinutos(60);
                else tipo.setDuracaoMinutos(30);
            }
        }
        for (Agendamento agendamento : dados.agendamentos) {
            if (agendamento.getTipoServico() != null) {
                if (agendamento.getDuracaoMinutos() < 5)
                    agendamento.setDuracaoMinutos(agendamento.getTipoServico().getDuracaoMinutos());
                if (agendamento.getPreco() == null)
                    agendamento.setPreco(agendamento.getTipoServico().getPreco());
            }
        }
    }

    /**
     * Gera o próximo id (único para todas as entidades).
     */
    public long proximoId() { return dados.proximoId++; }

    /**
     * Lê o arquivo na inicialização; se não existir ou estiver ilegível, começa vazio.
     */
    private Dados carregar() {
        if (!Files.exists(arquivo)) return new Dados();
        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(arquivo)))) {
            Dados carregados = (Dados) in.readObject();
            normalizarDadosAntigos(carregados);
            return carregados;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Não foi possível ler " + arquivo + ": " + e.getMessage() + " (iniciando vazio)");
            return new Dados();
        }
    }

    /**
     * Grava tudo em um arquivo temporário e depois troca pelo definitivo, evitando arquivo corrompido.
     */
    public void salvar() {
        try {
            Path pai = arquivo.toAbsolutePath().getParent();
            if (pai != null) Files.createDirectories(pai);
            Path tmp = Files.createTempFile(pai, "lavarapido", ".tmp");
            try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(tmp)))) {
                out.writeObject(dados);
            }
            Files.move(tmp, arquivo, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ValidacaoException("Falha ao salvar os dados: " + e.getMessage());
        }
    }
}
