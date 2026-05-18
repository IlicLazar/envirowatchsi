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

exports.createDataSource = async (req, res) => {
  try {
    const {
      name,
      type,
      url,
      isActive,
      refreshIntervalMinutes,
    } = req.body;

    const newDataSource = new DataSource({
      name,
      type,
      url,
      isActive,
      refreshIntervalMinutes,
    });

    const savedDataSource = await newDataSource.save();

    res.status(201).json(savedDataSource);
  } catch (error) {
    res.status(500).json({
      message: "Failed to create data source",
      error: error.message,
    });
  }
};