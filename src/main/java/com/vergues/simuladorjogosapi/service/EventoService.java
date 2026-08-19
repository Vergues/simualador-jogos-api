package com.vergues.simuladorjogosapi.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.vergues.simuladorjogosapi.model.Escalacao;
import com.vergues.simuladorjogosapi.model.EstatisticasJogo;
import com.vergues.simuladorjogosapi.model.Evento;
import com.vergues.simuladorjogosapi.model.Jogador;
import com.vergues.simuladorjogosapi.model.Jogo;
import com.vergues.simuladorjogosapi.model.Time;

@Service
public class EventoService {

    /*
     * RNG utilizado para escolher:
     *
     * - time do evento;
     * - jogador;
     * - descrição;
     * - cartão;
     * - substituição;
     * - consequência de briga.
     */
    private final Random random =
            new Random();


    /*
     * =========================================================
     * GOL
     * =========================================================
     */

    public void gerarGol(
            Jogo jogo,
            int minuto) {

        Time time =
                escolherTimeOfensivo(
                        jogo
                );


        List<Jogador> emCampo =
                buscarJogadoresEmCampo(
                        jogo,
                        time
                );


        Jogador jogador =
                escolherFinalizador(
                        emCampo
                );


        /*
         * Atualiza o placar.
         */
        if (time == jogo.getTimeCasa()) {

            jogo.setGolsCasa(
                    jogo.getGolsCasa() + 1
            );

        } else {

            jogo.setGolsFora(
                    jogo.getGolsFora() + 1
            );
        }


        /*
         * Gol também é chute no gol.
         */
        registrarFinalizacaoNoGol(
                jogo,
                time
        );


        String[] frases = {

                "Gol de "
                        + jogador.getNome()
                        + "! A bola morreu no fundo da rede!",

                jogador.getNome()
                        + " aparece na hora certa e marca!",

                "É GOL! "
                        + jogador.getNome()
                        + " coloca o "
                        + time.getNome()
                        + " no placar!",

                jogador.getNome()
                        + " finaliza com categoria e não dá chance para o goleiro.",

                jogador.getNome()
                        + " manda para a rede e explode a torcida!"
        };


        jogo.adicionarEvento(
                new Evento(
                        minuto,
                        "GOL",
                        time.getNome(),
                        jogador.getNome(),
                        escolherFrase(frases)
                )
        );
    }


    /*
     * =========================================================
     * FINALIZAÇÃO PARA FORA / BLOQUEADA
     * =========================================================
     */

    public void gerarFinalizacao(
            Jogo jogo,
            int minuto) {

        Time time =
                escolherTimeOfensivo(
                        jogo
                );


        Jogador jogador =
                escolherFinalizador(
                        buscarJogadoresEmCampo(
                                jogo,
                                time
                        )
                );


        registrarFinalizacao(
                jogo,
                time
        );


        String[] frases = {

                jogador.getNome()
                        + " arrisca de fora da área, mas manda para fora.",

                jogador.getNome()
                        + " recebe em boa posição e finaliza por cima.",

                "Boa chegada do "
                        + time.getNome()
                        + "! "
                        + jogador.getNome()
                        + " bate cruzado e a bola passa perto.",

                jogador.getNome()
                        + " tenta a finalização, mas a defesa bloqueia.",

                jogador.getNome()
                        + " domina e bate de primeira. Passou muito perto!"
        };


        jogo.adicionarEvento(
                new Evento(
                        minuto,
                        "FINALIZACAO",
                        time.getNome(),
                        jogador.getNome(),
                        escolherFrase(frases)
                )
        );
    }


    /*
     * =========================================================
     * DEFESA
     * =========================================================
     */

