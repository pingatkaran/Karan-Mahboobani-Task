package com.app.data.source.local

import android.provider.BaseColumns

object DbContract {
    object PortfolioEntry : BaseColumns {
        const val TABLE_NAME = "portfolios"
        const val COLUMN_NAME_SYMBOL = "symbol"
        const val COLUMN_NAME_QUANTITY = "quantity"
        const val COLUMN_NAME_LTP = "ltp"
        const val COLUMN_NAME_AVG_PRICE = "avgPrice"
        const val COLUMN_NAME_CLOSE = "close"
    }
}