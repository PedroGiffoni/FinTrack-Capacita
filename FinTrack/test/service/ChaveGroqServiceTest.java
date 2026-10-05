// Agrupa esta classe na camada service do projeto.
package service;

// Disponibiliza a seleção explícita de UTF-8.
import java.nio.charset.StandardCharsets;
// Disponibiliza operações com arquivos e caminhos.
import java.nio.file.*;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import org.junit.jupiter.api.Test;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import org.junit.jupiter.api.condition.EnabledOnOs;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import org.junit.jupiter.api.condition.OS;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import org.junit.jupiter.api.io.TempDir;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import static org.junit.jupiter.api.Assertions.*;

// Limita a execução ao Windows, pois esta verificação utiliza DPAPI.
@EnabledOnOs(OS.WINDOWS)
// Reúne os cenários de teste de ChaveGroqService.
class ChaveGroqServiceTest {
    // O JUnit cria uma pasta temporária exclusiva e a limpa ao terminar os testes.
    @TempDir Path pasta;

    // Cenário de regressão: salva criptografada e recupera em outra instancia.
    @Test void salvaCriptografadaERecuperaEmOutraInstancia() throws Exception {
        // Prepara arquivo com os dados ou recursos usados nas próximas operações.
        Path arquivo = pasta.resolve("groq.dpapi");
        // Prepara cofre com os dados ou recursos usados nas próximas operações.
        ChaveGroqService cofre = new ChaveGroqService(arquivo);
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(cofre.carregar().isEmpty());
        // Grava uma credencial fictícia para conferir sua persistência protegida.
        cofre.salvar("chave-ficticia-para-teste");
        // Lê o arquivo do cenário para verificar o conteúdo persistido.
        byte[] bytes = Files.readAllBytes(arquivo);
        // Exige que a condição proibida, ausente ou já removida seja falsa.
        assertFalse(new String(bytes, StandardCharsets.ISO_8859_1).contains("chave-ficticia-para-teste"));
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals("chave-ficticia-para-teste", new ChaveGroqService(arquivo).carregar().orElseThrow());
    }

    // Cenário de regressão: substituir e esquecer remove credencial.
    @Test void substituirEEsquecerRemoveCredencial() throws Exception {
        // Prepara cofre com os dados ou recursos usados nas próximas operações.
        ChaveGroqService cofre = new ChaveGroqService(pasta.resolve("groq.dpapi"));
        // Grava uma credencial fictícia para conferir sua persistência protegida.
        cofre.salvar("primeira-ficticia");
        // Grava uma credencial fictícia para conferir sua persistência protegida.
        cofre.salvar("segunda-ficticia");
        // Compara o resultado observado com o valor esperado para este cenário.
        assertEquals("segunda-ficticia", cofre.carregar().orElseThrow());
        // Remove a credencial fictícia para verificar a limpeza e a repetição segura da operação.
        cofre.esquecer();
        // Exige que a condição proibida, ausente ou já removida seja falsa.
        assertFalse(cofre.existe());
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(cofre.carregar().isEmpty());
        // Remove a credencial fictícia para verificar a limpeza e a repetição segura da operação.
        cofre.esquecer();
        // try-with-resources fecha automaticamente os recursos declarados, também em caso de erro.
        try (var arquivos = Files.list(pasta)) { assertEquals(0, arquivos.count()); }
    }

    // Cenário de regressão: arquivo alterado nao retorna chave.
    @Test void arquivoAlteradoNaoRetornaChave() throws Exception {
        // Prepara arquivo com os dados ou recursos usados nas próximas operações.
        Path arquivo = pasta.resolve("groq.dpapi");
        // Cria dados fictícios do cenário em arquivo temporário, sem usar a base pessoal.
        Files.writeString(arquivo, "arquivo-invalido");
        // Prepara cofre com os dados ou recursos usados nas próximas operações.
        ChaveGroqService cofre = new ChaveGroqService(arquivo);
        // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
        assertThrows(java.io.IOException.class, cofre::carregar);
        // Remove a credencial fictícia para verificar a limpeza e a repetição segura da operação.
        cofre.esquecer();
        // Exige que a condição proibida, ausente ou já removida seja falsa.
        assertFalse(Files.exists(arquivo));
    }

    // Cenário de regressão: chave vazia nao cria arquivo.
    @Test void chaveVaziaNaoCriaArquivo() {
        // Prepara cofre com os dados ou recursos usados nas próximas operações.
        ChaveGroqService cofre = new ChaveGroqService(pasta.resolve("groq.dpapi"));
        // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
        assertThrows(IllegalArgumentException.class, () -> cofre.salvar(" "));
        // Exige que a condição proibida, ausente ou já removida seja falsa.
        assertFalse(cofre.existe());
    }
}
