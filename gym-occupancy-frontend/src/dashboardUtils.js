const GAP_THRESHOLD_MS = 25 * 60 * 1000;

function baselineAt(timestamp, baseline) {
  if (!baseline.length) return null;
  const time = new Date(timestamp).getTime();
  const points = baseline
    .map((point) => ({
      ...point,
      average: Number(point.average),
      lowerQuartile: Number(point.lowerQuartile),
      upperQuartile: Number(point.upperQuartile),
      ms: new Date(point.time).getTime(),
    }))
    .filter((point) =>
      Number.isFinite(point.ms) &&
      Number.isFinite(point.average) &&
      Number.isFinite(point.lowerQuartile) &&
      Number.isFinite(point.upperQuartile)
    )
    .sort((a, b) => a.ms - b.ms);
  const exact = points.find((point) => point.ms === time);
  if (exact) return exact;
  const nextIndex = points.findIndex((point) => point.ms > time);
  if (nextIndex <= 0) return null;
  const before = points[nextIndex - 1];
  const after = points[nextIndex];
  const ratio = (time - before.ms) / (after.ms - before.ms);
  const interpolate = (key) => before[key] + (after[key] - before[key]) * ratio;
  return {
    average: interpolate("average"),
    lowerQuartile: interpolate("lowerQuartile"),
    upperQuartile: interpolate("upperQuartile"),
  };
}

export function buildChartData(dashboard) {
  if (!dashboard) return [];
  const actual = (Array.isArray(dashboard.measurements) ? dashboard.measurements : [])
    .map((point) => ({
      time: point.time,
      timestamp: new Date(point.time).getTime(),
      count: point.count == null || point.count === "" ? Number.NaN : Number(point.count),
    }))
    .filter((point) =>
      Number.isFinite(point.timestamp) && Number.isFinite(point.count) && point.count >= 0
    )
    .sort((a, b) => a.timestamp - b.timestamp);
  const rows = [];

  actual.forEach((point, index) => {
    if (index > 0) {
      const previous = actual[index - 1].timestamp;
      const current = point.timestamp;
      if (current - previous > GAP_THRESHOLD_MS) {
        // This null separator breaks the presentation line; it is never shown as a measurement.
        rows.push({ time: new Date(previous + (current - previous) / 2).toISOString(), count: null });
      }
    }
    rows.push({ time: point.time, count: point.count });
  });

  if (dashboard.range === "TODAY") {
    const typicalDay = Array.isArray(dashboard.typicalDay) ? dashboard.typicalDay : [];
    const lastActual = actual.length ? actual[actual.length - 1].timestamp : 0;
    typicalDay
      .filter((point) => Number.isFinite(new Date(point.time).getTime()) && new Date(point.time).getTime() > lastActual)
      .forEach((point) => rows.push({ time: point.time, count: null }));

    rows.forEach((row) => {
      // Interpolation only draws the historical baseline smoothly; measured counts are never interpolated.
      const baseline = baselineAt(row.time, typicalDay);
      row.expected = baseline?.average ?? null;
      row.expectedRange = baseline
        ? [baseline.lowerQuartile, baseline.upperQuartile]
        : null;
    });
  }

  return rows
    .sort((a, b) => new Date(a.time) - new Date(b.time))
    .map((row) => ({ ...row, timestamp: new Date(row.time).getTime() }));
}

export function formatRelativeAge(seconds) {
  if (seconds < 60) return "less than a minute ago";
  if (seconds < 3600) return `${Math.floor(seconds / 60)} min ago`;
  if (seconds < 86400) return `${Math.floor(seconds / 3600)} hr ago`;
  return `${Math.floor(seconds / 86400)} days ago`;
}

export function comparisonTone(level) {
  if (level === "QUIETER" || level === "MUCH_QUIETER") return "quiet";
  if (level === "BUSIER" || level === "MUCH_BUSIER") return "busy";
  return "neutral";
}
