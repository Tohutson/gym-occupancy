const API_BASE = (process.env.REACT_APP_API_BASE_URL || "").replace(/\/$/, "");

async function request(path, signal) {
  const response = await fetch(`${API_BASE}${path}`, {
    signal,
    headers: { Accept: "application/json" },
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || `Request failed with status ${response.status}`);
  }
  return response.json();
}

export function fetchFacilities(signal) {
  return request("/api/dashboard/facilities", signal);
}

export function fetchDashboard(facilityId, range, signal) {
  const query = new URLSearchParams({ facilityId, range });
  return request(`/api/dashboard?${query}`, signal);
}
