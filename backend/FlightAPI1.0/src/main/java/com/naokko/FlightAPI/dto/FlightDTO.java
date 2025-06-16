package com.naokko.FlightAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlightDTO {
    private String departureDay;
    private String departureTime;
    private String arrivalDay;
    private String arrivalTime;
    private String departureAirportName;
    private String departureAirportCode;
    private String arrivalAirportName;
    private String arrivalAirportCode;
    private String airlineName;
    private String airlineCode;
    private String totalTime;
    private List<StopDTO> stops;
    private BigDecimal totalPrice;
    private BigDecimal pricePerTraveler;
    private String flightNumber;
    private String aircraftType;
    private String segmentID;
}
