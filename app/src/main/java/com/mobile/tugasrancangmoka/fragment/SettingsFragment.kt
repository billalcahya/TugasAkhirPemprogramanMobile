package com.mobile.tugasrancangmoka.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobile.tugasrancangmoka.R
import com.mobile.tugasrancangmoka.activity.LoginActivity
import com.mobile.tugasrancangmoka.api.ApiClient
import com.mobile.tugasrancangmoka.databinding.DialogAddCategoryBinding
import com.mobile.tugasrancangmoka.databinding.DialogAddProductBinding
import com.mobile.tugasrancangmoka.databinding.FragmentSettingsBinding
import com.mobile.tugasrancangmoka.model.Product
import com.mobile.tugasrancangmoka.repository.CategoryRepo
import com.mobile.tugasrancangmoka.utils.SessionManager
import com.mobile.tugasrancangmoka.viewmodel.SettingsVM
import com.mobile.tugasrancangmoka.viewmodel.UnitModel
import com.mobile.tugasrancangmoka.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SettingsVM
    private var imageUri: Uri? = null
    private var currentDialogImageView: ImageView? = null

    private val getImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                currentDialogImageView?.setImageURI(it)
            }
        }

    private class UserAdapter(
        private val userList: List<com.mobile.tugasrancangmoka.model.User>,
        private val currentUserRole: String?, // Tambahkan parameter role user yang sedang login
        private val onEditClick: (com.mobile.tugasrancangmoka.model.User) -> Unit
    ) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvAvatar: TextView = view.findViewById(R.id.tv_avatar)
            val tvName: TextView = view.findViewById(R.id.tv_user_name)
            val tvRole: TextView = view.findViewById(R.id.tv_user_role)
            val btnAction: android.widget.ImageButton = view.findViewById(R.id.btn_action_user)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_user, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val user = userList[position]
            holder.tvName.text = user.fullName
            holder.tvRole.text = user.role.uppercase()
            holder.tvAvatar.text = user.initial

            // Kontrol visibilitas tombol edit per baris item user
            if (currentUserRole?.equals("admin", ignoreCase = true) == true) {
                holder.btnAction.visibility = View.VISIBLE
                holder.btnAction.setOnClickListener { onEditClick(user) }
            } else {
                holder.btnAction.visibility = View.GONE
            }
        }

        override fun getItemCount(): Int = userList.size
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        val apiService = ApiClient.getApiService(requireContext())
        val repository = CategoryRepo(apiService)
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[SettingsVM::class.java]

        val tvUsername = view.findViewById<TextView>(R.id.tv_username)
        val tvUserRole = view.findViewById<TextView>(R.id.tv_user_role)

        val sessionManager = SessionManager(requireContext())

        tvUsername.text = sessionManager.getName() ?: "Guest"
        tvUserRole.text = (sessionManager.getRole() ?: "No Role").uppercase(java.util.Locale.ROOT)

        setupActionListeners()
        setupObservers()

        val prefs = requireContext().getSharedPreferences("smartcafe_prefs", Context.MODE_PRIVATE)
        val userRole = prefs.getString("user_role", "cashier")
        if (userRole?.equals("admin", ignoreCase = true) == true) {
            // JIKA ADMIN: Tampilkan SEMUA tombol tanpa ada yang di-hide
            binding.btnGridAddCategory.visibility = View.VISIBLE
            binding.btnGridManageCategory.visibility = View.VISIBLE
            binding.btnGridAddProduct.visibility = View.VISIBLE
            binding.btnGridManageProduct.visibility = View.VISIBLE
            binding.btnAddUser.visibility = View.VISIBLE
        } else {
            // JIKA BUKAN ADMIN (KASIR): Sembunyikan semua tombol ADD, tombol informasi items tetap terlihat
            binding.btnGridAddCategory.visibility = View.GONE
            binding.btnGridManageCategory.visibility = View.VISIBLE
            binding.btnGridAddProduct.visibility = View.GONE
            binding.btnGridManageProduct.visibility = View.VISIBLE
            binding.btnAddUser.visibility = View.GONE
        }

        binding.switchAutoPrint.isChecked = prefs.getBoolean("auto_print", true)
        binding.switchAutoPrint.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("auto_print", isChecked).apply()
            val statusText = if (isChecked) "diaktifkan" else "dinonaktifkan"
            Toast.makeText(requireContext(), "Auto-print $statusText", Toast.LENGTH_SHORT).show()
        }

        binding.btnTestPrint.setOnClickListener {
            performTestPrint()
        }

        binding.btnLogout.setOnClickListener {
            val session = SessionManager(requireContext())
            session.clearSession()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        binding.rvUsers.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
//        val userAdapter = UserAdapter(emptyList()) { clickedUser ->
//            // Aksi jika ikon edit/hapus user diklik, contoh:
//            // showEditDeleteUserOptions(clickedUser)
//        }
        viewModel.users.observe(viewLifecycleOwner) { listUser ->
            if (!listUser.isNullOrEmpty()) {
                // Kirim userRole ke adapter
                binding.rvUsers.adapter = UserAdapter(listUser, userRole) { clickedUser ->
                    AlertDialog.Builder(requireContext())
                        .setTitle("Opsi User: ${clickedUser.fullName}")
                        .setItems(arrayOf("Edit Informasi", "Hapus User")) { _, which ->
                            when (which) {
                                0 -> showEditUserDialog(clickedUser)
                                1 -> {
                                    AlertDialog.Builder(requireContext())
                                        .setTitle("Hapus User")
                                        .setMessage("Apakah Anda yakin ingin menghapus user '${clickedUser.fullName}'?")
                                        .setPositiveButton("Ya") { _, _ ->
                                            viewModel.deleteUser(clickedUser.id)
                                        }
                                        .setNegativeButton("Batal", null)
                                        .show()
                                }
                            }
                        }.show()
                }
            }
        }

        viewModel.fetchCategoriesCount()
        viewModel.fetchUnitsCount()
        viewModel.fetchProducts(null, null)
        viewModel.fetchUsers()
    }

    private fun setupActionListeners() {
        binding.btnGridAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }
        binding.btnGridManageCategory.setOnClickListener {
            showManageCategoriesDialog()
        }
