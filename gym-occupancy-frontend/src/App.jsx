import { useCallback, useEffect, useState } from "react";
import "./App.css";
import OccupancyChart from "./OccupancyChart";
import { fetchDashboard, fetchFacilities } from "./api/facilities";
import { comparisonTone, formatRelativeAge } from "./dashboardUtils";

const RANGES = [
  ["today", "Today"],
  ["24h", "24 hours"],
  ["7d", "7 days"],
];
const REFRESH_INTERVAL_MS = 5 * 60 * 1000;

function formatNumber(value) {
  return value == null ? "—" : Math.round(value).toLocaleString();
}

function formatTime(value, timezone) {
  if (!value) return "—";
  return new Intl.DateTimeFormat("en-US", {
    timeZone: timezone,
    hour: "numeric",
    minute: "2-digit",
  }).format(new Date(value));
}

function formatWeekday(value, timezone) {
  return new Intl.DateTimeFormat("en-US", {
    timeZone: timezone,
    weekday: "long",
  }).format(new Date(value));
}

function App() {
  const [facilities, setFacilities] = useState([]);
  const [facilityId, setFacilityId] = useState("");
  const [range, setRange] = useState("today");
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const controller = new AbortController();
    fetchFacilities(controller.signal)
      .then((items) => {
        setFacilities(items);
        if (items.length) setFacilityId((current) => current || items[0].id);
        else setLoading(false);
      })
      .catch((requestError) => {
        if (requestError.name !== "AbortError") {
          setError(requestError.message);
          setLoading(false);
        }
      });
    return () => controller.abort();
  }, []);

  const loadDashboard = useCallback(
    async (signal, background = false) => {
      if (!facilityId) return;
      background ? setRefreshing(true) : setLoading(true);
      setError("");
      try {
        setDashboard(await fetchDashboard(facilityId, range, signal));
      } catch (requestError) {
        if (requestError.name !== "AbortError") setError(requestError.message);
      } finally {
        background ? setRefreshing(false) : setLoading(false);
      }
    },
    [facilityId, range]
  );

  useEffect(() => {
    const controller = new AbortController();
    loadDashboard(controller.signal);
    const interval = window.setInterval(() => loadDashboard(controller.signal, true), REFRESH_INTERVAL_MS);
    return () => {
      controller.abort();
      window.clearInterval(interval);
    };
  }, [loadDashboard]);

  if (loading && !dashboard) {
    return <main className="app-shell"><div className="loading-state">Loading occupancy data…</div></main>;
  }

  if (error && !dashboard) {
    return (
      <main className="app-shell">
        <div className="error-state">
          <h1>Occupancy data is unavailable</h1>
          <p>{error}</p>
          <button onClick={() => window.location.reload()}>Try again</button>
        </div>
      </main>
    );
  }

  if (!dashboard) {
    return <main className="app-shell"><div className="empty-state">No facility measurements are available.</div></main>;
  }

  const tone = comparisonTone(dashboard.comparison.level);
  const expected = dashboard.comparison.expectedCount;

  return (
    <main className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">Campus recreation</p>
          <h1>Gym occupancy</h1>
        </div>
        <label className="facility-control">
          <span>Facility</span>
          <select value={facilityId} onChange={(event) => setFacilityId(event.target.value)}>
            {facilities.map((facility) => (
              <option key={facility.id} value={facility.id}>{facility.locationName}</option>
            ))}
          </select>
        </label>
      </header>

      <section className="current-section" aria-labelledby="current-heading">
        <div className="current-copy">
          <p id="current-heading" className="section-label">Right now</p>
          <div className="occupancy-value">
            <strong>{dashboard.current.closed ? "Closed" : dashboard.current.count}</strong>
            {!dashboard.current.closed && <span>of {dashboard.facility.capacity}</span>}
          </div>
          {!dashboard.current.closed && (
            <div className="capacity-track" aria-label={`${Math.round(dashboard.current.percentOfCapacity)} percent occupied`}>
              <span style={{ width: `${Math.min(100, dashboard.current.percentOfCapacity)}%` }} />
            </div>
          )}
          <div className={`comparison comparison--${tone}`}>
            <strong>{dashboard.comparison.label}</strong>
            {expected != null && <span>Typical now: about {Math.round(expected)}</span>}
          </div>
          <p className={`freshness ${dashboard.freshness.stale ? "freshness--stale" : ""}`}>
            {dashboard.freshness.stale ? "Data may be stale" : "Updated"} {formatRelativeAge(dashboard.freshness.ageSeconds)}
            {refreshing && " · Refreshing"}
          </p>
        </div>

        <div className="current-context">
          <p>Today at a glance</p>
          <dl className="summary-list">
            <div><dt>Low</dt><dd>{formatNumber(dashboard.today.minimum)}</dd></div>
            <div><dt>Average</dt><dd>{formatNumber(dashboard.today.average)}</dd></div>
            <div><dt>Peak</dt><dd>{formatNumber(dashboard.today.maximum)}</dd></div>
          </dl>
          {dashboard.today.peakAt && (
            <p className="peak-time">Today’s peak was at {formatTime(dashboard.today.peakAt, dashboard.timezone)}.</p>
          )}
        </div>
      </section>

      {error && <div className="inline-error" role="status">Refresh failed: {error}. Showing the last response.</div>}

      <section className="chart-section" aria-labelledby="trend-heading">
        <div className="section-heading-row">
          <div>
            <p className="section-label">Occupancy trend</p>
            <h2 id="trend-heading">{dashboard.facility.locationName}</h2>
          </div>
          <div className="range-control" aria-label="Chart time range">
            {RANGES.map(([value, label]) => (
              <button
                key={value}
                className={range === value ? "active" : ""}
                onClick={() => setRange(value)}
                aria-pressed={range === value}
              >{label}</button>
            ))}
          </div>
        </div>
        <OccupancyChart dashboard={dashboard} />
        <div className="chart-legend">
          <span><i className="legend-line legend-line--actual" />Measured</span>
          {dashboard.range === "TODAY" && <span><i className="legend-line legend-line--typical" />Typical {formatWeekday(dashboard.current.measuredAt, dashboard.timezone)} and usual range</span>}
        </div>
      </section>

      <section className="visit-section" aria-labelledby="visit-heading">
        <div>
          <p className="section-label">Plan a visit</p>
          <h2 id="visit-heading">Quieter times later today</h2>
          <p>Based on the lowest expected 30-minute periods for this weekday.</p>
        </div>
        {dashboard.recommendedVisitWindows.length ? (
          <ol className="visit-times">
            {dashboard.recommendedVisitWindows.map((visitWindow) => (
              <li key={visitWindow.start}>
                <strong>{formatTime(visitWindow.start, dashboard.timezone)}</strong>
                <span>about {Math.round(visitWindow.expectedCount)} people</span>
              </li>
            ))}
          </ol>
        ) : (
          <p className="insufficient">Not enough later historical data to suggest a time.</p>
        )}
      </section>
    </main>
  );
}

export default App;
