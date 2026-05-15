const AirQuality = require("../models/AirQuality");

function createStationId(stationName) {
  return stationName.toLowerCase().replace(/\s+/g, "_");
}

exports.getAllAirQuality = async (req, res) => {
  const records = await AirQuality.find().sort({ createdAt: -1 });
  res.json(records);
};

exports.createAirQuality = async (req, res) => {
  const { stationName, latitude, longitude, aqi, airQualityIndex, pm10, pm2_5, o3, co, so2 } = req.body;

  if (!stationName || (aqi == null && airQualityIndex == null)) {
    return res.status(400).json({ message: "Invalid air quality input" });
  }

  const record = await AirQuality.create({
    stationId: createStationId(stationName),
    stationName,
    latitude,
    longitude,
    pm10,
    pm2_5,
    o3,
    co,
    so2,
    airQualityIndex: airQualityIndex ?? aqi,
  });

  res.status(201).json(record);
};

exports.updateAirQuality = async (req, res) => {
  const record = await AirQuality.findByIdAndUpdate(req.params.id, req.body, {
    new: true,
    runValidators: true,
  });

  if (!record) return res.status(404).json({ message: "Air quality record not found" });

  res.json(record);
};

exports.deleteAirQuality = async (req, res) => {
  const record = await AirQuality.findByIdAndDelete(req.params.id);

  if (!record) return res.status(404).json({ message: "Air quality record not found" });

  res.json({ message: "Air quality record deleted" });
};