const Meteo = require("../models/Meteo");

function createStationId(stationName) {
  return stationName.toLowerCase().replace(/\s+/g, "_");
}
function isValidLatitude(value) {
  return value == null || (!isNaN(value) && value >= -90 && value <= 90);
}

function isValidLongitude(value) {
  return value == null || (!isNaN(value) && value >= -180 && value <= 180);
}
exports.getAllMeteo = async (req, res) => {
  const records = await Meteo.find().sort({ createdAt: -1 });
  res.json(records);
};

exports.createMeteo = async (req, res) => {
  try {
    const {
      stationName,
      latitude,
      longitude,
      temperature,
      humidity,
      windSpeed,
      windDirection,
      precipitation,
    } = req.body;

    if (!stationName || stationName.trim() === "") {
      return res.status(400).json({
        message: "Station name is required",
      });
    }

    if (temperature == null || isNaN(temperature)) {
      return res.status(400).json({
        message: "Temperature must be a valid number",
      });
    }

    if (humidity == null || isNaN(humidity)) {
      return res.status(400).json({
        message: "Humidity must be a valid number",
      });
    }

    if (!isValidLatitude(latitude)) {
      return res.status(400).json({ message: "Latitude must be between -90 and 90" });
    }
    
    if (!isValidLongitude(longitude)) {
      return res.status(400).json({ message: "Longitude must be between -180 and 180" });
    }

    const record = await Meteo.create({
      stationId: createStationId(stationName),
      stationName: stationName.trim(),
      latitude,
      longitude,
      temperature,
      humidity,
      windSpeed,
      windDirection,
      precipitation,
    });

    res.status(201).json(record);
  } catch (err) {
    res.status(500).json({
      message: "Failed to create meteo record",
      error: err.message,
    });
  }
};

exports.updateMeteo = async (req, res) => {
  try {
    const {
      stationName,
      temperature,
      humidity,
    } = req.body;

    if (stationName != null && stationName.trim() === "") {
      return res.status(400).json({
        message: "Station name cannot be empty",
      });
    }

    if (temperature != null && isNaN(temperature)) {
      return res.status(400).json({
        message: "Temperature must be a valid number",
      });
    }

    if (humidity != null && isNaN(humidity)) {
      return res.status(400).json({
        message: "Humidity must be a valid number",
      });
    }

    const updateData = {
      ...req.body,
    };

    if (stationName) {
      updateData.stationName = stationName.trim();
      updateData.stationId = createStationId(stationName);
    }

    if (!isValidLatitude(latitude)) {
      return res.status(400).json({ message: "Latitude must be between -90 and 90" });
    }
    
    if (!isValidLongitude(longitude)) {
      return res.status(400).json({ message: "Longitude must be between -180 and 180" });
    }

    const record = await Meteo.findByIdAndUpdate(
      req.params.id,
      updateData,
      {
        new: true,
        runValidators: true,
      }
    );

    if (!record) {
      return res.status(404).json({
        message: "Meteo record not found",
      });
    }

    res.json(record);

  } catch (err) {

    res.status(500).json({
      message: "Failed to update meteo record",
      error: err.message,
    });
  }
};

exports.deleteMeteo = async (req, res) => {
  const record = await Meteo.findByIdAndDelete(req.params.id);

  if (!record) return res.status(404).json({ message: "Meteo record not found" });

  res.json({ message: "Meteo record deleted" });
};