const app = require("./src/app");
const connectMongo = require("./src/db/mongo");

const swaggerUi = require("swagger-ui-express");
const swaggerJsDoc = require("swagger-jsdoc");

const PORT = 3000;

const swaggerOptions = {
  definition: {
    openapi: "3.0.0",
    info: {
      title: "EnviroWatchSI API",
      version: "1.0.0",
      description: "API documentation for EnviroWatchSI",
    },
    servers: [
      {
        url: "http://localhost:3000",
      },
    ],
  },
  apis: ["./src/routes/*.js"],
};

const swaggerSpec = swaggerJsDoc(swaggerOptions);

connectMongo().then(() => {

  app.use(
    "/docs",
    swaggerUi.serve,
    swaggerUi.setup(swaggerSpec)
  );

  app.listen(PORT, () => {
    console.log(`EnviroWatchSI API running on port ${PORT}`);
  });

});