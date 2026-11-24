package lavarapido.service;

import lavarapido.model.Cliente;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Regras de negócio dos clientes: valida os dados, controla duplicidade de CPF e grava no Banco. */
public class ClienteService {
    private final Banco banco;

    public ClienteService(Banco banco) { this.banco = banco; }

    /**
     * Lista os clientes em ordem alfabética.
     */
    public List<Cliente> listar() {
        return banco.dados().clientes.stream()
                .sorted(Comparator.comparing(c -> c.getNome().toLowerCase())).toList();
    }

    public Optional<Cliente> buscar(long id) {
        return banco.dados().clientes.stream().filter(c -> c.getId() == id).findFirst();
    }

    /**
     * Valida os dados, gera um id novo e grava o cliente. Lança ValidacaoException se algo estiver inválido.
     */
    public Cliente cadastrar(String nome, String cpf, String telefone, String email, String placa) {
        Cliente c = new Cliente();
        preencher(c, nome, cpf, telefone, email, placa);
        c.setId(banco.proximoId());
        banco.dados().clientes.add(c);
        banco.salvar();
        return c;
    }

    /**
     * Atualiza um cliente existente; os dados só são alterados se TODAS as validações passarem.
     */
    public Cliente editar(long id, String nome, String cpf, String telefone, String email, String placa) {
        Cliente c = buscar(id).orElseThrow(() -> new ValidacaoException("Cliente não encontrado."));
        preencher(c, nome, cpf, telefone, email, placa);
        banco.salvar();
        return c;
    }

    /**
     * Exclui o cliente. Bloqueia se houver agendamento ativo; remove o histórico (cancelado/concluído) junto.
     */
    public void excluir(long id) {
        Cliente c = buscar(id).orElseThrow(() -> new ValidacaoException("Cliente não encontrado."));
        boolean temAtivo = banco.dados().agendamentos.stream()
                .anyMatch(a -> a.getCliente().equals(c) && a.isAtivo());
        if (temAtivo) {
            throw new ValidacaoException("Não é possível excluir: o cliente possui agendamentos ativos. Cancele-os primeiro.");
        }
        banco.dados().agendamentos.removeIf(a -> a.getCliente().equals(c)); // histórico inativo
        banco.dados().clientes.remove(c);
        banco.salvar();
    }

    /**
     * Valida cada campo e só depois copia os valores para o objeto (CPF e placa são normalizados).
     */
    private void preencher(Cliente c, String nome, String cpf, String telefone, String email, String placa) {
        if (nome == null || nome.isBlank()) throw new ValidacaoException("Informe o nome do cliente.");
        if (!Validador.cpfValido(cpf)) throw new ValidacaoException("CPF inválido.");
        String cpfLimpo = Validador.somenteDigitos(cpf);
        boolean duplicado = banco.dados().clientes.stream()
                .anyMatch(o -> o.getId() != c.getId() && o.getCpf().equals(cpfLimpo));
        if (duplicado) throw new ValidacaoException("Já existe um cliente cadastrado com este CPF.");
        if (!Validador.telefoneValido(telefone)) throw new ValidacaoException("Telefone inválido. Use DDD + número (10 ou 11 dígitos).");
        if (!Validador.emailValido(email)) throw new ValidacaoException("E-mail inválido.");
        if (!Validador.placaValida(placa)) throw new ValidacaoException("Placa inválida. Use o formato ABC1234 ou ABC1D23.");
        c.setNome(nome.trim());
        c.setCpf(cpfLimpo);
        c.setTelefone(Validador.somenteDigitos(telefone));
        c.setEmail(email.trim());
        c.setPlacaVeiculo(Validador.normalizarPlaca(placa));
    }
}
