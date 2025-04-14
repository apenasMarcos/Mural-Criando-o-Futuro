package br.com.mural.criando.futuro.model.cardapio;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.GenerationType;
import jakarta.persistence.EnumType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Dia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "semana_id")
    private Semana semana;

    @Enumerated(EnumType.STRING)
    private DiaSemana diaSemana;

    @OneToOne
    @JoinColumn(name = "cardapio_parcial_id")
    private Cardapio cardapioParcial;

    @OneToOne
    @JoinColumn(name = "cardapio_integral_id")
    private Cardapio cardapioIntegral;
}

