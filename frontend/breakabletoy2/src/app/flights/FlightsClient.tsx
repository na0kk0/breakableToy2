'use client'

import {ResponseDTO, RoundTrip, searchFlights} from "../../../lib/api";

import {useSearchParams} from "next/navigation";
import {useEffect, useState} from "react";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {faChevronDown, faChevronLeft, faChevronRight, faPlane} from '@fortawesome/free-solid-svg-icons';
import { useRouter } from 'next/navigation';

export default function Flights(){
    const router = useRouter();

    const searchParams = useSearchParams();
    const [results, setResults] = useState<ResponseDTO>({flights:[],total:0});
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [page, setPage] = useState(1);
    const [sortBy, setSortBy] = useState("");
    const [currency, setCurrency] = useState("");
    const [sortDisplay, setSortDisplay] = useState("Price - lower to higher");

    useEffect(() => {
        const fetchFlights = async ()=>{
            console.log("DEBUG: entering fetch flights")
            setIsLoading(true);
            const formData = {
                originLocationCode: searchParams.get('originLocationCode') || '',
                destinationLocationCode: searchParams.get('destinationLocationCode') || '',
                departureDate: searchParams.get('departureDate') || '',
                returnDate: searchParams.get('returnDate') || '',
                adults: searchParams.get('adults') || '1',
                nonStop: searchParams.get('nonStop') === 'true',
                currency: searchParams.get('currency') || 'EUR',
            };
            try{
                setResults(await searchFlights(formData.originLocationCode, formData.destinationLocationCode, formData.departureDate,
                    formData.returnDate, formData.adults, formData.nonStop, (currency == "" ? formData.currency : currency), page, sortBy));
                console.log("DEBUG: ")
                console.log(results);
            }catch (err){
                setIsLoading(false);
                console.log(err);
                setResults({flights:[], total:0});
            }finally {
                setIsLoading(false);
            }
        }
        console.log("DEBUG: entering useEffect")
        fetchFlights();

    }, [searchParams, sortBy, page, currency]);

    function handleSortMenu(){
        const sortMenuDiv = document.getElementById("sortMenu");
        if (sortMenuDiv) {
            if (sortMenuDiv.classList.contains('hidden')) {
                sortMenuDiv.classList.remove('hidden', 'pointer-events-none');

                void sortMenuDiv.offsetWidth;
                sortMenuDiv.classList.remove('opacity-0', 'invisible');
                sortMenuDiv.classList.add('opacity-100', 'visible');
            } else {
                sortMenuDiv.classList.remove('opacity-100', 'visible');
                sortMenuDiv.classList.add('opacity-0', 'invisible');

                sortMenuDiv.addEventListener('transitionend', function handler() {
                    sortMenuDiv.classList.add('hidden', 'pointer-events-none');
                    sortMenuDiv.removeEventListener('transitionend', handler);
                }, { once: true });
            }
        }
    }

    function handleCurrencyMenu(){
        const sortMenuDiv = document.getElementById("currencyMenu");
        if (sortMenuDiv) {
            if (sortMenuDiv.classList.contains('hidden')) {
                sortMenuDiv.classList.remove('hidden', 'pointer-events-none');

                void sortMenuDiv.offsetWidth;
                sortMenuDiv.classList.remove('opacity-0', 'invisible');
                sortMenuDiv.classList.add('opacity-100', 'visible');
            } else {
                sortMenuDiv.classList.remove('opacity-100', 'visible');
                sortMenuDiv.classList.add('opacity-0', 'invisible');

                sortMenuDiv.addEventListener('transitionend', function handler() {
                    sortMenuDiv.classList.add('hidden', 'pointer-events-none');
                    sortMenuDiv.removeEventListener('transitionend', handler);
                }, { once: true });
            }
        }
    }

    function handleDetails(flight: RoundTrip) {
        localStorage.setItem("flight", JSON.stringify(flight));
        router.push("/flights/details");
    }

    if (isLoading) {
        return (
            <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100">
                <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-gray-900"></div>
                <FontAwesomeIcon icon={faPlane} className="planeAnimation pt-4"/>
                <p className="mt-4 text-xl text-gray-700">Searching for flights...</p>
            </div>
        );
    }
    if (results.flights.length == 0){
        return(
            <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100">
                <p className="mt-4 text-xl text-gray-700">Looks like there's no available flights</p>
                <div className="flex pt-4"><button className="bg-blue-400 px-2 py-1 text-lg rounded-lg text-white shadow-md/30 hover:bg-white hover:text-blue-400 cursor-pointer transition duration-300s" onClick={()=> router.back()}>Return</button></div>
            </div>
        )
    }
    return (
        <div className="w-full flex gap-10 items-center flex-col bg-gray-200">
            <div className="w-2/3 h-15 bg-white flightSortPanel rounded-b-4xl shadow-xl flex gap-4 px-4 justify-around place-items-center text-black text-lg">
                <p>Origin Location: {searchParams.get("originLocationCode")}</p>
                <p>Destination Location: {searchParams.get("destinationLocationCode")}</p>
                <p>Departure Date: {searchParams.get("departureDate")}</p>
                <p>Return Date: {searchParams.get("departureDate") == "" ? ("-") : (searchParams.get("departureDate"))}</p>
                <div className="flex pr-4 text-gray-600 relative text-black">
                    <p className="text-black">Currency: {searchParams.get("currency")}<button className="cursor-pointer" onClick={handleCurrencyMenu}><FontAwesomeIcon icon={faChevronDown} className="w-10 h-10 text-black" /></button></p>
                    <div className="absolute top-8 right-10 w-30 bg-white shadow-lg p-2 rounded-md z-10 opacity-0 invisible transition-opacity duration-300 ease-in-out pointer-events-none hidden" id="currencyMenu">
                        <p className="hover:text-gray-400 cursor-pointer text-center" onClick={()=> {
                            const params = new URLSearchParams(searchParams.toString());
                            params.set("currency", "EUR");
                            router.push(`?${params.toString()}`);
                            handleCurrencyMenu();
                        }}>EUR</p>
                        <p className="hover:text-gray-400 cursor-pointer text-center" onClick={()=> {
                            const params = new URLSearchParams(searchParams.toString());
                            params.set("currency", "MXN");
                            router.push(`?${params.toString()}`);
                            handleCurrencyMenu();
                        }}>MXN</p>
                        <p className="hover:text-gray-400 cursor-pointer text-center" onClick={()=> {
                            const params = new URLSearchParams(searchParams.toString());
                            params.set("currency", "USD");
                            router.push(`?${params.toString()}`);
                            handleCurrencyMenu();
                        }}>USD</p>
                    </div>
                </div>
            </div>
            <div className="flex justify-end w-2/3">
                <div className="flex pr-4 text-gray-600 relative">
                    sort by: {sortDisplay}<button className="pl-2 cursor-pointer" onClick={handleSortMenu}><FontAwesomeIcon icon={faChevronDown} className="w-4"/></button>
                    <div className="absolute top-5 w-50 bg-white shadow-lg p-2 rounded-md z-10 opacity-0 invisible transition-opacity duration-300 ease-in-out pointer-events-none hidden" id="sortMenu">
                        <p className="hover:text-gray-400 cursor-pointer text-center" onClick={()=> {
                            setSortBy("PriceUp")
                            setSortDisplay("Price - lower to higher")
                            handleSortMenu();
                        }}>Price - lower to higher</p>
                        <p className="hover:text-gray-400 cursor-pointer text-center" onClick={()=> {
                            setSortBy("PriceDown")
                            setSortDisplay("Price - higher to lower")
                            handleSortMenu();
                        }}>Price - higher to lower</p>
                        <p className="hover:text-gray-400 cursor-pointer text-center" onClick={()=> {
                            setSortBy("TimeUp")
                            setSortDisplay("Time - shorter to longer")
                            handleSortMenu();
                        }}>Time - shorter to longer</p>
                        <p className="hover:text-gray-400 cursor-pointer text-center" onClick={()=> {
                            setSortBy("TimeDown")
                            setSortDisplay("Time - longer to shorter")
                            handleSortMenu()
                        }}>Time - longer to shorter</p>
                    </div>
                </div>
            </div>
            {results && results.flights.length > 0 ? (
                results.flights.map((result:RoundTrip, index:number) => (
                    <div key={index} className="grid grid-cols-4 grid-rows-2 text-black w-2/3 min-h-90 rounded-lg shadow-lg/30 bg-gradient-to-b from-white to-white hover:from-white hover:to-zinc-200 transition duration-300">
                        <div id="flightGo" className="col-start-1 col-end-4 row-start-1 row-span-1 rounded-tl-md flex flex-col justify-center relative">
                            <div className="absolute top-0 left-0 pl-4 pt-3">
                                <p className="text-lg text-center">{result.flightGo.departureDay} - Departure</p>
                            </div>
                            <div className="flex justify-center items-center w-full px-4 relative">
                                <div className="flex flex-col w-1/5 relative justify-center items-center">
                                    <p className="text-3xl pb-1">{result.flightGo.departureTime}</p>
                                    <div className="flex flex-col absolute top-10 items-center text-center">
                                        <p className="text-md font-light text-gray-500 ">{result.flightGo.departureAirportName}</p>
                                        <p className="text-md font-light text-gray-500 ">({result.flightGo.departureAirportCode})</p>
                                    </div>
                                </div>
                                <div className="bg-gray-300 flex-grow h-0.5"></div>
                                <div className="relative h-full flex flex-col items-center justify-center ">
                                    <p className="pt-10">{result.flightGo.totalTime}</p>
                                    <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2">
                                        <FontAwesomeIcon icon={faPlane} className="w-6 h-6 rotate-270 text-blue-500"/>
                                    </div>
                                    <div className="flex flex-col items-center text-center mt-25">
                                        <p className="">{result.flightGo.stops.length < 1 ? "NonStop" : result.flightGo.stops.length == 1 ? result.flightGo.stops.length + " stop" : result.flightGo.stops.length + " stops"}</p>
                                        <div className="flex flex-col items-center mt-2">
                                            {result.flightGo.stops.length > 0 && result.flightGo.stops.map((stop, index) => {
                                                return (
                                                    <p key={index} className="text-sm text-gray-600 whitespace-nowrap">{stop.duration} in {stop.airportName} ({stop.airportCode})</p>
                                                )
                                            })}
                                        </div>
                                    </div>
                                </div>
                                <div className="bg-gray-300 flex-grow h-0.5"></div>
                                <div className="flex flex-col w-1/5 relative justify-center items-center">
                                    <p className="text-3xl pb-1">{result.flightGo.arrivalTime}</p>
                                    <div className="flex flex-col absolute top-10 items-center">
                                        <p className="text-md font-light text-gray-500 text-center">{result.flightGo.arrivalAirportName}</p>
                                        <p className="text-md font-light text-gray-500 ">({result.flightGo.arrivalAirportCode})</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                        {result.flightReturn != null ? (
                            <div id="flightReturn" className="col-start-1 col-end-4 row-start-2 row-span-1 rounded-tl-md flex flex-col justify-center relative">
                                <div className="absolute top-0 left-0 pl-4 pt-3">
                                    <p className="text-lg text-center">{result.flightReturn.departureDay} - Return</p>
                                </div>
                                <div className="flex justify-center items-center w-full px-4">
                                    <div className="flex flex-col w-1/5 relative justify-center items-center">
                                        <p className="text-3xl pb-1">{result.flightReturn.departureTime}</p>
                                        <div className="flex flex-col absolute top-10 items-center">
                                            <p className="text-md font-light text-gray-500 text-center">{result.flightReturn.departureAirportName}</p>
                                            <p className="text-md font-light text-gray-500 ">({result.flightReturn.departureAirportCode})</p>
                                        </div>
                                    </div>
                                    <div className="bg-gray-300 flex-grow h-0.5"></div>
                                    <div className="relative h-full flex flex-col items-center justify-center">
                                        <p className="pt-10">{result.flightReturn.totalTime}</p>
                                        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2">
                                            <FontAwesomeIcon icon={faPlane} className="w-6 h-6 rotate-270 text-blue-500"/>
                                        </div>
                                        <div className="flex flex-col items-center text-center mt-20">
                                            <p className="">{result.flightGo.stops.length < 1 ? "NonStop" : result.flightReturn.stops.length == 1 ? result.flightReturn.stops.length + " stop" : result.flightReturn.stops.length + " stops"}</p>
                                            <div className="flex flex-col items-center mt-2">
                                                {result.flightReturn.stops.length > 0 && result.flightReturn.stops.map((stop, index) => {
                                                    return (
                                                        <p key={index} className="text-sm text-gray-600 whitespace-nowrap">{stop.duration} in {stop.airportName} ({stop.airportCode})</p>
                                                    )
                                                })}
                                            </div>
                                        </div>
                                    </div>
                                    <div className="bg-gray-300 flex-grow h-0.5"></div>
                                    <div className="flex flex-col w-1/5 relative justify-center items-center">
                                        <p className="text-3xl pb-1">{result.flightReturn.arrivalTime}</p>
                                        <div className="flex flex-col absolute top-10 items-center">
                                            <p className="text-md font-light text-gray-500 text-center">{result.flightReturn.arrivalAirportName}</p>
                                            <p className="text-md font-light text-gray-500 ">({result.flightReturn.arrivalAirportCode})</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        ):(
                            <div></div>
                        )}
                        <div id="price" className="col-start-4 col-end-5 row-start-1 row-end-3 rounded-r-md flex flex-col justify-center gap-10 items-center border-l-2 border-gray-300">
                            <div>
                                <p className="text-xl font-semibold">{searchParams.get("currency") == "EUR" ? "€": "$"}{result.flightGo.totalPrice.toFixed(2)} - {searchParams.get("currency")}</p>
                                <p className="text-xl font-semibold float-right">Total</p>
                            </div>
                            <div>
                                <p className="text-xl font-semibold">{searchParams.get("currency") == "EUR" ? "€": "$"}{result.flightGo.pricePerTraveler.toFixed(2)} - {searchParams.get("currency")}</p>
                                <p className="text-xl font-semibold float-right">Per traveler</p>
                            </div>
                            <button className="bg-blue-400 px-2 py-1 text-lg rounded-lg text-white shadow-md/30 hover:bg-white hover:text-blue-400 cursor-pointer transition duration-300" onClick={()=>{handleDetails(result)}}>See details</button>
                        </div>
                    </div>
                ))
            ) : (
                <div>
                    <p>Looks like there's no flights available</p>
                </div>
            )}
            <div className="flex w-full relative">
                <div className={results.flights.length <= 2 ? ("absolute left-1/2 transform -translate-x-1/2 self-center text-lg text-gray-600 flex bg-white w-full h-15 rounded-t-4xl shadow-xl fixed bottom-0 justify-around items-center") : ("absolute left-1/2 transform -translate-x-1/2 self-center text-lg text-gray-600 flex bg-white w-1/3 h-15 rounded-t-4xl shadow-xl justify-around items-center")}>
                    <button className={page == 1 ? ("text-gray-400") : ("cursor-pointer hover:text-gray-400")} onClick={()=>{setPage(page == 1 ? 1 : (page-1))}}><FontAwesomeIcon icon={faChevronLeft} className="w-3"/></button>
                    {Array.from({length: results.total}).map((_, index) => {
                        return(
                            <p key={index} className={page == (index + 1) ? ("text-gray-400") : ("cursor-pointer hover:text-gray-400")} onClick={()=>{setPage(index + 1)}}>{index + 1}</p>
                        )
                    })}
                    <button className={page == results.total ? ("text-gray-400") : ("cursor-pointer hover:text-gray-400")} onClick={()=>{setPage(page == results.total ? results.total : (page+1))}}><FontAwesomeIcon icon={faChevronRight} className="w-3"/></button>
                </div>
                <div className="flex justify-end w-full pr-10 pb-2"><button className="bg-blue-400 px-2 py-1 text-lg rounded-lg text-white shadow-md/30 hover:bg-white hover:text-blue-400 cursor-pointer transition duration-300s" onClick={()=> router.push("/search")}>Return</button></div>
            </div>
        </div>
    )
}