    public void gerarDefesa(
            Jogo jogo,
            int minuto) {

        Time ataque =
                escolherTimeOfensivo(
                        jogo
                );


        Time defesa =
                ataque == jogo.getTimeCasa()
                        ? jogo.getTimeFora()
                        : jogo.getTimeCasa();


        Jogador atacante =
                escolherFinalizador(
                        buscarJogadoresEmCampo(
                                jogo,
                                ataque
                        )
                );


        Jogador goleiro =
                buscarGoleiroEmCampo(
                        jogo,
                        defesa
                );


        registrarFinalizacaoNoGol(
                jogo,
                ataque
        );


        String[] frases = {

                atacante.getNome()
                        + " finaliza e "
                        + goleiro.getNome()
                        + " faz boa defesa!",

                "Grande defesa de "
                        + goleiro.getNome()
                        + " após chute de "
                        + atacante.getNome()
                        + "!",

                goleiro.getNome()
                        + " salva o "
                        + defesa.getNome()
                        + " em ótima chance de "
                        + atacante.getNome()
                        + ".",

                atacante.getNome()
                        + " bate firme, mas "
                        + goleiro.getNome()
                        + " segura."
        };


        jogo.adicionarEvento(
                new Evento(
                        minuto,
                        "DEFESA",
                        defesa.getNome(),
                        goleiro.getNome(),
                        escolherFrase(frases)
                )
        );
    }


    /*
     * =========================================================
     * FALTA
     * =========================================================
     *
     * TODA falta conta nas estatísticas.
     *
     * Porém apenas algumas aparecem na timeline.
     */
    public void gerarFalta(
            Jogo jogo,
            int minuto,
            boolean mostrarNaTimeline) {

        Time time =
                escolherTimeDisciplinar(
                        jogo
                );


        Jogador jogador =
                escolherJogadorParaFalta(
                        buscarJogadoresEmCampo(
                                jogo,
                                time
                        )
                );


        registrarFalta(
                jogo,
                time
        );


        /*
         * A maioria das faltas pode ficar
         * apenas nas estatísticas.
         */
        if (!mostrarNaTimeline) {
            return;
        }


        String[] frases = {

                jogador.getNome()
                        + " chega atrasado e derruba o adversário.",

                jogador.getNome()
                        + " mata o contra-ataque com uma falta tática.",

                jogador.getNome()
                        + " exagera na carga e o árbitro marca falta.",

                "Entrada forte de "
                        + jogador.getNome()
                        + ". O juiz paralisa o jogo.",

                jogador.getNome()
                        + " perde a dividida e acaba cometendo a falta.",

                jogador.getNome()
                        + " segura o adversário e interrompe a jogada."
        };


        jogo.adicionarEvento(
                new Evento(
                        minuto,
                        "FALTA",
                        time.getNome(),
                        jogador.getNome(),
                        escolherFrase(frases)
                )
        );
    }


    /*
     * =========================================================
     * CARTÃO
     * =========================================================
     *
     * Aqui nasce a lógica:
     *
     * primeiro amarelo -> amarelo
     *
     * segundo amarelo -> expulsão
     */
    public void gerarCartaoAmarelo(
            Jogo jogo,
            int minuto) {

        Time time =
                escolherTimeDisciplinar(
                        jogo
                );


        Jogador jogador =
                escolherJogadorParaCartao(
                        buscarJogadoresEmCampo(
                                jogo,
                                time
                        )
                );


        aplicarAmarelo(
                jogo,
                time,
                jogador,
                minuto,
                false
        );
    }


