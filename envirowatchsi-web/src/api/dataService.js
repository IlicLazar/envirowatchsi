import { getMeteoData } from "./meteoService";
import { getAirQualityData } from "./airQualityService";
import { getHydroData } from "./hydroService";

export async function getAllEnvironmentalData() {
  const [meteo, airQuality, hydro] = await Promise.all([
    getMeteoData(),
    getAirQualityData(),
    getHydroData(),
  ]);

  return {
    meteo,
    airQuality,
    hydro,
  };
}