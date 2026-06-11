const mongoose = require("mongoose");

const meteoSchema = new mongoose.Schema(
  {
    stationId: String,

    stationName: {
      type: String,
      required: true,
    },

    latitude: Number,
    longitude: Number,

    location: {
      type: {
        type: String,
        enum: ["Point"],
        default: "Point",
      },
      coordinates: {
        type: [Number], // [longitude, latitude]
      },
    },

    measuredAt: {
      type: Date,
      default: Date.now,
    },

    temperature: {
      type: Number,
      required: true,
    },

    humidity: {
      type: Number,
      required: true,
    },

    windSpeed: Number,
    windDirection: String,
    precipitation: Number,
  },
  {
    timestamps: true,
  }
);

meteoSchema.index({ location: "2dsphere" });

module.exports = mongoose.model("Meteo", meteoSchema);