// Agrupa esta classe na camada controller do projeto.
package controller;

// Referencia FinApp, componente da camada app utilizado neste fluxo.
import app.FinApp;
// Referencia PersistenciaException, componente da camada exceptions utilizado neste fluxo.
import exceptions.PersistenciaException;
// Disponibiliza o tratamento de falhas de arquivo ou comunicação.
import java.io.IOException;
// Disponibiliza a apresentação de valores monetários conforme a região.
import java.text.NumberFormat;
// Disponibiliza coleções ordenadas de registros ou mensagens.
import java.util.List;
// Disponibiliza a configuração regional e a normalização de texto.
import java.util.Locale;
// Disponibiliza o valor observável utilizado pelas células da tabela.
import javafx.beans.property.ReadOnlyStringWrapper;
// Disponibiliza a associação de campos e métodos ao arquivo de interface.
import javafx.fxml.FXML;
// Disponibiliza o carregamento de componentes definidos em FXML.
import javafx.fxml.FXMLLoader;
// Disponibiliza a raiz de uma árvore de componentes gráficos.
import javafx.scene.Parent;
// Disponibiliza o conjunto de componentes de uma janela.
import javafx.scene.Scene;
// Disponibiliza os controles visuais da tela.
import javafx.scene.control.*;
// Disponibiliza o bloqueio de interação durante janelas modais.
import javafx.stage.Modality;
// Disponibiliza as janelas da aplicação.
import javafx.stage.Stage;
// Referencia Transacao, componente da camada model utilizado neste fluxo.
import model.Transacao;
// Referencia TransacaoMensal, componente da camada model utilizado neste fluxo.
import model.TransacaoMensal;
// Referencia RepositorioGenerico, componente da camada repository utilizado neste fluxo.
import repository.RepositorioGenerico;
// Referencia TransacaoService, componente da camada service utilizado neste fluxo.
import service.TransacaoService;

