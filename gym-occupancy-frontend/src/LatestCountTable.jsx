import { useEffect, useState } from "react";

export default function LatestCountTable({
  apiUrl = "http://localhost:8080/api/facilities",
  refreshIntervalMs = 600000,
  onSelectLocation,
}) {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lastFetch, setLastFetch] = useState(null);

  // Helper: convert ISO timestamp to HH:mm:ss
  const formatToTime = (isoString) => {
    const date = new Date(isoString);
    return date.toTimeString().slice(0, 8); // HH:mm:ss
  };

  // Helper: format last updated timestamp for table
  const formatUpdated = (ts) => {
    if (!ts) return "—";
    const d = new Date(ts);
    return Number.isNaN(d.getTime()) ? "Invalid date" : d.toLocaleString();
  };

  const handleRowClick = (locationName) => {
    onSelectLocation?.(locationName);
  };

  // Combined fetch: latest counts + averages
  const fetchAll = async (signal) => {
    try {
      setError(null);
      setLoading(true);

      const resLatest = await fetch(`${apiUrl}/latest`, { signal });
      if (!resLatest.ok) throw new Error(`HTTP ${resLatest.status} ${resLatest.statusText}`);
      const latest = await resLatest.json();
      if (!Array.isArray(latest)) throw new Error("Invalid response shape: expected an array");

      // Fetch averages in parallel
      const combined = await Promise.all(
        latest.map(async (loc) => {
          const time = formatToTime(loc.recordedAt);
          const resAvg = await fetch(`${apiUrl}/average?time=${time}`, { signal });
          if (!resAvg.ok) throw new Error(`Average fetch failed for ${time}`);
          const avgData = await resAvg.json();
          return { ...loc, average: avgData.average };
        })
      );

      setData(combined);
      setLastFetch(Date.now());
    } catch (err) {
      if (err.name !== "AbortError") setError(err.message || String(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const controller = new AbortController();

    fetchAll(controller.signal); // initial fetch

    // Polling
    let intervalId;
    if (refreshIntervalMs) {
      intervalId = setInterval(() => {
        fetchAll(controller.signal).catch(() => {});
      }, refreshIntervalMs);
    }

    return () => {
      controller.abort();
      if (intervalId) clearInterval(intervalId);
    };
  }, [apiUrl, refreshIntervalMs]);

  return (
    <div style={{ marginTop: 16 }}>
      <h3>Latest Counts by Location</h3>
      {loading && <p>Loading latest counts…</p>}
      {error && <p style={{ color: "red" }}>Error: {error}</p>}
      {!loading && !error && data.length === 0 && <p>No data available.</p>}

      {!loading && !error && data.length > 0 && (
        <table style={{ width: "100%", borderCollapse: "collapse" }} aria-live="polite">
          <thead>
            <tr>
              <th style={{ textAlign: "left", padding: "8px", borderBottom: "1px solid #ccc" }}>
                Location
              </th>
              <th style={{ textAlign: "right", padding: "8px", borderBottom: "1px solid #ccc" }}>
                Current Count
              </th>
              <th style={{ textAlign: "left", padding: "8px", borderBottom: "1px solid #ccc" }}>
                Last Updated
              </th>
              <th style={{ textAlign: "right", padding: "8px", borderBottom: "1px solid #ccc" }}>
                Average
              </th>
            </tr>
          </thead>
          <tbody>
            {data.map((loc) => (
              <tr
                key={loc.id}
                onClick={() => handleRowClick(loc.locationName)}
                role={onSelectLocation ? "button" : undefined}
                tabIndex={onSelectLocation ? 0 : undefined}
                onKeyDown={(e) => {
                  if (onSelectLocation && (e.key === "Enter" || e.key === " ")) {
                    handleRowClick(loc.locationName);
                  }
                }}
                style={{
                  cursor: onSelectLocation ? "pointer" : "default",
                  borderTop: "1px solid #eee",
                }}
              >
                <td style={{ padding: "8px 4px" }}>{loc.locationName}</td>
                <td style={{ padding: "8px 4px", textAlign: "right" }}>
                  {loc.lastCount ?? "—"}
                </td>
                <td style={{ padding: "8px 4px" }}>{formatUpdated(loc.lastUpdatedDateAndTime)}</td>
                <td style={{ padding: "8px 4px", textAlign: "right" }}>
                  {loc.average ?? "—"}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {!loading && !error && lastFetch && (
        <div style={{ marginTop: 8, color: "#666", fontSize: 12 }}>
          Last updated: {new Date(lastFetch).toLocaleTimeString()}
        </div>
      )}
    </div>
  );
}
