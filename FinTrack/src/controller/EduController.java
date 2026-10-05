// Agrupa esta classe na camada controller do projeto.
package controller;

// Disponibiliza a apresentação de valores monetários conforme a região.
import java.text.NumberFormat;
// Disponibiliza coleções e utilitários usados nesta classe.
import java.util.*;
// Disponibiliza a execução de atualizações no thread JavaFX.
import javafx.application.Platform;
// Disponibiliza o trabalho em segundo plano com callbacks de interface.
import javafx.concurrent.Task;
// Disponibiliza a associação de campos e métodos ao arquivo de interface.
import javafx.fxml.FXML;
// Disponibiliza o alinhamento dos componentes de conversa.
import javafx.geometry.Pos;
// Disponibiliza os controles visuais da tela.
import javafx.scene.control.*;
// Disponibiliza a identificação de Enter nos atalhos de teclado.
import javafx.scene.input.KeyCode;
// Disponibiliza a organização e distribuição dos componentes.
import javafx.scene.layout.*;
// Disponibiliza as janelas da aplicação.
import javafx.stage.Stage;
// Referencia Transacao, componente da camada model utilizado neste fluxo.
import model.Transacao;
// Referencia EducacaoFinanceiraService, componente da camada service utilizado neste fluxo.
import service.EducacaoFinanceiraService;
// Referencia Mensagem, componente da camada service utilizado neste fluxo.
import service.EducacaoFinanceiraService.Mensagem;
// Referencia TransacaoService, componente da camada service utilizado neste fluxo.
import service.TransacaoService;

