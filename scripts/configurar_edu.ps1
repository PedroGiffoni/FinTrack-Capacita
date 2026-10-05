# Interrompe o script quando uma operação PowerShell falha.
$ErrorActionPreference = 'Stop'
# Resolve a raiz a partir do local do script, sem depender da pasta atual.
$raizProjeto = Split-Path -Parent $PSScriptRoot
$pastaEdu = Join-Path $raizProjeto 'eduIa'
# Executa os comandos relativos à raiz do projeto.
Set-Location -LiteralPath $pastaEdu
$pythonEdu = Join-Path $pastaEdu '.venv/Scripts/python.exe'

if (!(Test-Path -LiteralPath $pythonEdu)) {
    $python = Get-Command python.exe -ErrorAction SilentlyContinue
    $py = Get-Command py.exe -ErrorAction SilentlyContinue
    if ($python) {
        # Executa o programa indicado com os argumentos definidos nesta etapa.
        & $python.Source -m venv .venv
    } elseif ($py) {
        # Executa o programa indicado com os argumentos definidos nesta etapa.
        & $py.Source -3 -m venv .venv
    } else {
        # Informa o requisito ausente e interrompe a execução.
        throw 'Instale Python 3 e adicione-o ao PATH para configurar o Edu.'
    }
    # Não prossegue quando o comando externo anterior falhou.
    if ($LASTEXITCODE -ne 0) { throw 'Nao foi possivel criar o ambiente Python do Edu.' }
}
# Executa o programa indicado com os argumentos definidos nesta etapa.
& $pythonEdu -m pip install --no-cache-dir -r requirements.txt
# Não prossegue quando o comando externo anterior falhou.
if ($LASTEXITCODE -ne 0) { throw 'Nao foi possivel instalar as dependencias do Edu. Confira a conexao e tente novamente.' }
# Executa o programa indicado com os argumentos definidos nesta etapa.
& $pythonEdu -c 'import streamlit, pandas, groq, requests'
# Não prossegue quando o comando externo anterior falhou.
if ($LASTEXITCODE -ne 0) { throw 'O ambiente do Edu nao passou na verificacao de dependencias.' }
# Mostra no terminal o andamento ou as instruções de execução.
Write-Host 'Modulo legado Streamlit configurado. Execute eduIa/rodar_edu.bat para iniciar.'
