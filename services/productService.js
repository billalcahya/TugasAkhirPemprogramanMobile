const supabase = require('../config/supabase');

const getAllProducts = async () => {
  // Mengambil produk aktif beserta data kategori terkait (NFR-09)
  const { data, error } = await supabase
    .from('products')
    .select('*, categories(name)')
    .eq('is_active', true);

  if (error) throw error;
  return data;
};

const createProduct = async (productData) => {
  const { category_id, name, sell_price, cost_price, image_url, initial_stock, min_stock, unit } = productData;

  // 1. Masukkan data ke tabel products
  const { data: product, error: productError } = await supabase
    .from('products')
    .insert([{ category_id, name, sell_price, cost_price, image_url }])
    .select()
    .single();

  if (productError) throw productError;

  // 2. Otomatis masukkan stok awal ke tabel inventory (FR-05 / Halaman 16)
  const { error: inventoryError } = await supabase
    .from('inventory')
    .insert([
      {
        product_id: product.id,
        current_stock: initial_stock || 0,
        min_stock: min_stock || 5,
        unit: unit || 'pcs'
      }
    ]);

  if (inventoryError) throw inventoryError;

  return product;
};

module.exports = {
  getAllProducts,
  createProduct
};