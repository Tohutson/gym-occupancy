# Dashboard

The dashboard is a single-page React application. It uses the dashboard API and refreshes the selected facility every five minutes.

## Information order

The page shows information in this order:

1. Current occupancy and capacity.
2. Historical comparison and data freshness.
3. Today's minimum, average, and peak occupancy.
4. The selected occupancy time range.
5. Quieter expected periods later today.

The page does not show a comparison when the API has insufficient historical data. It shows the API label instead.

## Chart behavior

Use the range control to select Today, 24 hours, or 7 days.

The Today view shows these series:

- The measured occupancy line.
- The typical weekday line.
- The historical lower-to-upper quartile range.

The 24-hour and 7-day views show measured occupancy only. The chart inserts a break when two measurements are more than 25 minutes apart. The break prevents the chart from representing missing data as a continuous measurement.

## Local development

The Create React App development server proxies `/api` requests to `http://localhost:8080`.

1. Start the Spring Boot application.
2. Run `npm install` in `gym-occupancy-frontend`.
3. Run `npm start` in `gym-occupancy-frontend`.
4. Open `http://localhost:3000`.

Set `REACT_APP_API_BASE_URL` only when the frontend and API use different origins. Use the same origin in production when possible.
