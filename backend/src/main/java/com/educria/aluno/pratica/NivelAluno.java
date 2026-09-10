package com.educria.aluno.pratica;

import com.educria.aluno.aluno.Aluno;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "nivel_aluno", uniqueConstraints = @UniqueConstraint(columnNames = {"aluno_id", "assunto_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NivelAluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assunto_id", nullable = false)
    private Assunto assunto;

    @Column(name = "nivel_atual", nullable = false)
    @Builder.Default
    private Integer nivelAtual = 1;

    @Column(name = "acertos_seguidos", nullable = false)
    @Builder.Default
    private Integer acertosSeguidos = 0;

    @Column(name = "erros_seguidos", nullable = false)
    @Builder.Default
    private Integer errosSeguidos = 0;

    @Column(name = "sequencia_acertos_global", nullable = false)
    @Builder.Default
    private Integer sequenciaAcertosGlobal = 0;
}
