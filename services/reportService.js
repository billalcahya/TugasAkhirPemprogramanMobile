const supabase = require('../config/supabase');

const getDailyReport = async (dateStr) => {
  // dateStr format: YYYY-MM-DD
  const startOfDay = `${dateStr}T00:00:00.000Z`;
  const endOfDay = `${dateStr}T23:59:59.999Z`;

  // 1. Ambil semua transaksi sukses pada hari tersebut
  const { data: transactions, error: trxError } = await supabase
    .from('transactions')
    .select('id, grand_total, created_at')
    .eq('status', 'completed')
    .gte('created_at', startOfDay)
    .lte('created_at', endOfDay);

  if (trxError) throw trxError;

  const totalRevenue = transactions.reduce((sum, trx) => sum + trx.grand_total, 0);

  // 2. Ambil detail item dari transaksi tersebut untuk menghitung gross profit
  const trxIds = transactions.map(t => t.id);
  let grossProfit = 0;

  if (trxIds.length > 0) {
    const { data: details, error: detailsError } = await supabase
      .from('transaction_details')
      .select('sell_price, cost_price, quantity')
      .in('transaction_id', trxIds);

    if (detailsError) throw detailsError;

    if (details) {
      grossProfit = details.reduce((sum, item) => {
        const profitPerUnit = item.sell_price - (item.cost_price || 0);
        return sum + (profitPerUnit * item.quantity);
      }, 0);
    }
  }

  // 3. Kelompokkan pendapatan per jam untuk grafik tren
  const hourlyTrend = {};
  for (let h = 0; h < 24; h++) {
    const hourStr = String(h).padStart(2, '0') + ':00';
    hourlyTrend[hourStr] = 0;
  }

  transactions.forEach(t => {
    // Ambil jam dari created_at
    const localHour = new Date(t.created_at).getUTCHours();
    const hourStr = String(localHour).padStart(2, '0') + ':00';
    if (hourlyTrend[hourStr] !== undefined) {
      hourlyTrend[hourStr] += t.grand_total;
    }
  });

  const salesTrend = Object.keys(hourlyTrend).map(hour => ({
    label: hour,
    revenue: hourlyTrend[hour]
  })).sort((a, b) => a.label.localeCompare(b.label));

  return {
    total_revenue: totalRevenue,
    gross_profit: grossProfit,
    sales_trend: salesTrend,
    total_sales: totalRevenue,
    total_transactions: transactions.length,
    chart: salesTrend
  };
};

const getMonthlyReport = async (monthStr) => {
  // monthStr format: YYYY-MM
  const [year, month] = monthStr.split('-').map(Number);
  const startOfMonth = new Date(Date.UTC(year, month - 1, 1, 0, 0, 0, 0)).toISOString();
  const endOfMonth = new Date(Date.UTC(year, month, 0, 23, 59, 59, 999)).toISOString();

  // 1. Ambil semua transaksi sukses pada bulan tersebut
  const { data: transactions, error: trxError } = await supabase
    .from('transactions')
    .select('id, grand_total, created_at')
    .eq('status', 'completed')
    .gte('created_at', startOfMonth)
    .lte('created_at', endOfMonth);

  if (trxError) throw trxError;

  const totalRevenue = transactions.reduce((sum, trx) => sum + trx.grand_total, 0);

  // 2. Ambil detail item dari transaksi tersebut untuk menghitung gross profit
  const trxIds = transactions.map(t => t.id);
  let grossProfit = 0;

  if (trxIds.length > 0) {
    const { data: details, error: detailsError } = await supabase
      .from('transaction_details')
      .select('sell_price, cost_price, quantity')
      .in('transaction_id', trxIds);

    if (detailsError) throw detailsError;

    if (details) {
      grossProfit = details.reduce((sum, item) => {
        const profitPerUnit = item.sell_price - (item.cost_price || 0);
        return sum + (profitPerUnit * item.quantity);
      }, 0);
    }
  }

  // 3. Kelompokkan pendapatan per tanggal untuk grafik tren
  const daysInMonth = new Date(year, month, 0).getDate();
  const dailyTrend = {};
  for (let d = 1; d <= daysInMonth; d++) {
    const dateStr = `${monthStr}-${String(d).padStart(2, '0')}`;
    dailyTrend[dateStr] = 0;
  }

  transactions.forEach(t => {
    const dateStr = new Date(t.created_at).toISOString().split('T')[0];
    if (dailyTrend[dateStr] !== undefined) {
      dailyTrend[dateStr] += t.grand_total;
    }
  });

  const salesTrend = Object.keys(dailyTrend).map(date => ({
    label: date,
    revenue: dailyTrend[date]
  })).sort((a, b) => a.label.localeCompare(b.label));

  return {
    total_revenue: totalRevenue,
    gross_profit: grossProfit,
    sales_trend: salesTrend,
    total_sales: totalRevenue,
    total_transactions: transactions.length,
    chart: salesTrend
  };
};

module.exports = {
  getDailyReport,
  getMonthlyReport
};
