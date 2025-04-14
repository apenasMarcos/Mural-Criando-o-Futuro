package br.com.mural.criando.futuro.controller;

import br.com.mural.criando.futuro.model.postagem.Postagem;
import br.com.mural.criando.futuro.service.PostagemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Controller
public class PostagemController {

    private final PostagemService postagemService;

    public PostagemController(PostagemService postagemService) {
        this.postagemService = postagemService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("loginError", "Usuário ou senha incorretos. Tente novamente.");
        }
        return "login";
    }

    @GetMapping("/gerenciar-postagens")
    public String gerenciarPostagens() {
        return "postagens/gerenciarPostagens";
    }

    @GetMapping("/criar-postagem")
    public String criarPostagem() {
        return "postagens/criarPostagem";
    }

    @PostMapping("/postar-postagem")
    public String postarNoticia(@RequestParam("titulo") String titulo,
                                @RequestParam("autor") String autor,
                                @RequestParam("postagem") String texto,
                                @RequestParam(value = "imagens", required = false) MultipartFile[] imagens) {
        postagemService.criarNovaPostagem(titulo, autor, texto, imagens);
        return "redirect:/";
    }

    @GetMapping("/editar-postagens")
    public String editarPostagens(Model model) {
        model.addAttribute("postagens", postagemService.getAllPostagensAbreviadas());
        return "postagens/editarPostagens";
    }

    @GetMapping("/editar-postagem/{id}")
    public String getPostagem(@PathVariable(value = "id") Long id,Model model) {
        Optional<Postagem> postagemOpt = postagemService.getPostagemById(id);
        postagemOpt.ifPresent(postagem -> model.addAttribute("postagem", postagem));
        return "postagens/editarPostagem";
    }

    @GetMapping("/excluir-postagens")
    public String excluirPostagens(Model model) {
        model.addAttribute("postagens", postagemService.getAllPostagensAbreviadas());
        return "postagens/excluirPostagens";
    }

    @PostMapping("/excluir-postagem/{id}")
    public String excluirPostagem(@PathVariable long id) {
        postagemService.deletePostagem(id);
        return "redirect:/excluir-postagens";
    }
}

