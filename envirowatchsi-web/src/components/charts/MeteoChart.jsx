import { useState } from "react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from "recharts";

function formatTime(dateStr) {
  if (!dateStr) return "";
  const d = new Date(dateStr);
  return d.toLocaleString("sl-SI", {
    day: "2-digit",
    month: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function MeteoChart({ data }) {
  const [limit, setLimit] = useState(30);
  const [selectedStation, setSelectedStation] = useState("all");

  const uniqueStations = [...new Set(data.map(item => item.stationName).filter(Boolean))].sort();

  const sorted = [...data]
    .filter((item) => item.measuredAt)
    .sort((a, b) => new Date(a.measuredAt) - new Date(b.measuredAt))
    .map((item) => ({
      ...item,
      time: formatTime(item.measuredAt),
    }));

  const filteredByStation = selectedStation === "all" 
    ? sorted 
    : sorted.filter((item) => item.stationName === selectedStation);

  const displayedData = limit === "all" ? filteredByStation : filteredByStation.slice(-limit);

  return (
    <div style={{ width: "100%" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "20px" }}>
        <h2>Zgodovina Meritev</h2>
        <div style={{ display: "flex", gap: "16px" }}>
          <div>
            <label style={{ marginRight: "8px", fontSize: "14px", color: "var(--text-secondary)" }}>Postaja:</label>
            <select
              value={selectedStation}
              onChange={(e) => setSelectedStation(e.target.value)}
              className="input-field"
              style={{
                padding: "5px 10px",
                width: "180px",
                fontSize: "14px",
                cursor: "pointer",
                display: "inline-block"
              }}
            >
              <option value="all">Vse postaje</option>
              {uniqueStations.map(station => (
                <option key={station} value={station}>{station}</option>
              ))}
            </select>
          </div>
          <div>
            <label style={{ marginRight: "8px", fontSize: "14px", color: "var(--text-secondary)" }}>Prikaz zadnjih:</label>
          <select
            value={limit}
            onChange={(e) => setLimit(e.target.value === "all" ? "all" : Number(e.target.value))}
            className="input-field"
            style={{
              padding: "5px 10px",
              width: "140px",
              fontSize: "14px",
              cursor: "pointer",
              display: "inline-block"
            }}
          >
            <option value={15}>15 zapisov</option>
            <option value={30}>30 zapisov</option>
            <option value={50}>50 zapisov</option>
            <option value="all">Vsi zapisi</option>
          </select>
        </div>
      </div>
      </div>

      <div style={{ width: "100%", height: "350px" }}>
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={displayedData} margin={{ bottom: 15, left: -20, right: 10 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />

            <XAxis
              dataKey="time"
              tick={{ fontSize: 11, fill: "var(--text-secondary)" }}
              interval="preserveStartEnd"
              stroke="#cbd5e1"
            />

            <YAxis tick={{ fill: "var(--text-secondary)", fontSize: 11 }} stroke="#cbd5e1" />

            <Tooltip
              labelFormatter={(label) => `Čas: ${label}`}
              contentStyle={{ background: "#ffffff", borderColor: "#cbd5e1", borderRadius: "8px", color: "#0f172a", boxShadow: "0 4px 10px rgba(15, 23, 42, 0.05)" }}
            />

            <Legend wrapperStyle={{ paddingTop: "10px" }} />

            <Line
              type="monotone"
              dataKey="temperature"
              name="Temperatura (°C)"
              stroke="var(--accent-cyan)"
              strokeWidth={2}
              dot={false}
              isAnimationActive={true}
            />

            <Line
              type="monotone"
              dataKey="humidity"
              name="Vlažnost (%)"
              stroke="var(--accent-emerald)"
              strokeWidth={2}
              dot={false}
              isAnimationActive={true}
            />

            <Line
              type="monotone"
              dataKey="windSpeed"
              name="Hitrost vetra (km/h)"
              stroke="var(--accent-violet)"
              strokeWidth={2}
              dot={false}
              isAnimationActive={true}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

export default MeteoChart;
