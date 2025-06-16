# Flight API
In this API you can consult information related airports and flights. All the data is consulted to the Amadeus API.
The API needs to get a Token from the Amadeus service, then  stores it and check in every call if the token is still valid,
if not, get another one.
## Endpoints
### /get-airports
It receives a String for the keyword that is sent to the *searchAirport* function in the AirportService.java.
The function call to the Amadeus API to get the data about airports that match that keyword.
Then, it extracts the iataCode, the city name and the country name, put all those fields on a list and send it to the 
front end. The function has an annotation Retryable because it's used to get the airports in the search field.

### /get-airport-name
It receives the iataCode, call the Amadeus API and search for the airport with that code, then, return just the name.
This is used for the page that shows the flights.

### /get-flights
This one receives a lot of parameters: The origin location code, the destination location code, the departure and return
date (the return date can be empty), how many adults (at least one), if it's non-stop or not, the currency selected,
the page (since usually there's a lot of flights the app divide all the options in pages of 10 flights each), the kind
of sort (it can be empty) and a Http session (this way we reduce the loading time for stuff like changing the currency, 
change the sorting or navigate through pages).
The function *searchFlights* call to Amadeus sending all those parameters, then collect just the data that the front end
needs. It returns a list of flights and the total flight time. The flight service class have a few functions that helps 
to the process of the data (like the currency conversion or convert the duration to an easy read string). The stops and 
segments are separated because of how the front end was built and the data that display at each page (all the flight
and the detail flight page, it's not the most efficient way, but works... I'll update it latter... I mean, if I have time
before the demo). The data is collected from the JSON response, accessing the nodes that have relevant info for the front end.
The data is cached with the Http session, that way it can check if the search is the same and use the same data (it will 
not consult Amadeus again if the user just want another kind of sort or want another currency).
Also, the airport names are cached because the API used to collapse when this function was searching the airport names, 
but these are separated from the Http session and the user.
