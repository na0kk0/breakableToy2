package com.naokko.FlightAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StopDTO {
    private String airportCode;
    private String airportName;
    private String airlineCode;
    private String airlineName;
    private String duration;
    private String flightNumber;
    private String aircraftType;
    private String segmentID;
}
