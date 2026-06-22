const express = require("express");
const router = express.Router();

const authController = require("../controllers/authController");

router.get("/ping", (req, res) => {
  res.status(200).json({ message: "Auth route works" });
});

router.post("/register", authController.register);
router.post("/login", authController.login);

module.exports = router;