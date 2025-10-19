import { useEffect, useState } from "react";
import OccupancyChart from "./OccupancyChart";

function App() {
  const [facilities, setFacilities] = useState([]);
  const [locations, setLocations] = useState([]); // all unique facility names
  const [selectedLocation, setSelectedLocation] = useState(""); // dropdown selection

  // Fetch all facilities once to populate dropdown
  useEffect(() => {
    fetch("http://localhost:8080/api/facilities")
      .then((res) => res.json())
      .then((data) => {
        // Extract unique location names
        const uniqueLocations = [...new Set(data.map((d) => d.locationName))];
        setLocations(uniqueLocations);
      })
      .catch((err) => console.error("Error fetching locations:", err));
  }, []);

  // Fetch data for the selected facility
  useEffect(() => {
    if (!selectedLocation) return;

    fetch(
      `http://localhost:8080/api/facilities?locationName=${encodeURIComponent(
        selectedLocation
      )}`
    )
      .then((res) => res.json())
      .then((facilities) => {
        // Convert timestamps and sort by date
        const formatted = facilities
          .map((d) => ({
            time: new Date(d.lastUpdatedDateAndTime), // keep as Date for sorting
            count: d.lastCount,
          }))
          .sort((a, b) => a.time - b.time) // sort chronologically
          .map((d) => ({
            ...d,
            time: d.time.toLocaleString(), // format for display
          }));
        setFacilities(formatted);
      })
      .catch((err) => console.error("Error fetching facility data:", err));
  }, [selectedLocation]);

  return (
    <div style={{ padding: "20px", fontFamily: "sans-serif" }}>
      <h2>Real-Time Gym Occupancy</h2>

      {/* Dropdown */}
      <div style={{ marginBottom: "20px" }}>
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

      {/* Chart */}
      {selectedLocation ? (
        <OccupancyChart data={facilities} />
      ) : (
        <p>Please select a facility to view its occupancy.</p>
      )}
    </div>
  );
}

export default App;
