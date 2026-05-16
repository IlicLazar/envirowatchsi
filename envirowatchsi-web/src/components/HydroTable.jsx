function HydroTable({ data }) {
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
            <th>River</th>
            <th>Water Level</th>
            <th>Water Flow</th>
          </tr>
        </thead>
  
        <tbody>
          {data.map((item) => (
            <tr key={item._id}>
              <td>{item.stationName}</td>
              <td>{item.riverName}</td>
              <td>{item.waterLevel ?? "N/A"}</td>
              <td>{item.waterFlow ?? "N/A"}</td>
            </tr>
          ))}
        </tbody>
      </table>
    );
  }
  
  export default HydroTable;