const mongoose = require("mongoose");

async function connectMongo() {
  try {
    await mongoose.connect("mongodb://127.0.0.1:27017/envirowatchsi");
    console.log("Successfully connected to MongoDB");
  } catch (err) {
    console.error("Error while connecting to MongoDB:", err);
    process.exit(1);
  }
}

module.exports = connectMongo;