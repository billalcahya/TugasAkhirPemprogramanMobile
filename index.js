const express = require('express');
const app = express();
const PORT = 3000;

// Middleware untuk membaca JSON
app.use(express.json());

// Route Utama (Root)
app.get('/', (req, res) => {
    res.send('Halo! Express.js berhasil dijalankan.');
});

// Jalankan Server
app.listen(PORT, () => {
    console.log(`Server berjalan di http://localhost:${PORT}`);
});