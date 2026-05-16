import { useEffect, useState } from "react";
import { getMeteoData } from "../api/meteoService";
import MeteoTable from "../components/MeteoTable";

function MeteoPage() {
  const [meteoData, setMeteoData] = useState([]);

  useEffect(() => {
    async function fetchData() {
      const data = await getMeteoData();
      setMeteoData(data);
    }

    fetchData();
  }, []);

  return (
    <div style={{ padding: "20px" }}>
      <h1>Meteo Data</h1>
      <MeteoTable data={meteoData} />
    </div>
  );
}

export default MeteoPage;