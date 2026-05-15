const express = require("express");
const router = express.Router();

const {
  getAllAirQuality,
  createAirQuality,
  updateAirQuality,
  deleteAirQuality,
} = require("../controllers/airQualityController");

router.get("/", getAllAirQuality);
router.post("/", createAirQuality);
router.put("/:id", updateAirQuality);
router.delete("/:id", deleteAirQuality);

module.exports = router;