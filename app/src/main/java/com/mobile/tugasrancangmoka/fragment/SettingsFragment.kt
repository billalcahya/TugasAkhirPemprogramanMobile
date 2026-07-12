package com.mobile.tugasrancangmoka.fragment

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.mobile.tugasrancangmoka.R
import com.mobile.tugasrancangmoka.api.ApiClient
import com.mobile.tugasrancangmoka.databinding.DialogAddCategoryBinding
import com.mobile.tugasrancangmoka.databinding.DialogAddProductBinding
import com.mobile.tugasrancangmoka.databinding.FragmentSettingsBinding
import com.mobile.tugasrancangmoka.model.Product
import com.mobile.tugasrancangmoka.repository.CategoryRepo
import com.mobile.tugasrancangmoka.viewmodel.SettingsVM
import com.mobile.tugasrancangmoka.viewmodel.UnitModel
import com.mobile.tugasrancangmoka.viewmodel.ViewModelFactory

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SettingsVM
    private var imageUri : Uri? = null
    private var currentDialogImageView: ImageView?= null

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            currentDialogImageView?.setImageURI(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        val apiService = ApiClient.getApiService(requireContext())
        val repository = CategoryRepo(apiService)
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[SettingsVM::class.java]

        setupActionListeners()
        setupObservers()

        // Ambil jumlah kategori, unit, & produk saat fragment pertama kali dibuka
        viewModel.fetchCategoriesCount()
        viewModel.fetchUnitsCount()
        viewModel.fetchProducts(null, null)
    }

    private fun setupActionListeners() {
        binding.btnGridAddCategory.setOnClickListener {
            showAddCategoryDialog()
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

    private fun showUnitDialog() {
        val dialogBinding = DialogAddCategoryBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext()).setView(dialogBinding.root)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Ubah text secara dinamis agar sesuai dengan pengelolaan Unit
        val rootLayout = dialogBinding.root.getChildAt(0) as LinearLayout
        val titleText = rootLayout.getChildAt(0) as TextView
        titleText.text = "Tambah/Kelola Unit"
        val subtitleText = rootLayout.getChildAt(1) as TextView
        subtitleText.text = "Unit Saat Ini:"
        dialogBinding.tilCategoryName.hint = "Nama Unit baru (cth: Pcs)"
        dialogBinding.etCategoryName.hint = "Nama Unit baru (cth: Pcs)"

        // READ: Menampilkan List data ke ListView
        viewModel.units.observe(viewLifecycleOwner) { listUnit ->
            val namesList = listUnit.map { it.name }
            dialogBinding.rvExistingCategories.adapter = android.widget.ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                namesList
            )

            // UPDATE & DELETE: Ketika salah satu item unit di dalam list diklik
            dialogBinding.rvExistingCategories.setOnItemClickListener { _, _, position, _ ->
                val selectedUnit = listUnit[position]

                // Tampilkan opsi Edit atau Hapus
                AlertDialog.Builder(requireContext())
                    .setTitle("Opsi Unit: ${selectedUnit.name}")
                    .setItems(arrayOf("Edit Nama", "Hapus Unit")) { _, which ->
                        when (which) {
                            0 -> { // Memilih Edit Nama
                                dialogBinding.etCategoryName.setText(selectedUnit.name)
                                dialogBinding.btnSave.text = "Update"

                                dialogBinding.btnSave.setOnClickListener {
                                    val newName =
                                        dialogBinding.etCategoryName.text.toString().trim()
                                    if (newName.isNotEmpty()) {
                                        viewModel.updateUnit(selectedUnit.id, newName)
                                        alertDialog.dismiss()
                                    }
                                }
                            }

                            1 -> { // Memilih Hapus Unit
                                AlertDialog.Builder(requireContext())
                                    .setTitle("Hapus Unit")
                                    .setMessage("Apakah Anda yakin ingin menghapus unit '${selectedUnit.name}'?")
                                    .setPositiveButton("Ya") { _, _ ->
                                        viewModel.deleteUnit(selectedUnit.id)
                                        alertDialog.dismiss()
                                    }
                                    .setNegativeButton("Batal", null)
                                    .show()
                            }
                        }
                    }.show()
            }
        }

        // CREATE: Aksi default tombol simpan untuk menambah unit baru
        dialogBinding.btnSave.setOnClickListener {
            val unitName = dialogBinding.etCategoryName.text.toString().trim()
            if (unitName.isNotEmpty()) {
                viewModel.addUnit(unitName)
                alertDialog.dismiss()
            } else {
                dialogBinding.tilCategoryName.error = "Nama unit wajib diisi"
            }
        }

        dialogBinding.btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

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
                    Glide.with(this).load(url).placeholder(R.drawable.ic_placeholder_product).into(dialogBinding.ivProductPreview)

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

    private fun setupObservers() {
        viewModel.categoryResult.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            // Refresh jumlah data setelah berhasil menambah kategori baru
            viewModel.fetchCategoriesCount()
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}