    /*
     * Aplica um amarelo em um jogador específico.
     *
     * Também é usado nas confusões.
     */
    private void aplicarAmarelo(
            Jogo jogo,
            Time time,
            Jogador jogador,
            int minuto,
            boolean aposBriga) {

        registrarAmarelo(
                jogo,
                time
        );


        /*
         * Registra o amarelo individualmente.
         */
        jogo.adicionarAmarelo(
                jogador
        );


        int quantidade =
                jogo.getAmarelosDoJogador(
                        jogador
                );


        /*
         * =====================================================
         * PRIMEIRO AMARELO
         * =====================================================
         */
        if (quantidade == 1) {

            String descricao;


            if (aposBriga) {

                descricao =
                        "Depois da confusão, "
                        + jogador.getNome()
                        + " recebe cartão amarelo.";

            } else {

                String[] frases = {

                        jogador.getNome()
                                + " recebe amarelo por parar um contra-ataque.",

                        jogador.getNome()
                                + " chega forte demais e acaba advertido.",

                        "O árbitro não gostou da reclamação de "
                                + jogador.getNome()
                                + ". Cartão amarelo.",

                        "Entrada dura de "
                                + jogador.getNome()
                                + ". O juiz não deixa passar.",

                        jogador.getNome()
                                + " faz falta tática e recebe amarelo."
                };


                descricao =
                        escolherFrase(
                                frases
                        );
            }


            jogo.adicionarEvento(
                    new Evento(
                            minuto,
                            "CARTAO_AMARELO",
                            time.getNome(),
                            jogador.getNome(),
                            descricao
                    )
            );


            return;
        }


        /*
         * =====================================================
         * SEGUNDO AMARELO
         * =====================================================
         *
         * O jogador é expulso e não poderá
         * participar de nenhum outro evento.
         */
        jogo.expulsarJogador(
                jogador
        );


        registrarVermelho(
                jogo,
                time
        );


        jogo.adicionarEvento(
                new Evento(
                        minuto,
                        "CARTAO_VERMELHO",
                        time.getNome(),
                        jogador.getNome(),
                        "Segundo amarelo! "
                                + jogador.getNome()
                                + " está expulso e deixa sua equipe com um jogador a menos."
                )
        );
    }


    /*
     * =========================================================
     * ESCANTEIO
     * =========================================================
     */

    public void gerarEscanteio(
            Jogo jogo,
            int minuto) {

        Time time =
                escolherTimeOfensivo(
                        jogo
                );


        registrarEscanteio(
                jogo,
                time
        );


        String[] frases = {

                "Escanteio para o "
                        + time.getNome()
                        + ".",

                "A defesa corta para a linha de fundo. Escanteio para o "
                        + time.getNome()
                        + ".",

                time.getNome()
                        + " pressiona e ganha cobrança de escanteio."
        };


        jogo.adicionarEvento(
                new Evento(
                        minuto,
                        "ESCANTEIO",
                        time.getNome(),
                        "",
                        escolherFrase(frases)
                )
        );
    }


    /*
     * =========================================================
     * IMPEDIMENTO
     * =========================================================
     */

    public void gerarImpedimento(
            Jogo jogo,
            int minuto) {

        Time time =
                escolherTimeOfensivo(
                        jogo
                );


        /*
         * Só utilizamos jogadores realmente ofensivos.
         */
        Jogador jogador =
                escolherJogadorOfensivo(
                        buscarJogadoresEmCampo(
                                jogo,
                                time
                        )
                );


        registrarImpedimento(
                jogo,
                time
        );


        jogo.adicionarEvento(
                new Evento(
                        minuto,
                        "IMPEDIMENTO",
                        time.getNome(),
                        jogador.getNome(),
                        jogador.getNome()
                                + " é flagrado em posição de impedimento."
                )
        );
    }


    /*
     * =========================================================
     * SUBSTITUIÇÃO
     * =========================================================
     */

    public void gerarSubstituicao(
            Jogo jogo,
            Time time,
            int minuto) {

        /*
         * Limitamos a 5 substituições por equipe.
         */
        if (
                jogo.getSubstituicoesDoTime(
                        time
                ) >= 5
        ) {

            return;
        }


        Escalacao base =
                gerarEscalacaoBase(
                        time
                );


        List<Jogador> campo =
                buscarJogadoresEmCampo(
                        jogo,
                        time
                );


        /*
         * Reservas disponíveis.
         */
        List<Jogador> reservas =
                base.getReservas()
                        .stream()
                        .filter(jogador ->
                                !jogo.jogadorEntrou(
                                        jogador
                                )
                        )
                        .filter(jogador ->
                                !jogo.jogadorExpulso(
                                        jogador
                                )
                        )
                        .toList();


        if (
                campo.isEmpty()
                || reservas.isEmpty()
        ) {

            return;
        }


        /*
         * Evitamos substituir goleiro
         * aleatoriamente.
         */
        List<Jogador> candidatosSaida =
                campo.stream()
                        .filter(jogador ->
                                !jogador.getPosicao()
                                        .equalsIgnoreCase(
                                                "GOL"
                                        )
                        )
                        .toList();


        if (candidatosSaida.isEmpty()) {
            return;
        }


        Jogador saindo =
                candidatosSaida.get(
                        random.nextInt(
                                candidatosSaida.size()
                        )
                );


        /*
         * Tentamos colocar jogador de posição
         * parecida com o que saiu.
         */
        Jogador entrando =
                escolherReservaCompativel(
                        reservas,
                        saindo
                );


        jogo.registrarSubstituicao(
                saindo,
                entrando
        );


        jogo.adicionarSubstituicao(
                time
        );


        String[] frases = {

                time.getNome()
                        + " mexe no time: sai "
                        + saindo.getNome()
                        + " e entra "
                        + entrando.getNome()
                        + ".",

                "Substituição no "
                        + time.getNome()
                        + ": "
                        + entrando.getNome()
                        + " entra no lugar de "
                        + saindo.getNome()
                        + ".",

                "Hora de sangue novo: "
                        + entrando.getNome()
                        + " entra e "
                        + saindo.getNome()
                        + " deixa o campo."
        };


        jogo.adicionarEvento(
                new Evento(
                        minuto,
                        "SUBSTITUICAO",
                        time.getNome(),
                        entrando.getNome(),
                        escolherFrase(frases)
                )
        );
    }


