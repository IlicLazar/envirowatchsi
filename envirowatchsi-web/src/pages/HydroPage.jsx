import { useEffect, useState } from "react";
import { getHydroData } from "../api/hydroService";

function HydroPage() {
  const [hydroData, setHydroData] = useState([]);

  useEffect(() => {
    async function fetchData() {
      const data = await getHydroData();
      setHydroData(data);
    }

    fetchData();
  }, []);

  return (
    <div style={{ padding: "20px" }}>
      <h1>Hydro Page</h1>

      {hydroData.map((item) => (
        <div key={item._id} style={{ border: "1px solid #ccc", padding: "10px", marginBottom: "10px" }}>
          <h3>{item.stationName}</h3>
          <p>River: {item.riverName}</p>
          <p>Water level: {item.waterLevel ?? "N/A"}</p>
          <p>Water flow: {item.waterFlow ?? "N/A"}</p>
        </div>
      ))}
    </div>
  );
}

export default HydroPage;