//        binding.btnGridAddUnit.setOnClickListener {
//            showUnitDialog()
//        }
        binding.btnGridAddProduct.setOnClickListener {
            showProductDialog()
        }
        binding.btnGridManageProduct.setOnClickListener {
            showManageProductsDialog()
        }
        binding.btnAddUser.setOnClickListener {
            showAddUserDialog()
        }
        binding.btnAddUser.setOnClickListener {
            showAddUserDialog()
        }
    }

    private fun showAddCategoryDialog() {
        val dialogBinding = DialogAddCategoryBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext()).setView(dialogBinding.root)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // READ: Menampilkan List data ke ListView
        viewModel.categories.observe(viewLifecycleOwner) { listCategory ->
            val namesList = listCategory.map { it.name }
            dialogBinding.rvExistingCategories.adapter = android.widget.ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                namesList
            )

            // UPDATE & DELETE: Ketika salah satu item kategori di dalam list diklik
            dialogBinding.rvExistingCategories.setOnItemClickListener { _, _, position, _ ->
                val selectedCategory = listCategory[position]

                // Tampilkan opsi Edit atau Hapus
                AlertDialog.Builder(requireContext())
                    .setTitle("Opsi Kategori: ${selectedCategory.name}")
                    .setItems(arrayOf("Edit Nama", "Hapus Kategori")) { _, which ->
                        when (which) {
                            0 -> { // Memilih Edit Nama
                                dialogBinding.etCategoryName.setText(selectedCategory.name)
                                dialogBinding.btnSave.text = "Update"

                                dialogBinding.btnSave.setOnClickListener {
                                    val newName =
                                        dialogBinding.etCategoryName.text.toString().trim()
                                    if (newName.isNotEmpty()) {
                                        viewModel.updateCategory(selectedCategory.id, newName)
                                        alertDialog.dismiss()
                                    }
                                }
                            }

                            1 -> { // Memilih Hapus Kategori
                                AlertDialog.Builder(requireContext())
                                    .setTitle("Hapus Kategori")
                                    .setMessage("Apakah Anda yakin ingin menghapus kategori '${selectedCategory.name}'?")
                                    .setPositiveButton("Ya") { _, _ ->
                                        viewModel.deleteCategory(selectedCategory.id)
                                        alertDialog.dismiss()
                                    }
                                    .setNegativeButton("Batal", null)
                                    .show()
                            }
                        }
                    }.show()
            }
        }

        // CREATE: Aksi default tombol simpan untuk menambah kategori baru
        dialogBinding.btnSave.setOnClickListener {
            val categoryName = dialogBinding.etCategoryName.text.toString().trim()
            if (categoryName.isNotEmpty()) {
                viewModel.addCategory(categoryName)
                alertDialog.dismiss()
            } else {
                dialogBinding.tilCategoryName.error = "Nama kategori wajib diisi"
            }
        }

        dialogBinding.btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun showManageCategoriesDialog() {
        val dialogBinding = DialogAddCategoryBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext()).setView(dialogBinding.root)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Customise text
        val rootLayout = dialogBinding.root.getChildAt(0) as LinearLayout
        val titleText = rootLayout.getChildAt(0) as TextView
        titleText.text = "Kelola Kategori"
        val subtitleText = rootLayout.getChildAt(1) as TextView
        subtitleText.text = "Pilih kategori untuk diubah atau dihapus:"

        // Sembunyikan field input dan tombol save karena ini hanya untuk list/manage
        dialogBinding.tilCategoryName.visibility = View.GONE
        dialogBinding.btnSave.visibility = View.GONE

        // READ: Menampilkan List data ke ListView
        viewModel.categories.observe(viewLifecycleOwner) { listCategory ->
            val namesList = listCategory.map { it.name }
            dialogBinding.rvExistingCategories.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                namesList
            )

            // UPDATE & DELETE: Ketika salah satu item kategori di dalam list diklik
            dialogBinding.rvExistingCategories.setOnItemClickListener { _, _, position, _ ->
                val selectedCategory = listCategory[position]

                // Tampilkan opsi Edit atau Hapus
                AlertDialog.Builder(requireContext())
                    .setTitle("Opsi Kategori: ${selectedCategory.name}")
                    .setItems(arrayOf("Edit Nama", "Hapus Kategori")) { _, which ->
                        when (which) {
                            0 -> { // Memilih Edit Nama
                                val input = android.widget.EditText(requireContext())
                                input.setText(selectedCategory.name)
                                input.setSelection(selectedCategory.name.length)

                                val paddingPx = (24 * resources.displayMetrics.density).toInt()
                                val container = android.widget.FrameLayout(requireContext())
                                container.setPadding(
                                    paddingPx,
                                    paddingPx / 2,
                                    paddingPx,
                                    paddingPx / 2
                                )
                                container.addView(input)

                                AlertDialog.Builder(requireContext())
                                    .setTitle("Edit Nama Kategori")
                                    .setView(container)
                                    .setPositiveButton("Update") { _, _ ->
                                        val newName = input.text.toString().trim()
                                        if (newName.isNotEmpty()) {
                                            viewModel.updateCategory(selectedCategory.id, newName)
                                            alertDialog.dismiss()
                                        }
                                    }
                                    .setNegativeButton("Batal", null)
                                    .show()
                            }

                            1 -> { // Memilih Hapus Kategori
                                AlertDialog.Builder(requireContext())
                                    .setTitle("Hapus Kategori")
                                    .setMessage("Apakah Anda yakin ingin menghapus kategori '${selectedCategory.name}'?")
                                    .setPositiveButton("Ya") { _, _ ->
                                        viewModel.deleteCategory(selectedCategory.id)
                                        alertDialog.dismiss()
                                    }
                                    .setNegativeButton("Batal", null)
                                    .show()
                            }
                        }
                    }.show()
            }
        }

        dialogBinding.btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