    /*
     * Busca reserva de função parecida.
     */
    private Jogador escolherReservaCompativel(
            List<Jogador> reservas,
            Jogador saindo) {

        List<Jogador> mesmaPosicao =
                reservas.stream()
                        .filter(jogador ->
                                jogador.getPosicao()
                                        .equalsIgnoreCase(
                                                saindo.getPosicao()
                                        )
                        )
                        .toList();


        if (!mesmaPosicao.isEmpty()) {

            return mesmaPosicao.get(
                    random.nextInt(
                            mesmaPosicao.size()
                    )
            );
        }


        /*
         * Se não houver jogador da mesma posição,
         * entra qualquer reserva disponível.
         */
        return reservas.get(
                random.nextInt(
                        reservas.size()
                )
        );
    }


    /*
     * =========================================================
     * BRIGA
     * =========================================================
     */

    public void gerarBriga(
            Jogo jogo,
            int minuto) {

        String[] frases = {

                "Clima esquentou! Jogadores dos dois times se estranham e começa o empurra-empurra.",

                "Virou reunião de condomínio no meio-campo. Ninguém sabe direito quem começou.",

                "A bola ficou em segundo plano. O clássico resolveu ficar clássico.",

                "Empurra-empurra generalizado. O árbitro claramente reconsiderando suas escolhas profissionais.",

                "O clássico pegou fogo! Tem jogador discutindo, comissão técnica reclamando e o árbitro no meio da confusão.",

                "A paz mundial terá que esperar. Confusão entre os jogadores no meio-campo."
        };


        jogo.adicionarEvento(
                new Evento(
                        minuto,
                        "BRIGA",
                        "Ambos",
                        "",
                        escolherFrase(frases)
                )
        );


        /*
         * 60% só discussão
         * 30% amarelos
         * 8% vermelho
         * 2% dois vermelhos
         */
        int resultado =
                random.nextInt(100);


        if (resultado < 60) {
            return;
        }


        Jogador casa =
                escolherJogadorParaCartao(
                        buscarJogadoresEmCampo(
                                jogo,
                                jogo.getTimeCasa()
                        )
                );


        Jogador fora =
                escolherJogadorParaCartao(
                        buscarJogadoresEmCampo(
                                jogo,
                                jogo.getTimeFora()
                        )
                );


        /*
         * Dois amarelos.
         */
        if (resultado < 90) {

            aplicarAmarelo(
                    jogo,
                    jogo.getTimeCasa(),
                    casa,
                    minuto,
                    true
            );


            aplicarAmarelo(
                    jogo,
                    jogo.getTimeFora(),
                    fora,
                    minuto,
                    true
            );


            return;
        }


        /*
         * Um vermelho direto.
         */
        if (resultado < 98) {

            boolean expulsarCasa =
                    random.nextBoolean();


            Time time =
                    expulsarCasa
                            ? jogo.getTimeCasa()
                            : jogo.getTimeFora();


            Jogador jogador =
                    expulsarCasa
                            ? casa
                            : fora;


            expulsarDiretamente(
                    jogo,
                    time,
                    jogador,
                    minuto
            );


            return;
        }


        /*
         * Caos total.
         */
        expulsarDiretamente(
                jogo,
                jogo.getTimeCasa(),
                casa,
                minuto
        );


        expulsarDiretamente(
                jogo,
                jogo.getTimeFora(),
                fora,
                minuto
        );
    }


