package com.educria.aluno.pratica;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Questao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assunto_id", nullable = false)
    private Assunto assunto;

    @Column(nullable = false)
    private Integer nivel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String enunciado;

    @OneToMany(mappedBy = "questao", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Alternativa> alternativas = new ArrayList<>();
}
