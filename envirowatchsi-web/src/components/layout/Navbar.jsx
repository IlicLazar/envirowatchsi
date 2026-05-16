import { Link } from "react-router-dom";

function Navbar() {
  return (
    <nav
      style={{
        display: "flex",
        gap: "20px",
        padding: "20px",
        backgroundColor: "#eaeaea",
      }}
    >
      <Link to="/">Home</Link>
      <Link to="/meteo">Meteo</Link>
      <Link to="/air-quality">Air Quality</Link>
      <Link to="/hydro">Hydro</Link>
    </nav>
  );
}

export default Navbar;