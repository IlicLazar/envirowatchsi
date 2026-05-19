import { NavLink, useLocation } from "react-router-dom";

function Navbar() {
  const location = useLocation(); // Triggers re-render on route change
  const isAuthenticated = !!localStorage.getItem("token");

  return (
    <nav className="nav-container">
      <NavLink to="/" className="nav-brand">
        🌍 EnviroWatchSI
      </NavLink>
      <div className="nav-links">
        <NavLink to="/" end className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}>
          Domov
        </NavLink>
        <NavLink to="/meteo" className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}>
          Meteo
        </NavLink>
        <NavLink to="/air-quality" className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}>
          Kakovost Zraka
        </NavLink>
        <NavLink to="/hydro" className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}>
          Vode (Hydro)
        </NavLink>
        {isAuthenticated ? (
          <NavLink to="/admin" className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}>
            Admin
          </NavLink>
        ) : (
          <NavLink to="/login" className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}>
            Login
          </NavLink>
        )}
      </div>
    </nav>
  );
}

export default Navbar;
