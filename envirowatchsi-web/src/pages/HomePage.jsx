import { useEffect, useState } from "react";
import { getAllEnvironmentalData } from "../api/services/dataService";

function HomePage() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadData() {
      try {
        setLoading(true);

        const result = await getAllEnvironmentalData();

        setData(result);
      } catch (err) {
        console.error(err);
        setError("Failed to load environmental data.");
      } finally {
        setLoading(false);
      }
    }

    loadData();
  }, []);

  if (loading) {
    return <h2>Loading environmental data...</h2>;
  }

  if (error) {
    return <h2>{error}</h2>;
  }

  return (
    <div>
      <h1>EnviroWatchSI Dashboard</h1>

      <p>Meteo records: {data.meteo.length}</p>
      <p>Air quality records: {data.airQuality.length}</p>
      <p>Hydro records: {data.hydro.length}</p>
    </div>
  );
}

export default HomePage;