package com.educria.aluno.admin;

import com.educria.aluno.aluno.Aluno;
import com.educria.aluno.aluno.Turma;
import com.educria.aluno.exception.CredenciaisInvalidasException;
import com.educria.aluno.exception.RecursoNaoEncontradoException;
import com.educria.aluno.aluno.AlunoRepository;
import com.educria.aluno.aluno.TurmaRepository;
import com.educria.aluno.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Painel da coordenacao: cria turma, ve cadastros pendentes e aprova (gera matricula + senha
 * temporaria, hasheada com BCrypt antes de salvar, e manda por e-mail).
 */
@RestController
@RequestMapping("/admin")
@Tag(name = "Coordenacao")
public class AdminController {

    private final AlunoRepository alunoRepository;
    private final TurmaRepository turmaRepository;
    private final GeradorMatricula geradorMatricula;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String senhaAdmin;

    public AdminController(AlunoRepository alunoRepository,
                            TurmaRepository turmaRepository,
                            GeradorMatricula geradorMatricula,
                            EmailService emailService,
                            PasswordEncoder passwordEncoder,
                            JwtService jwtService,
                            @Value("${educria.admin.senha}") String senhaAdmin) {
        this.alunoRepository = alunoRepository;
        this.turmaRepository = turmaRepository;
        this.geradorMatricula = geradorMatricula;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.senhaAdmin = senhaAdmin;
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica a coordenacao e retorna JWT com papel ADMIN")
    public ResponseEntity<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest requisicao) {
        if (!senhaAdmin.equals(requisicao.senha())) {
            throw new CredenciaisInvalidasException("Senha invalida");
        }
        return ResponseEntity.ok(new AdminLoginResponse(jwtService.gerarTokenAdmin()));
    }

    @GetMapping("/alunos-pendentes")
    @Operation(summary = "Lista cadastros aguardando aprovacao (sem turma atribuida)")
    public List<AlunoPendenteDto> alunosPendentes() {
        return alunoRepository.findByTurmaIsNull().stream()
                .map(a -> new AlunoPendenteDto(a.getId(), a.getNome(), a.getEmail()))
                .toList();
    }

    @GetMapping("/turmas")
    @Operation(summary = "Lista turmas existentes")
    public List<TurmaDto> turmas() {
        List<Aluno> todosAlunos = alunoRepository.findAll();
        return turmaRepository.findAll().stream()
                .map(t -> new TurmaDto(t.getId(), t.getNome(), (int) todosAlunos.stream()
                        .filter(a -> a.getTurma() != null && a.getTurma().getId().equals(t.getId()))
                        .count()))
                .toList();
    }

    @PostMapping("/turmas")
    @Operation(summary = "Cria uma nova turma")
    public ResponseEntity<TurmaDto> criarTurma(@Valid @RequestBody CriarTurmaRequest requisicao) {
        if (turmaRepository.findByNome(requisicao.nome()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Turma turma = turmaRepository.save(Turma.builder().nome(requisicao.nome()).build());
        return ResponseEntity.status(HttpStatus.CREATED).body(new TurmaDto(turma.getId(), turma.getNome(), 0));
    }

    /** Aprova o cadastro: define a turma e gera matricula + senha temporaria de uma vez. */
    @PostMapping("/alunos/{id}/aprovar")
    @Operation(summary = "Aprova um cadastro pendente, atribuindo turma e gerando matricula/senha")
    public AlunoAtribuidoDto aprovar(@PathVariable Long id, @Valid @RequestBody AtribuirTurmaRequest requisicao) {
        Aluno aluno = alunoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno nao encontrado: " + id));
        Turma turma = turmaRepository.findById(requisicao.turmaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Turma nao encontrada: " + requisicao.turmaId()));

        String senhaGerada = GeradorSenha.gerar();
        aluno.setTurma(turma);
        aluno.setMatricula(geradorMatricula.gerar(turma));
        aluno.setSenhaHash(passwordEncoder.encode(senhaGerada));
        alunoRepository.save(aluno);

        boolean emailEnviado = emailService.enviarCredenciais(
                aluno.getEmail(), aluno.getNome(), aluno.getMatricula(), senhaGerada, turma.getNome());

        return new AlunoAtribuidoDto(
                aluno.getId(), aluno.getNome(), aluno.getEmail(), aluno.getMatricula(), turma.getNome(),
                senhaGerada, emailEnviado);
    }
}
