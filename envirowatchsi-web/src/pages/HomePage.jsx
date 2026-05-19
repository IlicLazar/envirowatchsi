import { useEffect, useState } from "react";
import { getAllEnvironmentalData } from "../api/services/dataService";
import Filters from "../components/filters/Filters";

function HomePage() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [filters, setFilters] = useState({});

  useEffect(() => {
    async function loadData() {
      try {
        setLoading(true);

        const result = await getAllEnvironmentalData(filters);

        setData(result);
      } catch (err) {
        console.error(err);
        setError("Failed to load environmental data.");
      } finally {
        setLoading(false);
      }
    }

    loadData();
  }, [filters]);

  if (loading && !data) {
    return (
      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "50vh" }}>
        <h2 style={{ color: "var(--text-secondary)" }}>Nalagam okoljske podatke...</h2>
      </div>
    );
  }

  if (error && !data) {
    return (
      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "50vh" }}>
        <h2 style={{ color: "#ef4444" }}>{error}</h2>
      </div>
    );
  }

  function getUniqueStationsCount(records) {
    if (!records || !Array.isArray(records)) return 0;
    const uniqueIds = new Set(records.map(r => r.stationId || r.stationName).filter(Boolean));
    return uniqueIds.size;
  }

  function formatSlovenian(count, wordType) {
    const mod100 = count % 100;
    if (wordType === "postaja") {
      if (mod100 === 1) return `${count} postaja`;
      if (mod100 === 2) return `${count} postaji`;
      if (mod100 === 3 || mod100 === 4) return `${count} postaje`;
      return `${count} postaj`;
    }
    if (wordType === "meritev") {
      if (mod100 === 1) return `${count} meritev`;
      if (mod100 === 2) return `${count} meritvi`;
      if (mod100 === 3 || mod100 === 4) return `${count} meritve`;
      return `${count} meritev`;
    }
    return `${count}`;
  }

  return (
    <div className="dashboard-container">
      {/* Hero Banner Section */}
      <div className="glass-panel" style={{
        background: "linear-gradient(135deg, rgba(99, 102, 241, 0.15) 0%, rgba(6, 182, 212, 0.15) 100%)",
        borderColor: "rgba(99, 102, 241, 0.2)",
        textAlign: "center",
        padding: "48px 24px",
        marginBottom: "40px"
      }}>
        <h1 style={{ fontSize: "3rem", marginBottom: "16px" }}>EnviroWatchSI</h1>
        <p style={{ color: "var(--text-secondary)", fontSize: "1.2rem", maxWidth: "700px", margin: "0 auto 24px auto" }}>
          Napredni sistem za spremljanje in analizo okoljskih parametrov v realnem času. Pregledujte vremenske pogoje, kakovost zraka in hidrološko stanje po celotni Sloveniji.
        </p>
        <div style={{ display: "flex", gap: "16px", justifyContent: "center" }}>
          <a href="/meteo" className="btn btn-primary">Prikaži podatke</a>
        </div>
      </div>

      <Filters filters={filters} onFilterChange={setFilters} />

      <h2 style={{ marginTop: "40px", marginBottom: "20px" }}>Pregled Okoljskih Področij</h2>
      <div className="stats-grid">
        <a href="/meteo" style={{ textDecoration: "none" }}>
          <div className="stats-card meteo" style={{ cursor: "pointer", minHeight: "150px" }}>
            <div>
              <span className="stats-card-title" style={{ fontSize: "1rem" }}>🌦️ Meteorologija</span>
              <p style={{ color: "var(--text-secondary)", fontSize: "0.85rem", marginTop: "8px" }}>
                Temperatura, vlažnost, hitrost vetra in padavine.
              </p>
            </div>
            <span className="stats-card-value" style={{ marginTop: "16px", display: "block", fontSize: "1.45rem", fontWeight: "700" }}>
              {formatSlovenian(getUniqueStationsCount(data.meteo), "postaja")}{" "}
              <span style={{ fontSize: "0.95rem", fontWeight: "normal", color: "var(--text-secondary)" }}>
                ({formatSlovenian(data.meteo.length, "meritev")})
              </span>
            </span>
          </div>
        </a>

        <a href="/air-quality" style={{ textDecoration: "none" }}>
          <div className="stats-card air-quality" style={{ cursor: "pointer", minHeight: "150px" }}>
            <div>
              <span className="stats-card-title" style={{ fontSize: "1rem" }}>💨 Kakovost Zraka</span>
              <p style={{ color: "var(--text-secondary)", fontSize: "0.85rem", marginTop: "8px" }}>
                AQI indeks, PM10, PM2.5 delci in koncentracije plinov.
              </p>
            </div>
            <span className="stats-card-value" style={{ marginTop: "16px", display: "block", fontSize: "1.45rem", fontWeight: "700" }}>
              {formatSlovenian(getUniqueStationsCount(data.airQuality), "postaja")}{" "}
              <span style={{ fontSize: "0.95rem", fontWeight: "normal", color: "var(--text-secondary)" }}>
                ({formatSlovenian(data.airQuality.length, "meritev")})
              </span>
            </span>
          </div>
        </a>

        <a href="/hydro" style={{ textDecoration: "none" }}>
          <div className="stats-card hydro" style={{ cursor: "pointer", minHeight: "150px" }}>
            <div>
              <span className="stats-card-title" style={{ fontSize: "1rem" }}>🌊 Hidrologija</span>
              <p style={{ color: "var(--text-secondary)", fontSize: "0.85rem", marginTop: "8px" }}>
                Vodostaji in pretoki rek po celotni Sloveniji.
              </p>
            </div>
            <span className="stats-card-value" style={{ marginTop: "16px", display: "block", fontSize: "1.45rem", fontWeight: "700" }}>
              {formatSlovenian(getUniqueStationsCount(data.hydro), "postaja")}{" "}
              <span style={{ fontSize: "0.95rem", fontWeight: "normal", color: "var(--text-secondary)" }}>
                ({formatSlovenian(data.hydro.length, "meritev")})
              </span>
            </span>
          </div>
        </a>
      </div>
    </div>
  );
}

export default HomePage;