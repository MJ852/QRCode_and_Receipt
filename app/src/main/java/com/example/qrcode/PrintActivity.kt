
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.util.Log
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.example.qrcode.MainActivity
import com.google.firebase.database.annotations.Nullable


class PrintActivity {

    // Initialize the BluetoothAdapter here, or consider initializing it when needed.
    private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    //private var bluetoothSocket: BluetoothSocket? = null


    fun PrintActivity(receiptData: MainActivity.ReceiptData) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    @SuppressLint("MissingPermission")
    @Nullable
    fun getList(): Array<BluetoothConnection?>? {
        if (bluetoothAdapter == null) {
            return null
        }
        if (!bluetoothAdapter!!.isEnabled) {
            return null
        }
        val bluetoothDevicesList = bluetoothAdapter!!.bondedDevices
        val bluetoothDevices = arrayOfNulls<BluetoothConnection>(bluetoothDevicesList.size)
        if (bluetoothDevicesList.size > 0) {
            var i = 0
            for (device in bluetoothDevicesList) {
                bluetoothDevices[i++] = BluetoothConnection(device)
            }
        }
        Log.d("Bluetooth", "$bluetoothDevices")
        return bluetoothDevices
    }

    fun openConnection() {

    }


    fun closeBluetoothConnection() {

    }

    fun sendDataToBluetoothPrinter(escPosCommands: ByteArray, receiptData: MainActivity.ReceiptData) {

        val printer = EscPosPrinter(
            BluetoothPrintersConnections.selectFirstPaired(),
            215,
            48f,
            32
        )

        val amountPaid = receiptData.amount_paid
        val orderId = receiptData.order_id
        val currentDateTime = receiptData.current_date_time

        val formattedText = """
            [L]
            [C]<font size='big'>PAYMENT RECEIPT</font>
            [C]================================
            [L]
            [L]<b>ORDER ID: </b>[R] $orderId  
            [L]<b>DATE&TIME:</b>[R]$currentDateTime
            [L]  
            [C]--------------------------------
            [C]<b>AMOUNT PAID: </b>
            [C]<b><font size='big'>P$amountPaid</b></font> 
            [C]================================
            [L]
            [C]<font size='tall'>THANK YOU!!</font>
            [L]
            [L]
            [L]
        """.trimIndent()

        printer.printFormattedText(formattedText)

    }
}
