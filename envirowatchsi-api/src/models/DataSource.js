const mongoose = require("mongoose");

const dataSourceSchema = new mongoose.Schema(
  {
    name: {
      type: String,
      required: true,
      trim: true,
    },

    type: {
      type: String,
      required: true,
      enum: ["meteo", "air-quality", "hydro"],
    },

    url: {
      type: String,
      required: true,
      trim: true,
    },

    isActive: {
      type: Boolean,
      default: true,
    },

    refreshIntervalMinutes: {
      type: Number,
      default: 60,
    },
  },
  { timestamps: true }
);

module.exports = mongoose.model("DataSource", dataSourceSchema);