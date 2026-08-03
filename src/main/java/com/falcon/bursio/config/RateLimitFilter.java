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

        // Public endpointler → polling göz önünde bulundurularak IP bazlı cömert limit
        if (path.startsWith("/api/public/")) {
            String key = "public:ip:" + getClientIp(request);
            Bucket bucket = buckets.computeIfAbsent(key, k -> newPublicBucket());
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
            return;
        }

        // Admin kullanıcıları rate limit'ten muaf
        if (isAdmin(request)) {
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
        if (path.startsWith("/api/auth/") || path.startsWith("/api/public/")) {
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

    private Bucket newPublicBucket() {
        // Public polling endpointleri → IP başına dakikada 120 istek
        // (~120 eşzamanlı kullanıcının aynı NAT'tan 60sn polling yapmasını karşılar)
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(120)
                        .refillIntervally(120, Duration.ofMinutes(1))
                        .build())
                .build();
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

    private boolean isAdmin(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return false;
        try {
            String token = authHeader.substring(7);
            String role = jwtService.extractClaims(token).get("role", String.class);
            return "ADMIN".equals(role);
        } catch (Exception e) {
            return false;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}