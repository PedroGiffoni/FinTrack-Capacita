# Importa os recursos usados nas próximas etapas deste módulo.
import os
# Importa os recursos usados nas próximas etapas deste módulo.
import sqlite3
# Importa os recursos usados nas próximas etapas deste módulo.
import tempfile
# Importa os recursos usados nas próximas etapas deste módulo.
import unittest
# Importa os recursos usados nas próximas etapas deste módulo.
from pathlib import Path
# Importa os recursos usados nas próximas etapas deste módulo.
from contextlib import closing
# Importa os recursos usados nas próximas etapas deste módulo.
from types import SimpleNamespace
# Importa os recursos usados nas próximas etapas deste módulo.
from unittest.mock import patch

# Importa os recursos usados nas próximas etapas deste módulo.
from streamlit.testing.v1 import AppTest


class EduTest(unittest.TestCase):
    # Define a rotina setUp e os argumentos necessários ao seu uso.
    def setUp(self):
        # Prepara self.pasta para a próxima etapa desta rotina.
        self.pasta = tempfile.TemporaryDirectory()
        # Executa esta operação como parte do fluxo atual.
        self.addCleanup(self.pasta.cleanup)
        # Prepara self.banco para a próxima etapa desta rotina.
        self.banco = Path(self.pasta.name) / "fintrack.db"
        # Limita o uso do recurso ao bloco e garante sua liberação ao terminar.
        with closing(sqlite3.connect(self.banco)) as conexao:
            # Executa esta operação como parte do fluxo atual.
            conexao.execute(
                """CREATE TABLE transacoes (
                   id INTEGER PRIMARY KEY, descricao TEXT, valor_centavos INTEGER,
                   tipo TEXT, categoria TEXT, data TEXT, mensal INTEGER, dia_vencimento INTEGER)"""
            )
            # Executa esta operação como parte do fluxo atual.
            conexao.execute(
                "INSERT INTO transacoes VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                (1, "Receita de teste do banco", 12345, "receita", "Outros", "2026-06-01", 0, None),
            )
            # Executa esta operação como parte do fluxo atual.
            conexao.commit()
        # Prepara self.ambiente para a próxima etapa desta rotina.
        self.ambiente = patch.dict(os.environ, {"FINTRACK_BANCO": str(self.banco)})
        # Executa esta operação como parte do fluxo atual.
        self.ambiente.start()
        # Executa esta operação como parte do fluxo atual.
        self.addCleanup(self.ambiente.stop)
        # Prepara self.aplicacao para a próxima etapa desta rotina.
        self.aplicacao = Path(__file__).resolve().parents[1] / "src" / "app.py"

    # Define a rotina test_solicita_chave_antes_de_consultar e os argumentos necessários ao seu uso.
    def test_solicita_chave_antes_de_consultar(self):
        # Limita o uso do recurso ao bloco e garante sua liberação ao terminar.
        with patch("groq.Groq") as cliente:
            # Prepara a aplicação Streamlit para uma execução controlada pelo teste.
            tela = AppTest.from_file(str(self.aplicacao)).run(timeout=30)
            # Executa esta operação como parte do fluxo atual.
            self.assertEqual(len(tela.exception), 0)
            # Executa esta operação como parte do fluxo atual.
            tela.text_area[0].input("Qual é meu saldo atual?")
            # Executa esta operação como parte do fluxo atual.
            tela.button[0].click().run()
            # Executa esta operação como parte do fluxo atual.
            self.assertTrue(any("API Key" in aviso.value for aviso in tela.warning))
            # Executa esta operação como parte do fluxo atual.
            cliente.assert_not_called()

    # Define a rotina test_consulta_banco_e_atualiza_contexto_a_cada_pergunta e os argumentos necessários ao seu uso.
    def test_consulta_banco_e_atualiza_contexto_a_cada_pergunta(self):
        # Limita o uso do recurso ao bloco e garante sua liberação ao terminar.
        with patch("groq.Groq") as cliente:
            # Prepara consulta para a próxima etapa desta rotina.
            consulta = cliente.return_value.chat.completions.create
            # Prepara consulta.return_value para a próxima etapa desta rotina.
            consulta.return_value = SimpleNamespace(
                choices=[SimpleNamespace(message=SimpleNamespace(content="Resposta de teste."))]
            )
            # Prepara a aplicação Streamlit para uma execução controlada pelo teste.
            tela = AppTest.from_file(str(self.aplicacao)).run(timeout=30)
            # Executa esta operação como parte do fluxo atual.
            tela.text_input[0].input("chave-de-teste")
            # Executa esta operação como parte do fluxo atual.
            tela.text_area[0].input("Qual é meu saldo atual?")
            # Executa esta operação como parte do fluxo atual.
            tela.button[0].click().run()
            # Executa esta operação como parte do fluxo atual.
            self.assertEqual(len(tela.exception), 0)
            # Prepara contexto para a próxima etapa desta rotina.
            contexto = consulta.call_args.kwargs["messages"][0]["content"]
            # Executa esta operação como parte do fluxo atual.
            self.assertIn("Receita de teste do banco", contexto)
            # Executa esta operação como parte do fluxo atual.
            self.assertIn("123.45", contexto)
            # Executa esta operação como parte do fluxo atual.
            self.assertTrue(any("Resposta de teste." in trecho.value for trecho in tela.markdown))

            # Limita o uso do recurso ao bloco e garante sua liberação ao terminar.
            with closing(sqlite3.connect(self.banco)) as conexao:
                # Executa esta operação como parte do fluxo atual.
                conexao.execute(
                    "INSERT INTO transacoes VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    (2, "Nova despesa", 5000, "despesa", "Outros", "2026-06-02", 0, None),
                )
                # Executa esta operação como parte do fluxo atual.
                conexao.commit()
            # Executa esta operação como parte do fluxo atual.
            tela.button[0].click().run()
            # Executa esta operação como parte do fluxo atual.
            self.assertEqual(len(tela.exception), 0)
            # Prepara novo_contexto para a próxima etapa desta rotina.
            novo_contexto = consulta.call_args.kwargs["messages"][0]["content"]
            # Executa esta operação como parte do fluxo atual.
            self.assertIn("Nova despesa", novo_contexto)
            # Executa esta operação como parte do fluxo atual.
            self.assertIn("50.0", novo_contexto)


# Executa este bloco somente quando a condição é atendida.
if __name__ == "__main__":
    # Executa esta operação como parte do fluxo atual.
    unittest.main()
