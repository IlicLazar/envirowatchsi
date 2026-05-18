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

export async function updateDataSource(id, dataSource) {
  const response = await apiClient.put(`/data-sources/${id}`, dataSource, {
    headers: getAuthHeaders(),
  });

  return response.data;
}

export async function deleteDataSource(id) {
  const response = await apiClient.delete(`/data-sources/${id}`, {
    headers: getAuthHeaders(),
  });

  return response.data;
}