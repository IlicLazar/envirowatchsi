import { useEffect, useState, useRef } from "react";
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
  const detailsRef = useRef(null);

  useEffect(() => {
    if (selectedRecord && detailsRef.current) {
      detailsRef.current.scrollIntoView({ behavior: "smooth", block: "start" });
    }
  }, [selectedRecord]);

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
    <div className="dashboard-container">
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
        <StatsCard title="Skupno postaj" value={filteredData.length} />
        <StatsCard title="Povprečni vodostaj" value={averageWaterLevel != null ? `${averageWaterLevel} cm` : "N/A"} />
        <StatsCard title="Povprečni pretok" value={averageWaterFlow != null ? `${averageWaterFlow} m³/s` : "N/A"} />
      </div>

      <div className="glass-panel" style={{ height: "650px", display: "flex", flexDirection: "column" }}>
        <h2>Zemljevid Merilnih Postaj</h2>
        <div style={{ flex: 1, minHeight: 0, marginTop: "16px" }}>
          <StationMap data={filteredData} />
        </div>
      </div>

      <div className="glass-panel" style={{ marginBottom: "40px" }}>
        <HydroChart data={filteredData} />
      </div>

      <HydroTable
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
              <div className="detail-label">Reka</div>
              <div className="detail-value">{selectedRecord.riverName}</div>
            </div>
            <div className="detail-item">
              <div className="detail-label">Vodni Vodostaj</div>
              <div className="detail-value">
                {selectedRecord.waterLevel != null ? `${selectedRecord.waterLevel} cm` : "N/A"}
              </div>
            </div>
            <div className="detail-item">
              <div className="detail-label">Pretok Vode</div>
              <div className="detail-value">
                {selectedRecord.waterFlow != null ? `${selectedRecord.waterFlow} m³/s` : "N/A"}
              </div>
            </div>
            <div className="detail-item">
              <div className="detail-label">Čas Meritve</div>
              <div className="detail-value">
                {selectedRecord.measuredAt
                  ? new Date(selectedRecord.measuredAt).toLocaleString("sl-SI")
                  : "N/A"}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default HydroPage;