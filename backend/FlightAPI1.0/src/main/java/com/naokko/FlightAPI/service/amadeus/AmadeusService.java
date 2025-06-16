package com.naokko.FlightAPI.service.amadeus;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
public class AmadeusService {
    @Value("${amadeus.client-id}")
    private String clientId;
    @Value("${amadeus.client-secret}")
    private String clientSecret;
    @Value("${amadeus.auth-url}")
    private String authUrl;
    @Value("${amadeus.base-url}")
    private String baseUrl;
    @Value("${amadeus.base-url-v2}")
    private String baseUrlV2;

    private AmadeusToken token;
    private final RestTemplate restTemplate;

    public AmadeusService() {
        restTemplate = new RestTemplate();
    }

    @PostConstruct
    public void init() {
        authenticate();
    }

    public String getToken() {
        authenticate();
        if(this.token == null || this.token.isExpired()) {
            throw new RuntimeException("Token is expired");
        }
        return this.token.getAccessToken();
    }

    public synchronized void authenticate() {
        if(token == null || token.isExpired()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");
            body.add("client_id", clientId.trim());
            body.add("client_secret", clientSecret.trim());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            try{
                ResponseEntity<AmadeusToken> response =restTemplate.postForEntity(authUrl, request, AmadeusToken.class);
                if(response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    token = response.getBody();
                    token.setGeneratedTime(LocalDateTime.now());
                }else{
                    throw new RuntimeException("Amadeus token could not be acquired: " + response.getStatusCode());
                }
            }catch (Exception e) {
                throw new RuntimeException("Authentication failed: " + e.getMessage());
            }
        }
    }

}
