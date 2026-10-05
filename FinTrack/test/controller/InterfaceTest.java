// Agrupa esta classe na camada controller do projeto.
package controller;

// Referencia FinApp, componente da camada app utilizado neste fluxo.
import app.FinApp;
// Referencia Conexao, componente da camada dao utilizado neste fluxo.
import dao.Conexao;
// Referencia TransacaoDAO, componente da camada dao utilizado neste fluxo.
import dao.TransacaoDAO;
// Disponibiliza o armazenamento de capturas da interface.
import java.awt.image.BufferedImage;
// Disponibiliza leitura, gravação e criação de arquivos e diretórios.
import java.nio.file.Files;
// Disponibiliza caminhos sem depender de separadores montados manualmente.
import java.nio.file.Path;
// Disponibiliza a execução de verificações de interface no thread JavaFX.
import java.util.concurrent.FutureTask;
// Disponibiliza a conversão e o uso de unidades de tempo nos limites de espera.
import java.util.concurrent.TimeUnit;
// Disponibiliza a gravação das capturas em PNG.
import javax.imageio.ImageIO;
// Disponibiliza a execução de atualizações no thread JavaFX.
import javafx.application.Platform;
// Disponibiliza o carregamento de componentes definidos em FXML.
import javafx.fxml.FXMLLoader;
// Disponibiliza a raiz de uma árvore de componentes gráficos.
import javafx.scene.Parent;
// Disponibiliza o conjunto de componentes de uma janela.
import javafx.scene.Scene;
// Disponibiliza os controles visuais da tela.
import javafx.scene.control.*;
// Disponibiliza a imagem produzida pelo snapshot da cena JavaFX.
import javafx.scene.image.WritableImage;
// Disponibiliza as janelas da aplicação.
import javafx.stage.Stage;
// Referencia Receita, componente da camada model utilizado neste fluxo.
import model.Receita;
// Referencia Despesa, componente da camada model utilizado neste fluxo.
import model.Despesa;
// Referencia Transacao, componente da camada model utilizado neste fluxo.
import model.Transacao;
// Referencia TransacaoMensal, componente da camada model utilizado neste fluxo.
import model.TransacaoMensal;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import org.junit.jupiter.api.*;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
// Referencia TransacaoService, componente da camada service utilizado neste fluxo.
import service.TransacaoService;
// Disponibiliza os recursos de teste JUnit para preparação, execução e verificações.
import static org.junit.jupiter.api.Assertions.*;

// Exige ativação explícita para não abrir janelas ou iniciar servidor nas execuções comuns.
@EnabledIfSystemProperty(named = "fintrack.testes.interface", matches = "true")
// Reúne os cenários de teste de Interface.
class InterfaceTest {
    // Prepara o recurso antes de devolvê-lo ao chamador.
    @BeforeAll static void iniciar() throws Exception {
        // Encapsula as operações de interface para executá-las no thread JavaFX e aguardar o resultado.
        FutureTask<Void> inicio = new FutureTask<>(() -> {
            // Mantém o JavaFX ativo entre os cenários mesmo depois de fechar a última janela.
            Platform.setImplicitExit(false);
            // Devolve o sinal de resultado que orientará o próximo passo do chamador.
            return null;
        });
        // Inicializa o runtime JavaFX uma única vez antes dos testes de interface.
        Platform.startup(inicio);
        // Espera a etapa de teste com prazo definido, evitando uma execução presa indefinidamente.
        inicio.get(15, TimeUnit.SECONDS);
    }

    // Libera recursos ou limpa o estado ao terminar a sessão.
    @AfterAll static void encerrar() { Platform.exit(); }

