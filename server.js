require('dotenv').config({ path: './config/.env' });
const express = require('express');
const cors = require('cors');
const morgan = require('morgan');
const app = express();
const routes = require('./routes/index'); // Import router gabungan

// Middleware
app.use(cors());
app.use(morgan('dev'));
app.use(express.json());

// Gunakan base URL sesuai spesifikasi API di halaman 16
app.use('/api/v1', routes);

// Global Error Handler
app.use((err, req, res, next) => {
  console.error("GLOBAL ERROR HANDLER:", err);
  res.status(err.status || 500).json({
    status: "error",
    message: err.message || "Terjadi kesalahan internal server"
  });
});

const supabase = require('./config/supabase');
const bcrypt = require('bcrypt');

// Route sementara untuk mendaftarkan user baru dengan hash yang valid
// app.post('/api/v1/auth/register-debug', async (req, res) => {
//   try {
//     const { email, password, full_name, role } = req.body;
    
//     // Hash password secara otomatis menggunakan bcrypt (salt 10)
//     const hashedPassword = await bcrypt.hash(password, 10);
    
//     // Simpan ke Supabase
//     const { data, error } = await supabase
//       .from('users')
//       .insert([
//         { 
//           email: email, 
//           password_hash: hashedPassword, 
//           full_name: full_name, 
//           role: role || 'admin' 
//         }
//       ])
//       .select();

//     if (error) throw error;

//     res.json({ status: "success", message: "User debug berhasil dibuat!", data });
//   } catch (err) {
//     res.status(500).json({ status: "error", message: err.message });
//   }
// });


const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server berjalan di http://localhost:${PORT}`);
});