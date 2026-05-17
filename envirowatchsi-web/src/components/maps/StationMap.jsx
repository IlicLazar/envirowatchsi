import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";

function StationMap({ data }) {
  const defaultCenter = [46.1512, 14.9955]; // Slovenia center

  return (
    <div style={{ height: "400px", width: "100%", marginTop: "30px" }}>
      <h2>Station Locations</h2>

      <MapContainer
        center={defaultCenter}
        zoom={8}
        style={{ height: "100%", width: "100%" }}
      >
        <TileLayer
          attribution="&copy; OpenStreetMap contributors"
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {data
          .filter((item) => item.latitude && item.longitude)
          .map((item) => (
            <Marker
              key={item._id}
              position={[item.latitude, item.longitude]}
            >
              <Popup>
                <strong>{item.stationName}</strong>
                <br />
                Latitude: {item.latitude}
                <br />
                Longitude: {item.longitude}
              </Popup>
            </Marker>
          ))}
      </MapContainer>
    </div>
  );
}

export default StationMap;