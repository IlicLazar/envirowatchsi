const DataSource = require("../models/DataSource");
const AirQuality = require("../models/AirQuality");
const Meteo = require("../models/Meteo");
const Hydro = require("../models/Hydro");
const { parseAirQualityData, parseMeteoData, parseHydroData } = require("../utils/xmlParser");
const { broadcastEvent } = require("../websocket/websocketServer");

/**
 * Synchronizes a single data source instantly
 */
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
      // Robust duplicate prevention: same stationId (or stationName if ID absent) and same measuredAt timestamp
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
        // Broadcast in real-time to updating page charts/tables
        broadcastEvent({ type: eventType, data: createdRecord });
      }
    }

    console.log(`[Scheduler] Sync finished successfully for "${source.name}". Added ${newRecordsCount} new records.`);

    source.lastRefreshed = new Date();
    source.lastStatus = "success";
    source.lastError = null;
    await source.save();

    // Broadcast updated data source status to admin panels
    broadcastEvent({ type: "DATA_SOURCE_UPDATED", data: source });
    
    return { success: true, newRecordsCount };
  } catch (error) {
    console.error(`[Scheduler] Error syncing ${source.name}:`, error.message);
    
    source.lastRefreshed = new Date();
    source.lastStatus = "error";
    source.lastError = error.message;
    await source.save();

    // Broadcast the failure status
    broadcastEvent({ type: "DATA_SOURCE_UPDATED", data: source });
    
    return { success: false, error: error.message };
  }
}

/**
 * Checks all active data sources and synchronizes those that are due
 */
async function checkAndSyncSources() {
  try {
    const activeSources = await DataSource.find({ isActive: true });
    const now = new Date();

    for (const source of activeSources) {
      const intervalMs = source.refreshIntervalMinutes * 60 * 1000;
      const lastTime = source.lastRefreshed ? new Date(source.lastRefreshed).getTime() : 0;

      if (!source.lastRefreshed || (now.getTime() - lastTime) >= intervalMs) {
        // Run sync asynchronously so other sources don't block
        syncSource(source);
      }
    }
  } catch (error) {
    console.error("[Scheduler] Error in checkAndSyncSources loop:", error.message);
  }
}

/**
 * Initializes the background scheduler
 */
function startScheduler() {
  console.log("[Scheduler] Background data ingestion scheduler initialized.");
  
  // Run an initial sync immediately on startup
  checkAndSyncSources();
  
  // Tick every 1 minute to check for due data sources
  setInterval(checkAndSyncSources, 60 * 1000);
}

module.exports = {
  startScheduler,
  syncSource
};
