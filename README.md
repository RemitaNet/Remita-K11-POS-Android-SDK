### REMITA K11 TECHNICAL DOCUMENTATION

This document outlines the steps involved in processing transactions using the Remita K11 terminal.
  
### **Steps**

1. **Initialize Remita K11:**  
   * Begin by setting up the Remita K11 terminal for transaction processing.  
2. **Process Card Transaction:**  
   * Call the `ProcessCardTransaction` method with the required inputs: `amount`, `transaction reference`, and `currency`.  
   * **Key Exchange:**  
     * Call the `keyExchange` method to retrieve the master and pin keys.  
     * Inject the master and pin keys into the terminal.  
     * Read the card details.  
3. **Request Card PIN:**  
   * Prompt the client to enter their card PIN.  
4. **Transaction Execution:**  
   * Remita processes the transaction and debits the card if the card details are valid.  
5. **Return Response:**  
   * Remita returns either a successful or a declined response, including the card details.

**Initialize RemitaK11**

```kotlin
val remitaK11 = RemitaK11(this, secretKey = "yourSecretKey")
//this-> The activity must extend FragmentActivity
//may throw a RuntimeException if secret key is not valid
//envType is an enum (Test, Live) that specifies an environment to be connected to.

```

## **Methods and Functions**

This section details the methods and functions used in the transaction process.

### **Get Device Serial Number**

* **Description:** Returns the serial number of the device. Returns an empty string if the app is running on a non-K11 device or if an error occurs.

**Usage:**
```kotlin
val serialNumber = RemitaK11.getSerialNumber()
```

**Key Exchange** 

* **Description:** Loads Remita master and pin keys into the K11 terminal.  
* **Method:** `keyExchange`

**Usage:**  
```Kotlin
val remitaK11 = RemitaK11(this, secretKey = "yourSecretKey")
```  
  * **Note:** The activity must extend `FragmentActivity`.  
  * **Exception:** May throw a `RuntimeException` if the secret key is not valid.

### **Process Card Transaction**

* **Description:** Reads card details and charges the card. The `keyExchange` method must be called before a card transaction. The `processCardTransaction` method will call `keyExchange` if it hasn't been invoked yet.  
* **Method:** `processCardTransaction`  
* **Parameters:**

| Parameter | Data Type | Additional Details | Description |
| :---- | :---- | :---- | :---- |
| amountInKobo | long |  | The amount to be charged from the card (in kobo) |
| transRef | String |  | Client’s unique identifier for the transaction |
| currencyCode | String |  | ISO 4271 (e.g “NGN”) |
| transactionType | TransactionType (enum) | ***billsPayment purchase*** | The type of transaction that is to be processed. |
| transactionDescription | String |  | A 50 character (max) description of the transaction |

**Usage:**

```Kotlin
remitaK11.processCardTransaction(  
    amountInKobo = 1000, //10 naira  
    transRef = "yourUniqueTransactionReference",  
    currencyCode = “NGN”,  
    transactionType = TransactionType.purchase  
    transactionDescription = “55 inches Smart tv purchase”  
)
```

Interface: **RemitaCardTransactionListener**

This interface provides updates regarding the transaction process.

* **Implementation Example:**

```Kotlin
class MainActivity : FragmentActivity(), RemitaCardTransactionListener {
	override fun onCreate(savedInstanceState: Bundle?) {    
 	super.onCreate(savedInstanceState)
  	val remitaK11 = RemitaK11(this, secretKey = "yourSecretKey")    
   	remitaK11.setRemitaCardTransactionListener(this)
	}
} 
```

* **Transaction Response Handling:**

