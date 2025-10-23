import { useEffect, useState } from "react";
import OccupancyChart from "./OccupancyChart";

function App() {
  const [facilities, setFacilities] = useState([]);
  const [locations, setLocations] = useState([]);
  const [selectedLocation, setSelectedLocation] = useState("");
  const [selectedDate, setSelectedDate] = useState(() => {
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, "0");
    const dd = String(today.getDate()).padStart(2, "0");
    return `${yyyy}-${mm}-${dd}`; // "2025-10-22"
  });

  // Fetch all facilities once to populate dropdown
  useEffect(() => {
    fetch("http://localhost:8080/api/facilities")
      .then((res) => res.json())
      .then((data) => {
        const uniqueLocations = [...new Set(data.map((d) => d.locationName))];
        setLocations(uniqueLocations);

        if (uniqueLocations.length > 0 && !selectedLocation) {
          setSelectedLocation("RWC Floor 2");
        }
      })
      .catch((err) => console.error("Error fetching locations:", err));
  }, []);

  // Fetch data for the selected facility and day
  useEffect(() => {
    if (!selectedLocation || !selectedDate) return;

    // Convert selected date to start and end ISO timestamps
    const [year, month, day] = selectedDate.split("-").map(Number);
    const start = new Date(year, month - 1, day, 0, 0, 0, 0);
    const end = new Date(year, month - 1, day, 23, 59, 59, 999);

    const startStr = start.toISOString().slice(0, 19); // "2025-10-16T00:00:00"
    const endStr = end.toISOString().slice(0, 19);

    fetch(
      `http://localhost:8080/api/facilities?locationName=${encodeURIComponent(
        selectedLocation
      )}&start=${startStr}&end=${endStr}`
    )
      .then((res) => res.json())
      .then((data) => {
        const formatted = data
          .map((d) => ({
            time: new Date(d.lastUpdatedDateAndTime),
            count: d.lastCount,
          }))
          .sort((a, b) => a.time - b.time)
          .map((d) => ({
            ...d,
            time: d.time.toLocaleString(),
          }));
        setFacilities(formatted);
      })
      .catch((err) => console.error("Error fetching facility data:", err));
  }, [selectedLocation, selectedDate]);

  return (
    <div style={{ padding: "20px", fontFamily: "sans-serif" }}>
      <h2>Real-Time Gym Occupancy</h2>

      {/* Facility dropdown */}
      <div style={{ marginBottom: "10px" }}>
        <label htmlFor="facility-select" style={{ marginRight: "10px" }}>
          Select Facility:
        </label>
        <select
          id="facility-select"
          value={selectedLocation}
          onChange={(e) => setSelectedLocation(e.target.value)}
        >
          <option value="">-- Choose a facility --</option>
          {locations.map((loc) => (
            <option key={loc} value={loc}>
              {loc}
            </option>
          ))}
        </select>
      </div>

      {/* Date picker */}
      <div style={{ marginBottom: "20px" }}>
        <label htmlFor="date-select" style={{ marginRight: "10px" }}>
          Select Date:
        </label>
        <input
          type="date"
          id="date-select"
          value={selectedDate}
          onChange={(e) => setSelectedDate(e.target.value)}
        />
      </div>

      {facilities.length > 0 ? (
        <OccupancyChart data={facilities} />
      ) : (
        <p>Loading occupancy data...</p>
      )}
    </div>
  );
}

export default App;
