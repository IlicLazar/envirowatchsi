const { buildFilterQuery } = require("../src/utils/filterUtils");

describe("buildFilterQuery", () => {
  test("creates measuredAt range filter when startDate and endDate are provided", () => {
    const query = buildFilterQuery({
      startDate: "2026-06-01T00:00:00.000Z",
      endDate: "2026-06-19T23:59:59.000Z",
    });

    expect(query).toEqual({
      measuredAt: {
        $gte: new Date("2026-06-01T00:00:00.000Z"),
        $lte: new Date("2026-06-19T23:59:59.000Z"),
      },
    });
  });

  test("creates location near filter with coordinates and radius in meters", () => {
    const query = buildFilterQuery({
      lat: "46.0569",
      lng: "14.5058",
      radius: "5",
    });

    expect(query).toEqual({
      location: {
        $near: {
          $geometry: {
            type: "Point",
            coordinates: [14.5058, 46.0569],
          },
          $maxDistance: 5000,
        },
      },
    });
  });

  test("does not create partial location filter when radius is missing", () => {
    const query = buildFilterQuery({
      lat: "46.0569",
      lng: "14.5058",
    });

    expect(query).toEqual({});
  });
});
