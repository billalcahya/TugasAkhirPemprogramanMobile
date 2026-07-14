package com.mobile.tugasrancangmoka.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.mobile.tugasrancangmoka.api.ApiClient
import com.mobile.tugasrancangmoka.databinding.FragmentDashboardBinding
import com.mobile.tugasrancangmoka.model.LowStockAlert
import com.mobile.tugasrancangmoka.model.TopProduct
import com.mobile.tugasrancangmoka.repository.DashboardRepo
import com.mobile.tugasrancangmoka.viewmodel.DashboardResult
import com.mobile.tugasrancangmoka.viewmodel.DashboardVM
import com.mobile.tugasrancangmoka.viewmodel.ViewModelFactory
import java.text.NumberFormat
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DashboardVM

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apiService = ApiClient.getApiService(requireContext())
        val repository = DashboardRepo(apiService)
        val factory = ViewModelFactory(repository)
        // Scope ke Activity agar data dashboard bertahan ketika berpindah tab
        viewModel = ViewModelProvider(requireActivity(), factory)[DashboardVM::class.java]

        setupRecyclerViews()
        observeViewModel()

        // Panggil API hanya jika data belum berhasil dimuat sebelumnya
        if (viewModel.dashboardState.value !is DashboardResult.Success) {
            viewModel.fetchDashboard()
        }
    }

    private fun setupRecyclerViews() {
        binding.rvTopProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLowStock.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeViewModel() {
        viewModel.dashboardState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is DashboardResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is DashboardResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val data = result.response.data
                    if (data != null) {
                        // Format currency
                        val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
                        formatter.maximumFractionDigits = 0
                        binding.textRevenueToday.text = formatter.format(data.totalRevenueToday)
                        binding.textTransactionsToday.text = data.totalTransactionsToday.toString()

                        // Top Products
                        if (data.topProducts.isNullOrEmpty()) {
                            binding.rvTopProducts.visibility = View.GONE
                            binding.textEmptyTopProducts.visibility = View.VISIBLE
                        } else {
                            binding.rvTopProducts.visibility = View.VISIBLE
                            binding.textEmptyTopProducts.visibility = View.GONE
                            binding.rvTopProducts.adapter = TopProductsAdapter(data.topProducts)
                        }

                        // Low Stock
                        if (data.lowStockAlerts.isNullOrEmpty()) {
                            binding.rvLowStock.visibility = View.GONE
                            binding.textEmptyLowStock.visibility = View.VISIBLE
                        } else {
                            binding.rvLowStock.visibility = View.VISIBLE
                            binding.textEmptyLowStock.visibility = View.GONE
                            binding.rvLowStock.adapter = LowStockAdapter(data.lowStockAlerts)
                        }
                    }
                }
                is DashboardResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Inner Adapter for Top Products
    private class TopProductsAdapter(private val list: List<TopProduct>) :
        RecyclerView.Adapter<TopProductsAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textName: TextView = view.findViewById(android.R.id.text1)
            val textQty: TextView = view.findViewById(android.R.id.text2)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.textName.text = item.productName
            holder.textQty.text = "Sold: ${item.quantity} units"
            holder.textName.setTextColor(holder.itemView.context.getColor(com.mobile.tugasrancangmoka.R.color.on_surface))
            holder.textQty.setTextColor(holder.itemView.context.getColor(com.mobile.tugasrancangmoka.R.color.on_surface_variant))
        }

        override fun getItemCount() = list.size
    }

    // Inner Adapter for Low Stock Alerts
    private class LowStockAdapter(private val list: List<LowStockAlert>) :
        RecyclerView.Adapter<LowStockAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textName: TextView = view.findViewById(android.R.id.text1)
            val textStock: TextView = view.findViewById(android.R.id.text2)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.textName.text = item.productName
            holder.textStock.text = "Current Stock: ${item.currentStock} left"
            holder.textName.setTextColor(holder.itemView.context.getColor(com.mobile.tugasrancangmoka.R.color.error))
            holder.textStock.setTextColor(holder.itemView.context.getColor(com.mobile.tugasrancangmoka.R.color.on_surface_variant))
        }

        override fun getItemCount() = list.size
    }
}
