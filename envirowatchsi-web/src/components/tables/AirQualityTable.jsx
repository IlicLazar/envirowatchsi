function AirQualityTable({ data, onSelectRecord }) {
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
            <tr key={item._id}>
              <td style={{ fontWeight: "600" }}>{item.stationName}</td>
              <td style={{ fontWeight: "600", color: item.airQualityIndex > 100 ? "#ef4444" : "#10b981" }}>
                {item.airQualityIndex}
              </td>
              <td>{item.pm10 != null ? `${item.pm10} µg/m³` : "N/A"}</td>
              <td>{item.pm2_5 != null ? `${item.pm2_5} µg/m³` : "N/A"}</td>
              <td>
                <button
                  onClick={() => onSelectRecord(item)}
                  className="btn btn-primary"
                  style={{ padding: "6px 12px", fontSize: "0.85rem" }}
                >
                  Podrobnosti
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default AirQualityTable;