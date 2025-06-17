# Breakable Toy 2 -Flight Search with Amadeus-
This breakable toy is an App for searching flights and see it's details consuming the Amadeus API. The frontend is a React App in Next js and the backend is a Spring boot API with Java and Gradle.

## Backend
The backend consumes data from the Amadeus API, then, proccess that data and get only the required data for the frontend. It have 3 endpoints: To get the airport information (requires a keyword as
parameter), to get the airport name (requires a keyword as parameter) and to get a list of flights (requieres the codes for the airports of departure and arrival, the departure date, an optional return date,
the number of adult tickets that you want, if you want the flight to be nonstop or not, the page of the flights, and the currency for the prices). The backend use a map and HttpSession for caching some data
like the codes of the aiports so the API don't call Amadeus a lot of times and the flights list for changinf the currency or the page without retrieving all the data again. It runs on port 8080.

## Frontend
The frontend only shows the data received from the backend, it don't operate over it. It runs on port 3000.

### Search Page

### Flights Page

### Details pages
