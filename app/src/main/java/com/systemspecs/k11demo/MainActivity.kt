package com.systemspecs.k11demo

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.compose.material3.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.systemspecs.remita.commons.CardAccountType
import com.systemspecs.remita.device.CombBitmap
import com.systemspecs.remita.device.GenerateBitmap
import com.systemspecs.remita.processor.device.CardTransactionResponse
import com.systemspecs.remita.processor.device.EnvType
import com.systemspecs.remita.processor.device.RemitaCardTransactionListener
import com.systemspecs.remita.processor.device.RemitaK11
import com.systemspecs.remita.processor.device.RemitaK11.Companion.getSerialNumber
import com.systemspecs.remita.processor.device.TransactionState
import com.systemspecs.remita.processor.device.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : FragmentActivity(), RemitaCardTransactionListener {
   private val apiKey =
       "remi_test_sk_YVZ6OXpSVmVaam0rOVVyTE1oVGRHajlMTjVHOE4yS0hoOWxaVkE9PTA3YmZhOWZhNzg4YjIyZDE2YWZiMDkzNjlmNWZmZDc0NzVkZTAwOGM3YTEzNzI0MTM0MDdmZTU4YTJhZjcwOGM="

    private val remitaK11 by lazy {
        RemitaK11(
            this,
            EnvType.Test,
            apiKey = apiKey,
        )
    }
    private var progressDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        remitaK11.setRemitaCardTransactionListener(this)

        val logo: ImageView = findViewById(R.id.logo)
        val amountInput: EditText = findViewById(R.id.amountInput)
        val readCardButton: Button = findViewById(R.id.buttonReadCard)
        val keyExchangeButton: Button = findViewById(R.id.buttonKeyExchange)
        val getTransactionsButton: Button = findViewById(R.id.transactionsList)
        val getTransactionDetailButton: Button = findViewById(R.id.transactionDetails)
        val getSerialNumberButton: Button = findViewById(R.id.buttonGetSerial)

        val printButton: Button = findViewById(R.id.buttonPrint)

        readCardButton.setOnClickListener {
            val amountInNaira = amountInput.text.toString().toDoubleOrNull()
            if (amountInNaira != null) {
                val amountInKobo = (amountInNaira * 100).toInt()
                showProgressDialog("Processing card transaction...")
                processCardTransaction(amountInKobo)
            } else {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }

        keyExchangeButton.setOnClickListener {
            keyExchange()
        }
        getTransactionsButton.setOnClickListener {
            getTransactions()
        }

        getTransactionDetailButton.setOnClickListener {
            getTransactionDetail(transRef = "CGN8lG43hT")
        }

        printButton.setOnClickListener {
            devicePrintingTest()
        }

        getSerialNumberButton.setOnClickListener {
           val serial = getSerialNumber()
            Toast.makeText(this, "Serial Number: $serial", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showProgressDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.progress_dialog, null)
        val messageTextView = dialogView.findViewById<TextView>(R.id.progressMessage)
        messageTextView.text = message
        builder.setView(dialogView)
        builder.setCancelable(false)
        progressDialog = builder.create()
        progressDialog?.show()
    }

    fun generateRandomRef(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")

    }

    private fun processCardTransaction(amountInKobo: Int) {

        lifecycleScope.launch {

            withContext(Dispatchers.IO) {
                remitaK11.processCardTransaction(
                    amountInKobo = amountInKobo.toLong(),
                    transRef = generateRandomRef(10),
                    currencyCode = "NGN",
                    transactionType = TransactionType.purchase,
                    transactionDescription = "Purchase of TV",
                    accountType = CardAccountType.DEFAULT
                )
            }
            progressDialog?.dismiss()
        }
    }


    private fun keyExchange() {
        lifecycleScope.launch {
            showProgressDialog("Performing key exchange...")
//            withContext(Dispatchers.IO) {
//                remitaK11.keyExchange()
//                Log.d("remitak11", "Key Exchange executed")
//            }

            val keyExchangeResponse = withContext(Dispatchers.IO) {
                remitaK11.keyExchange()
            }
            progressDialog?.dismiss()
            Log.d("remitak11", "Key Exchange executed")
            Log.d("Key Exchange Response", keyExchangeResponse.toString())
        }
    }


    private fun getTransactions() {
        lifecycleScope.launch {
            showProgressDialog("Fetching transactions...")
            val transactionResponse = withContext(Dispatchers.IO) {
                remitaK11.getTransactions()
            }
            progressDialog?.dismiss()
            Log.d("remitak11", "Transactions executed")
            Log.d("Transactions Lists", transactionResponse.toString())
            if (transactionResponse.data?.items?.isNotEmpty() == true) {
                val intent =
                    Intent(applicationContext, TransactionHistoryActivity::class.java).apply {
                        putExtra("transactionList", ArrayList(transactionResponse.data!!.items))
                    }
                applicationContext.startActivity(intent)
            }
        }
    }

    private fun getTransactionDetail(transRef: String) {
        lifecycleScope.launch {
            showProgressDialog("Fetching transaction details...")
            val transactionDetailsResponse = withContext(Dispatchers.IO) {
                remitaK11.getTransactionDetail(transRef = transRef)
            }
            Log.d("remitak11", "Transaction Detail executed")
            if (transactionDetailsResponse != null) {
                Log.d("Transaction Details", transactionDetailsResponse.toString())
                // Create and display the alert dialog
                val dialogView = LayoutInflater.from(this@MainActivity)
                    .inflate(R.layout.activity_transaction_detail, null)
                // Populate the dialog with transaction details
                val tvTransactionRef = dialogView.findViewById<TextView>(R.id.tvTransactionRef)
                val tvTransactionRRR = dialogView.findViewById<TextView>(R.id.tvTransactionRRR)
                val tvTransactionAmount =
                    dialogView.findViewById<TextView>(R.id.tvTransactionAmount)
                val tvTransactionStatus =
                    dialogView.findViewById<TextView>(R.id.tvTransactionStatus)
                val tvTransactionDate = dialogView.findViewById<TextView>(R.id.tvTransactionDate)
                val transactionDetails = transactionDetailsResponse.data
                tvTransactionRef.text = "Transaction Ref: ${transactionDetails?.transactionRef}"
                tvTransactionRRR.text = "RRR: ${transactionDetails?.rrr}"
                tvTransactionAmount.text =
                    "Amount: ${transactionDetails?.transactionAmount} ${transactionDetails?.currencyCode}"
                tvTransactionStatus.text = "Status: ${transactionDetails?.transactionStatus}"
                tvTransactionDate.text = "Date: ${transactionDetails?.transactionDate}"
                // Show the dialog
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Transaction Details")
                    .setView(dialogView)
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .setOnDismissListener { progressDialog?.dismiss() } // Dismiss progress dialog when AlertDialog is dismissed
                    .show()
            } else {
                progressDialog?.dismiss() // Dismiss progress dialog if transactionDetailsResponse is null
            }
        }
    }

    private fun responsePrinting(transactionData: Map<String, String>) {
        lifecycleScope.launch {
            showProgressDialog("Printing...")
            val jsonData =
                buildJsonString(transactionData) // Convert transaction data to JSON format
            val bitmap = withContext(Dispatchers.Default) {
                generateBitmap(this@MainActivity, jsonData)
            }
            remitaK11.print(img = bitmap, callBack = {
                Log.d("PRINT:", it)
                progressDialog?.dismiss()
            })
        }
    }

    private fun buildJsonString(data: Map<String, String>): String {
        return data.entries.joinToString(
            separator = ",\n",
            prefix = "{\n",
            postfix = "\n}"
        ) { "\"${it.key}\": \"${it.value}\"" }
    }


    private fun devicePrintingTest() {
        lifecycleScope.launch {
            showProgressDialog("Printing...")
            val bitmap =
                withContext(Dispatchers.Default) { // Use Dispatchers.Default for bitmap generation
                    generateBitmap(
                        this@MainActivity,
                        """
                {
                    "Name": "John Doe",
                    "Location": "Sample Location",
                    "Terminal ID:": "123456",
                    "Merchant ID:": "987654",
                    "Product Service": "Sample Product Service",
                    "Date:": "2023-07-26",
                    "Time:": "12:34:56",
                    "Trans Type:": "Purchase",
                    "RRR:": "654321",
                    "Ext Trans Ref:": "EXT12345",
                    "Card No:": "1234 5678 9012 3456",
                    "Card Holder Name:": "John Doe",
                    "EXP. DATE:": "12/25",
                    "CVM:": "PIN",
                    "RRN:": "654321789",
                    "STAN:": "123456",
                    "Auth Code:": "AUTH123",
                    "Amount": "100.00",
                    "DSCP:": "Sample Description",
                    "RSP CODE:": "00",
                    "Trans Status:": "Approved"
                }
            """
                    )
                }
            remitaK11.print(img = bitmap, callBack = {
                Log.d("PRINT:", it)
                progressDialog?.dismiss()
            })
        }
    }


    fun generateBitmap(
        context: Context,
        hashMap: String,
    ): Bitmap {
        val combBitmap = CombBitmap()
        combBitmap.addBitmap(GenerateBitmap.generateLine(1))

        val trimmedString = hashMap.replace("\n", "")

        JSONObject(trimmedString).toMap().forEach {
            when (it.key) {
                "Card Holder Name:", "Merchant Name:", "Name", "Location", "Ext Trans Ref:", "Product/Service:" -> {
                    combBitmap.addBitmap(
                        GenerateBitmap.str2Bitmap(
                            it.key,
                            16,
                            GenerateBitmap.AlignEnum.LEFT,
                            true,
                            false
                        )
                    )
                    combBitmap.addBitmap(
                        GenerateBitmap.str2Bitmap(
                            it.value,
                            16,
                            GenerateBitmap.AlignEnum.RIGHT,
                            true,
                            false
                        )
                    )
                }

                "STATUS" -> {
                    combBitmap.addBitmap(
                        GenerateBitmap.str2Bitmap(
                            it.value,
                            16,
                            GenerateBitmap.AlignEnum.CENTER,
                            true,
                            false
                        )
                    )
                }

                "DSCP:", "DSCP", "Response Code:", "Trans Status:", "RSP CODE:", "OWNER" -> {
                }

                else -> {
                    combBitmap.addBitmap(
                        GenerateBitmap.str2Bitmap(
                            it.key,
                            it.value,
                            16,
                            true,
                            false
                        )
                    )
                }
            }

        }
        JSONObject(trimmedString).toMap().forEach {
            when (it.key) {
                "DSCP:" -> {
                    combBitmap.addBitmap(
                        GenerateBitmap.str2Bitmap(
                            "----------------------------------------------------------------",
                            16,
                            GenerateBitmap.AlignEnum.CENTER,
                            true,
                            false
                        )
                    )
                    combBitmap.addBitmap(
                        GenerateBitmap.str2Bitmap(
                            it.value,
                            16,
                            GenerateBitmap.AlignEnum.CENTER,
                            true,
                            false
                        )
                    )
                    combBitmap.addBitmap(
                        GenerateBitmap.str2Bitmap(
                            "----------------------------------------------------------------",
                            16,
                            GenerateBitmap.AlignEnum.CENTER,
                            true,
                            false
                        )
                    )
                }

                "Response Code:", "RSP CODE:", "Trans Status:" -> {
                    combBitmap.addBitmap(
                        GenerateBitmap.str2Bitmap(
                            it.key,
                            it.value,
                            16,
                            true,
                            false
                        )
                    )
                }
            }
        }
        combBitmap.addBitmap(
            GenerateBitmap.str2Bitmap(
                "----------------------------------------------------------------",
                14,
                GenerateBitmap.AlignEnum.CENTER,
                true,
                false
            )
        )


        combBitmap.addBitmap(
            GenerateBitmap.str2Bitmap(
                //"Remita POS ${BuildConfig.VERSION_NAME}",
                "POS",
                16,
                GenerateBitmap.AlignEnum.RIGHT,
                true,
                false
            )
        )
        combBitmap.addBitmap(GenerateBitmap.generateGap(60))
        return combBitmap.combBitmap
        // }
    }

    fun JSONObject.toMap(): LinkedHashMap<String, String> {
        val map = linkedMapOf<String, String>()
        val keysItr: Iterator<String> = this.keys()
        while (keysItr.hasNext()) {
            val key = keysItr.next()
            var value: Any = this.get(key)
            when (value) {
                is JSONArray -> value = value.toList()
                is JSONObject -> value = value.toMap()
            }
            map[key] = value.toString()
        }
        return map
    }

    fun JSONArray.toList(): List<Any> {
        val list = mutableListOf<Any>()
        for (i in 0 until this.length()) {
            var value: Any = this[i]
            when (value) {
                is JSONArray -> value = value.toList()
                is JSONObject -> value = value.toMap()
            }
            list.add(value)
        }
        return list
    }

    override fun onRemitaCardTransactionResponse(response: CardTransactionResponse) {
        this.runOnUiThread {
            when (response.transactionState) {

                TransactionState.DONE_PROCESSING -> {
                    Toast.makeText(this, "Done Processing.", Toast.LENGTH_SHORT).show()
                    progressDialog?.dismiss()

                }

                TransactionState.INSERT_CARD -> {
                    Toast.makeText(this, "Please insert your card.", Toast.LENGTH_SHORT).show()
                }

                TransactionState.CARD_INSERTED -> {
                    Log.d("CARD_INSERTED", "Card inserted, please enter PIN.")
                }

                TransactionState.READ_CARD_FAILED -> {
                    Toast.makeText(this, "Failed to read card.", Toast.LENGTH_SHORT).show()
                    progressDialog?.dismiss()
                }

                TransactionState.FAILED -> {
                    Toast.makeText(this, "Transaction failed.", Toast.LENGTH_SHORT).show()
                }

                TransactionState.TRANSACTION_SUCCESSFUL -> {
                    progressDialog?.dismiss()
                    Log.d("TRANSACTION_SUCCESSFUL", response.transactionData.toString())

                    responsePrinting(
                        mapOf(
                            "Name" to (response.transactionData?.name ?: "N/A").toString(),
                            "Location" to (response.transactionData?.location ?: "N/A").toString(),
                            "Terminal ID:" to (response.transactionData?.terminalId
                                ?: "N/A").toString(),
                            "Merchant ID:" to (response.transactionData?.merchantId
                                ?: "N/A").toString(),
                            "DSCP:" to (response.transactionData?.description ?: "N/A").toString(),
                            "Date:" to (response.transactionData?.dateTime?.split(" ")?.get(0)
                                ?: "N/A").toString(),
                            "Time:" to (response.transactionData?.dateTime?.split(" ")?.get(1)
                                ?: "N/A").toString(),
                            "Trans Type:" to (response.transactionData?.transactionType
                                ?: "N/A").toString(),
                            "RRN:" to (response.transactionData?.rrn ?: "N/A").toString(),
                            "Card No:" to (response.transactionData?.cardNumber
                                ?: "N/A").toString(),
                            "Card Holder Name" to (response.transactionData?.cardHolderName
                                ?: "N/A").toString(),
                            "EXP. DATE:" to (response.transactionData?.cardExpireDate
                                ?: "N/A").toString(),
                            "CVM:" to (response.transactionData?.cvm ?: "N/A").toString(),
                            "STAN:" to (response.transactionData?.stan ?: "N/A").toString(),
                            "Auth Code:" to (response.transactionData?.authCode
                                ?: "N/A").toString(),
                            "Amount:" to (response.transactionData?.amount ?: "N/A").toString(),
                            "Trans Status:" to "Approved"
                        )
                    )
                }

                TransactionState.TRANSACTION_NOT_SUCCESSFUL -> {
                    progressDialog?.dismiss()
                    Log.d("TRANSACTION_NOT_SUCCESSFUL", response.transactionData.toString())
                    Toast.makeText(this, "Transaction not successful.", Toast.LENGTH_SHORT).show()

                    responsePrinting(
                        mapOf(
                            "Name" to (response.transactionData?.name ?: "N/A").toString(),
                            "Location" to (response.transactionData?.location ?: "N/A").toString(),
                            "Terminal ID:" to (response.transactionData?.terminalId
                                ?: "N/A").toString(),
                            "Merchant ID:" to (response.transactionData?.merchantId
                                ?: "N/A").toString(),
                            "DSCP:" to (response.transactionData?.description ?: "N/A").toString(),
                            "Date:" to (response.transactionData?.dateTime?.split(" ")?.get(0)
                                ?: "N/A").toString(),
                            "Time:" to (response.transactionData?.dateTime?.split(" ")?.get(1)
                                ?: "N/A").toString(),
                            "Trans Type:" to (response.transactionData?.transactionType
                                ?: "N/A").toString(),
                            "RRN:" to (response.transactionData?.rrn ?: "N/A").toString(),
                            "Card No:" to (response.transactionData?.cardNumber
                                ?: "N/A").toString(),
                            "Card Holder Name" to (response.transactionData?.cardHolderName
                                ?: "N/A").toString(),
                            "EXP. DATE:" to (response.transactionData?.cardExpireDate
                                ?: "N/A").toString(),
                            "CVM:" to (response.transactionData?.cvm ?: "N/A").toString(),
                            "STAN:" to (response.transactionData?.stan ?: "N/A").toString(),
                            "Auth Code:" to (response.transactionData?.authCode
                                ?: "N/A").toString(),
                            "Amount:" to (response.transactionData?.amount ?: "N/A").toString(),
                            "Trans Status:" to "Failed"
                        )
                    )
                }

                else -> {
                    Toast.makeText(this, "Transaction not successful.", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }
}
