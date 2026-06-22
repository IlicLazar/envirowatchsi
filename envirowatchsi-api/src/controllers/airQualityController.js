const AirQuality = require("../models/AirQuality");
const { broadcastEvent } = require("../websocket/websocketServer");
const { getCache, setCache, clearCache, createCacheKey } = require("../utils/cache");

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

exports.getAllAirQuality = async (req, res) => {
  try {
    const cacheKey = createCacheKey("air-quality:all", req.query);
    const cached = getCache(cacheKey);

    if (cached) {
      return res.json(cached);
    }

    const query = buildFilterQuery(req.query);
    if (!req.query.startDate && !req.query.endDate) {
      const sevenDaysAgo = new Date();
      sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);

      query.measuredAt = { $gte: sevenDaysAgo };
    }

    const records = await AirQuality.find(query).sort({ measuredAt: -1 });

    setCache(cacheKey, records);
    res.json(records);
  } catch (err) {
    res.status(500).json({
      message: "Failed to get air quality records",
      error: err.message,
    });
  }
};

exports.createAirQuality = async (req, res) => {
  try {
    const {
      stationName,
      latitude,
      longitude,
      aqi,
      airQualityIndex,
      pm10,
      pm2_5,
      o3,
      co,
      so2,
    } = req.body;

    if (!stationName || stationName.trim() === "") {
      return res.status(400).json({
        message: "Station name is required",
      });
    }

    const finalAqi = airQualityIndex ?? aqi;

    if (finalAqi == null || isNaN(finalAqi)) {
      return res.status(400).json({
        message: "Air quality index must be a valid number",
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

    const record = await AirQuality.create({
      stationId: createStationId(stationName),
      stationName: stationName.trim(),
      latitude,
      longitude,
      location,
      pm10,
      pm2_5,
      o3,
      co,
      so2,
      airQualityIndex: finalAqi,
    });

    clearCache("air-quality:");

    broadcastEvent({ type: "AIR_QUALITY_CREATED", data: record });
    res.status(201).json(record);

  } catch (err) {

    res.status(500).json({
      message: "Failed to create air quality record",
      error: err.message,
    });
  }
};

exports.updateAirQuality = async (req, res) => {
  try {
    const { stationName, latitude, longitude, aqi, airQualityIndex } = req.body;

    if (stationName != null && stationName.trim() === "") {
      return res.status(400).json({ message: "Station name cannot be empty" });
    }

    const finalAqi = airQualityIndex ?? aqi;

    if (finalAqi != null && isNaN(finalAqi)) {
      return res.status(400).json({
        message: "Air quality index must be a valid number",
      });
    }

    const updateData = {
      ...req.body,
    };

    if (stationName) {
      updateData.stationName = stationName.trim();
      updateData.stationId = createStationId(stationName);
    }

    if (finalAqi != null) {
      updateData.airQualityIndex = finalAqi;
      delete updateData.aqi;
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

    const record = await AirQuality.findByIdAndUpdate(
      req.params.id,
      updateData,
      { new: true, runValidators: true }
    );

    if (!record) {
      return res.status(404).json({ message: "Air quality record not found" });
    }

    clearCache("air-quality:");

    broadcastEvent({ type: "AIR_QUALITY_UPDATED", data: record });
    res.json(record);
  } catch (err) {
    res.status(500).json({
      message: "Failed to update air quality record",
      error: err.message,
    });
  }
};

exports.getNearbyAirQuality = async (req, res) => {
  try {
    const lat = Number(req.query.lat);
    const lng = Number(req.query.lng);
    const radius = Number(req.query.radius) || 10000;

    if (isNaN(lat) || lat < -90 || lat > 90) {
      return res.status(400).json({
        message: "Valid lat query parameter is required",
      });
    }

    if (isNaN(lng) || lng < -180 || lng > 180) {
      return res.status(400).json({
        message: "Valid lng query parameter is required",
      });
    }

    const cacheKey = createCacheKey("air-quality:near", req.query);
    const cached = getCache(cacheKey);

    if (cached) {
      return res.json(cached);
    }

    const records = await AirQuality.find({
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

    setCache(cacheKey, records);
    res.json(records);
  } catch (err) {
    res.status(500).json({
      message: "Failed to find nearby air quality records",
      error: err.message,
    });
  }
};

exports.deleteAirQuality = async (req, res) => {
  try {
    const record = await AirQuality.findByIdAndDelete(req.params.id);

    if (!record) {
      return res.status(404).json({
        message: "Air quality record not found",
      });
    }

    clearCache("air-quality:");

    broadcastEvent({
      type: "AIR_QUALITY_DELETED",
      data: record,
    });

    res.json({
      message: "Air quality record deleted",
    });
  } catch (err) {
    res.status(500).json({
      message: "Failed to delete air quality record",
      error: err.message,
    });
  }
};