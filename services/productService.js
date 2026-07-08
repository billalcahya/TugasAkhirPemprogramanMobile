const supabase = require('../config/supabase');

const getAllProducts = async (categoryId, search) => {
  // Mengambil produk aktif beserta data kategori dan inventori terkait
  let query = supabase
    .from('products')
    .select('*, categories(name), inventory(current_stock)')
    .eq('is_active', true);

  if (categoryId) {
    query = query.eq('category_id', categoryId);
  }

  if (search) {
    query = query.ilike('name', `%${search}%`);
  }

  const { data, error } = await query;

  if (error) throw error;

  // Petakan hasil query agar field stock dan buy_price terisi dengan benar
  return data.map(prod => {
    let currentStock = 0;
    if (prod.inventory) {
      if (Array.isArray(prod.inventory)) {
        currentStock = prod.inventory[0]?.current_stock || 0;
      } else {
        currentStock = prod.inventory.current_stock || 0;
      }
    }

    return {
      id: prod.id,
      category_id: prod.category_id || 0,
      name: prod.name || '',
      buy_price: prod.cost_price || 0.0, // Map cost_price ke buy_price demi konsistensi FE
      cost_price: prod.cost_price || 0.0,
      sell_price: prod.sell_price || 0.0,
      image_url: prod.image_url || null,
      is_active: prod.is_active !== undefined && prod.is_active !== null ? prod.is_active : true,
      stock: currentStock || 0
    };
  });
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

const updateProductStock = async (productId, newStock) => {
  // 1. Update stok di tabel inventory
  const { error: updateError } = await supabase
    .from('inventory')
    .update({ 
      current_stock: newStock,
      updated_at: new Date()
    })
    .eq('product_id', productId);

  if (updateError) throw updateError;

  // 2. Ambil data produk terbaru beserta stok terupdate
  const { data: prod, error: fetchError } = await supabase
    .from('products')
    .select('*, categories(name), inventory(current_stock)')
    .eq('id', productId)
    .single();

  if (fetchError || !prod) throw new Error('Gagal mengambil data produk terupdate');

  let currentStock = 0;
  if (prod.inventory) {
    if (Array.isArray(prod.inventory)) {
      currentStock = prod.inventory[0]?.current_stock || 0;
    } else {
      currentStock = prod.inventory.current_stock || 0;
    }
  }

  return {
    id: prod.id,
    category_id: prod.category_id || 0,
    name: prod.name || '',
    buy_price: prod.cost_price || 0.0,
    cost_price: prod.cost_price || 0.0,
    sell_price: prod.sell_price || 0.0,
    image_url: prod.image_url || null,
    is_active: prod.is_active !== undefined && prod.is_active !== null ? prod.is_active : true,
    stock: currentStock || 0
  };
};

module.exports = {
  getAllProducts,
  getAllProductsFiltered: getAllProducts, // Alias untuk kompatibilitas audit
  createProduct,
  updateProductStock
};