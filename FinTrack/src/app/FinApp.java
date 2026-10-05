// Agrupa esta classe na camada app do projeto.
package app;

// Referencia TransacoesController, componente da camada controller utilizado neste fluxo.
import controller.TransacoesController;
// Referencia Conexao, componente da camada dao utilizado neste fluxo.
import dao.Conexao;
// Referencia TransacaoDAO, componente da camada dao utilizado neste fluxo.
import dao.TransacaoDAO;
// Disponibiliza o tratamento de falhas de arquivo ou comunicação.
import java.io.IOException;
// Disponibiliza o ciclo de vida da aplicação JavaFX.
import javafx.application.Application;
// Disponibiliza o carregamento de componentes definidos em FXML.
import javafx.fxml.FXMLLoader;
// Disponibiliza o conjunto de componentes de uma janela.
import javafx.scene.Scene;
// Disponibiliza Alert para as operações desta classe.
import javafx.scene.control.Alert;
// Disponibiliza as janelas da aplicação.
import javafx.stage.Stage;
// Referencia MigracaoDados, componente da camada service utilizado neste fluxo.
import service.MigracaoDados;
// Referencia TransacaoService, componente da camada service utilizado neste fluxo.
import service.TransacaoService;

// Ponto de entrada JavaFX: abre o banco, carrega a tela principal e encerra os recursos.
public class FinApp extends Application {
    // Mantém o DAO aberto durante a sessão para compartilhá-lo com os serviços e fechá-lo no stop.
    private TransacaoDAO dao;
    // Referência da tela principal para encerrar seus recursos auxiliares no stop.
    private TransacoesController controller;

    // Indica que este membro implementa ou substitui um comportamento definido no tipo pai.
    @Override
    // Primeira etapa do ciclo de vida da aplicação JavaFX.
    public void start(Stage stage) {
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // Abre o banco e prepara o DAO uma vez para a sessão da aplicação.
            dao = new TransacaoDAO(Conexao.abrir());
            // Executa a importação antiga somente se o marcador ainda não existir.
            MigracaoDados.importar(Conexao.pastaDados().resolve("transacoes.csv"), dao);
            // Localiza a tela principal no classpath, incluindo execução pelo Maven.
            FXMLLoader loader = new FXMLLoader(FinApp.class.getResource("/view/transacoes.fxml"));
            // Carrega os componentes FXML e define o tamanho inicial da cena.
            Scene scene = new Scene(loader.load(), 1120, 740);
            // Obtém o controller associado pelo fx:controller da tela.
            controller = loader.getController();
            // Compartilha o mesmo serviço e DAO com a interface.
            controller.configurar(new TransacaoService(dao));
            // Fornece HostServices para abrir a página de criação da chave no navegador.
            controller.configurarNavegador(getHostServices()::showDocument);
            // Aplica a identidade visual comum a todos os controles da cena.
            scene.getStylesheets().add(FinApp.class.getResource("/css/fintrack.css").toExternalForm());
            // Carrega a logomarca empacotada nos recursos, sem depender de uma pasta externa.
            stage.getIcons().add(new javafx.scene.image.Image(FinApp.class.getResource("/images/fintrack.png").toExternalForm()));
            // Define o título que identifica a janela ou diálogo.
            stage.setTitle("FinTrack - Finanças pessoais");
            // Evita reduzir a janela a uma largura que comprometa a tabela.
            stage.setMinWidth(940);
            // Reserva altura mínima para filtros, tabela e rodapé.
            stage.setMinHeight(640);
            // Associa a cena carregada à janela principal.
            stage.setScene(scene);
            // Exibe a interface depois de concluir a configuração do banco e do controller.
            stage.show();
        // Trata falhas de carregamento de tela, migração e persistência na inicialização.
        } catch (IOException | RuntimeException e) {
            // Uma falha de inicialização precisa ser apresentada mesmo sem a tela principal pronta.
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            // Define o título que identifica a janela ou diálogo.
            alerta.setTitle("FinTrack");
            // Apresenta o contexto do diálogo antes da mensagem detalhada.
            alerta.setHeaderText("Não foi possível iniciar o FinTrack.");
            // Apresenta a mensagem da falha para orientar a correção.
            alerta.setContentText(e.getMessage());
            // Exibe o diálogo e aguarda a decisão do usuário antes de continuar.
            alerta.showAndWait();
            // Encerra o JavaFX quando a inicialização não pode ser concluída.
            javafx.application.Platform.exit();
        }
    }

    // Indica que este membro implementa ou substitui um comportamento definido no tipo pai.
    @Override
    // Etapa de encerramento do JavaFX; libera banco, janelas e tarefas.
    public void stop() {
        // Só encerra a janela auxiliar se a tela principal chegou a ser criada.
        if (controller != null) controller.encerrarEdu();
        // Evita acessar um DAO ausente quando a inicialização falhou.
        if (dao != null) {
            // Fecha a conexão mantida durante a sessão.
            dao.close();
        }
    }

    // Entrada chamada pela JVM; inicia a versão correspondente da aplicação.
    public static void main(String[] args) {
        // Entrega o ciclo de vida start/stop ao JavaFX.
        launch(args);
    }
}
