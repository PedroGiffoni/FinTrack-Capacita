// Agrupa esta classe na camada model do projeto.
package model;

// Disponibiliza cálculos monetários decimais sem somas em ponto flutuante.
import java.math.BigDecimal;
// Disponibiliza a política de arredondamento ou sua proibição durante validações.
import java.math.RoundingMode;
// Disponibiliza datas sem horário para cadastro, ordenação e filtros.
import java.time.LocalDate;
// Disponibiliza o formato usado para analisar e apresentar datas.
import java.time.format.DateTimeFormatter;
// Disponibiliza o tratamento de datas que não podem ser interpretadas.
import java.time.format.DateTimeParseException;
// Disponibiliza a rejeição de datas impossíveis em vez de sua correção automática.
import java.time.format.ResolverStyle;
// Disponibiliza a configuração regional e a normalização de texto.
import java.util.Locale;

/**
 * Classe base que representa uma transação financeira.
 *
 * Ela reúne os atributos e comportamentos comuns a todas as transações
 * do sistema, como receitas, despesas e transações mensais.
 *
 * A classe também é utilizada como superclasse para demonstrar o uso
 * de herança e polimorfismo no projeto.
 *
 * @author Pedro Giffoni
 */
public class Transacao {

    /**
     * Contador utilizado para gerar IDs automáticos para novas transações.
     */
    private static int contador = 1;

    /**
     * Identificador único da transação.
     */
    private int id;

    /**
     * Categoria da transação.
     */
    private String categoria;

    /**
     * Descrição informada pelo usuário.
     */
    private String descricao;

    /**
     * Valor financeiro da transação.
     */
    private double valor;

    /**
     * Tipo da transação: receita ou despesa.
     */
    private String tipo;

    /**
     * Data em que a transação ocorreu.
     */
    private String data;

    /**
     * Construtor utilizado durante o carregamento das transações
     * salvas no banco ou na base anterior.
     *
     * Mantém o ID original e atualiza o contador para evitar
     * duplicidade de identificadores.
     */
    public Transacao(int id, String descricao, double valor,
                     String tipo, String categoria, String data) {

        // Preserva ou aplica o identificador recebido; o modelo não consulta o banco.
        this.id = id;
        // Aplica o setter correspondente para validar o atributo também durante a construção.
        setDescricao(descricao);
        // Aplica o setter correspondente para validar o atributo também durante a construção.
        setValor(valor);
        // Aplica o setter correspondente para validar o atributo também durante a construção.
        setTipo(tipo);
        // Aplica o setter correspondente para validar o atributo também durante a construção.
        setCategoria(categoria);
        // Aplica o setter correspondente para validar o atributo também durante a construção.
        setData(data);

        // Ao reconstruir um registro, mantém o próximo ID provisório acima dos IDs já vistos.
        if (id >= contador) {
            // Evita reutilizar provisoriamente o ID de um registro carregado.
            contador = id + 1;
        }
    }

    /**
     * Construtor utilizado para criar novas transações.
     *
     * O ID é gerado automaticamente utilizando o contador.
     */
    public Transacao(String categoria, String descricao,
                     double valor, String tipo, String data) {

        // Atribui um ID provisório e incrementa o contador; inserir no DAO substituirá esse valor.
        this.id = contador++;
        // Aplica o setter correspondente para validar o atributo também durante a construção.
        setCategoria(categoria);
        // Aplica o setter correspondente para validar o atributo também durante a construção.
        setDescricao(descricao);
        // Aplica o setter correspondente para validar o atributo também durante a construção.
        setValor(valor);
        // Aplica o setter correspondente para validar o atributo também durante a construção.
        setTipo(tipo);
        // Aplica o setter correspondente para validar o atributo também durante a construção.
        setData(data);
    }
    // Devolve descricao sem alterar o estado do objeto.
    public String getDescricao() {
        // Entrega descricao ao chamador sem modificar o atributo.
        return descricao;
    }

    // Atualiza descricao, aplicando a validação definida para esse atributo.
    public void setDescricao(String descricao) {
        // Guarda no objeto o valor recebido para uso nas próximas operações.
        this.descricao = validarTexto(descricao, "A descrição");
    }

    // Devolve valor sem alterar o estado do objeto.
    public double getValor() {
        // Entrega valor ao chamador sem modificar o atributo.
        return valor;
    }

    // Atualiza valor, aplicando a validação definida para esse atributo.
    public void setValor(double valor) {
        // Rejeita NaN, infinito, zero e valores negativos antes de qualquer conversão monetária.
        if (!Double.isFinite(valor) || valor <= 0) {
            // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
            throw new IllegalArgumentException("O valor deve ser maior que zero.");
        }
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // Exige duas casas sem arredondar: uma terceira casa não nula gera ArithmeticException.
            BigDecimal decimal = BigDecimal.valueOf(valor).setScale(2, RoundingMode.UNNECESSARY);
            // O limite financeiro também evita extrapolar a faixa prevista pelo projeto.
            if (decimal.compareTo(new BigDecimal("99999999.99")) > 0) {
                // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
                throw new ArithmeticException("Valor acima do limite.");
            }
            // Verifica se a conversão exata para centavos cabe em um long.
            decimal.movePointRight(2).longValueExact();
            // Mantém compatibilidade com o modelo antigo; os cálculos atuais usam getValorDecimal.
            this.valor = decimal.doubleValue();
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (ArithmeticException e) {
            // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
            throw new IllegalArgumentException("Informe um valor com até duas casas decimais dentro do limite permitido.", e);
        }
    }

