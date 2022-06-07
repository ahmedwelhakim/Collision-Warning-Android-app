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


class Bluetooth(activity: Activity) {
    private var bluetoothAdapter: BluetoothAdapter?
    private val activity: Activity
    private var mDevice: BluetoothDevice? = null
    private var mSocket: BluetoothSocket? = null
    private var mSendReceiveThread: SendReceiveThread? = null

    companion object {
        private const val TAG = "Bluetooth"
        private val MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
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
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }
        bluetoothAdapter?.disable()
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
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        bluetoothAdapter?.cancelDiscovery()
    }

    private fun makeDiscoverable() {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)

        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        activity.startActivity(discoverableIntent)
        Log.i("Log", "Discoverable ")
    }


    fun getDeviceNameFromBroadcastReceiver(intent: Intent): String? {
        return try {
            (intent.getParcelableExtra<Parcelable>(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice?)!!.name
        }catch (e: SecurityException){
            Log.e(TAG,e.message.toString())
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
            false
        }
    }


    inner class ConnectThread(device: BluetoothDevice?) : Thread() {
        private var isSucceed = false

        init {
            isSucceed = true
            Log.d(TAG, "ConnectThread: started.")
            mDevice = device
        }

        fun isSucceed(): Boolean {
            return isSucceed
        }

        override fun run() {
            var tmp: BluetoothSocket? = null
            Log.i(TAG, "RUN mConnectThread ")
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                Log.d(
                    TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                            + MY_UUID_INSECURE
                )

                if (ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                }
                tmp = mDevice!!.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE)

            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "ConnectThread: Could not create InsecureRfcommSocket ${e.message} "
                )
                isSucceed = false
            }
            mSocket = tmp
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.d(TAG, "ConnectThread: Try mSocket Connected")
                mSocket!!.connect()
                Log.d(TAG, "ConnectThread: mSocket Connected")

            } catch (e: IOException) {
                isSucceed = false
                // Close the socket
                try {
                    mSocket?.close()
                    Log.e(TAG, "run: Closed Socket.")
                } catch (e1: IOException) {
                    Log.e(
                        TAG,
                        "mConnectThread: run: Unable to close connection in socket " + e1.message
                    )
                }
                Log.e(TAG, "run: ConnectThread: Could not connect to UUID: $MY_UUID_INSECURE")
            }

        }

        fun cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.")
                mSocket?.close()
            } catch (e: IOException) {
                Log.e(
                    TAG,
                    "cancel: close() of mSocket in ConnectThread failed.: ${e.message}"
                )
            }
        }
    }

    fun connectToThisDeviceToSendReceive(
        address: String,
        receivedMessage: ByteArray, len: Int
    ): Boolean {
        var succeed = false
        try {
            val mDevice = bluetoothAdapter?.getRemoteDevice(address)
            val cThread = ConnectThread(mDevice)
            cThread.start()
            cThread.join()
            succeed = cThread.isSucceed()
            mSendReceiveThread = SendReceiveThread(receivedMessage, len)
            Log.d(TAG, "Starting SendRecvThread")
            mSendReceiveThread!!.start()
            succeed = succeed && mSendReceiveThread!!.isConnected()
        } catch (ex: Exception) {
            succeed = false
            Log.e("Bluetooth connect to EXCEPTION", ex.message.toString())
        }
        return succeed
    }

    inner class SendReceiveThread(outMessage: ByteArray, len: Int) : Thread() {
        private var mmInStream: InputStream? = null
        private var mmOutStream: OutputStream? = null
        private var receivingMessage: ByteArray
        private var outputSize: Int
        private var isSucceed = true

        init {
            Log.d(TAG, "SendReceiveThread: Starting.")

            this.outputSize = len;
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null
            receivingMessage = outMessage
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
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
        }

        fun isConnected(): Boolean {
            return isSucceed && mSocket!!.isConnected
        }

        override fun run() {
            val buffer = ByteArray(MAX_RCV_BUFFER_LENGTH) // buffer store for the stream
            var bytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            Log.d(TAG, "SendReceiveThread: in run function")
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream!!.read(buffer)

                    for (i in 0 until MAX_RCV_BUFFER_LENGTH) {
                        receivingMessage[i] = buffer[i]
                    }
                    outputSize = bytes
                } catch (e: IOException) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.message)
                    isSucceed = false
                    break
                }
            }
        }

        fun write(bytes: ByteArray?) {
            val text = String(bytes!!, Charset.defaultCharset())
            Log.d(TAG, "write: Writing to outputStream: $text")
            try {
                mmOutStream!!.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "write: Error writing to output stream. " + e.message)
            }
        }

        /* Call this from the main activity to shutdown the connection */
        fun cancel() {
            try {
                mSocket!!.close()
            } catch (_: IOException) {
            }
        }
    }

    fun sendMessage(msg: ByteArray?) {
        try {
            mSendReceiveThread!!.write(msg)
        }catch (e: KotlinNullPointerException){
            Log.e(TAG,"KNP exception due to Send Receive thread not initialized: ${e.message}")
        }
    }

    private inner class AcceptThread : Thread() {
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
                }
                tmp = bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(
                    "appname",
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

            //talk about this is in the 3rd
            //if (socket != null) {
            //    connected(socket);
            // }
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

    fun startServer() {
        val accept = AcceptThread()
        accept.start()
    }


}