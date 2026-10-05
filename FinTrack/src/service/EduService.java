// Agrupa esta classe na camada service do projeto.
package service;

// Referencia Conexao, componente da camada dao utilizado neste fluxo.
import dao.Conexao;
// Disponibiliza o tratamento de falhas de arquivo ou comunicação.
import java.io.IOException;
// Disponibiliza o endereço de loopback do servidor local.
import java.net.InetAddress;
// Disponibiliza a escolha de uma porta disponível para o módulo legado.
import java.net.ServerSocket;
// Disponibiliza endereços de serviços e páginas externas.
import java.net.URI;
// Disponibiliza a comunicação HTTP com o serviço do Edu.
import java.net.http.HttpClient;
// Disponibiliza a montagem de pedidos HTTP.
import java.net.http.HttpRequest;
// Disponibiliza os códigos e conteúdos recebidos por HTTP.
import java.net.http.HttpResponse;
// Disponibiliza leitura, gravação e criação de arquivos e diretórios.
import java.nio.file.Files;
// Disponibiliza caminhos sem depender de separadores montados manualmente.
import java.nio.file.Path;
// Disponibiliza os limites de tempo de conexão e resposta.
import java.time.Duration;
// Disponibiliza listas cujo conteúdo pode crescer durante a execução.
import java.util.ArrayList;
// Disponibiliza a alteração da ordem dos descendentes antes de encerrar o processo.
import java.util.Collections;
// Disponibiliza a conversão e o uso de unidades de tempo nos limites de espera.
import java.util.concurrent.TimeUnit;

// Inicia e encerra o servidor da versão anterior do Edu, mantida em Python/Streamlit.
public class EduService implements AutoCloseable {
    // Localiza o módulo Streamlit legado, separado do Edu nativo Java.
    private final Path modulo;
    // Identifica o mesmo arquivo SQLite utilizado pelo aplicativo.
    private final Path banco;
    // Mantém a referência ao subprocesso legado iniciado por esta instância.
    private Process processo;
    // Guarda o endereço local do servidor legado enquanto ele estiver ativo.
    private URI endereco;
    // Permite que as threads observem o encerramento do serviço.
    private volatile boolean encerrado;

    // Construtor que prepara as dependências e o estado inicial desta classe.
    public EduService() {
        // O caminho padrão da versão antiga aponta para eduIa e para o SQLite do projeto.
        this(localizarModulo(), Conexao.pastaDados().resolve("fintrack.db"));
    }

    // Construtor que prepara as dependências e o estado inicial desta classe.
    public EduService(Path modulo, Path banco) {
        // Normaliza a pasta que contém a versão Python do Edu.
        this.modulo = modulo.toAbsolutePath().normalize();
        // Guarda o caminho absoluto do banco que será informado ao processo Python.
        this.banco = banco.toAbsolutePath().normalize();
    }

    // Encontra a pasta do módulo legado a partir do diretório de execução.
    private static Path localizarModulo() {
        // Resolve a localização do módulo a partir do diretório de execução.
        Path atual = Path.of("").toAbsolutePath();
        // Primeiro verifica se a execução começou na raiz do repositório.
        if (Files.isDirectory(atual.resolve("eduIa"))) return atual.resolve("eduIa");
        // Também aceita execução a partir da pasta interna FinTrack.
        if (atual.getParent() != null && Files.isDirectory(atual.getParent().resolve("eduIa"))) {
            // Procura o módulo legado ao lado da pasta FinTrack.
            return atual.getParent().resolve("eduIa");
        }
        // Mantém um caminho previsível; iniciar informará erro se o módulo não existir.
        return atual.resolve("eduIa");
    }