    /*
     * Vermelho direto.
     */
    private void expulsarDiretamente(
            Jogo jogo,
            Time time,
            Jogador jogador,
            int minuto) {

        jogo.expulsarJogador(
                jogador
        );


        registrarVermelho(
                jogo,
                time
        );


        jogo.adicionarEvento(
                new Evento(
                        minuto,
                        "CARTAO_VERMELHO",
                        time.getNome(),
                        jogador.getNome(),
                        jogador.getNome()
                                + " passou completamente do ponto e está expulso!"
                )
        );
    }


    /*
     * =========================================================
     * ESCALAÇÃO BASE
     * =========================================================
     */

    private Escalacao gerarEscalacaoBase(
            Time time) {

        List<Jogador> disponiveis =
                new ArrayList<>(
                        time.getJogadores()
                );


        List<Jogador> titulares =
                new ArrayList<>();


        adicionarMelhoresPorPosicao(
                titulares,
                disponiveis,
                "GOL",
                1
        );


        adicionarMelhoresPorPosicao(
                titulares,
                disponiveis,
                "ZAG",
                2
        );


        adicionarMelhoresPorPosicao(
                titulares,
                disponiveis,
                "LD",
                1
        );


        adicionarMelhoresPorPosicao(
                titulares,
                disponiveis,
                "LE",
                1
        );


        adicionarMelhoresDoMeio(
                titulares,
                disponiveis,
                3
        );


        adicionarMelhoresOfensivos(
                titulares,
                disponiveis,
                3
        );


        while (
                titulares.size() < 11
                && !disponiveis.isEmpty()
        ) {

            Jogador melhor =
                    disponiveis.stream()
                            .max(
                                    Comparator.comparingInt(
                                            Jogador::getOverall
                                    )
                            )
                            .orElse(null);


            if (melhor == null) {
                break;
            }


            titulares.add(
                    melhor
            );


            disponiveis.remove(
                    melhor
            );
        }


        return new Escalacao(
                titulares,
                new ArrayList<>(
                        disponiveis
                )
        );
    }


    /*
     * =========================================================
     * JOGADORES ATUALMENTE EM CAMPO
     * =========================================================
     *
     * Aqui está uma das partes mais importantes.
     *
     * Começamos com os 11 titulares.
     *
     * Depois removemos:
     *
     * - expulsos;
     * - substituídos.
     *
     * E adicionamos reservas que entraram.
     */
    private List<Jogador> buscarJogadoresEmCampo(
            Jogo jogo,
            Time time) {

        Escalacao base =
                gerarEscalacaoBase(
                        time
                );


        List<Jogador> campo =
                new ArrayList<>();


        for (
                Jogador jogador :
                base.getTitulares()
        ) {

            if (
                    jogo.jogadorDisponivel(
                            jogador
                    )
            ) {

                campo.add(
                        jogador
                );
            }
        }


        /*
         * Agora adicionamos reservas
         * que entraram durante a partida.
         */
        for (
                Jogador jogador :
                time.getJogadores()
        ) {

            if (
                    jogo.jogadorEntrou(
                            jogador
                    )
                    && !jogo.jogadorExpulso(
                            jogador
                    )
            ) {

                campo.add(
                        jogador
                );
            }
        }


        return campo;
    }


    /*
     * =========================================================
     * GOLEIRO
     * =========================================================
     */

    private Jogador buscarGoleiroEmCampo(
            Jogo jogo,
            Time time) {

        List<Jogador> campo =
                buscarJogadoresEmCampo(
                        jogo,
                        time
                );


        return campo.stream()
                .filter(jogador ->
                        jogador.getPosicao()
                                .equalsIgnoreCase(
                                        "GOL"
                                )
                )
                .findFirst()
                .orElse(
                        campo.get(0)
                );
    }


