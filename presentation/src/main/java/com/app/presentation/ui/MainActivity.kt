package com.app.presentation.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.presentation.R
import com.app.presentation.adapter.PortfolioAdapter
import com.app.presentation.databinding.ActivityMainBinding
import com.app.presentation.model.PortfolioUiModel
import com.app.presentation.viewmodel.PortfolioViewModel
import com.app.presentation.viewmodel.UiState
import com.app.presentation.viewmodel.ViewModelFactoryProvider
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val portfolioAdapter = PortfolioAdapter()
    private var isSummaryExpanded = false

    private val viewModel: PortfolioViewModel by viewModels {
        (application as ViewModelFactoryProvider).providePortfolioViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        setupToolbar()
        setupWindowInsets()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }

    private fun setupRecyclerView() {
        binding.holdingsRecyclerView.apply {
            adapter = portfolioAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun setupClickListeners() {
        binding.expanderIcon.setOnClickListener {
            toggleSummaryExpansion()
        }
    }

    private fun toggleSummaryExpansion() {
        isSummaryExpanded = !isSummaryExpanded

        val visibility = if (isSummaryExpanded) View.VISIBLE else View.GONE
        val iconRes = if (isSummaryExpanded) R.drawable.ic_expand else R.drawable.ic_collapse

        binding.currentValueLabel.visibility = visibility
        binding.currentValueAmount.visibility = visibility
        binding.totalInvestmentLabel.visibility = visibility
        binding.totalInvestmentAmount.visibility = visibility
        binding.divider.visibility = visibility
        binding.todaysPnlLabel.visibility = visibility
        binding.todaysPnlAmount.visibility = visibility

        binding.expanderIcon.setImageResource(iconRes)
    }

    private fun observeViewModel() {
        viewModel.portfolioData.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.summaryCard.visibility = View.GONE
                    binding.holdingsRecyclerView.visibility = View.GONE
                    binding.errorTextView.visibility = View.GONE
                }
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.summaryCard.visibility = View.VISIBLE
                    binding.holdingsRecyclerView.visibility = View.VISIBLE
                    binding.errorTextView.visibility = View.GONE
                    updateUi(state.data)
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.summaryCard.visibility = View.GONE
                    binding.holdingsRecyclerView.visibility = View.GONE
                    binding.errorTextView.visibility = View.VISIBLE
                    binding.errorTextView.text = state.message
                }
            }
        }
    }

    private fun updateUi(uiModel: PortfolioUiModel) {
        binding.currentValueAmount.text = formatCurrency(uiModel.totalCurrentValue)
        binding.totalInvestmentAmount.text = formatCurrency(uiModel.totalInvestment)
        updatePnlTextView(binding.todaysPnlAmount, uiModel.todaysPandL)
        val totalPnlText = getString(
            R.string.pnl_with_percentage_format,
            formatCurrency(uiModel.totalPandL),
            uiModel.totalPandLPercentage
        )
        binding.totalPnlAmount.text = totalPnlText

        val pnlColorRes = if (uiModel.totalPandL >= 0) Color.GREEN else Color.RED
        binding.totalPnlAmount.setTextColor(pnlColorRes)

        portfolioAdapter.submitList(uiModel.holdings)
    }

    private fun updatePnlTextView(textView: TextView, value: Double) {
        textView.text = formatCurrency(value)
        val pnlColorRes = if (value >= 0) Color.GREEN else Color.RED
        textView.setTextColor(pnlColorRes)
    }

    private fun formatCurrency(value: Double): String {
        val indianLocale = Locale("en", "IN")
        val formatter = NumberFormat.getCurrencyInstance(indianLocale)
        return formatter.format(value)
    }
}
