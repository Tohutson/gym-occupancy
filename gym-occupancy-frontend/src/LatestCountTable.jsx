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

  // Helper: format ISO timestamp to "HH:mm:ss"
  const formatToTime = (iso) => new Date(iso).toTimeString().slice(0, 8);

  // Helper: format last updated timestamp for display
  const formatUpdated = (iso) => {
    if (!iso) return "—";
    const d = new Date(iso);
    return isNaN(d.getTime()) ? "Invalid date" : d.toLocaleString();
  };

  // Handle row click
  const handleRowClick = (name) => onSelectLocation?.(name);

  useEffect(() => {
    const controller = new AbortController();

    // Single function: fetch latest counts + averages
    const fetchAll = async () => {
      try {
        setError(null);
        if (!data.length) setLoading(true); // only show loading on first fetch

        // Fetch latest counts
        const res = await fetch(`${apiUrl}/latest`, { signal: controller.signal });
        if (!res.ok) throw new Error(`HTTP ${res.status} ${res.statusText}`);
        const latest = await res.json();
        if (!Array.isArray(latest)) throw new Error("Expected an array from latest API");

        // Fetch averages in parallel
        const combined = await Promise.all(
          latest.map(async (loc) => {
            const time = formatToTime(loc.recordedAt);
            const resAvg = await fetch(`${apiUrl}/average?locationName=${encodeURIComponent(loc.locationName)}&time=${encodeURIComponent(time)}`, {
              signal: controller.signal
            });
            const avg = await resAvg.json();
            return { ...loc, average: avg.average };
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

    fetchAll();

    // Polling
    const intervalId = refreshIntervalMs
      ? setInterval(fetchAll, refreshIntervalMs)
      : null;

    return () => {
      controller.abort();
      if (intervalId) clearInterval(intervalId);
    };
  }, [apiUrl, refreshIntervalMs, data.length]);

  const getColor = (current, average) => {
    if (average === 0 || average == null || current == null) return "gray";
    const diff = current - average;
    const threshold = average * 0.25; // ±5% neutral range
  
    if (Math.abs(diff) < threshold) return "black";
    return diff < 0 ? "green" : "red";
  };

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
              <th style={{ textAlign: "left", padding: 8, borderBottom: "1px solid #ccc" }}>
                Location
              </th>
              <th style={{ textAlign: "right", padding: 8, borderBottom: "1px solid #ccc" }}>
                Current Count
              </th>
              <th style={{ textAlign: "right", padding: 8, borderBottom: "1px solid #ccc" }}>
                Average
              </th>
              <th style={{ textAlign: "right", padding: 8, borderBottom: "1px solid #ccc" }}>
                Last Updated
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
                onKeyDown={(e) =>
                  onSelectLocation &&
                  (e.key === "Enter" || e.key === " ") &&
                  handleRowClick(loc.locationName)
                }
                style={{
                  cursor: onSelectLocation ? "pointer" : "default",
                  borderTop: "1px solid #eee",
                }}
              >
                <td style={{ padding: "8px 4px" }}>{loc.locationName}</td>
  
                {/* Current Count */}
                <td
                  style={{
                    padding: "8px 4px",
                    textAlign: "right",
                    color: getColor(loc.lastCount, loc.average),
                    fontWeight: "bold"
                  }}
                >
                  {loc.lastCount ?? "—"}
                </td>
  
                {/* Average (2 decimal places) */}
                <td style={{ padding: "8px 4px", textAlign: "right" }}>
                  {loc.average !== undefined && loc.average !== null
                    ? loc.average.toFixed(2)
                    : "—"}
                </td>
  
                {/* Last Updated */}
                <td style={{ padding: "8px 4px", textAlign: "right" }}>
                  {formatUpdated(loc.lastUpdatedDateAndTime)}
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
