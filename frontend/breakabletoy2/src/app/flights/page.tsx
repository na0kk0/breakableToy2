import { Suspense } from "react";
import FlightsClient from "./FlightsClient";

export default function FlightsPage() {
    return (
        <Suspense fallback={<div className="p-10 text-center">Loading search...</div>}>
            <FlightsClient />
        </Suspense>
    );
}
