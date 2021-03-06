package com.example.bluetooth

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*


class Bluetooth private constructor(activity: Activity) {
    private var bluetoothAdapter: BluetoothAdapter?
    private var mDevice: BluetoothDevice? = null
    private var mSocket: BluetoothSocket? = null
    private var mInputStream: InputStream? = null
    private var mOutputStream: OutputStream? = null

    private var activity: Activity
    var isRunning = false
        private set


    companion object : SingletonHolder<Bluetooth, Activity>(::Bluetooth){

        private const val MY_UUID_INSECURE = "00001101-0000-1000-8000-00805F9B34FB"
        private const val TAG = "Bluetooth"
        const val REQUEST_CODE = 299
        const val MAX_RCV_BUFFER_LENGTH = 1024
        const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        const val REQUEST_ENABLE_BT = 1
        const val ACTION_FOUND = BluetoothDevice.ACTION_FOUND
        const val ACTION_DISCOVERY_FINISHED = BluetoothAdapter.ACTION_DISCOVERY_FINISHED
        const val ACTION_ACL_DISCONNECTED = BluetoothDevice.ACTION_ACL_DISCONNECTED
        const val ACTION_ACL_CONNECTED = BluetoothDevice.ACTION_ACL_CONNECTED



    }

    init {
        this.activity = activity
        val bluetoothManager =
            activity.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    open class SingletonHolder<out T: Any, in A>(creator: (A) -> T) {
        private var creator: ((A) -> T)? = creator
        @Volatile private var instance: T? = null

        fun getInstance(arg: A): T {
            val i = instance
            if (i != null) {
                return i
            }

            return synchronized(this) {
                val i2 = instance
                if (i2 != null) {
                    i2
                } else {
                    val created = creator!!(arg)
                    instance = created
                    creator = null
                    created
                }
            }
        }fun getInstanceWithoutArg():T?{
           return instance
        }
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_CODE
                    )
                }else{
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_ADMIN), REQUEST_CODE
                    )
                }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_CODE
                )
            }

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.BLUETOOTH_SCAN), REQUEST_CODE
                    )
                }
            }
            bluetoothAdapter?.cancelDiscovery()
        }
        bluetoothAdapter?.startDiscovery()
    }

    fun stopSearching() {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.BLUETOOTH_SCAN), REQUEST_CODE
                )
            }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.BLUETOOTH_ADVERTISE), REQUEST_CODE
                )
            }
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_CODE
                    )
                }

            }
            tmp = mDevice!!.createInsecureRfcommSocketToServiceRecord( UUID.fromString(MY_UUID_INSECURE))

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
        var status:Boolean
        val socketStatus:Boolean
        val streamStatus :Boolean
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
    private inner class SendThread(var data: ByteArray): Thread(){

        override fun run() {
            if (mOutputStream != null) {
                val text = String(data, Charset.defaultCharset())
                Log.d(TAG, "write: Writing to outputStream: $text")
                try {
                    mOutputStream!!.write(data)
                } catch (e: IOException) {
                    Log.e(TAG, "write: Error writing to output stream. " + e.message)
                    isRunning = false
                }
            }
        }
    }
    private lateinit var sendThread:SendThread
    fun send(data: ByteArray) {
       if (this::sendThread.isInitialized){
           if(sendThread.isAlive){
               // we won't send the data till the send thread finish
           } else {
               sendThread = SendThread(data)
               sendThread.start()
           }
       }else{
           sendThread = SendThread(data)
           sendThread.start()
       }
    }

    fun read(): Pair<ByteArray,Boolean> {
        val buffer = ByteArray(MAX_RCV_BUFFER_LENGTH)
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
    private inner class AcceptThread(activity: Activity, bluetoothAdapter: BluetoothAdapter?) : Thread() {
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                            200
                        )
                    }
                }
                tmp = bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(
                    "WarningSystem",
                    UUID.fromString(MY_UUID_INSECURE)
                )
                Log.d(TAG, "AcceptThread: Setting up Server using: $MY_UUID_INSECURE")
            } catch (e: IOException) {
                Log.e(TAG, "AcceptThread: IOException: " + e.message)
            }
            mmServerSocket = tmp
        }

        override fun run() {
            Log.d(TAG, "run: AcceptThread Running.")
            val socket: BluetoothSocket?
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.d(TAG, "run: RFCOMM server socket start.....")
                socket = mmServerSocket!!.accept()
                Log.d(TAG, "run: RFCOMM server socket accepted connection.")
                mSocket = socket
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


}




