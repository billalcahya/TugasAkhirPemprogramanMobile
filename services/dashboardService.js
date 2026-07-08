const supabase = require('../config/supabase');

const getSummaryStats = async () => {
  const todayStart = new Date();
  todayStart.setHours(0, 0, 0, 0);
  const todayEnd = new Date();
  todayEnd.setHours(23, 59, 59, 999);

  // 1. Ambil transaksi hari ini
  const { data: todayTransactions, error: todayTrxError } = await supabase
    .from('transactions')
    .select('grand_total, status')
    .eq('status', 'completed')
    .gte('created_at', todayStart.toISOString())
    .lte('created_at', todayEnd.toISOString());

  if (todayTrxError) throw todayTrxError;

  const totalRevenueToday = todayTransactions.reduce((sum, trx) => sum + trx.grand_total, 0);
  const totalTransactionsToday = todayTransactions.length;

  // 2. Ambil semua transaksi sukses untuk total_sales kumulatif
  const { data: allTransactions, error: trxError } = await supabase
    .from('transactions')
    .select('grand_total, status')
    .eq('status', 'completed');

  if (trxError) throw trxError;

  const totalSales = allTransactions.reduce((sum, trx) => sum + trx.grand_total, 0);
  const totalTransactions = allTransactions.length;

  // 3. Ambil data transaksi 7 hari terakhir untuk grafik tren
  const sevenDaysAgo = new Date();
  sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 6);
  sevenDaysAgo.setHours(0, 0, 0, 0);

  const { data: recentTransactions, error: recentError } = await supabase
    .from('transactions')
    .select('grand_total, created_at')
    .eq('status', 'completed')
    .gte('created_at', sevenDaysAgo.toISOString());

  if (recentError) throw recentError;

  const salesMap = {};
  for (let i = 0; i < 7; i++) {
    const d = new Date();
    d.setDate(d.getDate() - i);
    const dateKey = d.toISOString().split('T')[0];
    salesMap[dateKey] = 0;
  }

  if (recentTransactions) {
    recentTransactions.forEach(trx => {
      // Pastikan timezone disesuaikan ke format date YYYY-MM-DD
      const dateKey = new Date(trx.created_at).toISOString().split('T')[0];
      if (salesMap[dateKey] !== undefined) {
        salesMap[dateKey] += trx.grand_total;
      }
    });
  }

  const last_7_days_sales = Object.keys(salesMap).map(date => ({
    date,
    total: salesMap[date]
  })).sort((a, b) => a.date.localeCompare(b.date));

  // 4. Hitung Top Products (Agregasi dari transaction_details)
  const { data: details, error: detailsError } = await supabase
    .from('transaction_details')
    .select('product_name, quantity');

  if (detailsError) throw detailsError;

  const productSales = {};
  if (details) {
    details.forEach(d => {
      productSales[d.product_name] = (productSales[d.product_name] || 0) + d.quantity;
    });
  }
  const top_products = Object.keys(productSales).map(name => ({
    product_name: name,
    quantity: productSales[name]
  })).sort((a, b) => b.quantity - a.quantity).slice(0, 5);

  // 5. Ambil data inventori untuk low stock alerts
  const { data: allInventory, error: invError } = await supabase
    .from('inventory')
    .select('*, products(name)');

  if (invError) throw invError;

  const alertItems = allInventory?.filter(item => item.current_stock <= item.min_stock) || [];
  const low_stock_alerts = alertItems.map(item => ({
    product_name: item.products?.name || 'Unknown Product',
    current_stock: item.current_stock
  }));

  return {
    total_revenue_today: totalRevenueToday,
    total_sales: totalSales,
    total_transactions_today: totalTransactionsToday,
    total_transactions: totalTransactions,
    top_products,
    last_7_days_sales,
    low_stock_alerts
  };
};

module.exports = {
  getSummaryStats
};