//    private fun showUnitDialog() {
//        val dialogBinding = DialogAddCategoryBinding.inflate(layoutInflater)
//        val builder = AlertDialog.Builder(requireContext()).setView(dialogBinding.root)
//        val alertDialog = builder.create()
//        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
//
//        // Ubah text secara dinamis agar sesuai dengan pengelolaan Unit
//        val rootLayout = dialogBinding.root.getChildAt(0) as LinearLayout
//        val titleText = rootLayout.getChildAt(0) as TextView
//        titleText.text = "Tambah/Kelola Unit"
//        val subtitleText = rootLayout.getChildAt(1) as TextView
//        subtitleText.text = "Unit Saat Ini:"
//        dialogBinding.tilCategoryName.hint = "Nama Unit baru (cth: Pcs)"
//        dialogBinding.etCategoryName.hint = "Nama Unit baru (cth: Pcs)"
//
//        // READ: Menampilkan List data ke ListView
//        viewModel.units.observe(viewLifecycleOwner) { listUnit ->
//            val namesList = listUnit.map { it.name }
//            dialogBinding.rvExistingCategories.adapter = android.widget.ArrayAdapter(
//                requireContext(),
//                android.R.layout.simple_list_item_1,
//                namesList
//            )
//
//            // UPDATE & DELETE: Ketika salah satu item unit di dalam list diklik
//            dialogBinding.rvExistingCategories.setOnItemClickListener { _, _, position, _ ->
//                val selectedUnit = listUnit[position]
//
//                // Tampilkan opsi Edit atau Hapus
//                AlertDialog.Builder(requireContext())
//                    .setTitle("Opsi Unit: ${selectedUnit.name}")
//                    .setItems(arrayOf("Edit Nama", "Hapus Unit")) { _, which ->
//                        when (which) {
//                            0 -> { // Memilih Edit Nama
//                                dialogBinding.etCategoryName.setText(selectedUnit.name)
//                                dialogBinding.btnSave.text = "Update"
//
//                                dialogBinding.btnSave.setOnClickListener {
//                                    val newName =
//                                        dialogBinding.etCategoryName.text.toString().trim()
//                                    if (newName.isNotEmpty()) {
//                                        viewModel.updateUnit(selectedUnit.id, newName)
//                                        alertDialog.dismiss()
//                                    }
//                                }
//                            }
//
//                            1 -> { // Memilih Hapus Unit
//                                AlertDialog.Builder(requireContext())
//                                    .setTitle("Hapus Unit")
//                                    .setMessage("Apakah Anda yakin ingin menghapus unit '${selectedUnit.name}'?")
//                                    .setPositiveButton("Ya") { _, _ ->
//                                        viewModel.deleteUnit(selectedUnit.id)
//                                        alertDialog.dismiss()
//                                    }
//                                    .setNegativeButton("Batal", null)
//                                    .show()
//                            }
//                        }
//                    }.show()
//            }
//        }
//
//        // CREATE: Aksi default tombol simpan untuk menambah unit baru
//        dialogBinding.btnSave.setOnClickListener {
//            val unitName = dialogBinding.etCategoryName.text.toString().trim()
//            if (unitName.isNotEmpty()) {
//                viewModel.addUnit(unitName)
//                alertDialog.dismiss()
//            } else {
//                dialogBinding.tilCategoryName.error = "Nama unit wajib diisi"
//            }
//        }
//
//        dialogBinding.btnCancel.setOnClickListener {
//            alertDialog.dismiss()
//        }
//
//        alertDialog.show()
//    }

    private fun showProductDialog(existingProduct: Product? = null) {
        val dialogBinding = DialogAddProductBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext()).setView(dialogBinding.root)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        var selectedCategoryId = 0
        var selectedUnitName = "pcs"

        imageUri = null
        currentDialogImageView = dialogBinding.ivProductPreview

        dialogBinding.btnUploadImage.setOnClickListener {
            getImage.launch("image/*")
        }

        // Memuat list kategori ke dalam Spinner untuk relasi "category_id" produk
        viewModel.categories.observe(viewLifecycleOwner) { listCategory ->
            val categoryNames = listCategory.map { it.name }
            val adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            dialogBinding.spinnerCategory.adapter = adapter

            // Jika dalam mode EDIT, posisikan spinner pada kategori lama produk
            existingProduct?.let { prod ->
                val index = listCategory.indexOfFirst { it.id == prod.categoryId }
                if (index != -1) dialogBinding.spinnerCategory.setSelection(index)
            }

            dialogBinding.spinnerCategory.onItemSelectedListener =
                object : android.widget.AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: android.widget.AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        selectedCategoryId = listCategory[position].id
                    }

                    override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                }
        }

        // Memuat list unit ke dalam Spinner untuk relasi "unit" produk
        viewModel.units.observe(viewLifecycleOwner) { listUnit ->
            val unitNames = listUnit.map { it.name }.toMutableList()

            // Jika dalam mode EDIT dan unit produk tidak ada di list aktif, tambahkan sementara agar tidak hilang
            existingProduct?.let { prod ->
                val prodUnit = prod.unit ?: "pcs"
                val exists = unitNames.any { it.equals(prodUnit, ignoreCase = true) }
                if (!exists) {
                    unitNames.add(prodUnit)
                }
            }

            val adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, unitNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            dialogBinding.spinnerUnit.adapter = adapter

            // Jika dalam mode EDIT, posisikan spinner pada unit lama produk
            existingProduct?.let { prod ->
                val prodUnit = prod.unit ?: "pcs"
                val index = unitNames.indexOfFirst { it.equals(prodUnit, ignoreCase = true) }
                if (index != -1) {
                    dialogBinding.spinnerUnit.setSelection(index)
                    selectedUnitName = unitNames[index]
                }
            }

            dialogBinding.spinnerUnit.onItemSelectedListener =
                object : android.widget.AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: android.widget.AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        selectedUnitName = unitNames[position]
                    }

                    override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                }
        }

        // Kondisi jika data diklik untuk UPDATE / DELETE
        if (existingProduct != null) {
            dialogBinding.tvDialogTitle.text = "Ubah Produk"
            dialogBinding.etProductName.setText(existingProduct.name)
            dialogBinding.etSellPrice.setText(existingProduct.sellPrice.toInt().toString())
            dialogBinding.etCostPrice.setText(existingProduct.costPrice.toInt().toString())
            dialogBinding.etInitialStock.setText(existingProduct.initialStock.toString())
            dialogBinding.etMinimumStock.setText(existingProduct.minStock.toString())
            dialogBinding.btnSave.text = "Update"

            existingProduct.imageUrl?.let { url ->
                if (url.isNotEmpty()) {
                    // Gunakan pustaka load gambar seperti Glide atau Coil untuk efisiensi
                    Glide.with(this).load(url).placeholder(R.drawable.ic_placeholder_product)
                        .into(dialogBinding.ivProductPreview)

                    // Alternatif sederhana jika URL-nya lokal/base64, tapi Glide sangat disarankan
                    // dialogBinding.ivProductPreview.setImageURI(Uri.parse(url))
                }
            }

        }

        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.etProductName.text.toString().trim()
            val sellPriceStr = dialogBinding.etSellPrice.text.toString().trim()
            val costPriceStr = dialogBinding.etCostPrice.text.toString().trim()
            val initialStockStr = dialogBinding.etInitialStock.text.toString().trim()
            val minStockStr = dialogBinding.etMinimumStock.text.toString().trim()

            if (name.isEmpty() || sellPriceStr.isEmpty() || costPriceStr.isEmpty() ||
                initialStockStr.isEmpty() || minStockStr.isEmpty() || selectedCategoryId == 0
            ) {
                Toast.makeText(requireContext(), "Semua field wajib diisi!", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val productData = Product(
                id = existingProduct?.id,
                categoryId = selectedCategoryId,
                name = name,
                sellPrice = sellPriceStr.toDouble(),
                costPrice = costPriceStr.toDouble(),
                imageUrl = existingProduct?.imageUrl,
                initialStock = initialStockStr.toIntOrNull() ?: 0,
                minStock = minStockStr.toIntOrNull() ?: 0,
                unit = selectedUnitName,
                stock = existingProduct?.stock ?: (initialStockStr.toIntOrNull() ?: 0)
            )

            if (existingProduct == null) {
                // Jalankan Aksi CREATE
                viewModel.addProductWithImage(requireContext(), productData, imageUri)
            } else {
                // Jalankan Aksi UPDATE
                viewModel.updateProduct(existingProduct.id ?: 0, productData)
            }
            alertDialog.dismiss()
        }
        dialogBinding.btnCancel.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    private fun showManageProductsDialog() {
        val dialogBinding = DialogAddCategoryBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext()).setView(dialogBinding.root)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Customise text
        val rootLayout = dialogBinding.root.getChildAt(0) as LinearLayout
        val titleText = rootLayout.getChildAt(0) as TextView
        titleText.text = "Kelola Produk"
        val subtitleText = rootLayout.getChildAt(1) as TextView
        subtitleText.text = "Pilih produk untuk diubah atau dihapus:"

        // Sembunyikan field input dan tombol save karena ini hanya untuk list/manage
        dialogBinding.tilCategoryName.visibility = View.GONE
        dialogBinding.btnSave.visibility = View.GONE

        // READ: Menampilkan List data ke ListView
        viewModel.products.observe(viewLifecycleOwner) { listProduct ->
            val namesList =
                listProduct.map { "${it.name} (${it.unit ?: "pcs"}) - Rp ${it.sellPrice.toInt()}" }
            dialogBinding.rvExistingCategories.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                namesList
            )

            // UPDATE & DELETE: Ketika salah satu item produk di dalam list diklik
            dialogBinding.rvExistingCategories.setOnItemClickListener { _, _, position, _ ->
                val selectedProduct = listProduct[position]

                // Tampilkan opsi Edit atau Hapus
                AlertDialog.Builder(requireContext())
                    .setTitle("Opsi Produk: ${selectedProduct.name}")
                    .setItems(arrayOf("Edit Produk", "Hapus Produk")) { _, which ->
                        when (which) {
                            0 -> { // Memilih Edit Produk
                                showProductDialog(selectedProduct)
                                alertDialog.dismiss()
                            }

                            1 -> { // Memilih Hapus Produk
                                AlertDialog.Builder(requireContext())
                                    .setTitle("Hapus Produk")
                                    .setMessage("Apakah Anda yakin ingin menghapus produk '${selectedProduct.name}'?")
                                    .setPositiveButton("Ya") { _, _ ->
                                        viewModel.deleteProduct(selectedProduct.id ?: 0)
                                        alertDialog.dismiss()
                                    }
                                    .setNegativeButton("Batal", null)
                                    .show()
                            }
                        }
                    }.show()
            }
        }

        dialogBinding.btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun showAddUserDialog() {
        val dialogBinding =
            com.mobile.tugasrancangmoka.databinding.DialogAddUserBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext()).setView(dialogBinding.root)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val roles = arrayOf("ADMIN", "KASIR")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerUserRole.adapter = adapter

        dialogBinding.btnSave.setOnClickListener {
            val fullName = dialogBinding.etUserFullname.text.toString().trim()
            val email = dialogBinding.etUserEmail.text.toString().trim()
            val password = dialogBinding.etUserPassword.text.toString().trim()
            val selectedRole = dialogBinding.spinnerUserRole.selectedItem.toString().lowercase()

            dialogBinding.tilUserFullname.error = null
            dialogBinding.tilUserEmail.error = null
            dialogBinding.tilUserPassword.error = null

            // 1. Pengaman Nama Lengkap
            if (fullName.isEmpty()) {
                dialogBinding.tilUserFullname.error = "Nama lengkap wajib diisi"
                return@setOnClickListener
            }

            // 2. PENGAMAN EMAIL: Cek apakah kosong
            if (email.isEmpty()) {
                dialogBinding.tilUserEmail.error = "Email wajib diisi"
                return@setOnClickListener
            }

            // 3. PENGAMAN EMAIL: Validasi format regex email standar (huruf@domain.ekstensi)
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                dialogBinding.tilUserEmail.error =
                    "Format email tidak valid (contoh: user@gmail.com)"
                return@setOnClickListener
            }

            // 4. Pengaman Password
            if (password.isEmpty() || password.length < 6) {
                dialogBinding.tilUserPassword.error = "Password minimal 6 karakter"
                return@setOnClickListener
            }

            // Susun Map dengan key parameter yang sesuai dengan schema backend Node.js Anda
            // Jika lolos semua pengaman, kirim data ke backend
            val requestMap = mapOf(
                "full_name" to fullName,
                "email" to email,
                "password" to password,
                "role" to selectedRole
            )

            viewModel.addUser(requestMap)
            alertDialog.dismiss()
        }

        dialogBinding.btnCancel.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    private fun showEditUserDialog(user: com.mobile.tugasrancangmoka.model.User) {
        val dialogBinding =
            com.mobile.tugasrancangmoka.databinding.DialogAddUserBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext()).setView(dialogBinding.root)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.tvDialogTitle.text = "Ubah Informasi User"
        dialogBinding.btnSave.text = "Update"

        // 1. Set data lama user ke dalam form input
        dialogBinding.etUserFullname.setText(user.fullName)
        dialogBinding.etUserEmail.setText(user.email) // <-- Set email lama

        // 2. PASTIKAN INPUT EMAIL & PASSWORD KEDUANYA VISIBLE
        dialogBinding.tilUserEmail.visibility = View.VISIBLE    // Tampilkan email
        dialogBinding.tilUserPassword.visibility = View.VISIBLE // Tampilkan password
        dialogBinding.tilUserPassword.hint = "Isi jika ingin ganti password baru"

        // Setup Spinner Role
        val roles = arrayOf("ADMIN", "KASIR")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerUserRole.adapter = adapter

        val currentRoleIndex = if (user.role.equals(
                "cashier",
                ignoreCase = true
            )
        ) roles.indexOf("KASIR") else roles.indexOf("ADMIN")
        if (currentRoleIndex != -1) dialogBinding.spinnerUserRole.setSelection(currentRoleIndex)

        // Aksi Tombol Update
        dialogBinding.btnSave.setOnClickListener {
            val fullName = dialogBinding.etUserFullname.text.toString().trim()
            val email = dialogBinding.etUserEmail.text.toString().trim() // Ambil ketikan email baru
            val password = dialogBinding.etUserPassword.text.toString().trim()
            val selectedRoleText = dialogBinding.spinnerUserRole.selectedItem.toString()

            // --- VALIDASI DAN PENGAMAN INPUT ---
            if (fullName.isEmpty()) {
                dialogBinding.tilUserFullname.error = "Nama lengkap wajib diisi"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                dialogBinding.tilUserEmail.error = "Email wajib diisi"
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                dialogBinding.tilUserEmail.error = "Format email tidak valid"
                return@setOnClickListener
            }
            if (password.isNotEmpty() && password.length < 6) {
                dialogBinding.tilUserPassword.error = "Password baru minimal 6 karakter"
                return@setOnClickListener
            }

            val backendRole = if (selectedRoleText == "KASIR") "cashier" else "admin"

            // 3. Susun data Map lengkap termasuk email baru untuk dikirim ke ViewModel
            val updateMap = mutableMapOf(
                "full_name" to fullName,
                "email" to email, // <-- Kirim email ke server
                "role" to backendRole
            )

            if (password.isNotEmpty()) {
                updateMap["password"] = password
            }

            viewModel.updateUser(user.id, updateMap)
            alertDialog.dismiss()
        }

        dialogBinding.btnCancel.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    private fun setupObservers() {
        viewModel.categoryResult.observe(viewLifecycleOwner) { result ->
            if (!result.isNullOrEmpty()) {
                Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show()
                viewModel.resetCategoryResult()
            }
        }

        // Mengamati perubahan jumlah kategori dan memperbarui teks tombol secara dinamis
        viewModel.categoriesCount.observe(viewLifecycleOwner) { count ->
            binding.btnGridManageCategory.text = "$count Items"
        }

        viewModel.unitResult.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            viewModel.fetchUnitsCount()
        }

        viewModel.productResult.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                viewModel.resetProductResult()
                viewModel.fetchProducts(null, null)
            }
        }

        viewModel.products.observe(viewLifecycleOwner) { listProduct ->
            binding.btnGridManageProduct.text = "${listProduct.size} Items"
        }

        viewModel.userResult.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.resetUserResult()
            }
        }
    }

    private fun performTestPrint() {
        val printManager =
            requireContext().getSystemService(Context.PRINT_SERVICE) as android.print.PrintManager
        val jobName = "${getString(R.string.app_name)} Test Receipt"

        val width = 480
        val height = 680
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(android.graphics.Color.WHITE)
        val paint = Paint()
        paint.color = android.graphics.Color.BLACK
        paint.isAntiAlias = true

        // Header
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CENTRAL CAFE", 140f, 60f, paint)

        paint.textSize = 18f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Jl. Universitas No. 1, Salatiga", 110f, 95f, paint)
        canvas.drawText("Telp: (0298) 123456", 160f, 125f, paint)

        paint.textSize = 16f
        canvas.drawText("----------------------------------------", 40f, 160f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TEST KONEKSI PRINTER BERHASIL", 70f, 195f, paint)

        paint.typeface = Typeface.DEFAULT
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("in", "ID"))
        canvas.drawText("Waktu: ${sdf.format(Date())}", 50f, 240f, paint)
        canvas.drawText("Koneksi: Ethernet (Epson TM-T88VI)", 50f, 275f, paint)
        canvas.drawText("Status: Online & Ready", 50f, 310f, paint)

        canvas.drawText("----------------------------------------", 40f, 345f, paint)

        // Sample Items
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Item Tes", 50f, 385f, paint)
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("1 x Rp 25.000", 50f, 415f, paint)
        canvas.drawText("Rp 25.000", 350f, 415f, paint)

        canvas.drawText("----------------------------------------", 40f, 455f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL", 50f, 495f, paint)
        canvas.drawText("Rp 25.000", 350f, 495f, paint)

        paint.typeface = Typeface.DEFAULT
        canvas.drawText("----------------------------------------", 40f, 535f, paint)

        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Printer Anda siap digunakan untuk aplikasi!", 80f, 575f, paint)

        try {
            printManager.print(
                jobName,
                com.mobile.tugasrancangmoka.utils.BitmapPrintAdapter(bitmap),
                null
            )
            Toast.makeText(requireContext(), "Memulai Test Print...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal print: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}