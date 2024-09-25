package com.systemspecs.k11demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.systemspecs.remita.processor.model.TransactionDetailsResponseData

class TransactionHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)

        val transactionList = intent.getSerializableExtra("transactionList") as ArrayList<TransactionDetailsResponseData>
        val listView = findViewById<ListView>(R.id.lvTransactionHistory)

        val adapter = object : ArrayAdapter<TransactionDetailsResponseData>(
            this,
            R.layout.item_transaction_history,
            transactionList
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_transaction_history, parent, false)
                val transaction = transactionList[position]

                val tvTransactionRef = view.findViewById<TextView>(R.id.tvTransactionRef)
                val tvTransactionAmount = view.findViewById<TextView>(R.id.tvTransactionAmount)
                val tvTransactionDate = view.findViewById<TextView>(R.id.tvTransactionDate)

                tvTransactionRef.text = "Transaction Ref: ${transaction.transactionRef}"
                tvTransactionAmount.text = "Amount: ${transaction.transactionAmount} ${transaction.currencyCode}"
                tvTransactionDate.text = "Date: ${transaction.transactionDate}"

                return view
            }
        }

        listView.adapter = adapter
    }
}

