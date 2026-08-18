const selectCasa = document.getElementById("timeCasa");
const selectFora = document.getElementById("timeFora");

const botaoSimular = document.getElementById("botaoSimular");

const resultado = document.getElementById("resultado");

const nomeCasa = document.getElementById("nomeCasa");
const nomeFora = document.getElementById("nomeFora");

const siglaCasa = document.getElementById("siglaCasa");
const siglaFora = document.getElementById("siglaFora");

const golsCasa = document.getElementById("golsCasa");
const golsFora = document.getElementById("golsFora");

const eventosContainer = document.getElementById("eventos");


async function carregarTimes() {

    try {

        const resposta = await fetch("/api/times");

        const times = await resposta.json();

        selectCasa.innerHTML = "";
        selectFora.innerHTML = "";

        times.forEach(time => {

            const optionCasa = document.createElement("option");

            optionCasa.value = time.sigla;
            optionCasa.textContent = time.nome;

            selectCasa.appendChild(optionCasa);


            const optionFora = document.createElement("option");

            optionFora.value = time.sigla;
            optionFora.textContent = time.nome;

            selectFora.appendChild(optionFora);

        });

        if (times.length > 1) {
            selectFora.selectedIndex = 1;
        }

    } catch (erro) {

        console.error(
            "Erro ao carregar times:",
            erro
        );
    }
}


async function simularPartida() {

    const casa = selectCasa.value;
    const fora = selectFora.value;

    if (casa === fora) {

        alert(
            "Escolha dois times diferentes."
        );

        return;
    }

    botaoSimular.disabled = true;

    botaoSimular.textContent =
        "Simulando...";

    try {

        const resposta = await fetch(
            `/api/jogos/simular?casa=${casa}&fora=${fora}`
        );

        if (!resposta.ok) {

            throw new Error(
                "Erro ao simular partida."
            );
        }

        const jogo = await resposta.json();

        mostrarResultado(jogo);

    } catch (erro) {

        console.error(erro);

        alert(
            "Não foi possível simular a partida."
        );

    } finally {

        botaoSimular.disabled = false;

        botaoSimular.textContent =
            "Simular Partida";
    }
}


function mostrarResultado(jogo) {

    nomeCasa.textContent =
        jogo.timeCasa;

    nomeFora.textContent =
        jogo.timeFora;

    siglaCasa.textContent =
        jogo.siglaCasa;

    siglaFora.textContent =
        jogo.siglaFora;

    golsCasa.textContent =
        jogo.golsCasa;

    golsFora.textContent =
        jogo.golsFora;

    eventosContainer.innerHTML = "";

    if (
        !jogo.eventos ||
        jogo.eventos.length === 0
    ) {

        eventosContainer.innerHTML =
            "<p>Nenhum evento registrado.</p>";

    } else {

        jogo.eventos.forEach(evento => {

            const div =
                document.createElement("div");

            div.classList.add("evento");

            let icone = "⚽";

            if (
                evento.tipo ===
                "CARTAO_AMARELO"
            ) {

                icone = "🟨";

                div.classList.add(
                    "evento-cartao"
                );

            } else if (
                evento.tipo === "FALTA"
            ) {

                icone = "❌";

                div.classList.add(
                    "evento-falta"
                );

            } else if (
                evento.tipo === "GOL"
            ) {

                div.classList.add(
                    "evento-gol"
                );
            }

            div.innerHTML = `
                <span class="minuto">
                    ${evento.minuto}'
                </span>

                <span class="tipo-evento">
                    ${icone}
                </span>

                <span class="descricao">
                    ${evento.descricao}
                </span>
            `;

            eventosContainer.appendChild(div);
        });
    }

    resultado.classList.remove(
        "escondido"
    );
}


botaoSimular.addEventListener(
    "click",
    simularPartida
);


carregarTimes();