import { useEffect, useState } from "react";
import { getAirQualityData } from "../api/airQualityService";
import AirQualityTable from "../components/AirQualityTable";

function AirQualityPage() {
  const [airQualityData, setAirQualityData] = useState([]);
  const [selectedRecord, setSelectedRecord] = useState(null);

  useEffect(() => {
    async function fetchData() {
      const data = await getAirQualityData();
      setAirQualityData(data);
    }

    fetchData();
  }, []);

  return (
    <div style={{ padding: "20px" }}>
      <h1>Air Quality Data</h1>

      <AirQualityTable data={airQualityData} onSelectRecord={setSelectedRecord} />

      {selectedRecord && (
        <div style={{ marginTop: "20px", padding: "15px", border: "1px solid #ccc" }}>
          <h2>Record Details</h2>
          <p><strong>Station:</strong> {selectedRecord.stationName}</p>
          <p><strong>AQI:</strong> {selectedRecord.airQualityIndex}</p>
          <p><strong>PM10:</strong> {selectedRecord.pm10 ?? "N/A"}</p>
          <p><strong>PM2.5:</strong> {selectedRecord.pm2_5 ?? "N/A"}</p>
          <p><strong>O3:</strong> {selectedRecord.o3 ?? "N/A"}</p>
          <p><strong>CO:</strong> {selectedRecord.co ?? "N/A"}</p>
          <p><strong>SO2:</strong> {selectedRecord.so2 ?? "N/A"}</p>
        </div>
      )}
    </div>
  );
}

export default AirQualityPage;