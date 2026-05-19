import { useEffect, useState } from "react";
import { getMeteoData } from "../api/services/meteoService";
import { createWebSocketConnection } from "../api/websocket/websocketClient";
import MeteoTable from "../components/tables/MeteoTable";
import MeteoChart from "../components/charts/MeteoChart";
import StatsCard from "../components/stats/StatsCard";
import Filters from "../components/filters/Filters";
import StationMap from "../components/maps/StationMap";

function MeteoPage() {
  const [meteoData, setMeteoData] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [filters, setFilters] = useState({});

  useEffect(() => {
    async function fetchData() {
      const data = await getMeteoData(filters);
      setMeteoData(data);
    }

    fetchData();
  }, [filters]);

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

  const averageTemperature =
    filteredData.length > 0
      ? (
        filteredData.reduce((sum, item) => sum + Number(item.temperature || 0), 0) /
        filteredData.length
      ).toFixed(1)
      : "N/A";

  const averageHumidity =
    filteredData.length > 0
      ? (
        filteredData.reduce((sum, item) => sum + Number(item.humidity || 0), 0) /
        filteredData.length
      ).toFixed(1)
      : "N/A";

  return (
    <div className="dashboard-container">
      <h1>Meteorološki Podatki</h1>

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
        <StatsCard title="Povprečna temperatura" value={`${averageTemperature} °C`} />
        <StatsCard title="Povprečna vlažnost" value={`${averageHumidity} %`} />
      </div>

      <div className="glass-panel" style={{ height: "650px", display: "flex", flexDirection: "column" }}>
        <h2>Zemljevid Merilnih Postaj</h2>
        <div style={{ flex: 1, minHeight: 0, marginTop: "16px" }}>
          <StationMap data={filteredData} />
        </div>
      </div>

      <div className="glass-panel" style={{ marginBottom: "40px" }}>
        <MeteoChart data={filteredData} />
      </div>

      <MeteoTable data={filteredData} />
    </div>
  );
}

export default MeteoPage;
