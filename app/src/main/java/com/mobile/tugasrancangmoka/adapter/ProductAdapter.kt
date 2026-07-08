package com.mobile.tugasrancangmoka.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.mobile.tugasrancangmoka.R
import com.mobile.tugasrancangmoka.model.Product
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private val items: List<Product>,
    private val onAddToCartClick: (Product) -> Unit = {}
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProductImage: ShapeableImageView = view.findViewById(R.id.iv_product_image)
        val tvProductNamePrice: TextView = view.findViewById(R.id.tv_product_name_price)
        val tvStockRemaining: TextView = view.findViewById(R.id.tv_stock_remaining)
        val btnAddToCart: ImageView = view.findViewById(R.id.btn_add_to_cart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_card, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = items[position]
        
        holder.ivProductImage.setImageResource(product.imageResId)
        
        // Format price to IDR currency format (e.g. Rp 20.000)
        val numberFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
        val formattedPrice = numberFormat.format(product.price)
        holder.tvProductNamePrice.text = "${product.name} - Rp $formattedPrice"
        
        holder.tvStockRemaining.text = "${product.stock} pcs remaining"
        
        holder.btnAddToCart.setOnClickListener {
            onAddToCartClick(product)
        }
    }

    override fun getItemCount(): Int = items.size
}
