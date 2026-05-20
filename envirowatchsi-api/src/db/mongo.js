const mongoose = require("mongoose");

async function connectMongo() {
  try {
    await mongoose.connect(process.env.MONGO_URI);
    console.log("Successfully connected to MongoDB");
  } catch (err) {
    console.error("Error while connecting to MongoDB:", err);
    process.exit(1);
  }
}

module.exports = connectMongo;