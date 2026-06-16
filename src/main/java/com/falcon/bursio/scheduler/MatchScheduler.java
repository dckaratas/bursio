package com.falcon.bursio.scheduler;

import com.falcon.bursio.entity.MatchRequest;
import com.falcon.bursio.entity.enums.MatchStatus;
import com.falcon.bursio.repository.MatchRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchScheduler {

    private final MatchRequestRepository matchRequestRepository;

    @Scheduled(cron = "0 0 * * * *") // her saat başı çalışır
    @Transactional
    public void expireOldMatches() {
        List<MatchRequest> expired = matchRequestRepository
                .findExpiredMatches(LocalDateTime.now());

        if (expired.isEmpty()) return;

        expired.forEach(match -> match.setStatus(MatchStatus.EXPIRED));
        matchRequestRepository.saveAll(expired);

        log.info("{} adet eşleşme talebi EXPIRED yapıldı.", expired.size());
    }
}