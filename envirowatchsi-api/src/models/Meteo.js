const mongoose = require("mongoose");

const meteoSchema = new mongoose.Schema(
  {
    stationId: String,
    stationName: { type: String, required: true },
    latitude: Number,
    longitude: Number,
    measuredAt: { type: Date, default: Date.now },

    temperature: { type: Number, required: true },
    humidity: { type: Number, required: true },
    windSpeed: Number,
    windDirection: String,
    precipitation: Number,
  },
  { timestamps: true }
);

module.exports = mongoose.model("Meteo", meteoSchema);