import {
  BarChart,
  Bar,
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

function AirQualityChart({ data }) {
  const sorted = [...data]
    .filter((item) => item.measuredAt)
    .sort((a, b) => new Date(a.measuredAt) - new Date(b.measuredAt))
    .map((item) => ({
      ...item,
      time: formatTime(item.measuredAt),
    }));

  return (
    <div style={{ width: "100%", height: 400, marginTop: "30px" }}>
      <h2>Air Quality Graph</h2>

      <ResponsiveContainer>
        <BarChart data={sorted}>
          <CartesianGrid strokeDasharray="3 3" />

          <XAxis
            dataKey="time"
            tick={{ fontSize: 11 }}
            interval="preserveStartEnd"
          />

          <YAxis />

          <Tooltip labelFormatter={(label) => `Čas: ${label}`} />

          <Legend />

          <Bar
            dataKey="airQualityIndex"
            fill="#8884d8"
            isAnimationActive={true}
          />

          <Bar
            dataKey="pm10"
            fill="#82ca9d"
            isAnimationActive={true}
          />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}

export default AirQualityChart;