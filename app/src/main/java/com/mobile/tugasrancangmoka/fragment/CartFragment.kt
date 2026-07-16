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
import com.google.android.material.button.MaterialButton
import com.mobile.tugasrancangmoka.R
import com.mobile.tugasrancangmoka.api.ApiClient
import com.mobile.tugasrancangmoka.databinding.FragmentCartBinding
import com.mobile.tugasrancangmoka.model.CartItem
import com.mobile.tugasrancangmoka.model.Product
import com.mobile.tugasrancangmoka.repository.ProductRepo
import com.mobile.tugasrancangmoka.viewmodel.POSViewModel
import com.mobile.tugasrancangmoka.viewmodel.ViewModelFactory
import java.text.NumberFormat
import java.util.Locale

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var posViewModel: POSViewModel
    private lateinit var cartAdapter: CartItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apiService = ApiClient.getApiService(requireContext())
        val productRepo = ProductRepo(apiService)

        val factory = ViewModelFactory(productRepo)
        posViewModel = ViewModelProvider(requireActivity(), factory)[POSViewModel::class.java]

        setupRecyclerView()
        observeViewModel()
        setupActions()
    }

    private fun setupRecyclerView() {
        binding.rvCartItems.layoutManager = LinearLayoutManager(requireContext())
        cartAdapter = CartItemAdapter(emptyList(),
            onIncrease = { posViewModel.addToCart(it) },
            onDecrease = { posViewModel.decreaseQuantity(it) }
        )
        binding.rvCartItems.adapter = cartAdapter
    }

    private fun observeViewModel() {
        posViewModel.cartItems.observe(viewLifecycleOwner) { items ->
            if (items.isNullOrEmpty()) {
                binding.rvCartItems.visibility = View.GONE
                binding.textEmptyCart.visibility = View.VISIBLE
                binding.btnCheckout.isEnabled = false
            } else {
                binding.rvCartItems.visibility = View.VISIBLE
                binding.textEmptyCart.visibility = View.GONE
                binding.btnCheckout.isEnabled = true
                cartAdapter.updateData(items)
            }

            val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
            formatter.maximumFractionDigits = 0
            binding.textTotalPrice.text = formatter.format(posViewModel.getTotalPayment())
        }
    }

    private fun setupActions() {
        binding.btnClearCart.setOnClickListener {
            posViewModel.clearCart()
        }

        binding.btnCheckout.setOnClickListener {
            val checkoutBS = CheckoutBottomSheet()
            checkoutBS.show(parentFragmentManager, "CheckoutBottomSheet")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class CartItemAdapter(
        private var list: List<CartItem>,
        private val onIncrease: (Product) -> Unit,
        private val onDecrease: (Product) -> Unit
    ) : RecyclerView.Adapter<CartItemAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textName: TextView = view.findViewById(R.id.text_cart_product_name)
            val textPrice: TextView = view.findViewById(R.id.text_cart_product_price)
            val textQty: TextView = view.findViewById(R.id.text_cart_quantity)
            val textSubtotal: TextView = view.findViewById(R.id.text_cart_subtotal)
            val btnIncrease: MaterialButton = view.findViewById(R.id.btn_increase)
            val btnDecrease: MaterialButton = view.findViewById(R.id.btn_decrease)
        }

        fun updateData(newList: List<CartItem>) {
            this.list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_cart, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.textName.text = item.product.name

            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
            currencyFormatter.maximumFractionDigits = 0

            holder.textPrice.text = "${currencyFormatter.format(item.product.sellPrice)} / ${item.product.unit ?: "Pcs"}"
            holder.textQty.text = item.quantity.toString()
            holder.textSubtotal.text = currencyFormatter.format(item.subtotal)

            holder.btnIncrease.setOnClickListener { onIncrease(item.product) }
            holder.btnDecrease.setOnClickListener { onDecrease(item.product) }
        }

        override fun getItemCount() = list.size
    }
}
