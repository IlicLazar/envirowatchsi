const mongoose = require("mongoose");

const hydroSchema = new mongoose.Schema(
  {
    stationId: String,
    stationName: { type: String, required: true },
    latitude: Number,
    longitude: Number,

    location: {
      type: {
        type: String,
        enum: ["Point"],
        default: "Point",
      },
      coordinates: {
        type: [Number],
      },
    },

    riverName: { type: String, required: true },
    measuredAt: { type: Date, default: Date.now },

    waterLevel: Number,
    waterFlow: Number,
  },
  { timestamps: true }
);

hydroSchema.index({ location: "2dsphere" });
const Hydro = mongoose.model("Hydro", hydroSchema);
Hydro.syncIndexes();
module.exports = Hydro;