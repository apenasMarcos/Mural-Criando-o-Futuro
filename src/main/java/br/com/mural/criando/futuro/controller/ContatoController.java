package br.com.mural.criando.futuro.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/contato")
public class ContatoController {

    private static final Logger logger = LoggerFactory.getLogger(ContatoController.class);

    @GetMapping
    public String enviarEmail() {
        try {
            logger.info("Mensagem encaminhada com sucesso.");
            return "Mensagem encaminhada com sucesso!";
        } catch (Exception e) {
            logger.error("Erro ao encaminhar mensagem: ", e);
            return "Erro ao encaminhar mensagem.";
        }
    }
}

