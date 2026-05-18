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
  const sorted = [...data]
    .filter((item) => item.measuredAt)
    .sort((a, b) => new Date(a.measuredAt) - new Date(b.measuredAt))
    .map((item) => ({
      ...item,
      time: formatTime(item.measuredAt),
    }));

  return (
    <div style={{ width: "100%", height: 400, marginTop: "30px" }}>
      <h2>Hydrological Graph</h2>

      <ResponsiveContainer>
        <AreaChart data={sorted}>
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