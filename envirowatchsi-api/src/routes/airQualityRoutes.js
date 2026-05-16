const { authenticate, requireAdmin } = require("../middleware/authMiddleware");
const express = require("express");
const router = express.Router();

const {
  getAllAirQuality,
  createAirQuality,
  updateAirQuality,
  deleteAirQuality,
} = require("../controllers/airQualityController");

/**
 * @swagger
 * /api/air-quality:
 *   get:
 *     summary: Get all air quality records
 *     tags: [Air Quality]
 *     responses:
 *       200:
 *         description: List of air quality records
 */
router.get("/", getAllAirQuality);

/**
 * @swagger
 * /api/air-quality:
 *   post:
 *     summary: Create a new air quality record
 *     tags: [Air Quality]
 *     requestBody:
 *       required: true
 *     responses:
 *       201:
 *         description: Air quality record created
 */
router.post(
  "/",
  authenticate,
  requireAdmin,
  createAirQuality
);

/**
 * @swagger
 * /api/air-quality/{id}:
 *   put:
 *     summary: Update an air quality record
 *     tags: [Air Quality]
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Air quality record updated
 */
router.put(
  "/:id",
  authenticate,
  requireAdmin,
  updateAirQuality
);

/**
 * @swagger
 * /api/air-quality/{id}:
 *   delete:
 *     summary: Delete an air quality record
 *     tags: [Air Quality]
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Air quality record deleted
 */
router.delete(
  "/:id",
  authenticate,
  requireAdmin,
  deleteAirQuality
);

module.exports = router;