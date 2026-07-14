package com.mobile.tugasrancangmoka.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.mobile.tugasrancangmoka.R
import com.mobile.tugasrancangmoka.api.ApiClient
import com.mobile.tugasrancangmoka.databinding.BottomSheetCheckoutBinding
import com.mobile.tugasrancangmoka.repository.TransactionRepo
import com.mobile.tugasrancangmoka.viewmodel.CheckoutResult
import com.mobile.tugasrancangmoka.viewmodel.CheckoutViewModel
import com.mobile.tugasrancangmoka.viewmodel.DashboardVM
import com.mobile.tugasrancangmoka.viewmodel.POSViewModel
import com.mobile.tugasrancangmoka.viewmodel.ViewModelFactory
import java.text.NumberFormat
import java.util.Locale

class CheckoutBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCheckoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var posViewModel: POSViewModel
    private lateinit var checkoutViewModel: CheckoutViewModel

    private var subtotal = 0.0
    private var discount = 0.0
    private var tax = 0.0
    private var grandTotal = 0.0
    private var paymentAmount = 0.0
    private var change = 0.0
    private var paymentMethod = "cash"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apiService = ApiClient.getApiService(requireContext())
        val transactionRepo = TransactionRepo(apiService)

        // Ambil POSViewModel dari Activity scope agar cart data didapatkan
        posViewModel = ViewModelProvider(requireActivity())[POSViewModel::class.java]

        val factory = ViewModelFactory(transactionRepo)
        checkoutViewModel = ViewModelProvider(this, factory)[CheckoutViewModel::class.java]

        subtotal = posViewModel.getTotalPayment()
        calculateTotals()

        setupListeners()
        observeCheckoutState()
    }

    private fun calculateTotals() {
        val taxableAmount = (subtotal - discount).coerceAtLeast(0.0)
        tax = taxableAmount * 0.11
        grandTotal = taxableAmount + tax

        if (paymentMethod == "non_cash") {
            paymentAmount = grandTotal
            binding.layoutPaymentAmount.visibility = View.GONE
        } else {
            binding.layoutPaymentAmount.visibility = View.VISIBLE
            val inputStr = binding.editPaymentAmount.text.toString().trim()
            paymentAmount = inputStr.toDoubleOrNull() ?: 0.0
        }

        change = (paymentAmount - grandTotal).coerceAtLeast(0.0)

        val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
        formatter.maximumFractionDigits = 0

        binding.textCheckoutSubtotal.text = formatter.format(subtotal)
        binding.textCheckoutTax.text = formatter.format(tax)
        binding.textCheckoutTotal.text = formatter.format(grandTotal)
        binding.textChangeAmount.text = formatter.format(change)

        binding.btnSubmitPayment.isEnabled = paymentMethod == "non_cash" || paymentAmount >= grandTotal
    }

    private fun setupListeners() {
        binding.editCheckoutDiscount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                discount = s.toString().toDoubleOrNull() ?: 0.0
                calculateTotals()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.rgPaymentMethod.setOnCheckedChangeListener { _, checkedId ->
            paymentMethod = if (checkedId == R.id.rb_cash) "cash" else "non_cash"
            calculateTotals()
        }

        binding.editPaymentAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calculateTotals()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnSubmitPayment.setOnClickListener {
            val cart = posViewModel.cartItems.value.orEmpty()
            if (cart.isEmpty()) return@setOnClickListener

            checkoutViewModel.processCheckout(
                cartItems = cart,
                discount = discount,
                paymentMethod = paymentMethod,
                paymentAmount = paymentAmount
            )
        }
    }

    private fun observeCheckoutState() {
        checkoutViewModel.checkoutState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is CheckoutResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSubmitPayment.isEnabled = false
                }
                is CheckoutResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Transaction processed successfully!", Toast.LENGTH_SHORT).show()
                    
                    val currentCartItems = posViewModel.cartItems.value.orEmpty().toList()
                    posViewModel.clearCart()
                    
                    // Refresh data dashboard agar data terbaru muncul
                    try {
                        val dashboardVM = ViewModelProvider(requireActivity())[DashboardVM::class.java]
                        dashboardVM.fetchDashboard()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    val receiptDialog = ReceiptDialogFragment.newInstance(result.response, currentCartItems, paymentMethod, discount)
                    receiptDialog.show(parentFragmentManager, "ReceiptDialogFragment")
                    
                    dismiss()
                }
                is CheckoutResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmitPayment.isEnabled = true
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
