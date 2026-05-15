const express = require("express");
const router = express.Router();

const {
  getAllMeteo,
  createMeteo,
  updateMeteo,
  deleteMeteo,
} = require("../controllers/meteoController");

router.get("/", getAllMeteo);
router.post("/", createMeteo);
router.put("/:id", updateMeteo);
router.delete("/:id", deleteMeteo);

module.exports = router;