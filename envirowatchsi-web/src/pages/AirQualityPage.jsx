import { useEffect, useState } from "react";
import { createWebSocketConnection } from "../api/websocket/websocketClient";
import { getAirQualityData } from "../api/services/airQualityService";
import AirQualityTable from "../components/tables/AirQualityTable";
import AirQualityChart from "../components/charts/AirQualityChart";
import StatsCard from "../components/stats/StatsCard";
import Filters from "../components/filters/Filters";
import StationMap from "../components/maps/StationMap";

function AirQualityPage() {
  const [airQualityData, setAirQualityData] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [filters, setFilters] = useState({});

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

  const getLatestMeasurements = (records) => {
    if (!records || !Array.isArray(records)) return [];
    const map = new Map();
    const sorted = [...records].sort((a, b) => new Date(b.measuredAt || b.createdAt) - new Date(a.measuredAt || a.createdAt));
    for (const record of sorted) {
      const key = record.stationId || record.stationName;
      if (!map.has(key)) {
        map.set(key, record);
      }
    }
    return Array.from(map.values());
  };

  const latestAirQualityData = getLatestMeasurements(filteredData);

  const averageAqi =
    latestAirQualityData.length > 0
      ? (
        latestAirQualityData.reduce((sum, item) => sum + Number(item.airQualityIndex || 0), 0) /
        latestAirQualityData.length
      ).toFixed(1)
      : "N/A";

  const maxPm10 =
    latestAirQualityData.length > 0
      ? Math.max(...latestAirQualityData.map((item) => Number(item.pm10 || 0)))
      : "N/A";

  return (
    <div className="dashboard-container">
      {/* Search Header and Filters */}
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
        <StatsCard title="Skupno postaj" value={latestAirQualityData.length} />
        <StatsCard title="Povprečni AQI" value={averageAqi} />
        <StatsCard title="Maksimalni PM10" value={maxPm10 != null ? `${maxPm10} µg/m³` : "N/A"} />
      </div>

      <div className="glass-panel" style={{ height: "650px", display: "flex", flexDirection: "column" }}>
        <h2>Zemljevid Merilnih Postaj</h2>
        <div style={{ flex: 1, minHeight: 0, marginTop: "16px" }}>
          <StationMap data={latestAirQualityData} />
        </div>
      </div>

      <div className="glass-panel" style={{ marginBottom: "40px" }}>
        <AirQualityChart data={filteredData} />
      </div>

      <AirQualityTable
        data={latestAirQualityData}
      />
    </div>
  );
}

export default AirQualityPage;
