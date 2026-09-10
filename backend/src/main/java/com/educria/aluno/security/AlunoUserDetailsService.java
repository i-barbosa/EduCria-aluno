package com.educria.aluno.security;

import com.educria.aluno.aluno.AlunoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AlunoUserDetailsService implements UserDetailsService {

    private final AlunoRepository alunoRepository;

    public AlunoUserDetailsService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String matricula) throws UsernameNotFoundException {
        return alunoRepository.findByMatricula(matricula)
                .map(AlunoUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Aluno nao encontrado: " + matricula));
    }
}
