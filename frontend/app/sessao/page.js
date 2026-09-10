import { Suspense } from "react";
import SessaoConteudo from "./SessaoConteudo";

export default function PaginaSessao() {
  return (
    <Suspense fallback={null}>
      <SessaoConteudo />
    </Suspense>
  );
}