    /*
     * =========================================================
     * TIME OFENSIVO
     * =========================================================
     *
     * A força do time influencia quem
     * cria a jogada.
     *
     * E uma expulsão reduz a força efetiva.
     */
    private Time escolherTimeOfensivo(
            Jogo jogo) {

        Time casa =
                jogo.getTimeCasa();


        Time fora =
                jogo.getTimeFora();


        int forcaCasa =
                calcularForcaEfetiva(
                        jogo,
                        casa
                );


        int forcaFora =
                calcularForcaEfetiva(
                        jogo,
                        fora
                );


        int diferenca =
                forcaCasa
                - forcaFora;


        int chanceCasa =
                53 + diferenca;


        chanceCasa =
                Math.max(
                        10,
                        Math.min(
                                90,
                                chanceCasa
                        )
                );


        return random.nextInt(100)
                < chanceCasa
                        ? casa
                        : fora;
    }


    /*
     * Cada jogador expulso reduz bastante
     * a capacidade do time.
     */
    private int calcularForcaEfetiva(
            Jogo jogo,
            Time time) {

        int expulsos =
                contarExpulsosDoTime(
                        jogo,
                        time
                );


        return time.getForca()
                - (expulsos * 8);
    }


    private int contarExpulsosDoTime(
            Jogo jogo,
            Time time) {

        int quantidade = 0;


        for (
                Jogador jogador :
                time.getJogadores()
        ) {

            if (
                    jogo.jogadorExpulso(
                            jogador
                    )
            ) {

                quantidade++;
            }
        }


        return quantidade;
    }


    /*
     * Falta/cartão não precisa utilizar
     * força ofensiva.
     */
    private Time escolherTimeDisciplinar(
            Jogo jogo) {

        return random.nextBoolean()
                ? jogo.getTimeCasa()
                : jogo.getTimeFora();
    }


    /*
     * =========================================================
     * FINALIZADOR
     * =========================================================
     */

    private Jogador escolherFinalizador(
            List<Jogador> jogadores) {

        /*
         * Segurança caso um time tenha
         * muitos expulsos.
         */
        if (jogadores.isEmpty()) {

            throw new RuntimeException(
                    "Nenhum jogador disponível em campo."
            );
        }


        int pesoTotal = 0;


        for (Jogador jogador : jogadores) {

            pesoTotal +=
                    calcularPesoFinalizacao(
                            jogador
                    );
        }


        int sorteio =
                random.nextInt(
                        pesoTotal
                );


        int acumulado = 0;


        for (Jogador jogador : jogadores) {

            acumulado +=
                    calcularPesoFinalizacao(
                            jogador
                    );


            if (sorteio < acumulado) {

                return jogador;
            }
        }


        return jogadores.get(
                jogadores.size() - 1
        );
    }


    /*
     * Aqui corrigimos o problema de
     * zagueiro chutando o tempo todo.
     */
    private int calcularPesoFinalizacao(
            Jogador jogador) {

        int peso =
                jogador.getFinalizacao() * 3
                + jogador.getAtaque() * 2;


        String posicao =
                jogador.getPosicao();


        if (posicao.equalsIgnoreCase("ATA")) {

            peso += 220;

        } else if (
                posicao.equalsIgnoreCase("SA")) {

            peso += 180;

        } else if (
                posicao.equalsIgnoreCase("PD")
                || posicao.equalsIgnoreCase("PE")) {

            peso += 150;

        } else if (
                posicao.equalsIgnoreCase("MEI")) {

            peso += 90;

        } else if (
                posicao.equalsIgnoreCase("MC")) {

            peso += 35;

        } else if (
                posicao.equalsIgnoreCase("VOL")) {

            peso /= 4;

        } else if (
                posicao.equalsIgnoreCase("LD")
                || posicao.equalsIgnoreCase("LE")) {

            peso /= 5;

        } else if (
                posicao.equalsIgnoreCase("ZAG")) {

            peso /= 8;

        } else if (
                posicao.equalsIgnoreCase("GOL")) {

            peso = 1;
        }


        return Math.max(
                1,
                peso
        );
    }


