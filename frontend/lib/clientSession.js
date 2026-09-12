// Factory unica pra sessao de cliente sobre localStorage - aluno, admin e cadastro pendente
// usam o mesmo padrao de salvar/ler/encerrar, so muda a chave.

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
