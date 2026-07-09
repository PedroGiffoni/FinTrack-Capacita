package utils;

/**
 * Classe utilitária responsável por formatar valores utilizados
 * no sistema FinTrack.
 *
 * Atualmente possui um método para formatar valores monetários
 * no padrão brasileiro.
 *
 * @author Pedro Giffoni
 */
public class Formatador {

    /**
     * Formata um valor decimal para o formato monetário.
     *
     * Exemplo de saída:
     * R$ 1500.50 -> R$ 1500,50 (dependendo da configuração da JVM)
     */
    public static String formatarMoeda(double valor) {
        return String.format("R$ %.2f", valor);
    }
}