// Gerencia a conversa, a credencial, o compartilhamento de dados e a apresentação em Markdown.
public class EduController {
    // O FXMLLoader injeta este componente pelo fx:id. Container que recebe os balões da conversa em ordem.
    @FXML private VBox mensagens;
    // O FXMLLoader injeta este componente pelo fx:id. Rolagem principal de toda a conversa.
    @FXML private ScrollPane rolagem;
    // O FXMLLoader injeta este componente pelo fx:id. Campo visual protegido; credenciais recuperadas não são preenchidas nele.
    @FXML private PasswordField chave;
    // O FXMLLoader injeta este componente pelo fx:id. Opções de consentimento para compartilhar dados e persistir a credencial.
    @FXML private CheckBox usarTransacoes, lembrarChave;
    // O FXMLLoader injeta este componente pelo fx:id. Indicação de confirmação, persistência ou falha da credencial, sem revelar seu conteúdo.
    @FXML private Label situacaoChave;
    // O FXMLLoader injeta este componente pelo fx:id. Ações locais para substituir, remover e confirmar a chave.
    @FXML private Button trocarChave, esquecerChave, confirmarChave;
    // Dependência que será fornecida na inicialização; a classe não acessa armazenamento por conta própria.
    private service.ChaveGroqService cofre;
    // Credencial disponível na sessão; pode ter sido confirmada agora ou recuperada do cofre.
    private String chaveConfirmada = "";
    // O FXMLLoader injeta este componente pelo fx:id. Editor da pergunta atual, com suporte a múltiplas linhas.
    @FXML private TextArea pergunta;
    // O FXMLLoader injeta este componente pelo fx:id. Rótulos do panorama e das orientações da conversa.
    @FXML private Label receitas, despesas, saldo, quantidade, status;
    // O FXMLLoader injeta este componente pelo fx:id. Ações da conversa, sugestões de assuntos e acesso à página da chave.
    @FXML private Button enviar, cancelar, novaConversa, fechar, atualizar, orcamento, reserva, gastos, criarChave;
    // O FXMLLoader injeta este componente pelo fx:id. Indicação visual de uma chamada em andamento.
    @FXML private ProgressIndicator progresso;
    // Dependência que será fornecida na inicialização; a classe não acessa armazenamento por conta própria.
    private TransacaoService transacoes;
    // Dependência que será fornecida na inicialização; a classe não acessa armazenamento por conta própria.
    private EducacaoFinanceiraService educacao;
    // Referência da janela usada para controlar abertura e encerramento.
    private Stage janela;
    // Função fornecida pela aplicação para abrir a página da chave no navegador.
    private java.util.function.Consumer<String> abrirPagina;
    // Referência da consulta ativa, utilizada também para cancelamento.
    private Task<String> consulta;
    // Histórico somente em memória, descartado ao iniciar outra conversa ou fechar a janela.
    private final List<Mensagem> historico = new ArrayList<>();
    // Devolve currency instance sem alterar o estado do objeto.
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    // O FXMLLoader injeta este componente pelo fx:id. Mantém acesso ao controle definido no arquivo FXML.
    @FXML private void initialize() {
        // O botão aceita a chave sem precisar enviar uma pergunta.
        confirmarChave.setOnAction(e -> confirmarCredencial());
        // Enter no PasswordField executa a mesma confirmação do botão.
        chave.setOnAction(e -> confirmarCredencial());
        // A opção só é habilitada depois de verificar o suporte do cofre nesta conta.
        lembrarChave.setDisable(true);
        // Não oferece exclusão quando ainda não existe uma chave persistida.
        esquecerChave.setDisable(true);
        // Marcar autoriza a gravação na confirmação; desmarcar remove a cópia existente.
        lembrarChave.setOnAction(e -> {
            // Desmarcar deve retirar a chave persistida, e não apenas esconder a indicação na tela.
            if (!lembrarChave.isSelected()) esquecerChaveSalva();
            // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
            else situacaoChave.setText("Clique em Confirmar chave para salvar nesta conta do Windows.");
        });
        // Associa o controle ao evento executado quando o usuário confirma esta ação.
        esquecerChave.setOnAction(e -> esquecerChaveSalva());
        // A troca remove a chave antiga antes de solicitar outra.
        trocarChave.setOnAction(e -> {
            // Só inicia a troca depois de retirar a cópia anterior com sucesso.
            if (esquecerChaveSalva()) {
                // Limpa o campo visual sem revelar o conteúdo guardado em memória.
                chave.clear();
                // Direciona o teclado ao campo que o usuário deve preencher em seguida.
                chave.requestFocus();
                // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
                situacaoChave.setText("Informe a nova chave. Marque Lembrar e clique em Confirmar chave para salvar.");
            }
        });
        // O acesso ao navegador só é habilitado quando a aplicação fornece a função de abertura.
        criarChave.setDisable(true);
        // Associa o controle ao evento executado quando o usuário confirma esta ação.
        criarChave.setOnAction(e -> {
            // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
            try {
                // HostServices abre a página externa; a aplicação não cria a conta em nome do usuário.
                abrirPagina.accept("https://console.groq.com/keys");
                // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
                status.setText("Crie a chave na página aberta e cole no campo Chave da Groq.");
            // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
            } catch (RuntimeException falha) {
                // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
                status.setText("Abra https://console.groq.com/keys no navegador para criar sua chave.");
            }
        });
        // Associa o controle ao evento executado quando o usuário confirma esta ação.
        enviar.setOnAction(e -> enviarPergunta());
        // Associa o controle ao evento executado quando o usuário confirma esta ação.
        cancelar.setOnAction(e -> cancelarConsulta());
        // Associa o controle ao evento executado quando o usuário confirma esta ação.
        novaConversa.setOnAction(e -> limparConversa());
        // Associa o controle ao evento executado quando o usuário confirma esta ação.
        fechar.setOnAction(e -> janela.close());
        // Associa o controle ao evento executado quando o usuário confirma esta ação.
        atualizar.setOnAction(e -> atualizarPanorama());
        // Associa o controle ao evento executado quando o usuário confirma esta ação.
        orcamento.setOnAction(e -> sugerir("Como posso organizar meu orçamento?"));
        // Associa o controle ao evento executado quando o usuário confirma esta ação.
        reserva.setOnAction(e -> sugerir("Como funciona uma reserva de emergência?"));
        // Associa o controle ao evento executado quando o usuário confirma esta ação.
        gastos.setOnAction(e -> sugerir("O que minhas despesas cadastradas mostram sobre meus gastos?"));
        // O atalho da conversa é diferente do Enter usado para confirmar a chave.
        pergunta.setOnKeyPressed(e -> {
            // Ctrl + Enter envia; Enter simples continua criando uma nova linha na pergunta.
            if (e.isControlDown() && e.getCode() == KeyCode.ENTER) {
                // Impede que o mesmo atalho também insira uma quebra de linha no TextArea.
                e.consume();
                // Evita iniciar outra consulta enquanto a primeira está em andamento.
                if (!enviar.isDisabled()) enviarPergunta();
            }
        });
        // Quando um balão cresce, a conversa pode acompanhar a última mensagem.
        mensagens.heightProperty().addListener((o, anterior, atual) ->
                // Ajusta a rolagem depois que o JavaFX recalcular o layout.
                Platform.runLater(() -> rolagem.setVvalue(1)));
        // Alterar o consentimento limpa o histórico para a conversa nova não reutilizar dados anteriores.
        usarTransacoes.selectedProperty().addListener((o, anterior, atual) -> {
            // Descarta mensagens antigas e volta à apresentação inicial do Edu.
            limparConversa();
            // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
            status.setText(atual ? "Transações serão usadas nas próximas perguntas."
                    : "Nova conversa iniciada sem compartilhar transações.");
        });
        // Descarta mensagens antigas e volta à apresentação inicial do Edu.
        limparConversa();
    }

