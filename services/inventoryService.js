const supabase = require('../config/supabase');

const getAllInventory = async () => {
  // Mengambil semua data inventori dan memuat nama produknya (FR-05)
  const { data, error } = await supabase
    .from('inventory')
    .select('*, products(name)');

  if (error) throw error;
  return data;
};

const updateStockManual = async (productId, current_stock) => {
  // Update stok berdasarkan product_id (FR-05)
  const { data, error } = await supabase
    .from('inventory')
    .update({ 
      current_stock,
      updated_at: new Date()
    })
    .eq('product_id', productId)
    .select()
    .single();

  if (error) throw error;
  return data;
};

module.exports = {
  getAllInventory,
  updateStockManual
};