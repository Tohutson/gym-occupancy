import { useEffect, useState, useCallback } from "react";

/*
  Props:
    - apiUrl: string (default) - where to fetch latest counts
    - refreshIntervalMs: number | null - if provided, component will poll the API at this interval
    - onSelectLocation: function | undefined - optional callback when user clicks a row
*/
export default function LatestCountTable({
  apiUrl = "http://localhost:8080/api/facilities/latest",
  refreshIntervalMs = null,
  onSelectLocation,
}) {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lastFetch, setLastFetch] = useState(null);

  const fetchLatest = useCallback(async (signal) => {
    try {
      setError(null);
      if (!lastFetch) setLoading(true);

      const res = await fetch(apiUrl, { signal });
      if (!res.ok) {
        throw new Error(`HTTP ${res.status} ${res.statusText}`);
      }
      const json = await res.json();

      if (!Array.isArray(json)) {
        throw new Error("Invalid response shape: expected an array");
      }

      setData(json);
      setLastFetch(Date.now());
    } catch (err) {
      if (err.name === "AbortError") {
        return;
      }
      setError(err.message || String(err));
    } finally {
      setLoading(false);
    }
  }, [apiUrl, lastFetch]);

  useEffect(() => {
    const controller = new AbortController();
    const { signal } = controller;

    fetchLatest(signal);

    let timerId = null;
    if (refreshIntervalMs && typeof refreshIntervalMs === "number") {
      timerId = setInterval(() => {
        const pollController = new AbortController();
        fetchLatest(pollController.signal).catch(() => {
        });
      }, refreshIntervalMs);
    }

    return () => {
      controller.abort();
      if (timerId) clearInterval(timerId);
    };
  }, [apiUrl, refreshIntervalMs, fetchLatest]);

  const formatUpdated = (ts) => {
    if (!ts) return "—";
    const d = new Date(ts);
    if (Number.isNaN(d.getTime())) return "Invalid date";
    return d.toLocaleString();
  };

  const handleRowClick = (locationName) => {
    if (typeof onSelectLocation === "function") {
      onSelectLocation(locationName);
    }
  };

  return (
    <div style={{ marginTop: 16 }}>
      <h3>Latest Counts by Location</h3>

      {/* Loading / error states */}
      {loading && <p>Loading latest counts…</p>}
      {error && <p style={{ color: "red" }}>Error: {error}</p>}

      {/* When no data (after loading) */}
      {!loading && !error && data.length === 0 && <p>No data available.</p>}

      {/* Table */}
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
            </tr>
          </thead>
          <tbody>
            {data.map((loc) => {
              const key = loc.locationName;
              return (
                <tr
                  key={key}
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
                </tr>
              );
            })}
          </tbody>
        </table>
      )}

      {/* Footer: show last fetch time */}
      {!loading && !error && lastFetch && (
        <div style={{ marginTop: 8, color: "#666", fontSize: 12 }}>
          Last updated: {new Date(lastFetch).toLocaleTimeString()}
        </div>
      )}
    </div>
  );
}
