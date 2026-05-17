import apiClient from "../client/apiClient";

export async function getHydroData(filters = {}) {
  const response = await apiClient.get("/hydro", { params: filters });
  return response.data;
}