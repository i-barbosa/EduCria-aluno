"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { buscarAssuntos, buscarProgresso } from "@/lib/api";
import { sessaoAluno } from "@/lib/clientSession";
import Cabecalho from "@/components/Cabecalho";

const QUESTOES_POR_SESSAO = 5;

export default function PaginaInicio() {
  const router = useRouter();
  const [sessao, setSessao] = useState(null);
  const [progresso, setProgresso] = useState(null);
  const [assuntos, setAssuntos] = useState([]);
  const [assuntoSelecionado, setAssuntoSelecionado] = useState(null);
  const [erro, setErro] = useState("");

  useEffect(() => {
    const atual = sessaoAluno.ler();
    if (!atual) {
      router.replace("/");
      return;
    }
    setSessao(atual);
    Promise.all([buscarProgresso(atual.token, atual.alunoId), buscarAssuntos(atual.token)])
      .then(([p, a]) => {
        setProgresso(p);
        setAssuntos(a);
      })
      .catch(() => setErro("Nao foi possivel carregar seus dados."));
  }, [router]);

  function nivelDoAssunto(assunto) {
    const item = progresso?.assuntos.find((a) => a.assuntoId === assunto.id);
    return item ? item.nivelAtual : 1;
  }

  function xpDoAssunto(assunto) {
    return progresso?.assuntos.find((a) => a.assuntoId === assunto.id)?.xp ?? 0;
  }

  function sair() {
    sessaoAluno.encerrar();
    router.push("/");
  }

  function comecarSessao() {
    if (!assuntoSelecionado) return;
    router.push(`/sessao?assuntoId=${assuntoSelecionado.id}&assuntoNome=${encodeURIComponent(assuntoSelecionado.nome)}`);
  }

  if (!sessao) return null;

  return (
    <>
      <Cabecalho nome={sessao.nome} aoSair={sair} />
      <main>
        {erro && <div className="erro-topo">{erro}</div>}
        <div className="bloco">
          <h2>Bom trabalho, {sessao.nome?.split(" ")[0]}</h2>
          <div className="resumo-linha">
            <div className="rotulo">Streak</div>
            <div className="valor">{progresso?.streakDiasSeguidos ?? 0} dia(s)</div>
          </div>
        </div>

        <div className="bloco">
          <h2>Escolha a materia para treinar</h2>
          <p className="nota">Cada materia tem sua propria escala de nivel, independente das outras.</p>
          <ul className="escolha-conceito">
            {assuntos.map((a) => (
              <li key={a.id}>
                <button
                  className="opcao-conceito"
                  aria-pressed={assuntoSelecionado?.id === a.id}
                  onClick={() => setAssuntoSelecionado(a)}
                >
                  <span>{a.nome}</span>
                  <span className="direita">nivel {nivelDoAssunto(a)}/10, {xpDoAssunto(a)} xp</span>
                </button>
              </li>
            ))}
            {assuntos.length === 0 && !erro && (
              <li className="nota" style={{ padding: "10px 0" }}>Carregando materias...</li>
            )}
          </ul>
        </div>

        {assuntoSelecionado && (
          <div className="bloco">
            <h2>Praticar {assuntoSelecionado.nome}</h2>
            <div className="resumo-linha">
              <div className="rotulo">Nivel atual</div>
              <div className="valor">{nivelDoAssunto(assuntoSelecionado)}/10</div>
            </div>
            <div className="resumo-linha">
              <div className="rotulo">XP acumulado</div>
              <div className="valor">{xpDoAssunto(assuntoSelecionado)}</div>
            </div>
            <p className="nota" style={{ marginTop: 14 }}>
              O sistema escolhe a questao pelo seu nivel atual e pela fila de revisao. Uma sessao
              tem {QUESTOES_POR_SESSAO} questoes.
            </p>
            <button onClick={comecarSessao}>Comecar sessao</button>
          </div>
        )}
      </main>
    </>
  );
}
