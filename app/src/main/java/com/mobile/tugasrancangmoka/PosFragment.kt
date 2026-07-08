package com.mobile.tugasrancangmoka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.mobile.tugasrancangmoka.adapter.ProductAdapter
import com.mobile.tugasrancangmoka.dialog.CheckoutBottomSheet
import com.mobile.tugasrancangmoka.model.CartItem
import com.mobile.tugasrancangmoka.model.Product
import java.text.NumberFormat
import java.util.Locale

class PosFragment : Fragment() {

    private val cart = mutableListOf<CartItem>()
    private lateinit var cardCartBar: MaterialCardView
    private lateinit var tvCartSummary: TextView
    private lateinit var btnCheckout: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        cardCartBar = view.findViewById(R.id.card_cart_bar)
        tvCartSummary = view.findViewById(R.id.tv_cart_summary)
        btnCheckout = view.findViewById(R.id.btn_checkout)

        // Setup dummy products
        val dummyProducts = listOf(
            Product(1, "Cappuccino", 25000.0, 15, R.drawable.ic_cup),
            Product(2, "Espresso Double", 20000.0, 30, R.drawable.coffee_bean_icon),
            Product(3, "Croissant Keju", 18000.0, 8, R.drawable.ic_cup),
            Product(4, "Caramel Macchiato", 32000.0, 12, R.drawable.ic_cup),
            Product(5, "Red Velvet Latte", 28000.0, 10, R.drawable.ic_cup),
            Product(6, "Chocolate Muffin", 16000.0, 6, R.drawable.ic_cup)
        )

        // Setup recycler view
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_products)
        recyclerView.adapter = ProductAdapter(dummyProducts) { product ->
            addToCart(product)
        }

        // Initialize cart UI
        updateCartUI()

        // Checkout button click
        btnCheckout.setOnClickListener {
            if (cart.isNotEmpty()) {
                val checkoutBottomSheet = CheckoutBottomSheet.newInstance(cart) {
                    // Success callback: clear cart and update UI
                    cart.clear()
                    updateCartUI()
                }
                checkoutBottomSheet.show(parentFragmentManager, "CheckoutBottomSheet")
            }
        }
    }

    private fun addToCart(product: Product) {
        val existingItem = cart.find { it.product.id == product.id }
        if (existingItem != null) {
            existingItem.quantity += 1
        } else {
            cart.add(CartItem(product, 1))
        }
        updateCartUI()
    }

    private fun updateCartUI() {
        if (cart.isEmpty()) {
            cardCartBar.visibility = View.GONE
        } else {
            cardCartBar.visibility = View.VISIBLE
            val totalItems = cart.sumOf { it.quantity }
            val totalPrice = cart.sumOf { it.totalPrice }
            val numberFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
            val formattedPrice = numberFormat.format(totalPrice)
            
            tvCartSummary.text = "KERANJANG ($totalItems Items) - Rp $formattedPrice"
        }
    }
}
