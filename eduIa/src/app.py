# Importa funções para trabalhar com arquivos e caminhos do sistema.
import os

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

st.set_page_config(
    page_title="Edu - Educador Financeiro Inteligente",
    page_icon="💸",
    layout="wide",
    initial_sidebar_state="expanded"
)


# ============================================================
# 2. MODELO UTILIZADO NA GROQ
# ============================================================

MODEL_NAME = "openai/gpt-oss-20b"


# ============================================================
# 3. BARRA LATERAL
# ============================================================

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

    st.markdown("---")

    st.markdown(
        f"**Modelo utilizado:** `{MODEL_NAME}`"
    )

    st.markdown("---")

    st.caption(
        "A chave é utilizada somente durante a execução da aplicação "
        "e não é salva pelo Edu."
    )

    st.caption(
        "A IA pode cometer erros. Verifique informações importantes."
    )


# ============================================================
# 4. FUNÇÃO PARA CARREGAR AS BASES DE CONHECIMENTO
# ============================================================

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

    # Caminho do arquivo de transações gerado pelo FinTrack Java.
    transacoes_path = os.path.abspath(
        os.path.join(
            os.path.dirname(__file__),
            "..",
            "..",
            "FinTrack",
            "dados",
            "transacoes.csv"
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
    transacoes = pd.DataFrame()
    historico = pd.DataFrame()
    produtos = {}

    # Carrega o perfil financeiro.
    if os.path.exists(perfil_path):
        with open(
            perfil_path,
            "r",
            encoding="utf-8"
        ) as arquivo:
            perfil = json.load(arquivo)

    # Carrega as transações do FinTrack.
    if os.path.exists(transacoes_path):
        transacoes = pd.read_csv(
            transacoes_path,
            sep=";",
            names=[
                "id",
                "descricao",
                "valor",
                "tipo",
                "categoria",
                "data",
                "mensal",
                "dia"
            ],
            encoding="utf-8"
        )

    else:
        st.warning(
            "Arquivo de transações do FinTrack não encontrado em: "
            f"{transacoes_path}"
        )

    # Carrega o histórico de atendimento.
    if os.path.exists(historico_path):
        historico = pd.read_csv(
            historico_path,
            sep=","
        )

    # Carrega a lista de produtos financeiros.
    if os.path.exists(produtos_path):
        with open(
            produtos_path,
            "r",
            encoding="utf-8"
        ) as arquivo:
            produtos = json.load(arquivo)

    return perfil, transacoes, historico, produtos


# ============================================================
# 5. FUNÇÃO PARA BUSCAR COTAÇÕES REAIS
# ============================================================

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

    moeda = moeda.lower()

    if moeda not in urls:
        return None

    try:
        response = requests.get(
            urls[moeda],
            timeout=5
        )

        data = response.json()

        if "USDBRL" in data:
            return float(data["USDBRL"]["bid"])

        if "EURBRL" in data:
            return float(data["EURBRL"]["bid"])

        if "BTCBRL" in data:
            return float(data["BTCBRL"]["bid"])

        return None

    except Exception:
        return None


# ============================================================
# 6. FUNÇÃO PARA MONTAR O PROMPT COM CONTEXTO
# ============================================================

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

def chamar_groq(
    client,
    prompt
):

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

        return texto.strip()

    except Exception as erro:
        mensagem_erro = str(erro)

        if "401" in mensagem_erro:
            return (
                "A chave da API Groq não foi aceita. "
                "Verifique a chave informada."
            )

        if "429" in mensagem_erro:
            return (
                "O limite de uso da API Groq foi atingido. "
                "Tente novamente em alguns instantes."
            )

        if (
            "503" in mensagem_erro
            or "UNAVAILABLE" in mensagem_erro
        ):
            return (
                "O Edu está temporariamente indisponível. "
                "Tente novamente em alguns instantes."
            )

        return (
            "Não consegui consultar a IA agora. Erro: "
            + mensagem_erro
        )


# ============================================================
# 8. INTERFACE PRINCIPAL
# ============================================================

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
            st.warning(
                "Insira sua API Key da Groq na barra lateral."
            )
            return

        # Verifica se foi feita uma pergunta.
        if not pergunta.strip():
            st.warning(
                "Digite uma pergunta."
            )
            return

        try:
            # Cria o cliente com a chave fornecida pelo usuário.
            client = Groq(
                api_key=groq_api_key
            )

        except Exception as erro:
            st.error(
                "Não foi possível inicializar a Groq: "
                f"{erro}"
            )
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

        cotacao_detectada = None

        # Procura uma moeda dentro da pergunta.
        for palavra in palavras_cotacao:
            if palavra in pergunta.lower():
                cotacao_detectada = palavra
                break

        resposta_cotacao = None

        # Consulta a cotação caso uma moeda seja identificada.
        if cotacao_detectada:
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
            resposta_edu = chamar_groq(
                client,
                prompt
            )

        st.markdown(
            "### Resposta do Edu:"
        )

        # Exibe a cotação quando disponível.
        if resposta_cotacao:
            st.success(
                f"💱 Cotação atual "
                f"({cotacao_detectada.upper()}): "
                f"**R$ {resposta_cotacao:.2f}**"
            )

        elif cotacao_detectada:
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
    main()