```Kotlin
override fun onRemitaCardTransactionResponse(response: CardTransactionResponse) {
	this.runOnUiThread {
 		when (response.transactionState) {
   		TransactionState.INSERT_CARD -> {      
     		Toast.makeText(this, "Please insert your card.", Toast.LENGTH_SHORT).show()
       		}
	 	TransactionState.CARD_INSERTED -> {         
   		Log.d("CARD_INSERTED", "Card inserted, PIN fragment will load automatically")      
     		}
       		TransactionState.READ_CARD_FAILED -> {        
	 	Toast.makeText(this, "Failed to read card.", Toast.LENGTH_SHORT).show()     
   		}
     		TransactionState.FAILED -> {     
       		Toast.makeText(this, "Transaction failed.", Toast.LENGTH_SHORT).show()    
	 	}
   		TransactionState.TRANSACTION_SUCCESSFUL -> {      
     		Log.d("TRANSACTION_SUCCESSFUL", response.transactionData.toString())      
       		}
	 	TransactionState.TRANSACTION_NOT_SUCCESSFUL -> { 
   		Log.d("TRANSACTION_NOT_SUCCESSFUL", response.transactionData.toString())          
     		Toast.makeText(this, "Transaction not successful.", Toast.LENGTH_SHORT).show()         
       		}
	 	else -> {
   		Toast.makeText(this, "Transaction not successful.", Toast.LENGTH_SHORT).show()         
     		}
       	}
}
}
```

**CardTransactionResponse parameters**

This section describes the parameters included in the `CardTransactionResponse`.

| PARAMETERS | DATATYPE | Additional Details | Description |
| :---- | :---- | :---- | :---- |
| transactionState | Enum - TransactionState | **INSERT_CARD,** | Inform the user to insert a card.|
| | | **CARD_INSERTED**, | Card detected; a PIN dialog will appear automatically.|
| | | **READ_CARD_FAILED**,| An error occurred while reading the card. |
| | | **FAILED**, | An error occurred during the transaction. |
| | | **TRANSACTION_SUCCESSFUL**,| Card reading and transaction were successful. |
| | | **TRANSACTION_NOT_SUCCESSFUL** | Card reading was successful, but the transaction was not. |
| message | String |  |  |
| transactionData | ResponseData(nullable) |  |  |

**RESPONSEDATA parameters**  
responseData(Object) is expected to have data when the enum values TRANSACTION_SUCCESSFUL and TRANSACTION_NOT_SUCCESSFUL

| PARAMETERS | DATATYPE | Description |
| :---- | :---- | :---- |
| processMessage | String |  |
| description | String (nullable) |  |
| name | String (nullable) | Name of the merchant |
| location | String (nullable) | Merchant’s address |
| terminalId | String (nullable) | The terminal Id of the merchant |
| merchantId | String (nullable) | The merchant Id of the merchant |
| dateTime | String (nullable) | The date of time the transaction occurred |
| transactionType | String (nullable) |  |
| cardNumber | String (nullable) | The PAN of the card (some numbers are hidden) |
| cardHolderName | String (nullable) | The cardholder name |
| cardExpireDate | String (nullable) | The expire month and year of the card |
| cvm | String (nullable) | Cardholder Verification Method (ONLINE or OFFLINE) |
| rrn | String (nullable) |  |
| amount | String (nullable) | The amount of the transaction |
| processCode | String (nullable) | The status of the transaction |
| transactionReference | String (nullable) |  |
| stan | String (nullable) |  |
| authCode | String (nullable) |  |

**PROCESS CODE**  
The response code indicates the status of the transaction  
Below is a list of responseCode that can be returned with their description.

