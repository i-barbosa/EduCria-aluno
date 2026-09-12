"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { cadastrar } from "@/lib/api";
import { cadastroPendente } from "@/lib/clientSession";
import LogoEducria from "@/components/LogoEducria";
import Tagline from "@/components/Tagline";

export default function PaginaCadastro() {
  const router = useRouter();
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [erro, setErro] = useState("");
  const [enviando, setEnviando] = useState(false);

  async function criarConta(evento) {
    evento.preventDefault();
    setErro("");
    setEnviando(true);
    try {
      const resultado = await cadastrar(nome.trim(), email.trim());
      if (resultado.conflito) {
        setErro("Esse e-mail ja tem cadastro. Tente entrar em vez de cadastrar de novo.");
        return;
      }
      cadastroPendente.salvar({ nome: resultado.aluno.nome, email: resultado.aluno.email });
      router.push("/aguardando");
    } catch {
      setErro("Nao foi possivel cadastrar. O servidor esta rodando?");
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
          <span className="selo">Novo cadastro</span>
          <h2>Crie sua conta de estudante</h2>
          <p>E so nome e e-mail. A coordenacao aprova seu cadastro, define sua turma e envia sua matricula e senha.</p>
        </div>

        <form className="login-form" onSubmit={criarConta}>
          <h3>Seus dados</h3>
          <div className="campo">
            <label htmlFor="nome">Nome</label>
            <input
              id="nome"
              type="text"
              autoComplete="off"
              value={nome}
              onChange={(e) => setNome(e.target.value)}
              placeholder="Seu nome"
            />
          </div>
          <div className="campo">
            <label htmlFor="email">E-mail</label>
            <input
              id="email"
              type="email"
              autoComplete="off"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="voce@exemplo.com"
            />
          </div>
          <button type="submit" disabled={enviando}>
            {enviando ? "Enviando..." : "Cadastrar"}
          </button>
          <p className="erro-login" aria-live="polite">{erro}</p>
          <button
            type="button"
            className="secundario"
            style={{ marginTop: 10 }}
            onClick={() => router.push("/")}
          >
            Ja tenho conta, entrar
          </button>
        </form>
      </div>
    </main>
  );
}
