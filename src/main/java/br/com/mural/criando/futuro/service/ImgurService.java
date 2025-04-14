package br.com.mural.criando.futuro.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Service
public class ImgurService {

    private static final Logger logger = LoggerFactory.getLogger(ImgurService.class);
    private static final String IMGUR_CLIENT_ID = "151c60fcb07a72e";

    public String uploadImage(MultipartFile file) throws Exception {
        String url = "https://api.imgur.com/3/image";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Client-ID " + IMGUR_CLIENT_ID);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Define o recurso da imagem com nome e tipo correto
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ContentDisposition contentDisposition = ContentDisposition
                .builder("form-data")
                .name("image")
                .filename(file.getOriginalFilename())
                .build();

        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentDisposition(contentDisposition);
        fileHeaders.setContentType(MediaType.parseMediaType(Objects.requireNonNull(file.getContentType())));

        HttpEntity<Resource> fileEntity = new HttpEntity<>(
                new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename(); // necessário para o Content-Disposition
                    }
                }, fileHeaders
        );

        body.add("image", fileEntity);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        logger.info("Enviando imagem: {}", file.getOriginalFilename());

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            return jsonResponse.path("data").path("link").asText();
        } else {
            logger.error("Resposta da API do Imgur: {}", response.getBody());
            throw new Exception("Erro no upload da imagem.");
        }
    }


    public String uploadImageWithRetry(MultipartFile file) {
        int retries = 3;
        for (int i = 0; i < retries; i++) {
            try {
                return uploadImage(file);
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    logger.warn("Tentativa {} falhou. Retentando...", i + 1);
                    try {
                        Thread.sleep((long) Math.pow(2, i) * 1000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    throw e;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalStateException("Falha no upload após várias tentativas");
    }

}
