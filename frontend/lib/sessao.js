// Sessao do aluno (token JWT + dados basicos) guardada em localStorage.

const CHAVE_SESSAO = "educria_aluno_sessao";
const CHAVE_CADASTRO = "educria_aluno_cadastro_pendente";

export function salvarSessao({ token, alunoId, nome }) {
  const dados = { token, alunoId, nome };
  window.localStorage.setItem(CHAVE_SESSAO, JSON.stringify(dados));
  return dados;
}

export function lerSessao() {
  if (typeof window === "undefined") return null;
  const bruto = window.localStorage.getItem(CHAVE_SESSAO);
  if (!bruto) return null;
  try {
    return JSON.parse(bruto);
  } catch {
    return null;
  }
}

export function encerrarSessao() {
  window.localStorage.removeItem(CHAVE_SESSAO);
}

// Cadastro pendente (antes da aprovacao) - so guarda nome/email pra tela /aguardando poder
// consultar o status sem pedir de novo.
export function salvarCadastroPendente({ nome, email }) {
  window.localStorage.setItem(CHAVE_CADASTRO, JSON.stringify({ nome, email }));
}

export function lerCadastroPendente() {
  if (typeof window === "undefined") return null;
  const bruto = window.localStorage.getItem(CHAVE_CADASTRO);
  if (!bruto) return null;
  try {
    return JSON.parse(bruto);
  } catch {
    return null;
  }
}

export function encerrarCadastroPendente() {
  window.localStorage.removeItem(CHAVE_CADASTRO);
}
