import { useState } from "react";
import {
  AreaChart,
  Area,
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

function HydroChart({ data }) {
  const [limit, setLimit] = useState(30);

  const sorted = [...data]
    .filter((item) => item.measuredAt)
    .sort((a, b) => new Date(a.measuredAt) - new Date(b.measuredAt))
    .map((item) => ({
      ...item,
      time: formatTime(item.measuredAt),
    }));

  const displayedData = limit === "all" ? sorted : sorted.slice(-limit);

  return (
    <div style={{ width: "100%", height: 400, marginTop: "30px" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "15px" }}>
        <h2>Hydrological Graph</h2>
        <div>
          <label style={{ marginRight: "8px", fontSize: "14px", color: "#555" }}>Prikaz zadnjih:</label>
          <select
            value={limit}
            onChange={(e) => setLimit(e.target.value === "all" ? "all" : Number(e.target.value))}
            style={{
              padding: "5px 10px",
              borderRadius: "4px",
              border: "1px solid #ccc",
              fontSize: "14px",
              cursor: "pointer",
            }}
          >
            <option value={15}>15 zapisov</option>
            <option value={30}>30 zapisov</option>
            <option value={50}>50 zapisov</option>
            <option value="all">Vsi zapisi</option>
          </select>
        </div>
      </div>

      <ResponsiveContainer>
        <AreaChart data={displayedData}>
          <CartesianGrid strokeDasharray="3 3" />

          <XAxis
            dataKey="time"
            tick={{ fontSize: 11 }}
            interval="preserveStartEnd"
          />

          <YAxis />

          <Tooltip labelFormatter={(label) => `Čas: ${label}`} />

          <Legend />

          <Area
            type="monotone"
            dataKey="waterLevel"
            stroke="#0088FE"
            fill="#0088FE"
            isAnimationActive={true}
          />

          <Area
            type="monotone"
            dataKey="waterFlow"
            stroke="#00C49F"
            fill="#00C49F"
            isAnimationActive={true}
          />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
}

export default HydroChart;
