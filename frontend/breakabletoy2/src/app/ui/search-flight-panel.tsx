'use client'

import {Airport, searchCodes} from "../../../lib/api";
import { useState} from "react";
import { useRouter } from 'next/navigation';



export default function SearchFlightPanel() {
    const router = useRouter();

    const [airportsD, setAirportsD] = useState<Airport[]>([]);
    const [airportsA, setAirportsA] = useState<Airport[]>([]);

    const [minArrival, setMinArrival] = useState<string>(currentDatePlus());

    const [departureDate, setDepartureDate] = useState<string>("");
    const [arrivalDate, setArrivalDate] = useState<string>("");

    const [formData, setFormData] = useState({
        originLocationCode: "",
        destinationLocationCode: "",
        departureDate: "",
        returnDate: "",
        adults: "1",
        nonStop: false,
        currency: "EUR",
    });

    function currentDate():string{
        const date = new Date();
        return ""+date.getFullYear()+"-"+((date.getMonth()+1).toString().padStart(2,'0'))+"-"+(date.getDate().toString().padStart(2,'0'))+"";
    }
    function currentDatePlus():string{
        const date = new Date();
        return ""+date.getFullYear()+"-"+((date.getMonth()+1).toString().padStart(2,'0'))+"-"+((date.getDate()+1).toString().padStart(2,'0'))+"";
    }

    function handleDate(minDate:string):void{
        const departureDate = new Date(minDate);
        departureDate.setDate(departureDate.getDate()+1);
        const nextDay = `${departureDate.getFullYear()}-${(departureDate.getMonth()+1).toString().padStart(2, '0')}-${(departureDate.getDate()).toString().padStart(2,'0')}`;
        setMinArrival(nextDay);
        setDepartureDate(minDate);
        if(Date.parse(minDate) >= Date.parse(arrivalDate)){
            setArrivalDate("");
        }
    }

    const handleChange = (e:any)=>{
        const {name, value, type, checked} = e.target;
        setFormData(prevParams => ({
            ...prevParams,
            [name]: type === "checkbox" ? checked : value,
        }));
    };

    function handleSearch(){
        if(formData.originLocationCode == "" || formData.destinationLocationCode == "" || formData.departureDate == ""){
            alert("Please fill all the required fields");
            return;
        }
        const queryParams = new URLSearchParams(formData as unknown as Record<string, string>).toString();

        router.push(`/flights?${queryParams}`);
    }

    return(
        <div className="flex flex-col justify-center w-full gap-6">
            <div className="flex w-full justify-center gap-5">
                <label className="flex text-xl justify-center">Departure Airport</label>
                <div className="flex flex-col justify-center w-1/4 gap-0.5">
                    <input name="originLocationCode" id="departure-airport-input" autoComplete="off" className="w-full border-2 border-gray-300 rounded-lg px-1 text-lg hover:bg-gray-100" list="departure-airports" type="text" required onChange={async (e)=>{setAirportsD(await searchCodes(e.target.value))
                        handleChange(e)}} />
                    <datalist id="departure-airports">
                        {airportsD.length > 0 && airportsD.map((d) => (
                            <option
                                key={d.id}
                                value={d.iataCode}
                            >
                                {}
                                {`${d.name} (${d.iataCode}), ${d.cityName}, ${d.countryName}`}
                            </option>
                        ))}
                    </datalist>
                </div>
            </div>
            <div className="flex w-full justify-center gap-5">
                <label className="flex text-xl justify-center">Arrival Airport</label>
                <div className="flex flex-col justify-center w-1/4 gap-0.5">
                    <input name="destinationLocationCode" required id="arrival-airport-input" autoComplete="off" className="w-full border-2 border-gray-300 rounded-lg px-1 text-lg hover:bg-gray-100" list="arrival-airports" type="text" onChange={async (e)=>{setAirportsA(await searchCodes(e.target.value))
                         handleChange(e)}} />
                    <datalist id="arrival-airports">
                        {airportsA.length > 0 && airportsA.map((d) => (
                            <option
                                key={d.id}
                                value={d.iataCode}
                            >
                                {}
                                {`${d.name} (${d.iataCode}), ${d.cityName}, ${d.countryName}`}
                            </option>
                        ))}
                    </datalist>
                </div>
            </div>
            <div className="flex w-full justify-center gap-5">
                <label className="flex text-xl justify-center">Departure date</label>
                <div className="flex flex-col justify-center w-1/4 gap-0.5">
                    <input name="departureDate" required id="departure-date" autoComplete="off" className="w-full border-2 border-gray-300 rounded-lg px-1 text-lg hover:bg-gray-100" type="date" min={currentDate()} onChange={(e)=>{handleDate(e.currentTarget.value)
                        handleChange(e)}} value={departureDate}/>
                </div>
            </div>
            <div className="flex w-full justify-center gap-5">
                <label className="flex text-xl justify-center">Arrival date</label>
                <div className="flex flex-col justify-center w-1/4 gap-0.5">
                    <input name="returnDate" required id="arrival-date" autoComplete="off" className="w-full border-2 border-gray-300 rounded-lg px-1 text-lg hover:bg-gray-100" type="date" min={minArrival} onChange={(e)=>{setArrivalDate(e.currentTarget.value)
                        handleChange(e)}} value={arrivalDate}/>
                </div>
            </div>
            <div className="flex w-full justify-center gap-5">
                <label className="flex text-xl justify-center">Currency</label>
                <div className="flex flex-col justify-center w-1/4 gap-0.5">
                    <select name="currency" id="currency" className="w-full h-9 border-2 border-gray-300 rounded-lg px-1 text-lg hover:bg-gray-100" onChange={(e)=>{handleChange(e)}}>
                        <option value="EUR">EUR</option>
                        <option value="MXN">MXN</option>
                        <option value="USD">USD</option>
                    </select>
                </div>
            </div>
            <div className="flex w-full justify-center gap-5">
                <label className="text-xl">Number of adults</label>
                <div className="flex flex-col justify-center w-1/4">
                    <input name="adults" required id="passengers" autoComplete="off" className="w-1/3 border-2 border-gray-300 rounded-lg px-1 text-lg hover:bg-gray-100" min="1" max="10" type="number" value={formData.adults} onChange={(e)=>{handleChange(e)}}/>
                </div>
            </div>
            <div className="flex w-full justify-center">
                <label className="text-xl">Non-Stop</label>
                <div className="flex flex-col justify-center w-1/4">
                    <input name="nonStop" id="non-stop" className="w-full h-5 border-2 border-gray-300 rounded-lg px-1 text-lg hover:bg-gray-100" type="checkbox" onChange={(e)=>{handleChange(e)}}/>
                </div>
            </div>
            <div className="flex justify-center pt-4">
                <button className="bg-blue-400 px-2 py-1 text-lg rounded-lg text-white shadow-md/30 hover:bg-white hover:text-blue-400 cursor-pointer transition duration-300" onClick={() => {handleSearch()}}>Search</button>
            </div>
        </div>
    )
}