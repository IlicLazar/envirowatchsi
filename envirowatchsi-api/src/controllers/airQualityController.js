const AirQuality = require("../models/AirQuality");

function createStationId(stationName) {
  return stationName.toLowerCase().replace(/\s+/g, "_");
}
function isValidLatitude(value) {
  return value == null || (!isNaN(value) && value >= -90 && value <= 90);
}

function isValidLongitude(value) {
  return value == null || (!isNaN(value) && value >= -180 && value <= 180);
}
exports.getAllAirQuality = async (req, res) => {
  const records = await AirQuality.find().sort({ createdAt: -1 });
  res.json(records);
};

exports.createAirQuality = async (req, res) => {
  try {
    const {
      stationName,
      latitude,
      longitude,
      aqi,
      airQualityIndex,
      pm10,
      pm2_5,
      o3,
      co,
      so2,
    } = req.body;

    if (!stationName || stationName.trim() === "") {
      return res.status(400).json({
        message: "Station name is required",
      });
    }

    const finalAqi = airQualityIndex ?? aqi;

    if (finalAqi == null || isNaN(finalAqi)) {
      return res.status(400).json({
        message: "Air quality index must be a valid number",
      });
    }

    if (!isValidLatitude(latitude)) {
      return res.status(400).json({ message: "Latitude must be between -90 and 90" });
    }
    
    if (!isValidLongitude(longitude)) {
      return res.status(400).json({ message: "Longitude must be between -180 and 180" });
    }

    const record = await AirQuality.create({
      stationId: createStationId(stationName),
      stationName: stationName.trim(),
      latitude,
      longitude,
      pm10,
      pm2_5,
      o3,
      co,
      so2,
      airQualityIndex: finalAqi,
    });

    res.status(201).json(record);

  } catch (err) {

    res.status(500).json({
      message: "Failed to create air quality record",
      error: err.message,
    });
  }
};

exports.updateAirQuality = async (req, res) => {
  try {
    const { stationName, aqi, airQualityIndex } = req.body;

    if (stationName != null && stationName.trim() === "") {
      return res.status(400).json({ message: "Station name cannot be empty" });
    }

    const finalAqi = airQualityIndex ?? aqi;

    if (finalAqi != null && isNaN(finalAqi)) {
      return res.status(400).json({
        message: "Air quality index must be a valid number",
      });
    }

    const updateData = {
      ...req.body,
    };

    if (stationName) {
      updateData.stationName = stationName.trim();
      updateData.stationId = createStationId(stationName);
    }

    if (finalAqi != null) {
      updateData.airQualityIndex = finalAqi;
      delete updateData.aqi;
    }

    if (!isValidLatitude(latitude)) {
      return res.status(400).json({ message: "Latitude must be between -90 and 90" });
    }
    
    if (!isValidLongitude(longitude)) {
      return res.status(400).json({ message: "Longitude must be between -180 and 180" });
    }

    const record = await AirQuality.findByIdAndUpdate(
      req.params.id,
      updateData,
      { new: true, runValidators: true }
    );

    if (!record) {
      return res.status(404).json({ message: "Air quality record not found" });
    }

    res.json(record);
  } catch (err) {
    res.status(500).json({
      message: "Failed to update air quality record",
      error: err.message,
    });
  }
};

exports.deleteAirQuality = async (req, res) => {
  const record = await AirQuality.findByIdAndDelete(req.params.id);

  if (!record) return res.status(404).json({ message: "Air quality record not found" });

  res.json({ message: "Air quality record deleted" });
};