// Coordena a tabela, os filtros, o CRUD e a abertura das demais telas.
public class TransacoesController {
    // O FXMLLoader injeta este componente pelo fx:id. Tabela que apresenta os registros ou os agrupamentos da consulta.
    @FXML private TableView<Transacao> tabela;
    // O FXMLLoader injeta este componente pelo fx:id. Mantém acesso ao controle definido no arquivo FXML.
    @FXML private TableColumn<Transacao, String> colunaData;
    // O FXMLLoader injeta este componente pelo fx:id. Mantém acesso ao controle definido no arquivo FXML.
    @FXML private TableColumn<Transacao, String> colunaDescricao;
    // O FXMLLoader injeta este componente pelo fx:id. Mantém acesso ao controle definido no arquivo FXML.
    @FXML private TableColumn<Transacao, String> colunaCategoria;
    // O FXMLLoader injeta este componente pelo fx:id. Mantém acesso ao controle definido no arquivo FXML.
    @FXML private TableColumn<Transacao, String> colunaTipo;
    // O FXMLLoader injeta este componente pelo fx:id. Mantém acesso ao controle definido no arquivo FXML.
    @FXML private TableColumn<Transacao, String> colunaValor;
    // O FXMLLoader injeta este componente pelo fx:id. Mantém acesso ao controle definido no arquivo FXML.
    @FXML private TableColumn<Transacao, String> colunaMensal;
    // O FXMLLoader injeta este componente pelo fx:id. Rótulo com o total de entradas da seleção atual.
    @FXML private Label totalReceitas;
    // O FXMLLoader injeta este componente pelo fx:id. Rótulo com o total de saídas da seleção atual.
    @FXML private Label totalDespesas;
    // O FXMLLoader injeta este componente pelo fx:id. Rótulo do saldo calculado para o conjunto apresentado.
    @FXML private Label saldo;
    // O FXMLLoader injeta este componente pelo fx:id. Mensagem de orientação ou resultado da operação atual.
    @FXML private Label status;
    // O FXMLLoader injeta este componente pelo fx:id. Entrada de texto para pesquisar parte da descrição.
    @FXML private TextField busca;
    // O FXMLLoader injeta este componente pelo fx:id. Seleção opcional entre todas as movimentações, receitas e despesas.
    @FXML private ComboBox<String> filtroTipo;
    // O FXMLLoader injeta este componente pelo fx:id. Botão de abertura do formulário de cadastro.
    @FXML private Button nova;
    // O FXMLLoader injeta este componente pelo fx:id. Botão de edição, habilitado somente com uma linha selecionada.
    @FXML private Button editar;
    // O FXMLLoader injeta este componente pelo fx:id. Botão de exclusão com confirmação.
    @FXML private Button remover;
    // O FXMLLoader injeta este componente pelo fx:id. Botão de abertura do relatório por período e categoria.
    @FXML private Button relatorio;
    // O FXMLLoader injeta este componente pelo fx:id. Botão que reconsulta os dados ou aplica o filtro atual.
    @FXML private Button atualizar;
    // O FXMLLoader injeta este componente pelo fx:id. Botão que remove os critérios de filtro.
    @FXML private Button limpar;
    // O FXMLLoader injeta este componente pelo fx:id. Botão de acesso à conversa financeira integrada.
    @FXML private Button edu;
    // Dependência que será fornecida na inicialização; a classe não acessa armazenamento por conta própria.
    private TransacaoService service;
    // Abertura de páginas externas recebida de HostServices.
    private java.util.function.Consumer<String> abrirPagina;
    // Referência da janela usada para controlar abertura e encerramento.
    private Stage janelaEdu;
    // Permite encerrar a conversa e cancelar sua Task no fechamento da aplicação.
    private EduController eduController;
    // Devolve currency instance sem alterar o estado do objeto.
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    // Autoriza o FXMLLoader a chamar ou preencher o membro privado associado à tela.
    @FXML
    // O FXMLLoader chama este método depois de associar os campos anotados com @FXML.
    private void initialize() {
        // Associa cada célula à data já normalizada pelo modelo.
        colunaData.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getData()));
        // O comparador usa datas reais, evitando ordenar dd/MM/yyyy como texto.
        java.time.format.DateTimeFormatter formatoData = java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu");
        // Permite ordenar corretamente inclusive registros de meses e anos diferentes.
        colunaData.setComparator((a, b) -> java.time.LocalDate.parse(a, formatoData).compareTo(java.time.LocalDate.parse(b, formatoData)));
        // Remove a formatação monetária antes de comparar os valores numericamente.
        colunaValor.setComparator((a, b) -> new java.math.BigDecimal(a.replaceAll("[^0-9,-]", "").replace(',', '.'))
                .compareTo(new java.math.BigDecimal(b.replaceAll("[^0-9,-]", "").replace(',', '.'))));
        // Extrai a descrição do objeto de cada linha.
        colunaDescricao.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getDescricao()));
        // Extrai a categoria persistida, sem depender das sugestões do formulário.
        colunaCategoria.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getCategoria()));
        // Converte os tipos internos para os rótulos amigáveis da tabela.
        colunaTipo.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTipo().equals("receita") ? "Receita" : "Despesa"));
        // Formata BigDecimal em reais somente para apresentação.
        colunaValor.setCellValueFactory(c -> new ReadOnlyStringWrapper(moeda.format(c.getValue().getValorDecimal())));
        // Exibe o dia da subclasse mensal; registros comuns mostram um traço.
        colunaMensal.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue() instanceof TransacaoMensal mensal
                ? "Dia " + mensal.getDiaVencimento() : "-"));
        // Explica quando o filtro ou o banco não possui nenhuma transação.
        tabela.setPlaceholder(new Label("Nenhuma transação encontrada."));
        // A edição só fica disponível quando uma linha está selecionada.
        editar.disableProperty().bind(tabela.getSelectionModel().selectedItemProperty().isNull());
        // A exclusão também depende de uma seleção válida.
        remover.disableProperty().bind(tabela.getSelectionModel().selectedItemProperty().isNull());
        // A primeira opção representa todas as movimentações.
        filtroTipo.getItems().setAll("Todos os tipos", "Receita", "Despesa");
        // Começa sem restringir o tipo financeiro.
        filtroTipo.getSelectionModel().selectFirst();
        // O módulo nativo pode ser aberto sem instalar o ambiente Python.
        edu.setDisable(false);
        // Abre a tela JavaFX do educador financeiro.
        edu.setOnAction(e -> abrirEdu());
        // Um argumento null indica ao formulário que deve cadastrar, não editar.
        nova.setOnAction(e -> abrirFormulario(null));
        // Entrega ao formulário o objeto da linha selecionada.
        editar.setOnAction(e -> abrirFormulario(tabela.getSelectionModel().getSelectedItem()));
        // A remoção seguirá o fluxo de confirmação antes de modificar o banco.
        remover.setOnAction(e -> removerSelecionada());
        // Abre a janela de relatório com o serviço compartilhado.
        relatorio.setOnAction(e -> abrirRelatorio());
        // O botão aplica a busca e os filtros atuais.
        atualizar.setOnAction(e -> carregar());
        // Retira busca e tipo antes de consultar novamente.
        limpar.setOnAction(e -> {
            // Remove o conteúdo anterior para que ele não seja reutilizado no próximo fluxo.
            busca.clear();
            // Começa sem restringir o tipo financeiro.
            filtroTipo.getSelectionModel().selectFirst();
            // Relê o banco e recalcula tabela e totais conforme os filtros atuais.
            carregar();
        });
        // Enter na busca aplica o filtro da tabela.
        busca.setOnAction(e -> carregar());
        // Não consulta antes de o serviço ter sido fornecido ao controller.
        filtroTipo.setOnAction(e -> { if (service != null) carregar(); });
    }

    // Recebe dependências criadas pela aplicação; initialize cuida apenas dos controles do FXML.
    public void configurar(TransacaoService service) {
        // Compartilha a instância que usa o DAO aberto na inicialização.
        this.service = service;
        // Relê o banco e recalcula tabela e totais conforme os filtros atuais.
        carregar();
    }

    // Recebe a função que abre páginas externas pelo mecanismo da aplicação JavaFX.
    public void configurarNavegador(java.util.function.Consumer<String> abrirPagina) {
        // Guarda HostServices para o Edu abrir a página de criação da chave.
        this.abrirPagina = abrirPagina;
    }

    // Abre ou traz para frente a janela do educador financeiro.
    private void abrirEdu() {
        // Evita abrir várias janelas de conversa na mesma sessão.
        if (janelaEdu != null && janelaEdu.isShowing()) { janelaEdu.toFront(); return; }
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // Cada tela é carregada de um recurso empacotado junto do aplicativo.
            FXMLLoader loader = new FXMLLoader(FinApp.class.getResource("/view/edu.fxml"));
            // Constrói os controles e executa initialize do controller do FXML.
            Parent tela = loader.load();
            // Cria a janela com o estilo e o vínculo de propriedade da tela principal.
            janelaEdu = criarJanela(tela, "Edu — Educação financeira", 1120, 790);
            // Obtém o controller criado para este FXML.
            eduController = loader.getController();
            // O Edu recebe exatamente o serviço que consulta as transações da tela principal.
            eduController.configurar(service, new service.EducacaoFinanceiraService(), janelaEdu);
            // O cofre usa a conta atual do Windows; os dados não entram no SQLite.
            eduController.configurarCofre(new service.ChaveGroqService());
            // Habilita a página da chave somente quando a função de navegador está disponível.
            if (abrirPagina != null) eduController.configurarAcessoChave(abrirPagina);
            // Exibe a conversa depois de configurar serviço, cofre e navegador.
            janelaEdu.show();
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (IOException | RuntimeException e) {
            // Transforma a falha da operação em uma mensagem visual associada à janela.
            mostrarErro("Não foi possível abrir a tela do Edu.");
        }
    }

    // Cancela a consulta em andamento e fecha a janela do Edu.
    public void encerrarEdu() {
        // Cancela a tarefa e limpa o estado se a tela do Edu chegou a ser criada.
        if (eduController != null) eduController.encerrar();
        // Fecha a janela auxiliar no encerramento da aplicação.
        if (janelaEdu != null) janelaEdu.close();
    }

    // Consulta o armazenamento e atualiza o estado usado por esta classe.
    private void carregar() {
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // Traduz a seleção amigável para o valor interno do filtro.
            String tipo = filtroTipo.getSelectionModel().getSelectedIndex() == 0
                    ? null : filtroTipo.getValue().toLowerCase(Locale.ROOT);
            // Relê o banco e aplica os critérios da seleção atual.
            List<Transacao> transacoes = service.filtrar(busca.getText(), tipo, null, null, null);
            // Troca o conteúdo apresentado sem expor a coleção interna do serviço.
            tabela.getItems().setAll(transacoes);
            // O total acompanha somente as transações atualmente filtradas.
            totalReceitas.setText(moeda.format(TransacaoService.calcularTotal(transacoes, "receita")));
            // Apresenta as despesas da mesma seleção usada pela tabela.
            totalDespesas.setText(moeda.format(TransacaoService.calcularTotal(transacoes, "despesa")));
            // Calcula o impacto financeiro dos registros selecionados.
            saldo.setText(moeda.format(TransacaoService.calcularSaldo(transacoes)));
            // Remove estilos de estados anteriores antes de aplicar o estado atual.
            saldo.getStyleClass().removeAll("positivo", "negativo");
            // Aplica a classe visual correspondente, definida na folha CSS.
            saldo.getStyleClass().add(TransacaoService.calcularSaldo(transacoes).signum() < 0 ? "negativo" : "positivo");
            // O curinga ? permite contar sem depender do tipo específico dos elementos.
            status.setText(RepositorioGenerico.contar(transacoes) + " transação(ões) • Totais da seleção atual");
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (PersistenciaException e) {
            // Transforma a falha da operação em uma mensagem visual associada à janela.
            mostrarErro(e.getMessage());
        }
    }

    // Cria uma janela modal para cadastrar ou editar a transação selecionada.
    private void abrirFormulario(Transacao transacao) {
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // Cada tela é carregada de um recurso empacotado junto do aplicativo.
            FXMLLoader loader = new FXMLLoader(FinApp.class.getResource("/view/transacao-formulario.fxml"));
            // Constrói os controles e executa initialize do controller do FXML.
            Parent tela = loader.load();
            // O tamanho inicial deixa espaço para os controles da tela auxiliar.
            Stage janela = criarJanela(tela, transacao == null ? "Nova transação" : "Editar transação", 640, 650);
            // Obtém o controller do formulário para passar dependências e o registro original.
            TransacaoController controller = loader.getController();
            // Reutiliza o DAO da sessão e informa se a janela está cadastrando ou editando.
            controller.configurar(service, transacao, janela);
            // Aguarda o fechamento modal antes de continuar este fluxo do controller.
            janela.showAndWait();
            // Cancelar não altera o banco; recarrega somente após uma gravação.
            if (controller.isSalvo()) carregar();
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (IOException e) {
            // Transforma a falha da operação em uma mensagem visual associada à janela.
            mostrarErro("Não foi possível abrir o formulário.");
        }
    }

    // Abre a janela de filtros, totais e gráfico financeiro.
    private void abrirRelatorio() {
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // Cada tela é carregada de um recurso empacotado junto do aplicativo.
            FXMLLoader loader = new FXMLLoader(FinApp.class.getResource("/view/relatorio.fxml"));
            // Constrói os controles e executa initialize do controller do FXML.
            Parent tela = loader.load();
            // O tamanho inicial deixa espaço para os controles da tela auxiliar.
            Stage janela = criarJanela(tela, "Relatório financeiro", 980, 720);
            // Fornece ao relatório o mesmo serviço usado no cadastro e na tabela.
            RelatorioController controller = loader.getController();
            // O relatório recebe o mesmo serviço e o vínculo com sua janela.
            controller.configurar(service, janela);
            // Aguarda o fechamento modal antes de continuar este fluxo do controller.
            janela.showAndWait();
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (IOException e) {
            // Transforma a falha da operação em uma mensagem visual associada à janela.
            mostrarErro("Não foi possível abrir o relatório.");
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (PersistenciaException e) {
            // Transforma a falha da operação em uma mensagem visual associada à janela.
            mostrarErro(e.getMessage());
        }
    }

    // Aplica o mesmo estilo e associa a janela à tela principal.
    private Stage criarJanela(Parent tela, String titulo, double largura, double altura) {
        // Cria uma janela auxiliar, sem iniciar outra aplicação JavaFX.
        Stage janela = new Stage();
        // Usa a logomarca dos recursos empacotados como ícone da janela auxiliar.
        janela.getIcons().add(new javafx.scene.image.Image(FinApp.class.getResource("/images/fintrack.png").toExternalForm()));
        // Associa a janela auxiliar à janela principal.
        janela.initOwner(tabela.getScene().getWindow());
        // Bloqueia interação com a janela principal enquanto a auxiliar estiver aberta.
        janela.initModality(Modality.WINDOW_MODAL);
        // Define o título que identifica a janela ou diálogo.
        janela.setTitle(titulo);
        // Define a cena inicial da janela auxiliar.
        Scene scene = new Scene(tela, largura, altura);
        // Aplica a mesma identidade visual das outras telas.
        scene.getStylesheets().add(FinApp.class.getResource("/css/fintrack.css").toExternalForm());
        // Associa o conteúdo já carregado à janela criada.
        janela.setScene(scene);
        // Garante largura mínima para os componentes do formulário ou relatório.
        janela.setMinWidth(largura);
        // Garante altura mínima para o conteúdo da janela auxiliar.
        janela.setMinHeight(altura);
        // Devolve a janela pronta para ser configurada e apresentada.
        return janela;
    }

    // Só altera o banco depois da confirmação explícita do usuário.
    private void removerSelecionada() {
        // A operação deve usar o objeto atualmente selecionado.
        Transacao transacao = tabela.getSelectionModel().getSelectedItem();
        // Protege o fluxo caso a seleção seja perdida antes do evento.
        if (transacao == null) return;
        // Oferece cancelar e confirmar antes de excluir qualquer registro.
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                "Remover a transação “" + transacao.getDescricao() + "”?", ButtonType.CANCEL, ButtonType.OK);
        // Mantém o diálogo de confirmação associado à tela principal.
        confirmacao.initOwner(tabela.getScene().getWindow());
        // Define o título que identifica a janela ou diálogo.
        confirmacao.setTitle("Remover transação");
        // Apresenta o contexto do diálogo antes da mensagem detalhada.
        confirmacao.setHeaderText("Confirme a remoção");
        // Só OK autoriza a exclusão; fechar o diálogo equivale a cancelar.
        if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
            try {
                // Uma linha removida por outra operação precisa gerar aviso e recarregamento.
                if (!service.remover(transacao.getId())) {
                    // Transforma a falha da operação em uma mensagem visual associada à janela.
                    mostrarErro("A transação já foi removida. A lista será atualizada.");
                }
                // Relê o banco e recalcula tabela e totais conforme os filtros atuais.
                carregar();
            // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
            } catch (PersistenciaException e) {
                // Transforma a falha da operação em uma mensagem visual associada à janela.
                mostrarErro(e.getMessage());
            }
        }
    }

    // Apresenta uma mensagem da aplicação em uma caixa de diálogo.
    private void mostrarErro(String mensagem) {
        // Cria uma mensagem de erro com ação de fechamento.
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensagem, ButtonType.OK);
        // O diálogo só recebe proprietário se a tabela já estiver em uma cena.
        if (tabela.getScene() != null) alerta.initOwner(tabela.getScene().getWindow());
        // Define o título que identifica a janela ou diálogo.
        alerta.setTitle("FinTrack");
        // Apresenta o contexto do diálogo antes da mensagem detalhada.
        alerta.setHeaderText("Não foi possível concluir a operação.");
        // Exibe o diálogo e aguarda a decisão do usuário antes de continuar.
        alerta.showAndWait();
    }
}
