package com.naokko.FlightAPI.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naokko.FlightAPI.dto.*;
import com.naokko.FlightAPI.service.amadeus.AmadeusService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class FlightService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AmadeusService amadeusService;
    private final AirportService airportService;
    @Value("${amadeus.base-url-v2}")
    private String apiUrlV2;
    private static final String CACHED_FLIGHTS_KEY = "cachedFlightsList";
    private static final String SEARCH_PARAMS_HASH_KEY = "currentSearchParamsHash";

    @Autowired
    public FlightService(RestTemplate restTemplate, ObjectMapper objectMapper, AmadeusService amadeusService, AirportService airportService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.amadeusService = amadeusService;
        this.airportService = airportService;
    }

    public String getDuration(Duration time){
        long hours = time.toHours();
        long minutes = time.toMinutes() % 60;

        String formattedDuration;

        if(hours > 0 && minutes > 0){
            formattedDuration = hours + "h" + minutes + "m";
        }else if(hours > 0){
            formattedDuration = hours + "h";
        }else if(minutes > 0){
            formattedDuration = minutes + "m";
        }else{
            formattedDuration = "?";
        }

        return formattedDuration;
    }

    public BigDecimal currencyConversion(String actualCurrency, String expectedCurrency, BigDecimal amount) {
        if(actualCurrency.equals(expectedCurrency) || expectedCurrency.isEmpty()) {
            return amount;
        }
        switch (actualCurrency) {
            case "USD":
                switch (expectedCurrency) {
                    case "MXN":
                        return amount.multiply(new BigDecimal("19.12"));
                    case "EUR":
                        return amount.multiply(new BigDecimal("0.88"));
                }
                break;
            case "EUR":
                switch (expectedCurrency) {
                    case "MXN":
                        return amount.multiply(new BigDecimal("21.79"));
                    case "USD":
                        return amount.multiply(new BigDecimal("1.14"));
                }
                break;
            case "MXN":
                switch (expectedCurrency) {
                    case "EUR":
                        return amount.multiply(new BigDecimal("0.046"));
                    case "USD":
                        return amount.multiply(new BigDecimal("0.052"));
                }
                break;
        }
        return amount;
    }

    private String generateSearchParamsHash(String originLocationCode, String destinationLocationCode, String departureDate,
                                            String returnDate, String adults, String nonStop) {
        return originLocationCode + "|" + destinationLocationCode + "|" +
                departureDate + "|" + Objects.toString(returnDate, "") + "|" +
                adults + "|" + Objects.toString(nonStop, "");
    }

    private void calculateLayover(List<SegmentDTO> segments, String arrivalCode) {
        for(int i = 0; i < segments.size()-1; i++){
            SegmentDTO current = segments.get(i);
            SegmentDTO next = segments.get(i+1);

            if(!next.getDepartureAirportCode().equals(arrivalCode)){
                String arrivalDateTimeStr = current.getArrivalDate() + "T" + current.getArrivalTime();
                String nextDepartureDateTimeStr = next.getDepartureDate() + "T" + next.getDepartureTime();

                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

                LocalDateTime arrival = LocalDateTime.parse(arrivalDateTimeStr, formatter);
                LocalDateTime nextDeparture = LocalDateTime.parse(nextDepartureDateTimeStr, formatter);

                Duration layover = Duration.between(arrival, nextDeparture);
                long hours = layover.toHours();
                long minutes = layover.toMinutes() % 60;

                next.setLayover(hours + "h" + minutes + "m");
            }
        }
    }

    public ResponseDTO searchFlights(String originLocationCode, String destinationLocationCode, String departureDate, String returnDate, String adults, String nonStop, String currency, int page, String sortBy, HttpSession session) {
        originLocationCode = originLocationCode != null ? originLocationCode.trim() : "";
        destinationLocationCode = destinationLocationCode != null ? destinationLocationCode.trim() : "";
        departureDate = departureDate != null ? departureDate.trim() : "";
        returnDate = returnDate != null ? returnDate.trim() : "";
        adults = adults != null ? adults.trim() : "";
        nonStop = nonStop != null ? nonStop.trim() : "";
        currency = currency != null ? currency.trim() : "";
        sortBy = sortBy != null ? sortBy.trim() : "";
        String currentSearchParamsHash = generateSearchParamsHash(
            originLocationCode, destinationLocationCode, departureDate, returnDate, adults, nonStop
        );

        ResponseDTO responseDTO;
        List<RoundTripDTO> flights;
        String storedSearchParamsHash = (String) session.getAttribute(SEARCH_PARAMS_HASH_KEY);

        if (session.getAttribute(CACHED_FLIGHTS_KEY) != null &&
                currentSearchParamsHash.equals(storedSearchParamsHash)) {

            responseDTO = (ResponseDTO) session.getAttribute(CACHED_FLIGHTS_KEY);
            System.out.println("Using cached flights from session for the same search query.");
        } else {
            System.out.println("New search query detected or no cached flights. Calling Amadeus API.");

            session.removeAttribute(CACHED_FLIGHTS_KEY);
            session.removeAttribute(SEARCH_PARAMS_HASH_KEY);

            String token = amadeusService.getToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            String path = apiUrlV2 + "shopping/flight-offers";
            UriComponentsBuilder uriComponent = UriComponentsBuilder.fromHttpUrl(path)
                    .queryParam("originLocationCode", originLocationCode)
                    .queryParam("destinationLocationCode", destinationLocationCode)
                    .queryParam("departureDate", departureDate);

            if (returnDate != null && !returnDate.isEmpty()) {
                uriComponent.queryParam("returnDate", returnDate);
            }
            uriComponent.queryParam("adults", adults);
            if (nonStop != null && !nonStop.isEmpty()) {
                uriComponent.queryParam("nonStop", nonStop);
            }

            String url = uriComponent.toUriString();

            HttpEntity<String> entity = new HttpEntity<>(headers);
            flights = new ArrayList<>();
            responseDTO = new ResponseDTO();

            try {
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                String jsonResponse = response.getBody();
                JsonNode node = objectMapper.readTree(jsonResponse);
                int count = node.get("meta").get("count").asInt();
                responseDTO.setTotal(count);
                System.out.println("DEBUG: COUNT - "+count);
                JsonNode dataNode = node.get("data");
                JsonNode dictionariesNode = node.get("dictionaries");
                Map<String, String> carriers = new HashMap<>();
                Map<String, String> airCraft = new HashMap<>();
                String baseCurrency = "";
                if (dictionariesNode != null) {
                    JsonNode carriersNode = dictionariesNode.get("carriers");
                    if (carriersNode != null) {
                        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = carriersNode.fields();
                        while (fieldsIterator.hasNext()) {
                            Map.Entry<String, JsonNode> field = fieldsIterator.next();
                            carriers.put(field.getKey(), field.getValue().asText());
                        }
                    }

                    JsonNode airCraftNode = dictionariesNode.get("aircraft");
                    if (airCraftNode != null) {
                        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = airCraftNode.fields();
                        while (fieldsIterator.hasNext()) {
                            Map.Entry<String, JsonNode> field = fieldsIterator.next();
                            airCraft.put(field.getKey(), field.getValue().asText());
                        }
                    }
                }
                for (JsonNode data : dataNode) {
                    String departureAirportCode = "";
                    String departureAirportName = "";
                    String departureDayTime = "";
                    String arrivalAirportCode = "";
                    String arrivalAirportName = "";
                    String arrivalDayTime = "";
                    String totalTime = "";
                    String carrierCode = "";
                    String flightNumber = "";
                    String aircraftType = "";
                    String segmentID = "";
                    BigDecimal totalPrice = BigDecimal.ZERO;
                    BigDecimal pricePerTraveler = BigDecimal.ZERO;
                    List<StopDTO> stops = new ArrayList<>();
                    String actualCurrency = "";
                    FlightDTO flightGo = new FlightDTO();
                    FlightDTO flightReturn = null;
                    List<SegmentDTO> segments = new ArrayList<>();
                    long tripTime = 0;
                    Double base = 0.0;
                    List<FeesDTO> fees = new ArrayList<>();
                    List<TravelerFareDetailDTO> fareDetails = new ArrayList<>();
                    JsonNode priceNode = data.get("price");
                    if (priceNode != null) {
                        actualCurrency = priceNode.has("currency") ? priceNode.get("currency").asText() : "";
                        baseCurrency = actualCurrency;
                        totalPrice = priceNode.has("total") ? new BigDecimal(priceNode.get("total").asText()) : BigDecimal.ZERO;
                        totalPrice = currencyConversion(actualCurrency, currency, totalPrice);
                        pricePerTraveler = totalPrice.divide(new BigDecimal(adults));

                        base = priceNode.has("base") ? priceNode.get("base").asDouble() : 0;

                        JsonNode feesNode = priceNode.get("fees");
                        if (feesNode != null) {
                            for(JsonNode fee : feesNode) {
                                String amount = fee.has("amount") ? fee.get("amount").asText() : "";
                                String type = fee.has("type") ? fee.get("type").asText() : "";
                                fees.add(new FeesDTO(amount, type));
                            }
                        }
                    }
                    JsonNode travelerPricingsNode = data.get("travelerPricings");
                    if (travelerPricingsNode != null) {
                        JsonNode fareNode = travelerPricingsNode.get(0);
                        JsonNode fareDetailsBySegmentNode = fareNode.get("fareDetailsBySegment");
                        if (fareDetailsBySegmentNode != null) {
                            for(JsonNode fareDetail : fareDetailsBySegmentNode) {
                                String id = "";
                                String cabin = "";
                                String class_ = "";
                                List<AmenitiesDTO> amenities = new ArrayList<>();

                                id = fareDetail.has("segmentId") ? fareDetail.get("segmentId").asText() : "";
                                cabin = fareDetail.has("cabin") ? fareDetail.get("cabin").asText() : "";
                                class_ = fareDetail.has("class") ? fareDetail.get("class").asText() : "";

                                JsonNode amenitiesNode = fareDetail.get("amenities");
                                if (amenitiesNode != null) {
                                    for(JsonNode amenity : amenitiesNode) {
                                        String name = "";
                                        String chargeable = "";

                                        name = amenity.has("description") ? amenity.get("description").asText() : "";
                                        chargeable = amenity.has("isChargeable") ? amenity.get("isChargeable").asText() : "";

                                        amenities.add(new AmenitiesDTO(name, chargeable));
                                    }
                                }
                                fareDetails.add(new TravelerFareDetailDTO(id, cabin, class_, amenities));
                            }
                        }
                    }
                    JsonNode itinerariesNode = data.has("itineraries") ? data.get("itineraries") : null;
                    if (itinerariesNode != null) {
                        for (int j = 0; j < itinerariesNode.size(); j++) {
                            totalTime = itinerariesNode.get(j).get("duration").asText();
                            tripTime += Duration.parse(totalTime).toMinutes();
                            totalTime = getDuration(Duration.parse(totalTime));
                            JsonNode segmentsNode = itinerariesNode.get(j).has("segments") ? itinerariesNode.get(j).get("segments") : null;
                            if (segmentsNode != null) {
                                //Filling the segment List
                                for(JsonNode segmentN : segmentsNode) {
                                    String departureAirportNameSegment = "";
                                    String departureAirportCodeSegment = "";
                                    String departureDateSegment = "";
                                    String departureTimeSegment = "";
                                    String arrivalAirportNameSegment = "";
                                    String arrivalAirportCodeSegment = "";
                                    String arrivalDateSegment = "";
                                    String arrivalTimeSegment = "";
                                    String airlineNameSegment = "";
                                    String airlineCodeSegment = "";
                                    String flightNumberSegment = "";
                                    String aircraftTypeSegment = "";
                                    String durationSegment = "";
                                    DateTimeFormatter outputDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
                                    DateTimeFormatter outputTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
                                    JsonNode departureNodeS = segmentN.get("departure");
                                    if (departureNodeS != null) {
                                        departureAirportCodeSegment = departureNodeS.get("iataCode").asText();
                                        departureAirportNameSegment = airportService.searchAirportName(departureAirportCodeSegment);
                                        String departureDayTimeSegment = departureNodeS.get("at").asText();

                                        LocalDateTime departureLocalDTSegment = LocalDateTime.parse(departureDayTimeSegment);
                                        LocalDate departureLocalDateSegment = departureLocalDTSegment.toLocalDate();
                                        departureDateSegment = departureLocalDateSegment.format(outputDateFormatter);
                                        LocalTime departureLocalTimeSegment = departureLocalDTSegment.toLocalTime();
                                        departureTimeSegment = departureLocalTimeSegment.format(outputTimeFormatter);
                                    }
                                    JsonNode arrivalNodeS = segmentN.get("arrival");
                                    if (arrivalNodeS != null) {
                                        arrivalAirportCodeSegment = arrivalNodeS.get("iataCode").asText();
                                        arrivalAirportNameSegment = airportService.searchAirportName(arrivalAirportCodeSegment);
                                        String arrivalDayTimeSegment = arrivalNodeS.get("at").asText();

                                        LocalDateTime arrivalLocalDTSegment = LocalDateTime.parse(arrivalDayTimeSegment);
                                        LocalDate departureLocalDateSegment = arrivalLocalDTSegment.toLocalDate();
                                        arrivalDateSegment = departureLocalDateSegment.format(outputDateFormatter);
                                        LocalTime departureLocalTimeSegment = arrivalLocalDTSegment.toLocalTime();
                                        arrivalTimeSegment = departureLocalTimeSegment.format(outputTimeFormatter);
                                    }
                                    airlineCodeSegment = segmentN.get("carrierCode").asText();
                                    airlineNameSegment = carriers.get(airlineCodeSegment);
                                    flightNumberSegment = segmentN.get("number").asText();
                                    aircraftTypeSegment = airCraft.get(segmentN.get("aircraft").get("code").asText());
                                    durationSegment = getDuration(Duration.parse(segmentN.get("duration").asText()));

                                    segments.add(new SegmentDTO(departureAirportNameSegment, departureAirportCodeSegment, departureDateSegment,
                                            departureTimeSegment, arrivalAirportNameSegment, arrivalAirportCodeSegment, arrivalDateSegment,
                                            arrivalTimeSegment, airlineNameSegment, airlineCodeSegment, flightNumberSegment, aircraftTypeSegment,
                                            durationSegment, ""));
                                }
                                JsonNode departureNode = segmentsNode.get(0).get("departure");
                                if (departureNode != null) {
                                    departureAirportCode = departureNode.has("iataCode") ? departureNode.get("iataCode").asText() : "";
                                    departureAirportName = airportService.searchAirportName(departureAirportCode);
                                    departureDayTime = departureNode.has("at") ? departureNode.get("at").asText() : "";
                                    segmentID = segmentsNode.get(0).get("id").asText();
                                }
                                if (segmentsNode.size() == 1) { //It's non-stop
                                    JsonNode arrivalNode = segmentsNode.get(0).get("arrival");
                                    if (arrivalNode != null) {
                                        arrivalAirportCode = arrivalNode.has("iataCode") ? arrivalNode.get("iataCode").asText() : "";
                                        arrivalAirportName = airportService.searchAirportName(arrivalAirportCode);
                                        arrivalDayTime = arrivalNode.has("at") ? arrivalNode.get("at").asText() : "";
                                    }
                                    carrierCode = segmentsNode.get(0).has("carrierCode") ? segmentsNode.get(0).get("carrierCode").asText() : "";
                                    flightNumber = segmentsNode.get(0).has("number") ? segmentsNode.get(0).get("number").asText() : "";
                                    JsonNode aircraftNode = segmentsNode.get(0).get("aircraft");
                                    if (aircraftNode != null) {
                                        aircraftType = aircraftNode.has("code") ? aircraftNode.get("code").asText() : "";
                                    }
                                } else if (segmentsNode.size() > 1) {
                                    carrierCode = segmentsNode.get(0).has("carrierCode") ? segmentsNode.get(0).get("carrierCode").asText() : "";
                                    for (int i = 0; i < segmentsNode.size() - 1; i++) {
                                        JsonNode segment = segmentsNode.get(i);
                                        String stopAirportCode = "";
                                        String stopAirportName = "";
                                        String stopCarrierCode = "";
                                        String stopFlightNumber = "";
                                        String stopAircraftType = "";
                                        String stopSegmentID = "";
                                        String stopDuration = segment.has("duration") ? segment.get("duration").asText() : "";
                                        JsonNode stopArrivalNode = segment.get("arrival");
                                        if (stopArrivalNode != null) {
                                            stopAirportCode = stopArrivalNode.has("iataCode") ? stopArrivalNode.get("iataCode").asText() : "";
                                            stopAirportName = airportService.searchAirportName(stopAirportCode);
                                        }
                                        stopCarrierCode = segment.has("carrierCode") ? segment.get("carrierCode").asText() : "";
                                        stopFlightNumber = segment.has("number") ? segment.get("number").asText() : "";
                                        JsonNode aircraftTypeNode = segment.get("aircraft");
                                        if (aircraftTypeNode != null) {
                                            stopAircraftType = aircraftTypeNode.has("code") ? aircraftTypeNode.get("code").asText() : "";
                                        }
                                        stopSegmentID = segment.has("id") ? segment.get("id").asText() : "";
                                        stops.add(new StopDTO(stopAirportCode, stopAirportName, stopCarrierCode, carriers.get(stopCarrierCode), getDuration(Duration.parse(stopDuration)), stopFlightNumber, airCraft.get(stopAircraftType), stopSegmentID));
                                    }
                                    JsonNode arrivalNode = segmentsNode.get(segmentsNode.size() - 1).get("arrival");
                                    if (arrivalNode != null) {
                                        arrivalAirportCode = arrivalNode.has("iataCode") ? arrivalNode.get("iataCode").asText() : "";
                                        arrivalAirportName = airportService.searchAirportName(arrivalAirportCode);
                                        arrivalDayTime = arrivalNode.has("at") ? arrivalNode.get("at").asText() : "";
                                    }
                                    flightNumber = segmentsNode.get(segmentsNode.size() - 1).has("number") ? segmentsNode.get(0).get("number").asText() : "";
                                    JsonNode aircraftNode = segmentsNode.get(segmentsNode.size() - 1).get("aircraft");
                                    if (aircraftNode != null) {
                                        aircraftType = aircraftNode.has("code") ? aircraftNode.get("code").asText() : "";
                                    }
                                }
                            }
                            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH);
                            DateTimeFormatter outputTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

                            LocalDateTime departureLocalDT = LocalDateTime.parse(departureDayTime);
                            LocalDate departureLocalDate = departureLocalDT.toLocalDate();
                            String departureDateFormatted = departureLocalDate.format(outputFormatter);
                            LocalTime departureLocalTime = departureLocalDT.toLocalTime();
                            String departureTimeFormatted = departureLocalTime.format(outputTimeFormatter);


                            LocalDateTime arrivalLocalDT = LocalDateTime.parse(arrivalDayTime);
                            LocalDate arrivalLocalDate = arrivalLocalDT.toLocalDate();
                            String arrivalDateFormatted = arrivalLocalDate.format(outputFormatter);
                            LocalTime arrivalLocalTime = arrivalLocalDT.toLocalTime();
                            String arrivalTimeFormatted = arrivalLocalTime.format(outputTimeFormatter);

                            if (j == 0) {
                                flightGo = new FlightDTO(departureDateFormatted, departureTimeFormatted, arrivalDateFormatted, arrivalTimeFormatted,
                                        departureAirportName, departureAirportCode, arrivalAirportName,
                                        arrivalAirportCode, carriers.get(carrierCode), carrierCode, totalTime,
                                        stops, totalPrice, pricePerTraveler, flightNumber, airCraft.get(aircraftType), segmentID);
                            } else {
                                flightReturn = new FlightDTO(departureDateFormatted, departureTimeFormatted, arrivalDateFormatted, arrivalTimeFormatted,
                                        departureAirportName, departureAirportCode, arrivalAirportName,
                                        arrivalAirportCode, carriers.get(carrierCode), carrierCode, totalTime,
                                        stops, totalPrice, pricePerTraveler, flightNumber, airCraft.get(aircraftType), segmentID);
                            }
                        }
                    }
                    calculateLayover(segments, destinationLocationCode);
                    flights.add(new RoundTripDTO(flightGo, flightReturn, segments, currency, (flightGo.getTotalPrice().doubleValue()), (currencyConversion(baseCurrency, currency, new BigDecimal(String.valueOf(base))).doubleValue()), (currencyConversion(baseCurrency, currency, new BigDecimal(String.valueOf(base))).doubleValue())/Double.valueOf(adults), fareDetails, fees, tripTime));
                }
                responseDTO.setFlights(flights);
                session.setAttribute(CACHED_FLIGHTS_KEY, responseDTO);
                session.setAttribute(SEARCH_PARAMS_HASH_KEY, currentSearchParamsHash);
            } catch (Exception e) {
                session.removeAttribute(CACHED_FLIGHTS_KEY);
                session.removeAttribute(SEARCH_PARAMS_HASH_KEY);
                throw new RuntimeException(e);
            }
        }
        List<RoundTripDTO> displayFlights = new ArrayList<>(responseDTO.getFlights());

        if(sortBy != null && !displayFlights.isEmpty()) {
            switch(sortBy.toLowerCase()) {
                case "priceup":
                    displayFlights.sort(Comparator.comparingDouble(RoundTripDTO::getPrice));
                    break;
                case "pricedown":
                    displayFlights.sort(Comparator.comparingDouble(RoundTripDTO::getPrice).reversed());
                    break;
                case "timeup":
                    displayFlights.sort(Comparator.comparingLong(RoundTripDTO::getTotalTime));
                    break;
                case "timedown":
                    displayFlights.sort(Comparator.comparingLong(RoundTripDTO::getTotalTime).reversed());
                    break;
                default:
                    break;
            }
        }
        int pageSize = 10;
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, displayFlights.size());

        if (startIndex >= displayFlights.size()) {
            return new ResponseDTO(Collections.emptyList(),0);
        }
        for(int i = startIndex; i < endIndex; i++) {
            displayFlights.get(i).getFlightGo().setTotalPrice(currencyConversion(displayFlights.get(i).getCurrency(), currency, displayFlights.get(i).getFlightGo().getTotalPrice()));
            displayFlights.get(i).getFlightGo().setPricePerTraveler(currencyConversion(displayFlights.get(i).getCurrency(), currency, displayFlights.get(i).getFlightGo().getPricePerTraveler()));
            if(displayFlights.get(i).getFlightReturn() != null) {
                displayFlights.get(i).getFlightReturn().setTotalPrice(currencyConversion(displayFlights.get(i).getCurrency(), currency, displayFlights.get(i).getFlightReturn().getTotalPrice()));
                displayFlights.get(i).getFlightReturn().setPricePerTraveler(currencyConversion(displayFlights.get(i).getCurrency(), currency, displayFlights.get(i).getFlightReturn().getPricePerTraveler()));
            }
            BigDecimal newBase = currencyConversion(displayFlights.get(i).getCurrency(),currency, new BigDecimal(String.valueOf(displayFlights.get(i).getBase())));
            displayFlights.get(i).setBase(newBase.doubleValue());
            BigDecimal newBasePerTraveler = currencyConversion(displayFlights.get(i).getCurrency(),currency, new BigDecimal(String.valueOf(displayFlights.get(i).getBasePerTraveler())));
            displayFlights.get(i).setBasePerTraveler(newBasePerTraveler.doubleValue());
            displayFlights.get(i).setPrice(displayFlights.get(i).getFlightGo().getTotalPrice().doubleValue());
            displayFlights.get(i).setCurrency(currency);
        }
        return new ResponseDTO(displayFlights.subList(startIndex, endIndex),(int)Math.ceil((double)responseDTO.getTotal()/10));
    }
}
