import apiClient from "./apiClient";

export async function getAirQualityData() {
  const response = await apiClient.get("/air-quality");
  return response.data;
}