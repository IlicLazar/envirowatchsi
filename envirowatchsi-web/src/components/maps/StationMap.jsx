import { useEffect, useRef, useState } from "react";
import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import L from "leaflet";

const animationStyles = `
@keyframes pulse-marker {
  0%   { transform: scale(1);   opacity: 1; }
  50%  { transform: scale(2);   opacity: 0.5; }
  100% { transform: scale(1);   opacity: 1; }
}
.marker-pin {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #3388ff;
  border: 2px solid white;
  box-shadow: 0 0 6px rgba(0,0,0,0.4);
}
.marker-pin.marker-animated {
  background: #e63946;
  animation: pulse-marker 0.7s ease-in-out 4;
}
`;

function createMarkerIcon(animated) {
  return L.divIcon({
    className: "",
    html: `<div class="marker-pin${animated ? " marker-animated" : ""}"></div>`,
    iconSize: [14, 14],
    iconAnchor: [7, 7],
    popupAnchor: [0, -7],
  });
}

function StationMap({ data }) {
  const defaultCenter = [46.1512, 14.9955];
  const prevDataRef = useRef([]);
  const [animatedIds, setAnimatedIds] = useState(new Set());

  useEffect(() => {
    const prevMap = new Map(prevDataRef.current.map((item) => [item._id, item]));

    const newOrUpdated = data.filter((item) => {
      if (!prevMap.has(item._id)) return true;
      const prev = prevMap.get(item._id);
      return (
        prev.latitude !== item.latitude ||
        prev.longitude !== item.longitude ||
        prev.stationName !== item.stationName
      );
    });

    if (newOrUpdated.length > 0) {
      const ids = new Set(newOrUpdated.map((item) => item._id));
      setAnimatedIds(ids);
      const timer = setTimeout(() => setAnimatedIds(new Set()), 3500);
      prevDataRef.current = data;
      return () => clearTimeout(timer);
    }

    prevDataRef.current = data;
  }, [data]);

  return (
    <div style={{ height: "400px", width: "100%", marginTop: "30px" }}>
      <style>{animationStyles}</style>
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
              icon={createMarkerIcon(animatedIds.has(item._id))}
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