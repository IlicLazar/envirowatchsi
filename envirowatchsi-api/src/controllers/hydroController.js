const Hydro = require("../models/Hydro");
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

exports.getAllHydro = async (req, res) => {
  try {
    const query = buildFilterQuery(req.query);
    const records = await Hydro.find(query).sort({ createdAt: -1 });
    res.json(records);
  } catch (err) {
    res.status(500).json({ message: "Failed to get hydro records", error: err.message });
  }
};

exports.createHydro = async (req, res) => {
  try {
    const {
      stationName,
      riverName,
      latitude,
      longitude,
      waterLevel,
      waterFlow,
    } = req.body;

    if (!stationName || stationName.trim() === "") {
      return res.status(400).json({
        message: "Station name is required",
      });
    }

    if (!riverName || riverName.trim() === "") {
      return res.status(400).json({
        message: "River name is required",
      });
    }

    if (!isValidLatitude(latitude)) {
      return res.status(400).json({ message: "Latitude must be between -90 and 90" });
    }
    
    if (!isValidLongitude(longitude)) {
      return res.status(400).json({ message: "Longitude must be between -180 and 180" });
    }
let location;

if (latitude !== undefined && longitude !== undefined) {
  location = {
    type: "Point",
    coordinates: [longitude, latitude],
  };
}
    const record = await Hydro.create({
      stationId: createStationId(stationName),
      stationName: stationName.trim(),
      riverName: riverName.trim(),
      latitude,
      longitude,
      location,
      waterLevel,
      waterFlow,
    });

    broadcastEvent({ type: "HYDRO_CREATED", data: record });
    res.status(201).json(record);

  } catch (err) {

    res.status(500).json({
      message: "Failed to create hydro record",
      error: err.message,
    });
  }
};

exports.updateHydro = async (req, res) => {
  try {
    const { stationName, riverName, latitude, longitude } = req.body;

    if (stationName != null && stationName.trim() === "") {
      return res.status(400).json({
        message: "Station name cannot be empty",
      });
    }

    if (riverName != null && riverName.trim() === "") {
      return res.status(400).json({
        message: "River name cannot be empty",
      });
    }

    const updateData = {
      ...req.body,
    };

    if (stationName) {
      updateData.stationName = stationName.trim();
      updateData.stationId = createStationId(stationName);
    }

    if (riverName) {
      updateData.riverName = riverName.trim();
    }

    if (!isValidLatitude(latitude)) {
      return res.status(400).json({ message: "Latitude must be between -90 and 90" });
    }
    
    if (!isValidLongitude(longitude)) {
      return res.status(400).json({ message: "Longitude must be between -180 and 180" });
    }
if (latitude !== undefined && longitude !== undefined) {
  updateData.location = {
    type: "Point",
    coordinates: [longitude, latitude],
  };
}
    const record = await Hydro.findByIdAndUpdate(
      req.params.id,
      updateData,
      {
        new: true,
        runValidators: true,
      }
    );

    if (!record) {
      return res.status(404).json({
        message: "Hydro record not found",
      });
    }

    broadcastEvent({ type: "HYDRO_UPDATED", data: record });
    res.json(record);
  } catch (err) {
    res.status(500).json({
      message: "Failed to update hydro record",
      error: err.message,
    });
  }
};
exports.getNearbyHydro = async (req, res) => {
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

    const records = await Hydro.find({
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
      message: "Failed to find nearby hydro records",
      error: err.message,
    });
  }
};
exports.deleteHydro = async (req, res) => {
  const record = await Hydro.findByIdAndDelete(req.params.id);

  if (!record) return res.status(404).json({ message: "Hydro record not found" });
broadcastEvent({ type: "HYDRO_DELETED", data: record });
  res.json({ message: "Hydro record deleted" });
};