const { authenticate, requireAdmin } = require("../middleware/authMiddleware");
const express = require("express");
const router = express.Router();

const {
  getAllHydro,
  createHydro,
  updateHydro,
  getNearbyHydro,
  deleteHydro,
} = require("../controllers/hydroController");

console.log("Hydro routes loaded with /near")
/**
 * @swagger
 * /api/hydro:
 *   get:
 *     summary: Get all hydro records
 *     tags: [Hydro]
 *     responses:
 *       200:
 *         description: List of hydro records
 */
router.get("/near", getNearbyHydro);
router.get("/", getAllHydro);

/**
 * @swagger
 * /api/hydro:
 *   post:
 *     summary: Create a new hydro record
 *     tags: [Hydro]
 *     requestBody:
 *       required: true
 *     responses:
 *       201:
 *         description: Hydro record created
 */
router.post(
  "/",
  authenticate,
  requireAdmin,
  createHydro
);

/**
 * @swagger
 * /api/hydro/{id}:
 *   put:
 *     summary: Update a hydro record
 *     tags: [Hydro]
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Hydro record updated
 */
router.put(
  "/:id",
  authenticate,
  requireAdmin,
  updateHydro
);

/**
 * @swagger
 * /api/hydro/{id}:
 *   delete:
 *     summary: Delete a hydro record
 *     tags: [Hydro]
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Hydro record deleted
 */
router.delete(
  "/:id",
  authenticate,
  requireAdmin,
  deleteHydro
);

module.exports = router;