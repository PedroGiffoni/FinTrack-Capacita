# Importa funções para trabalhar com arquivos e caminhos do sistema.
import os
# Importa os recursos usados nas próximas etapas deste módulo.
import sqlite3
# Importa os recursos usados nas próximas etapas deste módulo.
from contextlib import closing
# Importa os recursos usados nas próximas etapas deste módulo.
from pathlib import Path

# Importa funções para leitura de arquivos JSON.
import json

# Importa o Pandas para leitura das bases em CSV.
import pandas as pd

# Importa o Streamlit para criar a interface web.
import streamlit as st

# Importa a classe Groq para conexão com a API.
from groq import Groq

# Importa Requests para consultar cotações externas.
import requests

# Importa expressões regulares para pequenos tratamentos de texto.
import re


# ============================================================
# 1. CONFIGURAÇÃO DA PÁGINA
# ============================================================

# Apresenta ou atualiza este elemento na interface Streamlit.
st.set_page_config(
    page_title="Edu - Educador Financeiro Inteligente",
    page_icon="💸",
    layout="wide",
    initial_sidebar_state="expanded"
)


# ============================================================
# 2. MODELO UTILIZADO NA GROQ
# ============================================================

# Prepara MODEL_NAME para a próxima etapa desta rotina.
MODEL_NAME = "openai/gpt-oss-20b"


# ============================================================
# 3. BARRA LATERAL
# ============================================================

# Limita o uso do recurso ao bloco e garante sua liberação ao terminar.
with st.sidebar:

    # Título da barra lateral.
    st.title("💸 Edu IA")

    # Pequena apresentação do assistente.
    st.markdown(
        "Educador financeiro inteligente conectado aos dados do FinTrack."
    )

    # Campo protegido para o usuário inserir sua própria chave.
    groq_api_key = st.text_input(
        "Insira sua API Key da Groq",
        type="password",
        help="Crie sua chave em https://console.groq.com/keys"
    )

    # Link para criação da chave.
    st.markdown(
        "🔑 [Criar uma chave da Groq]"
        "(https://console.groq.com/keys)"
    )

    # Apresenta ou atualiza este elemento na interface Streamlit.
    st.markdown("---")

    # Apresenta ou atualiza este elemento na interface Streamlit.
    st.markdown(
        f"**Modelo utilizado:** `{MODEL_NAME}`"
    )

    # Apresenta ou atualiza este elemento na interface Streamlit.
    st.markdown("---")

    # Apresenta ou atualiza este elemento na interface Streamlit.
    st.caption(
        "A chave é utilizada somente durante a execução da aplicação "
        "e não é salva pelo Edu."
    )

    # Apresenta ou atualiza este elemento na interface Streamlit.
    st.caption(
        "A IA pode cometer erros. Verifique informações importantes."
    )


# ============================================================
# 4. FUNÇÃO PARA CARREGAR AS BASES DE CONHECIMENTO
# ============================================================

