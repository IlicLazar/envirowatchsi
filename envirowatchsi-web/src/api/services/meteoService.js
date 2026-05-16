import apiClient from "../client/apiClient";

export async function getMeteoData() {
  const response = await apiClient.get("/meteo");
  return response.data;
}