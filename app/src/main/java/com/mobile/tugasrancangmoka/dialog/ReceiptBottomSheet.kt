package com.mobile.tugasrancangmoka.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mobile.tugasrancangmoka.R
import com.mobile.tugasrancangmoka.adapter.OrderSummaryAdapter
import com.mobile.tugasrancangmoka.model.CartItem
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReceiptBottomSheet : BottomSheetDialogFragment() {

    private var onDismissCallback: (() -> Unit)? = null

    companion object {
        private var receiptItems: List<CartItem> = emptyList()
        private var orderType: String = ""
        private var paymentMethod: String = ""
        private var grandTotal: Double = 0.0
        private var change: Double = 0.0

        fun newInstance(
            items: List<CartItem>,
            orderType: String,
            paymentMethod: String,
            grandTotal: Double,
            change: Double,
            onDismissCallback: () -> Unit
        ): ReceiptBottomSheet {
            val fragment = ReceiptBottomSheet()
            receiptItems = items
            this.orderType = orderType
            this.paymentMethod = paymentMethod
            this.grandTotal = grandTotal
            this.change = change
            fragment.onDismissCallback = onDismissCallback
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottomsheet_receipt, container, false)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        if (bottomSheet != null) {
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup transaction code
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val dateStr = sdf.format(Date())
        val randomNum = (1000..9999).random()
        val transactionCode = "TRX-$dateStr-$randomNum"
        
        view.findViewById<TextView>(R.id.tv_transaction_code).text = transactionCode

        // RecyclerView
        val rvReceiptItems = view.findViewById<RecyclerView>(R.id.rv_receipt_items)
        rvReceiptItems.layoutManager = LinearLayoutManager(context)
        rvReceiptItems.adapter = OrderSummaryAdapter(receiptItems)

        // Binding details
        val numberFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
        
        view.findViewById<TextView>(R.id.tv_order_type).text = orderType
        view.findViewById<TextView>(R.id.tv_payment_method).text = paymentMethod
        view.findViewById<TextView>(R.id.tv_grand_total).text = "Rp ${numberFormat.format(grandTotal)}"
        view.findViewById<TextView>(R.id.tv_change_amount).text = "Kembalian: Rp ${numberFormat.format(change)}"

        // Button selesai
        view.findViewById<Button>(R.id.btn_selesai).setOnClickListener {
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
    }
}
