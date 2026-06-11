const mongoose = require("mongoose");

const airQualitySchema = new mongoose.Schema(
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

    pm10: Number,
    pm2_5: Number,
    o3: Number,
    co: Number,
    so2: Number,
    airQualityIndex: Number,
  },
  {
    timestamps: true,
  }
);

airQualitySchema.index({ location: "2dsphere" });

module.exports = mongoose.model("AirQuality", airQualitySchema);