"use client";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { buscarProgresso, buscarProximaQuestao, responderTentativa } from "@/lib/api";
import { encerrarSessao, lerSessao } from "@/lib/sessao";
import Cabecalho from "@/components/Cabecalho";

const LETRAS = ["A", "B", "C", "D", "E"];
const QUESTOES_POR_SESSAO = 5;
const NIVEL_MAXIMO = 10;

export default function SessaoConteudo() {
  const router = useRouter();
  const params = useSearchParams();
  const assuntoId = params.get("assuntoId");
  const assuntoNome = params.get("assuntoNome");

  const [sessao, setSessao] = useState(null);
  const [tela, setTela] = useState("sessao"); // "sessao" | "resultado"

  const [sessaoContagem, setSessaoContagem] = useState(0);
  const [sessaoAcertos, setSessaoAcertos] = useState(0);
  const [sessaoErros, setSessaoErros] = useState(0);
  const [sessaoXp, setSessaoXp] = useState(0);
  const [sessaoEventos, setSessaoEventos] = useState([]);

  const [questao, setQuestao] = useState(null);
  const [alternativaEscolhida, setAlternativaEscolhida] = useState(null);
  const [respondida, setRespondida] = useState(null);
  const [nivelAtual, setNivelAtual] = useState(1);
  const [erro, setErro] = useState("");

  useEffect(() => {
    const atual = lerSessao();
    if (!atual || !assuntoId) {
      router.replace("/inicio");
      return;
    }
    setSessao(atual);
    buscarProgresso(atual.token, atual.alunoId)
      .then((progresso) => {
        const item = progresso.assuntos.find((a) => a.assuntoId === Number(assuntoId));
        setNivelAtual(item ? item.nivelAtual : 1);
        return buscarProximaQuestao(atual.token, atual.alunoId, assuntoId);
      })
      .then(setQuestao)
      .catch(() => setErro("Nao foi possivel carregar a questao."));
  }, [router, assuntoId]);

  async function proximaQuestao(tokenAtual, alunoId) {
    setRespondida(null);
    setAlternativaEscolhida(null);
    setErro("");
    try {
      setQuestao(await buscarProximaQuestao(tokenAtual, alunoId, assuntoId));
    } catch {
      setErro("Nao foi possivel carregar a proxima questao.");
    }
  }

  async function responder() {
    if (alternativaEscolhida == null || !questao || !sessao) return;
    try {
      const resultado = await responderTentativa(sessao.token, sessao.alunoId, questao.id, alternativaEscolhida);
      setRespondida(resultado);
      setNivelAtual(resultado.nivelAtual);
      setSessaoXp((v) => v + resultado.xpGanho);
      if (resultado.acertou) setSessaoAcertos((v) => v + 1);
      else setSessaoErros((v) => v + 1);

      const eventos = [];
      if (resultado.subiuNivel) eventos.push(`Subiu para o nivel ${resultado.nivelAtual}.`);
      if (resultado.desceuNivel) eventos.push(`Nivel baixou para ${resultado.nivelAtual}. Errar nao tira pontos.`);
      resultado.badgesDesbloqueados.forEach((b) => eventos.push(`Badge desbloqueado: ${b}`));
      resultado.conquistasDesbloqueadas.forEach((c) => eventos.push(`Conquista desbloqueada: ${c}`));
      if (eventos.length) setSessaoEventos((atual) => [...atual, ...eventos]);
    } catch {
      setErro("Nao foi possivel registrar sua resposta.");
    }
  }

  async function proximaOuEncerrar() {
    const proximaContagem = sessaoContagem + 1;
    setSessaoContagem(proximaContagem);
    if (proximaContagem >= QUESTOES_POR_SESSAO) {
      setTela("resultado");
      return;
    }
    await proximaQuestao(sessao.token, sessao.alunoId);
  }

  function novaSessao() {
    setSessaoContagem(0);
    setSessaoAcertos(0);
    setSessaoErros(0);
    setSessaoXp(0);
    setSessaoEventos([]);
    setTela("sessao");
    proximaQuestao(sessao.token, sessao.alunoId);
  }

  function sair() {
    encerrarSessao();
    router.push("/");
  }

  if (!sessao) return null;

  if (tela === "resultado") {
    return (
      <>
        <Cabecalho nome={sessao.nome} aoSair={sair} />
        <main>
          <div className="bloco">
            <h2>Sessao de {assuntoNome} encerrada, {sessao.nome?.split(" ")[0]}</h2>
            <div className="resumo-linha">
              <div className="rotulo">Nivel atual</div>
              <div className="valor">{nivelAtual}/{NIVEL_MAXIMO}</div>
            </div>
            <div className="resumo-linha">
              <div className="rotulo">XP ganho na sessao</div>
              <div className="valor">{sessaoXp}</div>
            </div>
            <div className="resumo-linha">
              <div className="rotulo">Acertos</div>
              <div className="valor">{sessaoAcertos} de {sessaoAcertos + sessaoErros}</div>
            </div>
          </div>

          {sessaoEventos.length > 0 && (
            <div className="bloco">
              <h2>O que aconteceu</h2>
              <ul className="alternativas" style={{ borderTop: "none" }}>
                {sessaoEventos.map((evento, i) => (
                  <li key={i} style={{ padding: "6px 0" }}>{evento}</li>
                ))}
              </ul>
            </div>
          )}

          <button onClick={novaSessao}>Nova sessao</button>{" "}
          <button className="secundario" onClick={() => router.push("/inicio")}>Voltar ao inicio</button>
        </main>
      </>
    );
  }

  return (
    <>
      <Cabecalho nome={sessao.nome} aoSair={sair} />
      <main>
        {erro && <div className="erro-topo">{erro}</div>}
        <div className="escada-caixa">
          <div className="escada-topo">
            <div className="eyebrow">{assuntoNome}</div>
            <div>Nivel {nivelAtual} de {NIVEL_MAXIMO}</div>
          </div>
          <div className="escada">
            {Array.from({ length: NIVEL_MAXIMO }, (_, i) => i + 1).map((n) => (
              <div
                key={n}
                className={"degrau" + (n === nivelAtual ? " atual" : n < nivelAtual ? " abaixo" : "")}
                style={{ height: 28 + (n - 1) * 7 }}
              >
                {n}
              </div>
            ))}
          </div>
          <div className="escada-base" />
        </div>

        <div className="bloco">
          <div className="marcador">
            <span>Questao {sessaoContagem + 1} de {QUESTOES_POR_SESSAO}</span>
            <span className="num">{sessaoXp} XP na sessao</span>
          </div>

          {questao && (
            <div>
              <div className="etiquetas">
                <span className="etiqueta">{assuntoNome}, nivel {questao.nivel}</span>
              </div>
              <p className="enunciado">{questao.enunciado}</p>
              <ul className="alternativas">
                {questao.alternativas.map((alt, i) => {
                  let classe = "alt";
                  if (respondida && alt.id === alternativaEscolhida) {
                    classe += respondida.acertou ? " certa" : " errada";
                  }
                  return (
                    <li key={alt.id}>
                      <button
                        className={classe}
                        disabled={!!respondida}
                        onClick={() => setAlternativaEscolhida(alt.id)}
                        style={!respondida && alternativaEscolhida === alt.id ? { background: "var(--papel)" } : undefined}
                      >
                        <span className="letra">{LETRAS[i]}</span>
                        <span>{alt.texto}</span>
                      </button>
                    </li>
                  );
                })}
              </ul>

              {!respondida && (
                <button style={{ marginTop: 16 }} disabled={alternativaEscolhida == null} onClick={responder}>
                  Responder
                </button>
              )}

              {respondida && (
                <div className={"veredito " + (respondida.acertou ? "acerto" : "erro")}>
                  <div className="titulo">
                    {respondida.acertou ? `Certo, +${respondida.xpGanho} pontos` : "Resposta incorreta"}
                  </div>
                  <div className="movimento">
                    Esta questao volta em {respondida.proximaRevisao}.
                    {respondida.subiuNivel && " Voce subiu de nivel!"}
                    {respondida.desceuNivel && " O nivel baixou, mas nenhum ponto foi perdido."}
                  </div>
                  <button onClick={proximaOuEncerrar}>
                    {sessaoContagem + 1 >= QUESTOES_POR_SESSAO ? "Ver resultado" : "Proxima questao"}
                  </button>
                </div>
              )}
            </div>
          )}
        </div>

        <button className="secundario" onClick={() => router.push("/inicio")}>Sair da sessao</button>
      </main>
    </>
  );
}
