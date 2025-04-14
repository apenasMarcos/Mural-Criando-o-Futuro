package br.com.mural.criando.futuro.model.cardapio;

import br.com.mural.criando.futuro.model.cardapio.enums.SemanaTipo;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.GenerationType;
import jakarta.persistence.EnumType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Semana {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private SemanaTipo tipo;

    @OneToMany(mappedBy = "semana")
    private List<Dia> dias;
}

