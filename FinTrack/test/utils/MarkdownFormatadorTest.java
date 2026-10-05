// Agrupa esta classe na camada utils do projeto.
package utils;

// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import org.junit.jupiter.api.Test;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import static org.junit.jupiter.api.Assertions.*;

// Reúne os cenários de teste de MarkdownFormatador.
class MarkdownFormatadorTest {
    // Cenário de regressão: renderiza titulos listas negrito e tabelas.
    @Test void renderizaTitulosListasNegritoETabelas() {
        // Prepara html com os dados ou recursos usados nas próximas operações.
        String html = MarkdownFormatador.formatar("### Orçamento\n\n**Essenciais**\n\n- Moradia\n- Transporte\n\n| Categoria | Valor |\n| --- | --- |\n| Moradia | R$ 100 |");
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(html.contains("<h3>Orçamento</h3>"));
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(html.contains("<strong>Essenciais</strong>"));
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(html.contains("<li>Moradia</li>"));
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(html.contains("<table>"));
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(html.contains("<th>Categoria</th>"));
    }

    // Cenário de regressão: html recebido nao executa scripts nem carrega imagens.
    @Test void htmlRecebidoNaoExecutaScriptsNemCarregaImagens() {
        // Prepara html com os dados ou recursos usados nas próximas operações.
        String html = MarkdownFormatador.formatar("<script>alert('x')</script>\n\n![imagem](https://exemplo.com/a.png)\n\n[link](javascript:alert(1))");
        // Exige que a condição proibida, ausente ou já removida seja falsa.
        assertFalse(html.contains("<script>"));
        // Exige que a condição proibida, ausente ou já removida seja falsa.
        assertFalse(html.contains("<img"));
        // Exige que a condição proibida, ausente ou já removida seja falsa.
        assertFalse(html.contains("href="));
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(html.contains("default-src 'none'"));
    }
}
