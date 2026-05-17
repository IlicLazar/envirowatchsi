import { useEffect, useState } from "react";
import { getHydroData } from "../api/services/hydroService";
import { createWebSocketConnection } from "../api/websocket/websocketClient";
import HydroTable from "../components/tables/HydroTable";
import HydroChart from "../components/charts/HydroChart";
import StatsCard from "../components/stats/StatsCard";
import Filters from "../components/filters/Filters";
import StationMap from "../components/maps/StationMap";

function HydroPage() {
  const [hydroData, setHydroData] = useState([]);
  const [selectedRecord, setSelectedRecord] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [filters, setFilters] = useState({});

  useEffect(() => {
    async function fetchData() {
      const data = await getHydroData(filters);
      setHydroData(data);
    }

    fetchData();
  }, [filters]);

  useEffect(() => {
    const socket = createWebSocketConnection((message) => {
      if (message.type === "HYDRO_CREATED") {
        setHydroData((prevData) => [message.data, ...prevData]);
      }
  
      if (message.type === "HYDRO_UPDATED") {
        setHydroData((prevData) =>
          prevData.map((item) =>
            item._id === message.data._id ? message.data : item
          )
        );
      }
  
      if (message.type === "HYDRO_DELETED") {
        setHydroData((prevData) =>
          prevData.filter((item) => item._id !== message.data._id)
        );
      }
    });
  
    return () => socket.close();
  }, []);

  const filteredData = hydroData.filter((item) =>
    item.stationName.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const averageWaterLevel =
  filteredData.length > 0
    ? (
        filteredData.reduce((sum, item) => sum + Number(item.waterLevel || 0), 0) /
        filteredData.length
      ).toFixed(1)
    : "N/A";

  const averageWaterFlow =
  filteredData.length > 0
    ? (
        filteredData.reduce((sum, item) => sum + Number(item.waterFlow || 0), 0) /
        filteredData.length
      ).toFixed(1)
    : "N/A";

  return (
    <div style={{ padding: "20px" }}>
      <h1>Hydro Data</h1>

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
      <Filters filters={filters} onFilterChange={setFilters} />
      <div style={{ display: "flex", gap: "20px", marginBottom: "20px" }}>
        <StatsCard title="Total stations" value={filteredData.length} />
        <StatsCard title="Average water level" value={averageWaterLevel} />
        <StatsCard title="Average water flow" value={averageWaterFlow} />
      </div>

      <HydroTable
        data={filteredData}
        onSelectRecord={setSelectedRecord}
      />

      <HydroChart data={filteredData} />

      <StationMap data={filteredData} />

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
            <strong>River:</strong> {selectedRecord.riverName}
          </p>

          <p>
            <strong>Water Level:</strong>{" "}
            {selectedRecord.waterLevel ?? "N/A"}
          </p>

          <p>
            <strong>Water Flow:</strong>{" "}
            {selectedRecord.waterFlow ?? "N/A"}
          </p>

          <p>
            <strong>Measured At:</strong>{" "}
            {selectedRecord.measuredAt ?? "N/A"}
          </p>
        </div>
      )}
    </div>
  );
}

export default HydroPage;