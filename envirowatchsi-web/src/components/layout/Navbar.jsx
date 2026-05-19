import { NavLink, useLocation } from "react-router-dom";

function Navbar() {
  const location = useLocation(); // Triggers re-render on route change
  const isAuthenticated = !!localStorage.getItem("token");

  return (
    <nav className="nav-container">
      <NavLink to="/" className="nav-brand" style={{ padding: 0, display: "flex", alignItems: "center", justifyContent: "center", height: "100%" }}>
        <img 
          src="/logo.png" 
          alt="EnviroWatchSI" 
          style={{ 
            height: "140px", 
            width: "auto", 
            marginTop: "-30px", 
            marginBottom: "-48px", 
            display: "block"
          }} 
        />
      </NavLink>
      <div className="nav-links">
        <NavLink to="/" end className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}>
          Domov
        </NavLink>
        <NavLink to="/meteo" className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}>
          Meteorologija
        </NavLink>
        <NavLink to="/air-quality" className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}>
          Kakovost zraka
        </NavLink>
        <NavLink to="/hydro" className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}>
          Hidrologija
        </NavLink>
        <NavLink to="/map" className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}>
          Zemljevid
        </NavLink>
        {isAuthenticated ? (
          <NavLink to="/admin" className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}>
            Admin
          </NavLink>
        ) : (
          <NavLink to="/login" className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}>
            Prijava
          </NavLink>
        )}
      </div>
    </nav>
  );
}

export default Navbar;
