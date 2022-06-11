package com.example.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.HashMap

val MY_UUID_INSECURE: UUID =
    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
const val TAG = "Bluetooth"

@RequiresApi(Build.VERSION_CODES.S)
class Bluetooth private constructor(activity: Activity) {
    private var bluetoothAdapter: BluetoothAdapter?
    private var mDevice: BluetoothDevice? = null
    private var mSocket: BluetoothSocket? = null
    private var mInputStream: InputStream? = null
    private var mOutputStream: OutputStream? = null
    private var activity: Activity
    var isRunning = true
        private set


    companion object {
        private val instancesMap: HashMap<Int, Bluetooth> = kotlin.collections.HashMap()
        const val serialVersionUID = 42L
        const val REQUEST_CODE = 299
        const val MAX_RCV_BUFFER_LENGTH = 1024
        const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        const val REQUEST_ENABLE_BT = 1
        const val ACTION_FOUND = BluetoothDevice.ACTION_FOUND
        const val ACTION_DISCOVERY_FINISHED = BluetoothAdapter.ACTION_DISCOVERY_FINISHED
        const val ACTION_ACL_DISCONNECTED = BluetoothDevice.ACTION_ACL_DISCONNECTED
        const val ACTION_ACL_CONNECTED = BluetoothDevice.ACTION_ACL_CONNECTED


        fun getBluetoothInstance(code: Int, activity: Activity): Bluetooth? {
            instancesMap[code]?.activity = activity
            return if (instancesMap.contains(code))
                instancesMap[code]
            else {
                instancesMap[code] = Bluetooth(activity)
                instancesMap[code]
            }
        }
        fun getBluetoothInstanceWithoutContext(code: Int):Bluetooth? {
            return instancesMap[code]
        }
    }

