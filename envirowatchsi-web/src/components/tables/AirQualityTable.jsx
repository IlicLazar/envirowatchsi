import { useState, Fragment } from "react";

function AirQualityTable({ data }) {
  const [expandedId, setExpandedId] = useState(null);

  function handleToggle(id) {
    setExpandedId((prev) => (prev === id ? null : id));
  }

  return (
    <div className="table-container">
      <table className="custom-table">
        <thead>
          <tr>
            <th>Postaja</th>
            <th>AQI Indeks</th>
            <th>PM10</th>
            <th>PM2.5</th>
            <th>Dejanja</th>
          </tr>
        </thead>
  
        <tbody>
          {data.map((item) => (
            <Fragment key={item._id}>
              <tr>
                <td style={{ fontWeight: "600" }}>{item.stationName}</td>
                <td style={{ fontWeight: "600", color: item.airQualityIndex > 100 ? "#ef4444" : "#10b981" }}>
                  {item.airQualityIndex}
                </td>
                <td>{item.pm10 != null ? `${item.pm10} µg/m³` : "N/A"}</td>
                <td>{item.pm2_5 != null ? `${item.pm2_5} µg/m³` : "N/A"}</td>
                <td>
                <button
                  onClick={() => handleToggle(item._id)}
                  className="btn btn-primary"
                  style={{
                    padding: "6px 12px",
                    fontSize: "0.85rem",
                    background: expandedId === item._id ? "var(--accent-coral)" : "var(--accent-cyan)",
                    color: "var(--background-dark)",
                    fontWeight: "600",
                    minWidth: "110px",
                    textAlign: "center"
                  }}
                >
                  {expandedId === item._id ? "Zapri" : "Podrobnosti"}
                </button>
              </td>
            </tr>
            {expandedId === item._id && (
              <tr style={{ background: "rgba(255, 255, 255, 0.01)" }} className="details-expanded-row">
                <td colSpan={5} style={{ padding: "20px 24px", borderBottom: "1px solid rgba(255, 255, 255, 0.05)" }}>
                  <div className="details-grid" style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(180px, 1fr))", gap: "16px" }}>
                    <div className="detail-item">
                      <div className="detail-label" style={{ fontSize: "0.75rem", color: "var(--text-secondary)", marginBottom: "4px" }}>AQI Indeks</div>
                      <div className="detail-value" style={{ fontSize: "1.1rem", fontWeight: "600", color: item.airQualityIndex > 100 ? "#ef4444" : "#10b981" }}>
                        {item.airQualityIndex}
                      </div>
                    </div>
                    <div className="detail-item">
                      <div className="detail-label" style={{ fontSize: "0.75rem", color: "var(--text-secondary)", marginBottom: "4px" }}>PM10</div>
                      <div className="detail-value" style={{ fontSize: "1.1rem", fontWeight: "600" }}>
                        {item.pm10 != null ? `${item.pm10} µg/m³` : "N/A"}
                      </div>
                    </div>
                    <div className="detail-item">
                      <div className="detail-label" style={{ fontSize: "0.75rem", color: "var(--text-secondary)", marginBottom: "4px" }}>PM2.5</div>
                      <div className="detail-value" style={{ fontSize: "1.1rem", fontWeight: "600" }}>
                        {item.pm2_5 != null ? `${item.pm2_5} µg/m³` : "N/A"}
                      </div>
                    </div>
                    <div className="detail-item">
                      <div className="detail-label" style={{ fontSize: "0.75rem", color: "var(--text-secondary)", marginBottom: "4px" }}>Ozon (O3)</div>
                      <div className="detail-value" style={{ fontSize: "1.1rem", fontWeight: "600" }}>
                        {item.o3 != null ? `${item.o3} µg/m³` : "N/A"}
                      </div>
                    </div>
                    <div className="detail-item">
                      <div className="detail-label" style={{ fontSize: "0.75rem", color: "var(--text-secondary)", marginBottom: "4px" }}>Ogljikov Monoksid (CO)</div>
                      <div className="detail-value" style={{ fontSize: "1.1rem", fontWeight: "600" }}>
                        {item.co != null ? `${item.co} mg/m³` : "N/A"}
                      </div>
                    </div>
                    <div className="detail-item">
                      <div className="detail-label" style={{ fontSize: "0.75rem", color: "var(--text-secondary)", marginBottom: "4px" }}>Žveplov Dioksid (SO2)</div>
                      <div className="detail-value" style={{ fontSize: "1.1rem", fontWeight: "600" }}>
                        {item.so2 != null ? `${item.so2} µg/m³` : "N/A"}
                      </div>
                    </div>
                    <div className="detail-item">
                      <div className="detail-label" style={{ fontSize: "0.75rem", color: "var(--text-secondary)", marginBottom: "4px" }}>Čas Meritve</div>
                      <div className="detail-value" style={{ fontSize: "0.95rem", fontWeight: "600" }}>
                        {item.measuredAt
                          ? new Date(item.measuredAt).toLocaleString("sl-SI", {
                              day: "2-digit",
                              month: "2-digit",
                              year: "numeric",
                              hour: "2-digit",
                              minute: "2-digit",
                            })
                          : "N/A"}
                      </div>
                    </div>
                  </div>
                </td>
              </tr>
            )}
            </Fragment>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default AirQualityTable;