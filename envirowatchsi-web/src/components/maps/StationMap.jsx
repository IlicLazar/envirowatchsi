import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import L from "leaflet";
import { useEffect } from "react";
import { useMap } from "react-leaflet";
import "leaflet.heat";

function HeatmapLayer({ data, dataType }) {
  const map = useMap();

  useEffect(() => {
    if (!data || data.length === 0) return;

    const heatPoints = data
      .filter((item) => item.latitude && item.longitude)
      .map((item) => {
        let intensity = 0;

        if (dataType === "air-quality") {
          const aqi = item.airQualityIndex ?? 0;
          if (aqi <= 50) intensity = 0.25;
          else if (aqi <= 100) intensity = 0.65;
          else intensity = 1;
        }

        if (dataType === "hydro") {
          const level = item.waterLevel ?? 0;
          if (level <= 150) intensity = 0.25;
          else if (level <= 300) intensity = 0.65;
          else intensity = 1;
        }

        if (dataType === "meteo") {
          const temp = item.temperature ?? 0;
          if (temp < 5) intensity = 0.25;
          else if (temp < 20) intensity = 0.5;
          else if (temp < 30) intensity = 0.75;
          else intensity = 1;
        }

        return [item.latitude, item.longitude, intensity];
      });

     const getHeatmapGradient = () => {
      if (dataType === "air-quality") {
        return {
          0.25: "#22c55e",
          0.65: "#f59e0b",
          1.0: "#dc2626",
        };
      }

      if (dataType === "hydro") {
        return {
          0.25: "#93c5fd",
          0.65: "#3b82f6",
          1.0: "#1e3a8a",
        };
      }

      return {
        0.25: "#38bdf8",
        0.5: "#22c55e",
        0.75: "#f97316",
        1.0: "#dc2626",
      };
    };

    const heatLayer = L.heatLayer(heatPoints, {
      radius: 45,
      blur: 30,
      maxZoom: 12,
      minOpacity: 0.35,
      gradient: getHeatmapGradient(),
    });

    heatLayer.addTo(map);

    return () => {
      map.removeLayer(heatLayer);
    };
  }, [data, dataType, map]);

  return null;
}

function StationMap({ data, dataType, selectedTime }) {
  const defaultCenter = [46.1512, 14.9955]; // Slovenia center

  const getMarkerIcon = (item) => {
    let color = "#16a34a";

    if (dataType === "air-quality") {
      if (item.airQualityIndex > 100) {
        color = "#dc2626";
      } else if (item.airQualityIndex > 50) {
        color = "#f59e0b";
      } else {
        color = "#16a34a";
      }
    }

    if (dataType === "hydro") {
      if (item.waterLevel > 300) {
        color = "#1d4ed8";
      } else if (item.waterLevel > 150) {
        color = "#3b82f6";
      } else {
        color = "#93c5fd";
      }
    }

    if (dataType === "meteo") {
      if (item.temperature < 5) {
        color = "#38bdf8";
      } else if (item.temperature < 20) {
        color = "#22c55e";
      } else {
        color = "#f97316";
      }
    }

    return L.divIcon({
      className: "",
      html: `
        <div style="
          position: relative;
          width: 26px;
          height: 26px;
          background: ${color};
          border: 3px solid white;
          border-radius: 50% 50% 50% 0;
          transform: rotate(-45deg);
          box-shadow: 0 3px 8px rgba(0,0,0,0.35);
        ">
          <div style="
            position: absolute;
            width: 9px;
            height: 9px;
            background: white;
            border-radius: 50%;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
          "></div>
        </div>
      `,
      iconSize: [26, 26],
      iconAnchor: [13, 26],
    });
  };

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

      <HeatmapLayer
        key={`${dataType}-${selectedTime}`}
        data={data}
        dataType={dataType}
      />

      {data
        .filter((item) => item.latitude && item.longitude)
        .map((item) => (
          <Marker
            key={item._id}
            position={[item.latitude, item.longitude]}
            icon={getMarkerIcon(item)}
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
