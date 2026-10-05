// Agrupa esta classe na camada utils do projeto.
package utils;

// Disponibiliza coleções ordenadas de registros ou mensagens.
import java.util.List;
// Disponibiliza a interpretação de tabelas Markdown.
import org.commonmark.ext.gfm.tables.TablesExtension;
// Disponibiliza os nós da árvore Markdown.
import org.commonmark.node.*;
// Disponibiliza a transformação de Markdown em uma árvore de nós.
import org.commonmark.parser.Parser;
// Disponibiliza a conversão dos nós Markdown em HTML local.
import org.commonmark.renderer.html.HtmlRenderer;

// Converte Markdown em HTML local, com estilos próprios e restrição de conteúdo externo.
public final class MarkdownFormatador {
    // Impede instanciar esta classe utilitária; a conversão é oferecida por um método estático.
    private MarkdownFormatador() { }

    // Produz o documento HTML usado localmente pelo WebView.
    public static String formatar(String texto) {
        // Habilita tabelas no estilo Markdown além dos elementos da especificação CommonMark.
        var extensoes = List.of(TablesExtension.create());
        // Produz uma árvore de nós Markdown antes de gerar HTML.
        var documento = Parser.builder().extensions(extensoes).build().parse(texto);
        // Percorre a árvore para retirar imagens antes de qualquer carregamento externo.
        documento.accept(new AbstractVisitor() {
            // Substitui o tratamento do nó de imagem antes da renderização HTML.
            @Override public void visit(Image imagem) {
                // Preserva uma indicação textual no lugar da imagem recebida.
                imagem.insertBefore(new Text(imagem.getTitle() == null ? "Imagem" : imagem.getTitle()));
                // Remove o nó de imagem, impedindo que gere uma tag img com endereço externo.
                imagem.unlink();
            }
        });
        // Converte a árvore em HTML; HTML recebido fica escapado e URLs passam por sanitização.
        String conteudo = HtmlRenderer.builder().extensions(extensoes).escapeHtml(true).sanitizeUrls(true)
                // Permite revisar atributos gerados pelo renderizador, sem alterar o texto Markdown.
                .attributeProviderFactory(contexto -> (node, tag, atributos) -> {
                    // Remove href para manter os links apenas como texto visual, sem navegação.
                    if (tag.equals("a")) atributos.remove("href");
                // Finaliza o renderizador e transforma a árvore já revisada em HTML.
                }).build().render(documento);
        // O documento local usa CSS da identidade visual e uma política de conteúdo restritiva.
        // default-src 'none' bloqueia recursos externos; style-src autoriza somente o estilo inserido aqui.
        // O controller mede a altura com uma expressão fixa, sem executar texto devolvido pelo Edu.
        return """
                <!doctype html><html lang="pt-BR"><head><meta charset="UTF-8">
                <meta http-equiv="Content-Security-Policy" content="default-src 'none'; style-src 'unsafe-inline'">
                <style>
                html,body { margin:0; padding:0; background:#f0f5ff; color:#17263e;
                    font:14px 'Segoe UI',sans-serif; line-height:1.6; overflow:hidden; }
                #conteudo { padding:1px 0; overflow-wrap:break-word; }
                h1,h2,h3,h4,h5,h6 { color:#0b1830; line-height:1.3; margin:18px 0 10px; }
                h1 { font-size:24px; } h2 { font-size:21px; } h3 { font-size:18px; }
                p { margin:0 0 12px; } ul,ol { padding-left:24px; margin:8px 0 14px; }
                li { margin:5px 0; } strong { font-weight:600; }
                blockquote { border-left:3px solid #0069e8; margin:12px 0; padding:8px 14px; background:#deebff; }
                blockquote p { margin:0; } a { color:#0054bd; text-decoration:underline; }
                table { border-collapse:collapse; width:100%; margin:14px 0; background:white; }
                th,td { border:1px solid #cddbf0; padding:9px 12px; text-align:left; }
                th { background:#deebff; font-weight:600; } tr:nth-child(even) { background:#f7faff; }
                code { font-family:Consolas,monospace; background:#e1eafa; border-radius:4px; padding:2px 4px; }
                pre { white-space:pre-wrap; background:#e1eafa; padding:12px; border-radius:6px; }
                pre code { padding:0; } hr { border:0; border-top:1px solid #cddbf0; margin:16px 0; }
                </style></head><body><div id="conteudo">
                """ + conteudo + "</div></body></html>";
    }
}
