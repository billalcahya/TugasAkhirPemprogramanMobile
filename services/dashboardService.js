const supabase = require('../config/supabase');

const getSummaryStats = async () => {
  // 1. Ambil semua transaksi yang sukses untuk menghitung omzet
  const { data: transactions, error: trxError } = await supabase
    .from('transactions')
    .select('grand_total, status')
    .eq('status', 'completed');

  if (trxError) throw trxError;

  const totalRevenue = transactions.reduce((sum, trx) => sum + trx.grand_total, 0);
  const totalTransactions = transactions.length;

  // 2. Ambil semua data inventori beserta nama produk terkait
  const { data: allInventory, error: invError } = await supabase
    .from('inventory')
    .select('*, products(name)');

  if (invError) throw invError;

  // 3. Filter produk yang stoknya menipis menggunakan logic Javascript (current_stock <= min_stock)
  const alertItems = allInventory?.filter(item => item.current_stock <= item.min_stock) || [];

  return {
    total_revenue: totalRevenue,
    total_transactions: totalTransactions,
    low_stock_alerts_count: alertItems.length,
    low_stock_products: alertItems.map(item => ({
      product_name: item.products?.name,
      current_stock: item.current_stock,
      min_stock: item.min_stock,
      unit: item.unit
    }))
  };
};

module.exports = {
  getSummaryStats
};