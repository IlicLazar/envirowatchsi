import { useEffect, useMemo, useState } from "react";
import {
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Scatter,
  ScatterChart,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { getAirQualityData } from "../api/services/airQualityService";
import { getHydroData } from "../api/services/hydroService";
import { getMeteoData } from "../api/services/meteoService";
import StatsCard from "../components/stats/StatsCard";

const DATASETS = {
  meteo: {
    label: "Meteorologija",
    load: getMeteoData,
    metrics: [
      { key: "precipitation", label: "Padavine", unit: "mm" },
      { key: "temperature", label: "Temperatura", unit: "C" },
      { key: "humidity", label: "Vlaznost", unit: "%" },
      { key: "windSpeed", label: "Hitrost vetra", unit: "km/h" },
    ],
  },
  hydro: {
    label: "Hidrologija",
    load: getHydroData,
    metrics: [
      { key: "waterLevel", label: "Vodostaj", unit: "cm" },
      { key: "waterFlow", label: "Pretok", unit: "m3/s" },
    ],
  },
  air: {
    label: "Kakovost zraka",
    load: getAirQualityData,
    metrics: [
      { key: "airQualityIndex", label: "AQI", unit: "" },
      { key: "pm10", label: "PM10", unit: "ug/m3" },
      { key: "pm2_5", label: "PM2.5", unit: "ug/m3" },
      { key: "o3", label: "O3", unit: "ug/m3" },
      { key: "co", label: "CO", unit: "mg/m3" },
      { key: "so2", label: "SO2", unit: "ug/m3" },
    ],
  },
};

const datasetOptions = Object.entries(DATASETS);

function stationKey(record) {
  return record.stationId || record.stationName;
}

function toNumber(value) {
  const numeric = Number(value);
  return Number.isFinite(numeric) ? numeric : null;
}

function getMeasuredDate(record) {
  return new Date(record.measuredAt || record.createdAt);
}

function formatTime(value) {
  return new Date(value).toLocaleString("sl-SI", {
    day: "2-digit",
    month: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function distanceKm(first, second) {
  if (!first || !second) return Infinity;

  const lat1 = toNumber(first.latitude);
  const lon1 = toNumber(first.longitude);
  const lat2 = toNumber(second.latitude);
  const lon2 = toNumber(second.longitude);

  if ([lat1, lon1, lat2, lon2].some((value) => value === null)) return Infinity;

  const toRad = (value) => (value * Math.PI) / 180;
  const earthRadiusKm = 6371;
  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(lat1)) *
      Math.cos(toRad(lat2)) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);

  return earthRadiusKm * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

function getStations(records) {
  const latestByStation = new Map();
  const sorted = [...records]
    .filter((record) => record.stationName && record.latitude && record.longitude)
    .sort((a, b) => getMeasuredDate(b) - getMeasuredDate(a));

  for (const record of sorted) {
    const key = stationKey(record);
    if (!latestByStation.has(key)) {
      latestByStation.set(key, {
        key,
        stationName: record.stationName,
        riverName: record.riverName,
        latitude: record.latitude,
        longitude: record.longitude,
      });
    }
  }

  return Array.from(latestByStation.values()).sort((a, b) =>
    a.stationName.localeCompare(b.stationName)
  );
}

function pearsonCorrelation(points) {
  if (points.length < 2) return null;

  const n = points.length;
  const sumX = points.reduce((sum, point) => sum + point.sourceValue, 0);
  const sumY = points.reduce((sum, point) => sum + point.targetValue, 0);
  const sumXY = points.reduce(
    (sum, point) => sum + point.sourceValue * point.targetValue,
    0
  );
  const sumX2 = points.reduce(
    (sum, point) => sum + point.sourceValue * point.sourceValue,
    0
  );
  const sumY2 = points.reduce(
    (sum, point) => sum + point.targetValue * point.targetValue,
    0
  );
  const numerator = n * sumXY - sumX * sumY;
  const denominator = Math.sqrt(
    (n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY)
  );

  return denominator === 0 ? null : numerator / denominator;
}

function correlationLabel(value) {
  if (value === null) return "Premalo podatkov";
  const absolute = Math.abs(value);
  if (absolute >= 0.7) return "Jaka povezanost";
  if (absolute >= 0.4) return "Srednja povezanost";
  if (absolute >= 0.2) return "Slaba povezanost";
  return "Skoraj brez povezanosti";
}

function metricLabel(type, metricKey) {
  return DATASETS[type].metrics.find((metric) => metric.key === metricKey)?.label || metricKey;
}

function CorrelationPage() {
  const [records, setRecords] = useState({ meteo: [], hydro: [], air: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [sourceType, setSourceType] = useState("meteo");
  const [targetType, setTargetType] = useState("hydro");
  const [sourceMetric, setSourceMetric] = useState("precipitation");
  const [targetMetric, setTargetMetric] = useState("waterLevel");
  const [sourceStationKey, setSourceStationKey] = useState("");
  const [targetStationKey, setTargetStationKey] = useState("");
  const [radiusKm, setRadiusKm] = useState(15);
  const [timeWindowHours, setTimeWindowHours] = useState(6);
  const [areaFilter, setAreaFilter] = useState("");

  useEffect(() => {
    async function loadAllData() {
      try {
        setLoading(true);
        const [meteo, hydro, air] = await Promise.all([
          DATASETS.meteo.load(),
          DATASETS.hydro.load(),
          DATASETS.air.load(),
        ]);
        setRecords({ meteo, hydro, air });
        setError("");
      } catch (requestError) {
        setError(requestError.message || "Podatkov trenutno ni mogoce naloziti.");
      } finally {
        setLoading(false);
      }
    }

    loadAllData();
  }, []);

  const stations = useMemo(
    () => ({
      meteo: getStations(records.meteo),
      hydro: getStations(records.hydro),
      air: getStations(records.air),
    }),
    [records]
  );

  const filteredSourceStations = useMemo(() => {
    const normalized = areaFilter.trim().toLowerCase();
    if (!normalized) return stations[sourceType];

    return stations[sourceType].filter((station) => {
      const searchable = `${station.stationName} ${station.riverName || ""}`.toLowerCase();
      return searchable.includes(normalized);
    });
  }, [areaFilter, sourceType, stations]);

  const selectedSourceStationKey = sourceStationKey || filteredSourceStations[0]?.key || "";
  const sourceStation = useMemo(
    () =>
      stations[sourceType].find((station) => station.key === selectedSourceStationKey) ||
      filteredSourceStations[0],
    [filteredSourceStations, selectedSourceStationKey, sourceType, stations]
  );

  const nearbyTargetStations = useMemo(() => {
    const normalized = areaFilter.trim().toLowerCase();
    return stations[targetType]
      .map((station) => ({
        ...station,
        distance: sourceStation ? distanceKm(sourceStation, station) : 0,
      }))
      .filter((station) => station.distance <= Number(radiusKm))
      .filter((station) => {
        if (!normalized) return true;
        const searchable = `${station.stationName} ${station.riverName || ""}`.toLowerCase();
        return searchable.includes(normalized);
      })
      .sort((a, b) => a.distance - b.distance);
  }, [areaFilter, radiusKm, sourceStation, stations, targetType]);

  const selectedTargetStationKey = targetStationKey || nearbyTargetStations[0]?.key || "";
  const targetStation = useMemo(
    () =>
      nearbyTargetStations.find((station) => station.key === selectedTargetStationKey) ||
      nearbyTargetStations[0],
    [nearbyTargetStations, selectedTargetStationKey]
  );

  const pairedData = useMemo(() => {
    if (!sourceStation || !targetStation) return [];

    const sourceRows = records[sourceType]
      .filter((record) => stationKey(record) === sourceStation.key)
      .map((record) => ({
        value: toNumber(record[sourceMetric]),
        date: getMeasuredDate(record),
      }))
      .filter((record) => record.value !== null && !Number.isNaN(record.date.getTime()))
      .sort((a, b) => a.date - b.date);

    const targetRows = records[targetType]
      .filter((record) => stationKey(record) === targetStation.key)
      .map((record) => ({
        value: toNumber(record[targetMetric]),
        date: getMeasuredDate(record),
      }))
      .filter((record) => record.value !== null && !Number.isNaN(record.date.getTime()))
      .sort((a, b) => a.date - b.date);

    const windowMs = Number(timeWindowHours) * 60 * 60 * 1000;

    return sourceRows
      .map((source) => {
        const nearest = targetRows.reduce(
          (best, target) => {
            const difference = Math.abs(target.date - source.date);
            return difference < best.difference ? { target, difference } : best;
          },
          { target: null, difference: Infinity }
        );

        if (!nearest.target || nearest.difference > windowMs) return null;

        return {
          time: formatTime(source.date),
          sourceValue: source.value,
          targetValue: nearest.target.value,
          sourceTime: source.date.toISOString(),
          targetTime: nearest.target.date.toISOString(),
        };
      })
      .filter(Boolean)
      .slice(-80);
  }, [
    records,
    sourceMetric,
    sourceStation,
    sourceType,
    targetMetric,
    targetStation,
    targetType,
    timeWindowHours,
  ]);

  const correlation = useMemo(() => pearsonCorrelation(pairedData), [pairedData]);
  const selectedDistance =
    sourceStation && targetStation ? distanceKm(sourceStation, targetStation) : null;
  const sourceMetricName = metricLabel(sourceType, sourceMetric);
  const targetMetricName = metricLabel(targetType, targetMetric);

  return (
    <div className="dashboard-container">
      <h1>Primerjava podatkov</h1>

      <div className="glass-panel correlation-intro">
        <p>
          Izberi dva tipa meritev, metriki in bliznji postaji. Primerjava uporablja samo
          postaje znotraj izbranega radija in pari meritve, ki so casovno dovolj blizu.
        </p>
      </div>

      <div className="glass-panel">
        <div className="comparison-grid">
          <div>
            <label className="filter-label">Obmocje ali ime postaje</label>
            <input
              className="input-field"
              value={areaFilter}
              onChange={(event) => setAreaFilter(event.target.value)}
              placeholder="npr. Ljubljana, Sava, Maribor"
            />
          </div>

          <div>
            <label className="filter-label">Najvecja razdalja med postajama</label>
            <select
              className="input-field"
              value={radiusKm}
              onChange={(event) => setRadiusKm(Number(event.target.value))}
            >
              <option value={5}>5 km</option>
              <option value={10}>10 km</option>
              <option value={15}>15 km</option>
              <option value={25}>25 km</option>
              <option value={50}>50 km</option>
            </select>
          </div>

          <div>
            <label className="filter-label">Casovno ujemanje meritev</label>
            <select
              className="input-field"
              value={timeWindowHours}
              onChange={(event) => setTimeWindowHours(Number(event.target.value))}
            >
              <option value={1}>do 1 ure</option>
              <option value={3}>do 3 ure</option>
              <option value={6}>do 6 ur</option>
              <option value={12}>do 12 ur</option>
              <option value={24}>do 24 ur</option>
            </select>
          </div>
        </div>

        <div className="comparison-columns">
          <div className="comparison-panel">
            <h2>Prvi podatek</h2>
            <label className="filter-label">Vir podatkov</label>
            <select
              className="input-field"
              value={sourceType}
              onChange={(event) => {
                const nextType = event.target.value;
                setSourceType(nextType);
                setSourceMetric(DATASETS[nextType].metrics[0].key);
                setSourceStationKey("");
                setTargetStationKey("");
              }}
            >
              {datasetOptions.map(([key, dataset]) => (
                <option key={key} value={key}>
                  {dataset.label}
                </option>
              ))}
            </select>

            <label className="filter-label">Metrika</label>
            <select
              className="input-field"
              value={sourceMetric}
              onChange={(event) => setSourceMetric(event.target.value)}
            >
              {DATASETS[sourceType].metrics.map((metric) => (
                <option key={metric.key} value={metric.key}>
                  {metric.label}
                </option>
              ))}
            </select>

            <label className="filter-label">Postaja</label>
            <select
              className="input-field"
              value={selectedSourceStationKey}
              onChange={(event) => setSourceStationKey(event.target.value)}
            >
              {filteredSourceStations.map((station) => (
                <option key={station.key} value={station.key}>
                  {station.stationName}
                </option>
              ))}
            </select>
          </div>

          <div className="comparison-panel">
            <h2>Drugi podatek</h2>
            <label className="filter-label">Vir podatkov</label>
            <select
              className="input-field"
              value={targetType}
              onChange={(event) => {
                const nextType = event.target.value;
                setTargetType(nextType);
                setTargetMetric(DATASETS[nextType].metrics[0].key);
                setTargetStationKey("");
              }}
            >
              {datasetOptions.map(([key, dataset]) => (
                <option key={key} value={key}>
                  {dataset.label}
                </option>
              ))}
            </select>

            <label className="filter-label">Metrika</label>
            <select
              className="input-field"
              value={targetMetric}
              onChange={(event) => setTargetMetric(event.target.value)}
            >
              {DATASETS[targetType].metrics.map((metric) => (
                <option key={metric.key} value={metric.key}>
                  {metric.label}
                </option>
              ))}
            </select>

            <label className="filter-label">Bliznja postaja</label>
            <select
              className="input-field"
              value={selectedTargetStationKey}
              onChange={(event) => setTargetStationKey(event.target.value)}
            >
              {nearbyTargetStations.map((station) => (
                <option key={station.key} value={station.key}>
                  {station.stationName} ({station.distance.toFixed(1)} km)
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {error && <div className="glass-panel error-panel">{error}</div>}
      {loading && <div className="glass-panel">Nalagam podatke za primerjavo...</div>}

      {!loading && !error && (
        <>
          <div className="stats-grid">
            <StatsCard title="Korelacija" value={correlation === null ? "N/A" : correlation.toFixed(2)} />
            <StatsCard title="Ocena" value={correlationLabel(correlation)} />
            <StatsCard title="Parov meritev" value={pairedData.length} />
            <StatsCard
              title="Razdalja postaj"
              value={selectedDistance === null ? "N/A" : `${selectedDistance.toFixed(1)} km`}
            />
          </div>

          {pairedData.length > 0 ? (
            <>
              <div className="glass-panel">
                <h2>Casovna primerjava</h2>
                <div className="chart-frame">
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={pairedData} margin={{ bottom: 15, left: -10, right: 10 }}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                      <XAxis dataKey="time" tick={{ fontSize: 11, fill: "var(--text-secondary)" }} />
                      <YAxis yAxisId="left" tick={{ fontSize: 11, fill: "var(--text-secondary)" }} />
                      <YAxis
                        yAxisId="right"
                        orientation="right"
                        tick={{ fontSize: 11, fill: "var(--text-secondary)" }}
                      />
                      <Tooltip contentStyle={{ borderRadius: "8px" }} />
                      <Legend />
                      <Line
                        yAxisId="left"
                        type="monotone"
                        dataKey="sourceValue"
                        name={sourceMetricName}
                        stroke="var(--accent-coral)"
                        strokeWidth={2}
                        dot={false}
                      />
                      <Line
                        yAxisId="right"
                        type="monotone"
                        dataKey="targetValue"
                        name={targetMetricName}
                        stroke="var(--accent-cyan)"
                        strokeWidth={2}
                        dot={false}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              </div>

              <div className="glass-panel">
                <h2>Raztros in korelacija</h2>
                <div className="chart-frame">
                  <ResponsiveContainer width="100%" height="100%">
                    <ScatterChart margin={{ bottom: 15, left: -10, right: 10 }}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                      <XAxis
                        type="number"
                        dataKey="sourceValue"
                        name={sourceMetricName}
                        tick={{ fontSize: 11, fill: "var(--text-secondary)" }}
                      />
                      <YAxis
                        type="number"
                        dataKey="targetValue"
                        name={targetMetricName}
                        tick={{ fontSize: 11, fill: "var(--text-secondary)" }}
                      />
                      <Tooltip cursor={{ strokeDasharray: "3 3" }} contentStyle={{ borderRadius: "8px" }} />
                      <Scatter name={`${sourceMetricName} / ${targetMetricName}`} data={pairedData} fill="var(--accent-emerald)" />
                    </ScatterChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </>
          ) : (
            <div className="glass-panel">
              Za izbrani par postaj ni dovolj casovno usklajenih meritev. Poskusi vecji radij,
              vecje casovno okno ali drugo kombinacijo metrik.
            </div>
          )}
        </>
      )}
    </div>
  );
}

export default CorrelationPage;
