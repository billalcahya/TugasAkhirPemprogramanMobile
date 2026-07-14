package com.mobile.tugasrancangmoka.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.mobile.tugasrancangmoka.R
import com.mobile.tugasrancangmoka.api.ApiClient
import com.mobile.tugasrancangmoka.databinding.FragmentReportBinding
import com.mobile.tugasrancangmoka.repository.ReportRepo
import com.mobile.tugasrancangmoka.viewmodel.ReportResult
import com.mobile.tugasrancangmoka.viewmodel.ReportVM
import com.mobile.tugasrancangmoka.viewmodel.ViewModelFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ReportVM
    private var isDailyMode = true
    private var selectedDate: Date = Date()
    private val dailyDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val monthlyDateFormatter = SimpleDateFormat("yyyy-MM", Locale.US)

    private val displayDailyFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val displayMonthlyFormatter = SimpleDateFormat("MMM yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apiService = ApiClient.getApiService(requireContext())
        val repository = ReportRepo(apiService)
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ReportVM::class.java]

        setupToggle()
        setupDatePicker()
        observeViewModel()
        loadReport()
    }

    private fun setupToggle() {
        binding.toggleGroupReport.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isDailyMode = checkedId == R.id.btn_toggle_daily
                updatePeriodButtonText()
                loadReport()
            }
        }
    }

    private fun setupDatePicker() {
        binding.btnSelectPeriod.setOnClickListener {
            val builder = MaterialDatePicker.Builder.datePicker()
            builder.setTitleText(if (isDailyMode) "Select Date" else "Select Month")
            val picker = builder.build()
            picker.addOnPositiveButtonClickListener { selection ->
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = selection
                selectedDate = calendar.time
                updatePeriodButtonText()
                loadReport()
            }
            picker.show(parentFragmentManager, "DatePicker")
        }
        updatePeriodButtonText()
    }

    private fun updatePeriodButtonText() {
        val text = if (isDailyMode) {
            displayDailyFormatter.format(selectedDate)
        } else {
            displayMonthlyFormatter.format(selectedDate)
        }
        binding.btnSelectPeriod.text = text
    }

    private fun loadReport() {
        if (isDailyMode) {
            val dateStr = dailyDateFormatter.format(selectedDate)
            viewModel.fetchDailyReport(dateStr)
        } else {
            val monthStr = monthlyDateFormatter.format(selectedDate)
            viewModel.fetchMonthlyReport(monthStr)
        }
    }

    private fun observeViewModel() {
        viewModel.reportState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ReportResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.cardChart.visibility = View.INVISIBLE
                }
                is ReportResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.cardChart.visibility = View.VISIBLE

                    val reportList = result.response.data
                    if (!reportList.isNullOrEmpty()) {
                        val totalRevenue = reportList.sumOf { it.totalRevenue }
                        val grossProfit = reportList.sumOf { it.grossProfit }

                        val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
                        formatter.maximumFractionDigits = 0
                        binding.textReportRevenue.text = formatter.format(totalRevenue)
                        binding.textReportMargin.text = formatter.format(grossProfit)

                        setupChart(reportList)
                    } else {
                        binding.textReportRevenue.text = "Rp0"
                        binding.textReportMargin.text = "Rp0"
                        binding.reportChart.clear()
                    }
                }
                is ReportResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.cardChart.visibility = View.INVISIBLE
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupChart(reportList: List<com.mobile.tugasrancangmoka.model.ReportData>) {
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        for ((index, report) in reportList.withIndex()) {
            entries.add(Entry(index.toFloat(), report.totalRevenue.toFloat()))

            val label = if (isDailyMode) {
                report.date ?: "-"
            } else {
                report.month ?: "-"
            }
            labels.add(label)
        }

        val dataSet = LineDataSet(entries, "Revenue Trend").apply {
            color = requireContext().getColor(R.color.primary)
            valueTextColor = requireContext().getColor(R.color.primary)
            setDrawCircles(true)
            setDrawValues(false)
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(requireContext().getColor(R.color.primary))
            setDrawFilled(true)
            fillColor = requireContext().getColor(R.color.primary)
            fillAlpha = 30
        }

        binding.reportChart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                isGranularityEnabled = true
                setDrawGridLines(false)
                textColor = requireContext().getColor(R.color.on_surface_variant)
            }

            axisLeft.apply {
                setDrawGridLines(true)
                textColor = requireContext().getColor(R.color.on_surface_variant)
            }

            axisRight.isEnabled = false

            animateX(500)
            invalidate()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
