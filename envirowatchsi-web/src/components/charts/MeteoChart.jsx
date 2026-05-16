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
  
  function MeteoChart({ data }) {
    return (
      <div style={{ width: "100%", height: 400, marginTop: "30px" }}>
        <h2>Meteorological Graph</h2>
  
        <ResponsiveContainer>
          <LineChart data={data}>
            <CartesianGrid strokeDasharray="3 3" />
  
            <XAxis dataKey="stationName" />
  
            <YAxis />
  
            <Tooltip />
  
            <Legend />
  
            <Line
              type="monotone"
              dataKey="temperature"
              stroke="#ff7300"
            />
  
            <Line
              type="monotone"
              dataKey="humidity"
              stroke="#387908"
            />
  
            <Line
              type="monotone"
              dataKey="windSpeed"
              stroke="#8884d8"
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    );
  }
  
  export default MeteoChart;