package com.educria.aluno.pratica;

import com.educria.aluno.aluno.Aluno;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "repeticao_agendada", uniqueConstraints = @UniqueConstraint(columnNames = {"aluno_id", "questao_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepeticaoAgendada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "questao_id", nullable = false)
    private Questao questao;

    @Column(name = "indice_sequencia", nullable = false)
    @Builder.Default
    private Integer indiceSequencia = 0;

    @Column(name = "proxima_data", nullable = false)
    private LocalDate proximaData;
}
