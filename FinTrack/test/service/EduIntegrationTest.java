// Agrupa esta classe na camada service do projeto.
package service;

// Referencia Conexao, componente da camada dao utilizado neste fluxo.
import dao.Conexao;
// Referencia TransacaoDAO, componente da camada dao utilizado neste fluxo.
import dao.TransacaoDAO;
// Disponibiliza o tratamento de falhas de arquivo ou comunicação.
import java.io.IOException;
// Disponibiliza endereços de serviços e páginas externas.
import java.net.URI;
// Disponibiliza a comunicação HTTP com o serviço do Edu.
import java.net.http.HttpClient;
// Disponibiliza a montagem de pedidos HTTP.
import java.net.http.HttpRequest;
// Disponibiliza os códigos e conteúdos recebidos por HTTP.
import java.net.http.HttpResponse;
// Disponibiliza caminhos sem depender de separadores montados manualmente.
import java.nio.file.Path;
// Disponibiliza os limites de tempo de conexão e resposta.
import java.time.Duration;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import org.junit.jupiter.api.Test;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import org.junit.jupiter.api.io.TempDir;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import static org.junit.jupiter.api.Assertions.*;

// Exige ativação explícita para não abrir janelas ou iniciar servidor nas execuções comuns.
@EnabledIfSystemProperty(named = "fintrack.testes.edu", matches = "true")
// Reúne os cenários de teste de EduIntegration.
class EduIntegrationTest {
    // O JUnit cria uma pasta temporária exclusiva e a limpa ao terminar os testes.
    @TempDir Path pasta;

    // Cenário de regressão: inicia servidor reutiliza processo e encerra.
    @Test void iniciaServidorReutilizaProcessoEEncerra() throws Exception {
        // Prepara banco com os dados ou recursos usados nas próximas operações.
        Path banco = pasta.resolve("fintrack.db");
        // try-with-resources fecha automaticamente os recursos declarados, também em caso de erro.
        try (TransacaoDAO dao = new TransacaoDAO(Conexao.abrir("jdbc:sqlite:" + banco))) {
            // Inclui o registro por meio da camada responsável pela coleção ou persistência.
            dao.inserir(new model.Receita("Outros", "Teste", 10, "01/01/2026"));
        }
        URI endereco;
        // try-with-resources fecha automaticamente os recursos declarados, também em caso de erro.
        try (EduService edu = new EduService(Path.of("eduIa"), banco)) {
            endereco = edu.iniciar();
            // Compara o resultado observado com o valor esperado para este cenário.
            assertEquals("127.0.0.1", endereco.getHost());
            // Exige que a condição de sucesso ou de estado válido seja verdadeira.
            assertTrue(servidorDisponivel(endereco));
            // Compara o resultado observado com o valor esperado para este cenário.
            assertEquals(endereco, edu.iniciar());
        }
        // Prepara limite com os dados ou recursos usados nas próximas operações.
        long limite = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
        // Repete o fluxo enquanto a condição permanecer válida.
        while (servidorDisponivel(endereco) && System.nanoTime() < limite) Thread.sleep(100);
        // Exige que a condição proibida, ausente ou já removida seja falsa.
        assertFalse(servidorDisponivel(endereco));
    }

    // Consulta a saúde do servidor local com limite de tempo.
    private boolean servidorDisponivel(URI endereco) throws InterruptedException {
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // Configura um cliente HTTP com tempo de conexão limitado.
            HttpClient cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build();
            // Prepara a consulta de disponibilidade do servidor local.
            HttpRequest pedido = HttpRequest.newBuilder(endereco.resolve("/_stcore/health"))
                    .timeout(Duration.ofSeconds(1)).GET().build();
            // Considera o servidor disponível somente quando ele devolve HTTP 200.
            return cliente.send(pedido, HttpResponse.BodyHandlers.discarding()).statusCode() == 200;
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (IOException e) {
            // Devolve o sinal de resultado que orientará o próximo passo do chamador.
            return false;
        }
    }
}