    // Cenário de regressão: valida telas cadastro edicao filtros e relatorio.
    @Test void validaTelasCadastroEdicaoFiltrosERelatorio() throws Exception {
        // Encapsula as operações de interface para executá-las no thread JavaFX e aguardar o resultado.
        FutureTask<Void> tarefa = new FutureTask<>(() -> {
            // Cria um banco isolado em memória; este cenário não usa as transações pessoais.
            try (TransacaoDAO dao = new TransacaoDAO(Conexao.abrir("jdbc:sqlite::memory:"))) {
                // Prepara service com os dados ou recursos usados nas próximas operações.
                TransacaoService service = new TransacaoService(dao);
                // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
                service.adicionar(new Receita("Salário", "Pagamento mensal", 5000, "05/06/2026"));
                // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
                service.adicionar(new Despesa("Alimentação", "Compras do mês", 380.75, "10/06/2026"));
                // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
                service.adicionar(new Despesa("Transporte", "Combustível", 250, "15/06/2026"));
                // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
                service.adicionar(new TransacaoMensal("Aluguel", 1500, "despesa", "Moradia", "10/06/2026", 10));

                // Seleciona o arquivo FXML usado para montar a tela deste cenário.
                FXMLLoader principalLoader = loader("transacoes.fxml");
                // Carrega ou recupera a raiz dos controles visuais que serão verificados.
                Parent principal = principalLoader.load();
                // Abre a janela de teste com as dimensões da tela correspondente.
                Stage principalStage = janela(principal, 1120, 740);
                // Obtém o controller criado pelo FXMLLoader para fornecer suas dependências.
                TransacoesController principalController = principalLoader.getController();
                // Fornece os serviços e a janela utilizados por este cenário.
                principalController.configurar(service);
                // Localiza a tabela pelo fx:id para conferir os itens e a ordenação.
                TableView<?> tabela = (TableView<?>) principal.lookup("#tabela");
                // Compara o resultado observado com o valor esperado para este cenário.
                assertEquals(4, tabela.getItems().size());
                // Exige que a condição proibida, ausente ou já removida seja falsa.
                assertFalse(((Button) principal.lookup("#nova")).isDisabled());
                // Exige que a condição de sucesso ou de estado válido seja verdadeira.
                assertTrue(((Button) principal.lookup("#editar")).isDisabled());
                // Salva uma imagem da tela para conferir também a apresentação visual.
                capturar(principal, "transacoes.png");
                // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
                ((TextField) principal.lookup("#busca")).setText("COMPRAS");
                // Simula a ação do usuário no controle sem acessar a API externa.
                ((Button) principal.lookup("#atualizar")).fire();
                // Compara o resultado observado com o valor esperado para este cenário.
                assertEquals(1, tabela.getItems().size());
                // Simula a ação do usuário no controle sem acessar a API externa.
                ((Button) principal.lookup("#limpar")).fire();
                // Compara o resultado observado com o valor esperado para este cenário.
                assertEquals(4, tabela.getItems().size());
                // Ordena pela coluna indicada para verificar seu comparador.
                ordenar(tabela, 4);
                // Compara o resultado observado com o valor esperado para este cenário.
                assertEquals(250, ((Transacao) tabela.getItems().get(0)).getValor());
                // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
                Receita posterior = new Receita("Outros", "Data posterior", 10, "01/07/2026");
                // Inclui o registro por meio da camada responsável pela coleção ou persistência.
                service.adicionar(posterior);
                // Simula a ação do usuário no controle sem acessar a API externa.
                ((Button) principal.lookup("#atualizar")).fire();
                // Ordena pela coluna indicada para verificar seu comparador.
                ordenar(tabela, 0);
                // Compara o resultado observado com o valor esperado para este cenário.
                assertEquals("05/06/2026", ((Transacao) tabela.getItems().get(0)).getData());
                // Compara o resultado observado com o valor esperado para este cenário.
                assertEquals("01/07/2026", ((Transacao) tabela.getItems().get(4)).getData());
                // Remove o registro do cenário pelo identificador persistido.
                service.remover(posterior.getId());
                // Simula a ação do usuário no controle sem acessar a API externa.
                ((Button) principal.lookup("#atualizar")).fire();
                // Compara o resultado observado com o valor esperado para este cenário.
                assertEquals(4, tabela.getItems().size());

                // Seleciona o arquivo FXML usado para montar a tela deste cenário.
                FXMLLoader formLoader = loader("transacao-formulario.fxml");
                // Carrega ou recupera a raiz dos controles visuais que serão verificados.
                Parent form = formLoader.load();
                // Abre a janela de teste com as dimensões da tela correspondente.
                Stage formStage = janela(form, 640, 650);
                // Obtém o controller criado pelo FXMLLoader para fornecer suas dependências.
                TransacaoController formController = formLoader.getController();
                // Fornece os serviços e a janela utilizados por este cenário.
                formController.configurar(service, null, formStage);
                // Simula a ação do usuário no controle sem acessar a API externa.
                ((Button) form.lookup("#salvar")).fire();
                // Exige que a condição proibida, ausente ou já removida seja falsa.
                assertFalse(formController.isSalvo());
                // Exige que a condição proibida, ausente ou já removida seja falsa.
                assertFalse(((Label) form.lookup("#erro")).getText().isEmpty());
                // Compara o resultado observado com o valor esperado para este cenário.
                assertEquals(4, service.listar().size());
                // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
                ((TextArea) form.lookup("#descricao")).setText("Internet mensal");
                // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
                ((TextField) form.lookup("#valor")).setText("99,90");
                // Preenche o controle com o valor necessário a este cenário.
                ((DatePicker) form.lookup("#data")).setValue(java.time.LocalDate.of(2026, 6, 20));
                // Simula a escolha da opção marcada pelo usuário.
                ((CheckBox) form.lookup("#mensal")).setSelected(true);
                // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
                ((Label) form.lookup("#erro")).setText("");
                // Salva uma imagem da tela para conferir também a apresentação visual.
                capturar(form, "cadastro.png");
                // Simula a ação do usuário no controle sem acessar a API externa.
                ((Button) form.lookup("#salvar")).fire();
                // Exige que a condição de sucesso ou de estado válido seja verdadeira.
                assertTrue(formController.isSalvo());
                // Compara o resultado observado com o valor esperado para este cenário.
                assertEquals(5, service.listar().size());
                // Relê os registros e captura o resultado que será apresentado ou verificado.
                Transacao cadastrada = service.listar().stream()
                        .filter(t -> t.getDescricao().equals("Internet mensal")).findFirst().orElseThrow();
                // Verifica a subclasse reconstruída, preservando o comportamento de receita, despesa ou mensal.
                assertInstanceOf(TransacaoMensal.class, cadastrada);

                // Seleciona o arquivo FXML usado para montar a tela deste cenário.
                FXMLLoader editLoader = loader("transacao-formulario.fxml");
                // Carrega ou recupera a raiz dos controles visuais que serão verificados.
                Parent edit = editLoader.load();
                // Abre a janela de teste com as dimensões da tela correspondente.
                Stage editStage = janela(edit, 640, 650);
                // Obtém o controller criado pelo FXMLLoader para fornecer suas dependências.
                TransacaoController editController = editLoader.getController();
                // Fornece os serviços e a janela utilizados por este cenário.
                editController.configurar(service, cadastrada, editStage);
                // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
                ((TextField) edit.lookup("#valor")).setText("80,00");
                // Simula a escolha da opção marcada pelo usuário.
                ((CheckBox) edit.lookup("#mensal")).setSelected(false);
                // Simula a ação do usuário no controle sem acessar a API externa.
                ((Button) edit.lookup("#salvar")).fire();
                // Exige que a condição de sucesso ou de estado válido seja verdadeira.
                assertTrue(editController.isSalvo());
                // Relê os registros e captura o resultado que será apresentado ou verificado.
                Transacao editada = service.listar().stream().filter(t -> t.getId() == cadastrada.getId()).findFirst().orElseThrow();
                // Verifica a subclasse reconstruída, preservando o comportamento de receita, despesa ou mensal.
                assertInstanceOf(Despesa.class, editada);
                // Compara o resultado observado com o valor esperado para este cenário.
                assertEquals(80, editada.getValor());

                // Seleciona o arquivo FXML usado para montar a tela deste cenário.
                FXMLLoader cancelLoader = loader("transacao-formulario.fxml");
                // Carrega ou recupera a raiz dos controles visuais que serão verificados.
                Parent cancel = cancelLoader.load();
                // Abre a janela de teste com as dimensões da tela correspondente.
                Stage cancelStage = janela(cancel, 640, 650);
                // Obtém o controller criado pelo FXMLLoader para fornecer suas dependências.
                TransacaoController cancelController = cancelLoader.getController();
                // Fornece os serviços e a janela utilizados por este cenário.
                cancelController.configurar(service, editada, cancelStage);
                // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
                ((TextField) cancel.lookup("#valor")).setText("1,00");
                // Simula a ação do usuário no controle sem acessar a API externa.
                ((Button) cancel.lookup("#cancelar")).fire();
                // Exige que a condição proibida, ausente ou já removida seja falsa.
                assertFalse(cancelController.isSalvo());
                // Compara o resultado observado com o valor esperado para este cenário.
                assertEquals(80, service.listar().stream().filter(t -> t.getId() == editada.getId()).findFirst().orElseThrow().getValor());

                // Seleciona o arquivo FXML usado para montar a tela deste cenário.
                FXMLLoader relLoader = loader("relatorio.fxml");
                // Carrega ou recupera a raiz dos controles visuais que serão verificados.
                Parent rel = relLoader.load();
                // Abre a janela de teste com as dimensões da tela correspondente.
                Stage relStage = janela(rel, 980, 720);
                // Obtém o controller criado pelo FXMLLoader para fornecer suas dependências.
                RelatorioController relController = relLoader.getController();
                // Fornece os serviços e a janela utilizados por este cenário.
                relController.configurar(service, relStage);
                // Compara o resultado observado com o valor esperado para este cenário.
                assertEquals(4, ((TableView<?>) rel.lookup("#tabela")).getItems().size());
                // Salva uma imagem da tela para conferir também a apresentação visual.
                capturar(rel, "relatorio.png");
                // Preenche o controle com o valor necessário a este cenário.
                ((DatePicker) rel.lookup("#inicio")).setValue(java.time.LocalDate.of(2026, 6, 10));
                // Preenche o controle com o valor necessário a este cenário.
                ((DatePicker) rel.lookup("#fim")).setValue(java.time.LocalDate.of(2026, 6, 10));
                // Simula a ação do usuário no controle sem acessar a API externa.
                ((Button) rel.lookup("#aplicar")).fire();
                // Compara o resultado observado com o valor esperado para este cenário.
                assertEquals(2, ((TableView<?>) rel.lookup("#tabela")).getItems().size());
                // Preenche o controle com o valor necessário a este cenário.
                ((DatePicker) rel.lookup("#inicio")).setValue(java.time.LocalDate.of(2026, 7, 1));
                // Simula a ação do usuário no controle sem acessar a API externa.
                ((Button) rel.lookup("#aplicar")).fire();
                // Exige que a condição proibida, ausente ou já removida seja falsa.
                assertFalse(((Label) rel.lookup("#erro")).getText().isEmpty());
                // Simula a ação do usuário no controle sem acessar a API externa.
                ((Button) rel.lookup("#limpar")).fire();
                // Compara o resultado observado com o valor esperado para este cenário.
                assertEquals("", ((Label) rel.lookup("#erro")).getText());
                // Simula a ação do usuário no controle sem acessar a API externa.
                ((Button) rel.lookup("#fechar")).fire();
                // Libera o recurso ou fecha a janela depois de concluir seu uso.
                principalStage.close();
            // Executa a limpeza obrigatória tanto no sucesso quanto na falha.
            } finally {
                // Fecha as janelas do cenário antes de iniciar o próximo teste.
                java.util.List.copyOf(javafx.stage.Window.getWindows()).forEach(javafx.stage.Window::hide);
            }
            // Devolve o sinal de resultado que orientará o próximo passo do chamador.
            return null;
        });
        // Agenda a operação no thread JavaFX, onde os controles podem ser atualizados.
        Platform.runLater(tarefa);
        // Espera a etapa de teste com prazo definido, evitando uma execução presa indefinidamente.
        tarefa.get(45, TimeUnit.SECONDS);
    }

