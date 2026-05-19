const DataSource = require("../models/DataSource");
const { syncSource } = require("../services/schedulerService");

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

exports.updateDataSource = async (req, res) => {
  try {
    const updatedDataSource = await DataSource.findByIdAndUpdate(
      req.params.id,
      req.body,
      {
        new: true,
        runValidators: true,
      }
    );

    if (!updatedDataSource) {
      return res.status(404).json({
        message: "Data source not found",
      });
    }

    res.status(200).json(updatedDataSource);
  } catch (error) {
    res.status(500).json({
      message: "Failed to update data source",
      error: error.message,
    });
  }
};

exports.deleteDataSource = async (req, res) => {
  try {
    const deletedDataSource = await DataSource.findByIdAndDelete(req.params.id);

    if (!deletedDataSource) {
      return res.status(404).json({
        message: "Data source not found",
      });
    }

    res.status(200).json({
      message: "Data source deleted successfully",
      data: deletedDataSource,
    });
  } catch (error) {
    res.status(500).json({
      message: "Failed to delete data source",
      error: error.message,
    });
  }
};

exports.syncDataSource = async (req, res) => {
  try {
    const source = await DataSource.findById(req.params.id);
    if (!source) {
      return res.status(404).json({
        message: "Data source not found"
      });
    }

    const result = await syncSource(source);
    if (result.success) {
      res.status(200).json({
        message: "Data source synchronized successfully",
        newRecordsCount: result.newRecordsCount,
      });
    } else {
      res.status(500).json({
        message: "Failed to synchronize data source",
        error: result.error,
      });
    }
  } catch (error) {
    res.status(500).json({
      message: "Failed to trigger sync",
      error: error.message,
    });
  }
};