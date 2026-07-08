package com.mobile.tugasrancangmoka.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobile.tugasrancangmoka.R
import com.mobile.tugasrancangmoka.model.StockItem
import com.mobile.tugasrancangmoka.model.StockStatus

class StockAdapter(private val items: List<StockItem>) :
    RecyclerView.Adapter<StockAdapter.StockViewHolder>() {

    class StockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textName: TextView = view.findViewById(R.id.text_stock_name)
        val textSubtitle: TextView = view.findViewById(R.id.text_stock_subtitle)
        val textPercentage: TextView = view.findViewById(R.id.text_stock_percentage)
        val progressStock: ProgressBar = view.findViewById(R.id.progress_stock)
        val badgeStatus: TextView = view.findViewById(R.id.badge_stock_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stock_watchlist, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val item = items[position]
        holder.textName.text = item.name
        holder.textSubtitle.text = item.subtitle
        holder.textPercentage.text = "${item.stockPercentage}%"
        holder.progressStock.progress = item.stockPercentage

        if (item.status == StockStatus.WARNING) {
            holder.badgeStatus.text = "LOW STOCK"
            holder.badgeStatus.visibility = View.VISIBLE
            holder.badgeStatus.setBackgroundResource(R.drawable.bg_pill_warning)
            holder.badgeStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.on_secondary_container))
            holder.progressStock.progressTintList = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.context, R.color.error)
            )
        } else {
            holder.badgeStatus.text = "NORMAL"
            holder.badgeStatus.visibility = View.VISIBLE
            holder.badgeStatus.setBackgroundResource(R.drawable.bg_pill_translucent)
            holder.badgeStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.primary))
            holder.progressStock.progressTintList = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.context, R.color.primary)
            )
        }
    }

    override fun getItemCount(): Int = items.size
}