    // Recebe dependências criadas pela aplicação; initialize cuida apenas dos controles do FXML.
    public void configurar(TransacaoService transacoes, EducacaoFinanceiraService educacao, Stage janela) {
        // Guarda no objeto o valor recebido para uso nas próximas operações.
        this.transacoes = transacoes;
        // Guarda no objeto o valor recebido para uso nas próximas operações.
        this.educacao = educacao;
        // Guarda no objeto o valor recebido para uso nas próximas operações.
        this.janela = janela;
        // Fechar a janela deve interromper a tarefa e limpar a credencial em memória.
        janela.setOnHidden(e -> encerrar());
        // A apresentação lê o SQLite no momento da abertura da tela.
        atualizarPanorama();
    }

    // Recupera a chave desta conta do Windows sem colocá-la no campo de texto.
    public void configurarCofre(service.ChaveGroqService cofre) {
        // Recebe o armazenamento protegido; os testes fornecem um arquivo temporário.
        this.cofre = cofre;
        // O usuário só pode escolher lembrar quando há DPAPI disponível.
        lembrarChave.setDisable(!cofre.disponivel());
        // Em outro sistema, a chave continua sendo usada somente durante a sessão.
        if (!cofre.disponivel()) {
            // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
            situacaoChave.setText("Neste sistema, a chave fica apenas nesta sessão.");
            // Encerra esta operação sem executar as etapas restantes.
            return;
        }
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // Optional.empty vira texto vazio; uma chave recuperada fica apenas no estado do controller.
            chaveConfirmada = cofre.carregar().orElse("");
            // Uma credencial recuperada já havia sido salva com consentimento.
            lembrarChave.setSelected(!chaveConfirmada.isEmpty());
            // A presença do arquivo define se a operação de exclusão faz sentido.
            esquecerChave.setDisable(!cofre.existe());
            // Não confunde ausência de credencial com uma chave salva disponível.
            if (!chaveConfirmada.isEmpty()) {
                // Limpa o campo visual sem revelar o conteúdo guardado em memória.
                chave.clear();
                // Mostra apenas uma indicação; o placeholder não contém a chave.
                chave.setPromptText("Chave salva disponível");
                // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
                situacaoChave.setText("Chave salva disponível para esta conta do Windows.");
            }
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (java.io.IOException e) {
            // A presença do arquivo define se a operação de exclusão faz sentido.
            esquecerChave.setDisable(!cofre.existe());
            // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
            situacaoChave.setText("Não foi possível recuperar a chave. Use Esquecer e informe outra.");
        }
    }

    // Remove a cópia persistida e limpa a credencial disponível na sessão.
    private boolean esquecerChaveSalva() {
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // Apaga a cópia criptografada somente no armazenamento disponível.
            if (cofre != null && cofre.disponivel()) cofre.esquecer();
            // Remove a referência utilizada para as próximas chamadas, sem prometer apagar Strings da memória da JVM.
            chaveConfirmada = "";
            // Uma chave esquecida não continua marcada para reutilização.
            lembrarChave.setSelected(false);
            // Não oferece exclusão quando ainda não existe uma chave persistida.
            esquecerChave.setDisable(true);
            // Limpa o campo visual sem revelar o conteúdo guardado em memória.
            chave.clear();
            // O campo volta ao estado de entrada, sem conter a credencial anterior.
            chave.setPromptText("Informe sua chave");
            // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
            situacaoChave.setText("Nenhuma chave salva. Informe uma chave para esta sessão.");
            // Informa sucesso ao evento que depende da conclusão desta operação.
            return true;
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (java.io.IOException e) {
            // Se apagar falhar, mantém a indicação de que ainda existe uma cópia salva.
            lembrarChave.setSelected(true);
            // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
            situacaoChave.setText("Não foi possível apagar a chave salva. Tente novamente.");
            // Interrompe o fluxo dependente quando a operação não pôde ser concluída.
            return false;
        }
    }

