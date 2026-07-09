package model;

/**
 * Representa uma receita financeira do usuário.
 *
 * A classe Receita herda todas as características da classe Transacao,
 * definindo automaticamente o tipo da transação como "receita".
 *
 * @author Pedro Giffoni
 */
public class Receita extends Transacao {

    /**
     * Construtor da classe Receita.
     */
    public Receita(String categoria, String descricao, double valor, String data) {
        super(categoria, descricao, valor, "receita", data);
    }
}