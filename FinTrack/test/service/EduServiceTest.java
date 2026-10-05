// Agrupa esta classe na camada service do projeto.
package service;

// Disponibiliza o tratamento de falhas de arquivo ou comunicação.
import java.io.IOException;
// Disponibiliza leitura, gravação e criação de arquivos e diretórios.
import java.nio.file.Files;
// Disponibiliza caminhos sem depender de separadores montados manualmente.
import java.nio.file.Path;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import org.junit.jupiter.api.Test;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import org.junit.jupiter.api.io.TempDir;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import static org.junit.jupiter.api.Assertions.*;

// Reúne os cenários de teste de EduService.
class EduServiceTest {
    // O JUnit cria uma pasta temporária exclusiva e a limpa ao terminar os testes.
    @TempDir Path pasta;

    // Cenário de regressão: modulo ausente informa problema.
    @Test void moduloAusenteInformaProblema() throws Exception {
        // try-with-resources fecha automaticamente os recursos declarados, também em caso de erro.
        try (EduService edu = new EduService(pasta, pasta.resolve("fintrack.db"))) {
            // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
            IOException erro = assertThrows(IOException.class, edu::iniciar);
            // Exige que a condição de sucesso ou de estado válido seja verdadeira.
            assertTrue(erro.getMessage().contains("não foi encontrado"));
        }
    }

    // Cenário de regressão: ambiente python ausente indica configuracao.
    @Test void ambientePythonAusenteIndicaConfiguracao() throws Exception {
        // Cria a pasta necessária aos recursos temporários deste cenário.
        Files.createDirectories(pasta.resolve("src"));
        // Cria dados fictícios do cenário em arquivo temporário, sem usar a base pessoal.
        Files.writeString(pasta.resolve("src/app.py"), "");
        // try-with-resources fecha automaticamente os recursos declarados, também em caso de erro.
        try (EduService edu = new EduService(pasta, pasta.resolve("fintrack.db"))) {
            // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
            IOException erro = assertThrows(IOException.class, edu::iniciar);
            // Exige que a condição de sucesso ou de estado válido seja verdadeira.
            assertTrue(erro.getMessage().contains("configurar_edu.bat"));
        }
    }

    // Cenário de regressão: fechamento impede nova inicializacao.
    @Test void fechamentoImpedeNovaInicializacao() throws Exception {
        // Prepara edu com os dados ou recursos usados nas próximas operações.
        EduService edu = new EduService(pasta, pasta.resolve("fintrack.db"));
        // Libera o recurso ou fecha a janela depois de concluir seu uso.
        edu.close();
        // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
        assertThrows(IOException.class, edu::iniciar);
    }
}
