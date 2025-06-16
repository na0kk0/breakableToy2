package com.naokko.FlightAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AirportDTO {
    private String id;
    private String iataCode;
    private String name;
    private String cityName;
    private String countryName;
}
