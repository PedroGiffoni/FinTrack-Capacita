package model;

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
     * salvas no arquivo.
     *
     * Mantém o ID original e atualiza o contador para evitar
     * duplicidade de identificadores.
     */
    public Transacao(int id, String descricao, double valor,
                     String tipo, String categoria, String data) {

        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.tipo = tipo;
        this.categoria = categoria;
        this.data = data;

        if (id >= contador) {
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

        this.id = contador++;
        this.categoria = categoria;
        this.descricao = descricao;
        this.valor = valor;
        this.tipo = tipo;
        this.data = data;
    }
    public String getDescricao() {
    return descricao;
}

public void setDescricao(String descricao) {
    this.descricao = descricao;
}

public double getValor() {
    return valor;
}

public void setValor(double valor) {
    this.valor = valor;
}

public String getTipo() {
    return tipo;
}

public void setTipo(String tipo) {
    this.tipo = tipo;
}

public static int getContador() {
    return contador;
}

public static void setContador(int contador) {
    Transacao.contador = contador;
}

public int getId() {
    return id;
}

public void setId(int id) {
    this.id = id;
}

public String getCategoria() {
    return categoria;
}

public void setCategoria(String categoria) {
    this.categoria = categoria;
}

public String getData() {
    return data;
}

public void setData(String data) {
    this.data = data;
}

public void exibirTransacao() {
    System.out.println("ID: " + id);
    System.out.println("Descrição: " + descricao);
    System.out.printf("Valor: R$ %.2f%n", valor);
    System.out.println("Tipo: " + tipo);
    System.out.println("Categoria: " + categoria);
    System.out.println("Data: " + data);
}

public double calcularImpactoNoSaldo() {
    if (tipo.equalsIgnoreCase("receita")) {
        return valor;
    } else {
        return -valor;
    }
}
}