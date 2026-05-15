const Meteo = require("../models/Meteo");

function createStationId(stationName) {
  return stationName.toLowerCase().replace(/\s+/g, "_");
}

exports.getAllMeteo = async (req, res) => {
  const records = await Meteo.find().sort({ createdAt: -1 });
  res.json(records);
};

exports.createMeteo = async (req, res) => {
  const { stationName, latitude, longitude, temperature, humidity, windSpeed, windDirection, precipitation } = req.body;

  if (!stationName || temperature == null || humidity == null) {
    return res.status(400).json({ message: "Invalid meteo input" });
  }

  const record = await Meteo.create({
    stationId: createStationId(stationName),
    stationName,
    latitude,
    longitude,
    temperature,
    humidity,
    windSpeed,
    windDirection,
    precipitation,
  });

  res.status(201).json(record);
};

exports.updateMeteo = async (req, res) => {
  const record = await Meteo.findByIdAndUpdate(req.params.id, req.body, {
    new: true,
    runValidators: true,
  });

  if (!record) return res.status(404).json({ message: "Meteo record not found" });

  res.json(record);
};

exports.deleteMeteo = async (req, res) => {
  const record = await Meteo.findByIdAndDelete(req.params.id);

  if (!record) return res.status(404).json({ message: "Meteo record not found" });

  res.json({ message: "Meteo record deleted" });
};