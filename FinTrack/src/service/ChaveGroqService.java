// Agrupa esta classe na camada service do projeto.
package service;

// Disponibiliza as funções nativas de proteção de dados do Windows.
import com.sun.jna.platform.win32.Crypt32Util;
// Disponibiliza as opções da chamada DPAPI no escopo do usuário.
import com.sun.jna.platform.win32.WinCrypt;
// Disponibiliza o tratamento de falhas de arquivo ou comunicação.
import java.io.IOException;
// Disponibiliza a seleção explícita de UTF-8.
import java.nio.charset.StandardCharsets;
// Disponibiliza operações com arquivos e caminhos.
import java.nio.file.*;
// Disponibiliza a limpeza de buffers utilizados na proteção da credencial.
import java.util.Arrays;
// Disponibiliza a representação de uma chave salva que pode estar ausente.
import java.util.Optional;

// Guarda a credencial criptografada com DPAPI por usuário; nenhuma chave é escrita no SQLite.
public class ChaveGroqService {
    // Caminho da cópia cifrada dentro do perfil do usuário ou da pasta temporária do teste.
    private final Path arquivo;

    // Construtor que prepara as dependências e o estado inicial desta classe.
    public ChaveGroqService() {
        // Obtém a pasta privada da conta atual, fora do repositório e do banco.
        String pasta = System.getenv("LOCALAPPDATA");
        // Sem LOCALAPPDATA, o armazenamento protegido fica indisponível.
        arquivo = pasta == null || pasta.isBlank() ? null
                // Cada conta do Windows usa seu próprio arquivo criptografado.
                : Path.of(pasta, "FinTrack", "credenciais", "groq.dpapi");
    }

    // Construtor que prepara as dependências e o estado inicial desta classe.
    public ChaveGroqService(Path arquivo) {
        // Permite usar uma pasta temporária nos testes sem alterar credenciais reais.
        this.arquivo = arquivo.toAbsolutePath().normalize();
    }

