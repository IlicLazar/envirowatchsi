import { useEffect, useState } from "react";
import { getMeteoData } from "../api/meteoService";
import MeteoTable from "../components/MeteoTable";

function MeteoPage() {
  const [meteoData, setMeteoData] = useState([]);
  const [selectedRecord, setSelectedRecord] = useState(null);

  useEffect(() => {
    async function fetchData() {
      const data = await getMeteoData();
      setMeteoData(data);
    }

    fetchData();
  }, []);

  return (
    <div style={{ padding: "20px" }}>
      <h1>Meteo Data</h1>

      <MeteoTable
        data={meteoData}
        onSelectRecord={setSelectedRecord}
      />

      {selectedRecord && (
        <div style={{ marginTop: "20px", padding: "15px", border: "1px solid #ccc" }}>
          <h2>Record Details</h2>
          <p><strong>Station:</strong> {selectedRecord.stationName}</p>
          <p><strong>Temperature:</strong> {selectedRecord.temperature} °C</p>
          <p><strong>Humidity:</strong> {selectedRecord.humidity} %</p>
          <p><strong>Wind speed:</strong> {selectedRecord.windSpeed ?? "N/A"}</p>
          <p><strong>Wind direction:</strong> {selectedRecord.windDirection ?? "N/A"}</p>
        </div>
      )}
    </div>
  );
}

export default MeteoPage;