# Define a rotina carregar_bases e os argumentos necessários ao seu uso.
def carregar_bases():

    # Localiza a pasta data do Edu.
    base_dir = os.path.join(
        os.path.dirname(__file__),
        "..",
        "data"
    )

    # Caminho do perfil financeiro.
    perfil_path = os.path.join(
        base_dir,
        "perfil_investidor.json"
    )

    # Caminho do banco de dados do FinTrack.
    transacoes_path = os.path.abspath(os.environ.get("FINTRACK_BANCO") or
        os.path.join(
            os.path.dirname(__file__),
            "..",
            "..",
            "FinTrack",
            "dados",
            "fintrack.db"
        )
    )

    # Caminho do histórico de atendimento.
    historico_path = os.path.join(
        base_dir,
        "historico_atendimento.csv"
    )

    # Caminho da lista de produtos financeiros.
    produtos_path = os.path.join(
        base_dir,
        "produtos_financeiros.json"
    )

    # Valores iniciais usados caso os arquivos não existam.
    perfil = {}
    # Prepara transacoes para a próxima etapa desta rotina.
    transacoes = pd.DataFrame()
    # Prepara historico para a próxima etapa desta rotina.
    historico = pd.DataFrame()
    # Prepara produtos para a próxima etapa desta rotina.
    produtos = {}

    # Carrega o perfil financeiro.
    if os.path.exists(perfil_path):
        # Limita o uso do recurso ao bloco e garante sua liberação ao terminar.
        with open(
            perfil_path,
            "r",
            encoding="utf-8"
        ) as arquivo:
            # Prepara perfil para a próxima etapa desta rotina.
            perfil = json.load(arquivo)

    # Consulta o mesmo banco utilizado pela aplicação Java.
    if os.path.exists(transacoes_path):
        # Agrupa operações que podem falhar para tratar seus erros.
        try:
            # Prepara uri para a próxima etapa desta rotina.
            uri = Path(transacoes_path).as_uri() + "?mode=ro"
            # Limita o uso do recurso ao bloco e garante sua liberação ao terminar.
            with closing(sqlite3.connect(uri, uri=True)) as conexao:
                # Converte o resultado SQL em um DataFrame para organizar o contexto financeiro.
                transacoes = pd.read_sql_query(
                    """SELECT id, descricao, valor_centavos / 100.0 AS valor,
                              tipo, categoria, strftime('%d/%m/%Y', data) AS data,
                              mensal, COALESCE(dia_vencimento, 0) AS dia
                       FROM transacoes ORDER BY transacoes.data, id""",
                    conexao
                )
        except (sqlite3.Error, pd.errors.DatabaseError):
            # Apresenta ou atualiza este elemento na interface Streamlit.
            st.warning("Não foi possível consultar as transações. Confira o banco do FinTrack.")
    else:
        # Apresenta ou atualiza este elemento na interface Streamlit.
        st.warning("Banco do FinTrack não encontrado. Execute o FinTrack antes de abrir este módulo.")

    # Carrega o histórico de atendimento.
    if os.path.exists(historico_path):
        # Prepara historico para a próxima etapa desta rotina.
        historico = pd.read_csv(
            historico_path,
            sep=","
        )

    # Carrega a lista de produtos financeiros.
    if os.path.exists(produtos_path):
        # Limita o uso do recurso ao bloco e garante sua liberação ao terminar.
        with open(
            produtos_path,
            "r",
            encoding="utf-8"
        ) as arquivo:
            # Prepara produtos para a próxima etapa desta rotina.
            produtos = json.load(arquivo)

    # Entrega o resultado desta rotina ao chamador.
    return perfil, transacoes, historico, produtos


# ============================================================
# 5. FUNÇÃO PARA BUSCAR COTAÇÕES REAIS
# ============================================================

# Define a rotina buscar_cotacao e os argumentos necessários ao seu uso.
def buscar_cotacao(moeda):

    # Endereços da AwesomeAPI para cada moeda aceita.
    urls = {
        "dolar": "https://economia.awesomeapi.com.br/json/last/USD-BRL",
        "dollar": "https://economia.awesomeapi.com.br/json/last/USD-BRL",
        "usd": "https://economia.awesomeapi.com.br/json/last/USD-BRL",
        "euro": "https://economia.awesomeapi.com.br/json/last/EUR-BRL",
        "eur": "https://economia.awesomeapi.com.br/json/last/EUR-BRL",
        "bitcoin": "https://economia.awesomeapi.com.br/json/last/BTC-BRL",
        "btc": "https://economia.awesomeapi.com.br/json/last/BTC-BRL"
    }

    # Prepara moeda para a próxima etapa desta rotina.
    moeda = moeda.lower()

    # Executa este bloco somente quando a condição é atendida.
    if moeda not in urls:
        # Entrega o resultado desta rotina ao chamador.
        return None

    # Agrupa operações que podem falhar para tratar seus erros.
    try:
        # Prepara response para a próxima etapa desta rotina.
        response = requests.get(
            urls[moeda],
            timeout=5
        )

        # Prepara data para a próxima etapa desta rotina.
        data = response.json()

        # Executa este bloco somente quando a condição é atendida.
        if "USDBRL" in data:
            # Entrega o resultado desta rotina ao chamador.
            return float(data["USDBRL"]["bid"])

        # Executa este bloco somente quando a condição é atendida.
        if "EURBRL" in data:
            # Entrega o resultado desta rotina ao chamador.
            return float(data["EURBRL"]["bid"])

        # Executa este bloco somente quando a condição é atendida.
        if "BTCBRL" in data:
            # Entrega o resultado desta rotina ao chamador.
            return float(data["BTCBRL"]["bid"])

        # Entrega o resultado desta rotina ao chamador.
        return None

    except Exception:
        # Entrega o resultado desta rotina ao chamador.
        return None


