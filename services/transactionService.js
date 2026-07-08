const supabase = require('../config/supabase');
const generateTransactionCode = require('../utils/generateCode');

const checkout = async (userId, transactionData) => {
    const { items, discountAmount, paymentMethod, paymentAmount } = transactionData;
    const transactionCode = generateTransactionCode();

    // 1. Ambil data produk untuk menghitung total harga dan mencatat snapshot nama/harga modal
    const productIds = items.map(item => item.productId);
    const { data: products, error: productError } = await supabase
        .from('products')
        .select('*, inventory(current_stock)')
        .in('id', productIds);

    if (productError || !products) throw new Error('Gagal mengambil data produk terkait');

    let totalAmount = 0;
    const detailInserts = [];
    const inventoryUpdates = [];

    // 2. Validasi stok dan kalkulasi harga
    for (const item of items) {
        const product = products.find(p => p.id === item.productId);
        if (!product) throw new Error(`Produk dengan ID ${item.productId} tidak ditemukan`);

        // Validasi kecukupan stok sesuai FR-05 / R-08
        // const currentStock = product.inventory?.[0]?.current_stock || 0;

        let currentStock = 0;
        if (product.inventory) {
            if (Array.isArray(product.inventory)) {
                currentStock = product.inventory[0]?.current_stock || 0;
            } else {
                currentStock = product.inventory.current_stock || 0;
            }
        }

        if (currentStock < item.quantity) {
            throw new Error(`Stok ${product.name} tidak mencukupi (sisa: ${currentStock})`);
        }

        const subtotal = product.sell_price * item.quantity;
        totalAmount += subtotal;

        // Siapkan data untuk transaction_details (Snapshot data sesuai halaman 15)
        detailInserts.push({
            product_id: product.id,
            product_name: product.name,
            sell_price: product.sell_price,
            cost_price: product.cost_price,
            quantity: item.quantity,
            subtotal: subtotal
        });

        // Siapkan data untuk update stok inventori
        inventoryUpdates.push({
            product_id: product.id,
            new_stock: currentStock - item.quantity
        });
    }

    const grandTotal = totalAmount - (discountAmount || 0);
    const changeAmount = paymentAmount - grandTotal;

    if (changeAmount < 0) {
        throw new Error('Nominal pembayaran kurang');
    }

    // 3. Eksekusi ke Database Supabase
    // Simpan data transaksi utama
    const { data: transaction, error: trxError } = await supabase
        .from('transactions')
        .insert([{
            transaction_code: transactionCode,
            user_id: userId,
            total_amount: totalAmount,
            discount_amount: discountAmount || 0,
            grand_total: grandTotal,
            payment_method: paymentMethod,
            payment_amount: paymentAmount,
            change_amount: changeAmount,
            status: 'completed'
        }])
        .select()
        .single();

    if (trxError) throw trxError;

    // Hubungkan id transaksi utama ke detail item
    const finalDetails = detailInserts.map(detail => ({
        ...detail,
        transaction_id: transaction.id
    }));

    // Simpan semua detail item transaksi
    const { error: detailsError } = await supabase.from('transaction_details').insert(finalDetails);
    if (detailsError) throw detailsError;

    // Update stok satu per satu ke tabel inventory
    for (const update of inventoryUpdates) {
        const { error: stockError } = await supabase
            .from('inventory')
            .update({ current_stock: update.new_stock })
            .eq('product_id', update.product_id);

        if (stockError) throw stockError;
    }

    return {
        transactionId: transaction.id,
        transactionCode: transaction.transaction_code,
        grandTotal: transaction.grand_total,
        changeAmount: transaction.change_amount
    };
};

const getTransactionHistory = async () => {
    // Mengambil daftar semua transaksi (FR-08)
    const { data, error } = await supabase
        .from('transactions')
        .select('*, users(full_name)')
        .order('created_at', { ascending: false });

    if (error) throw error;
    return data;
};

const getTransactionDetail = async (id) => {
    // Mengambil rincian item dari satu transaksi spesifik (UC-07)
    const { data, error } = await supabase
        .from('transaction_details')
        .select('*')
        .eq('transaction_id', id);

    if (error) throw error;
    return data;
};

const voidTransaction = async (id, voidReason) => {
    // 1. Ambil detail item dari transaksi yang akan dibatalkan
    const { data: details, error: detailsError } = await supabase
        .from('transaction_details')
        .select('product_id, quantity')
        .eq('transaction_id', id);

    if (detailsError || !details) throw new Error('Data transaksi tidak ditemukan');

    // 2. Update status transaksi menjadi 'voided' (UC-13)
    const { data: transaction, error: trxError } = await supabase
        .from('transactions')
        .update({
            status: 'voided',
            void_reason: voidReason
        })
        .eq('id', id)
        .select()
        .single();

    if (trxError) throw trxError;

    // 3. Kembalikan stok produk ke tabel inventory karena transaksi dibatalkan
    for (const item of details) {
        // Ambil stok saat ini
        const { data: inv } = await supabase
            .from('inventory')
            .select('current_stock')
            .eq('product_id', item.product_id)
            .single();

        if (inv) {
            const restoredStock = inv.current_stock + item.quantity;
            await supabase
                .from('inventory')
                .update({ current_stock: restoredStock })
                .eq('product_id', item.product_id);
        }
    }

    return transaction;
};

// Jangan lupa daftarkan fungsi baru ini ke module.exports di bagian paling bawah file:
module.exports = {
    checkout, // fungsi lama kamu
    getTransactionHistory,
    getTransactionDetail,
    voidTransaction
};
