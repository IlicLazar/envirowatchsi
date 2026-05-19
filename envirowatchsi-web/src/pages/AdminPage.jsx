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
    <div className="dashboard-container">
      <h1>Administracija Virov Podatkov</h1>

      <div className="glass-panel" style={{ maxWidth: "800px" }}>
        <h2>{editingId ? "Uredi Vir Podatkov" : "Dodaj Vir Podatkov"}</h2>
        
        <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: "16px", marginTop: "20px" }}>
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "16px" }}>
            <div>
              <label className="filter-label">Ime Vira</label>
              <input
                name="name"
                placeholder="npr. Agencija za okolje"
                value={formData.name}
                onChange={handleChange}
                required
                className="input-field"
              />
            </div>
            <div>
              <label className="filter-label">Tip Podatkov</label>
              <select name="type" value={formData.type} onChange={handleChange} className="input-field">
                <option value="meteo">Meteorološki (Meteo)</option>
                <option value="air-quality">Kakovost Zraka</option>
                <option value="hydro">Hidrološki (Hydro)</option>
              </select>
            </div>
          </div>

          <div>
            <label className="filter-label">URL Naslov Vira</label>
            <input
              name="url"
              placeholder="https://api.example.com/data"
              value={formData.url}
              onChange={handleChange}
              required
              className="input-field"
            />
          </div>

          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "16px", alignItems: "center" }}>
            <div>
              <label className="filter-label">Interval Osveževanja (minute)</label>
              <input
                name="refreshIntervalMinutes"
                type="number"
                value={formData.refreshIntervalMinutes}
                onChange={handleChange}
                className="input-field"
              />
            </div>
            <div style={{ display: "flex", alignItems: "center", gap: "10px", marginTop: "24px" }}>
              <input
                name="isActive"
                type="checkbox"
                checked={formData.isActive}
                onChange={handleChange}
                id="isActiveCheckbox"
                style={{ width: "20px", height: "20px", cursor: "pointer" }}
              />
              <label htmlFor="isActiveCheckbox" style={{ fontWeight: "500", cursor: "pointer", color: "var(--text-primary)" }}>
                Aktivno delovanje
              </label>
            </div>
          </div>

          <div style={{ display: "flex", gap: "12px", marginTop: "12px" }}>
            <button type="submit" className="btn btn-primary">
              {editingId ? "Posodobi Vir" : "Dodaj Vir"}
            </button>
            
            {editingId && (
              <button type="button" onClick={handleCancelEdit} className="btn btn-danger">
                Prekliči
              </button>
            )}
          </div>
        </form>
      </div>

      {message && (
        <p
          style={{
            margin: "20px 0",
            padding: "12px",
            borderRadius: "8px",
            background: message.includes("successfully") ? "rgba(16, 185, 129, 0.1)" : "rgba(239, 68, 68, 0.1)",
            border: message.includes("successfully") ? "1px solid rgba(16, 185, 129, 0.2)" : "1px solid rgba(239, 68, 68, 0.2)",
            color: message.includes("successfully") ? "var(--accent-emerald)" : "var(--accent-red)",
            fontWeight: "500",
            maxWidth: "800px"
          }}
        >
          {message}
        </p>
      )}

      <h2 style={{ marginTop: "40px", marginBottom: "20px" }}>Seznam Virov Podatkov</h2>
      <div className="table-container">
        <table className="custom-table">
          <thead>
            <tr>
              <th>Ime</th>
              <th>Tip</th>
              <th>URL naslov</th>
              <th>Aktivno</th>
              <th>Interval</th>
              <th>Dejanja</th>
            </tr>
          </thead>

          <tbody>
            {dataSources.map((source) => (
              <tr key={source._id}>
                <td style={{ fontWeight: "600" }}>{source.name}</td>
                <td>
                  <span style={{
                    padding: "4px 8px",
                    borderRadius: "4px",
                    fontSize: "0.8rem",
                    fontWeight: "600",
                    background: source.type === "meteo" ? "rgba(6, 182, 212, 0.15)" : source.type === "air-quality" ? "rgba(249, 115, 22, 0.15)" : "rgba(16, 185, 129, 0.15)",
                    color: source.type === "meteo" ? "var(--accent-cyan)" : source.type === "air-quality" ? "var(--accent-coral)" : "var(--accent-emerald)"
                  }}>
                    {source.type.toUpperCase()}
                  </span>
                </td>
                <td style={{ fontSize: "0.85rem", color: "var(--text-secondary)", maxWidth: "250px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                  {source.url}
                </td>
                <td>
                  <span style={{
                    fontWeight: "600",
                    color: source.isActive ? "var(--accent-emerald)" : "var(--accent-red)"
                  }}>
                    {source.isActive ? "DA" : "NE"}
                  </span>
                </td>
                <td>{source.refreshIntervalMinutes} min</td>
                <td>
                  <div style={{ display: "flex", gap: "8px" }}>
                    <button
                      onClick={() => handleEdit(source)}
                      className="btn btn-primary"
                      style={{ padding: "6px 12px", fontSize: "0.85rem" }}
                    >
                      Uredi
                    </button>
                    <button
                      onClick={() => handleDelete(source._id)}
                      className="btn btn-danger"
                      style={{ padding: "6px 12px", fontSize: "0.85rem" }}
                    >
                      Briši
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default AdminPage;