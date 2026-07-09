package model;

/**
 * Representa uma despesa financeira do usuário.
 *
 * A classe Despesa herda todas as características da classe Transacao,
 * alterando apenas o tipo da transação para "despesa".
 *
 * Essa herança evita repetição de código, pois todos os atributos e
 * comportamentos comuns já estão implementados na classe Transacao.
 *
 * @author Pedro Giffoni
 */
public class Despesa extends Transacao {

    /**
     * Construtor da classe Despesa.
     *
     * Ao criar uma despesa, o tipo da transação é definido
     * automaticamente como "despesa".
     */
    public Despesa(String categoria, String descricao, double valor, String data) {
        super(categoria, descricao, valor, "despesa", data);
    }
}