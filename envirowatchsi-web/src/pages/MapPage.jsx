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
  const [selectedTimeIndex, setSelectedTimeIndex] = useState(0);
  const [isPlaying, setIsPlaying] = useState(false);

  useEffect(() => {
    async function loadData() {
      try {
        setLoading(true);
        setError("");
        let data = [];

        const sevenDaysAgo = new Date();
        sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);

        const animationFilters = {
          ...filters,
          startDate: filters.startDate || sevenDaysAgo.toISOString(),
        };

        if (activeTab === "meteo") {
          data = await getMeteoData(animationFilters);
        } else if (activeTab === "air-quality") {
          data = await getAirQualityData(animationFilters);
        } else if (activeTab === "hydro") {
          data = await getHydroData(animationFilters);
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

  const sortedTimes = [...new Set(
  dataList
    .filter((item) => item.measuredAt)
    .map((item) => item.measuredAt)
)].sort();

const selectedTime = sortedTimes[selectedTimeIndex];

const getMeasurementsAtTime = (records, time) => {
  if (!time) return getLatestMeasurements(records);

  return records.filter(
    (record) => record.measuredAt === time
  );
};

const latestStations = getMeasurementsAtTime(dataList, selectedTime);

useEffect(() => {
  if (!isPlaying || sortedTimes.length === 0) return;

  const interval = setInterval(() => {
    setSelectedTimeIndex((prev) => {
      if (prev >= sortedTimes.length - 1) {
        setIsPlaying(false);
        return prev;
      }

      return prev + 1;
    });
  }, 800);

  return () => clearInterval(interval);
}, [isPlaying, sortedTimes.length]);

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
        <h1 style={{ margin: 0 }}>Interaktivni Okoljski Zemljevid</h1>
        <div style={{
          display: "inline-flex",
          background: "#f1f5f9",
          border: "1px solid #e2e8f0",
          borderRadius: "10px",
          padding: "4px"
        }}>
          <button
            onClick={() => handleTabChange("meteo")}
            style={{
              background: activeTab === "meteo" ? "rgba(22, 163, 74, 0.08)" : "transparent",
              color: activeTab === "meteo" ? "var(--accent-emerald)" : "var(--text-secondary)",
              border: activeTab === "meteo" ? "1px solid rgba(22, 163, 74, 0.15)" : "1px solid transparent",
              borderRadius: "6px",
              padding: "7px 15px",
              fontSize: "0.875rem",
              fontWeight: activeTab === "meteo" ? "600" : "500",
              cursor: "pointer",
              transition: "all 0.2s ease"
            }}
          >
            🌦️ Meteorologija
          </button>
          <button
            onClick={() => handleTabChange("air-quality")}
            style={{
              background: activeTab === "air-quality" ? "rgba(22, 163, 74, 0.08)" : "transparent",
              color: activeTab === "air-quality" ? "var(--accent-emerald)" : "var(--text-secondary)",
              border: activeTab === "air-quality" ? "1px solid rgba(22, 163, 74, 0.15)" : "1px solid transparent",
              borderRadius: "6px",
              padding: "7px 15px",
              fontSize: "0.875rem",
              fontWeight: activeTab === "air-quality" ? "600" : "500",
              cursor: "pointer",
              transition: "all 0.2s ease"
            }}
          >
            💨 Kakovost Zraka
          </button>
          <button
            onClick={() => handleTabChange("hydro")}
            style={{
              background: activeTab === "hydro" ? "rgba(22, 163, 74, 0.08)" : "transparent",
              color: activeTab === "hydro" ? "var(--accent-emerald)" : "var(--text-secondary)",
              border: activeTab === "hydro" ? "1px solid rgba(22, 163, 74, 0.15)" : "1px solid transparent",
              borderRadius: "6px",
              padding: "7px 15px",
              fontSize: "0.875rem",
              fontWeight: activeTab === "hydro" ? "600" : "500",
              cursor: "pointer",
              transition: "all 0.2s ease"
            }}
          >
            🌊 Hidrologija
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
        <strong>{activeTab === "meteo" ? "Meteorologija" : activeTab === "air-quality" ? "Kakovost Zraka" : "Hidrologija"}</strong>.
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
            {sortedTimes.length > 0 && (
          <div className="glass-panel" style={{ marginBottom: "16px", padding: "16px" }}>
            <h3 style={{ marginTop: 0 }}>Animiran prikaz skozi čas</h3>

            <input
              type="range"
              min="0"
              max={sortedTimes.length - 1}
              value={selectedTimeIndex}
              onChange={(e) => {
                setSelectedTimeIndex(Number(e.target.value));
                setIsPlaying(false);
              }}
              style={{ width: "100%" }}
            />

            <div style={{ display: "flex", gap: "10px", alignItems: "center", marginTop: "12px", flexWrap: "wrap" }}>
              <button
                className="btn-primary"
                onClick={() => setIsPlaying((prev) => !prev)}
              >
                {isPlaying ? "⏸ Pause" : "▶ Play"}
              </button>

              <button
                className="btn-secondary"
                onClick={() => {
                  setSelectedTimeIndex(0);
                  setIsPlaying(false);
                }}
              >
                ⏮ Začetek
              </button>

              <button
                className="btn-secondary"
                onClick={() => {
                  setSelectedTimeIndex(sortedTimes.length - 1);
                  setIsPlaying(false);
                }}
              >
                ⏭ Zadnje
              </button>

              <span>
                Prikazujem stanje ob:{" "}
                <strong>
                  {selectedTime ? new Date(selectedTime).toLocaleString("sl-SI") : "N/A"}
                </strong>
              </span>
            </div>
          </div>
        )}
          <StationMap
            data={filteredStations}
            dataType={activeTab}
            selectedTime={selectedTime}
          />
          </div>
        </div>
      )}
    </div>
  );
}

export default MapPage;
