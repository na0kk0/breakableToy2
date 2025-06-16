package com.naokko.FlightAPI;

import com.naokko.FlightAPI.controller.AirportController;
import com.naokko.FlightAPI.controller.FlightController;
import com.naokko.FlightAPI.dto.AirportDTO;
import com.naokko.FlightAPI.dto.ResponseDTO;
import com.naokko.FlightAPI.service.AirportService;
import com.naokko.FlightAPI.service.FlightService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AirportControllerTest {
    @Mock
    private AirportService airportService;

    @InjectMocks
    private AirportController airportController;

    @Test
    public void testAirportController_getAirports() {
        List<AirportDTO> airports = List.of(new AirportDTO());

        when(airportService.searchAirport("Guadalajara")).thenReturn(airports);

        List<AirportDTO> resp = airportController.getAirports("Guadalajara");

        assertEquals(resp, airports);
    }

    @Test
    public void testAirportController_getAirportName() {
        String airportName = "Guadalajara";

        when(airportService.searchAirportName("Guadalajara")).thenReturn(airportName);

        String resp = airportController.getAirportName("Guadalajara");

        assertEquals(resp, airportName);
    }
}
