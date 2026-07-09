package model;

/**
 * Representa uma transação recorrente do usuário.
 *
 * A classe TransacaoMensal herda todas as características da classe
 * Transacao e adiciona o dia de vencimento ou recebimento da transação.
 *
 * @author Pedro Giffoni
 */
public class TransacaoMensal extends Transacao {

    /**
     * Dia do mês em que a transação ocorre.
     */
    private int diaVencimento;

    /**
     * Construtor utilizado ao carregar uma transação mensal
     * salva no arquivo.
     *
     * @param id Identificador da transação.
     * @param descricao Descrição da transação.
     * @param valor Valor da transação.
     * @param tipo Tipo da transação (receita ou despesa).
     * @param categoria Categoria da transação.
     * @param data Data da transação.
     * @param diaVencimento Dia do vencimento ou recebimento.
     */
    public TransacaoMensal(int id, String descricao, double valor,
                           String tipo, String categoria,
                           String data, int diaVencimento) {

        super(id, descricao, valor, tipo, categoria, data);
        this.diaVencimento = diaVencimento;
    }

    /**
     * Construtor utilizado para cadastrar uma nova transação mensal.
     *
     * @param descricao Descrição da transação.
     * @param valor Valor da transação.
     * @param tipo Tipo da transação (receita ou despesa).
     * @param categoria Categoria da transação.
     * @param data Data da transação.
     * @param diaVencimento Dia do vencimento ou recebimento.
     */
    public TransacaoMensal(String descricao, double valor,
                           String tipo, String categoria,
                           String data, int diaVencimento) {

        super(categoria, descricao, valor, tipo, data);
        this.diaVencimento = diaVencimento;
    }

    // ==========================================================
    // Getters e Setters
    // ==========================================================

    public int getDiaVencimento() {
        return diaVencimento;
    }

    public void setDiaVencimento(int diaVencimento) {
        this.diaVencimento = diaVencimento;
    }

    /**
     * Exibe os dados da transação mensal.
     *
     * Reaproveita a exibição da classe Transacao e acrescenta
     * o dia de vencimento ou recebimento.
     */
    @Override
    public void exibirTransacao() {
        super.exibirTransacao();
        System.out.println("Transação mensal - Dia: " + diaVencimento);
    }
}