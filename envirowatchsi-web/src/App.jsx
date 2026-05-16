import { BrowserRouter, Routes, Route } from "react-router-dom";
import HomePage from "./pages/HomePage";
import MeteoPage from "./pages/MeteoPage";
import AirQualityPage from "./pages/AirQualityPage";
import HydroPage from "./pages/HydroPage";
import Navbar from "./components/Navbar";

function App() {
  return (
    <BrowserRouter>
    <Navbar />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/meteo" element={<MeteoPage />} />
        <Route path="/air-quality" element={<AirQualityPage />} />
        <Route path="/hydro" element={<HydroPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;