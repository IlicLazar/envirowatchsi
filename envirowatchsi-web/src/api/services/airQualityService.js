import apiClient from "../client/apiClient";

export async function getAirQualityData(filters = {}) {
  const response = await apiClient.get("/air-quality", { params: filters });
  return response.data;
}