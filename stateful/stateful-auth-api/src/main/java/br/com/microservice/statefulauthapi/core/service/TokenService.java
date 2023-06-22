package br.com.microservice.statefulauthapi.core.service;

import br.com.microservice.statefulauthapi.core.dto.TokenData;
import br.com.microservice.statefulauthapi.infra.exception.AuthenticationException;
import br.com.microservice.statefulauthapi.infra.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.util.ObjectUtils.isEmpty;

@Service
@AllArgsConstructor
public class TokenService {

    private static final Long ONE_DAY_IN_SECONDS = 86400L;
    private static final String EMPTY_SPACE = " ";
    private static final int TOKEN_INDEX = 1;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;


    public String createToken(String username) {
        var accessToken = UUID.randomUUID().toString();
        var data = new TokenData(username);
        var jsonData = getJsonData(data);
        redisTemplate.opsForValue().set(accessToken, jsonData);
        redisTemplate.expireAt(accessToken, Instant.now().plusSeconds(ONE_DAY_IN_SECONDS));

        return accessToken;
    }

    public TokenData getTokenData(String token) {
        String accessToken = extractToken(token);
        var jsonString = getRedisTokenValue(accessToken);

        try {
            return objectMapper.readValue(jsonString, TokenData.class);
        } catch (Exception e) {
            throw new AuthenticationException("Error extracting the authenticated user: " + e.getMessage());
        }
    }

    public boolean validateAccessToken(String token) {
        var accessToken = extractToken(token);
        var data = getRedisTokenValue(accessToken);
        return !isEmpty(data);
    }

    public void deleteRedisToken(String token) {
        var accessToken = extractToken(token);
        redisTemplate.delete(accessToken);
    }

    private String getRedisTokenValue(String accessToken) {
        return redisTemplate.opsForValue().get(accessToken);
    }

    private String getJsonData(TokenData data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return EMPTY_SPACE;
        }
    }

    private String extractToken(String token) {
        if (isEmpty(token)) {
            throw new ValidationException("The access token was not informed.");
        }

        if (token.contains(EMPTY_SPACE)) {
            return token.split(EMPTY_SPACE)[TOKEN_INDEX];
        }

        return token;
    }


}
