import { getMeteoData } from "./meteoService";
import { getAirQualityData } from "./airQualityService";
import { getHydroData } from "./hydroService";

export async function getAllEnvironmentalData(filters = {}) {
  const [meteo, airQuality, hydro] = await Promise.all([
    getMeteoData(filters),
    getAirQualityData(filters),
    getHydroData(filters),
  ]);

  return {
    meteo,
    airQuality,
    hydro,
  };
}