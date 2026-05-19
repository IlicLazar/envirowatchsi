import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";

function StationMap({ data, dataType }) {
  const defaultCenter = [46.1512, 14.9955]; // Slovenia center

  return (
    <MapContainer
      center={defaultCenter}
      zoom={8}
      style={{ height: "100%", width: "100%", borderRadius: "12px" }}
    >
      <TileLayer
        attribution="&copy; OpenStreetMap contributors"
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      {data
        .filter((item) => item.latitude && item.longitude)
        .map((item) => (
          <Marker
            key={item._id}
            position={[item.latitude, item.longitude]}
          >
            <Popup>
              <div style={{ fontFamily: "inherit", minWidth: "165px", color: "#0f172a" }}>
                <strong style={{ fontSize: "1.1rem", color: "#0f172a", display: "block", margin: "0 0 8px 0", fontWeight: "700", borderBottom: "1px solid #e2e8f0", paddingBottom: "4px" }}>
                  {item.stationName}
                </strong>
                {dataType === "meteo" && (
                  <div style={{ fontSize: "0.85rem", color: "#475569", display: "flex", flexDirection: "column", gap: "4px" }}>
                    <div>🌡️ <strong>Temperatura:</strong> <span style={{ color: "#0f172a", fontWeight: "600" }}>{item.temperature != null ? `${item.temperature} °C` : "N/A"}</span></div>
                    <div>💧 <strong>Vlažnost:</strong> <span style={{ color: "#0f172a", fontWeight: "600" }}>{item.humidity != null ? `${item.humidity} %` : "N/A"}</span></div>
                    <div>💨 <strong>Veter:</strong> <span style={{ color: "#0f172a", fontWeight: "600" }}>{item.windSpeed != null ? `${item.windSpeed} m/s` : "N/A"}</span></div>
                    <div>🌧️ <strong>Padavine:</strong> <span style={{ color: "#0f172a", fontWeight: "600" }}>{item.precipitation != null ? `${item.precipitation} mm` : "N/A"}</span></div>
                  </div>
                )}
                {dataType === "air-quality" && (
                  <div style={{ fontSize: "0.85rem", color: "#475569", display: "flex", flexDirection: "column", gap: "4px" }}>
                    <div>
                      ✨ <strong>AQI Indeks:</strong>{" "}
                      <span style={{ fontWeight: "bold", color: item.airQualityIndex > 100 ? "#dc2626" : "#16a34a" }}>
                        {item.airQualityIndex || "N/A"}
                      </span>
                    </div>
                    <div>🌫️ <strong>PM10:</strong> <span style={{ color: "#0f172a", fontWeight: "600" }}>{item.pm10 != null ? `${item.pm10} µg/m³` : "N/A"}</span></div>
                    <div>🌫️ <strong>PM2.5:</strong> <span style={{ color: "#0f172a", fontWeight: "600" }}>{item.pm2_5 != null ? `${item.pm2_5} µg/m³` : "N/A"}</span></div>
                    <div>☀️ <strong>Ozon (O3):</strong> <span style={{ color: "#0f172a", fontWeight: "600" }}>{item.o3 != null ? `${item.o3} µg/m³` : "N/A"}</span></div>
                  </div>
                )}
                {dataType === "hydro" && (
                  <div style={{ fontSize: "0.85rem", color: "#475569", display: "flex", flexDirection: "column", gap: "4px" }}>
                    <div>🌊 <strong>Reka:</strong> <span style={{ color: "#0f172a", fontWeight: "600" }}>{item.riverName || "N/A"}</span></div>
                    <div>📈 <strong>Vodostaj:</strong> <span style={{ color: "#0f172a", fontWeight: "600" }}>{item.waterLevel != null ? `${item.waterLevel} cm` : "N/A"}</span></div>
                    <div>📉 <strong>Pretok:</strong> <span style={{ color: "#0f172a", fontWeight: "600" }}>{item.waterFlow != null ? `${item.waterFlow} m³/s` : "N/A"}</span></div>
                  </div>
                )}
                {!dataType && (
                  <div style={{ fontSize: "0.85rem", color: "#475569" }}>
                    <div>Širina: {item.latitude}</div>
                    <div>Dolžina: {item.longitude}</div>
                  </div>
                )}
                <div style={{ fontSize: "0.72rem", color: "#64748b", marginTop: "8px", borderTop: "1px solid #e2e8f0", paddingTop: "4px" }}>
                  Čas meritve:{" "}
                  <span style={{ fontWeight: "500", color: "#475569" }}>
                    {item.measuredAt
                      ? new Date(item.measuredAt).toLocaleTimeString("sl-SI", { hour: "2-digit", minute: "2-digit" })
                      : "N/A"}
                  </span>
                </div>
              </div>
            </Popup>
          </Marker>
        ))}
    </MapContainer>
  );
}

export default StationMap;