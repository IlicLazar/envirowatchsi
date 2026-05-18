const DataSource = require("../models/DataSource");

exports.getAllDataSources = async (req, res) => {
  try {
    const dataSources = await DataSource.find().sort({ createdAt: -1 });

    res.status(200).json(dataSources);
  } catch (error) {
    res.status(500).json({
      message: "Failed to fetch data sources",
      error: error.message,
    });
  }
};