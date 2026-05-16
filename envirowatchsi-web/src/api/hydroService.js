import apiClient from "./apiClient";

export async function getHydroData() {
  const response = await apiClient.get("/hydro");
  return response.data;
}