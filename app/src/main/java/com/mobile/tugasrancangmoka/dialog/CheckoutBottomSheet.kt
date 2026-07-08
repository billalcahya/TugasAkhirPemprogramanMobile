package com.mobile.tugasrancangmoka.dialog

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mobile.tugasrancangmoka.R
import com.mobile.tugasrancangmoka.adapter.OrderSummaryAdapter
import com.mobile.tugasrancangmoka.model.CartItem
import java.text.NumberFormat
import java.util.Locale

class CheckoutBottomSheet : BottomSheetDialogFragment() {

    private var onCheckoutSuccess: (() -> Unit)? = null
    
    companion object {
        private var currentCartItems: List<CartItem> = emptyList()
        
        fun newInstance(cartItems: List<CartItem>, onSuccess: () -> Unit): CheckoutBottomSheet {
            val fragment = CheckoutBottomSheet()
            currentCartItems = cartItems
            fragment.onCheckoutSuccess = onSuccess
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottomsheet_checkout, container, false)
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

        val items = currentCartItems
        val rvOrderItems = view.findViewById<RecyclerView>(R.id.rv_order_items)
        rvOrderItems.layoutManager = LinearLayoutManager(context)
        rvOrderItems.adapter = OrderSummaryAdapter(items)

        // Calculations
        val subtotal = items.sumOf { it.totalPrice }
        val tax = subtotal * 0.11 // 11% tax
        val grandTotal = subtotal + tax

        val numberFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))

        val tvSubtotal = view.findViewById<TextView>(R.id.tv_subtotal)
        val tvTax = view.findViewById<TextView>(R.id.tv_tax)
        val tvGrandTotal = view.findViewById<TextView>(R.id.tv_grand_total)
        val tvKembalian = view.findViewById<TextView>(R.id.tv_kembalian)

        tvSubtotal.text = "Rp ${numberFormat.format(subtotal)}"
        tvTax.text = "Rp ${numberFormat.format(tax)}"
        tvGrandTotal.text = "Rp ${numberFormat.format(grandTotal)}"
        tvKembalian.text = "Kembalian: Rp 0"

        val rgOrderType = view.findViewById<RadioGroup>(R.id.rg_order_type)
        val rgPaymentMethod = view.findViewById<RadioGroup>(R.id.rg_payment_method)
        val tilNominalDiterima = view.findViewById<TextInputLayout>(R.id.til_nominal_diterima)
        val etNominalDiterima = view.findViewById<TextInputEditText>(R.id.et_nominal_diterima)
        val btnKonfirmasi = view.findViewById<Button>(R.id.btn_konfirmasi_bayar)

        // Payment method change listener
        rgPaymentMethod.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rb_non_tunai) {
                tilNominalDiterima.visibility = View.GONE
                tvKembalian.visibility = View.GONE
            } else {
                tilNominalDiterima.visibility = View.VISIBLE
                tvKembalian.visibility = View.VISIBLE
                etNominalDiterima.setText("")
                tvKembalian.text = "Kembalian: Rp 0"
            }
        }

        // Cash change calculations
        etNominalDiterima.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val inputStr = s.toString()
                if (inputStr.isNotEmpty()) {
                    try {
                        val cashReceived = inputStr.toDouble()
                        val change = cashReceived - grandTotal
                        if (change >= 0) {
                            tvKembalian.text = "Kembalian: Rp ${numberFormat.format(change)}"
                        } else {
                            tvKembalian.text = "Kembalian: Rp 0 (Kurang Rp ${numberFormat.format(-change)})"
                        }
                    } catch (e: Exception) {
                        tvKembalian.text = "Kembalian: Rp 0"
                    }
                } else {
                    tvKembalian.text = "Kembalian: Rp 0"
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnKonfirmasi.setOnClickListener {
            val orderType = if (rgOrderType.checkedRadioButtonId == R.id.rb_dine_in) "Dine In" else "Take Away"
            val paymentMethod = if (rgPaymentMethod.checkedRadioButtonId == R.id.rb_tunai) "Tunai" else "Non-Tunai"
            
            var nominalDiterima = grandTotal
            var kembalian = 0.0
            
            if (rgPaymentMethod.checkedRadioButtonId == R.id.rb_tunai) {
                val cashText = etNominalDiterima.text.toString()
                if (cashText.isNotEmpty()) {
                    nominalDiterima = cashText.toDoubleOrNull() ?: grandTotal
                    kembalian = (nominalDiterima - grandTotal).coerceAtLeast(0.0)
                }
            }

            // Dismiss checkout and show receipt
            dismiss()
            
            val receiptBottomSheet = ReceiptBottomSheet.newInstance(
                items = items,
                orderType = orderType,
                paymentMethod = paymentMethod,
                grandTotal = grandTotal,
                change = kembalian,
                onDismissCallback = {
                    onCheckoutSuccess?.invoke()
                }
            )
            receiptBottomSheet.show(parentFragmentManager, "ReceiptBottomSheet")
        }
    }
}
