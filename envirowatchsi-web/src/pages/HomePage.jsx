import { useEffect, useState } from "react";
import { getAllEnvironmentalData } from "../api/services/dataService";

function HomePage() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadData() {
      try {
        setLoading(true);

        const result = await getAllEnvironmentalData();

        setData(result);
      } catch (err) {
        console.error(err);
        setError("Failed to load environmental data.");
      } finally {
        setLoading(false);
      }
    }

    loadData();
  }, []);

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
            <span className="stats-card-value" style={{ marginTop: "16px", display: "block" }}>
              {data.meteo.length} <span style={{ fontSize: "1rem", fontWeight: "normal", color: "var(--text-secondary)" }}>zapisov</span>
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
            <span className="stats-card-value" style={{ marginTop: "16px", display: "block" }}>
              {data.airQuality.length} <span style={{ fontSize: "1rem", fontWeight: "normal", color: "var(--text-secondary)" }}>zapisov</span>
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
            <span className="stats-card-value" style={{ marginTop: "16px", display: "block" }}>
              {data.hydro.length} <span style={{ fontSize: "1rem", fontWeight: "normal", color: "var(--text-secondary)" }}>zapisov</span>
            </span>
          </div>
        </a>
      </div>
    </div>
  );
}

export default HomePage;
