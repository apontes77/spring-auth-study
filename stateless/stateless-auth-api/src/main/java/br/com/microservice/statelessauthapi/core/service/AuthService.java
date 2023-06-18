package br.com.microservice.statelessauthapi.core.service;

import br.com.microservice.statelessauthapi.core.dto.AuthRequest;
import br.com.microservice.statelessauthapi.core.dto.TokenDTO;
import br.com.microservice.statelessauthapi.core.repository.UserRepository;
import br.com.microservice.statelessauthapi.infra.exception.ValidationException;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import static org.springframework.util.ObjectUtils.isEmpty;
@Service
@AllArgsConstructor
public class AuthService {

    private JwtService jwtService;
    private PasswordEncoder passwordEncoder;
    private UserRepository repository;

    public TokenDTO login(AuthRequest authRequest) {
        var user = repository
                .findByUsername(authRequest.username())
                .orElseThrow(() -> new ValidationException("User not found"));

        var accessToken = jwtService.createToken(user);
        validatePassword(authRequest.password(), user.getPassword());
        return new TokenDTO(accessToken);
    }

    public TokenDTO validateToken(String accessToken) {
        validateExistingToken(accessToken);
        jwtService.validateAccessToken(accessToken);
        return new TokenDTO(accessToken);
    }

    private void validatePassword(String rawPassword, String encodedPassword) {

        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new ValidationException("The password is incorrect!");
        }
    }

    private void validateExistingToken(String accessToken) {
        if (isEmpty(accessToken)) {
            throw new ValidationException("The access token must be informed!");
        }
    }
}