# ============================================================
# 6. FUNÇÃO PARA MONTAR O PROMPT COM CONTEXTO
# ============================================================

# Define a rotina montar_prompt e os argumentos necessários ao seu uso.
def montar_prompt(
    pergunta,
    perfil,
    transacoes,
    historico,
    produtos
):

    # Define o comportamento do Edu.
    prompt_sistema = """
Você é o Edu, um educador e consultor financeiro inteligente.

OBJETIVO:
Ensinar finanças pessoais de forma simples e clara.
Você também pode fornecer cotações de moedas, pois recebe esses dados
de uma API externa.

REGRAS:
- Nunca invente nomes para o usuário.
- Só use um nome se o usuário disser explicitamente.
- Caso contrário, trate o usuário apenas como "você".
- Não crie saudações artificiais.
- Comece a resposta diretamente com o conteúdo.
- Você pode fornecer cotações reais quando a API retornar valores.
- Depois da cotação, pode complementar com explicações educativas.
- Não invente dados que não vieram da API ou das bases.
- Não recomende investimentos específicos.
- Não prometa rentabilidade.
- Use linguagem simples, como se explicasse para um amigo.
- Utilize as transações reais do FinTrack quando forem relevantes.
- Se não souber algo, admita que não possui a informação.
"""

    # Adiciona o perfil financeiro ao contexto.
    contexto = "=== PERFIL DO CLIENTE ===\n"
    contexto += json.dumps(
        perfil,
        ensure_ascii=False,
        indent=2
    )

    # Adiciona os produtos financeiros.
    contexto += (
        "\n\n=== PRODUTOS FINANCEIROS "
        "(PARA ENSINO) ===\n"
    )

    contexto += json.dumps(
        produtos,
        ensure_ascii=False,
        indent=2
    )

    # Adiciona as transações do FinTrack.
    if not transacoes.empty:
        contexto += (
            "\n\n=== TRANSAÇÕES DO CLIENTE ===\n"
        )

        contexto += transacoes.to_csv(
            index=False
        )

    # Adiciona o histórico de atendimento.
    if not historico.empty:
        contexto += (
            "\n\n=== HISTÓRICO DE ATENDIMENTO ===\n"
        )

        contexto += historico.to_csv(
            index=False
        )

    # Adiciona a pergunta realizada pelo usuário.
    prompt_usuario = f"""
Pergunta do usuário:
\"\"\"{pergunta}\"\"\"
"""

    # Entrega o resultado desta rotina ao chamador.
    return (
        prompt_sistema
        + "\n\n"
        + contexto
        + "\n\n"
        + prompt_usuario
    )


# ============================================================
# 7. FUNÇÃO PARA CHAMAR A GROQ
# ============================================================

# Define a rotina chamar_groq e os argumentos necessários ao seu uso.
def chamar_groq(
    client,
    prompt
):

    # Agrupa operações que podem falhar para tratar seus erros.
    try:
        # Envia o prompt para o modelo da Groq.
        chat_completion = client.chat.completions.create(
            model=MODEL_NAME,
            messages=[
                {
                    "role": "system",
                    "content": prompt
                }
            ],
            temperature=0.5,
            max_tokens=2048
        )

        # Recupera o conteúdo da resposta.
        texto = (
            chat_completion
            .choices[0]
            .message
            .content
        )

        # Remove apenas possíveis saudações artificiais.
        texto = re.sub(
            r"^(Olá|Oi|Saudações)[^a-zA-ZÀ-ÿ]*",
            "",
            texto,
            flags=re.IGNORECASE
        )

        # Entrega o resultado desta rotina ao chamador.
        return texto.strip()

    except Exception as erro:
        # Prepara mensagem_erro para a próxima etapa desta rotina.
        mensagem_erro = str(erro)

        # Executa este bloco somente quando a condição é atendida.
        if "401" in mensagem_erro:
            # Entrega o resultado desta rotina ao chamador.
            return (
                "A chave da API Groq não foi aceita. "
                "Verifique a chave informada."
            )

        # Executa este bloco somente quando a condição é atendida.
        if "429" in mensagem_erro:
            # Entrega o resultado desta rotina ao chamador.
            return (
                "O limite de uso da API Groq foi atingido. "
                "Tente novamente em alguns instantes."
            )

        # Executa este bloco somente quando a condição é atendida.
        if (
            "503" in mensagem_erro
            or "UNAVAILABLE" in mensagem_erro
        ):
            # Entrega o resultado desta rotina ao chamador.
            return (
                "O Edu está temporariamente indisponível. "
                "Tente novamente em alguns instantes."
            )

        # Entrega o resultado desta rotina ao chamador.
        return (
            "Não consegui consultar a IA agora. Erro: "
            + mensagem_erro
        )


