package com.mobile.tugasrancangmoka.fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobile.tugasrancangmoka.R
import com.mobile.tugasrancangmoka.databinding.DialogReceiptBinding
import com.mobile.tugasrancangmoka.model.CartItem
import com.mobile.tugasrancangmoka.model.CheckoutResponse
import com.mobile.tugasrancangmoka.utils.BitmapPrintAdapter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReceiptDialogFragment : DialogFragment() {

    private var _binding: DialogReceiptBinding? = null
    private val binding get() = _binding!!

    private var response: CheckoutResponse? = null
    private var cartItems: List<CartItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_TugasRancangMoka)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val responseJson = arguments?.getString(ARG_RESPONSE_JSON)
        val itemsJson = arguments?.getString(ARG_ITEMS_JSON)

        val gson = Gson()
        if (!responseJson.isNullOrEmpty()) {
            response = gson.fromJson(responseJson, CheckoutResponse::class.java)
        }
        if (!itemsJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<CartItem>>() {}.type
            cartItems = gson.fromJson(itemsJson, type)
        }

        setupViews()
        setupListeners()

        val prefs = requireContext().getSharedPreferences("smartcafe_prefs", Context.MODE_PRIVATE)
        val isAutoPrint = prefs.getBoolean("auto_print", true)
        if (isAutoPrint) {
            binding.root.postDelayed({
                if (isAdded) {
                    printReceipt()
                }
            }, 500)
        }
    }

    private fun setupViews() {
        val currentResponse = response ?: return
        val checkoutData = currentResponse.data ?: return

        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0

        binding.txtReceiptCode.text = checkoutData.transactionCode
        
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("in", "ID"))
        binding.txtReceiptDate.text = sdf.format(Date())

        val prefs = requireContext().getSharedPreferences("smartcafe_prefs", Context.MODE_PRIVATE)
        val cashierName = prefs.getString("user_name", "Kasir")
        binding.txtReceiptCashier.text = cashierName

        binding.layoutReceiptItems.removeAllViews()
        var calculatedSubtotal = 0.0
        val inflater = LayoutInflater.from(requireContext())

        for (item in cartItems) {
            val itemView = inflater.inflate(R.layout.item_receipt_product, binding.layoutReceiptItems, false)
            val txtName = itemView.findViewById<TextView>(R.id.txt_product_name)
            val txtQtyPrice = itemView.findViewById<TextView>(R.id.txt_product_qty_price)
            val txtSubtotal = itemView.findViewById<TextView>(R.id.txt_product_subtotal)

            txtName.text = item.product.name
            txtQtyPrice.text = "${item.quantity} x ${formatter.format(item.product.sellPrice)}"
            txtSubtotal.text = formatter.format(item.subtotal)

            calculatedSubtotal += item.subtotal
            binding.layoutReceiptItems.addView(itemView)
        }

        binding.txtReceiptSubtotal.text = formatter.format(calculatedSubtotal)

        val discount = arguments?.getDouble(ARG_DISCOUNT, 0.0) ?: 0.0
        val grandTotal = checkoutData.grandTotal
        
        if (discount > 0.0) {
            binding.layoutReceiptDiscount.visibility = View.VISIBLE
            binding.txtReceiptDiscount.text = "-${formatter.format(discount)}"
        } else {
            binding.layoutReceiptDiscount.visibility = View.GONE
        }

        val taxableAmount = (calculatedSubtotal - discount).coerceAtLeast(0.0)
        val tax = taxableAmount * 0.11
        binding.txtReceiptTax.text = formatter.format(tax)

        binding.txtReceiptGrandTotal.text = formatter.format(grandTotal)


        val isNonCash = checkoutData.changeAmount < 0.001 && checkoutData.grandTotal > 0 && Math.abs(checkoutData.grandTotal - (checkoutData.grandTotal + checkoutData.changeAmount)) < 0.001
        val changeAmount = checkoutData.changeAmount
        
        val paymentMethod = arguments?.getString(ARG_PAYMENT_METHOD) ?: "cash"
        
        if (paymentMethod == "non_cash") {
            binding.txtReceiptPaymentMethod.text = "NON-TUNAI (QRIS/DEBIT)"
            binding.layoutReceiptCashReceived.visibility = View.GONE
            binding.layoutReceiptChange.visibility = View.GONE
        } else {
            binding.txtReceiptPaymentMethod.text = "TUNAI"
            binding.layoutReceiptCashReceived.visibility = View.VISIBLE
            binding.layoutReceiptChange.visibility = View.VISIBLE
            
            val cashReceived = grandTotal + changeAmount
            binding.txtReceiptCashReceived.text = formatter.format(cashReceived)
            binding.txtReceiptChange.text = formatter.format(changeAmount)
        }
    }

    private fun setupListeners() {
        binding.btnPrintReceipt.setOnClickListener {
            printReceipt()
        }

        binding.btnDoneReceipt.setOnClickListener {
            dismiss()
        }
    }

    private fun printReceipt() {
        val printArea = binding.cardReceiptPrintArea
        val bitmap = createBitmapFromView(printArea)
        
        val printManager = requireContext().getSystemService(Context.PRINT_SERVICE) as android.print.PrintManager
        val jobName = "${getString(R.string.app_name)} Receipt"
        printManager.print(jobName, BitmapPrintAdapter(bitmap), null)
    }

    private fun createBitmapFromView(view: View): Bitmap {
        val width = if (view.width > 0) view.width else 480
        val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(widthSpec, heightSpec)
        
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val oldLeft = view.left
        val oldTop = view.top
        val oldRight = view.right
        val oldBottom = view.bottom
        
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.draw(canvas)
        
        view.layout(oldLeft, oldTop, oldRight, oldBottom)
        
        return bitmap
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_RESPONSE_JSON = "response_json"
        private const val ARG_ITEMS_JSON = "items_json"
        private const val ARG_PAYMENT_METHOD = "payment_method"
        private const val ARG_DISCOUNT = "discount"

        fun newInstance(response: CheckoutResponse, items: List<CartItem>, paymentMethod: String, discount: Double): ReceiptDialogFragment {
            val fragment = ReceiptDialogFragment()
            val args = Bundle()
            val gson = Gson()
            args.putString(ARG_RESPONSE_JSON, gson.toJson(response))
            args.putString(ARG_ITEMS_JSON, gson.toJson(items))
            args.putString(ARG_PAYMENT_METHOD, paymentMethod)
            args.putDouble(ARG_DISCOUNT, discount)
            fragment.arguments = args
            return fragment
        }
    }
}
