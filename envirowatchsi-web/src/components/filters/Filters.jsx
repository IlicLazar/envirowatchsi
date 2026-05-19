import React from "react";

function Filters({ filters, onFilterChange }) {
  const handleChange = (e) => {
    const { name, value } = e.target;
    onFilterChange({ ...filters, [name]: value });
  };

  return (
    <div className="glass-panel">
      <div className="filter-header-row">
        <h3>Filtri podatkov</h3>
        <button
          onClick={() => onFilterChange({})}
          className="btn btn-danger"
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
          <label className="filter-label">Širina (Latitude)</label>
          <input
            type="number"
            step="any"
            name="lat"
            placeholder="npr. 46.05"
            value={filters.lat || ""}
            onChange={handleChange}
            className="input-field"
          />
        </div>

        <div>
          <label className="filter-label">Dolžina (Longitude)</label>
          <input
            type="number"
            step="any"
            name="lng"
            placeholder="npr. 14.50"
            value={filters.lng || ""}
            onChange={handleChange}
            className="input-field"
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
          />
        </div>
      </div>
    </div>
  );
}

export default Filters;

