// Agrupa esta classe na camada service do projeto.
package service;

// Disponibiliza JsonParser para as operações desta classe.
import com.google.gson.JsonParser;
// Disponibiliza HttpServer para as operações desta classe.
import com.sun.net.httpserver.HttpServer;
// Disponibiliza os tipos do pacote java.net.
import java.net.*;
// Disponibiliza a comunicação HTTP com o serviço do Edu.
import java.net.http.HttpClient;
// Disponibiliza a seleção explícita de UTF-8.
import java.nio.charset.StandardCharsets;
// Disponibiliza coleções ordenadas de registros ou mensagens.
import java.util.List;
// Disponibiliza a captura de valores produzidos por callbacks durante testes.
import java.util.concurrent.atomic.AtomicReference;
// Referencia Receita, componente da camada model utilizado neste fluxo.
import model.Receita;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import org.junit.jupiter.api.Test;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import static org.junit.jupiter.api.Assertions.*;

// Reúne os cenários de teste de EducacaoFinanceiraService.
class EducacaoFinanceiraServiceTest {
    // Cenário de regressão: envia contexto historico e le resposta.
    @Test void enviaContextoHistoricoELeResposta() throws Exception {
        // Prepara recebido com os dados ou recursos usados nas próximas operações.
        AtomicReference<String> recebido = new AtomicReference<>();
        // Abre um servidor HTTP somente no loopback, com porta livre, para simular a Groq.
        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        // Define a resposta simulada da API para este endereço local.
        servidor.createContext("/", chamada -> {
            // Lê o corpo da requisição para conferir o JSON enviado ou liberar o fluxo.
            recebido.set(new String(chamada.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            // Compara o resultado observado com o valor esperado para este cenário.
            assertEquals("Bearer teste", chamada.getRequestHeaders().getFirst("Authorization"));
            // Prepara corpo com os dados ou recursos usados nas próximas operações.
            byte[] corpo = "{\"choices\":[{\"message\":{\"content\":\"Organize seu orçamento.\"}}]}".getBytes(StandardCharsets.UTF_8);
            // Devolve o código HTTP esperado por este cenário de sucesso ou erro.
            chamada.sendResponseHeaders(200, corpo.length);
            // Entrega ao cliente o JSON fictício usado neste teste.
            chamada.getResponseBody().write(corpo);
            // Libera o recurso ou fecha a janela depois de concluir seu uso.
            chamada.close();
        });
        // Inicia a tarefa ou recurso depois de configurar suas dependências.
        servidor.start();
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // Prepara service com os dados ou recursos usados nas próximas operações.
            EducacaoFinanceiraService service = new EducacaoFinanceiraService(HttpClient.newHttpClient(),
                    URI.create("http://127.0.0.1:" + servidor.getAddress().getPort() + "/"));
            // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
            var lista = List.<model.Transacao>of(new Receita("Trabalho", "Salário", 123.45, "05/06/2026"));
            // Compara o resultado observado com o valor esperado para este cenário.
            assertEquals("Organize seu orçamento.", service.responder("teste", "Como economizar?",
                    List.of(new EducacaoFinanceiraService.Mensagem("user", "Quero aprender")), lista, true));
            // Interpreta o JSON capturado para conferir o conteúdo das mensagens.
            var mensagens = JsonParser.parseString(recebido.get()).getAsJsonObject().getAsJsonArray("messages");
            // Compara o resultado observado com o valor esperado para este cenário.
            assertEquals(3, mensagens.size());
            // Exige que a condição de sucesso ou de estado válido seja verdadeira.
            assertTrue(mensagens.get(0).toString().contains("123.45"));
            // Exige que a condição de sucesso ou de estado válido seja verdadeira.
            assertTrue(mensagens.get(0).toString().contains("Salário"));
            // Compara o resultado observado com o valor esperado para este cenário.
            assertEquals("user", mensagens.get(2).getAsJsonObject().get("role").getAsString());
        // Encerra o servidor local mesmo quando uma verificação falha.
        } finally { servidor.stop(0); }
    }

    // Cenário de regressão: respeita opcao de nao compartilhar transacoes.
    @Test void respeitaOpcaoDeNaoCompartilharTransacoes() {
        // Prepara contexto com os dados ou recursos usados nas próximas operações.
        String contexto = EducacaoFinanceiraService.contexto(
                // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
                List.of(new Receita("Trabalho", "Descrição privada", 123.45, "05/06/2026")), false);
        // Exige que a condição proibida, ausente ou já removida seja falsa.
        assertFalse(contexto.contains("Descrição privada"));
        // Exige que a condição proibida, ausente ou já removida seja falsa.
        assertFalse(contexto.contains("123.45"));
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(contexto.contains("não compartilhar"));
    }

    // Cenário de regressão: chave recusada tem mensagem clara sem expor credencial.
    @Test void chaveRecusadaTemMensagemClaraSemExporCredencial() throws Exception {
        // Abre um servidor HTTP somente no loopback, com porta livre, para simular a Groq.
        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        // Define a resposta simulada da API para este endereço local.
        servidor.createContext("/", chamada -> {
            // Lê o corpo da requisição para conferir o JSON enviado ou liberar o fluxo.
            chamada.getRequestBody().readAllBytes();
            // Devolve o código HTTP esperado por este cenário de sucesso ou erro.
            chamada.sendResponseHeaders(401, -1);
            // Libera o recurso ou fecha a janela depois de concluir seu uso.
            chamada.close();
        });
        // Inicia a tarefa ou recurso depois de configurar suas dependências.
        servidor.start();
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // Prepara service com os dados ou recursos usados nas próximas operações.
            var service = new EducacaoFinanceiraService(HttpClient.newHttpClient(),
                    URI.create("http://127.0.0.1:" + servidor.getAddress().getPort() + "/"));
            // Confirma que a entrada inválida produz a exceção esperada, em vez de ser aceita.
            var erro = assertThrows(java.io.IOException.class,
                    () -> service.responder("credencial-privada", "Pergunta", List.of(), List.of(), false));
            // Exige que a condição de sucesso ou de estado válido seja verdadeira.
            assertTrue(erro.getMessage().contains("não foi aceita"));
            // Exige que a condição proibida, ausente ou já removida seja falsa.
            assertFalse(erro.getMessage().contains("credencial-privada"));
        // Encerra o servidor local mesmo quando uma verificação falha.
        } finally { servidor.stop(0); }
    }
}
