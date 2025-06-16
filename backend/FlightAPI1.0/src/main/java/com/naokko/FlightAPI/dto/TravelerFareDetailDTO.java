package com.naokko.FlightAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TravelerFareDetailDTO {
    private String id;
    private String cabin;
    private String class_;
    private List<AmenitiesDTO> amenities;
}
