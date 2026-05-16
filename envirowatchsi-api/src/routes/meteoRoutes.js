const { authenticate, requireAdmin } = require("../middleware/authMiddleware");
const express = require("express");
const router = express.Router();

const {
  getAllMeteo,
  createMeteo,
  updateMeteo,
  getNearbyMeteo,
  deleteMeteo,
} = require("../controllers/meteoController");
console.log("Meteo routes loaded with /near");
/**
 * @swagger
 * /api/meteo:
 *   get:
 *     summary: Get all meteo records
 *     tags: [Meteo]
 *     responses:
 *       200:
 *         description: List of meteo records
 */
router.get("/near", getNearbyMeteo);
router.get("/", getAllMeteo);

/**
 * @swagger
 * /api/meteo:
 *   post:
 *     summary: Create a new meteo record
 *     tags: [Meteo]
 *     requestBody:
 *       required: true
 *     responses:
 *       201:
 *         description: Meteo record created
 */
router.post("/", authenticate, requireAdmin, createMeteo);

/**
 * @swagger
 * /api/meteo/{id}:
 *   put:
 *     summary: Update a meteo record
 *     tags: [Meteo]
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Meteo record updated
 */
router.put("/:id", authenticate, requireAdmin, updateMeteo);

/**
 * @swagger
 * /api/meteo/{id}:
 *   delete:
 *     summary: Delete a meteo record
 *     tags: [Meteo]
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Meteo record deleted
 */
router.delete("/:id", authenticate, requireAdmin, deleteMeteo);

module.exports = router;