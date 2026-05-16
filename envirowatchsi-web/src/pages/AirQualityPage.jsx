import { useEffect, useState } from "react";
import { getAirQualityData } from "../api/airQualityService";
import AirQualityTable from "../components/AirQualityTable";

function AirQualityPage() {
  const [airQualityData, setAirQualityData] = useState([]);

  useEffect(() => {
    async function fetchData() {
      const data = await getAirQualityData();
      setAirQualityData(data);
    }

    fetchData();
  }, []);

  return (
    <div style={{ padding: "20px" }}>
      <h1>Air Quality Data</h1>
      <AirQualityTable data={airQualityData} />
    </div>
  );
}

export default AirQualityPage;