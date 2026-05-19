import { useEffect, useState } from "react";
import { getHydroData } from "../api/services/hydroService";
import { createWebSocketConnection } from "../api/websocket/websocketClient";
import HydroTable from "../components/tables/HydroTable";
import HydroChart from "../components/charts/HydroChart";
import StatsCard from "../components/stats/StatsCard";
import Filters from "../components/filters/Filters";

function HydroPage() {
  const [hydroData, setHydroData] = useState([]);
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

  const latestHydroData = getLatestMeasurements(filteredData);

  const averageWaterLevel =
    latestHydroData.length > 0
      ? (
        latestHydroData.reduce((sum, item) => sum + Number(item.waterLevel || 0), 0) /
        latestHydroData.length
      ).toFixed(1)
      : "N/A";

  const averageWaterFlow =
    latestHydroData.length > 0
      ? (
        latestHydroData.reduce((sum, item) => sum + Number(item.waterFlow || 0), 0) /
        latestHydroData.length
      ).toFixed(1)
      : "N/A";

  return (
    <div className="dashboard-container">
      {/* Search Header and Filters */}
      <h1>Hidrološki Podatki (Vode)</h1>

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
        <StatsCard title="Skupno postaj" value={latestHydroData.length} />
        <StatsCard title="Povprečni vodostaj" value={averageWaterLevel != null ? `${averageWaterLevel} cm` : "N/A"} />
        <StatsCard title="Povprečni pretok" value={averageWaterFlow != null ? `${averageWaterFlow} m³/s` : "N/A"} />
      </div>


      <div className="glass-panel" style={{ marginBottom: "40px" }}>
        <HydroChart data={filteredData} />
      </div>

      <HydroTable
        data={latestHydroData}
      />
    </div>
  );
}

export default HydroPage;