import React, { useEffect, useState } from "react";
import { getMeteoData } from "../api/services/meteoService";
import { getAirQualityData } from "../api/services/airQualityService";
import { getHydroData } from "../api/services/hydroService";
import Filters from "../components/filters/Filters";
import StationMap from "../components/maps/StationMap";

function MapPage() {
  const [activeTab, setActiveTab] = useState("meteo");
  const [searchTerm, setSearchTerm] = useState("");
  const [filters, setFilters] = useState({});
  const [dataList, setDataList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadData() {
      try {
        setLoading(true);
        setError("");
        let data = [];
        if (activeTab === "meteo") {
          data = await getMeteoData(filters);
        } else if (activeTab === "air-quality") {
          data = await getAirQualityData(filters);
        } else if (activeTab === "hydro") {
          data = await getHydroData(filters);
        }
        setDataList(data);
      } catch (err) {
        console.error(err);
        setError("Napaka pri nalaganju podatkov za zemljevid.");
      } finally {
        setLoading(false);
      }
    }
    loadData();
  }, [activeTab, filters]);

  // Clean filters when switching tabs
  const handleTabChange = (tab) => {
    setActiveTab(tab);
    setFilters({});
    setSearchTerm("");
  };

  // Helper to keep only the latest measurement for each station
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

  // Filter latest entries by name search
  const latestStations = getLatestMeasurements(dataList);
  const filteredStations = latestStations.filter((item) =>
    item.stationName.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Slovenian Plural support for active points count
  const formatSlovenianPoints = (count) => {
    const mod100 = count % 100;
    if (mod100 === 1) return `${count} aktivno merilno točko`;
    if (mod100 === 2) return `${count} aktivni merilni točki`;
    if (mod100 === 3 || mod100 === 4) return `${count} aktivne merilne točke`;
    return `${count} aktivnih merilnih točk`;
  };

  return (
    <div className="dashboard-container">
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "24px", flexWrap: "wrap", gap: "16px" }}>
        <h1 style={{ margin: 0 }}>🌍 Interaktivni Okoljski Zemljevid</h1>
        <div style={{
          display: "inline-flex",
          background: "rgba(255, 255, 255, 0.03)",
          border: "1px solid rgba(255, 255, 255, 0.08)",
          borderRadius: "12px",
          padding: "4px"
        }}>
          <button
            onClick={() => handleTabChange("meteo")}
            className={`btn ${activeTab === "meteo" ? "btn-primary" : ""}`}
            style={{
              background: activeTab === "meteo" ? "" : "transparent",
              color: activeTab === "meteo" ? "" : "var(--text-secondary)",
              border: "none",
              borderRadius: "8px",
              padding: "8px 16px",
              fontSize: "0.9rem"
            }}
          >
            🌦️ Meteo
          </button>
          <button
            onClick={() => handleTabChange("air-quality")}
            className={`btn ${activeTab === "air-quality" ? "btn-primary" : ""}`}
            style={{
              background: activeTab === "air-quality" ? "" : "transparent",
              color: activeTab === "air-quality" ? "" : "var(--text-secondary)",
              border: "none",
              borderRadius: "8px",
              padding: "8px 16px",
              fontSize: "0.9rem"
            }}
          >
            💨 Kakovost Zraka
          </button>
          <button
            onClick={() => handleTabChange("hydro")}
            className={`btn ${activeTab === "hydro" ? "btn-primary" : ""}`}
            style={{
              background: activeTab === "hydro" ? "" : "transparent",
              color: activeTab === "hydro" ? "" : "var(--text-secondary)",
              border: "none",
              borderRadius: "8px",
              padding: "8px 16px",
              fontSize: "0.9rem"
            }}
          >
            🌊 Vode (Hydro)
          </button>
        </div>
      </div>

      <div className="glass-panel" style={{ marginBottom: "24px" }}>
        <div className="search-container">
          <label className="filter-label">Iskanje po imenu merna mesta</label>
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

      <div style={{
        background: "rgba(99, 102, 241, 0.08)",
        border: "1px solid rgba(99, 102, 241, 0.15)",
        borderRadius: "8px",
        padding: "12px 18px",
        marginBottom: "24px",
        color: "var(--text-primary)",
        fontSize: "0.95rem",
        display: "flex",
        alignItems: "center",
        gap: "8px"
      }}>
        ℹ️ Na zemljevidu prikazujem <strong>{formatSlovenianPoints(filteredStations.length)}</strong> s trenutno zadnjimi podatki za tip{" "}
        <strong>{activeTab === "meteo" ? "Meteorologija" : activeTab === "air-quality" ? "Kakovost Zraka" : "Vode (Hydro)"}</strong>.
      </div>

      {loading ? (
        <div className="glass-panel" style={{ height: "600px", display: "flex", justifyContent: "center", alignItems: "center" }}>
          <h3 style={{ color: "var(--text-secondary)" }}>Nalagam zemljevid...</h3>
        </div>
      ) : error ? (
        <div className="glass-panel" style={{ height: "600px", display: "flex", justifyContent: "center", alignItems: "center" }}>
          <h3 style={{ color: "#ef4444" }}>{error}</h3>
        </div>
      ) : (
        <div className="glass-panel" style={{ height: "650px", display: "flex", flexDirection: "column", padding: "16px" }}>
          <div style={{ flex: 1, minHeight: 0 }}>
            <StationMap data={filteredStations} dataType={activeTab} />
          </div>
        </div>
      )}
    </div>
  );
}

export default MapPage;