    // Cenário de regressão: conversa edu valida chave e responde sem bloquear tela.
    @Test void conversaEduValidaChaveERespondeSemBloquearTela() throws Exception {
        // Permite aguardar a conclusão de um callback sem bloquear o thread da interface.
        java.util.concurrent.CompletableFuture<Void> respondeu = new java.util.concurrent.CompletableFuture<>();
        // Encapsula as operações de interface para executá-las no thread JavaFX e aguardar o resultado.
        FutureTask<Stage> montar = new FutureTask<>(() -> {
            // Seleciona o arquivo FXML usado para montar a tela deste cenário.
            FXMLLoader loader = loader("edu.fxml");
            // Carrega ou recupera a raiz dos controles visuais que serão verificados.
            Parent tela = loader.load();
            // Abre a janela de teste com as dimensões da tela correspondente.
            Stage janela = janela(tela, 1120, 790);
            // Cria um banco isolado em memória; este cenário não usa as transações pessoais.
            TransacaoDAO dao = new TransacaoDAO(Conexao.abrir("jdbc:sqlite::memory:"));
            // Prepara service com os dados ou recursos usados nas próximas operações.
            TransacaoService service = new TransacaoService(dao);
            // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
            service.adicionar(new Receita("Salário", "Pagamento mensal", 5000, "05/06/2026"));
            // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
            service.adicionar(new Despesa("Moradia", "Aluguel", 1500, "10/06/2026"));
            // Obtém o controller criado pelo FXMLLoader para fornecer suas dependências.
            EduController controller = loader.getController();
            // Fornece os serviços e a janela utilizados por este cenário.
            controller.configurar(service, new service.EducacaoFinanceiraService() {
                @Override public String responder(String chave, String pergunta,
                        java.util.List<Mensagem> historico, java.util.List<Transacao> transacoes, boolean usar) {
                    // Compara o resultado observado com o valor esperado para este cenário.
                    assertEquals("chave-teste", chave);
                    // Compara o resultado observado com o valor esperado para este cenário.
                    assertEquals(3, transacoes.size());
                    // Devolve uma resposta fictícia sem consultar a API externa.
                    return "Suas receitas são de R$ 5.000,00 e suas despesas somam R$ 1.550,00. O saldo cadastrado é de R$ 3.450,00. Para organizar o orçamento, acompanhe as categorias e separe os gastos essenciais dos que podem ser ajustados.";
                }
            }, janela);
            // Usa um cofre isolado para evitar leitura ou alteração de uma chave pessoal.
            controller.configurarCofre(new service.ChaveGroqService(Path.of("target/test-credenciais/groq-inexistente.dpapi")));
            // Prepara pagina com os dados ou recursos usados nas próximas operações.
            var pagina = new java.util.concurrent.atomic.AtomicReference<String>();
            // Substitui a abertura real do navegador por uma função observável pelo teste.
            controller.configurarAcessoChave(pagina::set);
            // Simula a ação do usuário no controle sem acessar a API externa.
            ((Button) tela.lookup("#criarChave")).fire();
            // Compara o resultado observado com o valor esperado para este cenário.
            assertEquals("https://console.groq.com/keys", pagina.get());
            // Exige que a condição de sucesso ou de estado válido seja verdadeira.
            assertTrue(((Label) tela.lookup("#saldo")).getText().contains("3.500"));
            // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
            service.adicionar(new Despesa("Transporte", "Passagem", 50, "11/06/2026"));
            // Simula a ação do usuário no controle sem acessar a API externa.
            ((Button) tela.lookup("#atualizar")).fire();
            // Exige que a condição de sucesso ou de estado válido seja verdadeira.
            assertTrue(((Label) tela.lookup("#saldo")).getText().contains("3.450"));
            // Simula a ação do usuário no controle sem acessar a API externa.
            ((Button) tela.lookup("#orcamento")).fire();
            // Simula a ação do usuário no controle sem acessar a API externa.
            ((Button) tela.lookup("#enviar")).fire();
            // Exige que a condição de sucesso ou de estado válido seja verdadeira.
            assertTrue(((Label) tela.lookup("#status")).getText().contains("chave"));
            // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
            ((PasswordField) tela.lookup("#chave")).setText("chave-teste");
            // Observa o estado do botão para detectar o término da consulta assíncrona.
            ((Button) tela.lookup("#enviar")).disableProperty().addListener((o, antes, depois) -> {
                // Sinaliza ao teste que o callback esperado terminou.
                if (!depois) respondeu.complete(null);
            });
            // Simula a ação do usuário no controle sem acessar a API externa.
            ((Button) tela.lookup("#enviar")).fire();
            // Exige que a condição de sucesso ou de estado válido seja verdadeira.
            assertTrue(((Button) tela.lookup("#enviar")).isDisabled());
            // Libera o recurso ou fecha a janela depois de concluir seu uso.
            dao.close();
            // Entrega a janela montada para as verificações seguintes.
            return janela;
        });
        // Agenda a operação no thread JavaFX, onde os controles podem ser atualizados.
        Platform.runLater(montar);
        // Espera a etapa de teste com prazo definido, evitando uma execução presa indefinidamente.
        Stage janela = montar.get(15, TimeUnit.SECONDS);
        // Espera a etapa de teste com prazo definido, evitando uma execução presa indefinidamente.
        respondeu.get(15, TimeUnit.SECONDS);
        // Encapsula as operações de interface para executá-las no thread JavaFX e aguardar o resultado.
        FutureTask<Void> conferir = new FutureTask<>(() -> {
            // Carrega ou recupera a raiz dos controles visuais que serão verificados.
            Parent tela = janela.getScene().getRoot();
            // Compara o resultado observado com o valor esperado para este cenário.
            assertEquals(3, ((javafx.scene.layout.VBox) tela.lookup("#mensagens")).getChildren().size());
            // Salva uma imagem da tela para conferir também a apresentação visual.
            capturar(tela, "edu.png");
            // Simula a ação do usuário no controle sem acessar a API externa.
            ((Button) tela.lookup("#novaConversa")).fire();
            // Compara o resultado observado com o valor esperado para este cenário.
            assertEquals(1, ((javafx.scene.layout.VBox) tela.lookup("#mensagens")).getChildren().size());
            // Libera o recurso ou fecha a janela depois de concluir seu uso.
            janela.close();
            // Compara o resultado observado com o valor esperado para este cenário.
            assertEquals("", ((PasswordField) tela.lookup("#chave")).getText());
            // Devolve o sinal de resultado que orientará o próximo passo do chamador.
            return null;
        });
        // Agenda a operação no thread JavaFX, onde os controles podem ser atualizados.
        Platform.runLater(conferir);
        // Espera a etapa de teste com prazo definido, evitando uma execução presa indefinidamente.
        conferir.get(15, TimeUnit.SECONDS);
    }

