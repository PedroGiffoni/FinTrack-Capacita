// Agrupa esta classe na camada model do projeto.
package model;

// Disponibiliza cálculos monetários decimais sem somas em ponto flutuante.
import java.math.BigDecimal;
// Disponibiliza datas sem horário para cadastro, ordenação e filtros.
import java.time.LocalDate;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import org.junit.jupiter.api.Test;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import static org.junit.jupiter.api.Assertions.*;

// Reúne os cenários de teste de Transacao.
class TransacaoTest {
    // Cenário de regressão: receita soma e despesa subtrai.
    @Test void receitaSomaEDespesaSubtrai() {
        // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
        Receita receita = new Receita("Salário", "Pagamento", 1200.50, "05/06/2026");
        // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
        Despesa despesa = new Despesa("Moradia", "Aluguel", 800, "2026-06-10");
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(new BigDecimal("1200.50"), receita.calcularImpactoDecimal());
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(new BigDecimal("-800.00"), despesa.calcularImpactoDecimal());
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(1200.50, receita.calcularImpactoNoSaldo());
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(-800, despesa.calcularImpactoNoSaldo());
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(LocalDate.of(2026, 6, 10), despesa.getDataComoLocalDate());
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals("10/06/2026", despesa.getData());
    }

    // Cenário de regressão: rejeita valores invalidos.
    @Test void rejeitaValoresInvalidos() {
        // Percorre a coleção ou intervalo definido, processando um elemento por repetição.
        for (double valor : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY, 1.001, 100000000}) {
            // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
            assertThrows(IllegalArgumentException.class, () -> new Receita("Outros", "Receita", valor, "01/01/2026"));
        }
    }

    // Cenário de regressão: rejeita campos vazios e tipo desconhecido.
    @Test void rejeitaCamposVaziosETipoDesconhecido() {
        // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
        assertThrows(IllegalArgumentException.class, () -> new Receita(" ", "Receita", 1, "01/01/2026"));
        // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
        assertThrows(IllegalArgumentException.class, () -> new Receita("Outros", null, 1, "01/01/2026"));
        // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
        assertThrows(IllegalArgumentException.class, () -> new Transacao("Outros", "Item", 1, "outro", "01/01/2026"));
        // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
        assertThrows(IllegalArgumentException.class, () -> new Receita("Outros", "Item", 1, "31/02/2026"));
        // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
        assertThrows(IllegalArgumentException.class, () -> new Receita("Outros", "Item", 1, ""));
    }

    // Cenário de regressão: valida alteracoes sem substituir valor anterior.
    @Test void validaAlteracoesSemSubstituirValorAnterior() {
        // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
        Receita receita = new Receita("Outros", "Item", 10, "01/01/2026");
        // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
        assertThrows(IllegalArgumentException.class, () -> receita.setValor(-1));
        // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
        assertThrows(IllegalArgumentException.class, () -> receita.setTipo(null));
        // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
        assertThrows(IllegalArgumentException.class, () -> receita.setData("29/02/2025"));
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(10, receita.getValor());
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals("receita", receita.getTipo());
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals("01/01/2026", receita.getData());
    }

    // Cenário de regressão: valida dia mensal e normaliza tipo.
    @Test void validaDiaMensalENormalizaTipo() {
        // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
        TransacaoMensal mensal = new TransacaoMensal("Conta", 25, "DESPESA", "Outros", "2026-02-28", 31);
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals("despesa", mensal.getTipo());
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(31, mensal.getDiaVencimento());
        // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
        assertThrows(IllegalArgumentException.class, () -> mensal.setDiaVencimento(0));
        // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
        assertThrows(IllegalArgumentException.class, () -> mensal.setDiaVencimento(32));
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(31, mensal.getDiaVencimento());
    }
}
