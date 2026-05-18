import { useEffect, useState } from "react";
import { getDataSources, createDataSource } from "../api/services/dataSourceService";

function AdminPage() {
  const [dataSources, setDataSources] = useState([]);
  const [message, setMessage] = useState("");
  const [formData, setFormData] = useState({
  name: "",
  type: "meteo",
  url: "",
  isActive: true,
  refreshIntervalMinutes: 60,
 });

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

  function handleChange(event) {
  const { name, value, type, checked } = event.target;

  setFormData({
    ...formData,
    [name]: type === "checkbox" ? checked : value,
  });
}

async function handleCreate(event) {
  event.preventDefault();

  try {
    const newSource = await createDataSource(formData);
    setDataSources([newSource, ...dataSources]);

    setFormData({
      name: "",
      type: "meteo",
      url: "",
      isActive: true,
      refreshIntervalMinutes: 60,
    });

    setMessage("Data source created successfully.");
  } catch (error) {
    setMessage("Failed to create data source.");
    console.error(error);
  }
}

  return (
    <div style={{ padding: "20px" }}>
      <h1>Admin Data Sources</h1>

      <form onSubmit={handleCreate} style={{ marginBottom: "20px" }}>
        <h2>Add Data Source</h2>

        <input
            name="name"
            placeholder="Name"
            value={formData.name}
            onChange={handleChange}
            required
        />

        <select name="type" value={formData.type} onChange={handleChange}>
            <option value="meteo">Meteo</option>
            <option value="air-quality">Air Quality</option>
            <option value="hydro">Hydro</option>
        </select>

        <input
            name="url"
            placeholder="URL"
            value={formData.url}
            onChange={handleChange}
            required
        />

        <input
            name="refreshIntervalMinutes"
            type="number"
            value={formData.refreshIntervalMinutes}
            onChange={handleChange}
        />

        <label>
            <input
            name="isActive"
            type="checkbox"
            checked={formData.isActive}
            onChange={handleChange}
            />
            Active
        </label>

        <button type="submit">Add Source</button>
        </form>

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