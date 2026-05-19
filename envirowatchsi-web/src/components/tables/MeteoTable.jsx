function MeteoTable({ data, onSelectRecord }) {
  return (
    <div className="table-container">
      <table className="custom-table">
        <thead>
          <tr>
            <th>Postaja</th>
            <th>Temperatura</th>
            <th>Vlažnost</th>
            <th>Hitrost Vetra</th>
            <th>Dejanja</th>
          </tr>
        </thead>
  
        <tbody>
          {data.map((item) => (
            <tr key={item._id}>
              <td style={{ fontWeight: "600" }}>{item.stationName}</td>
              <td>{item.temperature} °C</td>
              <td>{item.humidity} %</td>
              <td>{item.windSpeed != null ? `${item.windSpeed} km/h` : "N/A"}</td>
  
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

export default MeteoTable;