function StatsCard({ title, value }) {
  const lowerTitle = title.toLowerCase();
  let typeClass = "";
  
  if (
    lowerTitle.includes("temp") ||
    lowerTitle.includes("humid") ||
    window.location.pathname.includes("meteo")
  ) {
    typeClass = "meteo";
  } else if (
    lowerTitle.includes("aqi") ||
    lowerTitle.includes("pm") ||
    window.location.pathname.includes("air-quality")
  ) {
    typeClass = "air-quality";
  } else if (
    lowerTitle.includes("water") ||
    lowerTitle.includes("flow") ||
    window.location.pathname.includes("hydro")
  ) {
    typeClass = "hydro";
  }

  return (
    <div className={`stats-card ${typeClass}`}>
      <span className="stats-card-title">{title}</span>
      <span className="stats-card-value">{value}</span>
    </div>
  );
}

export default StatsCard;