| Code | Description | Suggested Action |
| ----- | ----- | ----- |
| 00 | Approved or completed successfully | No action needed; proceed with confirmation. |
| 01 | Refer to card issuer | Prompt user to contact their card issuer. |
| 02 | Refer to card issuer special condition | Prompt user to contact their card issuer. |
| 03 | Invalid merchant | Verify merchant details and retry. |
| 04 | Pick-up card | Inform user to retrieve their card; do not retry. |
| 05 | Do not honour | Advice user to contact their bank. |
| 06 | Error | Retry transaction; log error details. |
| 07 | Pick-up card special condition | Inform user to retrieve their card; do not retry. |
| 08 | Honor with identification | Request additional identification from user. |
| 09 | Request in progress | Wait and retry the transaction. |
| 10 | Approved partial | Confirm partial transaction with user. |
| 11 | Approved VIP | No action needed; proceed with confirmation. |
| 12 | Invalid transaction | Check transaction details and retry. |
| 13 | Invalid amount | Verify amount details and retry. |
| 14 | Invalid card number | Verify card number and retry. |
| 15 | No such issuer | Prompt user to check with their bank. |
| 16 | Approved update track 3 | No action needed; proceed with confirmation. |
| 17 | Customer cancellation | Confirm cancellation with user. |
| 18 | Customer dispute | Advice users to contact their bank. |
| 19 | Re-enter transaction | Request user to re-enter transaction details. |
| 20 | Invalid response | Retry transaction; log error details. |
| 21 | No action taken | Retry transaction if necessary. |
| 22 | Suspected malfunction   |  Retry transaction; check system logs. |
| 23 | Unacceptable transaction fee | Inform user and review fee structure. |
| 24 | File update not supported |  Check system compatibility and retry. |
| 25 | Unable to locate record  | Verify details and retry transaction. |
| 26 | Duplicate record | Check for the existing transaction and confirm with the user.  |
| 27 |  File update edit error | Verify and retry file update process. |
| 28 | File update file locked | Retry after unlocking file. | 
| 29 | File update failed | Verify file details and retry. | 
| 30 | Format error | Check data format and retry. | 
| 31 | Bank not supported | Inform user to contact their bank. | 
| 32 | Completed partially | Confirm partial transaction with user. | 
| 33 | Expired card pick-up | Inform user to retrieve their card; do not retry. | 
| 34 | Suspected fraud pick-up | Advice user to contact their bank. | 
| 35 | Contact acquirer pick-up | Advice user to contact their bank. | 
| 36 | Restricted card pick-up | Advice user to contact their bank. | 
| 37 | Call acquirer security pick-up | Advice user to contact their bank. | 
| 38 | PIN tries exceeded pick-up | Inform user to retrieve their card; do not retry. | 
| 39 | No credit account | Advice user to check their account details. | 
| 40 | Function not supported | Inform user of unsupported function. | 
| 41 | Lost card | Advice user to contact their bank. | 
| 42 | No universal account | Verify account details with user. | 
| 43 | Stolen card | Advice user to contact their bank. | 
| 44 | No investment account | Verify account details with user. | 
| 51 | Not sufficient funds | Inform user of insufficient funds. | 
| 52 | No check account | Verify account details with user. | 
| 53 | No savings account | Verify account details with user. | 
| 54 | Expired card | Inform the user to check their card's expiration date. | 
| 55 | Incorrect PIN | Prompt user to re-enter PIN. | 
| 56 | No card record | Verify card details with user. | 
| 57 | Transaction not permitted to cardholder | Advice user to contact their bank. | 
| 58 | Transaction not permitted on terminal | Verify terminal settings and retry. | 
| 59 | Suspected fraud | Advice user to contact their bank. | 
| 60 | Contact acquirer | Advice users to contact their bank. | 
| 61 | Exceeds withdrawal limit | Inform user of withdrawal limit. | 
| 62 | Restricted card | Advice user to contact their bank. | 
| 63 | Security violation | Verify transaction security settings. | 
| 64 | Original amount incorrect | Verify transaction amount and retry. | 
| 65 | Exceeds withdrawal frequency | Inform user of frequency limit. | 
| 66 | Call acquirer security | Advice user to contact their bank. | 
| 67 | Hard capture | Advice user to contact their bank. | 
| 68 | Response received too late | Retry transaction. | 
| 75 | PIN tries exceeded | Inform user to contact their bank. | 
| 77 | Intervene bank approval required | Advice user to contact their bank. | 
| 78 | Intervene bank approval required for partial amount | Advice user to contact their bank. | 
| 90 | Cut-off in progress | Retry transaction later. | 
| 91 | Issuer or switch inoperative | Retry transaction later. | 
| 92 | Routing error | Check network and retry. | 
| 93 | Violation of law | Do not retry; report as needed. | 
| 94 | Duplicate transaction | Verify transaction status with user. | 
| 95 | Reconcile error | Retry reconciliation process. | 
| 96 | System malfunction | Check system and retry transaction. | 
| 196 | REM TMS System malfunction | Check system and retry transaction. | 
| 98 | Exceeds cash limit | Inform user of cash limit. | 
| 99 | No response received or Timed-out | Retry transaction. | 
| 100 | No response received or Timed-out | Retry transaction. | 
| 101 | Terminal configuration error | Verify terminal configuration. | 
| 102 | Terminal not enabled | Check terminal status and enable. | 
| 103 | Unknown terminal | Verify terminal identity. | 
| 104 | Terminal keys error | Verify terminal keys and retry. | 
| 105 | Exception while preparing to process | Check system logs and retry. | 
| 106 | Host configuration error | Verify host settings and retry. | 
| 107 | Host connection error | Check network connection and retry. | 
| 108 | Terminal ID configuration error | Verify terminal ID settings. |


