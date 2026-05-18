const express = require("express");
const cors = require("cors");

const airQualityRoutes = require("./routes/airQualityRoutes");
const meteoRoutes = require("./routes/meteoRoutes");
const hydroRoutes = require("./routes/hydroRoutes");
const authRoutes = require("./routes/auth");
const dataSourceRoutes = require("./routes/dataSourceRoutes");

const app = express();

app.use(cors());
app.use(express.json());
app.use("/api/data-sources", dataSourceRoutes);

app.get("/", (req, res) => {
  res.status(200).json({ message: "EnviroWatchSI API is running" });
});

app.get("/health", (req, res) => {
  res.status(200).json({ status: "OK" });
});

app.use("/api/air-quality", airQualityRoutes);
app.use("/api/meteo", meteoRoutes);
app.use("/api/hydro", hydroRoutes);
app.use("/api/auth", authRoutes);
console.log("Loaded routes: /api/auth");
module.exports = app;