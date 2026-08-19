const parametros =
    new URLSearchParams(
        window.location.search
    );

const jogoId =
    parametros.get("id");


if (!jogoId) {

    alert(
        "ID da partida não informado."
    );

    window.location.href = "/";
}


async function carregarPartida() {

    try {

        const resposta =
            await fetch(
                `/api/brasileirao/jogo/${jogoId}`
            );

        if (!resposta.ok) {

            throw new Error(
                "Erro ao carregar partida."
            );
        }

        const jogo =
            await resposta.json();

        renderizarPartida(
            jogo
        );

    } catch (erro) {

        console.error(
            "Erro ao carregar partida:",
            erro
        );
    }
}


function renderizarPartida(jogo) {

    document.getElementById(
        "campeonato"
    ).textContent =
        `${jogo.campeonato} • Rodada ${jogo.rodada}`;


    document.getElementById(
        "timeCasa"
    ).textContent =
        jogo.timeCasa;

    document.getElementById(
        "siglaCasa"
    ).textContent =
        jogo.siglaCasa;


    document.getElementById(
        "timeFora"
    ).textContent =
        jogo.timeFora;

    document.getElementById(
        "siglaFora"
    ).textContent =
        jogo.siglaFora;


    document.getElementById(
        "golsCasa"
    ).textContent =
        jogo.golsCasa;

    document.getElementById(
        "golsFora"
    ).textContent =
        jogo.golsFora;


    document.getElementById(
        "status"
    ).textContent =
        formatarStatus(
            jogo.status
        );


    document.getElementById(
        "minuto"
    ).textContent =
        jogo.status === "AO_VIVO"
            ? `${jogo.minutoAtual}'`
            : "";


    document.getElementById(
        "nomeEstatisticaCasa"
    ).textContent =
        jogo.timeCasa;


    document.getElementById(
        "nomeEstatisticaFora"
    ).textContent =
        jogo.timeFora;


    renderizarEstatisticas(
        jogo.estatisticas
    );


    renderizarEventos(
        jogo.eventos || []
    );
}


function renderizarEstatisticas(
    estatisticas
) {

    const container =
        document.getElementById(
            "listaEstatisticas"
        );

    container.innerHTML = "";


    if (!estatisticas) {

        container.innerHTML =
            `
            <div class="vazio">
                Estatísticas indisponíveis.
            </div>
            `;

        return;
    }


    const dados = [

        {
            nome: "Posse de bola",
            casa: estatisticas.posseCasa,
            fora: estatisticas.posseFora,
            sufixo: "%"
        },

        {
            nome: "Finalizações",
            casa: estatisticas.finalizacoesCasa,
            fora: estatisticas.finalizacoesFora
        },

        {
            nome: "Finalizações no gol",
            casa: estatisticas.finalizacoesGolCasa,
            fora: estatisticas.finalizacoesGolFora
        },

        {
            nome: "Escanteios",
            casa: estatisticas.escanteiosCasa,
            fora: estatisticas.escanteiosFora
        },

        {
            nome: "Faltas",
            casa: estatisticas.faltasCasa,
            fora: estatisticas.faltasFora
        },

        {
            nome: "Cartões amarelos",
            casa: estatisticas.amarelosCasa,
            fora: estatisticas.amarelosFora
        },

        {
            nome: "Cartões vermelhos",
            casa: estatisticas.vermelhosCasa,
            fora: estatisticas.vermelhosFora
        },

        {
            nome: "Impedimentos",
            casa: estatisticas.impedimentosCasa,
            fora: estatisticas.impedimentosFora
        }
    ];


    dados.forEach(item => {

        criarEstatistica(
            container,
            item.nome,
            item.casa,
            item.fora,
            item.sufixo || ""
        );
    });
}


function criarEstatistica(
    container,
    nome,
    valorCasa,
    valorFora,
    sufixo
) {

    const total =
        valorCasa + valorFora;


    let percentualCasa = 50;
    let percentualFora = 50;


    if (total > 0) {

        percentualCasa =
            (valorCasa / total) * 100;

        percentualFora =
            100 - percentualCasa;
    }


    const div =
        document.createElement(
            "div"
        );

    div.classList.add(
        "estatistica-item"
    );


    div.innerHTML =
        `
        <div class="estatistica-valores">

            <div class="valor-casa">
                ${valorCasa}${sufixo}
            </div>

            <div class="nome-estatistica">
                ${nome}
            </div>

            <div class="valor-fora">
                ${valorFora}${sufixo}
            </div>

        </div>

        <div class="barra-estatistica">

            <div
                class="barra-casa"
                style="width: ${percentualCasa}%"
            >
            </div>

            <div
                class="barra-fora"
                style="width: ${percentualFora}%"
            >
            </div>

        </div>
        `;


    container.appendChild(
        div
    );
}


function renderizarEventos(
    eventos
) {

    const container =
        document.getElementById(
            "eventos"
        );

    container.innerHTML = "";


    if (eventos.length === 0) {

        container.innerHTML =
            `
            <div class="vazio">
                Nenhum evento registrado.
            </div>
            `;

        return;
    }


    /*
     * Eventos mais recentes aparecem no topo.
     */
    const ordenados =
        [...eventos].sort(
            (a, b) =>
                b.minuto - a.minuto
        );


    for (const evento of ordenados) {

        const div =
            document.createElement(
                "div"
            );

        div.classList.add(
            "evento"
        );


        const classe =
            obterClasseEvento(
                evento.tipo
            );


        if (classe) {

            div.classList.add(
                classe
            );
        }


        div.innerHTML =
            `
            <div class="evento-minuto">
                ${evento.minuto}'
            </div>

            <div class="evento-icone">
                ${obterIcone(evento.tipo)}
            </div>

            <div class="evento-descricao">
                ${evento.descricao}
            </div>
            `;


        container.appendChild(
            div
        );
    }
}


/*
 * =========================================================
 * ÍCONES DOS EVENTOS
 * =========================================================
 */
function obterIcone(tipo) {

    switch (tipo) {

        case "GOL":
            return "⚽";

        case "CARTAO_AMARELO":
            return "🟨";

        case "CARTAO_VERMELHO":
            return "🟥";

        case "FALTA":
            return "⚠️";

        case "FINALIZACAO":
            return "🎯";

        case "DEFESA":
            return "🧤";

        case "ESCANTEIO":
            return "🚩";

        case "IMPEDIMENTO":
            return "🚫";

        case "BRIGA":
            return "🔥";

        case "SUBSTITUICAO":
            return "🔄";

        default:
            return "•";
    }
}


/*
 * =========================================================
 * CLASSES VISUAIS
 * =========================================================
 */
function obterClasseEvento(tipo) {

    switch (tipo) {

        case "GOL":
            return "evento-gol";

        case "BRIGA":
            return "evento-briga";

        case "CARTAO_AMARELO":
            return "evento-amarelo";

        case "CARTAO_VERMELHO":
            return "evento-vermelho";

        case "DEFESA":
            return "evento-defesa";

        case "FINALIZACAO":
            return "evento-finalizacao";

        case "SUBSTITUICAO":
            return "evento-substituicao";

        default:
            return "";
    }
}


function formatarStatus(status) {

    switch (status) {

        case "AO_VIVO":
            return "AO VIVO";

        case "INTERVALO":
            return "INTERVALO";

        case "ENCERRADO":
            return "ENCERRADO";

        default:
            return "AGENDADO";
    }
}


carregarPartida();


setInterval(
    carregarPartida,
    5000
);