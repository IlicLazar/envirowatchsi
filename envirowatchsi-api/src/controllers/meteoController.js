const Meteo = require("../models/Meteo");
const { broadcastEvent } = require("../websocket/websocketServer");

function createStationId(stationName) {
  return stationName.toLowerCase().replace(/\s+/g, "_");
}
function isValidLatitude(value) {
  return value == null || (!isNaN(value) && value >= -90 && value <= 90);
}

function isValidLongitude(value) {
  return value == null || (!isNaN(value) && value >= -180 && value <= 180);
}
const { buildFilterQuery } = require("../utils/filterUtils");

exports.getAllMeteo = async (req, res) => {
  try {
    const query = buildFilterQuery(req.query);
    const records = await Meteo.find(query).sort({ createdAt: -1 });
    res.json(records);
  } catch (err) {
    res.status(500).json({ message: "Failed to get meteo records", error: err.message });
  }
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
    if (latitude !== undefined && longitude !== undefined) {
      req.body.location = {
        type: "Point",
        coordinates: [longitude, latitude],
      };
    }
    const record = await Meteo.create({
      stationId: createStationId(stationName),
      stationName: stationName.trim(),
      latitude,
      longitude,
      location: req.body.location,
      temperature,
      humidity,
      windSpeed,
      windDirection,
      precipitation,
    });

  broadcastEvent({
  type: "METEO_CREATED",
  data: record,
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
      latitude,
      longitude,
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
    if (req.body.latitude !== undefined && req.body.longitude !== undefined) {
      updateData.location = {
        type: "Point",
        coordinates: [req.body.longitude, req.body.latitude],
      };
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
    broadcastEvent({
  type: "METEO_UPDATED",
  data: record,
});
    res.json(record);
    
  } catch (err) {

    res.status(500).json({
      message: "Failed to update meteo record",
      error: err.message,
    });
  }
};
exports.getNearbyMeteo = async (req, res) => {
  try {
    const lat = Number(req.query.lat);
    const lng = Number(req.query.lng);
    const radius = Number(req.query.radius) || 10000;

    if (isNaN(lat) || lat < -90 || lat > 90) {
      return res.status(400).json({ message: "Valid lat query parameter is required" });
    }

    if (isNaN(lng) || lng < -180 || lng > 180) {
      return res.status(400).json({ message: "Valid lng query parameter is required" });
    }

    const records = await Meteo.find({
      location: {
        $near: {
          $geometry: {
            type: "Point",
            coordinates: [lng, lat],
          },
          $maxDistance: radius,
        },
      },
    });

    res.json(records);
  } catch (err) {
    res.status(500).json({
      message: "Failed to find nearby meteo records",
      error: err.message,
    });
  }
};
exports.deleteMeteo = async (req, res) => {
  const record = await Meteo.findByIdAndDelete(req.params.id);

  if (!record) return res.status(404).json({ message: "Meteo record not found" });
broadcastEvent({
  type: "METEO_DELETED",
  data: record,
});
  res.json({ message: "Meteo record deleted" });
};