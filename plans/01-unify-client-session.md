# Unificar sessão de cliente (`sessao.js` + `adminSessao.js` → `clientSession.js`)

## Contexto

`frontend/lib/sessao.js` e `frontend/lib/adminSessao.js` implementam a mesma coisa três vezes:
ler/escrever/apagar um blob JSON em `window.localStorage` sob uma chave fixa, com a mesma
guarda `typeof window === "undefined"` repetida em cada função de leitura. `sessao.js` sozinho
já duplica esse padrão duas vezes internamente (sessão do aluno + cadastro pendente).

**Nota sobre as fontes citadas no pedido original**: os arquivos `PATHFINDER-2026-09-11/*`
(flowchart, relatório de duplicação, proposta unificada) não existem neste checkout — busca
completa no repo não encontrou nada com esse nome. Este plano foi montado lendo o código-fonte
real (`sessao.js`, `adminSessao.js` e os 6 call sites, greppados com números de linha) em vez de
confiar nas referências. O desenho do componente e a lista de call sites do pedido batem
exatamente com o que o grep encontrou, então a especificação em si é tratada como válida —
só a proveniência dos documentos é que não pôde ser verificada.

## Componente alvo

Uma factory `criarSessao(chave)` que devolve um objeto com 3 métodos (`salvar`, `ler`,
`encerrar`) sobre `localStorage`. `clientSession.js` exporta a factory e três instâncias
prontas — não um `SessionManager` genérico, não uma classe, não mais que isso:

```js
// frontend/lib/clientSession.js
export function criarSessao(chave) {
  return {
    salvar(dados) {
      window.localStorage.setItem(chave, JSON.stringify(dados));
      return dados;
    },
    ler() {
      if (typeof window === "undefined") return null;
      const bruto = window.localStorage.getItem(chave);
      if (!bruto) return null;
      try {
        return JSON.parse(bruto);
      } catch {
        return null;
      }
    },
    encerrar() {
      window.localStorage.removeItem(chave);
    },
  };
}

export const sessaoAluno = criarSessao("educria_aluno_sessao");
export const sessaoAdmin = criarSessao("educria_admin_sessao");
export const cadastroPendente = criarSessao("educria_aluno_cadastro_pendente");
```

**Mudança de formato a notar**: `adminSessao.js` guardava o token como string crua
(`localStorage.setItem(CHAVE_ADMIN, token)`); a versão unificada sempre serializa em JSON
(`salvar({ token })` / `ler()?.token`), igual às outras duas sessões. Isso zera qualquer
sessão de admin já salva no navegador de quem estiver testando local — aceitável, é só forçar
login de novo.

## Anti-padrões a evitar (guarda explícita do pedido)

- Nada de `SessionManager` genérico/classe/config extra.
- Não manter `sessao.js`/`adminSessao.js` por trás de flag ou como wrapper fino chamando
  `clientSession.js` — eles são **deletados** na Fase 3, não preservados.
- Não adicionar abstração além dos 3 métodos da factory (sem `merge`, sem `subscribe`, sem
  namespace por ambiente).

## Fase 1 — Criar `frontend/lib/clientSession.js`

Criar o arquivo com o conteúdo exato da seção "Componente alvo" acima.

**Verificação**: `node --check frontend/lib/clientSession.js` (ou `npm run build` do frontend,
que já falha se o arquivo tiver erro de sintaxe) — sem call sites apontando pra ele ainda,
então não há mais o que testar nesta fase.

## Fase 2 — Reescrever os 6 call sites

Trocar import + chamadas em cada arquivo. Estado atual confirmado por grep (linhas exatas):

### `frontend/app/page.js`
- Linha 6: `import { salvarSessao } from "@/lib/sessao";` → `import { sessaoAluno } from "@/lib/clientSession";`
- Linha 27: `salvarSessao(resultado);` → `sessaoAluno.salvar(resultado);`

