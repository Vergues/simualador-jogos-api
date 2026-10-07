const $ = (id) => document.getElementById(id),
  esc = (v) =>
    String(v ?? "").replace(
      /[&<>"']/g,
      (c) =>
        ({
          "&": "&amp;",
          "<": "&lt;",
          ">": "&gt;",
          '"': "&quot;",
          "'": "&#39;",
        })[c],
    );
const atual = new Date().getFullYear();
let times = [],
  comps = [],
  timer,
  version = 0;
const p = () => new URLSearchParams(location.search),
  ano = () => Number(p().get("temporada")) || atual;
const url = (path, values = {}) => {
  let q = new URLSearchParams({ temporada: ano(), ...values });
  for (let [k, v] of [...q]) if (v === "" || v === "null") q.delete(k);
  return path + "?" + q;
};
const date = (v) =>
    new Date(v).toLocaleDateString("pt-BR", {
      timeZone: "America/Sao_Paulo",
      day: "2-digit",
      month: "short",
    }),
  hour = (v) =>
    new Date(v).toLocaleTimeString("pt-BR", {
      timeZone: "America/Sao_Paulo",
      hour: "2-digit",
      minute: "2-digit",
    });
const empty = (t) => `<p class="empty">${esc(t)}</p>`,
  badge = (s, l = false) => {
    const imagem = times.find((t) => t.sigla === s)?.escudo;
    const seguro = imagem && /^(https?:\/\/|\/(?!\/))/.test(imagem);
    return `<span class="badge${l ? " large" : ""}${seguro ? " image" : ""}" data-sigla="${esc(s)}">${seguro ? `<img src="${esc(imagem)}" alt="">` : esc(s)}</span>`;
  };
const title = (n, s) =>
  `<div class="page-heading"><div><p class="eyebrow">FUTEBOL · ${ano()}</p><h1>${esc(n)}</h1><p>${esc(s)}</p></div></div>`;
const panel = (n, c, link = "") =>
  `<section class="panel"><div class="panel-head"><h2>${esc(n)}</h2>${link ? `<a href="${esc(link)}">Ver todos →</a>` : ""}</div>${c}</section>`;
async function get(path) {
  let r = await fetch(path, { signal: AbortSignal.timeout(15000) }),
    d = await r.json();
  if (!r.ok) throw Error(d.erro || "Falha ao carregar os dados.");
  return d;
}
function matches(list) {
  return list.length
    ? list
        .map(
          (j) =>
            `<a class="match" href="${url("/partidas/" + j.id)}"><div class="match-meta"><span>${esc(j.campeonato)} · ${esc(j.grupo ? "Grupo " + j.grupo : j.fase)}${j.rodada ? " · R" + j.rodada : ""}${j.perna ? " · " + (j.perna === 1 ? "Ida" : "Volta") : ""}</span><span class="${["AO_VIVO", "INTERVALO"].includes(j.status) ? "status-live" : ""}">${j.status === "AGENDADO" ? date(j.dataHora) + " · " + hour(j.dataHora) : j.status === "ENCERRADO" ? "Encerrado" : j.status === "INTERVALO" ? "Intervalo" : "Ao vivo · " + j.minutoAtual + "′"}</span></div><div class="match-teams"><span class="team-inline">${badge(j.siglaCasa)}${esc(j.timeCasa)}</span><strong class="score">${j.status === "AGENDADO" ? "×" : j.golsCasa + " : " + j.golsFora}</strong><span class="team-inline away">${esc(j.timeFora)}${badge(j.siglaFora)}</span></div>${j.penaltisCasa != null ? `<div class="match-note">Pênaltis ${j.penaltisCasa}–${j.penaltisFora}</div>` : ""}</a>`,
        )
        .join("")
    : empty("Nenhum jogo encontrado.");
}
function table(list, small = false) {
  return list.length
    ? `<div class="table-wrap"><table class="${small ? "compact" : ""}"><thead><tr><th>#</th><th>Time</th>${(small ? ["PTS", "J", "SG"] : ["PTS", "J", "V", "E", "D", "GP", "GC", "SG", "%"]).map((x) => `<th>${x}</th>`).join("")}</tr></thead><tbody>${list.map((t, i) => `<tr><td class="rank ${i < 4 ? "top" : ""}">${i + 1}</td><td><a class="team-inline" href="${url("/times/" + t.slug)}">${badge(t.sigla)}${esc(t.time)}</a></td>${(small ? ["pontos", "jogos", "saldo"] : ["pontos", "jogos", "vitorias", "empates", "derrotas", "golsPro", "golsContra", "saldo", "aproveitamento"]).map((k) => `<td class="${k === "pontos" ? "points" : ""}">${t[k]}</td>`).join("")}</tr>`).join("")}</tbody></table></div>`
    : empty("A temporada ainda não tem jogos.");
}
function scorers(list, small = false) {
  return list.length
    ? small
      ? list
          .map(
            (j, i) =>
              `<a class="scorer" href="${url("/times/" + j.slug)}"><span>${i + 1}</span>${badge(j.sigla)}<span class="scorer-name"><strong>${esc(j.nome)}</strong><small>${esc(j.time)}</small></span><strong class="scorer-goals">${j.gols}<small>gols</small></strong></a>`,
          )
          .join("")
      : `<div class="table-wrap"><table><thead><tr><th>#</th><th>Jogador</th><th>Time</th><th>Gols</th><th>CA</th><th>CV</th></tr></thead><tbody>${list.map((j, i) => `<tr><td>${i + 1}</td><td>${esc(j.nome)}</td><td><a class="team-inline" href="${url("/times/" + j.slug)}">${badge(j.sigla)}${esc(j.time)}</a></td><td>${j.gols}</td><td>${j.amarelos}</td><td>${j.vermelhos}</td></tr>`).join("")}</tbody></table></div>`
    : empty("Os artilheiros aparecem após os primeiros gols.");
}
const option = (v, n, s) =>
  `<option value="${esc(v)}"${String(v) === String(s) ? " selected" : ""}>${esc(n)}</option>`;
const select = (name, label, options) =>
  `<label>${label}${name === "time" ? `<span id="escudoFiltro">${p().get("time") ? badge(p().get("time")) : ""}</span>` : ""}<select name="${name}">${options.map(([v, n]) => option(v, n, p().get(name) || "")).join("")}</select></label>`;
function filters(extra = "", all = true) {
  return `<form class="filters" id="filters">${select("competicao", "Competição", [...(all ? [["", "Todas"]] : []), ...comps.map((c) => [c.id, c.nome])])}${extra}<button class="button">Filtrar</button><a class="button secondary" href="${url(location.pathname)}">Limpar</a></form>`;
}
function bracket(list, c) {
  return c.fases
    .map((f) => panel(f, matches(list.filter((j) => j.fase === f))))
    .join("");
}
async function standings(c) {
  if (c.formato === "ELIMINATORIA")
    return bracket(await get(url("/api/jogos", { competicao: c.id })), c);
  let list = await get(url("/api/classificacao", { competicao: c.id }));
  return c.grupos
    ? [...new Set(list.map((t) => t.grupo))]
        .map((g) =>
          panel("Grupo " + g, table(list.filter((t) => t.grupo === g))),
        )
        .join("")
    : table(list);
}
async function render() {
  clearTimeout(timer);
  let token = ++version,
    interval = 0;
  $("erro").hidden = true;
  $("conteudo").innerHTML =
    '<div class="loading" role="status">Carregando o futebol…</div>';
  try {
    [times, comps] = await Promise.all([
      times.length ? times : get("/api/times"),
      get(url("/api/competicoes")),
    ]);
    let path = location.pathname,
      q = p(),
      html = "";
    if (path === "/") {
      let d = await get(url("/api/portal"));
      interval = 30000;
      html = `<section class="hero"><div><p class="eyebrow">DENTRO E FORA DAS QUATRO LINHAS</p><h1>O futebol acontece aqui.</h1><p>Resultados, próximos confrontos e os números da temporada.</p></div><div class="hero-number"><strong>${d.gols}</strong><span>gols em ${ano()}</span></div></section><div class="competition-strip">${comps.map((c) => `<a class="competition-pill" href="${url("/competicoes/" + c.id)}"><span class="comp-dot ${c.id}"></span>${esc(c.nome)}</a>`).join("")}</div><div class="home-grid"><div class="home-main">${panel("Jogos de hoje · " + date(d.data + "T12:00:00-03:00"), matches(d.hoje.slice(0, 4)), url("/jogos", { data: d.data }))}<div class="split">${panel("Próximos jogos", matches(d.proximos.slice(0, 4)), url("/jogos", { status: "proximos" }))}${panel("Últimos resultados", matches(d.resultados.slice(0, 4)), url("/jogos", { status: "encerrados" }))}</div></div><aside class="home-aside">${panel("Brasileirão", table(d.classificacao, true), url("/classificacao"))}${panel("Artilheiros", scorers(d.artilheiros, true), url("/artilharia"))}</aside></div>`;
    } else if (path === "/jogos") {
      let c = comps.find((c) => c.id === q.get("competicao")),
        list = await get(
          "/api/jogos?" +
            new URLSearchParams({
              temporada: ano(),
              ...Object.fromEntries(q),
              limite: 1500,
            }),
        );
      let extra =
        select("temporada", "Temporada", [
          [0, "Todo o histórico"],
          ...Array.from({ length: 8 }, (_, i) => [
            atual - 3 + i,
            atual - 3 + i,
          ]),
        ]) +
        `<label>Rodada<input name="rodada" type="number" min="1" max="38" placeholder="Todas" value="${esc(q.get("rodada"))}"></label>` +
        select("fase", "Fase", [
          ["", "Todas"],
          ...(c?.fases || [...new Set(comps.flatMap((c) => c.fases))]).map(
            (f) => [f, f],
          ),
        ]) +
        select("time", "Time", [
          ["", "Todos"],
          ...times.map((t) => [t.sigla, t.nome]),
        ]) +
        `<label>Data<input name="data" type="date" value="${esc(q.get("data"))}"></label>` +
        select("status", "Status", [
          ["", "Todos"],
          ["proximos", "Próximos"],
          ["andamento", "Em andamento"],
          ["encerrados", "Encerrados"],
        ]);
      html =
        title(
          "Jogos",
          list.length +
            " jogos encontrados · Consulte qualquer rodada ou temporada.",
        ) +
        filters(extra) +
        `<div id="listaJogos" class="games-grid">${matches(list.slice(0, 60))}</div>${list.length > 60 ? '<button class="button" id="mais">Mostrar mais jogos</button>' : ""}`;
      if (token === version) {
        $("conteudo").innerHTML = html;
        let limit = 60;
        $("mais")?.addEventListener("click", () => {
          limit += 60;
          $("listaJogos").innerHTML = matches(list.slice(0, limit));
          $("mais").hidden = limit >= list.length;
        });
      }
    } else if (path === "/competicoes") {
      html =
        title("Competições", "Quatro caminhos, uma temporada de futebol.") +
        `<div class="cards-grid">${comps.map((c) => `<a class="competition-card" href="${url("/competicoes/" + c.id)}"><span class="competition-emblem">${esc(c.nome.slice(0, 2).toUpperCase())}</span><h2>${esc(c.nome)}</h2><p>${esc(c.descricao)}</p><div class="card-bottom">${c.clubes.length} clubes · ${c.encerrados}/${c.jogos} jogos →</div>${c.campeao ? `<p>Campeão: ${esc(c.campeao)}</p>` : ""}</a>`).join("")}</div>`;
    } else if (path.startsWith("/competicoes/")) {
      let c = comps.find((c) => c.id === path.split("/")[2]);
      if (!c) throw Error("Competição não encontrada.");
      let [list, art] = await Promise.all([
        get(url("/api/jogos", { competicao: c.id })),
        get(url("/api/artilharia", { competicao: c.id })),
      ]);
      html =
        title(c.nome, c.descricao) +
        `<div class="tabs"><a class="tab" href="${url("/jogos", { competicao: c.id })}">Todos os jogos</a><a class="tab" href="${url("/classificacao", { competicao: c.id })}">Classificação / fases</a><a class="tab" href="${url("/artilharia", { competicao: c.id })}">Artilharia</a></div><div class="content-grid"><div class="stack">${panel("Próximos jogos", matches(list.filter((j) => j.status !== "ENCERRADO").slice(0, 6)))}${panel(
          "Últimos resultados",
          matches(
            list
              .filter((j) => j.status === "ENCERRADO")
              .reverse()
              .slice(0, 6),
          ),
        )}${panel("Classificação e fases", await standings(c))}</div><aside class="stack">${panel("Artilheiros", scorers(art.slice(0, 8), true))}${panel("Formato", `<div class="panel-body"><p>${esc(c.descricao)}</p><p>${c.fases.map(esc).join(" → ")}</p>${c.campeao ? `<p>Campeão: ${esc(c.campeao)}</p>` : ""}</div>`)}</aside></div>`;
    } else if (path === "/classificacao") {
      let c = comps.find(
        (c) => c.id === (q.get("competicao") || "brasileirao"),
      );
      if (!c) throw Error("Competição não encontrada.");
      html =
        title("Classificação", c.nome) +
        filters("", false) +
        panel(
          c.formato === "ELIMINATORIA"
            ? "Confrontos e classificados"
            : "Tabela da competição",
          await standings(c),
        ) +
        '<p class="format-note">PTS pontos · J jogos · V vitórias · E empates · D derrotas · GP gols pró · GC gols contra · SG saldo · % aproveitamento. Desempate: pontos, vitórias, saldo e gols pró.</p>';
    } else if (path === "/artilharia") {
      html =
        title(
          "Artilharia",
          "Gols e cartões calculados a partir dos eventos das partidas.",
        ) +
        filters() +
        panel(
          "Artilheiros da temporada",
          scorers(
            await get(
              url("/api/artilharia", { competicao: q.get("competicao") }),
            ),
          ),
        );
    } else if (path === "/times") {
      html =
        title("Times", "Escolha um clube para acompanhar sua temporada.") +
        `<div class="cards-grid">${times.map((t) => `<a class="team-card" href="${url("/times/" + t.slug)}">${badge(t.sigla, true)}<div><h2>${esc(t.nome)}</h2><p>${esc(t.pais)} · ${t.jogadores.length} jogadores</p></div><span>→</span></a>`).join("")}</div>`;
    } else if (path.startsWith("/times/")) {
      let d = await get(url("/api" + path)),
        t = d.time,
        s = d.desempenho;
      html = `<div class="team-hero">${badge(t.sigla, true)}<div><p class="eyebrow">${esc(t.pais)} · ${ano()}</p><h1>${esc(t.nome)}</h1></div><a class="button secondary" href="${url("/jogos", { time: t.sigla, temporada: 0 })}">Todo o histórico →</a></div><div class="metrics">${[
        ["Jogos", s.jogos],
        ["Vitórias", s.vitorias],
        ["Empates", s.empates],
        ["Derrotas", s.derrotas],
        ["Gols pró / contra", s.golsPro + " / " + s.golsContra],
        ["Aproveitamento", s.aproveitamento + "%"],
      ]
        .map(
          ([n, v]) =>
            `<div class="metric"><span>${n}</span><strong>${v}</strong></div>`,
        )
        .join(
          "",
        )}</div><p class="format-note">Saldo ${s.saldo} · ${s.amarelos} amarelos · ${s.vermelhos} vermelhos</p><div class="content-grid"><div class="stack">${panel("Próximos jogos", matches(d.proximos))}${panel("Últimos resultados", matches(d.ultimos))}${panel(
        "Elenco",
        `<div class="table-wrap"><table><thead><tr><th>Jogador</th><th>Posição</th><th>Gols</th><th>CA</th><th>CV</th></tr></thead><tbody>${t.jogadores
          .map((j, i) => {
            let e = d.jogadores.find((x) => x.id === i) || {};
            return `<tr><td>${esc(j.nome)}</td><td>${esc(j.posicao)}</td><td>${e.gols || 0}</td><td>${e.amarelos || 0}</td><td>${e.vermelhos || 0}</td></tr>`;
          })
          .join("")}</tbody></table></div>`,
      )}</div><aside>${panel("Nas competições", `<div class="panel-body">${d.competicoes.map((c) => `<p><a href="${url("/competicoes/" + c.id)}"><strong>${esc(c.nome)}</strong></a><br>${c.posicao ? c.posicao + "º lugar" + (c.grupo ? " · Grupo " + esc(c.grupo) : "") : esc(c.fase)}</p>`).join("")}</div>`)}</aside></div>`;
    } else if (path === "/calendario") {
      let month = Number(q.get("mes")) || new Date().getMonth() + 1,
        list = await get(
          url("/api/jogos", { competicao: q.get("competicao"), limite: 1500 }),
        );
      html =
        title("Calendário", "Todas as competições no mesmo campo de visão.") +
        filters(
          select(
            "mes",
            "Mês",
            Array.from({ length: 12 }, (_, i) => [
              i + 1,
              new Date(2026, i, 1).toLocaleDateString("pt-BR", {
                month: "long",
              }),
            ]),
          ),
        ) +
        `<div class="calendar-grid">${["Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"].map((d) => `<div class="calendar-weekday">${d}</div>`).join("")}${'<div class="calendar-day blank"></div>'.repeat(new Date(ano(), month - 1, 1).getDay())}${Array.from(
          { length: new Date(ano(), month, 0).getDate() },
          (_, i) => {
            let key = `${ano()}-${String(month).padStart(2, "0")}-${String(i + 1).padStart(2, "0")}`,
              day = list.filter((j) => j.dataHora.slice(0, 10) === key);
            return `<div class="calendar-day"><strong class="calendar-number">${i + 1}</strong>${day
              .slice(0, 3)
              .map(
                (j) =>
                  `<a class="calendar-game ${esc(j.competicao)}" href="${url("/partidas/" + j.id)}"><span class="calendar-teams">${badge(j.siglaCasa)}<span class="calendar-club-name">${esc(j.siglaCasa)}</span><span>${j.status === "AGENDADO" ? "×" : j.golsCasa + "–" + j.golsFora}</span>${badge(j.siglaFora)}<span class="calendar-club-name">${esc(j.siglaFora)}</span></span><small class="calendar-time">${esc(j.campeonato)} · ${hour(j.dataHora)}</small></a>`,
              )
              .join(
                "",
              )}${day.length > 3 ? `<a class="calendar-more" href="${url("/jogos", { data: key })}">+${day.length - 3} jogos</a>` : ""}</div>`;
          },
        ).join(
          "",
        )}</div><p class="format-note">Calendário acadêmico com pelo menos 48 horas entre partidas do mesmo clube. As próximas fases aparecem quando os classificados são definidos.</p>`;
    } else if (path.startsWith("/partidas/") || path === "/partida.html") {
      let id = path === "/partida.html" ? q.get("id") : path.split("/")[2],
        j = await get("/api/jogos/" + encodeURIComponent(id)),
        e = j.estatisticas;
      interval =
        j.status === "ENCERRADO" ? 0 : j.status === "AGENDADO" ? 60000 : 5000;
      let roster = (side) =>
        `<div class="table-wrap"><table><thead><tr><th>Jogador</th><th>Posição</th><th>Situação</th><th>CA</th></tr></thead><tbody>${(j.escalacoes?.[side] || []).map((x) => `<tr><td>${esc(x.nome)}</td><td>${esc(x.posicao)}</td><td>${esc(x.situacao)}</td><td>${x.amarelos}</td></tr>`).join("")}</tbody></table></div>`;
      html = `<p class="breadcrumb"><a href="${url("/competicoes/" + j.competicao)}">${esc(j.campeonato)}</a> / ${esc(j.fase)} · Rodada ${j.rodada}${j.grupo ? " · Grupo " + esc(j.grupo) : ""}${j.perna ? " · " + (j.perna === 1 ? "Ida" : "Volta") : ""}</p><section class="match-hero"><h1>Detalhes da partida</h1><div class="match-scoreboard"><a class="match-club" href="${url("/times/" + j.slugCasa)}">${badge(j.siglaCasa, true)}<h2>${esc(j.timeCasa)}</h2></a><div class="match-score"><strong>${j.status === "AGENDADO" ? "×" : j.golsCasa + " : " + j.golsFora}</strong><div class="match-clock">${j.status === "AGENDADO" ? "Agendado" : j.status === "ENCERRADO" ? "Encerrado" : j.status === "INTERVALO" ? "Intervalo" : "Ao vivo · " + j.minutoAtual + "′"}</div>${j.penaltisCasa != null ? `<p>Pênaltis ${j.penaltisCasa}–${j.penaltisFora}</p>` : ""}</div><a class="match-club" href="${url("/times/" + j.slugFora)}">${badge(j.siglaFora, true)}<h2>${esc(j.timeFora)}</h2></a></div><p>${date(j.dataHora)} de ${j.temporada} · ${hour(j.dataHora)}${j.vencedor ? " · Classificado: " + esc(times.find((t) => t.sigla === j.vencedor)?.nome || j.vencedor) : ""}</p></section><div class="content-grid"><div class="stack">${panel(
        "Eventos",
        j.eventos?.length
          ? `<div class="timeline">${[...j.eventos]
              .reverse()
              .map(
                (x) =>
                  `<div class="event"><strong class="event-time">${x.minuto}′</strong><span class="event-icon">${{ GOL: "⚽", CARTAO_AMARELO: "🟨", CARTAO_VERMELHO: "🟥", SUBSTITUICAO: "↔" }[x.tipo] || "•"}</span><div><strong>${esc(x.tipo.replaceAll("_", " "))}</strong><p>${esc(x.descricao)}</p><small>${esc(x.time)}</small></div></div>`,
              )
              .join("")}</div>`
          : empty("Os eventos aparecem quando a partida começa."),
      )}${panel("Escalação · " + j.timeCasa, roster("casa"))}${panel("Escalação · " + j.timeFora, roster("fora"))}</div><aside>${panel(
        "Estatísticas",
        e
          ? `<div class="panel-body">${[
              ["Posse de bola", "posse"],
              ["Finalizações", "finalizacoes"],
              ["No gol", "finalizacoesGol"],
              ["Escanteios", "escanteios"],
              ["Faltas", "faltas"],
              ["Amarelos", "amarelos"],
              ["Vermelhos", "vermelhos"],
              ["Impedimentos", "impedimentos"],
            ]
              .map(
                ([n, k]) =>
                  `<div class="stats-row"><strong>${e[k + "Casa"]}</strong><span>${n}</span><strong>${e[k + "Fora"]}</strong></div>`,
              )
              .join("")}</div>`
          : empty("Disponíveis após o início."),
      )}</aside></div>`;
    } else throw Error("Página não encontrada.");
    if (token !== version) return;
    if (path !== "/jogos") $("conteudo").innerHTML = html;
    if (path === "/jogos")
      document.querySelector("[name=temporada]").value =
        q.get("temporada") ?? ano();
    if (path === "/calendario")
      document.querySelector("[name=mes]").value =
        q.get("mes") || new Date().getMonth() + 1;
    if (path === "/classificacao")
      document.querySelector("[name=competicao]").value =
        q.get("competicao") || "brasileirao";
    document.querySelector("[name=time]")?.addEventListener("change", (ev) => {
      $("escudoFiltro").innerHTML = ev.target.value
        ? badge(ev.target.value)
        : "";
    });
    $("filters")?.addEventListener("submit", (ev) => {
      ev.preventDefault();
      navigate(url(path, Object.fromEntries(new FormData(ev.target))));
    });
    document.title =
      ($("conteudo").querySelector("h1")?.textContent || "Partida") +
      " · Linha de Fundo";
    document
      .querySelectorAll("#menu a")
      .forEach((a) =>
        a.classList.toggle(
          "active",
          a.pathname === "/" ? path === "/" : path.startsWith(a.pathname),
        ),
      );
    if (interval)
      timer = setTimeout(() => {
        let y = scrollY;
        render().then(() => scrollTo(0, y));
      }, interval);
  } catch (error) {
    if (token !== version) return;
    $("conteudo").innerHTML =
      `<section class="panel"><div class="panel-body"><h1>Não foi possível carregar</h1><p>${esc(error.message)}</p><button class="button" id="retry">Tentar novamente</button></div></section>`;
    $("retry").onclick = render;
    $("erro").textContent = error.message;
    $("erro").hidden = false;
  }
}
function navigate(href) {
  history.pushState({}, "", href);
  $("menu").classList.remove("open");
  $("menuToggle").setAttribute("aria-expanded", "false");
  $("temporadaGlobal").value = ano();
  scrollTo(0, 0);
  render();
}
document.addEventListener("click", (ev) => {
  let a = ev.target.closest("a");
  if (
    !a ||
    ev.ctrlKey ||
    ev.metaKey ||
    ev.shiftKey ||
    ev.altKey ||
    ev.button !== 0 ||
    a.origin !== location.origin ||
    a.hash ||
    a.target
  )
    return;
  ev.preventDefault();
  let u = new URL(a.href);
  if (!u.searchParams.has("temporada")) u.searchParams.set("temporada", ano());
  navigate(u.pathname + u.search);
});
$("temporadaGlobal").innerHTML = Array.from({ length: 8 }, (_, i) =>
  option(atual - 3 + i, atual - 3 + i, ano()),
).join("");
$("temporadaGlobal").onchange = () =>
  navigate(
    url(location.pathname, {
      ...Object.fromEntries(p()),
      temporada: $("temporadaGlobal").value,
    }),
  );
$("menuToggle").onclick = () => {
  $("menu").classList.toggle("open");
  $("menuToggle").setAttribute(
    "aria-expanded",
    String($("menu").classList.contains("open")),
  );
};
addEventListener("popstate", () => {
  $("temporadaGlobal").value = ano();
  render();
});
addEventListener("pagehide", () => clearTimeout(timer));
render();

document.addEventListener(
  "error",
  (event) => {
    const image = event.target;
    if (
      image instanceof HTMLImageElement &&
      image.parentElement?.classList.contains("badge")
    ) {
      const container = image.parentElement;
      container.textContent = container.dataset.sigla;
      container.classList.remove("image");
    }
  },
  true,
);
