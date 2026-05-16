const Hydro = require("../models/Hydro");

function createStationId(stationName) {
  return stationName.toLowerCase().replace(/\s+/g, "_");
}
function isValidLatitude(value) {
  return value == null || (!isNaN(value) && value >= -90 && value <= 90);
}

function isValidLongitude(value) {
  return value == null || (!isNaN(value) && value >= -180 && value <= 180);
}
exports.getAllHydro = async (req, res) => {
  const records = await Hydro.find().sort({ createdAt: -1 });
  res.json(records);
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

    const record = await Hydro.create({
      stationId: createStationId(stationName),
      stationName: stationName.trim(),
      riverName: riverName.trim(),
      latitude,
      longitude,
      waterLevel,
      waterFlow,
    });

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
    const { stationName, riverName } = req.body;

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

    res.json(record);
  } catch (err) {
    res.status(500).json({
      message: "Failed to update hydro record",
      error: err.message,
    });
  }
};

exports.deleteHydro = async (req, res) => {
  const record = await Hydro.findByIdAndDelete(req.params.id);

  if (!record) return res.status(404).json({ message: "Hydro record not found" });

  res.json({ message: "Hydro record deleted" });
};