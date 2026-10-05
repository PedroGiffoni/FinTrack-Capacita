// Agrupa esta classe na camada dao do projeto.
package dao;

// Referencia PersistenciaException, componente da camada exceptions utilizado neste fluxo.
import exceptions.PersistenciaException;
// Disponibiliza cálculos monetários decimais sem somas em ponto flutuante.
import java.math.BigDecimal;
// Disponibiliza a sessão JDBC aberta com o SQLite.
import java.sql.Connection;
// Disponibiliza SQL parametrizado, mantendo valores separados da instrução.
import java.sql.PreparedStatement;
// Disponibiliza o percurso das linhas retornadas por uma consulta SQL.
import java.sql.ResultSet;
// Disponibiliza o tratamento de falhas devolvidas pelo driver de banco.
import java.sql.SQLException;
// Disponibiliza comandos SQL fixos que não recebem dados do usuário.
import java.sql.Statement;
// Disponibiliza listas cujo conteúdo pode crescer durante a execução.
import java.util.ArrayList;
// Disponibiliza operações que aceitam diferentes implementações de coleção.
import java.util.Collection;
// Disponibiliza coleções ordenadas de registros ou mensagens.
import java.util.List;
// Disponibiliza validação de referências obrigatórias.
import java.util.Objects;
// Disponibiliza a classificação dos registros de diagnóstico.
import java.util.logging.Level;
// Disponibiliza diagnósticos das operações de persistência.
import java.util.logging.Logger;
// Referencia Despesa, componente da camada model utilizado neste fluxo.
import model.Despesa;
// Referencia Receita, componente da camada model utilizado neste fluxo.
import model.Receita;
// Referencia Transacao, componente da camada model utilizado neste fluxo.
import model.Transacao;
// Referencia TransacaoMensal, componente da camada model utilizado neste fluxo.
import model.TransacaoMensal;

// Traduz objetos em registros SQL e devolve registros como objetos do modelo.
public class TransacaoDAO implements AutoCloseable {
    // Mantém o logger da classe para registrar falhas de persistência.
    private static final Logger LOG = Logger.getLogger(TransacaoDAO.class.getName());
    // A conexão é compartilhada pelas operações desta instância e liberada no close.
    private final Connection conexao;

    // Construtor que prepara as dependências e o estado inicial desta classe.
    public TransacaoDAO(Connection conexao) {
        // O DAO utiliza a conexão recebida, permitindo trocar o banco por outro em memória nos testes.
        this.conexao = Objects.requireNonNull(conexao);
        // Prepara o esquema antes da primeira consulta ou gravação.
        criarTabelas();
    }

