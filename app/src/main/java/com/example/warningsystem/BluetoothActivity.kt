package com.example.warningsystem


import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import com.example.bluetooth.Bluetooth
import kotlin.math.max


class BluetoothActivity : AppCompatActivity() {
    private lateinit var scanButton: AppCompatButton
    private lateinit var lvBtDev: ListView
    private lateinit var lvList: ArrayList<ArrayList<String>>
    private lateinit var messageBytes: ByteArray
    private var messageLen: Int = 0
    private lateinit var emptyTv: TextView
    private var isEnabled = false
    private lateinit var intentToNextActivity: Intent
    private lateinit var bluetooth: Bluetooth
    private var lvAdapter: LvAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bluetooth = Bluetooth(this@BluetoothActivity)
        bluetooth.bluetoothEnable()

        lvBtDev = findViewById<View>(R.id.lvDevices) as ListView
        lvBtDev.divider = null
        lvBtDev.dividerHeight = 0

        lvList = ArrayList(ArrayList())
        lvAdapter = LvAdapter(applicationContext, lvList)

        scanButton = findViewById<AppCompatButton>(R.id.scan_bt)

        messageBytes = ByteArray(Bluetooth.MAX_RCV_BUFFER_LENGTH)
        emptyTv = findViewById<View>(R.id.empty_textview) as TextView
        lvBtDev.emptyView = emptyTv
        lvBtDev.adapter = lvAdapter
        lvBtDev.setOnItemClickListener { _, _, position, _ ->
            if (bluetooth.isEnabled()) {
                bluetooth.stopSearching(applicationContext)
                val o: Any = lvBtDev.getItemAtPosition(position)
                var data = o.toString()
                data = data.replace("\\[".toRegex(), "").replace("\\]".toRegex(), "").toString()
                val dataSplited = data.split(", ", ignoreCase = true, limit = 2)
                val btName = dataSplited[0]
                val btAddress = dataSplited[1]
                val t = BlueToothConnectThread(
                    btAddress,
                    messageBytes,
                    messageLen
                )
                t.start()
            } else {
                bluetooth.bluetoothEnable()
            }
        }

        scanButton.setOnClickListener { _ ->
            lvList.removeAll(lvList.toSet())
            if (bluetooth.isEnabled()) {
                scanButton.isEnabled = false
                bluetooth.stopSearching(applicationContext)
                bluetooth.startSearching(
                    receiver, arrayOf(
                        Bluetooth.ACTION_FOUND,
                        Bluetooth.ACTION_DISCOVERY_FINISHED,
                        Bluetooth.ACTION_ACL_DISCONNECTED
                    )
                )
                Toast.makeText(
                    applicationContext,
                    "    Searching   .  .  .  ",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                bluetooth.bluetoothEnable()
                Toast.makeText(
                    applicationContext,
                    "Please Enable your Bluetooth!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    inner class BlueToothConnectThread(
        btAddress: String,
        messageBytes: ByteArray?,
        messageLen: Int
    ) :
        Thread() {
        private val btAddress: String
        private val messageLen: Int

        init {
            this.btAddress = btAddress
            this.messageLen = messageLen
        }

        override fun run() {
            val status: Boolean = bluetooth.connectToThisDeviceToSendReceive(
                btAddress,
                messageBytes,
                messageLen
            )
            if (!status) {
                runOnUiThread(Runnable {
                    Toast.makeText(
                        applicationContext,
                        "Failed to connect",
                        Toast.LENGTH_SHORT
                    ).show()
                })
            } else {
                runOnUiThread(Runnable {
                    Toast.makeText(
                        applicationContext,
                        "Connected",
                        Toast.LENGTH_SHORT
                    ).show()
                })
                intentToNextActivity = Intent(
                    applicationContext, MonitorActivity::class.java
                )
                startActivity(intentToNextActivity)
            }
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                Bluetooth.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    val deviceName: String? = bluetooth.getDeviceNameFromBroadcastReceiver(intent)
                    val deviceHardwareAddress: String? =
                        bluetooth.getDeviceAddressFromBroadcastReceiver(intent) // MAC address
                    var isNotExist = true
                    for (i in lvList.indices) {
                        isNotExist = isNotExist &&
                                !lvList[i].contains(deviceHardwareAddress) && deviceName != null
                    }
                    if (isNotExist) {
                        // this line adds the data of your EditText and puts in your array
                        if (deviceName != null) {
                            lvList.add(ArrayList())
                            lvList[max(lvList.size - 1, 0)].add(deviceName)
                            if (deviceHardwareAddress != null) {
                                lvList[max(lvList.size - 1, 0)].add(deviceHardwareAddress)
                            }
                            // next thing you have to do is check if your adapter has changed
                            lvAdapter?.notifyDataSetChanged()
                        }
                    }
                }
                Bluetooth.ACTION_DISCOVERY_FINISHED -> scanButton.isEnabled = true
                Bluetooth.ACTION_ACL_DISCONNECTED -> {
                    runOnUiThread {
                        Toast.makeText(
                            context,
                            "  Disconnected  ",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (MonitorActivity.getInstance() != null)
                        MonitorActivity.getInstance()!!.finish()
                }
            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        if (bluetooth.isEnabled()) {
            bluetooth.bluetoothDisable()
        }
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {
            e.message
        }
    }


    class LvAdapter(context: Context, data: ArrayList<ArrayList<String>>) :
        BaseAdapter() {
        private var context: Context
        private var data: ArrayList<ArrayList<String>>
        private var inflater: LayoutInflater? = null

        init {
            // TODO Auto-generated constructor stub
            this.context = context
            this.data = data
            inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        }

        override fun getCount(): Int {
            // TODO Auto-generated method stub
            return data.size
        }

        override fun getItem(position: Int): Any {
            // TODO Auto-generated method stub
            return data[position]
        }

        override fun getItemId(position: Int): Long {
            // TODO Auto-generated method stub
            return position.toLong()
        }

        @SuppressLint("InflateParams")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            // TODO Auto-generated method stub
            var vi = convertView
            if (vi == null) vi = inflater?.inflate(R.layout.list_item, null)
            val tvBtName = vi?.findViewById<View>(R.id.bt_name) as TextView
            tvBtName.text = data[position][0]
            val tvBtAddress = vi.findViewById<View>(R.id.bt_address) as TextView
            tvBtAddress.text = data[position][1]
            return vi
        }
    }
}