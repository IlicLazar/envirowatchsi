import { useEffect, useState } from "react";
import {
  getDataSources,
  createDataSource,
  updateDataSource,
  deleteDataSource,
} from "../api/services/dataSourceService";

function AdminPage() {
  const [dataSources, setDataSources] = useState([]);
  const [message, setMessage] = useState("");
  const [editingId, setEditingId] = useState(null);

  const emptyForm = {
    name: "",
    type: "meteo",
    url: "",
    isActive: true,
    refreshIntervalMinutes: 60,
  };

  const [formData, setFormData] = useState(emptyForm);

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

  async function handleSubmit(event) {
    event.preventDefault();

    try {
      if (editingId) {
        const updatedSource = await updateDataSource(editingId, formData);

        setDataSources(
          dataSources.map((source) =>
            source._id === editingId ? updatedSource : source
          )
        );

        setMessage("Data source updated successfully.");
        setEditingId(null);
      } else {
        const newSource = await createDataSource(formData);
        setDataSources([newSource, ...dataSources]);
        setMessage("Data source created successfully.");
      }

      setFormData(emptyForm);
    } catch (error) {
      setMessage("Failed to save data source.");
      console.error(error);
    }
  }

  function handleEdit(source) {
    setEditingId(source._id);

    setFormData({
      name: source.name,
      type: source.type,
      url: source.url,
      isActive: source.isActive,
      refreshIntervalMinutes: source.refreshIntervalMinutes,
    });
  }

  function handleCancelEdit() {
    setEditingId(null);
    setFormData(emptyForm);
  }

  async function handleDelete(id) {
  const confirmed = window.confirm("Are you sure you want to delete this data source?");

  if (!confirmed) return;

  try {
    await deleteDataSource(id);

    setDataSources(dataSources.filter((source) => source._id !== id));
    setMessage("Data source deleted successfully.");
  } catch (error) {
    setMessage("Failed to delete data source.");
    console.error(error);
  }
}

  return (
    <div style={{ padding: "20px" }}>
      <h1>Admin Data Sources</h1>

      <form onSubmit={handleSubmit} style={{ marginBottom: "20px" }}>
        <h2>{editingId ? "Edit Data Source" : "Add Data Source"}</h2>

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

        <button type="submit">
          {editingId ? "Update Source" : "Add Source"}
        </button>

        {editingId && (
          <button type="button" onClick={handleCancelEdit}>
            Cancel
          </button>
        )}
      </form>

      {message && <p>{message}</p>}

      <table
        border="1"
        cellPadding="10"
        style={{ borderCollapse: "collapse", width: "100%" }}
      >
        <thead>
          <tr>
            <th>Name</th>
            <th>Type</th>
            <th>URL</th>
            <th>Active</th>
            <th>Refresh interval</th>
            <th>Actions</th>
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
              <td>
                <button onClick={() => handleEdit(source)}>Edit</button>
                <button onClick={() => handleDelete(source._id)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default AdminPage;