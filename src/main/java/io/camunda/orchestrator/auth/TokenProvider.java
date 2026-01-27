package io.camunda.orchestrator.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

public class TokenProvider {
    private record CachedToken(String token, Instant expiresAt) {}
    private final AtomicReference<CachedToken> cache = new AtomicReference<>();
    private final AuthClient authClient;

    public TokenProvider(ObjectMapper mapper) {
        this.authClient = new AuthClient(mapper, HttpClient.newHttpClient());
    }

    public synchronized String getToken(String clientId, String secret) {
        CachedToken cached = cache.get();
        
        if (cached != null && cached.expiresAt().isAfter(Instant.now().plusSeconds(30))) {
            return cached.token();
        }

        AuthClient.TokenResult result = authClient.fetchToken(clientId, secret);
        CachedToken newToken = new CachedToken(result.token(), Instant.now().plusSeconds(result.expiresIn()));
        cache.set(newToken);

        return result.token();
    }
}