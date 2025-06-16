package com.naokko.FlightAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SegmentDTO {
    private String departureAirportName;
    private String departureAirportCode;
    private String departureDate;
    private String departureTime;
    private String arrivalAirportName;
    private String arrivalAirportCode;
    private String arrivalDate;
    private String arrivalTime;
    private String airlineName;
    private String airlineCode;
    private String flightNumber;
    private String aircraftType;
    private String duration;
    private String layover;
}