    // Habilita o botão de criação da chave com a função de abertura do navegador.
    public void configurarAcessoChave(java.util.function.Consumer<String> abrirPagina) {
        // Exige uma função válida de abertura para evitar um botão sem ação.
        this.abrirPagina = java.util.Objects.requireNonNull(abrirPagina);
        // Habilita a criação de chave depois de receber a função de abertura de página.
        criarChave.setDisable(false);
    }

    // Preenche a pergunta sem iniciar uma chamada à API.
    private void sugerir(String texto) {
        // Devolve a pergunta para permitir correção ou nova tentativa.
        pergunta.setText(texto);
        // Direciona o teclado ao campo que o usuário deve preencher em seguida.
        pergunta.requestFocus();
    }

    // Busca as transações no SQLite; os totais não dependem do histórico da conversa.
    private List<Transacao> atualizarPanorama() {
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // Consulta o mesmo serviço e banco utilizados pela tela principal.
            List<Transacao> lista = transacoes.listar();
            // Exibe o total de entradas em reais.
            receitas.setText(moeda.format(TransacaoService.calcularTotal(lista, "receita")));
            // Exibe o total de saídas em reais.
            despesas.setText(moeda.format(TransacaoService.calcularTotal(lista, "despesa")));
            // O saldo é calculado com BigDecimal antes da formatação.
            var total = TransacaoService.calcularSaldo(lista);
            // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
            saldo.setText(moeda.format(total));
            // Remove a cor anterior antes de aplicar a situação atual do saldo.
            saldo.getStyleClass().removeAll("positivo", "negativo");
            // Valores negativos usam a cor de despesa; os demais usam a cor positiva.
            saldo.getStyleClass().add(total.signum() < 0 ? "negativo" : "positivo");
            // Indica quantos registros contribuíram para os totais do panorama.
            quantidade.setText(lista.size() + " transação(ões) no FinTrack");
            // Disponibiliza exatamente a lista usada para calcular o panorama.
            return lista;
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (RuntimeException e) {
            // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
            status.setText("Não foi possível consultar as transações. Tente atualizar o panorama.");
            // Sinaliza falha de consulta; o envio não deve continuar com dados incompletos.
            return null;
        }
    }

    // Aceita a chave para a sessão e, com consentimento, guarda a cópia criptografada.
    private boolean confirmarCredencial() {
        // Remove espaços das extremidades antes de aceitar a credencial.
        String informada = chave.getText().trim();
        // Campo vazio pode reutilizar uma chave confirmada ou recuperada; não exige expô-la novamente.
        String credencial = informada.isEmpty() ? chaveConfirmada : informada;
        // Sem chave digitada nem recuperada, a confirmação precisa orientar o usuário.
        if (credencial.isBlank()) {
            // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
            situacaoChave.setText("Informe sua chave da Groq antes de confirmar.");
            // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
            status.setText("Informe sua chave da Groq no painel ao lado.");
            // Direciona o teclado ao campo que o usuário deve preencher em seguida.
            chave.requestFocus();
            // Interrompe o fluxo dependente quando a operação não pôde ser concluída.
            return false;
        }
        // Rejeita espaços internos e quebras de linha, inadequados no cabeçalho HTTP.
        if (credencial.chars().anyMatch(Character::isWhitespace)) {
            // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
            situacaoChave.setText("A chave não pode conter espaços ou quebras de linha.");
            // Direciona o teclado ao campo que o usuário deve preencher em seguida.
            chave.requestFocus();
            // Interrompe o fluxo dependente quando a operação não pôde ser concluída.
            return false;
        }
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // Persistir exige consentimento e um cofre compatível; fora disso a chave fica na sessão.
            boolean salvar = lembrarChave.isSelected() && cofre != null && cofre.disponivel();
            // A criptografia termina antes de indicar sucesso na interface.
            if (salvar) cofre.salvar(credencial);
            // Disponibiliza a credencial para o envio, sem colocá-la no banco ou no histórico.
            chaveConfirmada = credencial;
            // Limpa o campo visual sem revelar o conteúdo guardado em memória.
            chave.clear();
            // A indicação mostra somente se houve persistência, nunca o valor da credencial.
            chave.setPromptText(salvar ? "Chave salva disponível" : "Chave confirmada para esta sessão");
            // Sem armazenamento ou sem arquivo, não há uma cópia para excluir.
            esquecerChave.setDisable(cofre == null || !cofre.existe());
            // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
            situacaoChave.setText(salvar ? "Chave confirmada e salva com proteção do Windows."
                    : "Chave confirmada para esta sessão.");
            // Confirmar no aplicativo não é validar na Groq; a validação remota ocorrerá na pergunta.
            status.setText("Chave pronta para uso. A Groq verificará a validade ao enviar uma pergunta.");
            // Direciona o teclado ao campo que o usuário deve preencher em seguida.
            pergunta.requestFocus();
            // Informa sucesso ao evento que depende da conclusão desta operação.
            return true;
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (java.io.IOException e) {
            // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
            situacaoChave.setText("Não foi possível salvar. Desmarque Lembrar para usar apenas nesta sessão.");
            // Interrompe o fluxo dependente quando a operação não pôde ser concluída.
            return false;
        }
    }

