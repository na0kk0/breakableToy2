package com.naokko.FlightAPI.service.amadeus;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmadeusToken {
    private String type;
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("expires_in")
    private int expiresIn;
    private LocalDateTime generatedTime;

    public boolean isExpired() {
        if (generatedTime == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(generatedTime.plusSeconds(expiresIn-60));
    }
}
