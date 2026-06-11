import axios from "axios";

const apiClient = axios.create({
  baseURL: "http://68.210.201.189:3000/api",
  headers: {
    "Content-Type": "application/json",
  },
});

export default apiClient;