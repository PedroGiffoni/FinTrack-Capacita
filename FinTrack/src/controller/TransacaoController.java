// Agrupa esta classe na camada controller do projeto.
package controller;

// Referencia PersistenciaException, componente da camada exceptions utilizado neste fluxo.
import exceptions.PersistenciaException;
// Disponibiliza cálculos monetários decimais sem somas em ponto flutuante.
import java.math.BigDecimal;
// Disponibiliza a política de arredondamento ou sua proibição durante validações.
import java.math.RoundingMode;
// Disponibiliza datas sem horário para cadastro, ordenação e filtros.
import java.time.LocalDate;
// Disponibiliza o formato usado para analisar e apresentar datas.
import java.time.format.DateTimeFormatter;
// Disponibiliza a rejeição de datas impossíveis em vez de sua correção automática.
import java.time.format.ResolverStyle;
// Disponibiliza a associação de campos e métodos ao arquivo de interface.
import javafx.fxml.FXML;
// Disponibiliza os controles visuais da tela.
import javafx.scene.control.*;
// Disponibiliza as janelas da aplicação.
import javafx.stage.Stage;
// Disponibiliza a conversão entre texto e valores de controles JavaFX.
import javafx.util.StringConverter;
// Referencia Despesa, componente da camada model utilizado neste fluxo.
import model.Despesa;
// Referencia Receita, componente da camada model utilizado neste fluxo.
import model.Receita;
// Referencia Transacao, componente da camada model utilizado neste fluxo.
import model.Transacao;
// Referencia TransacaoMensal, componente da camada model utilizado neste fluxo.
import model.TransacaoMensal;
// Referencia TransacaoService, componente da camada service utilizado neste fluxo.
import service.TransacaoService;

// Valida o formulário e cria uma nova transação sem modificar o objeto original durante a edição.
public class TransacaoController {
    // O FXMLLoader injeta este componente pelo fx:id. Título que distingue cadastro novo e edição.
    @FXML private Label titulo;
    // O FXMLLoader injeta este componente pelo fx:id. Campo de descrição com múltiplas linhas.
    @FXML private TextArea descricao;
    // O FXMLLoader injeta este componente pelo fx:id. Entrada do valor monetário sem separador de milhar.
    @FXML private TextField valor;
    // O FXMLLoader injeta este componente pelo fx:id. Seleção do tipo receita ou despesa.
    @FXML private ComboBox<String> tipo;
    // O FXMLLoader injeta este componente pelo fx:id. Seleção da categoria utilizada em filtros e relatórios.
    @FXML private ComboBox<String> categoria;
    // O FXMLLoader injeta este componente pelo fx:id. Calendário e editor de data, com conversão estrita.
    @FXML private DatePicker data;
    // O FXMLLoader injeta este componente pelo fx:id. Opção que distingue transações comuns e mensais.
    @FXML private CheckBox mensal;
    // O FXMLLoader injeta este componente pelo fx:id. Controle inteiro para o vencimento ou recebimento mensal.
    @FXML private Spinner<Integer> dia;
    // O FXMLLoader injeta este componente pelo fx:id. Botão de validação e gravação do formulário.
    @FXML private Button salvar;
    // O FXMLLoader injeta este componente pelo fx:id. Botão para cancelar edição ou consulta, conforme a tela.
    @FXML private Button cancelar;
    // O FXMLLoader injeta este componente pelo fx:id. Rótulo para mensagens de validação ou persistência.
    @FXML private Label erro;
    // Dependência que será fornecida na inicialização; a classe não acessa armazenamento por conta própria.
    private TransacaoService service;
    // Preserva o registro anterior até a gravação; campos do formulário não alteram essa referência.
    private Transacao original;
    // Referência da janela usada para controlar abertura e encerramento.
    private Stage janela;
    // Começa em false e só muda depois da gravação bem-sucedida.
    private boolean salvo;