## **Get Transaction Details**
Takes **transRef** as an input (String) and returns the details about the transaction (including its current status)
 ```Kotlin
private fun getTransactionDetail(transRef: String) {
	runBlocking {
		async(Dispatchers.IO) {
	val transactionDetailsResponse =
	remitaK11.getTransactionDetail(transRef = transRef)
		Log.d("remitak11","Transaction Detail executed")
	if (transactionDetailsResponse != null) {
		Log.d("Transaction Details",transactionDetailsResponse.toString())
	val intent = Intent(context,
	TransactionDetailActivity::class.java).apply {
		putExtra("transactionDetails", transactionDetailsResponse)
	}
	context.startActivity(intent)
		}
	  }
	}
}
```
## **Get Transactions**
Returns a list of all transactions performed by the terminal

```Kotlin
private fun getTransactionDetail(transRef: String) {
	runBlocking {
		async(Dispatchers.IO) {
		val transactionDetailsResponse = remitaK11.getTransactionDetail(transRef= transRef)
			Log.d("remitak11","Transaction Detail executed")
		if (transactionDetailsResponse != null) {
		Log.d("Transaction Details", transactionDetailsResponse.toString())
			}
		}
	}
}
```
## **Printing Method**

### **Print**

* **Description:** Prints an image.  
* **Method:** `print`  
* **Parameters:**

| PARAMETER | DATA TYPE | DESCRIPTION |
| :---- | :---- | :---- |
| img | Bitmap | Bitmap image to be printed |
| callback | (String)-\> Unit | Returns either “SUCCESS” or “FAILED” |

* **Example:**
```Kotlin
remitaK11.print(img, callBack = {
	Log.d("PRINT:", it)
	})
}
```
**Compulsory Receipt Fields** 

The following fields are mandatory on the receipt printed by the Remita K11 terminal:

| Field Name | Field Description |
| :---- | :---- |
| Merchant Name | Full Name |
| Merchant Location | Full address |
| Terminal ID | Host Terminal ID |
| Merchant ID | Host Merchant ID |
| TMS ID | TMS ID |
| Date | Transaction Date |
| Time | Transaction Time |
| Transaction Type | Purchase |
| RRN | RRN |
| EXT Trans Ref | Ext Transaction Ref |
| Card Number | Card number |
| Card Holder Name | Card holder name |
| Exp Date | Ext Date |
| STAN | Stan |
| Auth Code | Auth Code |
| Transaction Amount | Transaction Amount |
| Total Amount | Total amount |
| Response Code | Response Code |
| Transaction Status | Transaction Status |
| Response Message | Response Message |
| Footer Details | Powered by Remita POS |
| Header  | Terminal ID Provider logo  |

