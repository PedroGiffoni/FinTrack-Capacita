@echo off
REM Mantém as variáveis deste lançador restritas à execução atual.
setlocal
REM Usa UTF-8 para apresentar os textos no terminal.
chcp 65001 >nul
REM Acessa a pasta deste arquivo, inclusive quando estiver em outra unidade.
cd /d "%~dp0"
if not exist ".venv\Scripts\python.exe" (
    echo Configure o Edu executando configurar_edu.bat na pasta principal do projeto.
    REM Mantém a mensagem visível até o usuário pressionar uma tecla.
    pause
    REM Retorna o resultado ao terminal que iniciou este arquivo.
    exit /b 1
)
".venv\Scripts\python.exe" -m streamlit run src/app.py --server.address=127.0.0.1 --browser.gatherUsageStats=false
REM Trata a falha devolvida pelo comando anterior.
if errorlevel 1 pause
REM Restaura o ambiente existente antes de setlocal.
endlocal