    // Cenário de regressão: cancela consulta e restaura pergunta.
    @Test void cancelaConsultaERestauraPergunta() throws Exception {
        // Sincroniza o teste com o início da chamada simulada em segundo plano.
        var iniciou = new java.util.concurrent.CountDownLatch(1);
        // Encapsula as operações de interface para executá-las no thread JavaFX e aguardar o resultado.
        FutureTask<Stage> montar = new FutureTask<>(() -> {
            // Seleciona o arquivo FXML usado para montar a tela deste cenário.
            FXMLLoader loader = loader("edu.fxml");
            // Carrega ou recupera a raiz dos controles visuais que serão verificados.
            Parent tela = loader.load();
            // Abre a janela de teste com as dimensões da tela correspondente.
            Stage janela = janela(tela, 1120, 790);
            // Cria um banco isolado em memória; este cenário não usa as transações pessoais.
            try (TransacaoDAO dao = new TransacaoDAO(Conexao.abrir("jdbc:sqlite::memory:"))) {
                // Obtém o controller criado pelo FXMLLoader para fornecer suas dependências.
                EduController controller = loader.getController();
                // Fornece os serviços e a janela utilizados por este cenário.
                controller.configurar(new TransacaoService(dao), new service.EducacaoFinanceiraService() {
                    @Override public String responder(String chave, String pergunta,
                            java.util.List<Mensagem> historico, java.util.List<Transacao> transacoes, boolean usar)
                            throws InterruptedException {
                        // Sinaliza ao teste que a etapa assíncrona esperada foi alcançada.
                        iniciou.countDown();
                        Thread.sleep(30000);
                        // Devolve uma resposta fictícia sem consultar a API externa.
                        return "Resposta";
                    }
                }, janela);
                // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
                ((PasswordField) tela.lookup("#chave")).setText("teste");
                // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
                ((TextArea) tela.lookup("#pergunta")).setText("Como economizar?");
                // Simula a ação do usuário no controle sem acessar a API externa.
                ((Button) tela.lookup("#enviar")).fire();
            }
            // Entrega a janela montada para as verificações seguintes.
            return janela;
        });
        // Agenda a operação no thread JavaFX, onde os controles podem ser atualizados.
        Platform.runLater(montar);
        // Espera a etapa de teste com prazo definido, evitando uma execução presa indefinidamente.
        Stage janela = montar.get(15, TimeUnit.SECONDS);
        // Exige que a condição de sucesso ou de estado válido seja verdadeira.
        assertTrue(iniciou.await(15, TimeUnit.SECONDS));
        // Encapsula as operações de interface para executá-las no thread JavaFX e aguardar o resultado.
        FutureTask<Void> cancelar = new FutureTask<>(() -> {
            // Simula a ação do usuário no controle sem acessar a API externa.
            ((Button) janela.getScene().lookup("#cancelar")).fire();
            // Devolve o sinal de resultado que orientará o próximo passo do chamador.
            return null;
        });
        // Agenda a operação no thread JavaFX, onde os controles podem ser atualizados.
        Platform.runLater(cancelar);
        // Espera a etapa de teste com prazo definido, evitando uma execução presa indefinidamente.
        cancelar.get(15, TimeUnit.SECONDS);
        // Encapsula as operações de interface para executá-las no thread JavaFX e aguardar o resultado.
        FutureTask<Void> conferir = new FutureTask<>(() -> {
            // Exige que a condição proibida, ausente ou já removida seja falsa.
            assertFalse(((Button) janela.getScene().lookup("#enviar")).isDisabled());
            // Compara o resultado observado com o valor esperado para este cenário.
            assertEquals("Como economizar?", ((TextArea) janela.getScene().lookup("#pergunta")).getText());
            // Exige que a condição de sucesso ou de estado válido seja verdadeira.
            assertTrue(((Label) janela.getScene().lookup("#status")).getText().contains("cancelada"));
            // Libera o recurso ou fecha a janela depois de concluir seu uso.
            janela.close();
            // Devolve o sinal de resultado que orientará o próximo passo do chamador.
            return null;
        });
        // Agenda a operação no thread JavaFX, onde os controles podem ser atualizados.
        Platform.runLater(conferir);
        // Espera a etapa de teste com prazo definido, evitando uma execução presa indefinidamente.
        conferir.get(15, TimeUnit.SECONDS);
    }

