package com.app.presentation.adapter

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.domain.Portfolio
import com.app.presentation.databinding.ItemHoldingBinding
import java.text.NumberFormat
import java.util.Locale

class PortfolioAdapter : RecyclerView.Adapter<PortfolioAdapter.HoldingViewHolder>() {

    private var holdings: List<Portfolio> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HoldingViewHolder {
        val binding = ItemHoldingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HoldingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HoldingViewHolder, position: Int) {
        val holding = holdings[position]
        holder.bind(holding)
    }

    override fun getItemCount(): Int {
        return holdings.size
    }

    fun submitList(newHoldings: List<Portfolio>) {
        holdings = newHoldings
        notifyDataSetChanged()
    }

    class HoldingViewHolder(private val binding: ItemHoldingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(holding: Portfolio) {
            binding.symbolTextView.text = holding.symbol
            setSpannableText(binding.quantityTextView, "NET QTY: ", holding.quantity.toString())
            setSpannableText(binding.ltpTextView, "LTP: ", formatCurrency(holding.ltp))

            val pnl = holding.profitAndLoss
            val pnlColor = if (pnl >= 0) Color.GREEN else Color.RED
            setSpannableText(binding.pnlTextView, "P&L: ", formatCurrency(pnl), pnlColor)
        }

        private fun setSpannableText(
            textView: android.widget.TextView,
            label: String,
            value: String,
            valueColor: Int? = null
        ) {
            val fullText = label + value
            val spannable = SpannableString(fullText)

            spannable.setSpan(
                ForegroundColorSpan(Color.LTGRAY),
                0,
                label.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (valueColor != null) {
                spannable.setSpan(
                    ForegroundColorSpan(valueColor),
                    label.length,
                    fullText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            textView.text = spannable
        }

        private fun formatCurrency(value: Double): String {
            val indianLocale = Locale("en", "IN")
            val formatter = NumberFormat.getCurrencyInstance(indianLocale)
            return formatter.format(value)
        }
    }
}
