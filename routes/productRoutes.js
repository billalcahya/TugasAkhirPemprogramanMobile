const express = require('express');
const router = express.Router();
const productController = require('../controllers/productController');
const authMiddleware = require('../middleware/authMiddleware');

router.get('/', authMiddleware, productController.getProducts);
router.post('/', authMiddleware, productController.addProduct);
router.put('/:id/stock', authMiddleware, productController.updateStock);

module.exports = router;