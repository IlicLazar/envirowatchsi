import { useEffect, useState } from "react";
import { getMeteoData } from "../api/meteoService";

function MeteoPage() {
  const [meteoData, setMeteoData] = useState([]);

  useEffect(() => {
    async function fetchData() {
      try {
        const data = await getMeteoData();
        setMeteoData(data);
      } catch (error) {
        console.error("Failed to fetch meteo data:", error);
      }
    }

    fetchData();
  }, []);

  return (
    <div style={{ padding: "20px" }}>
      <h1>Meteo Page</h1>

      {meteoData.map((item) => (
        <div
          key={item._id}
          style={{
            border: "1px solid #ccc",
            padding: "10px",
            marginBottom: "10px",
          }}
        >
          <h3>{item.stationName}</h3>
          <p>Temperature: {item.temperature} °C</p>
          <p>Humidity: {item.humidity} %</p>
        </div>
      ))}
    </div>
  );
}

export default MeteoPage;