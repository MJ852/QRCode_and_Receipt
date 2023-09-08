package com.example.qrcode

import PrintActivity
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.Serializable
import java.util.Hashtable
import java.util.Locale





class MainActivity : AppCompatActivity() {

    private lateinit var qrCodeImageView: ImageView
    private lateinit var uniqueIDTV: TextView
    private lateinit var database: DatabaseReference
    private lateinit var txtToSpeechMssg: TextToSpeech
    private var deviceID: String = ""
    //private var qrCodeLink: String = ""
    private var storedDeviceID: String = ""
    private var dialogOnStartup = false
    //private lateinit var button: Button
    private lateinit var receiptData: ReceiptData
    private lateinit var printActivity: PrintActivity




    data class ReceiptData(
        val amount_paid: Double = 0.0,
        val order_id: String = "",
        val current_date_time: String = "",
        val deviceID: String = ""
    ): Serializable


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)
        database = FirebaseDatabase.getInstance().reference
        qrCodeImageView = findViewById(R.id.imageqr1)
        uniqueIDTV = findViewById(R.id.uniqueid)
        deviceID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        printActivity = PrintActivity() // Initialize USB printer connection

        generateAndDisplayQRCode(storedDeviceID)
        retrieveLatestData()
        dialogOnStartup = false
        speechMessage()

        val qrCodeLink = "http://localhost/filipay/?sn=$deviceID"
        uniqueIDTV.text = qrCodeLink


    }

    private fun retrieveLatestData() {
        val paymentsRef = database.child("device_ids").child(deviceID).child("payments")
        paymentsRef.orderByChild("currentDateTime").limitToLast(1)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    val storedDeviceID = dataSnapshot.child("deviceID").getValue(String::class.java)
                    val outputData = dataSnapshot.child("amount_paid").getValue(String::class.java)
                    val orderID = dataSnapshot.child("order_id").getValue(String::class.java) ?: ""
                    val dateTime =
                        dataSnapshot.child("current_date_time").getValue(String::class.java) ?: ""
                    val amount = outputData?.toDoubleOrNull() ?: 0.0
                     receiptData = ReceiptData(amount, orderID, dateTime)

                    try {

                        if (storedDeviceID == deviceID) {
                            if (!dialogOnStartup) {
                                showPaymentDialog(amount)
                                //PRINTING RECEIPT
                                printActivity.openConnection()

                                val escPosCommands = byteArrayOf()
                                val receiptData = ReceiptData(
                                    amount_paid = amount,
                                    order_id = orderID,
                                    current_date_time = dateTime
                                )
                                printActivity.sendDataToBluetoothPrinter(escPosCommands, receiptData)
                                printActivity.closeBluetoothConnection()
                                Log.d("RECEIPT DETAILS", "$receiptData")
                                Log.d("DataSnapshot= IF", dataSnapshot.toString())
                            } else {
                                Log.d("DialogBox", "Not shown")

                            }
                        } else {
                            // Toast.makeText(this@MainActivity, "Device ID doesn't match $storedDeviceID and $deviceID", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("On Child Changed", "Child changed")
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.d("On Child Removed", "Child removed")
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("On Child Moved", "Child moved")
            }
            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException(); }
        })


    }

    private fun showPaymentDialog(amount: Double) {
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val qrCodeLink = "http://localhost/filipay/confirmation.php?sn=$deviceId" // Use the correct variable
        val urlDeviceId = qrCodeLink.substringAfterLast("sn=")

        if (urlDeviceId == deviceId) {
            val dialogBox = DialogBox.newInstance(amount)
            dialogBox.dialogMessage("You have paid $amount pesos. Salamat")
            dialogBox.show(supportFragmentManager, "popup")
        } else {
            Log.d("link", "error link qrcode")
        }
    }

    private fun speechMessage(){
        txtToSpeechMssg = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = txtToSpeechMssg.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "speech error", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateAndDisplayQRCode(storedDeviceID: String) {
        val qrCodeLink = "http://localhost/filipay/?sn=$deviceID"
        val hints = Hashtable<EncodeHintType, Any>()
        hints[EncodeHintType.ERROR_CORRECTION] =
            com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix =
            qrCodeWriter.encode(qrCodeLink, BarcodeFormat.QR_CODE, 1080, 1080, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        qrCodeImageView.setImageBitmap(bitmap)
    }



}


