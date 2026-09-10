"use client";

import LogoEducria from "./LogoEducria";
import Tagline from "./Tagline";

export default function Cabecalho({ nome, aoSair }) {
  return (
    <header className="topo">
      <div className="topo-interno">
        <div className="marca">
          <LogoEducria />
          <div>
            <div className="nome"><b>EDU</b><i>CRIA</i></div>
            <div className="sub">Degrau · plataforma de acompanhamento</div>
          </div>
        </div>
        {nome && (
          <div style={{ textAlign: "right", fontSize: "0.85rem" }}>
            <strong style={{ display: "block" }}>{nome}</strong>
            <button className="btn-sair" onClick={aoSair}>Sair</button>
          </div>
        )}
      </div>
      <div className="topo-interno" style={{ paddingTop: 6 }}>
        <Tagline />
      </div>
    </header>
  );
}
