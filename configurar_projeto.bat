@echo off
setlocal

REM ============================================================
REM CONFIGURADOR DO FINTRACK
REM ============================================================
REM
REM Este arquivo prepara o projeto para a primeira execucao.
REM
REM Ele:
REM 1. Verifica se o Java esta instalado.
REM 2. Verifica se o compilador Java (javac) esta instalado.
REM 3. Verifica se o Python esta instalado.
REM 4. Cria o ambiente virtual do Edu IA.
REM 5. Instala as dependencias do requirements.txt.
REM
REM Depois da configuracao, o projeto pode ser iniciado usando:
REM rodar_fintrack.bat
REM ============================================================

title Configuracao do FinTrack

echo.
echo ============================================================
echo            CONFIGURACAO INICIAL DO FINTRACK
echo ============================================================
echo.
echo Este processo ira preparar o FinTrack e o Edu IA.
echo.


REM ============================================================
REM 1. VERIFICAR JAVA
REM ============================================================

echo [1/5] Verificando Java...

java --version >nul 2>&1

if errorlevel 1 (
    echo.
    echo [ERRO] Java nao foi encontrado.
    echo.
    echo Instale o Java JDK antes de continuar.
    echo Depois, execute este arquivo novamente.
    echo.
    pause
    exit /b 1
)

echo [OK] Java encontrado.


REM ============================================================
REM 2. VERIFICAR COMPILADOR JAVA
REM ============================================================

echo.
echo [2/5] Verificando compilador Java...

javac --version >nul 2>&1

if errorlevel 1 (
    echo.
    echo [ERRO] O compilador javac nao foi encontrado.
    echo.
    echo O FinTrack precisa do JDK completo para compilar o projeto.
    echo Instale um Java JDK e execute este arquivo novamente.
    echo.
    pause
    exit /b 1
)

echo [OK] Compilador Java encontrado.


REM ============================================================
REM 3. VERIFICAR PYTHON
REM ============================================================

echo.
echo [3/5] Verificando Python...

python --version >nul 2>&1

if errorlevel 1 (
    echo.
    echo [ERRO] Python nao foi encontrado.
    echo.
    echo Instale o Python antes de continuar.
    echo Durante a instalacao, marque a opcao:
    echo Add Python to PATH
    echo.
    echo Depois, execute este arquivo novamente.
    echo.
    pause
    exit /b 1
)

echo [OK] Python encontrado.


REM ============================================================
REM 4. CRIAR AMBIENTE VIRTUAL DO EDU IA
REM ============================================================

echo.
echo [4/5] Configurando ambiente virtual do Edu IA...

REM %~dp0 representa automaticamente a pasta onde este BAT esta.
REM Isso permite executar o projeto mesmo depois de mover ou clonar
REM o repositorio para outro local.

cd /d "%~dp0eduIa"

REM Verifica se o ambiente virtual ja existe.
if exist ".venv\Scripts\python.exe" (
    echo [OK] Ambiente virtual ja existe.
) else (
    echo Criando ambiente virtual...
    
    python -m venv .venv

    if errorlevel 1 (
        echo.
        echo [ERRO] Nao foi possivel criar o ambiente virtual.
        echo.
        pause
        exit /b 1
    )

    echo [OK] Ambiente virtual criado.
)


REM ============================================================
REM 5. INSTALAR DEPENDENCIAS
REM ============================================================

echo.
echo [5/5] Instalando dependencias do Edu IA...
echo.
echo Aguarde. Na primeira execucao este processo pode levar
echo alguns minutos.
echo.

".venv\Scripts\python.exe" -m pip install --upgrade pip

if errorlevel 1 (
    echo.
    echo [AVISO] Nao foi possivel atualizar o pip.
    echo A configuracao continuara com a versao atual.
    echo.
)

".venv\Scripts\python.exe" -m pip install -r requirements.txt

if errorlevel 1 (
    echo.
    echo ============================================================
    echo [ERRO] Nao foi possivel instalar todas as dependencias.
    echo ============================================================
    echo.
    echo Verifique sua conexao com a internet e tente novamente.
    echo.
    pause
    exit /b 1
)


REM ============================================================
REM CONFIGURACAO CONCLUIDA
REM ============================================================

echo.
echo ============================================================
echo              CONFIGURACAO CONCLUIDA!
echo ============================================================
echo.
echo Java: OK
echo Compilador Java: OK
echo Python: OK
echo Ambiente virtual: OK
echo Dependencias do Edu IA: OK
echo.
echo O FinTrack esta pronto para ser executado.
echo.
echo Para iniciar o programa, execute:
echo.
echo     rodar_fintrack.bat
echo.
echo ============================================================

pause

endlocal