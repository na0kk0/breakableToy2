package com.naokko.FlightAPI.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naokko.FlightAPI.dto.AirportDTO;
import com.naokko.FlightAPI.service.amadeus.AmadeusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AirportService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AmadeusService amadeusService;
    @Value("${amadeus.base-url}")
    private String apiUrlV1;
    private final Map<String, String> airportNamesCache = new ConcurrentHashMap<>();

    @Autowired
    public AirportService(RestTemplate restTemplate, ObjectMapper objectMapper, AmadeusService amadeusService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.amadeusService = amadeusService;
    }

    public List<AirportDTO> searchAirport(String keyword) {
        String token = amadeusService.getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String path = apiUrlV1 + "reference-data/locations";
        String url = UriComponentsBuilder.fromHttpUrl(path)
                .queryParam("keyword", keyword)
                .queryParam("subType", "AIRPORT")
                .toUriString();

        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<AirportDTO> airports = new ArrayList<>();

        try{
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String jsonResponse = response.getBody();
                JsonNode node = objectMapper.readTree(jsonResponse);
                JsonNode dataNode = node.get("data");
                if(dataNode.isArray()) {
                    for(JsonNode data : dataNode) {
                        String id = data.get("id").asText();
                        String iataCode = data.has("iataCode") ? data.get("iataCode").asText() : null;
                        String name = data.has("name") ? data.get("name").asText() : null;
                        String cityName = "";
                        String countryName = "";
                        JsonNode addressNode = data.has("address") ? data.get("address") : null;
                        if(addressNode != null) {
                            cityName = addressNode.has("cityName") ? addressNode.get("cityName").asText() : null;
                            countryName = addressNode.has("countryName") ? addressNode.get("countryName").asText() : null;
                        }
                        airports.add(new AirportDTO(id, iataCode, name, cityName, countryName));
                    }
                }
                return airports;
            }else{
                return airports;
            }

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    @Retryable(
            value = {HttpClientErrorException.TooManyRequests.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public String searchAirportName(String keyword) {
        if(airportNamesCache.containsKey(keyword)) {
            return airportNamesCache.get(keyword);
        }
        String token = amadeusService.getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String path = apiUrlV1 + "reference-data/locations";
        String url = UriComponentsBuilder.fromHttpUrl(path)
                .queryParam("keyword", keyword.trim())
                .queryParam("subType", "AIRPORT")
                .toUriString();

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try{
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String jsonResponse = response.getBody();
                JsonNode node = objectMapper.readTree(jsonResponse);
                JsonNode dataNode = node.get("data");
                String name = "Looks like there's no info for this Airport";
                if(dataNode.get(0) != null) {
                    name = dataNode.get(0).has("name") ? dataNode.get(0).get("name").asText() : "Looks like there's no info for this Airport";
                }
                airportNamesCache.put(keyword, name);
                return name;
            }else{
                System.out.println("DEBUG: JSON empty");
                return "Looks like there's no info for this Airport";
            }

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
