package com.mobile.tugasrancangmoka.fragment

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mobile.tugasrancangmoka.R
import com.mobile.tugasrancangmoka.api.ApiClient
import com.mobile.tugasrancangmoka.databinding.DialogAddCategoryBinding
import com.mobile.tugasrancangmoka.databinding.FragmentSettingsBinding
import com.mobile.tugasrancangmoka.repository.CategoryRepo
import com.mobile.tugasrancangmoka.viewmodel.SettingsVM
import com.mobile.tugasrancangmoka.viewmodel.ViewModelFactory

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SettingsVM

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        val apiService = ApiClient.getApiService(requireContext())
        val repository = CategoryRepo(apiService)
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[SettingsVM::class.java]

        setupActionListeners()
        setupObservers()

        // Ambil jumlah kategori saat fragment pertama kali dibuka
        viewModel.fetchCategoriesCount()
    }

    private fun setupActionListeners() {
        binding.btnGridAddCategory.setOnClickListener {
            showAddCategoryDialog()
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
                                    val newName = dialogBinding.etCategoryName.text.toString().trim()
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}