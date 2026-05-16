function StatsCard({ title, value }) {
    return (
      <div
        style={{
          border: "1px solid #ccc",
          padding: "20px",
          borderRadius: "10px",
          minWidth: "200px",
          backgroundColor: "#f8f8f8",
        }}
      >
        <h3>{title}</h3>
  
        <p
          style={{
            fontSize: "24px",
            fontWeight: "bold",
          }}
        >
          {value}
        </p>
      </div>
    );
  }
  
  export default StatsCard;