const BASE_URL = "http://localhost:8080/api"

export async function fetchLocations() {
    const res = await fetch(`${BASE_URL}/facilities`);
    if (!res.ok) throw new Error("Failed to fetch facilities");
    const data = await res.json();
    return [...new Set(data.map((d) => d.locationName))];
}

export async function fetchFacilityData(location, date) {
    const [year, month, day] = date.split("-").map(Number);
    const start = new Date(year, month - 1, day, 0, 0, 0, 0);
    const end = new Date(year, month - 1, day, 23, 59, 59, 999);
    const startStr = start.toISOString().slice(0, 19);
    const endStr = end.toISOString().slice(0, 19);

    const res = await fetch(
        `${BASE_URL}/facilities?locationName=${encodeURIComponent(location)}&start=${startStr}&end=${endStr}`
    );
    if (!res.ok) throw new Error("Failed to fetch facility data");
    const data = await res.json();

    return data
        .map((d) => ({
            time: new Date(d.lastUpdatedDateAndTime),
            count: d.lastCount,
        }))
        .sort((a, b) => a.time - b.time)
        .map((d) => ({
            ...d,
            time: d.time.toLocaleString(),
        }));
}