    // Prepara o recurso antes de devolvê-lo ao chamador.
    public URI iniciar() throws IOException, InterruptedException {
        // A referência local distingue este processo de uma eventual tentativa posterior.
        Process iniciado;
        // O endereço ficará associado à porta reservada para esta instância.
        URI url;
        // Protege a criação e a troca do processo; a espera HTTP ocorre fora deste bloqueio.
        synchronized (this) {
            // Uma instância já fechada não deve iniciar outro processo.
            if (encerrado) throw new IOException("O Edu já foi encerrado.");
            // Reutiliza o servidor existente e inicia outro somente se o anterior não estiver ativo.
            if (processo == null || !processo.isAlive()) {
                // Localiza o arquivo que o Streamlit precisa executar.
                Path aplicacao = modulo.resolve("src/app.py");
                // Apresenta uma orientação quando o módulo não foi encontrado.
                if (!Files.isRegularFile(aplicacao)) {
                    // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
                    throw new IOException("O módulo Edu não foi encontrado na pasta eduIa.");
                }
                // Resolve o interpretador antes de criar o processo.
                Path python = localizarPython();
                // A saída de inicialização fica em um diretório local ignorado pelo Git.
                Path logs = modulo.resolve("logs");
                // Cria a pasta de logs se ainda não existir.
                Files.createDirectories(logs);
                // Reserva a variável que receberá uma porta livre do sistema.
                int porta;
                // Reserva uma porta disponível somente no loopback, sem expor o servidor na rede.
                try (ServerSocket socket = new ServerSocket(0, 0, InetAddress.getByName("127.0.0.1"))) {
                    // O sistema escolhe uma porta livre; ela será passada ao Streamlit.
                    porta = socket.getLocalPort();
                }
                // Executa Python diretamente, sem montar um comando textual de shell.
                ProcessBuilder comando = new ProcessBuilder(
                        python.toString(), "-m", "streamlit", "run", aplicacao.toString(),
                        "--server.address=127.0.0.1", "--server.port=" + porta,
                        "--server.headless=true", "--browser.gatherUsageStats=false");
                // O módulo executa em sua própria pasta para resolver recursos relativos.
                comando.directory(modulo.toFile());
                // Compartilha o caminho do mesmo SQLite usado pela aplicação Java.
                comando.environment().put("FINTRACK_BANCO", banco.toString());
                // Padroniza a codificação do processo Python.
                comando.environment().put("PYTHONUTF8", "1");
                // Reúne saída normal e erros de inicialização no mesmo destino.
                comando.redirectErrorStream(true);
                // Acrescenta diagnósticos ao log local sem enviar a chave por argumentos.
                comando.redirectOutput(ProcessBuilder.Redirect.appendTo(logs.resolve("edu.log").toFile()));
                // Inicia somente o processo que esta instância ficará responsável por encerrar.
                processo = comando.start();
                // O servidor é acessado pelo loopback e pela porta desta inicialização.
                endereco = URI.create("http://127.0.0.1:" + porta);
            }
            // Captura o processo que esta chamada está aguardando.
            iniciado = processo;
            // Captura o endereço correspondente ao mesmo processo.
            url = endereco;
        }

        // Usa um timeout curto porque a consulta é apenas ao servidor local.
        HttpClient cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build();
        // O endpoint de saúde informa quando o Streamlit está pronto.
        HttpRequest consulta = HttpRequest.newBuilder(url.resolve("/_stcore/health"))
                .timeout(Duration.ofSeconds(2)).GET().build();
        // Usa relógio monotônico para limitar a espera a 30 segundos.
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
        try {
            // A inicialização é consultada repetidamente, até sucesso ou limite de tempo.
            while (System.nanoTime() < limite) {
                // Detecta fechamento da instância ou falha do processo durante a inicialização.
                if (encerrado || !iniciado.isAlive()) {
                    // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
                    throw new IOException("Não foi possível iniciar o Edu. Execute configurar_edu.bat e confira eduIa/logs/edu.log.");
                }
                // Delimita uma operação que pode falhar e possui tratamento específico abaixo.
                try {
                    // HTTP 200 indica que o servidor local está pronto para receber a interface.
                    if (cliente.send(consulta, HttpResponse.BodyHandlers.discarding()).statusCode() == 200) {
                        // Entrega o endereço somente depois da verificação de saúde.
                        return url;
                    }
                // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
                } catch (IOException e) {
                    // O servidor ainda pode estar carregando.
                }
                // Espaça as consultas para não ocupar a CPU enquanto o servidor carrega.
                Thread.sleep(200);
            }
            // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
            throw new IOException("O Edu demorou para iniciar. Confira eduIa/logs/edu.log e tente novamente.");
        // Trata a falha desta operação sem continuar como se o resultado tivesse sido obtido.
        } catch (IOException | InterruptedException e) {
            // Protege a criação e a troca do processo; a espera HTTP ocorre fora deste bloqueio.
            synchronized (this) {
                // Uma falha só deve encerrar o processo associado a esta tentativa.
                if (processo == iniciado) pararProcesso();
            }
            // Propaga a falha original, mantendo seu tipo e diagnóstico.
            throw e;
        }
    }

    // Seleciona o interpretador configurado ou o ambiente virtual do módulo.
    private Path localizarPython() throws IOException {
        // Permite informar outro Python por propriedade da JVM.
        String configurado = System.getProperty("fintrack.python");
        // Sem configuração explícita, usa o ambiente virtual local apropriado ao sistema.
        Path python = configurado != null && !configurado.isBlank() ? Path.of(configurado)
                : modulo.resolve(System.getProperty("os.name").toLowerCase().contains("win")
                        ? ".venv/Scripts/python.exe" : ".venv/bin/python");
        // Orienta configurar o módulo quando o interpretador ainda não existe.
        if (!Files.isRegularFile(python)) {
            // Interrompe este fluxo com uma mensagem que o chamador pode apresentar ou verificar.
            throw new IOException("Configure o Edu executando configurar_edu.bat na pasta principal do projeto.");
        }
        // Devolve um caminho que não depende do diretório usado pelo ProcessBuilder.
        return python.toAbsolutePath();
    }

    // Encerra somente o processo iniciado por esta instância e seus descendentes.
    private void pararProcesso() {
        // Encerrar uma instância sem processo não deve tentar acessar recursos inexistentes.
        if (processo != null) {
            // Captura os descendentes pertencentes ao servidor iniciado pelo módulo.
            var filhos = new ArrayList<>(processo.descendants().toList());
            // Tenta encerrar os descendentes antes de seu processo pai.
            Collections.reverse(filhos);
            // Finaliza apenas os processos vinculados a esta instância.
            filhos.forEach(ProcessHandle::destroyForcibly);
            // Finaliza o servidor próprio, sem interferir em outros servidores Streamlit.
            processo.destroyForcibly();
            // Remove a referência ao processo encerrado.
            processo = null;
            // O endereço anterior não representa mais um servidor ativo.
            endereco = null;
        }
    }

    // Indica que este membro implementa ou substitui um comportamento definido no tipo pai.
    @Override
    // Implementa AutoCloseable para liberar o recurso no try-with-resources.
    public synchronized void close() {
        // Impede novos inícios após close.
        encerrado = true;
        // Libera o servidor iniciado por esta instância.
        pararProcesso();
    }
}
