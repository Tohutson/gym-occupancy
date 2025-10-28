import { useEffect, useState } from "react";
import OccupancyChart from "./OccupancyChart";
import LatestCountTable from "./LatestCountTable";
import { fetchLocations, fetchFacilityData } from "./api/facilities";

function App() {
  const [facilities, setFacilities] = useState([]);
  const [locations, setLocations] = useState([]);
  const [selectedLocation, setSelectedLocation] = useState("");
  const [selectedDate, setSelectedDate] = useState(() => {
    const today = new Date();
    return today.toISOString().slice(0, 10);
  });
  const [loading, setLoading] = useState(false);

  // Fetch all locations once to populate dropdown
  useEffect(() => {
    fetchLocations()
      .then((uniqueLocations) => {
        setLocations(uniqueLocations);
        if (!selectedLocation && uniqueLocations.length)
          setSelectedLocation("RWC Floor 2");
      })
      .catch((err) => console.error("Error fetching locations:", err));
  }, []);

  // Fetch data for the selected facility and day
  useEffect(() => {
    if (!selectedLocation || !selectedDate) return;
    setLoading(true);
    fetchFacilityData(selectedLocation, selectedDate)
    .then(setFacilities)
    .catch((err) => console.error("error fetching facility data:", err))
    .finally(() => setLoading(false));
  }, [selectedLocation, selectedDate]);
    

  return (
    <div style={{ padding: "20px", fontFamily: "sans-serif" }}>
      <h2>Real-Time Gym Occupancy</h2>

      {/* Facility selector */}
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

      {/* Data display */}
      {loading ? (
        <p>Loading occupancy data...</p>
      ) : facilities.length > 0 ? (
        <>
          <LatestCountTable onSelectLocation={setSelectedLocation} />
          <OccupancyChart data={facilities} />
        </>
      ) : (
        <p>No data available for this date/location.</p>
      )}
    </div>
  );
}

export default App;
