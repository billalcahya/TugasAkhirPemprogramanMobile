const productService = require('../services/productService');

const getProducts = async (req, res) => {
  try {
    const data = await productService.getAllProducts();
    return res.status(200).json({
      status: "success",
      message: "Berhasil mengambil daftar produk",
      data
    });
  } catch (error) {
    return res.status(500).json({ status: "error", message: error.message });
  }
};

const addProduct = async (req, res) => {
  try {
    const { category_id, name, sell_price, cost_price, image_url, initial_stock, min_stock, unit } = req.body;

    // Validasi input wajib sesuai skema halaman 14
    if (!category_id || !name || !sell_price) {
      return res.status(400).json({ 
        status: "error", 
        message: "Kategori, nama produk, dan harga jual wajib diisi" 
      });
    }

    const product = await productService.createProduct(req.body);

    return res.status(201).json({
      status: "success",
      message: "Produk dan stok awal berhasil ditambahkan",
      data: product
    });
  } catch (error) {
    return res.status(400).json({ status: "error", message: error.message });
  }
};

module.exports = {
  getProducts,
  addProduct
};