    // Cria o esquema somente quando necessário, mantendo os registros existentes.
    private void criarTabelas() {
        // As restrições exigem textos preenchidos, valor positivo e tipo válido.
        // O modelo exige dia de 1 a 31 nas mensais; o CHECK verifica a faixa quando o dia não é nulo.
        // id: chave primária; descrição e categoria: textos; valor_centavos: inteiro sem ponto flutuante.
        // tipo: receita ou despesa; data: texto ISO; mensal: indicador 0/1; dia_vencimento: dado opcional.
        String sql = """
                CREATE TABLE IF NOT EXISTS transacoes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    descricao TEXT NOT NULL CHECK(length(trim(descricao)) > 0),
                    valor_centavos INTEGER NOT NULL CHECK(valor_centavos > 0),
                    tipo TEXT NOT NULL CHECK(tipo IN ('receita', 'despesa')),
                    categoria TEXT NOT NULL CHECK(length(trim(categoria)) > 0),
                    data TEXT NOT NULL,
                    mensal INTEGER NOT NULL DEFAULT 0 CHECK(mensal IN (0, 1)),
                    dia_vencimento INTEGER,
                    CHECK((mensal = 0 AND dia_vencimento IS NULL) OR
                          (mensal = 1 AND dia_vencimento BETWEEN 1 AND 31))
                )
                """;
        // try-with-resources fecha automaticamente os recursos declarados, também em caso de erro.
        try (Statement comando = conexao.createStatement()) {
            // Cria a tabela se ela não existir, sem substituir os dados.
            comando.execute(sql);
            // Guarda os nomes das migrações concluídas para impedir importações repetidas.
            comando.execute("CREATE TABLE IF NOT EXISTS migracoes (nome TEXT PRIMARY KEY)");
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (SQLException e) {
            // Registra o diagnóstico e propaga a falha de persistência ao serviço ou controller.
            throw erro("Não foi possível preparar o banco de dados.", e);
        }
    }

    // Cadastra o objeto e atualiza seu identificador com o ID gerado pelo SQLite.
    public void inserir(Transacao transacao) {
        // Os valores entram pelos parâmetros ?, sem serem concatenados ao SQL.
        String sql = "INSERT INTO transacoes (descricao, valor_centavos, tipo, categoria, data, mensal, dia_vencimento) VALUES (?, ?, ?, ?, ?, ?, ?)";
        // try-with-resources fecha automaticamente os recursos declarados, também em caso de erro.
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            // Reutiliza o mesmo mapeamento dos campos para inserir, editar e importar.
            preencher(comando, transacao);
            // Executa uma gravação; diferente de executeQuery, não devolve linhas de consulta.
            comando.executeUpdate();
            // try-with-resources fecha automaticamente os recursos declarados, também em caso de erro.
            try (Statement consulta = conexao.createStatement();
                 // Recupera o último ID gerado nesta conexão, substituindo o ID provisório do modelo.
                 ResultSet resultado = consulta.executeQuery("SELECT last_insert_rowid()")) {
                // Posiciona o cursor na única linha devolvida pela consulta do ID.
                resultado.next();
                // O objeto passa a usar o identificador definitivo gerado pelo banco.
                transacao.setId(resultado.getInt(1));
            }
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (SQLException e) {
            // Registra o diagnóstico e propaga a falha de persistência ao serviço ou controller.
            throw erro("Não foi possível cadastrar a transação.", e);
        }
    }

    // Atualiza os campos do registro identificado pelo ID e informa se encontrou uma linha.
    public boolean atualizar(Transacao transacao) {
        // Os sete primeiros parâmetros contêm os dados; o último identifica a linha.
        String sql = "UPDATE transacoes SET descricao = ?, valor_centavos = ?, tipo = ?, categoria = ?, data = ?, mensal = ?, dia_vencimento = ? WHERE id = ?";
        // try-with-resources fecha automaticamente os recursos declarados, também em caso de erro.
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            // Reutiliza o mesmo mapeamento dos campos para inserir, editar e importar.
            preencher(comando, transacao);
            // Identifica o registro na edição ou preserva seu ID na importação.
            comando.setInt(8, transacao.getId());
            // Uma linha afetada significa sucesso; zero indica registro ausente.
            return comando.executeUpdate() == 1;
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (SQLException e) {
            // Registra o diagnóstico e propaga a falha de persistência ao serviço ou controller.
            throw erro("Não foi possível atualizar a transação.", e);
        }
    }

    // Remove o registro solicitado e informa se algo foi encontrado.
    public boolean remover(int id) {
        // O WHERE limita a exclusão ao ID recebido.
        try (PreparedStatement comando = conexao.prepareStatement("DELETE FROM transacoes WHERE id = ?")) {
            // Associa o ID ao primeiro parâmetro do DELETE.
            comando.setInt(1, id);
            // Uma linha afetada significa sucesso; zero indica registro ausente.
            return comando.executeUpdate() == 1;
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (SQLException e) {
            // Registra o diagnóstico e propaga a falha de persistência ao serviço ou controller.
            throw erro("Não foi possível remover a transação.", e);
        }
    }

    // Devolve os registros disponíveis sem permitir alteração direta da coleção interna.
    public List<Transacao> listar() {
        // Acumula os objetos reconstruídos a partir das linhas consultadas.
        List<Transacao> transacoes = new ArrayList<>();
        // Ordena pela data ISO e depois pelo ID, do mais recente para o mais antigo.
        try (PreparedStatement comando = conexao.prepareStatement("SELECT * FROM transacoes ORDER BY data DESC, id DESC");
             // Obtém o cursor que percorrerá as linhas retornadas pelo banco.
             ResultSet resultado = comando.executeQuery()) {
            // Avança linha a linha até não existir outro resultado.
            while (resultado.next()) {
                // Converte a linha atual na subclasse apropriada e a adiciona à coleção.
                transacoes.add(ler(resultado));
            }
            // Devolve todos os objetos reconstruídos; o try fecha os recursos da consulta.
            return transacoes;
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (SQLException e) {
            // Registra o diagnóstico e propaga a falha de persistência ao serviço ou controller.
            throw erro("Não foi possível consultar as transações.", e);
        }
    }

    // Verifica o marcador que impede importar o mesmo lote novamente.
    public boolean migracaoConcluida(String nome) {
        // Verifica apenas a existência do marcador, sem carregar outros dados.
        try (PreparedStatement comando = conexao.prepareStatement("SELECT 1 FROM migracoes WHERE nome = ?")) {
            // Usa o nome da migração como parâmetro do comando.
            comando.setString(1, nome);
            // Obtém o cursor que percorrerá as linhas retornadas pelo banco.
            try (ResultSet resultado = comando.executeQuery()) {
                // Uma linha encontrada confirma a presença do marcador de migração.
                return resultado.next();
            }
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (SQLException e) {
            // Registra o diagnóstico e propaga a falha de persistência ao serviço ou controller.
            throw erro("Não foi possível consultar a migração.", e);
        }
    }

    // Executa a etapa de migração respeitando o marcador de conclusão.
    public void importar(String nome, Collection<? extends Transacao> transacoes) {
        // Repetir a mesma importação não deve duplicar seus registros.
        if (migracaoConcluida(nome)) {
            // Encerra esta operação sem executar as etapas restantes.
            return;
        }
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // Agrupa o lote e o marcador em uma única transação SQL.
            conexao.setAutoCommit(false);
            // try-with-resources fecha automaticamente os recursos declarados, também em caso de erro.
            try (PreparedStatement comando = conexao.prepareStatement("INSERT INTO transacoes (descricao, valor_centavos, tipo, categoria, data, mensal, dia_vencimento, id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                // Percorre o lote validado; extends permite receber subclasses de Transacao.
                for (Transacao transacao : transacoes) {
                    // Reutiliza o mesmo mapeamento dos campos para inserir, editar e importar.
                    preencher(comando, transacao);
                    // Identifica o registro na edição ou preserva seu ID na importação.
                    comando.setInt(8, transacao.getId());
                    // Executa uma gravação; diferente de executeQuery, não devolve linhas de consulta.
                    comando.executeUpdate();
                }
            }
            // Grava o marcador dentro da mesma transação do lote importado.
            try (PreparedStatement comando = conexao.prepareStatement("INSERT INTO migracoes (nome) VALUES (?)")) {
                // Usa o nome da migração como parâmetro do comando.
                comando.setString(1, nome);
                // Executa uma gravação; diferente de executeQuery, não devolve linhas de consulta.
                comando.executeUpdate();
            }
            // Confirma todas as gravações somente quando o lote inteiro foi bem-sucedido.
            conexao.commit();
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (SQLException | RuntimeException e) {
            // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
            try {
                // Desfaz também as inserções anteriores do lote se algum registro falhar.
                conexao.rollback();
            // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
            } catch (SQLException falha) {
                // Preserva uma falha de rollback sem esconder a causa original.
                e.addSuppressed(falha);
            }
            // Registra o diagnóstico e propaga a falha de persistência ao serviço ou controller.
            throw erro("Não foi possível importar os registros antigos. Nenhum registro foi importado.", e);
        // Executa a limpeza obrigatória tanto no sucesso quanto na falha.
        } finally {
            // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
            try {
                // Restaura o modo normal para as próximas operações, mesmo após falhas.
                conexao.setAutoCommit(true);
            // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
            } catch (SQLException e) {
                // Registra o diagnóstico e propaga a falha de persistência ao serviço ou controller.
                throw erro("Não foi possível finalizar a migração.", e);
            }
        }
    }

    // Associa valores aos parâmetros JDBC; a numeração começa em 1.
    private void preencher(PreparedStatement comando, Transacao transacao) throws SQLException {
        // Impede ler atributos de uma referência nula.
        Objects.requireNonNull(transacao, "A transação é obrigatória.");
        // O primeiro parâmetro recebe a descrição validada pelo modelo.
        comando.setString(1, transacao.getDescricao());
        // Converte reais em centavos; longValueExact impede truncamento e estouro.
        comando.setLong(2, transacao.getValorDecimal().movePointRight(2).longValueExact());
        // Guarda o tipo receita ou despesa.
        comando.setString(3, transacao.getTipo());
        // Persiste a categoria usada em filtros e relatórios.
        comando.setString(4, transacao.getCategoria());
        // LocalDate.toString produz yyyy-MM-dd, permitindo ordenação cronológica no SQLite.
        comando.setString(5, transacao.getDataComoLocalDate().toString());
        // instanceof identifica a subclasse que possui vencimento mensal.
        boolean mensal = transacao instanceof TransacaoMensal;
        // O driver converte o booleano no indicador mensal armazenado pelo SQLite.
        comando.setBoolean(6, mensal);
        // Somente a subclasse mensal deve gravar um dia de vencimento.
        if (mensal) {
            // Grava o dia adicional da subclasse TransacaoMensal.
            comando.setInt(7, ((TransacaoMensal) transacao).getDiaVencimento());
        // Executa a alternativa quando a condição anterior não foi atendida.
        } else {
            // Uma transação comum usa SQL NULL para o vencimento.
            comando.setNull(7, java.sql.Types.INTEGER);
        }
    }

    // Converte a linha atual do ResultSet para a subclasse correspondente.
    private Transacao ler(ResultSet resultado) throws SQLException {
        // Recupera o identificador permanente da linha.
        int id = resultado.getInt("id");
        // Lê a descrição pelo nome da coluna.
        String descricao = resultado.getString("descricao");
        // Reconstrói reais a partir de centavos, posicionando a vírgula com escala 2.
        double valor = BigDecimal.valueOf(resultado.getLong("valor_centavos"), 2).doubleValue();
        // O tipo determina a subclasse que será reconstruída.
        String tipo = resultado.getString("tipo");
        // Recupera a categoria registrada pelo usuário.
        String categoria = resultado.getString("categoria");
        // O modelo aceita o formato ISO e o normaliza para apresentação.
        String data = resultado.getString("data");
        // A recorrência tem prioridade, pois inclui um atributo adicional.
        if (resultado.getBoolean("mensal")) {
            // Reconstrói a transação mensal preservando o ID e o dia de vencimento.
            return new TransacaoMensal(id, descricao, valor, tipo, categoria, data, resultado.getInt("dia_vencimento"));
        }
        // Escolhe Receita ou Despesa para manter o polimorfismo dos registros comuns.
        Transacao transacao = tipo.equals("receita")
                // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
                ? new Receita(categoria, descricao, valor, data)
                // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
                : new Despesa(categoria, descricao, valor, data);
        // Substitui o ID provisório do construtor pelo identificador persistido.
        transacao.setId(id);
        // Devolve o objeto com seu ID persistido e sua subclasse financeira.
        return transacao;
    }

    // Registra a causa para diagnóstico e devolve uma exceção com mensagem de domínio.
    private PersistenciaException erro(String mensagem, Exception causa) {
        // Registra a causa técnica junto da mensagem para diagnóstico.
        LOG.log(Level.SEVERE, mensagem, causa);
        // Propaga a falha JDBC como uma exceção da aplicação.
        return new PersistenciaException(mensagem, causa);
    }

    // Indica que este membro implementa ou substitui um comportamento definido no tipo pai.
    @Override
    // Implementa AutoCloseable para liberar o recurso no try-with-resources.
    public void close() {
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // Libera a conexão; try-with-resources chama close automaticamente.
            conexao.close();
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (SQLException e) {
            // Registra o diagnóstico e propaga a falha de persistência ao serviço ou controller.
            throw erro("Não foi possível encerrar a conexão.", e);
        }
    }
}
