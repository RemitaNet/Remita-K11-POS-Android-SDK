package com.systemspecs.k11demo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.systemspecs.remita.processor.model.TransactionDetailsResponseData

class TransactionDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_detail)

        val transactionDetails = intent.getSerializableExtra("transactionDetails") as TransactionDetailsResponseData

        val tvTransactionRef = findViewById<TextView>(R.id.tvTransactionRef)
        val tvTransactionRRR = findViewById<TextView>(R.id.tvTransactionRRR)
        val tvTransactionAmount = findViewById<TextView>(R.id.tvTransactionAmount)
        val tvTransactionStatus = findViewById<TextView>(R.id.tvTransactionStatus)
        val tvTransactionDate = findViewById<TextView>(R.id.tvTransactionDate)

        tvTransactionRef.text = "Transaction Ref: ${transactionDetails.transactionRef}"
        tvTransactionRRR.text = "RRR: ${transactionDetails.rrr}"
        tvTransactionAmount.text = "Amount: ${transactionDetails.transactionAmount} ${transactionDetails.currencyCode}"
        tvTransactionStatus.text = "Status: ${transactionDetails.transactionStatus}"
        tvTransactionDate.text = "Date: ${transactionDetails.transactionDate}"

    }
}
