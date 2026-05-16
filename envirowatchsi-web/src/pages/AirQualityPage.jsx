import { useEffect, useState } from "react";
import { getAirQualityData } from "../api/airQualityService";

function AirQualityPage() {
  const [airQualityData, setAirQualityData] = useState([]);

  useEffect(() => {
    async function fetchData() {
      const data = await getAirQualityData();
      setAirQualityData(data);
    }

    fetchData();
  }, []);

  return (
    <div style={{ padding: "20px" }}>
      <h1>Air Quality Page</h1>

      {airQualityData.map((item) => (
        <div key={item._id} style={{ border: "1px solid #ccc", padding: "10px", marginBottom: "10px" }}>
          <h3>{item.stationName}</h3>
          <p>Air Quality Index: {item.airQualityIndex}</p>
          <p>PM10: {item.pm10 ?? "N/A"}</p>
        </div>
      ))}
    </div>
  );
}

export default AirQualityPage;