    // Autoriza o FXMLLoader a chamar ou preencher o membro privado associado à tela.
    @FXML
    // O FXMLLoader chama este método depois de associar os campos anotados com @FXML.
    private void initialize() {
        // Os textos da interface serão convertidos para os tipos internos receita e despesa.
        tipo.getItems().setAll("Receita", "Despesa");
        // Oferece categorias iniciais; a edição também preserva categorias já existentes.
        categoria.getItems().setAll("Alimentação", "Transporte", "Moradia", "Saúde", "Educação",
                "Lazer", "Salário", "Freelance", "Investimentos", "Outros");
        // Define o tipo inicial do formulário de cadastro.
        tipo.getSelectionModel().select("Despesa");
        // Define uma categoria válida antes de qualquer interação.
        categoria.getSelectionModel().select("Outros");
        // Um cadastro novo começa com a data atual do computador.
        data.setValue(LocalDate.now());
        // Aplica o mesmo conversor estrito utilizado no relatório.
        configurarData(data);
        // Limita o Spinner aos dias 1 a 31.
        dia.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 31, 1));
        // O dia fica habilitado somente quando a opção mensal está marcada.
        dia.disableProperty().bind(mensal.selectedProperty().not());
        // Liga o botão à validação e à gravação do formulário.
        salvar.setOnAction(e -> salvar());
        // Fecha sem gravar; o objeto original não foi alterado pelos campos.
        cancelar.setOnAction(e -> janela.close());
    }

    // Usa o mesmo formato e validação de data no cadastro e no relatório.
    public static void configurarData(DatePicker campo) {
        // uuuu com resolução estrita rejeita datas como 31/02 sem corrigir silenciosamente.
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
        // Converte entre o texto digitado e o LocalDate utilizado pelo DatePicker.
        campo.setConverter(new StringConverter<>() {
            // Traduz LocalDate para o texto exibido pelo DatePicker.
            @Override public String toString(LocalDate data) {
                // Campo sem data mostra texto vazio; uma data válida usa o formato brasileiro.
                return data == null ? "" : data.format(formato);
            }
            // Traduz o texto digitado para uma data válida ou informa falha de formato.
            @Override public LocalDate fromString(String texto) {
                // Texto vazio vira null; os demais valores são analisados com validação estrita.
                return texto == null || texto.isBlank() ? null : LocalDate.parse(texto.trim(), formato);
            }
        });
        // Indica o formato esperado sem preencher a data pelo usuário.
        campo.setPromptText("dd/mm/aaaa");
    }

    // Recebe dependências criadas pela aplicação; initialize cuida apenas dos controles do FXML.
    public void configurar(TransacaoService service, Transacao original, Stage janela) {
        // Recebe o serviço compartilhado; não cria outro banco para o formulário.
        this.service = service;
        // Mantém referência para recuperar o ID, mas não modifica seus atributos durante a edição.
        this.original = original;
        // Permite fechar a janela depois de salvar ou cancelar.
        this.janela = janela;
        // Distingue a edição de um registro existente de um cadastro novo.
        if (original != null) {
            // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
            titulo.setText("Editar transação");
            // Apresenta a descrição atual sem alterar o modelo original.
            descricao.setText(original.getDescricao());
            // Converte o decimal para texto com vírgula, sem separador de milhar.
            valor.setText(original.getValorDecimal().toPlainString().replace('.', ','));
            // Traduz o tipo interno para o texto mostrado pelo ComboBox.
            tipo.getSelectionModel().select(original.getTipo().equals("receita") ? "Receita" : "Despesa");
            // Procura uma categoria equivalente, ignorando diferenças de maiúsculas.
            String categoriaAtual = categoria.getItems().stream()
                    // Se a categoria não estiver nas sugestões, preserva o texto armazenado.
                    .filter(c -> c.equalsIgnoreCase(original.getCategoria())).findFirst().orElse(original.getCategoria());
            // Inclui uma categoria antiga ou personalizada para que a edição não a perca.
            if (!categoria.getItems().contains(categoriaAtual)) categoria.getItems().add(categoriaAtual);
            // Mantém a categoria da transação mesmo quando não fazia parte das sugestões iniciais.
            categoria.getSelectionModel().select(categoriaAtual);
            // Carrega a data como LocalDate, adequada ao calendário.
            data.setValue(original.getDataComoLocalDate());
            // Reconhece a recorrência pela subclasse do registro.
            mensal.setSelected(original instanceof TransacaoMensal);
            // Pattern matching também disponibiliza a variável com o tipo mensal.
            if (original instanceof TransacaoMensal recorrente) {
                // Carrega o dia de vencimento persistido.
                dia.getValueFactory().setValue(recorrente.getDiaVencimento());
            }
        }
    }

    // Valida e grava os dados; a apresentação de erro fica sob responsabilidade do chamador.
    private void salvar() {
        // Retira a mensagem da tentativa anterior antes de validar novamente.
        erro.setText("");
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // Confirma o texto digitado no DatePicker, mesmo quando o calendário não foi usado.
            data.commitValue();
            // Impede salvar uma movimentação sem data.
            if (data.getValue() == null) throw new IllegalArgumentException("Informe a data da transação.");
            // Remove espaços nas extremidades do valor digitado.
            String texto = valor.getText().trim();
            // Aceita apenas dígitos e até duas casas decimais, sem separador de milhar.
            if (!texto.matches("[0-9]+([,.][0-9]{1,2})?")) {
                // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
                throw new IllegalArgumentException("Informe o valor sem separador de milhar, por exemplo: 1250,50.");
            }
            // Troca a vírgula por ponto e exige duas casas sem arredondar indevidamente.
            BigDecimal quantia = new BigDecimal(texto.replace(',', '.')).setScale(2, RoundingMode.UNNECESSARY);
            // Converte o texto amigável em um dos tipos aceitos pelo modelo e pelo SQLite.
            String tipoAtual = tipo.getValue().equals("Receita") ? "receita" : "despesa";
            // Um novo objeto será criado; cancelar nunca deixa o objeto original parcialmente editado.
            Transacao transacao;
            // Escolhe a classe que possui vencimento recorrente.
            if (mensal.isSelected()) {
                // Confirma o valor digitado no Spinner antes de construir a transação.
                dia.commitValue();
                // O construtor valida os atributos comuns e o dia de vencimento.
                transacao = new TransacaoMensal(descricao.getText(), quantia.doubleValue(), tipoAtual,
                        categoria.getValue(), data.getValue().toString(), dia.getValue());
            // Executa a alternativa quando a condição anterior não foi atendida.
            } else {
                // Escolhe a subclasse comum correspondente ao tipo financeiro.
                transacao = tipoAtual.equals("receita")
                        // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
                        ? new Receita(categoria.getValue(), descricao.getText(), quantia.doubleValue(), data.getValue().toString())
                        // Constrói um registro do modelo com os dados deste cenário, aplicando as validações do construtor.
                        : new Despesa(categoria.getValue(), descricao.getText(), quantia.doubleValue(), data.getValue().toString());
            }
            // Sem objeto original, a operação é uma inserção.
            if (original == null) {
                // Persiste a transação e recebe o ID definitivo do banco.
                service.adicionar(transacao);
            // Executa a alternativa quando a condição anterior não foi atendida.
            } else {
                // A edição precisa manter o ID para alterar a mesma linha do SQLite.
                transacao.setId(original.getId());
                // Delega a alteração ao serviço, que detecta se a linha deixou de existir.
                service.atualizar(transacao);
            }
            // A tela principal usará este sinal para recarregar a tabela.
            salvo = true;
            // Fecha o formulário somente depois de a persistência terminar.
            janela.close();
        // Apresenta instrução específica quando a data digitada é impossível.
        } catch (java.time.format.DateTimeParseException e) {
            // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
            erro.setText("Informe uma data válida no formato dd/mm/aaaa.");
        // Modelos e DAO devolvem mensagens que o formulário pode apresentar sem encerrar o app.
        } catch (IllegalArgumentException | PersistenciaException e) {
            // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
            erro.setText(e.getMessage());
        }
    }

    // Informa à tela principal se deve atualizar a tabela após o fechamento do formulário.
    public boolean isSalvo() {
        // Permite verificar a conclusão da operação sem acessar campos internos.
        return salvo;
    }
}
