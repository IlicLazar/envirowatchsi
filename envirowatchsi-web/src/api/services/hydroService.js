import apiClient from "../client/apiClient";

export async function getHydroData() {
  const response = await apiClient.get("/hydro");
  return response.data;
}