package com.mobile.tugasrancangmoka.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.mobile.tugasrancangmoka.R
import com.mobile.tugasrancangmoka.api.ApiClient
import com.mobile.tugasrancangmoka.databinding.DialogTransactionDetailBinding
import com.mobile.tugasrancangmoka.databinding.FragmentHistoryBinding
import com.mobile.tugasrancangmoka.model.TransactionRecord
import com.mobile.tugasrancangmoka.repository.TransactionRepo
import com.mobile.tugasrancangmoka.viewmodel.DetailResult
import com.mobile.tugasrancangmoka.viewmodel.HistoryResult
import com.mobile.tugasrancangmoka.viewmodel.TransactionVM
import com.mobile.tugasrancangmoka.viewmodel.ViewModelFactory
import com.mobile.tugasrancangmoka.viewmodel.VoidResult
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TransactionVM
    private var currentFilter: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apiService = ApiClient.getApiService(requireContext())
        val repository = TransactionRepo(apiService)
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TransactionVM::class.java]

        setupRecyclerView()
        setupFilters()
        observeViewModel()

        viewModel.fetchTransactionHistory(currentFilter)
    }

    private fun setupRecyclerView() {
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupFilters() {
        binding.btnFilterAll.setOnClickListener {
            updateFilterButtons(binding.btnFilterAll)
            currentFilter = null
            viewModel.fetchTransactionHistory(currentFilter)
        }
        binding.btnFilterCompleted.setOnClickListener {
            updateFilterButtons(binding.btnFilterCompleted)
            currentFilter = "completed"
            viewModel.fetchTransactionHistory(currentFilter)
        }
        binding.btnFilterVoided.setOnClickListener {
            updateFilterButtons(binding.btnFilterVoided)
            currentFilter = "voided"
            viewModel.fetchTransactionHistory(currentFilter)
        }
    }

    private fun updateFilterButtons(selectedButton: MaterialButton) {
        val buttons = listOf(binding.btnFilterAll, binding.btnFilterCompleted, binding.btnFilterVoided)
        for (btn in buttons) {
            if (btn == selectedButton) {
                btn.setTextColor(requireContext().getColor(R.color.primary))
                btn.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                btn.setTextColor(requireContext().getColor(R.color.on_surface_variant))
                btn.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.historyState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is HistoryResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.textEmptyState.visibility = View.GONE
                }
                is HistoryResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (result.list.isEmpty()) {
                        binding.rvHistory.visibility = View.GONE
                        binding.textEmptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvHistory.visibility = View.VISIBLE
                        binding.textEmptyState.visibility = View.GONE
                        binding.rvHistory.adapter = HistoryAdapter(result.list) { record ->
                            viewModel.fetchTransactionDetail(record.id)
                        }
                    }
                }
                is HistoryResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.textEmptyState.visibility = View.VISIBLE
                    binding.textEmptyState.text = result.message
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        viewModel.detailState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is DetailResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is DetailResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    showTransactionDetailDialog(result.record)
                }
                is DetailResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        viewModel.voidState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is VoidResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is VoidResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Transaction voided successfully", Toast.LENGTH_SHORT).show()
                    viewModel.fetchTransactionHistory(currentFilter)
                }
                is VoidResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showTransactionDetailDialog(record: TransactionRecord) {
        val dialogBinding = DialogTransactionDetailBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.textDetailCode.text = record.transactionCode

        // Format Date
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val date = parser.parse(record.createdAt)
            dialogBinding.textDetailDate.text = formatter.format(date!!)
        } catch (e: Exception) {
            dialogBinding.textDetailDate.text = record.createdAt
        }

        // Format currencies
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        currencyFormatter.maximumFractionDigits = 0

        dialogBinding.textDetailTotal.text = currencyFormatter.format(record.totalAmount)
        dialogBinding.textDetailDiscount.text = currencyFormatter.format(record.discountAmount)
        dialogBinding.textDetailGrandTotal.text = currencyFormatter.format(record.grandTotal)

        // Status styling
        if (record.status == "voided") {
            dialogBinding.textDetailStatus.text = "Voided"
            dialogBinding.textDetailStatus.setBackgroundResource(R.drawable.bg_status_voided)
            dialogBinding.btnVoidTransaction.visibility = View.GONE
            dialogBinding.layoutVoidInfo.visibility = View.VISIBLE
            dialogBinding.textDetailVoidReason.text = record.voidReason ?: "No reason given"
        } else {
            dialogBinding.textDetailStatus.text = "Completed"
            dialogBinding.textDetailStatus.setBackgroundResource(R.drawable.bg_status_completed)
            dialogBinding.btnVoidTransaction.visibility = View.VISIBLE
            dialogBinding.layoutVoidInfo.visibility = View.GONE
        }

        // Populate Items programmatically
        dialogBinding.layoutDetailItems.removeAllViews()
        val items = record.items.orEmpty()
        for (item in items) {
            val itemView = LayoutInflater.from(requireContext())
                .inflate(android.R.layout.simple_list_item_2, dialogBinding.layoutDetailItems, false)
            val text1 = itemView.findViewById<TextView>(android.R.id.text1)
            val text2 = itemView.findViewById<TextView>(android.R.id.text2)

            text1.text = "${item.productName} (x${item.quantity})"
            text2.text = currencyFormatter.format(item.subtotal)
            text1.setTextColor(requireContext().getColor(R.color.on_surface))
            text2.setTextColor(requireContext().getColor(R.color.primary))

            dialogBinding.layoutDetailItems.addView(itemView)
        }

        dialogBinding.btnVoidTransaction.setOnClickListener {
            dialog.dismiss()
            showVoidConfirmationDialog(record.id)
        }

        dialog.show()
    }

    private fun showVoidConfirmationDialog(transactionId: Int) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Void Transaction")
        builder.setMessage("Please enter the reason for canceling this transaction:")

        val input = EditText(requireContext())
        input.hint = "Wrong order / customer request"
        builder.setView(input)

        builder.setPositiveButton("Void") { dialog, _ ->
            val reason = input.text.toString().trim()
            if (reason.isNotEmpty()) {
                viewModel.voidTransaction(transactionId, reason)
            } else {
                Toast.makeText(requireContext(), "Reason cannot be empty", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Inner Adapter untuk History List
    private class HistoryAdapter(
        private val list: List<TransactionRecord>,
        private val onClick: (TransactionRecord) -> Unit
    ) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textCode: TextView = view.findViewById(R.id.text_history_code)
            val textDate: TextView = view.findViewById(R.id.text_history_date)
            val textTotal: TextView = view.findViewById(R.id.text_history_total)
            val textStatus: TextView = view.findViewById(R.id.text_history_status)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_history, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.textCode.text = item.transactionCode

            // Format date
            try {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                val date = parser.parse(item.createdAt)
                holder.textDate.text = formatter.format(date!!)
            } catch (e: Exception) {
                holder.textDate.text = item.createdAt
            }

            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            currencyFormatter.maximumFractionDigits = 0
            holder.textTotal.text = currencyFormatter.format(item.grandTotal)

            if (item.status == "voided") {
                holder.textStatus.text = "Voided"
                holder.textStatus.setBackgroundResource(R.drawable.bg_status_voided)
            } else {
                holder.textStatus.text = "Completed"
                holder.textStatus.setBackgroundResource(R.drawable.bg_status_completed)
            }

            holder.itemView.setOnClickListener {
                onClick(item)
            }
        }

        override fun getItemCount() = list.size
    }
}