# ============================================================
# 8. INTERFACE PRINCIPAL
# ============================================================

# Define a rotina main e os argumentos necessários ao seu uso.
def main():

    # Título principal do Edu.
    st.title(
        "💸 Edu - Educador Financeiro Inteligente"
    )

    # Texto de apresentação.
    st.write(
        "Agente de IA que ensina finanças pessoais "
        "com base nos dados cadastrados no FinTrack."
    )

    # Campo da pergunta.
    pergunta = st.text_area(
        "Digite sua pergunta:",
        height=100
    )

    # Botão que inicia a consulta.
    if st.button("Perguntar para o Edu"):

        # Verifica se o usuário forneceu a chave da Groq.
        if not groq_api_key:
            # Apresenta ou atualiza este elemento na interface Streamlit.
            st.warning(
                "Insira sua API Key da Groq na barra lateral."
            )
            # Entrega o resultado desta rotina ao chamador.
            return

        # Verifica se foi feita uma pergunta.
        if not pergunta.strip():
            # Apresenta ou atualiza este elemento na interface Streamlit.
            st.warning(
                "Digite uma pergunta."
            )
            # Entrega o resultado desta rotina ao chamador.
            return

        # Agrupa operações que podem falhar para tratar seus erros.
        try:
            # Cria o cliente com a chave fornecida pelo usuário.
            client = Groq(
                api_key=groq_api_key
            )

        except Exception as erro:
            # Apresenta ou atualiza este elemento na interface Streamlit.
            st.error(
                "Não foi possível inicializar a Groq: "
                f"{erro}"
            )
            # Entrega o resultado desta rotina ao chamador.
            return

        # Palavras utilizadas para detectar consulta de cotação.
        palavras_cotacao = [
            "dólar",
            "dolar",
            "dollar",
            "usd",
            "euro",
            "eur",
            "bitcoin",
            "btc"
        ]

        # Prepara cotacao_detectada para a próxima etapa desta rotina.
        cotacao_detectada = None

        # Procura uma moeda dentro da pergunta.
        for palavra in palavras_cotacao:
            # Executa este bloco somente quando a condição é atendida.
            if palavra in pergunta.lower():
                # Prepara cotacao_detectada para a próxima etapa desta rotina.
                cotacao_detectada = palavra
                break

        # Prepara resposta_cotacao para a próxima etapa desta rotina.
        resposta_cotacao = None

        # Consulta a cotação caso uma moeda seja identificada.
        if cotacao_detectada:
            # Prepara resposta_cotacao para a próxima etapa desta rotina.
            resposta_cotacao = buscar_cotacao(
                cotacao_detectada
            )

        # Carrega novamente os dados do FinTrack.
        perfil, transacoes, historico, produtos = carregar_bases()

        # Monta o prompt com os dados financeiros.
        prompt = montar_prompt(
            pergunta,
            perfil,
            transacoes,
            historico,
            produtos
        )

        # Mostra um indicador de processamento.
        with st.spinner(
            "Analisando sua pergunta..."
        ):
            # Prepara resposta_edu para a próxima etapa desta rotina.
            resposta_edu = chamar_groq(
                client,
                prompt
            )

        # Apresenta ou atualiza este elemento na interface Streamlit.
        st.markdown(
            "### Resposta do Edu:"
        )

        # Exibe a cotação quando disponível.
        if resposta_cotacao:
            # Apresenta ou atualiza este elemento na interface Streamlit.
            st.success(
                f"💱 Cotação atual "
                f"({cotacao_detectada.upper()}): "
                f"**R$ {resposta_cotacao:.2f}**"
            )

        # Executa este bloco somente quando a condição é atendida.
        elif cotacao_detectada:
            # Apresenta ou atualiza este elemento na interface Streamlit.
            st.error(
                "Não consegui obter a cotação agora. "
                "Tente novamente em instantes."
            )

        # Exibe a resposta da inteligência artificial.
        st.write(
            resposta_edu
        )


# Executa a função principal quando o arquivo for iniciado.
if __name__ == "__main__":
    # Executa esta operação como parte do fluxo atual.
    main()
