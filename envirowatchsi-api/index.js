const app = require("./src/app");
const connectMongo = require("./src/db/mongo");

const PORT = 3000;

connectMongo().then(() => {
  app.listen(PORT, () => {
    console.log(`EnviroWatchSI API running on port ${PORT}`);
  });
});