// Agrupa esta classe na camada dao do projeto.
package dao;

// Referencia PersistenciaException, componente da camada exceptions utilizado neste fluxo.
import exceptions.PersistenciaException;
// Disponibiliza a sessão JDBC aberta com o SQLite.
import java.sql.Connection;
// Disponibiliza coleções ordenadas de registros ou mensagens.
import java.util.List;
// Referencia Despesa, componente da camada model utilizado neste fluxo.
import model.Despesa;
// Referencia Receita, componente da camada model utilizado neste fluxo.
import model.Receita;
// Referencia Transacao, componente da camada model utilizado neste fluxo.
import model.Transacao;
// Referencia TransacaoMensal, componente da camada model utilizado neste fluxo.
import model.TransacaoMensal;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import org.junit.jupiter.api.*;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import static org.junit.jupiter.api.Assertions.*;

// Reúne os cenários de teste de TransacaoDAO.
class TransacaoDAOTest {
    private Connection conexao;
    // Dependência que será fornecida na inicialização; a classe não acessa armazenamento por conta própria.
    private TransacaoDAO dao;

    // Cria dependências novas antes de cada teste, isolando os cenários.
    @BeforeEach void preparar() throws Exception {
        // Cria um banco isolado em memória; este cenário não usa as transações pessoais.
        conexao = Conexao.abrir("jdbc:sqlite::memory:");
        dao = new TransacaoDAO(conexao);
    }

    // Libera recursos ou limpa o estado ao terminar a sessão.
    @AfterEach void encerrar() {
        // Libera o recurso ou fecha a janela depois de concluir seu uso.
        dao.close();
    }

    // Cenário de regressão: insere consulta atualiza e remove.
    @Test void insereConsultaAtualizaERemove() {
        // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
        Receita receita = new Receita("Salário", "Pagamento", 1234.56, "10/06/2026");
        // Inclui o registro por meio da camada responsável pela coleção ou persistência.
        dao.inserir(receita);
        // Prepara id com os dados ou recursos usados nas próximas operações.
        int id = receita.getId();
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(id > 0);
        // Relê os registros e captura o resultado que será apresentado ou verificado.
        Transacao cadastrada = dao.listar().get(0);
        // Verifica a subclasse reconstruída, preservando o comportamento de receita, despesa ou mensal.
        assertInstanceOf(Receita.class, cadastrada);
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(receita.getValorDecimal(), cadastrada.getValorDecimal());
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(id, cadastrada.getId());
        // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
        Despesa alterada = new Despesa("Moradia", "Aluguel", 400, "2026-06-11");
        // Mantém o identificador do registro que será alterado ou reconstruído.
        alterada.setId(id);
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(dao.atualizar(alterada));
        // Relê os registros e captura o resultado que será apresentado ou verificado.
        Transacao atualizada = dao.listar().get(0);
        // Verifica a subclasse reconstruída, preservando o comportamento de receita, despesa ou mensal.
        assertInstanceOf(Despesa.class, atualizada);
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals("Aluguel", atualizada.getDescricao());
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals("11/06/2026", atualizada.getData());
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(dao.remover(id));
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(dao.listar().isEmpty());
        // Exige que a condição proibida, ausente ou já removida seja falsa.
        assertFalse(dao.remover(id));
        // Exige que a condição proibida, ausente ou já removida seja falsa.
        assertFalse(dao.atualizar(alterada));
    }

    // Cenário de regressão: preserva mensal e permite converter para comum.
    @Test void preservaMensalEPermiteConverterParaComum() {
        // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
        TransacaoMensal mensal = new TransacaoMensal("Conta", 90.10, "despesa", "Moradia", "15/06/2026", 15);
        // Inclui o registro por meio da camada responsável pela coleção ou persistência.
        dao.inserir(mensal);
        // Verifica a subclasse reconstruída, preservando o comportamento de receita, despesa ou mensal.
        TransacaoMensal lida = assertInstanceOf(TransacaoMensal.class, dao.listar().get(0));
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(15, lida.getDiaVencimento());
        // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
        Despesa comum = new Despesa("Moradia", "Conta avulsa", 85, "15/06/2026");
        // Mantém o identificador do registro que será alterado ou reconstruído.
        comum.setId(mensal.getId());
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(dao.atualizar(comum));
        // Verifica a subclasse reconstruída, preservando o comportamento de receita, despesa ou mensal.
        assertInstanceOf(Despesa.class, dao.listar().get(0));
    }

