import { BrowserRouter, Routes, Route } from "react-router-dom";
import HomePage from "./pages/HomePage";
import MeteoPage from "./pages/MeteoPage";
import AirQualityPage from "./pages/AirQualityPage";
import HydroPage from "./pages/HydroPage";
import MapPage from "./pages/MapPage";
import Navbar from "./components/layout/Navbar";
import LoginPage from "./pages/LoginPage";
import AdminPage from "./pages/AdminPage";

function App() {
  return (
    <BrowserRouter>
      <Navbar />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/meteo" element={<MeteoPage />} />
        <Route path="/air-quality" element={<AirQualityPage />} />
        <Route path="/hydro" element={<HydroPage />} />
        <Route path="/map" element={<MapPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/admin" element={<AdminPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;