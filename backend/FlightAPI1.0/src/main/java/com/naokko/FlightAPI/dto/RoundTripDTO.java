package com.naokko.FlightAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoundTripDTO {
    private FlightDTO flightGo;
    private FlightDTO flightReturn;
    private List<SegmentDTO> segments;
    private String currency;
    private Double price;
    private Double base;
    private Double basePerTraveler;
    private List<TravelerFareDetailDTO> fareDetails;
    private List<FeesDTO> fees;
    private long totalTime;
}
