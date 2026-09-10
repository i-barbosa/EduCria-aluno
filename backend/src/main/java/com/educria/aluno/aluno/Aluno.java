package com.educria.aluno.aluno;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "aluno")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    /** Nula ate a coordenacao aprovar o cadastro - e ai que uma senha temporaria e gerada. */
    @Column(name = "senha_hash")
    private String senhaHash;

    /** Nula ate a coordenacao atribuir o aluno a uma turma - e ai que a matricula e gerada. */
    @Column(unique = true)
    private String matricula;

    /** Nula ate a coordenacao atribuir uma turma. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id")
    private Turma turma;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.ALUNO;
}