    // Devolve tipo sem alterar o estado do objeto.
    public String getTipo() {
        // Entrega tipo ao chamador sem modificar o atributo.
        return tipo;
    }

    // Atualiza tipo, aplicando a validação definida para esse atributo.
    public void setTipo(String tipo) {
        // Remove espaços e padroniza o tipo sem depender da região configurada na JVM.
        String normalizado = validarTexto(tipo, "O tipo").toLowerCase(Locale.ROOT);
        // Aceita somente os tipos reconhecidos pelo serviço e pelo banco.
        if (!normalizado.equals("receita") && !normalizado.equals("despesa")) {
            // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
            throw new IllegalArgumentException("O tipo deve ser receita ou despesa.");
        }
        // Armazena o tipo já validado e normalizado.
        this.tipo = normalizado;
    }

    // Devolve contador sem alterar o estado do objeto.
    public static int getContador() {
        // Entrega contador ao chamador sem modificar o atributo.
        return contador;
    }

    // Atualiza contador, aplicando a validação definida para esse atributo.
    public static void setContador(int contador) {
        // Atualiza o contador provisório; o ID persistido continua sendo gerado pelo SQLite.
        Transacao.contador = contador;
    }

    // Devolve id sem alterar o estado do objeto.
    public int getId() {
        // Entrega id ao chamador sem modificar o atributo.
        return id;
    }

    // Atualiza id, aplicando a validação definida para esse atributo.
    public void setId(int id) {
        // Preserva ou aplica o identificador recebido; o modelo não consulta o banco.
        this.id = id;
    }

    // Devolve categoria sem alterar o estado do objeto.
    public String getCategoria() {
        // Entrega categoria ao chamador sem modificar o atributo.
        return categoria;
    }

    // Atualiza categoria, aplicando a validação definida para esse atributo.
    public void setCategoria(String categoria) {
        // Guarda no objeto o valor recebido para uso nas próximas operações.
        this.categoria = validarTexto(categoria, "A categoria");
    }

    // Devolve data sem alterar o estado do objeto.
    public String getData() {
        // Entrega data ao chamador sem modificar o atributo.
        return data;
    }

    // Atualiza data, aplicando a validação definida para esse atributo.
    public void setData(String data) {
        // A data precisa existir como texto antes de ser convertida.
        String texto = validarTexto(data, "A data");
        // Usa uuuu com validação estrita, rejeitando datas inexistentes como 31/02.
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // Aceita o formato brasileiro da interface ou o ISO devolvido pelo banco.
            LocalDate dia = texto.contains("/") ? LocalDate.parse(texto, formato) : LocalDate.parse(texto);
            // Uniformiza a apresentação interna para dd/MM/yyyy.
            this.data = dia.format(formato);
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (DateTimeParseException e) {
            // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
            throw new IllegalArgumentException("Informe uma data válida.", e);
        }
    }

    // Centraliza a validação de descrição, categoria, tipo e data contra conteúdo ausente.
    private static String validarTexto(String texto, String campo) {
        // Um texto ausente ou formado apenas por espaços não atende um campo obrigatório.
        if (texto == null || texto.isBlank()) {
            // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
            throw new IllegalArgumentException(campo + " é obrigatório(a).");
        }
        // Guarda o conteúdo sem espaços desnecessários nas extremidades.
        return texto.trim();
    }

    // Devolve data como local date sem alterar o estado do objeto.
    public LocalDate getDataComoLocalDate() {
        // Expõe a data como um objeto próprio para comparar e filtrar períodos.
        return LocalDate.parse(data, DateTimeFormatter.ofPattern("dd/MM/uuuu"));
    }

    // Devolve valor decimal sem alterar o estado do objeto.
    public BigDecimal getValorDecimal() {
        // Converte pela representação decimal e mantém escala 2 para os cálculos monetários.
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.UNNECESSARY);
    }

    // Devolve o sinal da movimentação em decimal para o cálculo atual do saldo.
    public BigDecimal calcularImpactoDecimal() {
        // Receitas aumentam o saldo; despesas devolvem o valor com sinal negativo.
        return tipo.equals("receita") ? getValorDecimal() : getValorDecimal().negate();
    }

    // Mantém a apresentação dos atributos comuns para a versão de console.
    public void exibirTransacao() {
        // Apresenta esta informação no terminal da versão de console.
        System.out.println("ID: " + id);
        // Apresenta esta informação no terminal da versão de console.
        System.out.println("Descrição: " + descricao);
        // Apresenta esta informação no terminal da versão de console.
        System.out.printf("Valor: R$ %.2f%n", valor);
        // Apresenta esta informação no terminal da versão de console.
        System.out.println("Tipo: " + tipo);
        // Apresenta esta informação no terminal da versão de console.
        System.out.println("Categoria: " + categoria);
        // Apresenta esta informação no terminal da versão de console.
        System.out.println("Data: " + data);
    }

    // Mantém a assinatura antiga; o serviço da interface usa a versão BigDecimal.
    public double calcularImpactoNoSaldo() {
        // Na apresentação antiga, a receita também aumenta o saldo.
        if (tipo.equalsIgnoreCase("receita")) {
            // Entrega valor ao chamador sem modificar o atributo.
            return valor;
        // Executa a alternativa quando a condição anterior não foi atendida.
        } else {
            // Versão de compatibilidade com o console: a despesa reduz o saldo.
            return -valor;
        }
    }
}
