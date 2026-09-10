"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { login } from "@/lib/api";
import { salvarSessao } from "@/lib/sessao";
import LogoEducria from "@/components/LogoEducria";
import Tagline from "@/components/Tagline";

export default function PaginaLogin() {
  const router = useRouter();
  const [matricula, setMatricula] = useState("DEMO0001");
  const [senha, setSenha] = useState("123456");
  const [erro, setErro] = useState("");
  const [enviando, setEnviando] = useState(false);

  async function entrar(evento) {
    evento.preventDefault();
    setErro("");
    setEnviando(true);
    try {
      const resultado = await login(matricula.trim(), senha.trim());
      if (!resultado) {
        setErro("Matricula ou senha incorreta.");
        return;
      }
      salvarSessao(resultado);
      router.push("/inicio");
    } catch {
      setErro("Nao foi possivel falar com o servidor. Ele esta rodando?");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <main style={{ maxWidth: 900 }}>
      <div className="login-caixa">
        <div className="login-marca">
          <div className="marca-grande">
            <LogoEducria tamanho={76} />
            <div>
              <div className="titulo"><b>EDU</b><i>CRIA</i></div>
              <Tagline style={{ marginTop: 6 }} />
            </div>
          </div>
          <span className="selo">Ambiente de teste</span>
          <h2>Acesso ao modulo Degrau</h2>
          <p>Acompanhamento de aprendizagem por assunto. Pratica adaptativa, sem ranking entre colegas.</p>
          <dl className="login-avisos">
            <dt>Modulos disponiveis</dt>
            <dd>JavaScript, CSS, HTML, C e Java liberados nesta demo.</dd>
            <dt>Protecao de dados</dt>
            <dd>Desempenho individual, nunca comparado com colegas.</dd>
          </dl>
        </div>

        <form className="login-form" onSubmit={entrar}>
          <h3>Identificacao</h3>
          <div className="campo">
            <label htmlFor="matricula">Matricula</label>
            <input
              id="matricula"
              type="text"
              autoComplete="off"
              value={matricula}
              onChange={(e) => setMatricula(e.target.value)}
              placeholder="Ex.: A10070"
            />
          </div>
          <div className="campo">
            <label htmlFor="senha">Senha</label>
            <input
              id="senha"
              type="password"
              autoComplete="off"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
            />
          </div>
          <button type="submit" disabled={enviando}>
            {enviando ? "Entrando..." : "Entrar"}
          </button>
          <p className="erro-login" aria-live="polite">{erro}</p>
          <button
            type="button"
            className="secundario"
            style={{ marginTop: 10 }}
            onClick={() => router.push("/cadastro")}
          >
            Ainda nao tenho conta, cadastrar
          </button>
          <div className="credenciais">
            <span className="eyebrow">Conta de demonstracao</span>
            <div>matricula DEMO0001 / senha 123456</div>
          </div>
        </form>
      </div>
    </main>
  );
}
