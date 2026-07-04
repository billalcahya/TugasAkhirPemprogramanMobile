const express = require('express');
const router = express.Router();
const authRoutes = require('./authRoutes');
const categoryRoutes = require('./categoryRoutes');
const productRoutes = require('./productRoutes');
const inventoryRoutes = require('./inventoryRoutes');
const transactionRoutes = require('./transactionRoutes');
const dashboardRoutes = require('./dashboardRoutes');

// Gabungkan endpoint auth (Base URL menjadi /api/v1/auth)
router.use('/auth', authRoutes);
router.use('/categories', categoryRoutes);
router.use('/products', productRoutes);
router.use('/inventory', inventoryRoutes);
router.use('/transactions', transactionRoutes);
router.use('/dashboard', dashboardRoutes);

module.exports = router;
