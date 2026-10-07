// Agrupa esta classe na camada service do projeto.
package service;

// Referencia Conexao, componente da camada dao utilizado neste fluxo.
import dao.Conexao;
// Referencia TransacaoDAO, componente da camada dao utilizado neste fluxo.
import dao.TransacaoDAO;
// Disponibiliza cálculos monetários decimais sem somas em ponto flutuante.
import java.math.BigDecimal;
// Disponibiliza datas sem horário para cadastro, ordenação e filtros.
import java.time.LocalDate;
// Disponibiliza coleções ordenadas de registros ou mensagens.
import java.util.List;
// Referencia Despesa, componente da camada model utilizado neste fluxo.
import model.Despesa;
// Referencia Receita, componente da camada model utilizado neste fluxo.
import model.Receita;
// Referencia Transacao, componente da camada model utilizado neste fluxo.
import model.Transacao;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import org.junit.jupiter.api.*;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import static org.junit.jupiter.api.Assertions.*;

// Reúne os cenários de teste de TransacaoService.
class TransacaoServiceTest {
    // Dependência que será fornecida na inicialização; a classe não acessa armazenamento por conta própria.
    private TransacaoDAO dao;
    // Dependência que será fornecida na inicialização; a classe não acessa armazenamento por conta própria.
    private TransacaoService service;

    // Cria dependências novas antes de cada teste, isolando os cenários.
    @BeforeEach void preparar() throws Exception {
        // Cria um banco isolado em memória; este cenário não usa as transações pessoais.
        dao = new TransacaoDAO(Conexao.abrir("jdbc:sqlite::memory:"));
        service = new TransacaoService(dao);
    }

    // Libera recursos ou limpa o estado ao terminar a sessão.
    @AfterEach void encerrar() { dao.close(); }

    // Exercita o contrato genérico com a persistência real em SQLite em memória.
    @Test void contratoGenericoPersisteListaERemoveTransacoes() {
        // A referência usa a abstração; a instância continua sendo o serviço financeiro.
        ServicoGenerico<Transacao> registros = service;
        // Receita é um subtipo aceito pelo contrato especializado em Transacao.
        Receita receita = new Receita("Outros", "Pagamento", 10, "01/01/2026");
        // A chamada genérica precisa gravar no DAO, não apenas numa lista em memória.
        registros.adicionar(receita);
        // Confere a persistência independentemente da listagem do serviço.
        assertEquals(receita.getId(), dao.listar().get(0).getId());
        // Guarda a fotografia anterior para verificar isolamento estrutural.
        List<Transacao> anteriores = registros.listar();
        // O resultado não permite incluir registros sem passar pelo serviço.
        assertThrows(UnsupportedOperationException.class, anteriores::clear);
        // A remoção pelo contrato genérico deve atingir a mesma linha do banco.
        assertTrue(registros.remover(receita.getId()));
        // A nova consulta reflete a exclusão, enquanto a fotografia anterior permanece.
        assertTrue(registros.listar().isEmpty());
        // Confere que a lista anterior não foi modificada pela nova operação.
        assertEquals(1, anteriores.size());
    }

    // Verifica o curinga super e a leitura atualizada da fonte em cada consulta.
    @Test void filtroGenericoAceitaSupertipoEReconsultaBanco() {
        // Um predicado de Object pode avaliar elementos de Transacao.
        java.util.function.Predicate<Object> despesas = item -> item instanceof Despesa;
        // A referência evidencia que o filtro pertence ao serviço genérico.
        ServicoGenerico<Transacao> registros = service;
        // O banco começa vazio, portanto nenhum registro atende ao critério.
        assertTrue(registros.filtrar(despesas).isEmpty());
        // Grava diretamente no DAO para verificar que o serviço não usa cache antigo.
        dao.inserir(new Despesa("Moradia", "Aluguel", 100, "02/01/2026"));
        // Inclui uma receita que precisa ser excluída pelo predicado.
        dao.inserir(new Receita("Outros", "Salário", 200, "02/01/2026"));
        // A nova consulta deve encontrar somente a despesa recém-gravada.
        assertEquals("Aluguel", registros.filtrar(despesas).get(0).getDescricao());
        // Confere a quantidade para impedir que o filtro aceite também a receita.
        assertEquals(1, registros.filtrar(despesas).size());
    }

    // Cenário de regressão: calcula saldo sem erro de arredondamento.
    @Test void calculaSaldoSemErroDeArredondamento() {
        // Prepara transacoes com os dados ou recursos usados nas próximas operações.
        List<Transacao> transacoes = List.of(
                // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
                new Receita("Outros", "Um", 0.10, "01/01/2026"),
                // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
                new Receita("Outros", "Dois", 0.20, "01/01/2026"),
                // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
                new Despesa("Outros", "Três", 0.30, "01/01/2026"));
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(new BigDecimal("0.00"), TransacaoService.calcularSaldo(transacoes));
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(new BigDecimal("0.30"), TransacaoService.calcularTotal(transacoes, "receita"));
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(new BigDecimal("0.30"), TransacaoService.calcularTotal(transacoes, "despesa"));
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(new BigDecimal("0.00"), TransacaoService.calcularSaldo(List.of()));
    }

    // Cenário de regressão: filtra por descricao tipo categoria e datas inclusivas.
    @Test void filtraPorDescricaoTipoCategoriaEDatasInclusivas() {
        // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
        service.adicionar(new Receita("Salário", "Pagamento", 5000, "01/06/2026"));
        // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
        service.adicionar(new Despesa("Moradia", "Aluguel mensal", 1500, "10/06/2026"));
        // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
        service.adicionar(new Despesa("Moradia", "Conta de luz", 100, "01/07/2026"));
        // Prepara filtradas com os dados ou recursos usados nas próximas operações.
        List<Transacao> filtradas = service.filtrar("ALUGUEL", "despesa", "moradia",
                LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 10));
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(1, filtradas.size());
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals("Aluguel mensal", filtradas.get(0).getDescricao());
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(new BigDecimal("-1500.00"), TransacaoService.calcularSaldo(filtradas));
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(service.filtrar("ausente", null, null, null, null).isEmpty());
    }

    // Cenário de regressão: rejeita periodo invertido.
    @Test void rejeitaPeriodoInvertido() {
        // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
        assertThrows(IllegalArgumentException.class, () -> service.filtrar(null, null, null,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 6, 1)));
    }

    // Cenário de regressão: agrupa categoria sem distinguir maiusculas.
    @Test void agrupaCategoriaSemDistinguirMaiusculas() {
        // Prepara transacoes com os dados ou recursos usados nas próximas operações.
        List<Transacao> transacoes = List.of(
                // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
                new Despesa("Moradia", "Aluguel", 100, "01/01/2026"),
                // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
                new Despesa("moradia", "Conta", 25, "01/01/2026"),
                // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
                new Receita("Salário", "Pagamento", 200, "01/01/2026"));
        // Prepara totais com os dados ou recursos usados nas próximas operações.
        var totais = TransacaoService.despesasPorCategoria(transacoes);
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(1, totais.size());
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(new BigDecimal("125.00"), totais.get("Moradia"));
    }

    // Cenário de regressão: edicao de registro ausente informa falha.
    @Test void edicaoDeRegistroAusenteInformaFalha() {
        // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
        Receita ausente = new Receita("Outros", "Ausente", 1, "01/01/2026");
        // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
        assertThrows(IllegalArgumentException.class, () -> service.atualizar(ausente));
        // Exige que a condição proibida, ausente ou já removida seja falsa.
        assertFalse(service.remover(ausente.getId()));
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(service.listar().isEmpty());
    }
}
