import { useEffect, useState } from "react";

function App() {
  const [facilities, setFacilities] = useState([]);

  // Runs once after component loads
  useEffect(() => {
    fetch("http://localhost:8080/api/facilities")
      .then((res) => res.json())
      .then((data) => setFacilities(data))
      .catch((err) => console.error("Error fetching data:", err));
  }, []);

  return (
    <div style={{ padding: "2rem" }}>
      <h1>🏋️ Pitt Gym Occupancy Dashboard</h1>

      <table border="1" cellPadding="8">
        <thead>
          <tr>
            <th>Facility</th>
            <th>Location</th>
            <th>Current Count</th>
            <th>Capacity</th>
            <th>Updated</th>
          </tr>
        </thead>
        <tbody>
          {facilities.map((f) => (
            <tr key={f.id}>
              <td>{f.facilityName}</td>
              <td>{f.locationName}</td>
              <td>{f.lastCount}</td>
              <td>{f.totalCapacity}</td>
              <td>{new Date(f.lastUpdatedDateAndTime).toLocaleString()}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default App;
