const mongoose = require("mongoose");

const hydroSchema = new mongoose.Schema(
  {
    stationId: String,
    stationName: { type: String, required: true },
    latitude: Number,
    longitude: Number,
    riverName: { type: String, required: true },
    measuredAt: { type: Date, default: Date.now },

    waterLevel: Number,
    waterFlow: Number,
  },
  { timestamps: true }
);

module.exports = mongoose.model("Hydro", hydroSchema);