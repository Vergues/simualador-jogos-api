const listaHoje =
    document.getElementById(
        "listaHoje"
    );

const listaProximos =
    document.getElementById(
        "listaProximos"
    );


async function carregarHome() {

    await carregarJogosHoje();

    await carregarProximosJogos();
}


async function carregarJogosHoje() {

    try {

        const resposta =
            await fetch(
                "/api/brasileirao/hoje"
            );

        const jogos =
            await resposta.json();

        renderizarJogos(
            listaHoje,
            jogos,
            "Nenhum jogo marcado para hoje."
        );

    } catch (erro) {

        console.error(
            "Erro ao carregar jogos de hoje:",
            erro
        );

        listaHoje.innerHTML =
            `
            <div class="vazio">
                Erro ao carregar os jogos de hoje.
            </div>
            `;
    }
}


async function carregarProximosJogos() {

    try {

        const resposta =
            await fetch(
                "/api/brasileirao/proximos?limite=10"
            );

        const jogos =
            await resposta.json();

        renderizarJogos(
            listaProximos,
            jogos,
            "Nenhum próximo jogo encontrado."
        );

    } catch (erro) {

        console.error(
            "Erro ao carregar próximos jogos:",
            erro
        );

        listaProximos.innerHTML =
            `
            <div class="vazio">
                Erro ao carregar os próximos jogos.
            </div>
            `;
    }
}


function renderizarJogos(
    container,
    jogos,
    mensagemVazio
) {

    container.innerHTML = "";

    container.classList.add(
        "lista-jogos"
    );

    if (
        !jogos ||
        jogos.length === 0
    ) {

        container.innerHTML =
            `
            <div class="vazio">
                ${mensagemVazio}
            </div>
            `;

        return;
    }

    jogos.forEach(jogo => {

        const card =
            criarCardJogo(jogo);

        container.appendChild(
            card
        );
    });
}


function criarCardJogo(jogo) {

    const card =
        document.createElement(
            "div"
        );

    card.classList.add(
        "jogo-card"
    );

    if (
        jogo.status === "AO_VIVO"
    ) {

        card.classList.add(
            "ao-vivo"
        );
    }

    card.style.cursor =
        "pointer";

    card.addEventListener(
        "click",
        () => {

            window.location.href =
                `/partida.html?id=${jogo.id}`;
        }
    );


    const data =
        new Date(
            jogo.dataHora
        );


    const dataFormatada =
        data.toLocaleDateString(
            "pt-BR"
        );


    const horario =
        data.toLocaleTimeString(
            "pt-BR",
            {
                hour: "2-digit",
                minute: "2-digit"
            }
        );


    const statusTexto =
        formatarStatus(jogo);


    const classeStatus =
        obterClasseStatus(
            jogo.status
        );


    const placar =
        montarPlacar(jogo);


    card.innerHTML =
        `
        <div class="jogo-topo">

            <span class="jogo-campeonato">
                ${jogo.campeonato}
                • Rodada ${jogo.rodada}
            </span>

            <span class="status ${classeStatus}">
                ${statusTexto}
            </span>

        </div>


        <div class="jogo-confronto">

            <div class="time-casa">

                <span class="nome-time">
                    ${jogo.timeCasa}
                </span>

            </div>


            <div class="placar">
                ${placar}
            </div>


            <div class="time-fora">

                <span class="nome-time">
                    ${jogo.timeFora}
                </span>

            </div>

        </div>


        <div class="jogo-rodape">

            <span>
                ${dataFormatada}
            </span>

            <span>
                ${horario}
            </span>

        </div>
        `;


    return card;
}


function montarPlacar(jogo) {

    if (
        jogo.status === "AGENDADO"
    ) {

        return `
            <span class="x">
                x
            </span>
        `;
    }

    return `
        <span class="numero">
            ${jogo.golsCasa}
        </span>

        <span class="x">
            x
        </span>

        <span class="numero">
            ${jogo.golsFora}
        </span>
    `;
}


function formatarStatus(jogo) {

    if (
        jogo.status === "AO_VIVO"
    ) {

        return `AO VIVO • ${jogo.minutoAtual}'`;
    }

    if (
        jogo.status === "INTERVALO"
    ) {

        return "INTERVALO";
    }

    if (
        jogo.status === "ENCERRADO"
    ) {

        return "ENCERRADO";
    }

    return "AGENDADO";
}


function obterClasseStatus(status) {

    if (
        status === "AO_VIVO"
    ) {

        return "status-ao-vivo";
    }

    if (
        status === "ENCERRADO"
    ) {

        return "status-encerrado";
    }

    if (
        status === "INTERVALO"
    ) {

        return "status-intervalo";
    }

    return "status-agendado";
}


carregarHome();


setInterval(
    carregarHome,
    5000
);