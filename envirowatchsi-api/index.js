const express = require('express');
const cors = require("cors");
const swaggerUi = require("swagger-ui-express");
const swaggerJsDoc = require("swagger-jsdoc");
const mongoose = require("mongoose");

const app = express();

app.use(express.json());

mongoose.connect("mongodb://127.0.0.1:27017/enrirowatchsi").then(() => {
    console.log("Successfully connected to db :D");
}).catch((err) => {
    console.log("Error while connecting to db: ", err)
})

app.use("/auth", require("./src/routes/auth"))

app.get('/', (req, res) => {
  res.status(200).json({message: "Test"});
})

app.listen(3000, () => {
  console.log(`Example app listening on port ${3000}`)
})