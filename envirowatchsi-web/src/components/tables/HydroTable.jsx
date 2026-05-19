import { useState, Fragment } from "react";

function HydroTable({ data }) {
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
            <th>Reka</th>
            <th>Vodni Vodostaj</th>
            <th>Pretok Vode</th>
            <th>Dejanja</th>
          </tr>
        </thead>
  
        <tbody>
          {data.map((item) => (
            <Fragment key={item._id}>
              <tr>
                <td style={{ fontWeight: "600" }}>{item.stationName}</td>
                <td>{item.riverName}</td>
                <td>{item.waterLevel != null ? `${item.waterLevel} cm` : "N/A"}</td>
                <td>{item.waterFlow != null ? `${item.waterFlow} m³/s` : "N/A"}</td>
    
                <td>
                <button
                  onClick={() => handleToggle(item._id)}
                  className={`btn-details ${expandedId === item._id ? "active" : ""}`}
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
                      <div className="detail-label" style={{ fontSize: "0.75rem", color: "var(--text-secondary)", marginBottom: "4px" }}>Reka</div>
                      <div className="detail-value" style={{ fontSize: "1.1rem", fontWeight: "600" }}>{item.riverName}</div>
                    </div>
                    <div className="detail-item">
                      <div className="detail-label" style={{ fontSize: "0.75rem", color: "var(--text-secondary)", marginBottom: "4px" }}>Vodni Vodostaj</div>
                      <div className="detail-value" style={{ fontSize: "1.1rem", fontWeight: "600" }}>
                        {item.waterLevel != null ? `${item.waterLevel} cm` : "N/A"}
                      </div>
                    </div>
                    <div className="detail-item">
                      <div className="detail-label" style={{ fontSize: "0.75rem", color: "var(--text-secondary)", marginBottom: "4px" }}>Pretok Vode</div>
                      <div className="detail-value" style={{ fontSize: "1.1rem", fontWeight: "600" }}>
                        {item.waterFlow != null ? `${item.waterFlow} m³/s` : "N/A"}
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

export default HydroTable;