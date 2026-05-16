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
  
  function HydroChart({ data }) {
    return (
      <div style={{ width: "100%", height: 400, marginTop: "30px" }}>
        <h2>Hydrological Graph</h2>
  
        <ResponsiveContainer>
          <AreaChart data={data}>
            <CartesianGrid strokeDasharray="3 3" />
  
            <XAxis dataKey="stationName" />
  
            <YAxis />
  
            <Tooltip />
  
            <Legend />
  
            <Area
              type="monotone"
              dataKey="waterLevel"
              stroke="#0088FE"
              fill="#0088FE"
            />
  
            <Area
              type="monotone"
              dataKey="waterFlow"
              stroke="#00C49F"
              fill="#00C49F"
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    );
  }
  
  export default HydroChart;