const reportService = require('../services/reportService');

const getDailyReport = async (req, res) => {
  try {
    const { date } = req.query; // format: YYYY-MM-DD
    if (!date) {
      return res.status(400).json({ status: "error", message: "Parameter tanggal (date) wajib diisi" });
    }

    const data = await reportService.getDailyReport(date);
    return res.status(200).json({
      status: "success",
      message: "Berhasil mengambil laporan harian",
      data
    });
  } catch (error) {
    return res.status(500).json({ status: "error", message: error.message });
  }
};

const getMonthlyReport = async (req, res) => {
  try {
    const { month } = req.query; // format: YYYY-MM
    if (!month) {
      return res.status(400).json({ status: "error", message: "Parameter bulan (month) wajib diisi" });
    }

    const data = await reportService.getMonthlyReport(month);
    return res.status(200).json({
      status: "success",
      message: "Berhasil mengambil laporan bulanan",
      data
    });
  } catch (error) {
    return res.status(500).json({ status: "error", message: error.message });
  }
};

module.exports = {
  getDailyReport,
  getMonthlyReport
};
