const cache = new Map();

const DEFAULT_TTL_MS = 55 * 60 * 1000;

function getCache(key) {
  const item = cache.get(key);

  if (!item) return null;

  if (Date.now() > item.expiresAt) {
    cache.delete(key);
    return null;
  }

  return item.data;
}

function setCache(key, data, ttlMs = DEFAULT_TTL_MS) {
  cache.set(key, {
    data,
    expiresAt: Date.now() + ttlMs,
  });
}

function clearCache(prefix = null) {
  if (!prefix) {
    cache.clear();
    return;
  }

  for (const key of cache.keys()) {
    if (key.startsWith(prefix)) {
      cache.delete(key);
    }
  }
}

function createCacheKey(prefix, query) {
  const sortedQuery = Object.keys(query)
    .sort()
    .map((key) => `${key}=${query[key]}`)
    .join("&");

  return `${prefix}:${sortedQuery || "all"}`;
}

module.exports = {
  getCache,
  setCache,
  clearCache,
  createCacheKey,
};