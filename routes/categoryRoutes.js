const express = require('express');
const router = express.Router();
const categoryController = require('../controllers/categoryController');
const authMiddleware = require('../middleware/authMiddleware');

// Semua endpoint kategori wajib menyertakan Bearer Token JWT
router.get('/', authMiddleware, categoryController.getCategories);
router.post('/', authMiddleware, categoryController.addCategory);

module.exports = router;