'use client'


import {useEffect, useState} from "react";
import { useRouter } from 'next/navigation';
import {RoundTrip, Segment} from "../../../../lib/api";

export default function Details(){

    const [flight, setFlight] = useState<RoundTrip | null>(null);
    const router = useRouter();

    useEffect(() => {
        const flight = JSON.parse(localStorage.getItem("flight") || "{}");
        setFlight(flight);
        console.log("DEBUG:" + JSON.stringify(flight));
    }, []);

    if (!flight) {
        return(
            <div>
                Look's like there's a problem with the flight details.
            </div>
        )
    }
    return(
        <div className="flex bg-white w-3/4 justify-self-center my-10 rounded-lg text-black gap-10 shadow-xl flex-col">
            <div className="flex gap-10">
                <div className="w-2/3 my-5 gap-5 flex flex-col">
                    {flight.segments.map((segment:Segment, index:number) => {
                        return (
                            <div key={index} className="flex bg-gray-200 w-full rounded-lg shadow-lg mx-10">
                                <div className="flex flex-col w-2/3 pl-4 pt-1 text-lg justify-around    ">
                                    <p>Segment {index + 1}</p>
                                    <div className="grid grid-cols-3 grid-rows-1 justify-items-center items-center">
                                        <p className="col-start-1 col-end-2">{segment.departureDate} {segment.departureTime}</p>
                                        <div className="col-start-2 col-end-3 bg-black w-1/4 h-0.5"></div>
                                        <p className="col-start-3 col-end-4">{segment.arrivalDate} {segment.arrivalTime}</p>
                                    </div>
                                    <div className="grid grid-cols-3 grid-rows-1 justify-items-center items-center text-center text-sm">
                                        <p className="col-start-1 col-end-2">{segment.departureAirportName} ({segment.departureAirportCode})</p>
                                        <div className="col-start-2 col-end-3 bg-black w-1/6 h-0.5"></div>
                                        <p className="col-start-3 col-end-4">{segment.arrivalAirportName} ({segment.arrivalAirportCode})</p>
                                    </div>
                                    <div>
                                        <p className="pt-2">{segment.airlineName} ({segment.airlineCode}) - Flight number: {segment.flightNumber}</p>
                                        <p className="py-1">{segment.aircraftType}</p>
                                        <p>{segment.layover == "" ? ("") : (`Layover: ${segment.layover}`)}</p>
                                    </div>
                                </div>
                                <div className="flex flex-col w-1/3 bg-gray-100 my-4 mx-4 rounded-lg pt-1 px-4">
                                    <p>Traveler fare details</p>
                                    <p>Cabin: {flight.fareDetails[index].cabin}</p>
                                    <p>Class: {flight.fareDetails[index].class_}</p>
                                    <p>Amenities:</p>
                                    <div className="pl-2">
                                        {flight.fareDetails[index].amenities.length < 1 ? ("No amenities") : (
                                            flight.fareDetails[index].amenities.map((amenitie, index:number) => {
                                                return (
                                                    <p key={index}>{amenitie.name}: {amenitie.chargeable ? ("Extra charge") : "No extra charge"}</p>
                                                )
                                            })
                                        )}
                                    </div>
                                </div>
                            </div>
                        )
                    })}
                </div>
                <div id="praceBreakDown" className="w-1/3 h-fit flex flex-col items-center bg-gray-300 mx-10 my-5 rounded-lg p-2 shadow-lg">
                    <p className="text-xl">Price breakdown</p>
                    <div className="flex flex-col w-2/3 pt-2 gap-1 text-lg">
                        <p>Base: {flight.currency == "EUR" ? ("€") : ("$")}{flight.base.toFixed(2)} {flight.currency}</p>
                        <p>Fees:
                            {flight.fees.map((fee, index:number) => {
                                return (
                                    <div key={index}>
                                        <p className="pl-2">{fee.type}: {flight.currency == "EUR" ? ("€") : ("$")}{fee.amount} {flight.currency}</p>
                                    </div>
                                )
                            })}
                        </p>
                        <p>Total: {flight.currency == "EUR" ? ("€") : ("$")}{flight.price.toFixed(2)} {flight.currency}</p>
                    </div>
                    <div id="perTraveler" className="className=flex bg-gray-100 flex-col w-2/3 my-2 gap-1 text-lg rounded-lg shadow-md">
                        <p className="text-xl justify-self-center">Per traveler</p>
                        <p className="pl-4">Base: {flight.currency == "EUR" ? ("€") : ("$")}{flight.basePerTraveler.toFixed(2)} {flight.currency}</p>
                        <p className="pl-4">Total: {flight.currency == "EUR" ? ("€") : ("$")}{flight.flightGo.pricePerTraveler.toFixed(2)} {flight.currency}</p>
                    </div>
                </div>
            </div>
            <div className="flex self-end pr-4 pb-2"><button className="bg-blue-400 px-2 py-1 text-lg rounded-lg text-white shadow-md/30 hover:bg-white hover:text-blue-400 cursor-pointer transition duration-300s" onClick={()=> router.back()}>Return</button></div>
        </div>
    )

}