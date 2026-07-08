const supabase = require('../config/supabase');

const getAllCategories = async () => {
  const { data, error } = await supabase
    .from('categories')
    .select('*')
    .eq('is_active', true); // Hanya mengambil kategori yang aktif (soft delete)

  if (error) throw error;
  return data;
};

const createCategory = async (name, color_hex, icon_name) => {
  const { data, error } = await supabase
    .from('categories')
    .insert([{ name, color_hex, icon_name }])
    .select()
    .single();

  if (error) throw error;
  return data;
};

module.exports = {
  getAllCategories,
  createCategory
};