    /*
     * =========================================================
     * IMPEDIMENTO
     * =========================================================
     */

    private Jogador escolherJogadorOfensivo(
            List<Jogador> jogadores) {

        List<Jogador> ofensivos =
                jogadores.stream()
                        .filter(jogador -> {

                            String p =
                                    jogador.getPosicao();


                            return p.equalsIgnoreCase("ATA")
                                    || p.equalsIgnoreCase("SA")
                                    || p.equalsIgnoreCase("PD")
                                    || p.equalsIgnoreCase("PE");
                        })
                        .toList();


        if (!ofensivos.isEmpty()) {

            return ofensivos.get(
                    random.nextInt(
                            ofensivos.size()
                    )
            );
        }


        return escolherFinalizador(
                jogadores
        );
    }


    /*
     * =========================================================
     * FALTA / CARTÃO
     * =========================================================
     */

    private Jogador escolherJogadorParaFalta(
            List<Jogador> jogadores) {

        List<Jogador> candidatos =
                jogadores.stream()
                        .filter(jogador ->
                                !jogador.getPosicao()
                                        .equalsIgnoreCase(
                                                "GOL"
                                        )
                        )
                        .toList();


        return escolherPorPesoDefensivo(
                candidatos
        );
    }


    private Jogador escolherJogadorParaCartao(
            List<Jogador> jogadores) {

        return escolherJogadorParaFalta(
                jogadores
        );
    }


    private Jogador escolherPorPesoDefensivo(
            List<Jogador> jogadores) {

        int pesoTotal = 0;


        for (Jogador jogador : jogadores) {

            int peso =
                    jogador.getDefesa()
                    + jogador.getFisico();


            String posicao =
                    jogador.getPosicao();


            if (
                    posicao.equalsIgnoreCase("ZAG")
                    || posicao.equalsIgnoreCase("VOL")
            ) {

                peso += 40;
            }


            pesoTotal +=
                    Math.max(
                            1,
                            peso
                    );
        }


        int sorteio =
                random.nextInt(
                        pesoTotal
                );


        int acumulado = 0;


        for (Jogador jogador : jogadores) {

            int peso =
                    jogador.getDefesa()
                    + jogador.getFisico();


            String posicao =
                    jogador.getPosicao();


            if (
                    posicao.equalsIgnoreCase("ZAG")
                    || posicao.equalsIgnoreCase("VOL")
            ) {

                peso += 40;
            }


            acumulado +=
                    Math.max(
                            1,
                            peso
                    );


            if (sorteio < acumulado) {

                return jogador;
            }
        }


        return jogadores.get(
                jogadores.size() - 1
        );
    }


    /*
     * =========================================================
     * MONTAGEM DA ESCALAÇÃO
     * =========================================================
     */

    private void adicionarMelhoresPorPosicao(
            List<Jogador> titulares,
            List<Jogador> disponiveis,
            String posicao,
            int quantidade) {

        List<Jogador> encontrados =
                disponiveis.stream()
                        .filter(jogador ->
                                jogador.getPosicao()
                                        .equalsIgnoreCase(
                                                posicao
                                        )
                        )
                        .sorted(
                                Comparator.comparingInt(
                                        Jogador::getOverall
                                ).reversed()
                        )
                        .limit(quantidade)
                        .toList();


        titulares.addAll(
                encontrados
        );


        disponiveis.removeAll(
                encontrados
        );
    }


    private void adicionarMelhoresDoMeio(
            List<Jogador> titulares,
            List<Jogador> disponiveis,
            int quantidade) {

        List<Jogador> encontrados =
                disponiveis.stream()
                        .filter(jogador -> {

                            String p =
                                    jogador.getPosicao();


                            return p.equalsIgnoreCase("VOL")
                                    || p.equalsIgnoreCase("MC")
                                    || p.equalsIgnoreCase("MEI");
                        })
                        .sorted(
                                Comparator.comparingInt(
                                        Jogador::getOverall
                                ).reversed()
                        )
                        .limit(quantidade)
                        .toList();


        titulares.addAll(
                encontrados
        );


        disponiveis.removeAll(
                encontrados
        );
    }


