package exceptions;

/**
 * Exceção personalizada utilizada pelo sistema FinTrack.
 *
 * Esta classe representa situações em que o usuário informa
 * um dado inválido durante a utilização da aplicação.
 *
  * Esta classe herda da classe Exception, tornando-se uma exceção
 * verificada (Checked Exception), que pode ser lançada com
 * o comando throw e tratada utilizando try/catch.
 *
  * @author Pedro Giffoni
 */
public class EntradaInvalidaException extends Exception {

    /**
     * Construtor da exceção personalizada.
     *
     * Recebe uma mensagem explicando o motivo do erro e
     * a encaminha para a classe Exception.
     *
     * @param mensagem descrição do erro ocorrido.
     */
    public EntradaInvalidaException(String mensagem) {
        super(mensagem);
    }
}