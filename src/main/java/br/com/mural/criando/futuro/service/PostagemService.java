package br.com.mural.criando.futuro.service;

import br.com.mural.criando.futuro.model.postagem.Postagem;
import br.com.mural.criando.futuro.model.postagem.PostagemAbreviada;
import br.com.mural.criando.futuro.model.postagem.PostagemTitulo;
import br.com.mural.criando.futuro.repository.PostagemRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PostagemService {

    private static final Logger logger = LoggerFactory.getLogger(PostagemService.class);
    private final PostagemRepository postagemRepository;
    private final ImgurService imgurService;
    private final Cache<String, Page<PostagemAbreviada>> cachePostagensAbreviadas;


    public PostagemService(PostagemRepository postagemRepository, ImgurService imgurService) {
        this.postagemRepository = postagemRepository;
        this.imgurService = imgurService;
        this.cachePostagensAbreviadas = Caffeine.newBuilder()
                .expireAfterWrite(7, TimeUnit.DAYS)
                .maximumSize(1000)
                .build();
    }

    public List<PostagemAbreviada> getAllPostagensAbreviadas() {
        List<PostagemAbreviada> postagensAbreviadas = postagemRepository.findAllPostagensAbreviadas();
        carregarImagensParaPostagens(postagensAbreviadas);
        return postagensAbreviadas;
    }

    public Optional<Postagem> getPostagemById(Long id) {
        return postagemRepository.findById(id);
    }


    public void deletePostagem(Long id) {
        postagemRepository.deleteById(id);
        cachePostagensAbreviadas.invalidateAll();
    }

    public Page<PostagemAbreviada> getPostagensAbreviadasPage(int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(pagina, tamanho);

        String cacheKey = String.format("postagens_page_%d_size_%d", pagina, tamanho);
        Page<PostagemAbreviada> postagensAbreviadas = cachePostagensAbreviadas.getIfPresent(cacheKey);

        if (postagensAbreviadas == null) {
            postagensAbreviadas = postagemRepository.findAllPostagensAbreviadas(pageable);
            carregarImagensParaPostagens(postagensAbreviadas.getContent());
            cachePostagensAbreviadas.put(cacheKey, postagensAbreviadas);
        }
        return postagensAbreviadas;
    }

    public List<PostagemTitulo> getUltimasPostagens(int limite) {
        Pageable pageable = PageRequest.of(0, limite, Sort.by(Sort.Order.desc("dataPublicacao")));
        return postagemRepository.findAllByOrderByDataPublicacaoDesc(pageable);
    }

    private void carregarImagensParaPostagens(List<PostagemAbreviada> postagensAbreviadas) {
        for (PostagemAbreviada postagem : postagensAbreviadas) {
            List<String> imagens = postagemRepository.findImagensByPostagemId(postagem.getId());
            if (!imagens.isEmpty()) {
                postagem.setImagem(imagens.get(0));
            }
        }
    }

    public void criarNovaPostagem(String titulo, String autor, String texto, MultipartFile[] imagens) {
        Postagem postagem = new Postagem(titulo, texto, autor);

        if (imagens != null && Arrays.stream(imagens).anyMatch(imagem -> !Objects.requireNonNull(imagem.getOriginalFilename()).isEmpty())) {
            List<String> urlsImagens = Arrays.stream(imagens)
                    .map(this::uploadImageSafely)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            logger.info("URLs das imagens carregadas: {}", urlsImagens);

            postagem.setImagens(urlsImagens);
        }

        postagemRepository.save(postagem);
        cachePostagensAbreviadas.invalidateAll();
    }

    private String uploadImageSafely(MultipartFile imagem) {
        try {
            return imgurService.uploadImageWithRetry(imagem);
        } catch (Exception e) {
            logger.error("Erro ao fazer upload da imagem: {}", imagem.getOriginalFilename(), e);
            return null;
        }
    }
}
