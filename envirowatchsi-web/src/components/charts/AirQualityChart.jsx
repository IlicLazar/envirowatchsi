import {
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
  } from "recharts";
  
  function AirQualityChart({ data }) {
    return (
      <div style={{ width: "100%", height: 400, marginTop: "30px" }}>
        <h2>Air Quality Graph</h2>
  
        <ResponsiveContainer>
          <BarChart data={data}>
            <CartesianGrid strokeDasharray="3 3" />
  
            <XAxis dataKey="stationName" />
  
            <YAxis />
  
            <Tooltip />
  
            <Bar dataKey="airQualityIndex" fill="#8884d8" />
  
            <Bar dataKey="pm10" fill="#82ca9d" />
          </BarChart>
        </ResponsiveContainer>
      </div>
    );
  }
  
  export default AirQualityChart;