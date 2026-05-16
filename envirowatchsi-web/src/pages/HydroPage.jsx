import { useEffect, useState } from "react";
import { getHydroData } from "../api/hydroService";
import HydroTable from "../components/HydroTable";

function HydroPage() {
  const [hydroData, setHydroData] = useState([]);
  const [selectedRecord, setSelectedRecord] = useState(null);

  useEffect(() => {
    async function fetchData() {
      const data = await getHydroData();
      setHydroData(data);
    }

    fetchData();
  }, []);

  return (
    <div style={{ padding: "20px" }}>
      <h1>Hydro Data</h1>

      <HydroTable
        data={hydroData}
        onSelectRecord={setSelectedRecord}
      />

      {selectedRecord && (
        <div
          style={{
            marginTop: "20px",
            padding: "15px",
            border: "1px solid #ccc",
          }}
        >
          <h2>Record Details</h2>

          <p>
            <strong>Station:</strong> {selectedRecord.stationName}
          </p>

          <p>
            <strong>River:</strong> {selectedRecord.riverName}
          </p>

          <p>
            <strong>Water Level:</strong>{" "}
            {selectedRecord.waterLevel ?? "N/A"}
          </p>

          <p>
            <strong>Water Flow:</strong>{" "}
            {selectedRecord.waterFlow ?? "N/A"}
          </p>

          <p>
            <strong>Measured At:</strong>{" "}
            {selectedRecord.measuredAt ?? "N/A"}
          </p>
        </div>
      )}
    </div>
  );
}

export default HydroPage;