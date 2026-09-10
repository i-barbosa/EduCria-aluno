// Sessao da coordenacao (token JWT com papel ADMIN), separada da sessao do aluno.

const CHAVE_ADMIN = "educria_admin_sessao";

export function salvarSessaoAdmin(token) {
  window.localStorage.setItem(CHAVE_ADMIN, token);
}

export function lerSessaoAdmin() {
  if (typeof window === "undefined") return null;
  return window.localStorage.getItem(CHAVE_ADMIN);
}

export function encerrarSessaoAdmin() {
  window.localStorage.removeItem(CHAVE_ADMIN);
}