    init {
        this.activity = activity
        val bluetoothManager =
            activity.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    fun bluetoothEnable() {
        checkNeededPermission()
        if (bluetoothAdapter == null) {
            Toast.makeText(
                activity, "This Device does not support Bluetooth",
                Toast.LENGTH_LONG
            ).show()
        } else if (!bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_CODE
                )
            }
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)

        }
    }

    fun bluetoothDisable() {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_CODE
            )

        }
        bluetoothAdapter?.disable()
        isRunning = false
    }

    fun isEnabled(): Boolean {
        return if (bluetoothAdapter == null) false else bluetoothAdapter!!.isEnabled
    }

    fun startSearching(receiver: BroadcastReceiver, actions: Array<String>) {
        checkNeededPermission()
        Log.i(TAG, "in the start searching method")
        for (a in actions) {
            activity.registerReceiver(receiver, IntentFilter(a))
        }
        if (bluetoothAdapter?.isDiscovering == true) {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.BLUETOOTH_SCAN), REQUEST_CODE
                )
            }
            bluetoothAdapter?.cancelDiscovery()
        }
        bluetoothAdapter?.startDiscovery()
    }

    fun stopSearching(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.BLUETOOTH_SCAN), REQUEST_CODE
            )
        }
        bluetoothAdapter?.cancelDiscovery()
    }

    private fun makeDiscoverable() {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, REQUEST_CODE)

        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.BLUETOOTH_ADVERTISE), REQUEST_CODE
            )
        }
        activity.startActivity(discoverableIntent)
        Log.i("Log", "Discoverable ")
    }


    fun getDeviceNameFromBroadcastReceiver(intent: Intent): String? {
        return try {
            (intent.getParcelableExtra<Parcelable>(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice?)!!.name
        } catch (e: SecurityException) {
            Log.e(TAG, e.message.toString())
            null
        }
    }

    fun getDeviceAddressFromBroadcastReceiver(intent: Intent): String? {

        return (intent.getParcelableExtra<Parcelable>(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice?)!!.address
    }

    fun isConnected(): Boolean {
        return if (mSocket != null) {
            mSocket!!.isConnected
        } else {
            isRunning = false
            false
        }
    }

    private fun getInputOutputStream(): Boolean {
        var tmpIn: InputStream? = null
        var tmpOut: OutputStream? = null
        var isSucceed = true
        if (mSocket == null) {
            Log.e(TAG, "WARNING!! mSocket is NULL in sendReceiveThread")
            isSucceed = false
        }
        try {
            tmpIn = mSocket!!.inputStream
            tmpOut = mSocket!!.outputStream
        } catch (e: Exception) {
            Log.e("EXCEPTION", e.message.toString())
            isSucceed = false
            isRunning = false
        }
        mInputStream = tmpIn
        mOutputStream = tmpOut
        if (tmpIn == null || tmpOut == null) isSucceed = false
        return isSucceed
    }

    private fun getSocket(): Boolean {
        var isSucceed = true
        var tmp: BluetoothSocket? = null
        // Get a BluetoothSocket for a connection with the
        // given BluetoothDevice
        try {
            Log.d(
                TAG, "getSocket: Trying to create InsecureRfcommSocket using UUID: "
                        + MY_UUID_INSECURE
            )

            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT), Bluetooth.REQUEST_CODE
                )

            }
            tmp = mDevice!!.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE)

        } catch (e: Exception) {
            Log.e(
                TAG,
                "getSocket: Could not create InsecureRfcommSocket ${e.message} "
            )
            isSucceed = false
            isRunning = false
        }
        mSocket = tmp
        if(mSocket == null){
            Log.d(TAG, "getSocket: mSocket is null!!!")
        }else {
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.d(TAG, "getSocket: Try mSocket Connect")
                mSocket!!.connect()
                Log.d(TAG, "getSocket: mSocket Connected")

            } catch (e: IOException) {
                isSucceed = false
                isRunning = false
                Log.e(TAG, "getSocket Exception: ${e.message}")
                // Close the socket
                try {
                    mSocket?.close()
                    Log.e(TAG, "getSocket: Closed Socket.")
                } catch (e1: IOException) {
                    Log.e(
                        TAG,
                        "getSocket: Unable to close connection in socket " + e1.message
                    )
                }
                Log.e(TAG, "getSocket: Could not connect to UUID: $MY_UUID_INSECURE")
            }
        }
        return isSucceed
    }

    fun connectToThisDevice(
        address: String
    ): Boolean {
        var status = false
        var socketStatus = false
        var streamStatus = false
        try {
            mDevice = bluetoothAdapter?.getRemoteDevice(address)
            socketStatus = getSocket()
            streamStatus = getInputOutputStream()
            status = socketStatus && streamStatus
            isRunning = status
            Log.d(TAG, "connectToThisDevice: socket status = $socketStatus stream status = $streamStatus")
        } catch (ex: Exception) {
            status = false
            isRunning = false
            Log.e("Bluetooth connect to EXCEPTION", ex.message.toString())
        }
        return status
    }

    fun send(data: ByteArray): Boolean {
        if (mOutputStream != null) {
            val text = String(data, Charset.defaultCharset())
            Log.d(TAG, "write: Writing to outputStream: $text")
            try {
                mOutputStream!!.write(data)
                return true
            } catch (e: IOException) {
                Log.e(TAG, "write: Error writing to output stream. " + e.message)
                isRunning = false
            }
        }
        return false
    }

    fun read(): Pair<ByteArray,Boolean> {
        val buffer: ByteArray = ByteArray(MAX_RCV_BUFFER_LENGTH)
        var size: Int = -1
        var status = false
        if (mInputStream != null) {
            try {
                size = mInputStream!!.read(buffer)
                status = true
            } catch (e: IOException) {
                Log.e(TAG, "write: Error reading Input Stream. " + e.message)
                isRunning = false
                status = false
            }
        }
        val message:ByteArray = if(size>0){
            ByteArray(size)
        }else ByteArray(0)
        for (i in 0 until size){
            message[i]=buffer[i]
        }
        return Pair(message,status)
    }


    private fun checkNeededPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                AlertDialog.Builder(activity)
                    .setTitle("Location Permission")
                    .setMessage("Location permission needed")
                    .setPositiveButton("Ok") { _: DialogInterface, _: Int ->
                        ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_LOCATION
                        )
                    }
                    .create().show()
            } else {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
            return false
        } else {
            return true
        }
    }

    fun startServer() {
        val accept = AcceptThread(activity, bluetoothAdapter)
        accept.start()
    }


}

@RequiresApi(Build.VERSION_CODES.S)
private class AcceptThread(activity: Activity, bluetoothAdapter: BluetoothAdapter?) : Thread() {
    // The local server socket
    private val mmServerSocket: BluetoothServerSocket?

    init {
        var tmp: BluetoothServerSocket? = null

        // Create a new listening server socket
        try {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    200
                )
            }
            tmp = bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(
                "WarningSystem",
                MY_UUID_INSECURE
            )
            Log.d(TAG, "AcceptThread: Setting up Server using: $MY_UUID_INSECURE")
        } catch (e: IOException) {
            Log.e(TAG, "AcceptThread: IOException: " + e.message)
        }
        mmServerSocket = tmp
    }

    override fun run() {
        Log.d(TAG, "run: AcceptThread Running.")
        var socket: BluetoothSocket? = null
        try {
            // This is a blocking call and will only return on a
            // successful connection or an exception
            Log.d(TAG, "run: RFCOMM server socket start.....")
            socket = mmServerSocket!!.accept()
            Log.d(TAG, "run: RFCOMM server socket accepted connection.")
        } catch (e: IOException) {
            Log.e(TAG, "AcceptThread: IOException: " + e.message)
        }
        Log.i(TAG, "END mAcceptThread ")
    }

    fun cancel() {
        Log.d(TAG, "cancel: Canceling AcceptThread.")
        try {
            mmServerSocket!!.close()
        } catch (e: IOException) {
            Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.message)
        }
    }
}


