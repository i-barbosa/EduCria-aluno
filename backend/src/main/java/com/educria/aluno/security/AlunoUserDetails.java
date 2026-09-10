package com.educria.aluno.security;

import com.educria.aluno.aluno.Aluno;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AlunoUserDetails implements UserDetails {

    private final Aluno aluno;

    public AlunoUserDetails(Aluno aluno) {
        this.aluno = aluno;
    }

    public Aluno getAluno() {
        return aluno;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + aluno.getRole().name()));
    }

    @Override
    public String getPassword() {
        return aluno.getSenhaHash();
    }

    @Override
    public String getUsername() {
        return aluno.getMatricula();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
