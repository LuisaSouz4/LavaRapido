package lavarapido;

import lavarapido.model.*;
import lavarapido.service.*;

import java.nio.file.*;
import java.time.*;

/** Testes simples da camada de negócio (sem dependências externas). Execute com executar-testes.sh */
public class TesteSistema {
    static int ok = 0;

    static void verifica(boolean cond, String nome) {
        if (!cond) throw new AssertionError("FALHOU: " + nome);
        ok++;
        System.out.println("ok - " + nome);
    }

    static void esperaErro(Runnable r, String trecho, String nome) {
        try { r.run(); }
        catch (ValidacaoException e) { verifica(e.getMessage().contains(trecho), nome); return; }
        throw new AssertionError("Esperava ValidacaoException: " + nome);
    }

    public static void main(String[] args) throws Exception {
        Path arq = Files.createTempDirectory("lava").resolve("t.dat");
        Banco banco = new Banco(arq);
        ClienteService cs = new ClienteService(banco);
        TipoServicoService ts = new TipoServicoService(banco);
        AgendamentoService as = new AgendamentoService(banco);

        verifica(Validador.cpfValido("529.982.247-25"), "CPF válido");
        verifica(!Validador.cpfValido("111.111.111-11"), "CPF repetido inválido");
        verifica(!Validador.cpfValido("529.982.247-24"), "CPF dígito errado");
        verifica(Validador.placaValida("abc-1234") && Validador.placaValida("BRA2E19"), "placas antiga e Mercosul");
        verifica(!Validador.placaValida("AB12345"), "placa inválida");

        Cliente c = cs.cadastrar("Maria Silva", "529.982.247-25", "(11) 98888-7777", "maria@email.com", "abc-1234");
        verifica(c.getPlacaVeiculo().equals("ABC1234") && c.getCpf().equals("52998224725"), "cliente normalizado");
        esperaErro(() -> cs.cadastrar("Outro", "52998224725", "11988887777", "o@e.com", "XYZ9876"), "CPF", "CPF duplicado");
        esperaErro(() -> cs.cadastrar("", "52998224725", "11988887777", "o@e.com", "XYZ9876"), "nome", "nome obrigatório");
        esperaErro(() -> cs.cadastrar("X", "111.444.777-35", "123", "o@e.com", "XYZ9876"), "Telefone", "telefone inválido");
        esperaErro(() -> cs.cadastrar("X", "111.444.777-35", "11988887777", "oe.com", "XYZ9876"), "E-mail", "e-mail inválido");
        cs.editar(c.getId(), "Maria S. Souza", "529.982.247-25", "1133334444", "maria@email.com", "BRA2E19");
        verifica(cs.buscar(c.getId()).get().getNome().equals("Maria S. Souza"), "editar cliente (mesmo CPF permitido)");

        ts.semearSeVazio();
        verifica(ts.listar().size() == 4, "serviços padrão");
        verifica(TipoServicoService.parsePreco("R$ 1.250,5").toPlainString().equals("1250.50"), "parse preço BR");
        esperaErro(() -> ts.cadastrar("Teste", "", "0", "30"), "maior que zero", "preço zero");
        esperaErro(() -> ts.cadastrar("lavagem SIMPLES", "", "10", "30"), "nome", "serviço duplicado");
        TipoServico t = ts.listar().stream().filter(x -> x.getNome().equals("Lavagem simples")).findFirst().orElseThrow(); // 30 minutos
        TipoServico umaHora = ts.cadastrar("Serviço de uma hora", "Teste de duração", "75,00", "60");
        esperaErro(() -> ts.cadastrar("Duração inválida", "", "20", "4"), "5 e 480", "duração mínima validada");
        esperaErro(() -> ts.cadastrar("Duração inválida 2", "", "20", "481"), "5 e 480", "duração máxima validada");

        LocalDateTime amanha10 = LocalDate.now().plusDays(1).atTime(10, 0);
        Agendamento a = as.agendar(c, umaHora, amanha10);
        verifica(a.getStatus() == StatusAgendamento.AGENDADO, "agendar");
        verifica(a.getFim().equals(amanha10.plusMinutes(60)), "calcula término pela duração");
        verifica(a.getPreco().compareTo(new java.math.BigDecimal("75.00")) == 0, "copia preço para agendamento");
        esperaErro(() -> as.agendar(c, t, amanha10.plusMinutes(30)), "box estará ocupado", "bloqueia início dentro de outro serviço");
        esperaErro(() -> as.agendar(c, t, amanha10.plusMinutes(59)), "box estará ocupado", "bloqueia sobreposição de um minuto");
        verifica(as.agendar(c, t, amanha10.plusHours(1)) != null, "permite serviço quando o anterior termina");
        esperaErro(() -> as.agendar(c, t, amanha10.minusMinutes(15)), "box estará ocupado", "bloqueia serviço que termina dentro de outro");
        esperaErro(() -> as.agendar(c, t, LocalDateTime.now().minusHours(1)), "passada", "data passada");
        as.editar(a.getId(), c, umaHora, amanha10.plusHours(3));
        verifica(a.getDataHora().getHour() == 13, "editar agendamento");
        verifica(as.listarPorDia(amanha10.toLocalDate()).size() == 2, "agenda do dia");
        verifica(as.listarPorSemana(amanha10.toLocalDate()).size() == 2, "agenda da semana");
        verifica(as.listarPorDia(amanha10.toLocalDate().plusDays(1)).isEmpty(), "dia sem agendamentos");

        esperaErro(() -> cs.excluir(c.getId()), "ativos", "bloqueia excluir cliente com agendamento ativo");
        // Alterar o tipo de serviço não altera o preço/duração já copiados ao agendamento.
        ts.editar(umaHora.getId(), "Serviço de uma hora", "Preço atualizado", "99,00", "90");
        verifica(a.getPreco().compareTo(new java.math.BigDecimal("75.00")) == 0 && a.getDuracaoMinutos() == 60,
                "mantém preço e duração históricos após edição do serviço");
        as.cancelar(a.getId());
        verifica(as.agendar(c, t, amanha10.plusHours(2)) != null, "horário liberado após cancelar");
        esperaErro(() -> as.editar(a.getId(), c, t, amanha10.plusHours(5)), "Agendado", "não edita cancelado");

        Banco recarregado = new Banco(arq);
        verifica(recarregado.dados().agendamentos.size() == 3 && recarregado.dados().clientes.size() == 1, "persistência em arquivo");
        verifica(recarregado.dados().agendamentos.get(0).getCliente() == recarregado.dados().clientes.get(0), "referências preservadas");

        System.out.println("\n" + ok + " verificações passaram.");
    }
}