    // Verifica se há suporte à proteção de credenciais por usuário do Windows.
    public boolean disponivel() {
        // DPAPI é específico do Windows; não há fallback que salve a chave em texto puro.
        return System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT).startsWith("windows")
                // Além do sistema compatível, é preciso ter um caminho de armazenamento.
                && arquivo != null;
    }

    // Recupera a chave protegida no perfil atual e devolve Optional.empty quando ela não existe.
    public Optional<String> carregar() throws IOException {
        // Interrompe a operação em sistemas sem suporte à proteção nativa.
        verificarDisponibilidade();
        // Uma instalação sem chave salva devolve Optional.empty, sem representar um erro.
        if (!Files.exists(arquivo)) return Optional.empty();
        // Lê somente o conteúdo criptografado; o arquivo não contém a chave em texto puro.
        byte[] protegido = Files.readAllBytes(arquivo);
        // Reserva a referência para limpar o buffer após a conversão.
        byte[] aberto = null;
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // Descriptografa com a conta atual; UI_FORBIDDEN impede diálogos nativos inesperados.
            aberto = Crypt32Util.cryptUnprotectData(protegido, WinCrypt.CRYPTPROTECT_UI_FORBIDDEN);
            // Converte o buffer UTF-8 em uma credencial para a sessão.
            // String é imutável e não pode ser zerada; por isso seu uso deve ficar restrito à memória da sessão.
            String chave = new String(aberto, StandardCharsets.UTF_8);
            // Rejeita um conteúdo recuperado que não contenha uma credencial.
            if (chave.isBlank()) throw new IOException("A chave salva está inválida. Esqueça a chave e informe outra.");
            // Indica a existência de uma chave recuperada, sem exibi-la ou registrá-la em log.
            return Optional.of(chave);
        // Uma falha nativa retorna mensagem genérica, sem revelar credenciais.
        } catch (RuntimeException e) {
            // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
            throw new IOException("Não foi possível abrir a chave salva nesta conta do Windows. Esqueça a chave e informe outra.");
        // Executa a limpeza obrigatória tanto no sucesso quanto na falha.
        } finally {
            // Zera o buffer criptografado depois do uso.
            Arrays.fill(protegido, (byte) 0);
            // Zera o buffer de texto puro; isso não garante apagar cópias em Strings da JVM.
            if (aberto != null) Arrays.fill(aberto, (byte) 0);
        }
    }

    // Valida e grava os dados; a apresentação de erro fica sob responsabilidade do chamador.
    public void salvar(String chave) throws IOException {
        // Interrompe a operação em sistemas sem suporte à proteção nativa.
        verificarDisponibilidade();
        // Não cria arquivo para uma credencial ausente ou vazia.
        if (chave == null || chave.isBlank()) throw new IllegalArgumentException("Informe a chave antes de salvar.");
        // Remove espaços nas extremidades e prepara os bytes UTF-8 para a API nativa.
        byte[] aberto = chave.trim().getBytes(StandardCharsets.UTF_8);
        // A referência permite limpar o buffer cifrado mesmo se uma etapa posterior falhar.
        byte[] protegido = null;
        // Mantém o caminho do arquivo temporário para removê-lo também em falhas.
        Path temporario = null;
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // Criptografa com DPAPI no escopo do usuário.
            // LOCAL_MACHINE não é usado: essa opção permitiria descriptografia por outras contas do computador.
            protegido = Crypt32Util.cryptProtectData(aberto, WinCrypt.CRYPTPROTECT_UI_FORBIDDEN);
            // Cria o diretório dentro do perfil atual, sem usar uma pasta compartilhada do projeto.
            Files.createDirectories(arquivo.getParent());
            // O temporário será preenchido apenas com conteúdo já criptografado.
            temporario = Files.createTempFile(arquivo.getParent(), "groq-", ".tmp");
            // Grava o resultado da criptografia, nunca o buffer de texto puro.
            Files.write(temporario, protegido);
            // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
            try {
                // Substitui o arquivo inteiro de uma vez quando o sistema de arquivos suporta movimento atômico.
                Files.move(temporario, arquivo, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            // Utiliza a substituição comum quando o armazenamento não suporta movimento atômico.
            } catch (AtomicMoveNotSupportedException e) {
                // Substitui a cópia antiga quando o sistema não oferece movimentação atômica.
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
            }
        // Uma falha nativa retorna mensagem genérica, sem revelar credenciais.
        } catch (RuntimeException e) {
            // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
            throw new IOException("Não foi possível proteger a chave com a conta do Windows.");
        // Executa a limpeza obrigatória tanto no sucesso quanto na falha.
        } finally {
            // Limpa o buffer que continha a chave logo após a tentativa de gravação.
            Arrays.fill(aberto, (byte) 0);
            // Limpa também o buffer cifrado criado para persistência.
            if (protegido != null) Arrays.fill(protegido, (byte) 0);
            // Remove o temporário caso a substituição não o tenha consumido.
            if (temporario != null) Files.deleteIfExists(temporario);
        }
    }

    // Verifica presença sem descriptografar; a interface usa isso para habilitar Esquecer.
    public boolean existe() { return arquivo != null && Files.exists(arquivo); }

    // Apaga o arquivo criptografado; repetir a operação não deve causar erro.
    public void esquecer() throws IOException {
        // Interrompe a operação em sistemas sem suporte à proteção nativa.
        verificarDisponibilidade();
        // Apaga a cópia criptografada; executar novamente também é permitido.
        Files.deleteIfExists(arquivo);
    }

    // Impede usar o armazenamento protegido em um sistema sem suporte.
    private void verificarDisponibilidade() throws IOException {
        // Sem proteção nativa, o usuário continua podendo usar a chave apenas na sessão.
        if (!disponivel()) throw new IOException("Salvar a chave está disponível apenas no Windows. Use a chave nesta sessão.");
    }
}
