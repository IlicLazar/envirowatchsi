import { useEffect, useState } from "react";
import { getHydroData } from "../api/hydroService";
import HydroTable from "../components/HydroTable";

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
      <h1>Hydro Data</h1>
      <HydroTable data={hydroData} />
    </div>
  );
}

export default HydroPage;