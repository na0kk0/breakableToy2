package com.naokko.FlightAPI.controller;

import com.naokko.FlightAPI.dto.ResponseDTO;
import com.naokko.FlightAPI.dto.RoundTripDTO;
import com.naokko.FlightAPI.service.FlightService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@SpringBootApplication
public class FlightController {
    private final FlightService flightService;

    @Autowired
    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping("/get-flights")
    public ResponseDTO getFlights(@RequestParam String originLocationCode, @RequestParam String destinationLocationCode, @RequestParam String departureDate, @RequestParam String returnDate, @RequestParam String adults, @RequestParam Boolean nonStop, @RequestParam String currency, @RequestParam int page, @RequestParam String sortBy, HttpSession session) {
        return flightService.searchFlights(originLocationCode, destinationLocationCode, departureDate, returnDate, adults, nonStop.toString(), currency, page, sortBy, session);
    }
}
