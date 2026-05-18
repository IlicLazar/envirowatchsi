import { useEffect, useState } from "react";
import { getDataSources } from "../api/services/dataSourceService";

function AdminPage() {
  const [dataSources, setDataSources] = useState([]);
  const [message, setMessage] = useState("");

  useEffect(() => {
    async function loadDataSources() {
      try {
        const data = await getDataSources();
        setDataSources(data);
      } catch (error) {
        setMessage("Failed to load data sources. Please login as admin.");
        console.error(error);
      }
    }

    loadDataSources();
  }, []);

  return (
    <div style={{ padding: "20px" }}>
      <h1>Admin Data Sources</h1>

      {message && <p>{message}</p>}

      <table border="1" cellPadding="10" style={{ borderCollapse: "collapse", width: "100%" }}>
        <thead>
          <tr>
            <th>Name</th>
            <th>Type</th>
            <th>URL</th>
            <th>Active</th>
            <th>Refresh interval</th>
          </tr>
        </thead>

        <tbody>
          {dataSources.map((source) => (
            <tr key={source._id}>
              <td>{source.name}</td>
              <td>{source.type}</td>
              <td>{source.url}</td>
              <td>{source.isActive ? "Yes" : "No"}</td>
              <td>{source.refreshIntervalMinutes} min</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default AdminPage;