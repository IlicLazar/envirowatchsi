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
  const sorted = [...data]
    .filter((item) => item.measuredAt)
    .sort((a, b) => new Date(a.measuredAt) - new Date(b.measuredAt))
    .map((item) => ({
      ...item,
      time: formatTime(item.measuredAt),
    }));

  return (
    <div style={{ width: "100%", height: 400, marginTop: "30px" }}>
      <h2>Meteorological Graph</h2>

      <ResponsiveContainer>
        <LineChart data={sorted}>
          <CartesianGrid strokeDasharray="3 3" />

          <XAxis
            dataKey="time"
            tick={{ fontSize: 11 }}
            interval="preserveStartEnd"
          />

          <YAxis />

          <Tooltip
            labelFormatter={(label) => `Čas: ${label}`}
          />

          <Legend />

          <Line
            type="monotone"
            dataKey="temperature"
            stroke="#ff7300"
            dot={false}
            isAnimationActive={true}
          />

          <Line
            type="monotone"
            dataKey="humidity"
            stroke="#387908"
            dot={false}
            isAnimationActive={true}
          />

          <Line
            type="monotone"
            dataKey="windSpeed"
            stroke="#8884d8"
            dot={false}
            isAnimationActive={true}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}

export default MeteoChart;