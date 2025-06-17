const API_URL: string = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/";

let debounceTimer:NodeJS.Timeout;

export interface Airport {
    id: string;
    name: string;
    iataCode: string;
    cityName: string;
    countryName: string;
}

export interface Stop{
    airportCode: string;
    airportName: string;
    airlineCode: string;
    airlineName: string;
    duration: string;
}

export interface Flight {
    departureDay: string;
    departureTime: string;
    arrivalDay: string;
    arrivalTime: string;
    departureAirportName: string;
    departureAirportCode: string;
    arrivalAirportName: string;
    arrivalAirportCode: string;
    airlineName: string;
    airlineCode: string;
    totalTime: string;
    stops: Stop[];
    totalPrice: number;
    pricePerTraveler: number;
}

export interface Amenities{
    name: string;
    chargeable: boolean;
}

export interface TravelerFareDetails{
    id: string;
    cabin: string;
    class_: string;
    amenities: Amenities[];
}

export interface FeesDTO{
    amount: string;
    type: string;
}

export interface Segment{
    departureAirportName: string;
    departureAirportCode: string;
    departureDate: string;
    departureTime: string;
    arrivalAirportName: string;
    arrivalAirportCode: string;
    arrivalDate: string;
    arrivalTime: string;
    airlineName: string;
    airlineCode: string;
    flightNumber: string;
    aircraftType: string;
    duration: string;
    layover: string;
}

export interface RoundTrip{
    flightGo: Flight;
    flightReturn: Flight;
    segments: Segment[];
    currency: string;
    price: number;
    base: number;
    basePerTraveler: number;
    totalTime: number;
    fareDetails: TravelerFareDetails[];
    fees: FeesDTO[];
}

export interface ResponseDTO{
    flights: RoundTrip[];
    total: number;
}

export async function searchCodes(keyword:string):Promise<Airport[]> {
    return new Promise((resolve,reject)=>{
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(async ()=>{
            try{
                const controller = new AbortController();
                const signal = controller.signal;

                if(keyword == "" || keyword.length <=2){
                    controller.abort();
                    resolve([]);
                    return;
                }

                const url = `${API_URL}get-airports?keyword=${keyword}`;
                const response:Response = await fetch(url, {signal});
                if(!response.ok){
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                const json:Airport[] = await response.json();
                resolve(json);
            }catch (err:any){
                if(err.name === "AbortError"){
                    console.log("Abort Error");
                    resolve([]);
                }else{
                    console.log(err);
                    reject(err);
                }
            }
        }, 300);
    })
}

export async function searchFlights(originLocationCode:string, destinationLocationCode:string, departureDate:string, returnDate:string, adults:string, nonStop:boolean, currency:string, page:number, sortBy:string):Promise<ResponseDTO> {
    return new Promise((resolve,reject)=>{
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(async ()=>{
            // @ts-ignore
            try{
                const controller = new AbortController();
                const signal = controller.signal;

                if(originLocationCode == "" || destinationLocationCode == "" || departureDate == "" || adults == "" || currency == ""){
                    controller.abort();
                    resolve({flights:[],total:0});
                    return
                }

                const url = `${API_URL}get-flights?originLocationCode=${originLocationCode}&destinationLocationCode=${destinationLocationCode}&departureDate=${departureDate}&returnDate=${returnDate}&adults=${adults}&nonStop=${nonStop}&currency=${currency}&page=${page}&sortBy=${sortBy}`;

                const response:Response = await fetch(url, {
                    signal,
                    credentials: "include"});
                if(!response.ok){
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                const json:ResponseDTO = await response.json();
                resolve(json);
            }catch(err:any){
                if(err.name === "AbortError"){
                    console.log("Abort Error");
                    resolve({flights:[],total:0});
                }else{
                    console.log(err);
                    reject(err);
                }
            }
        })
    })
}