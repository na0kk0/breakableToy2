import SearchFlightPanel from '@/app/ui/search-flight-panel'

export default function SearchPage(){
    return (
        <div className="flex flex-col gap-[32px] row-start-2 items-center sm:items-start w-full text-black ">
            <div className="flex flex-col justify-center self-center bg-white w-1/3 mt-6 rounded-2xl px-6 shadow-2xl">
                <div className="flex justify-center w-full py-4">
                    <h1 className="text-6xl">Flight Search</h1>
                </div>
                <div className="flex flex-col justify-center w-full py-4">
                    <SearchFlightPanel />
                </div>
            </div>
        </div>
    )
}