### `frontend/app/inicio/page.js`
- Linha 6: `import { encerrarSessao, lerSessao } from "@/lib/sessao";` → `import { sessaoAluno } from "@/lib/clientSession";`
- Linha 20: `const atual = lerSessao();` → `const atual = sessaoAluno.ler();`
- Linha 44: `encerrarSessao();` → `sessaoAluno.encerrar();`

### `frontend/app/sessao/SessaoConteudo.js`
- Linha 6: `import { encerrarSessao, lerSessao } from "@/lib/sessao";` → `import { sessaoAluno } from "@/lib/clientSession";`
- Linha 35: `const atual = lerSessao();` → `const atual = sessaoAluno.ler();`
- Linha 104: `encerrarSessao();` → `sessaoAluno.encerrar();`

### `frontend/app/admin/page.js`
- Linha 11: `import { encerrarSessaoAdmin, lerSessaoAdmin, salvarSessaoAdmin } from "@/lib/adminSessao";` → `import { sessaoAdmin } from "@/lib/clientSession";`
- Linha 35: `encerrarSessaoAdmin();` → `sessaoAdmin.encerrar();`
- Linha 40: `const token = lerSessaoAdmin();` → `const token = sessaoAdmin.ler()?.token;`
- Linha 61: `salvarSessaoAdmin(resultado.token);` → `sessaoAdmin.salvar({ token: resultado.token });`
- Linha 72: `encerrarSessaoAdmin();` → `sessaoAdmin.encerrar();`
- Linha 80: `const token = lerSessaoAdmin();` → `const token = sessaoAdmin.ler()?.token;`
- Linha 93: `const token = lerSessaoAdmin();` → `const token = sessaoAdmin.ler()?.token;`

### `frontend/app/cadastro/page.js`
- Linha 6: `import { salvarCadastroPendente } from "@/lib/sessao";` → `import { cadastroPendente } from "@/lib/clientSession";`
- Linha 27: `salvarCadastroPendente({ nome: resultado.aluno.nome, email: resultado.aluno.email });` → `cadastroPendente.salvar({ nome: resultado.aluno.nome, email: resultado.aluno.email });`

### `frontend/app/aguardando/page.js`
- Linha 6: `import { encerrarCadastroPendente, lerCadastroPendente } from "@/lib/sessao";` → `import { cadastroPendente } from "@/lib/clientSession";`
- Linha 18: `const atual = lerCadastroPendente();` → `const atual = cadastroPendente.ler();`
- Linha 45: `encerrarCadastroPendente();` → `cadastroPendente.encerrar();`
- Linha 50: `encerrarCadastroPendente();` → `cadastroPendente.encerrar();`

**Verificação desta fase**:
```bash
grep -rn "from \"@/lib/sessao\"\|from \"@/lib/adminSessao\"" frontend/app
```
deve retornar vazio antes de seguir pra Fase 3.

## Fase 3 — Apagar os arquivos antigos

```bash
rm frontend/lib/sessao.js frontend/lib/adminSessao.js
```

Não deixar nenhum dos dois como wrapper fino re-exportando `clientSession.js` — é remoção
completa.

## Verificação final

1. `grep -rln "sessao\.js\|adminSessao\.js" frontend` → só deve aparecer `clientSession.js`
   se ele mesmo citar o nome em comentário (não deveria); nenhum outro arquivo deve referenciar
   os módulos antigos.
2. `cd frontend && npm run build` → build limpo, confirma que todo import resolve e não sobrou
   `salvarSessao`/`lerSessaoAdmin`/etc. undefined.
3. Teste manual do fluxo (local, `npm run dev` + backend local): login de aluno → `/inicio` →
   sair → login de admin em `/admin` → aprovar aluno → sair. Cobre as 3 instâncias
   (`sessaoAluno`, `sessaoAdmin`, `cadastroPendente`) na prática.
4. `git status` deve mostrar exatamente: `clientSession.js` novo, `sessao.js` e
   `adminSessao.js` deletados, os 6 call sites modificados — nada mais.
