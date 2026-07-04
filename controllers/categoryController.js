const categoryService = require('../services/categoryService');

const getCategories = async (req, res) => {
  try {
    const data = await categoryService.getAllCategories();
    return res.status(200).json({
      status: "success",
      message: "Berhasil mengambil daftar kategori",
      data
    });
  } catch (error) {
    console.error("DEBUG GET CATEGORIES ERROR:", error);
    return res.status(500).json({ status: "error", message: error.message });
  }
};

const addCategory = async (req, res) => {
  try {
    const { name, color_hex, icon_name } = req.body;
    if (!name) {
      return res.status(400).json({ status: "error", message: "Nama kategori wajib diisi" });
    }

    const data = await categoryService.createCategory(name, color_hex, icon_name);
    return res.status(201).json({
      status: "success",
      message: "Kategori berhasil ditambahkan",
      data
    });
  } catch (error) {
    return res.status(400).json({ status: "error", message: error.message });
  }
};

module.exports = {
  getCategories,
  addCategory
};