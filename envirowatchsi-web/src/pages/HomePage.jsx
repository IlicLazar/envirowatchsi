import { useEffect, useState } from "react";
import { getAllEnvironmentalData } from "../api/services/dataService";
import Filters from "../components/filters/Filters";

function HomePage() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [filters, setFilters] = useState({});

  useEffect(() => {
    async function loadData() {
      try {
        setLoading(true);

        const result = await getAllEnvironmentalData(filters);

        setData(result);
      } catch (err) {
        console.error(err);
        setError("Failed to load environmental data.");
      } finally {
        setLoading(false);
      }
    }

    loadData();
  }, [filters]);

  if (loading) {
    return <h2>Loading environmental data...</h2>;
  }

  if (error) {
    return <h2>{error}</h2>;
  }

  return (
    <div style={{ padding: "20px" }}>
      <h1>EnviroWatchSI Dashboard</h1>
      <Filters filters={filters} onFilterChange={setFilters} />

      <p>Meteo records: {data.meteo.length}</p>
      <p>Air quality records: {data.airQuality.length}</p>
      <p>Hydro records: {data.hydro.length}</p>
    </div>
  );
}

export default HomePage;