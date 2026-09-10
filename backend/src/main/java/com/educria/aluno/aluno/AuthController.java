package com.educria.aluno.aluno;

import com.educria.aluno.aluno.CadastroRequest;
import com.educria.aluno.aluno.CadastroResponse;
import com.educria.aluno.aluno.LoginRequest;
import com.educria.aluno.aluno.LoginResponse;
import com.educria.aluno.aluno.StatusCadastroResponse;
import com.educria.aluno.aluno.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticacao")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica aluno (matricula + senha) e retorna JWT")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/cadastro")
    @Operation(summary = "Cria um cadastro pendente (nome + email), aguardando aprovacao da coordenacao")
    public ResponseEntity<CadastroResponse> cadastro(@Valid @RequestBody CadastroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.cadastrar(request));
    }

    @GetMapping("/status")
    @Operation(summary = "Consulta se um cadastro ja foi aprovado (turma atribuida), sem expor senha")
    public ResponseEntity<StatusCadastroResponse> status(@RequestParam String email) {
        return ResponseEntity.ok(authService.status(email));
    }
}
