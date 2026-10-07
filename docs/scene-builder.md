# Revisão das telas no Scene Builder

Os arquivos FXML foram definidos no projeto e revisados visualmente no Scene Builder 26.0.0. A revisão verificou a abertura das quatro telas, o carregamento da logomarca e do CSS, a disposição dos controles e as dimensões de referência.

## Abrir as telas

Obtenha o editor na [página oficial da Gluon](https://gluonhq.com/products/scene-builder/). No Scene Builder, escolha **Open Project** e abra um dos arquivos abaixo, a partir da raiz do repositório:

| Tela | Arquivo | Controller | Dimensões de referência |
| --- | --- | --- | --- |
| Principal | FinTrack/resources/view/transacoes.fxml | controller.TransacoesController | 1120 × 740 |
| Cadastro e edição | FinTrack/resources/view/transacao-formulario.fxml | controller.TransacaoController | 640 × 650 |
| Relatório | FinTrack/resources/view/relatorio.fxml | controller.RelatorioController | 980 × 720 |
| Edu | FinTrack/resources/view/edu.fxml | controller.EduController | 1120 × 790 |

A versão do editor não altera a versão JavaFX da aplicação: as dependências continuam definidas no pom.xml.

## O que conferir no editor

1. No painel **Hierarchy**, examine BorderPane, VBox, HBox e os controles da tela.
2. No painel **Controller**, confira a classe associada ao documento.
3. Selecione um controle e confira seu fx:id na seção **Code** do Inspector. O identificador deve corresponder ao campo @FXML do controller.
4. Na raiz, confira a folha de estilo em **Stylesheets** e as dimensões em **Layout**.
5. Use **Preview → Show Preview in Window** para visualizar a composição sem os painéis do editor.

O CSS usa o caminho relativo `@../css/fintrack.css`, e a imagem usa `@../images/fintrack.png`. Preserve a organização das pastas para que os recursos sejam encontrados no editor e no aplicativo.

## Diferença entre a prévia e o aplicativo

O Scene Builder apresenta os componentes declarados no FXML sem iniciar os serviços do FinTrack. Por isso, tabelas e gráficos podem aparecer vazios; opções de seleção, saldos e mensagens são preenchidos pelos controllers durante a execução.

A tela do Edu apresenta a estrutura da conversa, o campo da chave e os botões. As mensagens e os componentes WebView de Markdown são criados dinamicamente pelo EduController.

As dimensões da raiz são referências para a edição. A janela do aplicativo continua permitindo o redimensionamento de acordo com sua configuração JavaFX.

## Validar depois de editar

Preserve fx:controller, fx:id, caminhos dos recursos e os comentários didáticos ao salvar alterações. Confira o diff do arquivo para identificar mudanças feitas pelo editor.

Na raiz do repositório, execute com JDK 17 ou superior:

```powershell
mvn '-Dmaven.repo.local=.m2/repository' '-Dfintrack.testes.interface=true' '-Dtest=InterfaceTest' test
```

Os testes verificam o carregamento das telas, os eventos de cadastro e edição, os filtros, o relatório e os fluxos do Edu, usando dados e credenciais fictícios.

Depois, execute o aplicativo e confira as telas com as instruções do [README](../README.md). O Scene Builder é uma ferramenta de desenvolvimento e não precisa ser instalado para usar o FinTrack.
