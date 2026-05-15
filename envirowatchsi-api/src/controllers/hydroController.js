const Hydro = require("../models/Hydro");

function createStationId(stationName) {
  return stationName.toLowerCase().replace(/\s+/g, "_");
}

exports.getAllHydro = async (req, res) => {
  const records = await Hydro.find().sort({ createdAt: -1 });
  res.json(records);
};

exports.createHydro = async (req, res) => {
  const { stationName, riverName, latitude, longitude, waterLevel, waterFlow } = req.body;

  if (!stationName || !riverName) {
    return res.status(400).json({ message: "Invalid hydro input" });
  }

  const record = await Hydro.create({
    stationId: createStationId(stationName),
    stationName,
    riverName,
    latitude,
    longitude,
    waterLevel,
    waterFlow,
  });

  res.status(201).json(record);
};

exports.updateHydro = async (req, res) => {
  const record = await Hydro.findByIdAndUpdate(req.params.id, req.body, {
    new: true,
    runValidators: true,
  });

  if (!record) return res.status(404).json({ message: "Hydro record not found" });

  res.json(record);
};

exports.deleteHydro = async (req, res) => {
  const record = await Hydro.findByIdAndDelete(req.params.id);

  if (!record) return res.status(404).json({ message: "Hydro record not found" });

  res.json({ message: "Hydro record deleted" });
};