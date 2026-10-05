@echo off
REM Mantém as variáveis deste lançador restritas à execução atual.
setlocal
REM Usa UTF-8 para apresentar os textos no terminal.
chcp 65001 >nul
REM Chama o script que verifica os requisitos e executa a ação.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\configurar_edu.ps1"
REM Guarda um valor necessário ao fluxo deste lançador.
set "resultado=%errorlevel%"
REM Mantém a mensagem visível até o usuário pressionar uma tecla.
pause
REM Retorna o resultado ao terminal que iniciou este arquivo.
exit /b %resultado%
