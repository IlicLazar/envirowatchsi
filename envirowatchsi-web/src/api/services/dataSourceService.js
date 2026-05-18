import apiClient from "../client/apiClient";

function getAuthHeaders() {
  const token = localStorage.getItem("token");

  return {
    Authorization: `Bearer ${token}`,
  };
}

export async function getDataSources() {
  const response = await apiClient.get("/data-sources", {
    headers: getAuthHeaders(),
  });

  return response.data;
}

export async function createDataSource(dataSource) {
  const response = await apiClient.post("/data-sources", dataSource, {
    headers: getAuthHeaders(),
  });

  return response.data;
}