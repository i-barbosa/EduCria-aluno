# EDUCRIA — Sistema 1 (Aluno)

Plataforma de nivelamento adaptativo com gamificação individual para alunos do Ensino
Fundamental II. O aluno pratica questões de uma matéria e o sistema mede o nível dele
individualmente — nunca há comparação visível com colegas (sem ranking).

**No ar:**
- Frontend: https://educria-aluno.vercel.app
- Backend (API): https://educria-aluno.onrender.com

> O backend está no plano free do Render — a primeira chamada depois de ~15 min sem uso
> demora 30-50s pra "acordar". Normal, não é bug.

## Stack

| Camada | Tecnologia |
|---|---|
| Backend | Java 17, Spring Boot 4.1.1, Spring Security 7, Spring Data JPA, Hibernate |
| Banco | H2 em memória (dev) / PostgreSQL (produção) |
| Autenticação | JWT (jjwt) — dois papéis: `ALUNO` (login por matrícula) e `ADMIN` (painel coordenação) |
| Frontend | Next.js 16 (App Router), React 19 |
| Deploy | Render (backend, via Docker) + Vercel (frontend) |
| Testes | JUnit 5 + Mockito + AssertJ (motor adaptativo e repetição espaçada) |

## Como rodar local

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

Sobe em `localhost:8080` com H2 em memória. Um `ConteudoSeeder` popula 5 matérias
(JavaScript, CSS, HTML, C, Java — 50 questões cada) automaticamente no primeiro boot, e um
`DevDataSeeder` cria um aluno de teste:

```
matrícula: DEMO0001
senha: 123456
```

Painel de coordenação (`/admin`): senha padrão `admin123` (troque via env var `ADMIN_SENHA`
em produção).

Swagger: `localhost:8080/swagger-ui.html`

**Rodar com Postgres real** (via `docker-compose.yml`):
```bash
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

**Segredos locais** (chave da API do Brevo, pra enviar e-mail de credenciais):
```bash
cp backend/src/main/resources/application-secrets.properties.example backend/src/main/resources/application-secrets.properties
# edita e preenche educria.email.brevo-api-key / educria.email.remetente
```
Esse arquivo é ignorado pelo Git. Sem ele o backend funciona normal, só não manda e-mail.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Sobe em `localhost:3000`, já aponta pro backend em `localhost:8080`.

### Rodar os testes

```bash
cd backend
./mvnw test
```

## Estrutura do backend (organizado por feature)

```
com.educria.aluno/
├── aluno/       # Aluno, Turma, login/cadastro/status
├── pratica/     # motor adaptativo, repetição espaçada, gamificação, questões
│   └── seed/    # DTOs de leitura do questoes.json
├── admin/       # painel da coordenação (aprovação, matrícula, e-mail)
├── security/    # JWT (filtro, geração/validação de token)
├── config/      # Spring Security, seeders, propriedades, OpenAPI
└── exception/   # tratamento de erro HTTP
```

Cada pacote reúne entidade + repositório + serviço + controller + DTO daquele domínio — pra
achar tudo sobre "aluno" ou sobre "admin" você olha uma pasta só, não pula entre 5.

## Estrutura do frontend (por rota)

```
frontend/
├── app/
│   ├── page.js            # login (matrícula + senha)
│   ├── cadastro/page.js    # nome + email → aguardando aprovação
│   ├── aguardando/page.js  # poll de status até a coordenação aprovar
│   ├── inicio/page.js      # escolha de matéria + resumo de progresso
│   ├── sessao/             # prática (5 questões, escada de nível, resultado)
│   └── admin/page.js       # painel da coordenação
├── lib/                    # api.js (fetch helpers), sessao.js, adminSessao.js (localStorage)
└── components/             # Logo, Tagline, Cabeçalho
```

## Regras de negócio (resumo — ver documentação completa para detalhe)

- **Nível por matéria**: escala 1 a 10, independente entre matérias.
- **Sobe de nível**: 2 acertos seguidos no mesmo nível.
- **Desce de nível**: só no 2º erro seguido (1 erro isolado não desce nada — a IA nunca pune,
  só recalibra a dificuldade).
- **Repetição espaçada**: questão acertada volta em 1, 3, 7, 16, 35, 70 dias. Um erro na
  revisão reinicia a sequência.
- **Gamificação**: XP, badge por subida de nível, streak de dias seguidos, conquistas — tudo
  visível só para o próprio aluno, nunca comparado com colegas.
- **Fluxo de acesso**: aluno se cadastra (nome + email) → fica pendente → coordenação aprova
  no `/admin` e define a turma → sistema gera matrícula (prefixo da turma + 4 dígitos) e senha
  temporária (8 caracteres, sem `0/O/1/I/l`) → envia por e-mail (API Brevo) → aluno loga com
  matrícula + senha.

## Deploy

- **Backend (Render)**: build via `backend/Dockerfile` (multi-stage, JDK 21 build → JRE 21
  runtime). Variáveis de ambiente: `SPRING_PROFILES_ACTIVE=postgres`, `PGHOST`/`PGPORT`/
  `PGDATABASE`/`PGUSER`/`PGPASSWORD` (Postgres do Render), `ADMIN_SENHA`, `JWT_SECRET`,
  `BREVO_API_KEY`, `EMAIL_REMETENTE`, `CORS_ALLOWED_ORIGINS`.
- **Frontend (Vercel)**: Root Directory `frontend`, env var `NEXT_PUBLIC_API_URL` apontando
  pro backend do Render.

## Documentação completa

Ver `DOCUMENTACAO.md` (ou o artifact publicado) para o funcionamento detalhado de cada parte
do sistema, com diagramas de fluxo — pensado pra estudo e apresentação.
