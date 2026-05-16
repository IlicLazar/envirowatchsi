function MeteoTable({ data }) {
    return (
      <table
        border="1"
        cellPadding="10"
        style={{
          borderCollapse: "collapse",
          width: "100%",
          marginTop: "20px",
        }}
      >
        <thead>
          <tr>
            <th>Station</th>
            <th>Temperature</th>
            <th>Humidity</th>
            <th>Wind Speed</th>
          </tr>
        </thead>
  
        <tbody>
          {data.map((item) => (
            <tr key={item._id}>
              <td>{item.stationName}</td>
              <td>{item.temperature} °C</td>
              <td>{item.humidity} %</td>
              <td>{item.windSpeed ?? "N/A"}</td>
            </tr>
          ))}
        </tbody>
      </table>
    );
  }
  
  export default MeteoTable;