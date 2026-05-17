import React from "react";

function Filters({ filters, onFilterChange }) {
  const handleChange = (e) => {
    const { name, value } = e.target;
    onFilterChange({ ...filters, [name]: value });
  };

  return (
    <div style={{ marginBottom: "20px", padding: "15px", border: "1px solid #ddd", borderRadius: "8px" }}>
      <h3>Filters</h3>
      
      <div style={{ display: "flex", gap: "20px", marginBottom: "15px", flexWrap: "wrap" }}>
        <div>
          <label style={{ display: "block", marginBottom: "5px" }}>Start Date</label>
          <input
            type="date"
            name="startDate"
            value={filters.startDate || ""}
            onChange={handleChange}
            style={{ padding: "8px", borderRadius: "4px", border: "1px solid #ccc" }}
          />
        </div>
        <div>
          <label style={{ display: "block", marginBottom: "5px" }}>End Date</label>
          <input
            type="date"
            name="endDate"
            value={filters.endDate || ""}
            onChange={handleChange}
            style={{ padding: "8px", borderRadius: "4px", border: "1px solid #ccc" }}
          />
        </div>

        <div>
          <label style={{ display: "block", marginBottom: "5px" }}>Latitude</label>
          <input
            type="number"
            step="any"
            name="lat"
            placeholder="e.g. 46.05"
            value={filters.lat || ""}
            onChange={handleChange}
            style={{ padding: "8px", borderRadius: "4px", border: "1px solid #ccc", width: "120px" }}
          />
        </div>

        <div>
          <label style={{ display: "block", marginBottom: "5px" }}>Longitude</label>
          <input
            type="number"
            step="any"
            name="lng"
            placeholder="e.g. 14.50"
            value={filters.lng || ""}
            onChange={handleChange}
            style={{ padding: "8px", borderRadius: "4px", border: "1px solid #ccc", width: "120px" }}
          />
        </div>

        <div>
          <label style={{ display: "block", marginBottom: "5px" }}>Radius (km)</label>
          <input
            type="number"
            name="radius"
            placeholder="e.g. 50"
            value={filters.radius || ""}
            onChange={handleChange}
            style={{ padding: "8px", borderRadius: "4px", border: "1px solid #ccc", width: "100px" }}
          />
        </div>
      </div>
    </div>
  );
}

export default Filters;
