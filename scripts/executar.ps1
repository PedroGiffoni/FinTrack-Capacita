# Recebe a ação solicitada pelo arquivo BAT ou pelo terminal.
param(
    [ValidateSet('compilar', 'testar', 'interface', 'migrar')]
    [string]$Acao = 'interface'
)
# Interrompe o script quando uma operação PowerShell falha.
$ErrorActionPreference = 'Stop'
# Resolve a raiz a partir do local do script, sem depender da pasta atual.
$raizProjeto = Split-Path -Parent $PSScriptRoot
# Executa os comandos relativos à raiz do projeto.
Set-Location -LiteralPath $raizProjeto

# Monta a lista de possíveis instalações do JDK.
$candidatos = @()
if ($env:JAVA_HOME) { $candidatos += $env:JAVA_HOME }
$javaAtual = Get-Command java.exe -ErrorAction SilentlyContinue
if ($javaAtual) { $candidatos += Split-Path -Parent (Split-Path -Parent $javaAtual.Source) }
foreach ($pasta in @("$env:ProgramFiles/Java", "$env:ProgramFiles/Eclipse Adoptium", "$env:ProgramFiles/Microsoft")) {
    if (Test-Path -LiteralPath $pasta) {
        # Monta a lista de possíveis instalações do JDK.
        $candidatos += Get-ChildItem -LiteralPath $pasta -Directory | Select-Object -ExpandProperty FullName
    }
}
$jdkSelecionado = $null
# Verifica as instalações encontradas até localizar um JDK compatível.
foreach ($candidato in ($candidatos | Select-Object -Unique)) {
    if (!(Test-Path -LiteralPath "$candidato/bin/javac.exe")) { continue }
    $consulta = New-Object System.Diagnostics.Process
    # Configura a consulta de versão em um processo separado.
    $consulta.StartInfo.FileName = "$candidato/bin/java.exe"
    # Configura a consulta de versão em um processo separado.
    $consulta.StartInfo.Arguments = '--version'
    # Configura a consulta de versão em um processo separado.
    $consulta.StartInfo.UseShellExecute = $false
    # Configura a consulta de versão em um processo separado.
    $consulta.StartInfo.CreateNoWindow = $true
    # Configura a consulta de versão em um processo separado.
    $consulta.StartInfo.RedirectStandardOutput = $true
    # Configura a consulta de versão em um processo separado.
    $consulta.StartInfo.RedirectStandardError = $true
    try {
        [void]$consulta.Start()
        # Lê o fluxo sem bloquear o outro canal de saída do processo.
        $saida = $consulta.StandardOutput.ReadToEndAsync()
        # Lê o fluxo sem bloquear o outro canal de saída do processo.
        $avisos = $consulta.StandardError.ReadToEndAsync()
        $consulta.WaitForExit()
        $versao = $saida.Result
        [void]$avisos.Result
        $codigoSaida = $consulta.ExitCode
    } finally {
        $consulta.Dispose()
    }
    if ($codigoSaida -eq 0 -and ($versao -match '(?m)^(?:openjdk|java) (\d+)') -and [int]$Matches[1] -ge 17) {
        $jdkSelecionado = $candidato
        break
    }
}
if (!$jdkSelecionado) { throw 'Instale um JDK 17 ou superior e configure JAVA_HOME.' }
if (!(Get-Command mvn.cmd -ErrorAction SilentlyContinue)) { throw 'Instale o Apache Maven e adicione sua pasta bin ao PATH.' }
# Seleciona o JDK somente no ambiente deste processo.
$env:JAVA_HOME = $jdkSelecionado
# Prioriza os executáveis do JDK selecionado no ambiente atual.
$env:PATH = "$jdkSelecionado/bin;$env:PATH"
# Mostra no terminal o andamento ou as instruções de execução.
Write-Host "JDK: $jdkSelecionado"

# Usa saída de lote e um repositório Maven local ao projeto.
$argumentos = @('--batch-mode', '--no-transfer-progress', '-Dmaven.repo.local=.m2/repository')
# Encaminha a ação solicitada para o comando correspondente.
switch ($Acao) {
    # Empacota a aplicação sem executar os testes.
    'compilar' { & mvn.cmd @argumentos '-DskipTests' package }
    # Executa os testes automatizados.
    'testar' { & mvn.cmd @argumentos test }
    # Inicia a aplicação JavaFX pelo plugin Maven.
    'interface' { & mvn.cmd @argumentos javafx:run }
    # Executa a importação única dos dados antigos para o SQLite.
    'migrar' {
        # Executa o programa indicado com os argumentos definidos nesta etapa.
        & mvn.cmd @argumentos '-DskipTests' compile 'dependency:build-classpath' '-Dmdep.outputFile=target/classpath.txt'
        # Não prossegue quando o comando externo anterior falhou.
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
        $dependencias = (Get-Content -LiteralPath 'target/classpath.txt' -Raw).Trim()
        # Executa o programa indicado com os argumentos definidos nesta etapa.
        & "$jdkSelecionado/bin/java.exe" -cp "target/classes;$dependencias" service.MigracaoDados
    }
}
# Devolve ao terminal o resultado do último comando externo.
exit $LASTEXITCODE
