// Agrupa esta classe na camada exceptions do projeto.
package exceptions;

// Exceção não verificada que preserva a causa técnica de falhas de persistência.
public class PersistenciaException extends RuntimeException {
    // Construtor que prepara as dependências e o estado inicial desta classe.
    public PersistenciaException(String mensagem, Throwable causa) {
        // Encaminha os atributos ao construtor da classe pai, reaproveitando seu estado e validações.
        super(mensagem, causa);
    }
}
