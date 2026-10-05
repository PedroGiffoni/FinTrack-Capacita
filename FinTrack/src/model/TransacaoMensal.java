// Agrupa esta classe na camada model do projeto.
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
     * salva no banco de dados.
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

        // Reutiliza as validações do modelo e preserva o identificador carregado do banco.
        super(id, descricao, valor, tipo, categoria, data);
        // O atributo adicional também passa por validação ao construir o objeto.
        setDiaVencimento(diaVencimento);
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

        // Cria os atributos comuns com o construtor da superclasse.
        super(categoria, descricao, valor, tipo, data);
        // O atributo adicional também passa por validação ao construir o objeto.
        setDiaVencimento(diaVencimento);
    }

    // ==========================================================
    // Getters e Setters
    // ==========================================================

    public int getDiaVencimento() {
        // Entrega dia vencimento ao chamador sem modificar o atributo.
        return diaVencimento;
    }

    // Atualiza dia vencimento, aplicando a validação definida para esse atributo.
    public void setDiaVencimento(int diaVencimento) {
        // O dia deve pertencer ao intervalo possível de dias do mês.
        if (diaVencimento < 1 || diaVencimento > 31) {
            // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
            throw new IllegalArgumentException("O dia de vencimento deve estar entre 1 e 31.");
        }
        // Só guarda o dia depois de verificar seu intervalo.
        this.diaVencimento = diaVencimento;
    }

    /**
     * Exibe os dados da transação mensal.
     *
     * Reaproveita a exibição da classe Transacao e acrescenta
     * o dia de vencimento ou recebimento.
     */
    @Override
    // Sobrescreve a apresentação comum para acrescentar o dia de vencimento mensal.
    public void exibirTransacao() {
        // Reutiliza a apresentação comum do console antes de acrescentar o vencimento.
        super.exibirTransacao();
        // Apresenta esta informação no terminal da versão de console.
        System.out.println("Transação mensal - Dia: " + diaVencimento);
    }
}
