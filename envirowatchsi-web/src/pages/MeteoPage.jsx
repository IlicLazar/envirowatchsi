import { useEffect, useState } from "react";
import { getMeteoData } from "../api/meteoService";
import MeteoTable from "../components/MeteoTable";
import { createWebSocketConnection } from "../api/websocketClient";

function MeteoPage() {
  const [meteoData, setMeteoData] = useState([]);
  const [selectedRecord, setSelectedRecord] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");

  useEffect(() => {
    async function fetchData() {
      const data = await getMeteoData();
      setMeteoData(data);
    }

    fetchData();
  }, []);

  useEffect(() => {
    const socket = createWebSocketConnection((message) => {
      if (message.type === "METEO_CREATED") {
        setMeteoData((prevData) => [message.data, ...prevData]);
      }
  
      if (message.type === "METEO_UPDATED") {
        setMeteoData((prevData) =>
          prevData.map((item) =>
            item._id === message.data._id ? message.data : item
          )
        );
      }
  
      if (message.type === "METEO_DELETED") {
        setMeteoData((prevData) =>
          prevData.filter((item) => item._id !== message.data._id)
        );
      }
    });
  
    return () => {
      socket.close();
    };
  }, []);

  const filteredData = meteoData.filter((item) =>
    item.stationName.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div style={{ padding: "20px" }}>
      <h1>Meteo Data</h1>

      <input
        type="text"
        placeholder="Search by station name..."
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        style={{
          padding: "10px",
          width: "300px",
          marginBottom: "20px",
        }}
      />

      <MeteoTable data={filteredData} onSelectRecord={setSelectedRecord} />

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