package com.naokko.FlightAPI.controller;

import com.naokko.FlightAPI.dto.AirportDTO;
import com.naokko.FlightAPI.service.AirportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@SpringBootApplication
public class AirportController {
    private final AirportService airportService;
    @Autowired
    public AirportController(AirportService airportService) {
        this.airportService = airportService;
    }
    @GetMapping("/get-airports")
    public List<AirportDTO> getAirports(@RequestParam String keyword) {
        return airportService.searchAirport(keyword);
    }
    @GetMapping("/get-airport-name")
    public String getAirportName(@RequestParam String code) {
        return airportService.searchAirportName(code);
    }
}