    // Captura os dados da tela antes de iniciar a tarefa de rede em segundo plano.
    private void enviarPergunta() {
        // Captura a pergunta no thread JavaFX antes de iniciar trabalho em segundo plano.
        String texto = pergunta.getText().trim();
        // Uma pergunta vazia deve ser corrigida sem chamada de rede.
        if (texto.isEmpty()) { status.setText("Digite uma pergunta para o Edu."); return; }
        // Limita o texto da pergunta antes de construir o contexto.
        if (texto.length() > 4000) { status.setText("Use até 4.000 caracteres na pergunta."); return; }
        // Mantém o envio compatível com quem digitou a chave e clicou diretamente em Enviar.
        if ((!chave.getText().isBlank() || chaveConfirmada.isBlank()) && !confirmarCredencial()) return;
        // O contexto só recebe registros quando o compartilhamento está marcado.
        List<Transacao> lista = usarTransacoes.isSelected() ? atualizarPanorama() : List.of();
        // Falha no SQLite impede enviar um contexto financeiro incompleto.
        if (lista == null) return;
        // Captura a credencial da consulta antes que os campos visuais sejam bloqueados.
        String credencial = chaveConfirmada;
        // Captura o consentimento utilizado por esta pergunta.
        boolean compartilhar = usarTransacoes.isSelected();
        // A tarefa recebe uma fotografia do histórico, sem ler controles ou lista mutável no thread de rede.
        List<Mensagem> anteriores = List.copyOf(historico);
        // Exibe a pergunta imediatamente, antes de aguardar a resposta.
        adicionarMensagem("Você", texto, true);
        // O texto já foi capturado; em erro ou cancelamento será restaurado.
        pergunta.clear();
        // Bloqueia alterações no contexto durante a consulta e apresenta o progresso.
        ocupado(true);
        // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
        status.setText("O Edu está preparando sua resposta...");
        // Task separa a operação de rede dos eventos executados no thread JavaFX.
        consulta = new Task<>() {
            // A Task executa este método fora do thread JavaFX.
            @Override protected String call() throws Exception {
                // Executa a chamada com os dados capturados; o serviço não acessa controles da tela.
                return educacao.responder(credencial, texto, anteriores, lista, compartilhar);
            }
        };
        // O callback de sucesso volta ao thread JavaFX e pode atualizar os componentes.
        consulta.setOnSucceeded(e -> {
            // Só inclui a pergunta no histórico depois de uma resposta bem-sucedida.
            historico.add(new Mensagem("user", texto));
            // Guarda a resposta em memória para contextualizar a próxima pergunta.
            historico.add(new Mensagem("assistant", consulta.getValue()));
            // Apresenta a resposta com a formatação Markdown da aplicação.
            adicionarMensagem("Edu", consulta.getValue(), false);
            // Restaura os controles após sucesso, falha ou cancelamento.
            ocupado(false);
            // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
            status.setText("Ctrl + Enter para enviar");
            // Direciona o teclado ao campo que o usuário deve preencher em seguida.
            pergunta.requestFocus();
        });
        // Tratamento visual de erros sem bloquear ou encerrar o aplicativo.
        consulta.setOnFailed(e -> {
            // Restaura os controles após sucesso, falha ou cancelamento.
            ocupado(false);
            // Devolve a pergunta para permitir correção ou nova tentativa.
            pergunta.setText(texto);
            // Retira a pergunta sem resposta para evitar duplicá-la em uma nova tentativa.
            mensagens.getChildren().remove(mensagens.getChildren().size() - 1);
            // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
            status.setText(consulta.getException().getMessage());
        });
        // O usuário não precisa esperar a operação terminar para recuperar a tela.
        consulta.setOnCancelled(e -> {
            // Restaura os controles após sucesso, falha ou cancelamento.
            ocupado(false);
            // Devolve a pergunta para permitir correção ou nova tentativa.
            pergunta.setText(texto);
            // Preserva o balão inicial e remove somente a pergunta interrompida.
            if (mensagens.getChildren().size() > 1)
                // Retira a pergunta sem resposta para evitar duplicá-la em uma nova tentativa.
                mensagens.getChildren().remove(mensagens.getChildren().size() - 1);
            // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
            status.setText("Consulta cancelada. Você pode enviar novamente.");
        });
        // A chamada HTTP pode demorar; uma thread separada mantém a janela responsiva.
        Thread tarefa = new Thread(consulta, "edu-consulta");
        // A thread não impede o encerramento da JVM se a aplicação for fechada.
        tarefa.setDaemon(true);
        // Inicia a execução de call; os callbacks da Task atualizam a tela depois.
        tarefa.start();
    }

