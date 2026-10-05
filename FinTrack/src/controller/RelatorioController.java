// Agrupa esta classe na camada controller do projeto.
package controller;

// Referencia PersistenciaException, componente da camada exceptions utilizado neste fluxo.
import exceptions.PersistenciaException;
// Disponibiliza cálculos monetários decimais sem somas em ponto flutuante.
import java.math.BigDecimal;
// Disponibiliza a apresentação de valores monetários conforme a região.
import java.text.NumberFormat;
// Disponibiliza coleções ordenadas de registros ou mensagens.
import java.util.List;
// Disponibiliza a configuração regional e a normalização de texto.
import java.util.Locale;
// Disponibiliza associação entre categoria e total financeiro.
import java.util.Map;
// Disponibiliza o valor observável utilizado pelas células da tabela.
import javafx.beans.property.ReadOnlyStringWrapper;
// Disponibiliza a associação de campos e métodos ao arquivo de interface.
import javafx.fxml.FXML;
// Disponibiliza o gráfico de distribuição das despesas.
import javafx.scene.chart.PieChart;
// Disponibiliza os controles visuais da tela.
import javafx.scene.control.*;
// Disponibiliza as janelas da aplicação.
import javafx.stage.Stage;
// Referencia Transacao, componente da camada model utilizado neste fluxo.
import model.Transacao;
// Referencia TransacaoService, componente da camada service utilizado neste fluxo.
import service.TransacaoService;

