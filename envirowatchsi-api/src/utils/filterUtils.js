exports.buildFilterQuery = (queryParams) => {
  const { startDate, endDate, lat, lng, radius } = queryParams;
  const query = {};

  if (startDate || endDate) {
    query.createdAt = {};
    if (startDate) query.createdAt.$gte = new Date(startDate);
    if (endDate) query.createdAt.$lte = new Date(endDate);
  }

  if (lat && lng && radius) {
    query.location = {
      $near: {
        $geometry: {
          type: "Point",
          coordinates: [Number(lng), Number(lat)],
        },
        $maxDistance: Number(radius) * 1000, // convert km to meters
      },
    };
  }

  return query;
};
