function HydroTable({ data, onSelectRecord }) {
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
            <tr key={item._id}>
              <td style={{ fontWeight: "600" }}>{item.stationName}</td>
              <td>{item.riverName}</td>
              <td>{item.waterLevel != null ? `${item.waterLevel} cm` : "N/A"}</td>
              <td>{item.waterFlow != null ? `${item.waterFlow} m³/s` : "N/A"}</td>
  
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

export default HydroTable;