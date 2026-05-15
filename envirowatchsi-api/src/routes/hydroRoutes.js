const express = require("express");
const router = express.Router();

const {
  getAllHydro,
  createHydro,
  updateHydro,
  deleteHydro,
} = require("../controllers/hydroController");

router.get("/", getAllHydro);
router.post("/", createHydro);
router.put("/:id", updateHydro);
router.delete("/:id", deleteHydro);

module.exports = router;