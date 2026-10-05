// Agrupa esta classe na camada dao do projeto.
package dao;

// Referencia PersistenciaException, componente da camada exceptions utilizado neste fluxo.
import exceptions.PersistenciaException;
// Disponibiliza o tratamento de falhas de arquivo ou comunicação.
import java.io.IOException;
// Disponibiliza leitura, gravação e criação de arquivos e diretórios.
import java.nio.file.Files;
// Disponibiliza caminhos sem depender de separadores montados manualmente.
import java.nio.file.Path;
// Disponibiliza a sessão JDBC aberta com o SQLite.
import java.sql.Connection;
// Disponibiliza a seleção do driver JDBC pela URL de conexão.
import java.sql.DriverManager;
// Disponibiliza o tratamento de falhas devolvidas pelo driver de banco.
import java.sql.SQLException;
// Disponibiliza a classificação dos registros de diagnóstico.
import java.util.logging.Level;
// Disponibiliza diagnósticos das operações de persistência.
import java.util.logging.Logger;

// Centraliza o caminho do SQLite e a configuração das conexões JDBC.
public final class Conexao {
    // Devolve logger sem alterar o estado do objeto.
    private static final Logger LOG = Logger.getLogger(Conexao.class.getName());

    // Construtor que prepara as dependências e o estado inicial desta classe.
    private Conexao() { }

    // Escolhe a pasta configurada ou o diretório local de dados do projeto.
    public static Path pastaDados() {
        // Permite escolher a pasta por propriedade da JVM sem alterar o código.
        String configurada = System.getProperty("fintrack.dados");
        // Uma propriedade ausente ou vazia mantém a seleção do diretório padrão.
        if (configurada != null && !configurada.isBlank()) {
            // Normaliza a pasta configurada e usa seu caminho absoluto.
            return Path.of(configurada).toAbsolutePath().normalize();
        }
        // O caminho vazio representa o diretório em que o processo foi iniciado.
        Path atual = Path.of("").toAbsolutePath();
        // Distingue execução na raiz do projeto e na pasta interna FinTrack.
        return Files.isDirectory(atual.resolve("FinTrack/src"))
                ? atual.resolve("FinTrack/dados") : atual.resolve("dados");
    }

    // Obtém uma conexão JDBC pronta para uso; o chamador será responsável por fechá-la.
    public static Connection abrir() {
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // Resolve a pasta antes de criar o arquivo do banco.
            Path pasta = pastaDados();
            // Cria somente as pastas que faltam, preservando dados existentes.
            Files.createDirectories(pasta);
            // A URL seleciona o driver SQLite e o arquivo local de dados.
            return abrir("jdbc:sqlite:" + pasta.resolve("fintrack.db"));
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (IOException | SQLException e) {
            // Registra a causa técnica da falha ao criar a pasta ou abrir o SQLite.
            LOG.log(Level.SEVERE, "Não foi possível abrir o banco de dados.", e);
            // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
            throw new PersistenciaException("Não foi possível abrir o banco de dados.", e);
        }
    }

    // Obtém uma conexão JDBC pronta para uso; o chamador será responsável por fechá-la.
    public static Connection abrir(String url) throws SQLException {
        // O DriverManager encontra o driver compatível com a URL recebida.
        Connection conexao = DriverManager.getConnection(url);
        // try-with-resources fecha automaticamente os recursos declarados, também em caso de erro.
        try (var comando = conexao.createStatement()) {
            // Aguarda até cinco segundos por bloqueios antes de informar uma falha.
            comando.execute("PRAGMA busy_timeout = 5000");
            // Ativa as restrições de chaves estrangeiras para esta conexão.
            comando.execute("PRAGMA foreign_keys = ON");
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (SQLException e) {
            // Fecha a conexão se sua configuração falhar, antes de devolvê-la ao chamador.
            conexao.close();
            // Propaga a falha original, mantendo seu tipo e diagnóstico.
            throw e;
        }
        // Devolve a conexão somente depois de concluir sua configuração.
        return conexao;
    }
}
