package com.mobile.tugasrancangmoka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.mobile.tugasrancangmoka.R
import com.mobile.tugasrancangmoka.adapter.StockAdapter
import com.mobile.tugasrancangmoka.model.StockItem
import com.mobile.tugasrancangmoka.model.StockStatus

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    // onViewCreated dipanggil SETELAH view selesai di-inflate dari XML.
    // Ini tempat yang tepat untuk findViewById dan pasang adapter,
    // karena di onCreateView view belum tentu siap sepenuhnya.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupStockWatchlist(view)
    }

    private fun setupStockWatchlist(view: View) {
        // Data contoh (dummy) — nanti di tahap integrasi,
        // ini akan diganti dengan data asli dari ViewModel/Repository
        // yang mengambil dari GET /inventory
        val dummyStockItems = listOf(
            StockItem(
                id = 1,
                name = "House Blend Beans",
                subtitle = "4.2kg remaining",
                stockPercentage = 70,
                status = StockStatus.NORMAL
            ),
            StockItem(
                id = 2,
                name = "Oat Milk (Barista)",
                subtitle = "Only 2 cartons left",
                stockPercentage = 0,
                status = StockStatus.WARNING
            ),
            StockItem(
                id = 3,
                name = "Paper Cups (12oz)",
                subtitle = "~250 units remaining",
                stockPercentage = 85,
                status = StockStatus.NORMAL
            )
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_stock_watchlist)
        recyclerView.adapter = StockAdapter(dummyStockItems)
    }
}