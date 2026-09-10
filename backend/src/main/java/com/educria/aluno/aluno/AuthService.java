package com.educria.aluno.aluno;

import com.educria.aluno.aluno.CadastroRequest;
import com.educria.aluno.aluno.CadastroResponse;
import com.educria.aluno.aluno.LoginRequest;
import com.educria.aluno.aluno.LoginResponse;
import com.educria.aluno.aluno.StatusCadastroResponse;
import com.educria.aluno.aluno.Aluno;
import com.educria.aluno.aluno.Role;
import com.educria.aluno.exception.CredenciaisInvalidasException;
import com.educria.aluno.exception.RecursoConflitanteException;
import com.educria.aluno.exception.RecursoNaoEncontradoException;
import com.educria.aluno.aluno.AlunoRepository;
import com.educria.aluno.security.AlunoUserDetails;
import com.educria.aluno.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AlunoRepository alunoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AlunoRepository alunoRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.alunoRepository = alunoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Aluno aluno = alunoRepository.findByMatricula(request.matricula())
                .orElseThrow(() -> new CredenciaisInvalidasException("Matricula ou senha invalidos"));

        if (aluno.getSenhaHash() == null || !passwordEncoder.matches(request.senha(), aluno.getSenhaHash())) {
            throw new CredenciaisInvalidasException("Matricula ou senha invalidos");
        }

        String token = jwtService.gerarToken(new AlunoUserDetails(aluno), aluno.getId());
        return new LoginResponse(token, aluno.getId(), aluno.getNome());
    }

    /** Cadastro inicial: so nome+email. Senha, matricula e turma ficam nulas ate a coordenacao aprovar. */
    public CadastroResponse cadastrar(CadastroRequest request) {
        if (alunoRepository.findByEmail(request.email()).isPresent()) {
            throw new RecursoConflitanteException("Ja existe cadastro com este email");
        }

        Aluno aluno = alunoRepository.save(Aluno.builder()
                .nome(request.nome())
                .email(request.email())
                .role(Role.ALUNO)
                .build());

        return new CadastroResponse(aluno.getId(), aluno.getNome(), aluno.getEmail());
    }

    /** Para a tela de espera checar se a coordenacao ja atribuiu uma turma, sem pedir senha. */
    public StatusCadastroResponse status(String email) {
        Aluno aluno = alunoRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cadastro nao encontrado: " + email));

        boolean aprovado = aluno.getTurma() != null;
        return new StatusCadastroResponse(
                aluno.getNome(), aprovado, aluno.getMatricula(), aprovado ? aluno.getTurma().getNome() : null);
    }
}
