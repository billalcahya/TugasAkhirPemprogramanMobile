package com.mobile.tugasrancangmoka.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.mobile.tugasrancangmoka.R
import com.mobile.tugasrancangmoka.api.ApiClient
import com.mobile.tugasrancangmoka.databinding.FragmentMenuBinding
import com.mobile.tugasrancangmoka.model.Category
import com.mobile.tugasrancangmoka.model.Product
import com.mobile.tugasrancangmoka.repository.CategoryRepo
import com.mobile.tugasrancangmoka.repository.ProductRepo
import com.mobile.tugasrancangmoka.viewmodel.CategoryResult
import com.mobile.tugasrancangmoka.viewmodel.CategoryVM
import com.mobile.tugasrancangmoka.viewmodel.POSViewModel
import com.mobile.tugasrancangmoka.viewmodel.ProductResult
import com.mobile.tugasrancangmoka.viewmodel.ViewModelFactory
import java.text.NumberFormat
import java.util.Locale

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!
    private lateinit var posViewModel: POSViewModel
    private lateinit var categoryViewModel: CategoryVM
    private var selectedCategoryId: Int? = null
    private var searchQuery: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apiService = ApiClient.getApiService(requireContext())
        val productRepo = ProductRepo(apiService)
        val categoryRepo = CategoryRepo(apiService)
        val posFactory = ViewModelFactory(productRepo)
        posViewModel = ViewModelProvider(requireActivity(), posFactory)[POSViewModel::class.java]

        val catFactory = ViewModelFactory(categoryRepo)
        categoryViewModel = ViewModelProvider(this, catFactory)[CategoryVM::class.java]

        setupRecyclerViews()
        setupSearch()
        observeViewModels()

        categoryViewModel.fetchCategories()
        posViewModel.fetchProducts(selectedCategoryId, searchQuery)
    }

    private fun setupRecyclerViews() {
        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    private fun setupSearch() {
        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = if (s.isNullOrEmpty()) null else s.toString().trim()
                posViewModel.fetchProducts(selectedCategoryId, searchQuery)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModels() {
        categoryViewModel.categoryState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is CategoryResult.Loading -> {}
                is CategoryResult.Success -> {
                    val fullList = mutableListOf(Category(0, "All Menu", "#FFFFFF", true))
                    fullList.addAll(result.categories)
                    binding.rvCategories.adapter = CategoryAdapter(fullList)
                }
                is CategoryResult.Error -> {
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        posViewModel.productState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ProductResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.textEmptyState.visibility = View.GONE
                }
                is ProductResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (result.products.isEmpty()) {
                        binding.rvProducts.visibility = View.GONE
                        binding.textEmptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvProducts.visibility = View.VISIBLE
                        binding.textEmptyState.visibility = View.GONE
                        binding.rvProducts.adapter = ProductAdapter(result.products) { product ->
                            posViewModel.addToCart(product)
                            Toast.makeText(requireContext(), "${product.name} added to cart", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                is ProductResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.textEmptyState.visibility = View.VISIBLE
                    binding.textEmptyState.text = result.message
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class CategoryAdapter(private val categories: List<Category>) :
        RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val cardChip: MaterialCardView = view.findViewById(R.id.card_chip)
            val textName: TextView = view.findViewById(R.id.text_category_name)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_category_chip, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = categories[position]
            holder.textName.text = item.name

            val isSelected = (selectedCategoryId == null && item.id == 0) || (selectedCategoryId == item.id)
            if (isSelected) {
                holder.cardChip.setCardBackgroundColor(requireContext().getColor(R.color.primary))
                holder.textName.setTextColor(requireContext().getColor(R.color.on_primary))
                holder.cardChip.strokeWidth = 0
            } else {
                holder.cardChip.setCardBackgroundColor(requireContext().getColor(R.color.surface_container))
                holder.textName.setTextColor(requireContext().getColor(R.color.on_surface_variant))
                holder.cardChip.strokeWidth = 1
            }

            holder.cardChip.setOnClickListener {
                selectedCategoryId = if (item.id == 0) null else item.id
                notifyDataSetChanged()
                posViewModel.fetchProducts(selectedCategoryId, searchQuery)
            }
        }

        override fun getItemCount() = categories.size
    }

    private class ProductAdapter(
        private val products: List<Product>,
        private val onProductClick: (Product) -> Unit
    ) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val cardProduct: MaterialCardView = view.findViewById(R.id.card_product)
            val imgProduct: ImageView = view.findViewById(R.id.img_product)
            val textName: TextView = view.findViewById(R.id.text_product_name)
            val textPrice: TextView = view.findViewById(R.id.text_product_price)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_product, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = products[position]
            holder.textName.text = item.name

            val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
            formatter.maximumFractionDigits = 0
            holder.textPrice.text = "${formatter.format(item.sellPrice)} / ${item.unit ?: "Pcs"}"

            if (!item.imageUrl.isNullOrEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.ic_coffee)
                    .error(R.drawable.ic_coffee)
                    .into(holder.imgProduct)
            } else {
                holder.imgProduct.setImageResource(R.drawable.ic_coffee)
            }

            holder.cardProduct.setOnClickListener {
                onProductClick(item)
            }
        }

        override fun getItemCount() = products.size
    }
}
