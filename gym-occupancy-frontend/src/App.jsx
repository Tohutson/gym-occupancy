import { useEffect, useState } from "react";
import OccupancyChart from "./OccupancyChart";

function App() {
  const [facilities, setFacilities] = useState([]);

  // Runs once after component loads
  useEffect(() => {
    fetch("http://localhost:8080/api/facilities")
      .then((res) => res.json())
      .then((facilities) => {
        // Convert timestamps into readable times for the X-axis
        const formatted = facilities.map((d) => ({
          time: new Date(d.lastUpdatedDateAndTime).toLocaleTimeString(),
          count: d.lastCount,
        }));
        setFacilities(formatted);
      })
      .catch((err) => console.error(err));
  }, []);

  return (
    <div>
      <h2>Real-Time Gym Occupancy</h2>
      <OccupancyChart data={facilities} />
    </div>
  );
}

export default App;
