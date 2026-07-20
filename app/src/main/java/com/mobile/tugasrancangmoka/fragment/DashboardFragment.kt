package com.mobile.tugasrancangmoka.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.mobile.tugasrancangmoka.api.ApiClient
import com.mobile.tugasrancangmoka.databinding.FragmentDashboardBinding
import com.mobile.tugasrancangmoka.model.LowStockAlert
import com.mobile.tugasrancangmoka.utils.SessionManager
import com.mobile.tugasrancangmoka.model.TopProduct
import com.mobile.tugasrancangmoka.repository.DashboardRepo
import com.mobile.tugasrancangmoka.viewmodel.DashboardResult
import com.mobile.tugasrancangmoka.viewmodel.DashboardVM
import com.mobile.tugasrancangmoka.viewmodel.ViewModelFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
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
        viewModel = ViewModelProvider(requireActivity(), factory)[DashboardVM::class.java]

        setupRecyclerViews()
        setupClickListeners()
        observeViewModel()

        val sessionManager = SessionManager(requireContext())
        val userRole = sessionManager.getRole() ?: ""
        if (!userRole.equals("admin", ignoreCase = true)) {
            binding.cardActionStockIn.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchDashboard()
    }

    private fun setupRecyclerViews() {
        binding.rvTopProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLowStock.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupClickListeners() {
        val bottomNav = activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(com.mobile.tugasrancangmoka.R.id.bottom_nav)

        binding.cardActionNewOrder.setOnClickListener {
            if (bottomNav != null) {
                bottomNav.selectedItemId = com.mobile.tugasrancangmoka.R.id.navigation_pos
            } else {
                findNavController().navigate(com.mobile.tugasrancangmoka.R.id.navigation_pos)
            }
        }
        binding.cardActionStockIn.setOnClickListener {
            if (bottomNav != null) {
                bottomNav.selectedItemId = com.mobile.tugasrancangmoka.R.id.navigation_inventory
            } else {
                findNavController().navigate(com.mobile.tugasrancangmoka.R.id.navigation_inventory)
            }
        }
        binding.cardActionReports.setOnClickListener {
            if (bottomNav != null) {
                bottomNav.selectedItemId = com.mobile.tugasrancangmoka.R.id.navigation_report
            } else {
                findNavController().navigate(com.mobile.tugasrancangmoka.R.id.navigation_report)
            }
        }
        binding.btnLiveFeed.setOnClickListener {
            if (bottomNav != null) {
                bottomNav.selectedItemId = com.mobile.tugasrancangmoka.R.id.navigation_history
            } else {
                findNavController().navigate(com.mobile.tugasrancangmoka.R.id.navigation_history)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.dashboardState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is DashboardResult.Loading -> {
                    if (binding.cardHero.visibility != View.VISIBLE) {
                        binding.layoutDashboardSkeleton.visibility = View.VISIBLE
                        binding.cardHero.visibility = View.GONE
                        binding.layoutQuickActions.visibility = View.GONE
                        binding.layoutStatusCards.visibility = View.GONE
                        binding.cardTopProducts.visibility = View.GONE
                        binding.cardLowStock.visibility = View.GONE
                    }
                    binding.progressBar.visibility = View.GONE
                }
                is DashboardResult.Success -> {
                    binding.layoutDashboardSkeleton.visibility = View.GONE
                    binding.cardHero.visibility = View.VISIBLE
                    binding.layoutQuickActions.visibility = View.VISIBLE
                    binding.layoutStatusCards.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE

                    val data = result.response.data
                    if (data != null) {
                        val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
                        formatter.maximumFractionDigits = 0
                        binding.textRevenueToday.text = formatter.format(data.totalRevenueToday)
                        binding.textTransactionsToday.text = data.totalTransactionsToday.toString()

                        val lowStockCount = data.lowStockAlerts?.size ?: 0
                        binding.textLowStockCount.text = if (lowStockCount == 1) "1 Item" else "$lowStockCount Items"

                        binding.cardTopProducts.visibility = View.VISIBLE
                        if (data.topProducts.isNullOrEmpty()) {
                            binding.rvTopProducts.visibility = View.GONE
                            binding.textEmptyTopProducts.visibility = View.VISIBLE
                        } else {
                            binding.rvTopProducts.visibility = View.VISIBLE
                            binding.textEmptyTopProducts.visibility = View.GONE
                            binding.rvTopProducts.adapter = TopProductsAdapter(data.topProducts)
                        }

                        binding.cardLowStock.visibility = View.VISIBLE
                        if (data.lowStockAlerts.isNullOrEmpty()) {
                            binding.rvLowStock.visibility = View.GONE
                            binding.textEmptyLowStock.visibility = View.VISIBLE
                        } else {
                            binding.rvLowStock.visibility = View.VISIBLE
                            binding.textEmptyLowStock.visibility = View.GONE
                            val sortedLowStock = data.lowStockAlerts.sortedBy { it.productName.lowercase(java.util.Locale.ROOT) }
                            binding.rvLowStock.adapter = LowStockAdapter(sortedLowStock) { item ->
                                val sharedPrefs = requireContext().getSharedPreferences("DashboardPrefs", android.content.Context.MODE_PRIVATE)
                                sharedPrefs.edit().putString("pending_restock_search", item.productName).apply()

                                val bottomNav = activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(com.mobile.tugasrancangmoka.R.id.bottom_nav)
                                if (bottomNav != null) {
                                    bottomNav.selectedItemId = com.mobile.tugasrancangmoka.R.id.navigation_inventory
                                } else {
                                    findNavController().navigate(com.mobile.tugasrancangmoka.R.id.navigation_inventory)
                                }
                            }
                        }
                    }
                }
                is DashboardResult.Error -> {
                    binding.layoutDashboardSkeleton.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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

    private class LowStockAdapter(
        private val list: List<LowStockAlert>,
        private val onRestockClick: (LowStockAlert) -> Unit
    ) : RecyclerView.Adapter<LowStockAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textName: TextView = view.findViewById(com.mobile.tugasrancangmoka.R.id.text_product_name)
            val textStock: TextView = view.findViewById(com.mobile.tugasrancangmoka.R.id.text_stock_status)
            val btnRestock: com.google.android.material.button.MaterialButton = view.findViewById(com.mobile.tugasrancangmoka.R.id.btn_restock)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(com.mobile.tugasrancangmoka.R.layout.item_low_stock, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.textName.text = item.productName
            holder.textStock.text = "Current Stock: ${item.currentStock} left"
            
            holder.btnRestock.setOnClickListener {
                onRestockClick(item)
            }
        }

        override fun getItemCount() = list.size
    }
}
