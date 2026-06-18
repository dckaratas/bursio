package com.falcon.bursio.config;

import com.falcon.bursio.security.JwtService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Swagger'ı atla
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = resolveKey(request, path);
        Bucket bucket = resolveBucket(key, path);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("""
                    {
                        "code": "TOO_MANY_REQUESTS",
                        "message": "Çok fazla istek gönderildi. Lütfen biraz bekleyin."
                    }
                    """);
        }
    }

    private String resolveKey(HttpServletRequest request, String path) {
        // Auth endpointleri → IP bazlı
        if (path.startsWith("/api/auth/")) {
            return "ip:" + getClientIp(request);
        }

        // Diğer endpointler → userId bazlı
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                Long userId = jwtService.extractUserId(token);
                return "user:" + userId + ":" + path;
            } catch (Exception e) {
                return "ip:" + getClientIp(request);
            }
        }

        return "ip:" + getClientIp(request);
    }

    private Bucket resolveBucket(String key, String path) {
        return buckets.computeIfAbsent(key, k -> newBucket(path));
    }

    private Bucket newBucket(String path) {
        // Match random → saatte 5 istek
        if (path.equals("/api/matches/random")) {
            return Bucket.builder()
                    .addLimit(Bandwidth.builder()
                            .capacity(5)
                            .refillIntervally(5, Duration.ofHours(1))
                            .build())
                    .build();
        }

        // Auth endpointleri → 3 dakikada 10 istek
        if (path.startsWith("/api/auth/")) {
            return Bucket.builder()
                    .addLimit(Bandwidth.builder()
                            .capacity(10)
                            .refillIntervally(10, Duration.ofMinutes(3))
                            .build())
                    .build();
        }

        // Genel endpointler → dakikada 30 istek
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(60)
                        .refillIntervally(30, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}