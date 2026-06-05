function Filters({ filters, onFilterChange }) {
  const handleChange = (e) => {
    const { name, value } = e.target;
    onFilterChange({ ...filters, [name]: value });
  };

  const hasLat = !!filters.lat;
  const hasLng = !!filters.lng;
  const hasRadius = !!filters.radius;
  const isGeospatialIncomplete = (hasLat || hasLng || hasRadius) && !(hasLat && hasLng && hasRadius);

  return (
    <div className="glass-panel">
      <div className="filter-header-row">
        <h3>Filtri podatkov</h3>
        <button
          onClick={() => onFilterChange({})}
          className="btn-delete"
          style={{ padding: "6px 14px", fontSize: "0.85rem" }}
        >
          Ponastavi filtre
        </button>
      </div>
      
      <div className="filters-grid">
        <div>
          <label className="filter-label">Začetni datum</label>
          <input
            type="date"
            name="startDate"
            value={filters.startDate || ""}
            onChange={handleChange}
            className="input-field"
          />
        </div>
        <div>
          <label className="filter-label">Končni datum</label>
          <input
            type="date"
            name="endDate"
            value={filters.endDate || ""}
            onChange={handleChange}
            className="input-field"
          />
        </div>

        <div>
          <label className="filter-label">Geografska širina</label>
          <input
            type="number"
            step="any"
            name="lat"
            placeholder="npr. 46.05"
            value={filters.lat || ""}
            onChange={handleChange}
            className="input-field"
            style={{
              borderColor: (isGeospatialIncomplete && !filters.lat) ? "var(--accent-red)" : "",
              background: (isGeospatialIncomplete && !filters.lat) ? "rgba(220, 38, 38, 0.02)" : "",
              transition: "all 0.2s ease"
            }}
          />
        </div>

        <div>
          <label className="filter-label">Geografska dolžina</label>
          <input
            type="number"
            step="any"
            name="lng"
            placeholder="npr. 14.50"
            value={filters.lng || ""}
            onChange={handleChange}
            className="input-field"
            style={{
              borderColor: (isGeospatialIncomplete && !filters.lng) ? "var(--accent-red)" : "",
              background: (isGeospatialIncomplete && !filters.lng) ? "rgba(220, 38, 38, 0.02)" : "",
              transition: "all 0.2s ease"
            }}
          />
        </div>

        <div>
          <label className="filter-label">Radij (km)</label>
          <input
            type="number"
            name="radius"
            placeholder="npr. 50"
            value={filters.radius || ""}
            onChange={handleChange}
            className="input-field"
            style={{
              borderColor: (isGeospatialIncomplete && !filters.radius) ? "var(--accent-red)" : "",
              background: (isGeospatialIncomplete && !filters.radius) ? "rgba(220, 38, 38, 0.02)" : "",
              transition: "all 0.2s ease"
            }}
          />
        </div>
      </div>

      {isGeospatialIncomplete && (
        <div style={{
          color: "var(--accent-red)",
          fontSize: "0.875rem",
          marginTop: "16px",
          display: "flex",
          alignItems: "center",
          gap: "6px"
        }}>
          ⚠️ Za iskanje po lokaciji morate vnesti vsa tri polja: Geografsko širino, Geografsko dolžino in Radij!
        </div>
      )}
    </div>
  );
}

export default Filters;

