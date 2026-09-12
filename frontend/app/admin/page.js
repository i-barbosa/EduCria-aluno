"use client";

import { useCallback, useEffect, useState } from "react";
import {
  aprovarAluno,
  buscarAlunosPendentes,
  buscarTurmas,
  criarTurma,
  loginAdmin,
} from "@/lib/api";
import { sessaoAdmin } from "@/lib/clientSession";
import LogoEducria from "@/components/LogoEducria";

export default function PaginaAdmin() {
  const [senha, setSenha] = useState("");
  const [autenticado, setAutenticado] = useState(false);
  const [verificandoSessao, setVerificandoSessao] = useState(true);
  const [erroLogin, setErroLogin] = useState("");
  const [enviandoLogin, setEnviandoLogin] = useState(false);

  const [turmas, setTurmas] = useState([]);
  const [pendentes, setPendentes] = useState([]);
  const [nomeNovaTurma, setNomeNovaTurma] = useState("");
  const [erroTurma, setErroTurma] = useState("");
  const [selecaoTurma, setSelecaoTurma] = useState({});
  const [aviso, setAviso] = useState(null);

  const carregarDados = useCallback(async (token) => {
    try {
      const [t, p] = await Promise.all([buscarTurmas(token), buscarAlunosPendentes(token)]);
      setTurmas(t);
      setPendentes(p);
    } catch {
      setAutenticado(false);
      sessaoAdmin.encerrar();
    }
  }, []);

  useEffect(() => {
    const token = sessaoAdmin.ler()?.token;
    if (!token) {
      setVerificandoSessao(false);
      return;
    }
    carregarDados(token).then(() => {
      setAutenticado(true);
      setVerificandoSessao(false);
    });
  }, [carregarDados]);

  async function entrar(evento) {
    evento.preventDefault();
    setErroLogin("");
    setEnviandoLogin(true);
    try {
      const resultado = await loginAdmin(senha);
      if (!resultado) {
        setErroLogin("Senha incorreta.");
        return;
      }
      sessaoAdmin.salvar({ token: resultado.token });
      await carregarDados(resultado.token);
      setAutenticado(true);
    } catch {
      setErroLogin("Nao foi possivel falar com o servidor.");
    } finally {
      setEnviandoLogin(false);
    }
  }

  function sair() {
    sessaoAdmin.encerrar();
    setAutenticado(false);
    setSenha("");
  }

  async function criar(evento) {
    evento.preventDefault();
    setErroTurma("");
    const token = sessaoAdmin.ler()?.token;
    const resultado = await criarTurma(token, nomeNovaTurma.trim());
    if (resultado.conflito) {
      setErroTurma("Ja existe uma turma com esse nome.");
      return;
    }
    setNomeNovaTurma("");
    carregarDados(token);
  }

  async function aprovar(alunoId) {
    const turmaId = selecaoTurma[alunoId];
    if (!turmaId) return;
    const token = sessaoAdmin.ler()?.token;
    const resultado = await aprovarAluno(token, alunoId, Number(turmaId));
    setAviso(resultado);
    carregarDados(token);
  }

  if (verificandoSessao) return null;

  if (!autenticado) {
    return (
      <main style={{ maxWidth: 480 }}>
        <div className="login-marca" style={{ marginBottom: 0 }}>
          <div className="marca-grande">
            <LogoEducria />
            <div className="titulo"><b>EDU</b><i>CRIA</i></div>
          </div>
          <span className="selo">Painel da coordenacao</span>
        </div>
        <form className="login-form" onSubmit={entrar}>
          <h3>Entrar</h3>
          <div className="campo">
            <label htmlFor="senha-admin">Senha</label>
            <input
              id="senha-admin"
              type="password"
              autoComplete="off"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
            />
          </div>
          <button type="submit" disabled={enviandoLogin}>
            {enviandoLogin ? "Entrando..." : "Entrar"}
          </button>
          <p className="erro-login">{erroLogin}</p>
        </form>
      </main>
    );
  }

  return (
    <main>
      <header className="topo" style={{ marginLeft: -24, marginRight: -24, marginTop: -28 }}>
        <div className="topo-interno">
          <div className="marca">
            <LogoEducria />
            <div className="nome">
              <b>EDU</b><i>CRIA</i> <span style={{ fontSize: "0.5em" }}>· coordenacao</span>
            </div>
          </div>
          <button className="btn-sair pequeno" onClick={sair}>Sair</button>
        </div>
      </header>

      <div style={{ marginTop: 22 }}>
        {aviso && (
          <div className="bloco" style={{ borderColor: "var(--verde)", borderWidth: 2 }}>
            <h2>Cadastro aprovado</h2>
            <p>
              <strong>{aviso.nome}</strong> ({aviso.email}) agora e da turma <strong>{aviso.turma}</strong>.{" "}
              {aviso.emailEnviado
                ? "As credenciais ja foram enviadas por e-mail para ele."
                : "Nao foi possivel enviar o e-mail automatico - copie as credenciais abaixo e repasse manualmente."}
            </p>
            <table>
              <tbody>
                <tr>
                  <td>Matricula</td>
                  <td className="n">{aviso.matricula}</td>
                </tr>
                <tr>
                  <td>Senha temporaria</td>
                  <td className="n">{aviso.senha}</td>
                </tr>
              </tbody>
            </table>
          </div>
        )}

        <section className="bloco">
          <h2>Turmas</h2>
          <table>
            <thead>
              <tr>
                <th>Nome</th>
                <th>Alunos</th>
              </tr>
            </thead>
            <tbody>
              {turmas.map((t) => (
                <tr key={t.id}>
                  <td data-rotulo="Nome">{t.nome}</td>
                  <td data-rotulo="Alunos" className="n">{t.quantidadeAlunos}</td>
                </tr>
              ))}
              {turmas.length === 0 && (
                <tr><td colSpan={2}>Nenhuma turma criada ainda.</td></tr>
              )}
            </tbody>
          </table>

          <form onSubmit={criar} style={{ marginTop: 16 }} className="campos">
            <div className="campo">
              <label htmlFor="nova-turma">Nova turma</label>
              <input
                id="nova-turma"
                type="text"
                value={nomeNovaTurma}
                onChange={(e) => setNomeNovaTurma(e.target.value)}
                placeholder="Ex.: A1"
              />
            </div>
            <div className="campo" style={{ display: "flex", alignItems: "flex-end" }}>
              <button type="submit">Criar turma</button>
            </div>
          </form>
          <p className="erro-login">{erroTurma}</p>
          <p className="nota">O nome da turma vira o prefixo da matricula (ex.: turma "A1" gera A10000, A10001...).</p>
        </section>

        <section className="bloco">
          <h2>Cadastros aguardando aprovacao</h2>
          {pendentes.length === 0 && <p className="nota">Ninguem esperando no momento.</p>}
          {pendentes.map((aluno) => (
            <div key={aluno.id} className="resumo-linha" style={{ alignItems: "center" }}>
              <div>
                <strong>{aluno.nome}</strong>
                <div className="nota">{aluno.email}</div>
              </div>
              <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
                <select
                  value={selecaoTurma[aluno.id] || ""}
                  onChange={(e) => setSelecaoTurma({ ...selecaoTurma, [aluno.id]: e.target.value })}
                >
                  <option value="">Escolha a turma</option>
                  {turmas.map((t) => (
                    <option key={t.id} value={t.id}>{t.nome}</option>
                  ))}
                </select>
                <button className="pequeno" onClick={() => aprovar(aluno.id)} disabled={!selecaoTurma[aluno.id]}>
                  Aprovar
                </button>
              </div>
            </div>
          ))}
        </section>
      </div>
    </main>
  );
}
