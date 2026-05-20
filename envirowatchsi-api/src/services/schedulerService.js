const DataSource = require("../models/DataSource");
const AirQuality = require("../models/AirQuality");
const Meteo = require("../models/Meteo");
const Hydro = require("../models/Hydro");
const { parseAirQualityData, parseMeteoData, parseHydroData } = require("../utils/xmlParser");
const { broadcastEvent } = require("../websocket/websocketServer");

async function syncSource(source) {
  console.log(`[Scheduler] Syncing data source: ${source.name} (${source.type}) from ${source.url}`);
  try {
    const res = await fetch(source.url);
    if (!res.ok) {
      throw new Error(`Failed to fetch XML. HTTP Status: ${res.status}`);
    }
    const xmlText = await res.text();

    let stations = [];
    let Model;
    let eventType;

    if (source.type === "air-quality") {
      stations = parseAirQualityData(xmlText);
      Model = AirQuality;
      eventType = "AIR_QUALITY_CREATED";
    } else if (source.type === "meteo") {
      stations = parseMeteoData(xmlText);
      Model = Meteo;
      eventType = "METEO_CREATED";
    } else if (source.type === "hydro") {
      stations = parseHydroData(xmlText);
      Model = Hydro;
      eventType = "HYDRO_CREATED";
    } else {
      throw new Error(`Unknown data source type: ${source.type}`);
    }

    console.log(`[Scheduler] Parsed ${stations.length} stations. Checking database for duplicates...`);

    let newRecordsCount = 0;
    for (const record of stations) {
      const query = {
        measuredAt: record.measuredAt
      };
      if (record.stationId) {
        query.stationId = record.stationId;
      } else {
        query.stationName = record.stationName;
      }

      const exists = await Model.findOne(query);
      if (!exists) {
        const createdRecord = await Model.create(record);
        newRecordsCount++;
        broadcastEvent({ type: eventType, data: createdRecord });
      }
    }

    console.log(`[Scheduler] Sync finished successfully for "${source.name}". Added ${newRecordsCount} new records.`);

    source.lastRefreshed = new Date();
    source.lastStatus = "success";
    source.lastError = null;
    await source.save();

    broadcastEvent({ type: "DATA_SOURCE_UPDATED", data: source });
    
    return { success: true, newRecordsCount };
  } catch (error) {
    console.error(`[Scheduler] Error syncing ${source.name}:`, error.message);
    
    source.lastRefreshed = new Date();
    source.lastStatus = "error";
    source.lastError = error.message;
    await source.save();

    broadcastEvent({ type: "DATA_SOURCE_UPDATED", data: source });
    
    return { success: false, error: error.message };
  }
}

async function checkAndSyncSources() {
  try {
    const activeSources = await DataSource.find({ isActive: true });
    const now = new Date();

    for (const source of activeSources) {
      const intervalMs = source.refreshIntervalMinutes * 60 * 1000;
      const lastTime = source.lastRefreshed ? new Date(source.lastRefreshed).getTime() : 0;

      if (!source.lastRefreshed || (now.getTime() - lastTime) >= intervalMs) {
        syncSource(source).catch((err) => {
          console.error(`[Scheduler] Unhandled error during background sync for "${source.name}":`, err.message);
        });
      }
    }
  } catch (error) {
    console.error("[Scheduler] Error in checkAndSyncSources loop:", error.message);
  }
}

function startScheduler() {
  console.log("[Scheduler] Background data ingestion scheduler initialized.");
  checkAndSyncSources();
  setInterval(checkAndSyncSources, 60 * 1000);
}

module.exports = {
  startScheduler,
  syncSource
};
