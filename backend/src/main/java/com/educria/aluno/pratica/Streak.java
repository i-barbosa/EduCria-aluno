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
@Table(name = "streak")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Streak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false, unique = true)
    private Aluno aluno;

    @Column(name = "dias_seguidos", nullable = false)
    @Builder.Default
    private Integer diasSeguidos = 0;

    @Column(name = "ultima_data_pratica")
    private LocalDate ultimaDataPratica;
}
