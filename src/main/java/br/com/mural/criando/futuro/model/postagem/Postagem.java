package br.com.mural.criando.futuro.model.postagem;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.GenerationType;
import jakarta.persistence.PrePersist;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Postagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titulo;
    @Column(name = "texto", columnDefinition = "TEXT")
    private String texto;
    private String autor;
    private LocalDate dataPublicacao;
    @ElementCollection
    @CollectionTable(
            name = "postagem_imagens",
            joinColumns = @JoinColumn(name = "postagem_id")
    )
    @Column(name = "url_imagem")
    @Cascade(CascadeType.REMOVE)
    private List<String> imagens;

    public Postagem(String titulo, String texto, String autor) {
        this.titulo = titulo;
        this.texto = texto;
        this.autor = autor;
    }

    public String getTextoComoHtml() {
        if (this.texto != null && !this.texto.isEmpty()) {
            Parser parser = Parser.builder().build();
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            return renderer.render(parser.parse(this.texto));
        }
        return "";
    }

    public String getDataPublicacaoFormatada() {
        if (dataPublicacao != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
            return dataPublicacao.format(formatter);
        }
        return "";
    }

    @PrePersist
    public void prePersist() {
        if (dataPublicacao == null) {
            dataPublicacao = LocalDate.now();
        }
    }
}
