package com.app.data.source.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Portfolios.db"
    }

    override fun onCreate(p0: SQLiteDatabase) {
        val sql = "CREATE TABLE ${DbContract.PortfolioEntry.TABLE_NAME} (" +
                "${DbContract.PortfolioEntry.COLUMN_NAME_SYMBOL} TEXT PRIMARY KEY," +
                "${DbContract.PortfolioEntry.COLUMN_NAME_QUANTITY} INTEGER NOT NULL," +
                "${DbContract.PortfolioEntry.COLUMN_NAME_LTP} REAL NOT NULL," +
                "${DbContract.PortfolioEntry.COLUMN_NAME_AVG_PRICE} REAL NOT NULL," +
                "${DbContract.PortfolioEntry.COLUMN_NAME_CLOSE} REAL NOT NULL)"
        p0.execSQL(sql)
    }

    override fun onUpgrade(p0: SQLiteDatabase, p1: Int, p2: Int) {
        p0.execSQL("DROP TABLE IF EXISTS ${DbContract.PortfolioEntry.TABLE_NAME}")
        onCreate(p0)
    }
}