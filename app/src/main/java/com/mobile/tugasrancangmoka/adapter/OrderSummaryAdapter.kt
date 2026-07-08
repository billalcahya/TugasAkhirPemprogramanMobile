package com.mobile.tugasrancangmoka.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.mobile.tugasrancangmoka.R
import com.mobile.tugasrancangmoka.model.CartItem
import java.text.NumberFormat
import java.util.Locale

class OrderSummaryAdapter(private val items: List<CartItem>) :
    RecyclerView.Adapter<OrderSummaryAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivItemImage: ShapeableImageView = view.findViewById(R.id.iv_item_image)
        val tvItemName: TextView = view.findViewById(R.id.tv_item_name)
        val tvItemQty: TextView = view.findViewById(R.id.tv_item_qty)
        val tvItemPrice: TextView = view.findViewById(R.id.tv_item_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_summary, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val item = items[position]
        holder.ivItemImage.setImageResource(item.product.imageResId)
        holder.tvItemName.text = item.product.name
        holder.tvItemQty.text = "x${item.quantity}"
        
        val numberFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
        val formattedPrice = numberFormat.format(item.totalPrice)
        holder.tvItemPrice.text = "Rp $formattedPrice"
    }

    override fun getItemCount(): Int = items.size
}
