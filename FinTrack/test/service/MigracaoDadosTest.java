// Agrupa esta classe na camada service do projeto.
package service;

// Referencia Conexao, componente da camada dao utilizado neste fluxo.
import dao.Conexao;
// Referencia TransacaoDAO, componente da camada dao utilizado neste fluxo.
import dao.TransacaoDAO;
// Disponibiliza o tratamento de falhas de arquivo ou comunicação.
import java.io.IOException;
// Disponibiliza cálculos monetários decimais sem somas em ponto flutuante.
import java.math.BigDecimal;
// Disponibiliza leitura, gravação e criação de arquivos e diretórios.
import java.nio.file.Files;
// Disponibiliza caminhos sem depender de separadores montados manualmente.
import java.nio.file.Path;
// Referencia TransacaoMensal, componente da camada model utilizado neste fluxo.
import model.TransacaoMensal;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import org.junit.jupiter.api.*;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import org.junit.jupiter.api.io.TempDir;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import static org.junit.jupiter.api.Assertions.*;

// Reúne os cenários de teste de MigracaoDados.
class MigracaoDadosTest {
    // O JUnit cria uma pasta temporária exclusiva e a limpa ao terminar os testes.
    @TempDir Path pasta;
    // Dependência que será fornecida na inicialização; a classe não acessa armazenamento por conta própria.
    private TransacaoDAO dao;

    // Cria dependências novas antes de cada teste, isolando os cenários.
    @BeforeEach void preparar() throws Exception {
        // Cria um banco isolado em memória; este cenário não usa as transações pessoais.
        dao = new TransacaoDAO(Conexao.abrir("jdbc:sqlite::memory:"));
    }

    // Libera recursos ou limpa o estado ao terminar a sessão.
    @AfterEach void encerrar() { dao.close(); }

    // Cenário de regressão: importa ids valores e acentos uma vez.
    @Test void importaIdsValoresEAcentosUmaVez() throws Exception {
        // Prepara arquivo com os dados ou recursos usados nas próximas operações.
        Path arquivo = pasta.resolve("transacoes.csv");
        // Prepara conteudo com os dados ou recursos usados nas próximas operações.
        String conteudo = "7;salário;1000.10;receita;Salário;05/06/2026;true;5\n8;almoço;20.15;despesa;Alimentação;06/06/2026;false;0\n";
        // Cria dados fictícios do cenário em arquivo temporário, sem usar a base pessoal.
        Files.writeString(arquivo, conteudo);
        // Executa a migração sobre dados fictícios para conferir importação, repetição ou rollback.
        MigracaoDados.importar(arquivo, dao);
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(2, dao.listar().size());
        // Verifica a subclasse reconstruída, preservando o comportamento de receita, despesa ou mensal.
        TransacaoMensal mensal = assertInstanceOf(TransacaoMensal.class, dao.listar().get(1));
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(7, mensal.getId());
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals("salário", mensal.getDescricao());
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(new BigDecimal("1000.10"), mensal.getValorDecimal());
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(5, mensal.getDiaVencimento());
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(conteudo, Files.readString(arquivo));
        // Remove somente o arquivo temporário criado por este teste.
        Files.delete(arquivo);
        // Executa a migração sobre dados fictícios para conferir importação, repetição ou rollback.
        MigracaoDados.importar(arquivo, dao);
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(2, dao.listar().size());
    }

    // Cenário de regressão: arquivo invalido nao importa parcialmente.
    @Test void arquivoInvalidoNaoImportaParcialmente() throws Exception {
        // Prepara arquivo com os dados ou recursos usados nas próximas operações.
        Path arquivo = pasta.resolve("transacoes.csv");
        // Cria dados fictícios do cenário em arquivo temporário, sem usar a base pessoal.
        Files.writeString(arquivo, "1;Válida;10;receita;Outros;01/01/2026;false;0\n2;Inválida;-10;despesa;Outros;01/01/2026;false;0\n");
        // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
        IOException erro = assertThrows(IOException.class, () -> MigracaoDados.importar(arquivo, dao));
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(erro.getMessage().contains("linha 2"));
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(dao.listar().isEmpty());
        // Exige que a condição proibida, ausente ou já removida seja falsa.
        assertFalse(dao.migracaoConcluida("transacoes_csv"));
    }

    // Cenário de regressão: ids duplicados sao rejeitados.
    @Test void idsDuplicadosSaoRejeitados() throws Exception {
        // Prepara arquivo com os dados ou recursos usados nas próximas operações.
        Path arquivo = pasta.resolve("transacoes.csv");
        // Cria dados fictícios do cenário em arquivo temporário, sem usar a base pessoal.
        Files.writeString(arquivo, "1;A;10;receita;Outros;01/01/2026;false;0\n1;B;10;despesa;Outros;01/01/2026;false;0\n");
        // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
        assertThrows(IOException.class, () -> MigracaoDados.importar(arquivo, dao));
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(dao.listar().isEmpty());
    }

    // Cenário de regressão: ausencia de csv nao impede uso do banco.
    @Test void ausenciaDeCsvNaoImpedeUsoDoBanco() throws Exception {
        // Executa a migração sobre dados fictícios para conferir importação, repetição ou rollback.
        MigracaoDados.importar(pasta.resolve("ausente.csv"), dao);
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(dao.migracaoConcluida("transacoes_csv"));
        // Inclui o registro por meio da camada responsável pela coleção ou persistência.
        dao.inserir(new model.Receita("Outros", "Nova", 10, "01/01/2026"));
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(1, dao.listar().size());
    }

    // Cenário de regressão: banco em arquivo mantem registros ao reabrir.
    @Test void bancoEmArquivoMantemRegistrosAoReabrir() throws Exception {
        // Prepara url com os dados ou recursos usados nas próximas operações.
        String url = "jdbc:sqlite:" + pasta.resolve("fintrack.db");
        // try-with-resources fecha automaticamente os recursos declarados, também em caso de erro.
        try (TransacaoDAO primeiro = new TransacaoDAO(Conexao.abrir(url))) {
            // Inclui o registro por meio da camada responsável pela coleção ou persistência.
            primeiro.inserir(new model.Receita("Outros", "Persistente", 0.01, "01/01/2026"));
        }
        // try-with-resources fecha automaticamente os recursos declarados, também em caso de erro.
        try (TransacaoDAO segundo = new TransacaoDAO(Conexao.abrir(url))) {
            // Compara o resultado observado com o valor esperado para este cenário.
            assertEquals("Persistente", segundo.listar().get(0).getDescricao());
            // Compara o resultado observado com o valor esperado para este cenário.
            assertEquals(new BigDecimal("0.01"), segundo.listar().get(0).getValorDecimal());
        }
    }
}
