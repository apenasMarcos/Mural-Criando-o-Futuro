package br.com.mural.criando.futuro.controller;

import br.com.mural.criando.futuro.service.PageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class PageController {

    private final PageService pageService;

    public PageController(PageService pageService) {
        this.pageService = pageService;
    }

    @GetMapping("/")
    public String index(@RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "10") int size,
                        Model model) {
        return pageService.carregarDadosIndex(page, size, model);
    }

    @GetMapping("/postagem/{id}")
    public String postagem(@PathVariable(value = "id") Long id, Model model) {
        return pageService.carregarDadosPostagem(id, model);
    }

    @GetMapping("/cardapio")
    public String cardapio(Model model) {
        return pageService.carregarDadosCardapio(model);
    }

    @GetMapping("/informacoes")
    public String informacoes() {
        return "principais/informacoes";
    }

    @GetMapping("/contato")
    public String contato() {
        return "principais/contato";
    }
}
