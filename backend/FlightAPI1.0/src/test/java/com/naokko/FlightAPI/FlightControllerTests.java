package com.naokko.FlightAPI;

import com.naokko.FlightAPI.controller.FlightController;
import com.naokko.FlightAPI.dto.ResponseDTO;
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
public class FlightControllerTests {
    @Mock
    private FlightService flightService;

    @InjectMocks
    private FlightController flightController;

    @Test
    public void testSearchFlights() {
        ResponseDTO response = new ResponseDTO(List.of(), 30);

        when(flightService.searchFlights("GDL", "LAX", "2025-10-12",
                "", "1","true", "EUR", 1, "", null)).thenReturn(response);

        ResponseDTO resp = flightController.getFlights("GDL", "LAX", "2025-10-12",
                "", "1",true, "EUR", 1, "", null);

        assertEquals(response, resp);


    }
}