    // Cria um balão; perguntas usam texto simples e respostas usam Markdown.
    private void adicionarMensagem(String autor, String texto, boolean usuario) {
        // Cria o rótulo do autor do balão, separado do conteúdo.
        Label nome = new Label(autor);
        // Usa a cor e o peso de texto definidos no CSS comum.
        nome.getStyleClass().add("edu-autor");
        // O mesmo balão recebe TextArea para perguntas ou WebView para respostas.
        javafx.scene.Node conteudo;
        // Perguntas são texto simples; somente a resposta do Edu recebe interpretação Markdown.
        if (usuario) {
            // Mantém a pergunta selecionável, sem transformar seu texto em HTML.
            TextArea campo = new TextArea(texto);
            // Permite ler e copiar, mas não modificar uma mensagem já enviada.
            campo.setEditable(false);
            // Quebra linhas na largura disponível em vez de criar rolagem horizontal.
            campo.setWrapText(true);
            // Aplica a classe visual correspondente, definida na folha CSS.
            campo.getStyleClass().add("edu-texto");
            // Estima a altura da pergunta e limita o número de linhas visuais.
            campo.setPrefRowCount(Math.min(16, Math.max(2, texto.length() / 75 + texto.split("\\n").length)));
            // O nó de conteúdo do balão passa a ser o TextArea configurado.
            conteudo = campo;
        // Executa a alternativa quando a condição anterior não foi atendida.
        } else {
            // Usa o renderizador local do JavaFX para títulos, listas, tabelas e código.
            javafx.scene.web.WebView visualizacao = new javafx.scene.web.WebView();
            // Evita ações de navegação e de inspeção no menu do componente HTML.
            visualizacao.setContextMenuEnabled(false);
            // Reserva espaço inicial antes de o HTML terminar de carregar.
            visualizacao.setMinHeight(40);
            // Reserva uma altura inicial enquanto o documento HTML ainda está carregando.
            visualizacao.setPrefHeight(80);
            // Permite o crescimento necessário para ler a resposta na rolagem principal.
            visualizacao.setMaxHeight(Double.MAX_VALUE);
            // A medição é repetida depois do carregamento e quando a largura muda.
            Runnable ajustarAltura = () -> {
                // Mede somente um documento cujo carregamento local já terminou.
                if (visualizacao.getEngine().getLoadWorker().getState() == javafx.concurrent.Worker.State.SUCCEEDED) {
                    // Executa somente esta expressão fixa para medir o conteúdo; o texto do Edu não vira script.
                    Number altura = (Number) visualizacao.getEngine().executeScript("document.getElementById('conteudo').offsetHeight");
                    // Acrescenta uma pequena folga para não cortar a última linha.
                    visualizacao.setPrefHeight(altura.doubleValue() + 4);
                }
            };
            // Espera o HTML carregar antes de calcular a altura.
            visualizacao.getEngine().getLoadWorker().stateProperty().addListener((o, antes, atual) -> {
                // Agenda a medição depois de um ciclo de atualização do layout.
                if (atual == javafx.concurrent.Worker.State.SUCCEEDED) Platform.runLater(ajustarAltura);
            });
            // Agenda a medição depois de um ciclo de atualização do layout.
            visualizacao.widthProperty().addListener((o, antes, atual) -> Platform.runLater(ajustarAltura));
            // Carrega HTML produzido localmente; o formatador escapa HTML recebido e bloqueia recursos externos.
            visualizacao.getEngine().loadContent(utils.MarkdownFormatador.formatar(texto));
            // O balão de resposta utiliza o WebView, sem alterar o modelo da conversa.
            conteudo = visualizacao;
        }
        // Agrupa autor e conteúdo com espaçamento vertical.
        VBox balao = new VBox(8, nome, conteudo);
        // Distingue visualmente a pergunta do usuário e a resposta do Edu.
        balao.getStyleClass().add(usuario ? "edu-pergunta" : "edu-resposta");
        // Permite que o balão acompanhe a largura disponível na conversa.
        balao.setMaxWidth(Double.MAX_VALUE);
        // Cria uma linha de layout para o balão.
        HBox linha = new HBox(balao);
        // Aplica alinhamento conforme o autor da mensagem.
        linha.setAlignment(usuario ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        // Distribui a largura horizontal ao balão.
        HBox.setHgrow(balao, Priority.ALWAYS);
        // Acrescenta a nova mensagem ao final da conversa.
        mensagens.getChildren().add(linha);
    }

    // Descarta o histórico em memória e apresenta novamente a mensagem inicial.
    private void limparConversa() {
        // O histórico existe só nesta sessão e é descartado ao limpar ou fechar.
        historico.clear();
        // Remove os balões antigos da apresentação.
        mensagens.getChildren().clear();
        // Apresenta a resposta com a formatação Markdown da aplicação.
        adicionarMensagem("Edu", "Vamos conversar sobre suas finanças. Posso ajudar você a entender seus gastos, organizar o orçamento e aprender a cuidar do dinheiro. Escolha um assunto abaixo ou escreva sua pergunta.", false);
        // O texto já foi capturado; em erro ou cancelamento será restaurado.
        pergunta.clear();
        // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
        status.setText("Ctrl + Enter para enviar");
    }

    // Bloqueia os controles que poderiam alterar o contexto durante uma consulta.
    private void ocupado(boolean valor) {
        // Impede envio duplicado enquanto uma consulta está ativa.
        enviar.setDisable(valor);
        // Não permite apagar o histórico no meio da consulta que o utiliza.
        novaConversa.setDisable(valor);
        // Mantém o consentimento fixo enquanto esta pergunta é processada.
        usarTransacoes.setDisable(valor);
        // A credencial desta consulta não pode ser trocada durante a espera.
        chave.setDisable(valor);
        // A confirmação aguarda o término da consulta em andamento.
        confirmarChave.setDisable(valor);
        // Combina o estado da consulta com a disponibilidade do cofre.
        lembrarChave.setDisable(valor || cofre == null || !cofre.disponivel());
        // Evita remover ou substituir a credencial enquanto ela está em uso.
        trocarChave.setDisable(valor);
        // Só permite excluir quando há arquivo salvo e nenhuma consulta ativa.
        esquecerChave.setDisable(valor || cofre == null || !cofre.existe());
        // Evita alterações no texto já capturado para a consulta.
        pergunta.setDisable(valor);
        // Impede substituir a pergunta por uma sugestão durante a consulta.
        orcamento.setDisable(valor);
        // Mantém a pergunta capturada até o término ou cancelamento da resposta.
        reserva.setDisable(valor);
        // Bloqueia a sugestão de gastos enquanto seu contexto está em uso.
        gastos.setDisable(valor);
        // Mostra a indicação de atividade apenas durante a espera.
        progresso.setVisible(valor);
        // Um controle não gerenciado não reserva espaço no layout quando está oculto.
        progresso.setManaged(valor);
        // Oferece cancelamento somente durante uma consulta.
        cancelar.setVisible(valor);
        // Libera o espaço do botão quando não há consulta a cancelar.
        cancelar.setManaged(valor);
    }

    // Solicita interrupção da tarefa; o tratamento de cancelamento restaura a tela.
    private void cancelarConsulta() { if (consulta != null && consulta.isRunning()) consulta.cancel(true); }
    // Remove a referência utilizada para as próximas chamadas, sem prometer apagar Strings da memória da JVM.
    public void encerrar() { cancelarConsulta(); chave.clear(); chaveConfirmada = ""; historico.clear(); }
}
