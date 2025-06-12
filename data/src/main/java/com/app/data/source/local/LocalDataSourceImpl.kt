package com.app.data.source.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.app.domain.Portfolio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalDataSourceImpl(private val dbHelper: DbHelper) : LocalDataSource {

    override suspend fun getPortfolios(): List<Portfolio> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DbContract.PortfolioEntry.TABLE_NAME, null, null, null, null, null, null)
        val portfolios = mutableListOf<Portfolio>()
        with(cursor) {
            while (moveToNext()) {
                portfolios.add(
                    Portfolio(
                        symbol = getString(getColumnIndexOrThrow(DbContract.PortfolioEntry.COLUMN_NAME_SYMBOL)),
                        quantity = getInt(getColumnIndexOrThrow(DbContract.PortfolioEntry.COLUMN_NAME_QUANTITY)),
                        ltp = getDouble(getColumnIndexOrThrow(DbContract.PortfolioEntry.COLUMN_NAME_LTP)),
                        avgPrice = getDouble(getColumnIndexOrThrow(DbContract.PortfolioEntry.COLUMN_NAME_AVG_PRICE)),
                        close = getDouble(getColumnIndexOrThrow(DbContract.PortfolioEntry.COLUMN_NAME_CLOSE))
                    )
                )
            }
        }
        cursor.close()
        db.close()
        return@withContext portfolios
    }

    override suspend fun savePortfolios(portfolios: List<Portfolio>) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete(DbContract.PortfolioEntry.TABLE_NAME, null, null)

            portfolios.forEach { portfolio ->
                val values = ContentValues().apply {
                    put(DbContract.PortfolioEntry.COLUMN_NAME_SYMBOL, portfolio.symbol)
                    put(DbContract.PortfolioEntry.COLUMN_NAME_QUANTITY, portfolio.quantity)
                    put(DbContract.PortfolioEntry.COLUMN_NAME_LTP, portfolio.ltp)
                    put(DbContract.PortfolioEntry.COLUMN_NAME_AVG_PRICE, portfolio.avgPrice)
                    put(DbContract.PortfolioEntry.COLUMN_NAME_CLOSE, portfolio.close)
                }
                db.insertWithOnConflict(DbContract.PortfolioEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }
}
