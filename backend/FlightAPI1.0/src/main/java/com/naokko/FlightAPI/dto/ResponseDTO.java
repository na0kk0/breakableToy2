package com.naokko.FlightAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDTO {
    private List<RoundTripDTO> flights;
    private int total;
}
