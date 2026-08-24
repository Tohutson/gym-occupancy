const GAP_THRESHOLD_MS = 25 * 60 * 1000;

function baselineAt(timestamp, baseline) {
  if (!baseline.length) return null;
  const time = new Date(timestamp).getTime();
  const points = baseline.map((point) => ({ ...point, ms: new Date(point.time).getTime() }));
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
  const actual = [...dashboard.measurements].sort(
    (a, b) => new Date(a.time) - new Date(b.time)
  );
  const rows = [];

  actual.forEach((point, index) => {
    if (index > 0) {
      const previous = new Date(actual[index - 1].time).getTime();
      const current = new Date(point.time).getTime();
      if (current - previous > GAP_THRESHOLD_MS) {
        rows.push({ time: new Date(previous + (current - previous) / 2).toISOString(), count: null });
      }
    }
    rows.push({ time: point.time, count: point.count });
  });

  if (dashboard.range === "TODAY") {
    const lastActual = actual.length ? new Date(actual[actual.length - 1].time).getTime() : 0;
    dashboard.typicalDay
      .filter((point) => new Date(point.time).getTime() > lastActual)
      .forEach((point) => rows.push({ time: point.time, count: null }));

    rows.forEach((row) => {
      const baseline = baselineAt(row.time, dashboard.typicalDay);
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
