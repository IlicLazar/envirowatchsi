const { DOMParser } = require("@xmldom/xmldom");

function parseDate(dateStr) {
  if (!dateStr) return new Date();

  let d = new Date(dateStr);
  if (!isNaN(d.getTime())) return d;

  const slRegex = /(\d{1,2})\.(\d{1,2})\.(\d{4})(?:\s+(\d{1,2}):(\d{1,2})(?::(\d{1,2}))?)?/;
  const match = dateStr.match(slRegex);
  if (match) {
    const day = parseInt(match[1], 10);
    const month = parseInt(match[2], 10) - 1;
    const year = parseInt(match[3], 10);
    const hour = parseInt(match[4] || "0", 10);
    const minute = parseInt(match[5] || "0", 10);
    const second = parseInt(match[6] || "0", 10);
    return new Date(year, month, day, hour, minute, second);
  }

  return new Date();
}

function parseAirQualityData(xmlText) {
  const doc = new DOMParser().parseFromString(xmlText, "text/xml");
  const nodeList = doc.getElementsByTagName("postaja");
  const stations = [];

  for (let i = 0; i < nodeList.length; i++) {
    const el = nodeList.item(i);

    const getTagText = (tagName) => {
      const node = el.getElementsByTagName(tagName)[0];
      return node ? node.textContent.trim() : "";
    };

    const textOrNull = (tagName) => {
      const value = getTagText(tagName);
      if (!value) return null;
      const num = parseFloat(value);
      if (isNaN(num)) return null;
      return num < 1.0 ? null : num;
    };

    const stationId = el.getAttribute("sifra");
    const stationName = getTagText("merilno_mesto");
    if (!stationName) continue;

    const lat = parseFloat(el.getAttribute("wgs84_sirina")) || 0.0;
    const lon = parseFloat(el.getAttribute("wgs84_dolzina")) || 0.0;
    const measuredAtStr = getTagText("datum_od");
    const measuredAt = parseDate(measuredAtStr);

    const pm10 = textOrNull("pm10");
    const pm2_5 = textOrNull("pm2.5");
    const o3 = textOrNull("o3");
    const co = textOrNull("co");
    const so2 = textOrNull("so2");

    const pollutants = [pm10, pm2_5, o3, co, so2].filter(v => v !== null);
    const aqi = pollutants.length > 0 ? Math.max(...pollutants) : null;

    stations.push({
      stationId,
      stationName,
      latitude: lat,
      longitude: lon,
      location: {
        type: "Point",
        coordinates: [lon, lat]
      },
      measuredAt,
      pm10,
      pm2_5,
      o3,
      co,
      so2,
      airQualityIndex: aqi
    });
  }

  return stations;
}

function parseMeteoData(xmlText) {
  const doc = new DOMParser().parseFromString(xmlText, "text/xml");
  const nodeList = doc.getElementsByTagName("metData");
  const stations = [];

  for (let i = 0; i < nodeList.length; i++) {
    const el = nodeList.item(i);

    const getTagText = (tagName) => {
      const node = el.getElementsByTagName(tagName)[0];
      return node ? node.textContent.trim() : "";
    };

    const stationId = getTagText("domain_meteosiId");
    const stationName = getTagText("domain_shortTitle");
    if (!stationName) continue;

    const lat = parseFloat(getTagText("domain_lat"));
    const lon = parseFloat(getTagText("domain_lon"));
    const measuredAtStr = getTagText("tsValid_issued");
    const measuredAt = parseDate(measuredAtStr);

    const tempVal = getTagText("t");
    const humVal = getTagText("rh");

    if (!tempVal || !humVal) continue;

    const temperature = parseFloat(tempVal);
    const humidity = parseFloat(humVal);

    if (isNaN(temperature) || isNaN(humidity)) continue;

    const windSpeed = parseFloat(getTagText("ff_val"));
    const windDirection = getTagText("dd_shortText");
    const precipitation = parseFloat(getTagText("tp_acc"));

    stations.push({
      stationId,
      stationName,
      latitude: isNaN(lat) ? 0.0 : lat,
      longitude: isNaN(lon) ? 0.0 : lon,
      location: {
        type: "Point",
        coordinates: [isNaN(lon) ? 0.0 : lon, isNaN(lat) ? 0.0 : lat]
      },
      measuredAt,
      temperature,
      humidity,
      windSpeed: isNaN(windSpeed) ? null : windSpeed,
      windDirection: windDirection || null,
      precipitation: isNaN(precipitation) ? null : precipitation
    });
  }

  return stations;
}

function parseHydroData(xmlText) {
  const doc = new DOMParser().parseFromString(xmlText, "text/xml");
  const nodeList = doc.getElementsByTagName("postaja");
  const stations = [];

  for (let i = 0; i < nodeList.length; i++) {
    const el = nodeList.item(i);

    const getTagText = (tagName) => {
      const node = el.getElementsByTagName(tagName)[0];
      return node ? node.textContent.trim() : "";
    };

    const stationId = el.getAttribute("sifra");
    const stationName = getTagText("merilno_mesto");
    if (!stationName) continue;

    const lat = parseFloat(el.getAttribute("wgs84_sirina")) || 0.0;
    const lon = parseFloat(el.getAttribute("wgs84_dolzina")) || 0.0;

    const datum = getTagText("datum");
    const ura = getTagText("ura");
    const measuredAtStr = datum && ura ? `${datum} ${ura}` : "";
    const measuredAt = parseDate(measuredAtStr);

    const riverName = getTagText("reka") || "Neznano";
    const waterLevel = parseFloat(getTagText("vodostaj"));
    const waterFlow = parseFloat(getTagText("pretok"));

    stations.push({
      stationId,
      stationName,
      latitude: lat,
      longitude: lon,
      location: {
        type: "Point",
        coordinates: [lon, lat]
      },
      riverName,
      measuredAt,
      waterLevel: isNaN(waterLevel) ? null : waterLevel,
      waterFlow: isNaN(waterFlow) ? null : waterFlow
    });
  }

  return stations;
}

module.exports = {
  parseDate,
  parseAirQualityData,
  parseMeteoData,
  parseHydroData
};
