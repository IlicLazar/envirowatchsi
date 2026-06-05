exports.buildFilterQuery = (queryParams) => {
  const { startDate, endDate, lat, lng, radius } = queryParams;
  const query = {};

  if (startDate || endDate) {
    query.measuredAt = {};
    if (startDate) query.measuredAt.$gte = new Date(startDate);
    if (endDate) query.measuredAt.$lte = new Date(endDate);
  }

  if (lat && lng && radius) {
    query.location = {
      $near: {
        $geometry: {
          type: "Point",
          coordinates: [Number(lng), Number(lat)],
        },
        $maxDistance: Number(radius) * 1000,
      },
    };
  }

  return query;
};