package br.com.mural.criando.futuro.service;

import br.com.mural.criando.futuro.model.cardapio.SemanaDTO;
import br.com.mural.criando.futuro.model.cardapio.Semana;


import br.com.mural.criando.futuro.model.cardapio.enums.SemanaTipo;
import br.com.mural.criando.futuro.repository.SemanaRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class SemanaService {

    private final SemanaRepository semanaRepository;
    private final Cache<String, SemanaDTO> cacheSemanaAtual;

    public SemanaService(SemanaRepository semanaRepository) {
        this.semanaRepository = semanaRepository;
        this.cacheSemanaAtual = Caffeine.newBuilder()
                .maximumSize(1)  // Mantém apenas o cache para a semana atual
                .build();
    }

    public SemanaDTO getSemanaAtual() {
        String cacheKey = "semana_atual";
        SemanaDTO semanaDTO = cacheSemanaAtual.getIfPresent(cacheKey);

        if (semanaDTO != null) {
            LocalDate dataAtual = LocalDate.now();
            SemanaTipo semanaAtual = SemanaTipo.values()[(dataAtual.getDayOfYear() - 1) / 7 % 4];
            if(semanaAtual != semanaDTO.getSemana().getTipo()) {
                semanaDTO = atualizarCacheSemana();
            }
            return semanaDTO;
        }
        semanaDTO = atualizarCacheSemana();
        return semanaDTO;
    }

    @Scheduled(cron = "0 0 0 * * MON") // Executa às 00:00 toda segunda-feira
    public SemanaDTO atualizarCacheSemana() {
        LocalDate dataAtual = LocalDate.now();
        LocalDate inicioSemana = dataAtual.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate fimSemana = inicioSemana.plusDays(6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        SemanaTipo semanaAtual = SemanaTipo.values()[(dataAtual.getDayOfYear() - 1) / 7 % 4];

        String inicioFormatado = inicioSemana.format(formatter);
        String fimFormatado = fimSemana.format(formatter);
        Optional<Semana> semanaOpt = semanaRepository.findByTipo(semanaAtual);

        if (semanaOpt.isPresent()) {
            Semana semana = semanaOpt.get();
            SemanaDTO semanaDTO = new SemanaDTO(semana, inicioFormatado, fimFormatado);
            // Atualiza o cache
            cacheSemanaAtual.put("semana_atual", semanaDTO);
            return semanaDTO;
        } else {
            throw new RuntimeException("Semana não encontrada no banco de dados.");
        }
    }
}

