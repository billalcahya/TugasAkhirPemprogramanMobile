package com.mobile.tugasrancangmoka.fragment

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.mobile.tugasrancangmoka.R
import com.mobile.tugasrancangmoka.api.ApiClient
import com.mobile.tugasrancangmoka.databinding.FragmentInventoryBinding
import com.mobile.tugasrancangmoka.model.InventoryItem
import com.mobile.tugasrancangmoka.repository.InventoryRepo
import com.mobile.tugasrancangmoka.viewmodel.InventoryResult
import com.mobile.tugasrancangmoka.viewmodel.InventoryViewModel
import com.mobile.tugasrancangmoka.viewmodel.UpdateStockResult
import com.mobile.tugasrancangmoka.viewmodel.ViewModelFactory

class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: InventoryViewModel
    private var searchQuery: String? = null
    private var fullInventoryList: List<InventoryItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apiService = ApiClient.getApiService(requireContext())
        val repository = InventoryRepo(apiService)
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[InventoryViewModel::class.java]

        setupRecyclerView()
        setupSearch()
        observeViewModel()

        // Check for pending restock search from Dashboard
        val sharedPrefs = requireContext().getSharedPreferences("DashboardPrefs", android.content.Context.MODE_PRIVATE)
        val pendingSearch = sharedPrefs.getString("pending_restock_search", null)
        if (!pendingSearch.isNullOrEmpty()) {
            sharedPrefs.edit().remove("pending_restock_search").apply()
            binding.editSearch.setText(pendingSearch)
        }

        viewModel.fetchStock()
    }

    private fun setupRecyclerView() {
        binding.rvInventory.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupSearch() {
        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = if (s.isNullOrEmpty()) null else s.toString().trim()
                filterAndDisplayStock()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterAndDisplayStock() {
        val query = searchQuery
        val filteredList = if (query.isNullOrEmpty()) {
            fullInventoryList
        } else {
            fullInventoryList.filter { item ->
                item.name.contains(query, ignoreCase = true)
            }
        }

        if (filteredList.isEmpty()) {
            binding.rvInventory.visibility = View.GONE
            binding.textEmptyState.visibility = View.VISIBLE
            binding.textEmptyState.text = "No products found"
        } else {
            binding.rvInventory.visibility = View.VISIBLE
            binding.textEmptyState.visibility = View.GONE
            binding.rvInventory.adapter = InventoryAdapter(filteredList) { item ->
                showUpdateStockDialog(item)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.inventoryState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is InventoryResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.textEmptyState.visibility = View.GONE
                }
                is InventoryResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    fullInventoryList = result.products
                    filterAndDisplayStock()
                }
                is InventoryResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.textEmptyState.visibility = View.VISIBLE
                    binding.textEmptyState.text = result.message
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.updateStockState.observe(viewLifecycleOwner) { result ->
            if (result == null) return@observe
            when (result) {
                is UpdateStockResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is UpdateStockResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Stock updated successfully", Toast.LENGTH_SHORT).show()
                    viewModel.resetUpdateState()
                }
                is UpdateStockResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    viewModel.resetUpdateState()
                }
            }
        }
    }

    private fun showUpdateStockDialog(product: InventoryItem) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Update Stock: ${product.name}")

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.setText(product.stock.toString())
        input.selectAll()
        builder.setView(input)

        builder.setPositiveButton("Save") { dialog, _ ->
            val value = input.text.toString().trim()
            val newStock = value.toIntOrNull()
            if (newStock != null) {
                viewModel.updateProductStock(product.id, newStock)
            } else {
                Toast.makeText(requireContext(), "Invalid stock amount", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class InventoryAdapter(
        private val list: List<InventoryItem>,
        private val onUpdateClick: (InventoryItem) -> Unit
    ) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textName: TextView = view.findViewById(R.id.text_inventory_product_name)
            val textCategory: TextView = view.findViewById(R.id.text_inventory_category)
            val textStock: TextView = view.findViewById(R.id.text_inventory_stock_count)
            val btnEdit: MaterialButton = view.findViewById(R.id.btn_edit_stock)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_inventory, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.textName.text = item.name
            holder.textCategory.text = "Product ID: ${item.productId}"
            holder.textStock.text = "Stock: ${item.stock} ${item.unit.orEmpty()}".trim()

            holder.btnEdit.setOnClickListener {
                onUpdateClick(item)
            }
        }

        override fun getItemCount() = list.size
    }
}