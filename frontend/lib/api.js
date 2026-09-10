const BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export class ErroApi extends Error {}

async function tratar(resposta, mensagem) {
  const texto = await resposta.text();
  const corpo = texto ? JSON.parse(texto) : null;
  if (!resposta.ok) {
    throw new ErroApi(corpo?.erro ?? `${mensagem} (status ${resposta.status})`);
  }
  return corpo;
}

/* ---------- autenticacao do aluno ---------- */

export async function login(matricula, senha) {
  const resposta = await fetch(`${BASE_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ matricula, senha }),
  });
  if (resposta.status === 401) return null;
  return tratar(resposta, "Falha ao entrar");
}

export async function cadastrar(nome, email) {
  const resposta = await fetch(`${BASE_URL}/auth/cadastro`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ nome, email }),
  });
  if (resposta.status === 409) return { conflito: true };
  return { conflito: false, aluno: await tratar(resposta, "Falha ao cadastrar") };
}

export function buscarStatus(email) {
  return fetch(`${BASE_URL}/auth/status?email=${encodeURIComponent(email)}`).then((r) =>
    tratar(r, "Falha ao consultar status")
  );
}

/* ---------- assuntos, progresso, sessao ---------- */

function autorizado(token) {
  return { Authorization: `Bearer ${token}` };
}

export function buscarAssuntos(token) {
  return fetch(`${BASE_URL}/assuntos`, { headers: autorizado(token) }).then((r) =>
    tratar(r, "Falha ao carregar materias")
  );
}

export function buscarProgresso(token, alunoId) {
  return fetch(`${BASE_URL}/alunos/${alunoId}/progresso`, { headers: autorizado(token) }).then((r) =>
    tratar(r, "Falha ao carregar progresso")
  );
}

export function buscarProximaQuestao(token, alunoId, assuntoId) {
  return fetch(`${BASE_URL}/alunos/${alunoId}/proxima-questao?assunto=${assuntoId}`, {
    headers: autorizado(token),
  }).then((r) => tratar(r, "Falha ao buscar questao"));
}

export function responderTentativa(token, alunoId, questaoId, alternativaEscolhidaId) {
  return fetch(`${BASE_URL}/alunos/${alunoId}/tentativas`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...autorizado(token) },
    body: JSON.stringify({ questaoId, alternativaEscolhidaId }),
  }).then((r) => tratar(r, "Falha ao responder"));
}

/* ---------- coordenacao (admin) ---------- */

export async function loginAdmin(senha) {
  const resposta = await fetch(`${BASE_URL}/admin/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ senha }),
  });
  if (resposta.status === 401) return null;
  return tratar(resposta, "Falha ao entrar");
}

export function buscarAlunosPendentes(tokenAdmin) {
  return fetch(`${BASE_URL}/admin/alunos-pendentes`, { headers: autorizado(tokenAdmin) }).then((r) =>
    tratar(r, "Falha ao listar alunos pendentes")
  );
}

export function buscarTurmas(tokenAdmin) {
  return fetch(`${BASE_URL}/admin/turmas`, { headers: autorizado(tokenAdmin) }).then((r) =>
    tratar(r, "Falha ao listar turmas")
  );
}

export async function criarTurma(tokenAdmin, nome) {
  const resposta = await fetch(`${BASE_URL}/admin/turmas`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...autorizado(tokenAdmin) },
    body: JSON.stringify({ nome }),
  });
  if (resposta.status === 409) return { conflito: true };
  return { conflito: false, turma: await tratar(resposta, "Falha ao criar turma") };
}

export function aprovarAluno(tokenAdmin, alunoId, turmaId) {
  return fetch(`${BASE_URL}/admin/alunos/${alunoId}/aprovar`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...autorizado(tokenAdmin) },
    body: JSON.stringify({ turmaId }),
  }).then((r) => tratar(r, "Falha ao aprovar aluno"));
}