    // Cenário de regressão: texto com sql permanece como dado.
    @Test void textoComSqlPermaneceComoDado() {
        // Usa texto semelhante a SQL malicioso para comprovar que a descrição permanece apenas um dado.
        String descricao = "Pagamento'); DROP TABLE transacoes; --";
        // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
        dao.inserir(new Receita("Outros", descricao, 1, "01/01/2026"));
        // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
        dao.inserir(new Receita("Outros", "Outra", 2, "02/01/2026"));
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(2, dao.listar().size());
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(descricao, dao.listar().get(1).getDescricao());
    }

    // Cenário de regressão: ids sao gerados pelo banco e nao reutilizados.
    @Test void idsSaoGeradosPeloBancoENaoReutilizados() {
        // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
        Receita primeira = new Receita("Outros", "Primeira", 1, "01/01/2026");
        // Inclui o registro por meio da camada responsável pela coleção ou persistência.
        dao.inserir(primeira);
        // Remove o registro do cenário pelo identificador persistido.
        dao.remover(primeira.getId());
        // Altera o contador legado para comprovar que o SQLite gera os identificadores definitivos.
        Transacao.setContador(1);
        // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
        Receita segunda = new Receita("Outros", "Segunda", 2, "01/01/2026");
        // Inclui o registro por meio da camada responsável pela coleção ou persistência.
        dao.inserir(segunda);
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(segunda.getId() > primeira.getId());
    }

    // Cenário de regressão: importacao duplicada nao repete registros.
    @Test void importacaoDuplicadaNaoRepeteRegistros() {
        // Monta um registro fictício com ID explícito para testar a persistência ou migração.
        Transacao transacao = new Transacao(40, "Antiga", 5, "receita", "Outros", "01/01/2026");
        // Executa a migração sobre dados fictícios para conferir importação, repetição ou rollback.
        dao.importar("csv", List.of(transacao));
        // Executa a migração sobre dados fictícios para conferir importação, repetição ou rollback.
        dao.importar("csv", List.of(transacao));
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(dao.migracaoConcluida("csv"));
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(1, dao.listar().size());
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(40, dao.listar().get(0).getId());
        // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
        Receita nova = new Receita("Outros", "Nova", 1, "01/01/2026");
        // Inclui o registro por meio da camada responsável pela coleção ou persistência.
        dao.inserir(nova);
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(nova.getId() > 40);
    }

    // Cenário de regressão: falha no lote faz rollback e sem marcar migracao.
    @Test void falhaNoLoteFazRollbackESemMarcarMigracao() throws Exception {
        // Monta um registro fictício com ID explícito para testar a persistência ou migração.
        Transacao a = new Transacao(5, "Primeira", 1, "receita", "Outros", "01/01/2026");
        // Monta um registro fictício com ID explícito para testar a persistência ou migração.
        Transacao b = new Transacao(5, "Duplicada", 2, "despesa", "Outros", "01/01/2026");
        // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
        assertThrows(PersistenciaException.class, () -> dao.importar("csv", List.of(a, b)));
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(dao.listar().isEmpty());
        // Exige que a condição proibida, ausente ou já removida seja falsa.
        assertFalse(dao.migracaoConcluida("csv"));
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(conexao.getAutoCommit());
        // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
        dao.inserir(new Receita("Outros", "Após falha", 1, "01/01/2026"));
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals(1, dao.listar().size());
    }

    // Cenário de regressão: restricoes do banco rejeitam valor negativo.
    @Test void restricoesDoBancoRejeitamValorNegativo() throws Exception {
        // try-with-resources fecha automaticamente os recursos declarados, também em caso de erro.
        try (var comando = conexao.prepareStatement("INSERT INTO transacoes (descricao, valor_centavos, tipo, categoria, data) VALUES (?, ?, ?, ?, ?)")) {
            // Preenche o parâmetro SQL com o conteúdo fictício do teste.
            comando.setString(1, "Item");
            // Tenta inserir valor negativo diretamente no SQL para verificar a restrição do banco.
            comando.setLong(2, -100);
            // Preenche o parâmetro SQL com o conteúdo fictício do teste.
            comando.setString(3, "despesa");
            // Preenche o parâmetro SQL com o conteúdo fictício do teste.
            comando.setString(4, "Outros");
            // Preenche o parâmetro SQL com o conteúdo fictício do teste.
            comando.setString(5, "2026-01-01");
            // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
            assertThrows(java.sql.SQLException.class, comando::executeUpdate);
        }
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(dao.listar().isEmpty());
    }
}
