import apiClient from "../client/apiClient";

export async function getMeteoData(filters = {}) {
  const response = await apiClient.get("/meteo", { params: filters });
  return response.data;
}