// Consulta o período selecionado e apresenta totais e despesas agrupadas por categoria.
public class RelatorioController {
    // O FXMLLoader injeta este componente pelo fx:id. Limite inicial opcional do período, incluindo a própria data.
    @FXML private DatePicker inicio;
    // O FXMLLoader injeta este componente pelo fx:id. Limite final opcional do período, incluindo a própria data.
    @FXML private DatePicker fim;
    // O FXMLLoader injeta este componente pelo fx:id. Seleção da categoria utilizada em filtros e relatórios.
    @FXML private ComboBox<String> categoria;
    // O FXMLLoader injeta este componente pelo fx:id. Rótulo com as receitas do conjunto consultado.
    @FXML private Label receitas;
    // O FXMLLoader injeta este componente pelo fx:id. Rótulo com as despesas do conjunto consultado.
    @FXML private Label despesas;
    // O FXMLLoader injeta este componente pelo fx:id. Rótulo do saldo calculado para o conjunto apresentado.
    @FXML private Label saldo;
    // O FXMLLoader injeta este componente pelo fx:id. Rótulo para mensagens de validação ou persistência.
    @FXML private Label erro;
    // O FXMLLoader injeta este componente pelo fx:id. Rótulo com a quantidade de registros da seleção.
    @FXML private Label resumo;
    // O FXMLLoader injeta este componente pelo fx:id. Gráfico das despesas agrupadas por categoria.
    @FXML private PieChart grafico;
    // O FXMLLoader injeta este componente pelo fx:id. Mantém acesso ao controle definido no arquivo FXML.
    @FXML private TableView<Map.Entry<String, BigDecimal>> tabela;
    // O FXMLLoader injeta este componente pelo fx:id. Mantém acesso ao controle definido no arquivo FXML.
    @FXML private TableColumn<Map.Entry<String, BigDecimal>, String> colunaCategoria;
    // O FXMLLoader injeta este componente pelo fx:id. Mantém acesso ao controle definido no arquivo FXML.
    @FXML private TableColumn<Map.Entry<String, BigDecimal>, String> colunaTotal;
    // O FXMLLoader injeta este componente pelo fx:id. Mantém acesso ao controle definido no arquivo FXML.
    @FXML private Button aplicar;
    // O FXMLLoader injeta este componente pelo fx:id. Botão que remove os critérios de filtro.
    @FXML private Button limpar;
    // O FXMLLoader injeta este componente pelo fx:id. Botão de retorno à tela principal.
    @FXML private Button fechar;
    // Dependência que será fornecida na inicialização; a classe não acessa armazenamento por conta própria.
    private TransacaoService service;
    // Referência da janela usada para controlar abertura e encerramento.
    private Stage janela;
    // Devolve currency instance sem alterar o estado do objeto.
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    // Autoriza o FXMLLoader a chamar ou preencher o membro privado associado à tela.
    @FXML
    // O FXMLLoader chama este método depois de associar os campos anotados com @FXML.
    private void initialize() {
        // Reutiliza a validação de datas do cadastro para manter o mesmo formato.
        TransacaoController.configurarData(inicio);
        // Reutiliza a validação de datas do cadastro para manter o mesmo formato.
        TransacaoController.configurarData(fim);
        // Lê a chave de cada agrupamento para mostrar o nome da categoria.
        colunaCategoria.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getKey()));
        // Formata o total decimal do grupo para apresentação em reais.
        colunaTotal.setCellValueFactory(c -> new ReadOnlyStringWrapper(moeda.format(c.getValue().getValue())));
        // Explica uma seleção sem despesas, em vez de exibir apenas uma tabela vazia.
        tabela.setPlaceholder(new Label("Nenhuma despesa neste período."));
        // Evita animações durante atualizações e facilita a conferência visual dos resultados.
        grafico.setAnimated(false);
        // Reconsulta e recalcula o relatório a partir dos filtros atuais.
        aplicar.setOnAction(e -> atualizar());
        // Remove todos os limites do filtro antes de recalcular.
        limpar.setOnAction(e -> {
            // Retira o limite inicial; null significa qualquer data anterior.
            inicio.setValue(null);
            // Retira o limite final; null significa qualquer data posterior.
            fim.setValue(null);
            // Apaga também o texto que ainda pudesse estar digitado no editor da data.
            inicio.getEditor().clear();
            // Sincroniza a apresentação do limite final com seu valor nulo.
            fim.getEditor().clear();
            // A primeira opção corresponde à ausência de filtro por categoria.
            categoria.getSelectionModel().selectFirst();
            // Consulta o banco e reaplica a seleção aos totais, à tabela e ao gráfico.
            atualizar();
        });
        // Associa o controle ao evento executado quando o usuário confirma esta ação.
        fechar.setOnAction(e -> janela.close());
    }

    // Recebe dependências criadas pela aplicação; initialize cuida apenas dos controles do FXML.
    public void configurar(TransacaoService service, Stage janela) {
        // Guarda no objeto o valor recebido para uso nas próximas operações.
        this.service = service;
        // Guarda no objeto o valor recebido para uso nas próximas operações.
        this.janela = janela;
        // Inclui uma opção para consultar todas as categorias do banco.
        categoria.getItems().add("Todas as categorias");
        // As opções são derivadas de registros reais, não somente das sugestões do cadastro.
        service.listar().stream().map(Transacao::getCategoria)
                // O TreeSet remove duplicatas e ordena sem distinguir maiúsculas.
                .collect(java.util.stream.Collectors.toCollection(() -> new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER)))
                // Preenche o ComboBox com os grupos encontrados.
                .forEach(categoria.getItems()::add);
        // A primeira opção corresponde à ausência de filtro por categoria.
        categoria.getSelectionModel().selectFirst();
        // Consulta o banco e reaplica a seleção aos totais, à tabela e ao gráfico.
        atualizar();
    }

    // Aplica a alteração ou atualiza a apresentação, conforme a responsabilidade desta classe.
    private void atualizar() {
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // Valida o texto digitado no limite inicial.
            inicio.commitValue();
            // Valida o texto digitado no limite final.
            fim.commitValue();
            // Converte a opção Todas as categorias em null para o serviço.
            String filtro = categoria.getSelectionModel().getSelectedIndex() == 0 ? null : categoria.getValue();
            // Consulta novamente o banco e aplica categoria e limites de data inclusivos.
            List<Transacao> transacoes = service.filtrar(null, null, filtro, inicio.getValue(), fim.getValue());
            // Soma e formata apenas as entradas da seleção.
            receitas.setText(moeda.format(TransacaoService.calcularTotal(transacoes, "receita")));
            // Soma e formata apenas as saídas da seleção.
            despesas.setText(moeda.format(TransacaoService.calcularTotal(transacoes, "despesa")));
            // O saldo considera receitas e despesas do mesmo conjunto filtrado.
            saldo.setText(moeda.format(TransacaoService.calcularSaldo(transacoes)));
            // Agrupa somente as despesas para a tabela e o gráfico.
            Map<String, BigDecimal> totais = TransacaoService.despesasPorCategoria(transacoes);
            // Substitui os resultados anteriores pelos grupos da seleção atual.
            tabela.getItems().setAll(totais.entrySet());
            // Evita acumular fatias de filtros anteriores.
            grafico.getData().clear();
            // Cria uma fatia por categoria; a conversão para double ocorre somente na apresentação gráfica.
            totais.forEach((nome, total) -> grafico.getData().add(new PieChart.Data(nome, total.doubleValue())));
            // Informa quantos registros contribuíram para o período selecionado.
            resumo.setText(transacoes.size() + " transação(ões) no período selecionado");
            // Limpa o erro somente depois de atualizar o relatório com sucesso.
            erro.setText("");
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (java.time.format.DateTimeParseException e) {
            // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
            erro.setText("Informe datas válidas no formato dd/mm/aaaa.");
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (IllegalArgumentException | PersistenciaException e) {
            // Atualiza a apresentação deste controle com a orientação ou resultado da operação.
            erro.setText(e.getMessage());
        }
    }
}
