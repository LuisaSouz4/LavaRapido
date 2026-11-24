package lavarapido.service;

import lavarapido.model.TipoServico;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Regras de negócio dos tipos de serviço (nome único, preço maior que zero). */
public class TipoServicoService {
    private final Banco banco;

    public TipoServicoService(Banco banco) { this.banco = banco; }

    public List<TipoServico> listar() {
        return banco.dados().tipos.stream()
                .sorted(Comparator.comparing(t -> t.getNome().toLowerCase())).toList();
    }

    public Optional<TipoServico> buscar(long id) {
        return banco.dados().tipos.stream().filter(t -> t.getId() == id).findFirst();
    }

    /**
     * Valida e grava um novo tipo de serviço.
     */
    public TipoServico cadastrar(String nome, String descricao, String preco, String duracao) {
        TipoServico t = new TipoServico();
        preencher(t, nome, descricao, preco, duracao);
        t.setId(banco.proximoId());
        banco.dados().tipos.add(t);
        banco.salvar();
        return t;
    }

    public TipoServico editar(long id, String nome, String descricao, String preco, String duracao) {
        TipoServico t = buscar(id).orElseThrow(() -> new ValidacaoException("Tipo de serviço não encontrado."));
        preencher(t, nome, descricao, preco, duracao);
        banco.salvar();
        return t;
    }

    /**
     * Exclui o serviço. Bloqueia se houver agendamento ativo com ele.
     */
    public void excluir(long id) {
        TipoServico t = buscar(id).orElseThrow(() -> new ValidacaoException("Tipo de serviço não encontrado."));
        boolean temAtivo = banco.dados().agendamentos.stream()
                .anyMatch(a -> a.getTipoServico().equals(t) && a.isAtivo());
        if (temAtivo) {
            throw new ValidacaoException("Não é possível excluir: há agendamentos ativos com este serviço.");
        }
        banco.dados().agendamentos.removeIf(a -> a.getTipoServico().equals(t));
        banco.dados().tipos.remove(t);
        banco.salvar();
    }

    /** Cadastra os serviços padrão na primeira execução. */
    public void semearSeVazio() {
        if (!banco.dados().tipos.isEmpty()) return;
        cadastrar("Lavagem simples", "Lavagem externa com shampoo e secagem", "30,00", "30");
        cadastrar("Lavagem completa", "Lavagem externa e interna, aspiração e pretinho nos pneus", "60,00", "60");
        cadastrar("Lavagem com cera", "Lavagem completa com aplicação de cera protetora", "90,00", "90");
        cadastrar("Lavagem detalhada", "Higienização detalhada, polimento de faróis e hidratação de bancos", "180,00", "180");
    }

    /** Aceita "25", "25.5", "25,50" e "1.250,00" (com ou sem "R$"). */
    public static BigDecimal parsePreco(String texto) {
        if (texto == null || texto.isBlank()) throw new ValidacaoException("Informe o preço.");
        String s = texto.replace("R$", "").replace(" ", "").trim();
        if (s.contains(",")) s = s.replace(".", "").replace(',', '.');
        try {
            BigDecimal v = new BigDecimal(s).setScale(2, RoundingMode.HALF_UP);
            if (v.signum() <= 0) throw new ValidacaoException("O preço deve ser maior que zero.");
            return v;
        } catch (NumberFormatException e) {
            throw new ValidacaoException("Preço inválido. Exemplo: 45,90");
        }
    }

    /**
     * Valida nome (único) e preço e copia os valores para o objeto.
     */
    private void preencher(TipoServico t, String nome, String descricao, String preco, String duracao) {
        if (nome == null || nome.isBlank()) throw new ValidacaoException("Informe o nome do serviço.");
        boolean duplicado = banco.dados().tipos.stream()
                .anyMatch(o -> o.getId() != t.getId() && o.getNome().equalsIgnoreCase(nome.trim()));
        if (duplicado) throw new ValidacaoException("Já existe um serviço com este nome.");
        t.setPreco(parsePreco(preco));
        int minutos;
        try { minutos = Integer.parseInt(duracao.trim()); }
        catch (Exception e) { throw new ValidacaoException("A duração deve ser um número inteiro de minutos."); }
        if (minutos < 5 || minutos > 480) {
            throw new ValidacaoException("A duração deve ficar entre 5 e 480 minutos.");
        }
        t.setDuracaoMinutos(minutos);
        t.setNome(nome.trim());
        t.setDescricao(descricao == null ? "" : descricao.trim());
    }
}
