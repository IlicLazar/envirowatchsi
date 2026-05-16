function AirQualityTable({ data, onSelectRecord }) {
    return (
      <table border="1" cellPadding="10" style={{ borderCollapse: "collapse", width: "100%", marginTop: "20px" }}>
        <thead>
          <tr>
            <th>Station</th>
            <th>AQI</th>
            <th>PM10</th>
            <th>PM2.5</th>
            <th>Details</th>
          </tr>
        </thead>
  
        <tbody>
          {data.map((item) => (
            <tr key={item._id}>
              <td>{item.stationName}</td>
              <td>{item.airQualityIndex}</td>
              <td>{item.pm10 ?? "N/A"}</td>
              <td>{item.pm2_5 ?? "N/A"}</td>
              <td>
                <button onClick={() => onSelectRecord(item)}>View Details</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    );
  }
  
  export default AirQualityTable;