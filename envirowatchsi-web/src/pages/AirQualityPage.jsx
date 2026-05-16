import { useEffect, useState } from "react";
import { createWebSocketConnection } from "../api/websocket/websocketClient";
import { getAirQualityData } from "../api/services/airQualityService";
import AirQualityTable from "../components/tables/AirQualityTable";
import AirQualityChart from "../components/charts/AirQualityChart";

function AirQualityPage() {
  const [airQualityData, setAirQualityData] = useState([]);
  const [selectedRecord, setSelectedRecord] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");

  useEffect(() => {
    async function fetchData() {
      const data = await getAirQualityData();
      setAirQualityData(data);
    }

    fetchData();
  }, []);

  useEffect(() => {
    const socket = createWebSocketConnection((message) => {
      if (message.type === "AIR_QUALITY_CREATED") {
        setAirQualityData((prevData) => [message.data, ...prevData]);
      }
  
      if (message.type === "AIR_QUALITY_UPDATED") {
        setAirQualityData((prevData) =>
          prevData.map((item) =>
            item._id === message.data._id ? message.data : item
          )
        );
      }
  
      if (message.type === "AIR_QUALITY_DELETED") {
        setAirQualityData((prevData) =>
          prevData.filter((item) => item._id !== message.data._id)
        );
      }
    });
  
    return () => socket.close();
  }, []);

  const filteredData = airQualityData.filter((item) =>
    item.stationName.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div style={{ padding: "20px" }}>
      <h1>Air Quality Data</h1>

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

      <AirQualityTable
        data={filteredData}
        onSelectRecord={setSelectedRecord}
      />

      <AirQualityChart data={filteredData} />

      {selectedRecord && (
        <div
          style={{
            marginTop: "20px",
            padding: "15px",
            border: "1px solid #ccc",
          }}
        >
          <h2>Record Details</h2>

          <p>
            <strong>Station:</strong> {selectedRecord.stationName}
          </p>

          <p>
            <strong>AQI:</strong> {selectedRecord.airQualityIndex}
          </p>

          <p>
            <strong>PM10:</strong> {selectedRecord.pm10 ?? "N/A"}
          </p>

          <p>
            <strong>PM2.5:</strong> {selectedRecord.pm2_5 ?? "N/A"}
          </p>

          <p>
            <strong>O3:</strong> {selectedRecord.o3 ?? "N/A"}
          </p>

          <p>
            <strong>CO:</strong> {selectedRecord.co ?? "N/A"}
          </p>

          <p>
            <strong>SO2:</strong> {selectedRecord.so2 ?? "N/A"}
          </p>
        </div>
      )}
    </div>
  );
}

export default AirQualityPage;