    // Limita a execução ao Windows, pois esta verificação utiliza DPAPI.
    @org.junit.jupiter.api.condition.EnabledOnOs(org.junit.jupiter.api.condition.OS.WINDOWS)
    // Cenário de regressão: reabre com chave salva sem revela la e permite esquecer.
    @Test void reabreComChaveSalvaSemRevelaLaEPermiteEsquecer() throws Exception {
        // Prepara pasta com os dados ou recursos usados nas próximas operações.
        Path pasta = Files.createTempDirectory("fintrack-cofre-teste");
        // Prepara arquivo com os dados ou recursos usados nas próximas operações.
        Path arquivo = pasta.resolve("groq.dpapi");
        // Prepara cofre com os dados ou recursos usados nas próximas operações.
        service.ChaveGroqService cofre = new service.ChaveGroqService(arquivo);
        // Grava uma credencial fictícia para conferir sua persistência protegida.
        cofre.salvar("credencial-ficticia");
        // Encapsula as operações de interface para executá-las no thread JavaFX e aguardar o resultado.
        FutureTask<Void> tarefa = new FutureTask<>(() -> {
            // Cria um banco isolado em memória; este cenário não usa as transações pessoais.
            try (TransacaoDAO dao = new TransacaoDAO(Conexao.abrir("jdbc:sqlite::memory:"))) {
                // Seleciona o arquivo FXML usado para montar a tela deste cenário.
                FXMLLoader loader = loader("edu.fxml");
                // Carrega ou recupera a raiz dos controles visuais que serão verificados.
                Parent tela = loader.load();
                // Abre a janela de teste com as dimensões da tela correspondente.
                Stage janela = janela(tela, 1120, 790);
                // Obtém o controller criado pelo FXMLLoader para fornecer suas dependências.
                EduController controller = loader.getController();
                // Fornece os serviços e a janela utilizados por este cenário.
                controller.configurar(new TransacaoService(dao), new service.EducacaoFinanceiraService(), janela);
                // Usa um cofre isolado para evitar leitura ou alteração de uma chave pessoal.
                controller.configurarCofre(cofre);
                // Compara o resultado observado com o valor esperado para este cenário.
                assertEquals("", ((PasswordField) tela.lookup("#chave")).getText());
                // Exige que a condição de sucesso ou de estado válido seja verdadeira.
                assertTrue(((CheckBox) tela.lookup("#lembrarChave")).isSelected());
                // Exige que a condição de sucesso ou de estado válido seja verdadeira.
                assertTrue(((Label) tela.lookup("#situacaoChave")).getText().contains("Chave salva disponível"));
                // Simula a ação do usuário no controle sem acessar a API externa.
                ((Button) tela.lookup("#esquecerChave")).fire();
                // Exige que a condição proibida, ausente ou já removida seja falsa.
                assertFalse(cofre.existe());
                // Exige que a condição proibida, ausente ou já removida seja falsa.
                assertFalse(((CheckBox) tela.lookup("#lembrarChave")).isSelected());
                // Simula a ação do usuário no controle sem acessar a API externa.
                ((Button) tela.lookup("#confirmarChave")).fire();
                // Exige que a condição de sucesso ou de estado válido seja verdadeira.
                assertTrue(((Label) tela.lookup("#situacaoChave")).getText().contains("antes de confirmar"));
                // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
                ((PasswordField) tela.lookup("#chave")).setText("confirmada-pelo-botao");
                // Simula a escolha da opção marcada pelo usuário.
                ((CheckBox) tela.lookup("#lembrarChave")).setSelected(true);
                // Simula a ação do usuário no controle sem acessar a API externa.
                ((Button) tela.lookup("#confirmarChave")).fire();
                // Compara o resultado observado com o valor esperado para este cenário.
                assertEquals("confirmada-pelo-botao", cofre.carregar().orElseThrow());
                // Compara o resultado observado com o valor esperado para este cenário.
                assertEquals("", ((PasswordField) tela.lookup("#chave")).getText());
                // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
                ((PasswordField) tela.lookup("#chave")).setText("confirmada-pelo-enter");
                ((PasswordField) tela.lookup("#chave")).fireEvent(new javafx.event.ActionEvent());
                // Compara o resultado observado com o valor esperado para este cenário.
                assertEquals("confirmada-pelo-enter", cofre.carregar().orElseThrow());
                // Grava uma credencial fictícia para conferir sua persistência protegida.
                cofre.salvar("outra-ficticia");
                // Usa um cofre isolado para evitar leitura ou alteração de uma chave pessoal.
                controller.configurarCofre(cofre);
                // Simula a ação do usuário no controle sem acessar a API externa.
                ((Button) tela.lookup("#trocarChave")).fire();
                // Exige que a condição proibida, ausente ou já removida seja falsa.
                assertFalse(cofre.existe());
                // Compara o resultado observado com o valor esperado para este cenário.
                assertEquals("", ((PasswordField) tela.lookup("#chave")).getText());
                // Libera o recurso ou fecha a janela depois de concluir seu uso.
                janela.close();
            }
            // Devolve o sinal de resultado que orientará o próximo passo do chamador.
            return null;
        });
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // Agenda a operação no thread JavaFX, onde os controles podem ser atualizados.
            Platform.runLater(tarefa);
            // Espera a etapa de teste com prazo definido, evitando uma execução presa indefinidamente.
            tarefa.get(15, TimeUnit.SECONDS);
        // Executa a limpeza obrigatória tanto no sucesso quanto na falha.
        } finally {
            // Remove a credencial fictícia para verificar a limpeza e a repetição segura da operação.
            cofre.esquecer();
            // Remove somente o arquivo temporário criado por este teste.
            Files.deleteIfExists(pasta);
        }
    }

    // Cenário de regressão: resposta markdown mostra formatacao e altura completa.
    @Test void respostaMarkdownMostraFormatacaoEAlturaCompleta() throws Exception {
        // Permite aguardar a conclusão de um callback sem bloquear o thread da interface.
        var carregou = new java.util.concurrent.CompletableFuture<javafx.scene.web.WebView>();
        // Encapsula as operações de interface para executá-las no thread JavaFX e aguardar o resultado.
        FutureTask<Stage> montar = new FutureTask<>(() -> {
            // Seleciona o arquivo FXML usado para montar a tela deste cenário.
            FXMLLoader loader = loader("edu.fxml");
            // Carrega ou recupera a raiz dos controles visuais que serão verificados.
            Parent tela = loader.load();
            // Abre a janela de teste com as dimensões da tela correspondente.
            Stage janela = janela(tela, 1120, 790);
            // Prepara mensagens com os dados ou recursos usados nas próximas operações.
            var mensagens = (javafx.scene.layout.VBox) tela.lookup("#mensagens");
            // Prepara linha com os dados ou recursos usados nas próximas operações.
            var linha = (javafx.scene.layout.HBox) mensagens.getChildren().get(0);
            // Prepara balao com os dados ou recursos usados nas próximas operações.
            var balao = (javafx.scene.layout.VBox) linha.getChildren().get(0);
            // Prepara visualizacao com os dados ou recursos usados nas próximas operações.
            var visualizacao = (javafx.scene.web.WebView) balao.getChildren().get(1);
            visualizacao.getEngine().getLoadWorker().stateProperty().addListener((o, antes, atual) -> {
                if (atual == javafx.concurrent.Worker.State.SUCCEEDED) {
                    // Prepara pausa com os dados ou recursos usados nas próximas operações.
                    var pausa = new javafx.animation.PauseTransition(javafx.util.Duration.millis(400));
                    // Entrega o resultado do callback ao teste que o está aguardando.
                    pausa.setOnFinished(e -> carregou.complete(visualizacao));
                    pausa.play();
                }
            });
            visualizacao.getEngine().loadContent(utils.MarkdownFormatador.formatar("### Organize seu orçamento\n\n**Comece pelos gastos essenciais.**\n\n- Moradia\n- Alimentação\n\n> Dica: acompanhe os valores todo mês.\n\n| Categoria | Meta mensal |\n| --- | --- |\n| Moradia | R$ 1.500,00 |\n| Alimentação | R$ 600,00 |"));
            // Entrega a janela montada para as verificações seguintes.
            return janela;
        });
        // Agenda a operação no thread JavaFX, onde os controles podem ser atualizados.
        Platform.runLater(montar);
        // Espera a etapa de teste com prazo definido, evitando uma execução presa indefinidamente.
        Stage janela = montar.get(15, TimeUnit.SECONDS);
        // Espera a etapa de teste com prazo definido, evitando uma execução presa indefinidamente.
        var visualizacao = carregou.get(15, TimeUnit.SECONDS);
        // Encapsula as operações de interface para executá-las no thread JavaFX e aguardar o resultado.
        FutureTask<Void> conferir = new FutureTask<>(() -> {
            // Prepara documento com os dados ou recursos usados nas próximas operações.
            var documento = visualizacao.getEngine().getDocument();
            // Compara o resultado observado com o valor esperado para este cenário.
            assertEquals(1, documento.getElementsByTagName("h3").getLength());
            // Compara o resultado observado com o valor esperado para este cenário.
            assertEquals(1, documento.getElementsByTagName("table").getLength());
            // Compara o resultado observado com o valor esperado para este cenário.
            assertEquals(2, documento.getElementsByTagName("li").getLength());
            // Exige que a condição de sucesso ou de estado válido seja verdadeira.
            assertTrue(visualizacao.getPrefHeight() > 200);
            // Salva uma imagem da tela para conferir também a apresentação visual.
            capturar(janela.getScene().getRoot(), "edu-markdown.png");
            // Libera o recurso ou fecha a janela depois de concluir seu uso.
            janela.close();
            // Devolve o sinal de resultado que orientará o próximo passo do chamador.
            return null;
        });
        // Agenda a operação no thread JavaFX, onde os controles podem ser atualizados.
        Platform.runLater(conferir);
        // Espera a etapa de teste com prazo definido, evitando uma execução presa indefinidamente.
        conferir.get(15, TimeUnit.SECONDS);
    }

    // Ordena pela coluna indicada para verificar seu comparador.
    private static <T> void ordenar(TableView<T> tabela, int indice) {
        // Prepara coluna com os dados ou recursos usados nas próximas operações.
        TableColumn<T, ?> coluna = tabela.getColumns().get(indice);
        coluna.setSortType(TableColumn.SortType.ASCENDING);
        tabela.getSortOrder().setAll(java.util.List.of(coluna));
        tabela.sort();
    }

    // Seleciona o arquivo FXML usado para montar a tela deste cenário.
    private static FXMLLoader loader(String arquivo) {
        return new FXMLLoader(FinApp.class.getResource("/view/" + arquivo));
    }

    private static Stage janela(Parent tela, int largura, int altura) {
        // Abre a janela de teste com as dimensões da tela correspondente.
        Stage stage = new Stage();
        // Prepara scene com os dados ou recursos usados nas próximas operações.
        Scene scene = new Scene(tela, largura, altura);
        scene.getStylesheets().add(FinApp.class.getResource("/css/fintrack.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
        tela.applyCss();
        tela.layout();
        return stage;
    }

    // Salva uma imagem da tela para conferir também a apresentação visual.
    private static void capturar(Parent tela, String nome) throws Exception {
        tela.applyCss();
        tela.layout();
        // Prepara captura com os dados ou recursos usados nas próximas operações.
        WritableImage captura = tela.snapshot(null, null);
        // Obtém a largura em pixels da imagem capturada.
        int largura = (int) captura.getWidth();
        // Obtém a altura em pixels da imagem capturada.
        int altura = (int) captura.getHeight();
        // Prepara uma imagem com transparência para copiar os pixels da captura JavaFX.
        BufferedImage imagem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
        // Percorre a coleção ou intervalo definido, processando um elemento por repetição.
        for (int y = 0; y < altura; y++) {
            // Percorre a coleção ou intervalo definido, processando um elemento por repetição.
            for (int x = 0; x < largura; x++) imagem.setRGB(x, y, captura.getPixelReader().getArgb(x, y));
        }
        // Prepara pasta com os dados ou recursos usados nas próximas operações.
        Path pasta = Path.of("target/interface");
        // Cria a pasta necessária aos recursos temporários deste cenário.
        Files.createDirectories(pasta);
        // Grava a captura PNG na pasta de resultados, fora dos fontes.
        ImageIO.write(imagem, "png", pasta.resolve(nome).toFile());
    }
}
