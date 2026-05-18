import { useEffect, useState, useRef } from "react";
import { createWebSocketConnection } from "../api/websocket/websocketClient";
import { getAirQualityData } from "../api/services/airQualityService";
import AirQualityTable from "../components/tables/AirQualityTable";
import AirQualityChart from "../components/charts/AirQualityChart";
import StatsCard from "../components/stats/StatsCard";
import Filters from "../components/filters/Filters";
import StationMap from "../components/maps/StationMap";

function AirQualityPage() {
  const [airQualityData, setAirQualityData] = useState([]);
  const [selectedRecord, setSelectedRecord] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [filters, setFilters] = useState({});
  const detailsRef = useRef(null);

  useEffect(() => {
    if (selectedRecord && detailsRef.current) {
      detailsRef.current.scrollIntoView({ behavior: "smooth", block: "start" });
    }
  }, [selectedRecord]);

  useEffect(() => {
    async function fetchData() {
      const data = await getAirQualityData(filters);
      setAirQualityData(data);
    }

    fetchData();
  }, [filters]);

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

  const averageAqi =
  filteredData.length > 0
    ? (
        filteredData.reduce((sum, item) => sum + Number(item.airQualityIndex || 0), 0) /
        filteredData.length
      ).toFixed(1)
    : "N/A";

  const maxPm10 =
    filteredData.length > 0
      ? Math.max(...filteredData.map((item) => Number(item.pm10 || 0)))
      : "N/A";

  return (
    <div className="dashboard-container">
      <h1>Kakovost Zraka (Air Quality)</h1>

      <div className="glass-panel">
        <div className="search-container">
          <label className="filter-label">Iskanje po imenu postaje</label>
          <input
            type="text"
            placeholder="Vpišite ime postaje..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="input-field search-field"
          />
        </div>
        <Filters filters={filters} onFilterChange={setFilters} />
      </div>

      <div className="stats-grid">
        <StatsCard title="Skupno postaj" value={filteredData.length} />
        <StatsCard title="Povprečni AQI" value={averageAqi} />
        <StatsCard title="Maksimalni PM10" value={maxPm10 != null ? `${maxPm10} µg/m³` : "N/A"} />
      </div>

      <div className="glass-panel" style={{ height: "650px", display: "flex", flexDirection: "column" }}>
        <h2>Zemljevid Merilnih Postaj</h2>
        <div style={{ flex: 1, minHeight: 0, marginTop: "16px" }}>
          <StationMap data={filteredData} />
        </div>
      </div>

      <div className="glass-panel" style={{ marginBottom: "40px" }}>
        <AirQualityChart data={filteredData} />
      </div>

      <AirQualityTable
        data={filteredData}
        onSelectRecord={setSelectedRecord}
      />

      {selectedRecord && (
        <div ref={detailsRef} className="details-panel">
          <h2>Podrobnosti o zapisu</h2>
          <div className="details-grid">
            <div className="detail-item">
              <div className="detail-label">Merilna Postaja</div>
              <div className="detail-value">{selectedRecord.stationName}</div>
            </div>
            <div className="detail-item">
              <div className="detail-label">AQI Indeks</div>
              <div className="detail-value" style={{ color: selectedRecord.airQualityIndex > 100 ? "#ef4444" : "#10b981" }}>
                {selectedRecord.airQualityIndex}
              </div>
            </div>
            <div className="detail-item">
              <div className="detail-label">PM10</div>
              <div className="detail-value">
                {selectedRecord.pm10 != null ? `${selectedRecord.pm10} µg/m³` : "N/A"}
              </div>
            </div>
            <div className="detail-item">
              <div className="detail-label">PM2.5</div>
              <div className="detail-value">
                {selectedRecord.pm2_5 != null ? `${selectedRecord.pm2_5} µg/m³` : "N/A"}
              </div>
            </div>
            <div className="detail-item">
              <div className="detail-label">Ozon (O3)</div>
              <div className="detail-value">
                {selectedRecord.o3 != null ? `${selectedRecord.o3} µg/m³` : "N/A"}
              </div>
            </div>
            <div className="detail-item">
              <div className="detail-label">Ogljikov Monoksid (CO)</div>
              <div className="detail-value">
                {selectedRecord.co != null ? `${selectedRecord.co} mg/m³` : "N/A"}
              </div>
            </div>
            <div className="detail-item">
              <div className="detail-label">Žveplov Dioksid (SO2)</div>
              <div className="detail-value">
                {selectedRecord.so2 != null ? `${selectedRecord.so2} µg/m³` : "N/A"}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default AirQualityPage;