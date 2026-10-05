@echo off
REM Mantém as variáveis deste lançador restritas à execução atual.
setlocal
REM Usa UTF-8 para apresentar os textos no terminal.
chcp 65001 >nul
REM Acessa a pasta deste arquivo, inclusive quando estiver em outra unidade.
cd /d "%~dp0"
REM Chama o script que verifica os requisitos e executa a ação.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\executar.ps1" -Acao interface
REM Trata a falha devolvida pelo comando anterior.
if errorlevel 1 (
    echo.
    echo Nao foi possivel iniciar o FinTrack. Confira a mensagem acima.
    REM Mantém a mensagem visível até o usuário pressionar uma tecla.
    pause
)
REM Restaura o ambiente existente antes de setlocal.
endlocal