    private void adicionarMelhoresOfensivos(
            List<Jogador> titulares,
            List<Jogador> disponiveis,
            int quantidade) {

        List<Jogador> encontrados =
                disponiveis.stream()
                        .filter(jogador -> {

                            String p =
                                    jogador.getPosicao();


                            return p.equalsIgnoreCase("ATA")
                                    || p.equalsIgnoreCase("SA")
                                    || p.equalsIgnoreCase("PD")
                                    || p.equalsIgnoreCase("PE");
                        })
                        .sorted(
                                Comparator.comparingInt(
                                        Jogador::getOverall
                                ).reversed()
                        )
                        .limit(quantidade)
                        .toList();


        titulares.addAll(
                encontrados
        );


        disponiveis.removeAll(
                encontrados
        );
    }


    /*
     * =========================================================
     * ESTATÍSTICAS
     * =========================================================
     */

    private void registrarFinalizacao(
            Jogo jogo,
            Time time) {

        EstatisticasJogo e =
                jogo.getEstatisticas();


        if (time == jogo.getTimeCasa()) {

            e.setFinalizacoesCasa(
                    e.getFinalizacoesCasa() + 1
            );

        } else {

            e.setFinalizacoesFora(
                    e.getFinalizacoesFora() + 1
            );
        }
    }


    private void registrarFinalizacaoNoGol(
            Jogo jogo,
            Time time) {

        registrarFinalizacao(
                jogo,
                time
        );


        EstatisticasJogo e =
                jogo.getEstatisticas();


        if (time == jogo.getTimeCasa()) {

            e.setFinalizacoesGolCasa(
                    e.getFinalizacoesGolCasa() + 1
            );

        } else {

            e.setFinalizacoesGolFora(
                    e.getFinalizacoesGolFora() + 1
            );
        }
    }


    private void registrarEscanteio(
            Jogo jogo,
            Time time) {

        EstatisticasJogo e =
                jogo.getEstatisticas();


        if (time == jogo.getTimeCasa()) {

            e.setEscanteiosCasa(
                    e.getEscanteiosCasa() + 1
            );

        } else {

            e.setEscanteiosFora(
                    e.getEscanteiosFora() + 1
            );
        }
    }


    private void registrarFalta(
            Jogo jogo,
            Time time) {

        EstatisticasJogo e =
                jogo.getEstatisticas();


        if (time == jogo.getTimeCasa()) {

            e.setFaltasCasa(
                    e.getFaltasCasa() + 1
            );

        } else {

            e.setFaltasFora(
                    e.getFaltasFora() + 1
            );
        }
    }


    private void registrarAmarelo(
            Jogo jogo,
            Time time) {

        EstatisticasJogo e =
                jogo.getEstatisticas();


        if (time == jogo.getTimeCasa()) {

            e.setAmarelosCasa(
                    e.getAmarelosCasa() + 1
            );

        } else {

            e.setAmarelosFora(
                    e.getAmarelosFora() + 1
            );
        }
    }


    private void registrarVermelho(
            Jogo jogo,
            Time time) {

        EstatisticasJogo e =
                jogo.getEstatisticas();


        if (time == jogo.getTimeCasa()) {

            e.setVermelhosCasa(
                    e.getVermelhosCasa() + 1
            );

        } else {

            e.setVermelhosFora(
                    e.getVermelhosFora() + 1
            );
        }
    }


    private void registrarImpedimento(
            Jogo jogo,
            Time time) {

        EstatisticasJogo e =
                jogo.getEstatisticas();


        if (time == jogo.getTimeCasa()) {

            e.setImpedimentosCasa(
                    e.getImpedimentosCasa() + 1
            );

        } else {

            e.setImpedimentosFora(
                    e.getImpedimentosFora() + 1
            );
        }
    }


    /*
     * =========================================================
     * UTILITÁRIO
     * =========================================================
     */

    private String escolherFrase(
            String[] frases) {

        return frases[
                random.nextInt(
                        frases.length
                )
        ];
    }
}