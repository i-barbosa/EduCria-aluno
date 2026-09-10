"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { buscarStatus } from "@/lib/api";
import { encerrarCadastroPendente, lerCadastroPendente } from "@/lib/sessao";
import LogoEducria from "@/components/LogoEducria";
import Tagline from "@/components/Tagline";

export default function PaginaAguardando() {
  const router = useRouter();
  const [pendente, setPendente] = useState(null);
  const [verificando, setVerificando] = useState(false);
  const [mensagem, setMensagem] = useState("");
  const [aprovado, setAprovado] = useState(null);

  useEffect(() => {
    const atual = lerCadastroPendente();
    if (!atual) {
      router.replace("/cadastro");
      return;
    }
    setPendente(atual);
  }, [router]);

  async function verificarNovamente() {
    if (!pendente) return;
    setVerificando(true);
    setMensagem("");
    try {
      const status = await buscarStatus(pendente.email);
      if (status.aprovado) {
        setAprovado(status);
      } else {
        setMensagem("Ainda sem turma atribuida. Tente de novo mais tarde.");
      }
    } catch {
      setMensagem("Nao foi possivel verificar agora.");
    } finally {
      setVerificando(false);
    }
  }

  function irParaLogin() {
    encerrarCadastroPendente();
    router.push("/");
  }

  function sair() {
    encerrarCadastroPendente();
    router.push("/");
  }

  if (!pendente) return null;

  if (aprovado) {
    return (
      <main style={{ maxWidth: 700, display: "flex", justifyContent: "center" }}>
        <div className="login-marca" style={{ width: "100%" }}>
          <div className="marca-grande">
            <LogoEducria />
            <div>
              <div className="titulo"><b>EDU</b><i>CRIA</i></div>
              <Tagline style={{ marginTop: 6 }} />
            </div>
          </div>
          <span className="selo">Cadastro aprovado</span>
          <h2>Sua matricula: {aprovado.matricula}</h2>
          <p>
            Turma <strong>{aprovado.turma}</strong>. A senha temporaria foi enviada para o seu
            e-mail (ou repassada pela coordenacao). Use matricula + senha na tela de login.
          </p>
          <button onClick={irParaLogin}>Ir para o login</button>
        </div>
      </main>
    );
  }

  return (
    <main style={{ maxWidth: 700, display: "flex", justifyContent: "center" }}>
      <div className="login-marca" style={{ width: "100%" }}>
        <div className="marca-grande">
          <LogoEducria />
          <div>
            <div className="titulo"><b>EDU</b><i>CRIA</i></div>
            <Tagline style={{ marginTop: 6 }} />
          </div>
        </div>
        <span className="selo">Cadastro recebido</span>
        <h2>Oi, {pendente.nome.split(" ")[0]}</h2>
        <p>
          Seu cadastro foi recebido. Assim que a coordenacao aprovar e definir sua turma, voce
          recebe uma matricula e uma senha temporaria para entrar.
        </p>
        <p className="nota" style={{ color: "var(--papel)", opacity: 0.85 }}>{mensagem}</p>
        <button onClick={verificarNovamente} disabled={verificando} style={{ marginRight: 10 }}>
          {verificando ? "Verificando..." : "Verificar novamente"}
        </button>
        <button className="secundario" onClick={sair}>Sair</button>
      </div>
    </main>
  );
}
