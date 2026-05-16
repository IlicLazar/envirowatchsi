const express = require("express");
const router = express.Router();

const {
  getAllMeteo,
  createMeteo,
  updateMeteo,
  deleteMeteo,
} = require("../controllers/meteoController");

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
router.post("/", createMeteo);

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
router.put("/:id", updateMeteo);

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
router.delete("/:id", deleteMeteo);

module.exports = router;