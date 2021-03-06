package com.example.warningsystem.activities


import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.bluetooth.Bluetooth
import com.example.warningsystem.R
import com.example.warningsystem.adapters.BluetoothListviewAdapter
import com.example.warningsystem.states.States
import kotlin.math.max



class BluetoothActivity : AppCompatActivity(), AdapterView.OnItemClickListener,
    View.OnClickListener {
    private lateinit var scanButton: AppCompatButton
    private lateinit var lvBtDev: ListView
    private lateinit var lvList: ArrayList<ArrayList<String>>
    private lateinit var messageBytes: ByteArray
    private lateinit var emptyTv: TextView
    private lateinit var intentToNextActivity: Intent
    private lateinit var bluetooth: Bluetooth
    private var lvAdapter: BluetoothListviewAdapter? = null
    private lateinit var mPrefs: SharedPreferences
    private lateinit var mEditor: SharedPreferences.Editor
    private lateinit var mMainView:ConstraintLayout


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)


        bluetooth = Bluetooth.getInstance( this@BluetoothActivity)
        bluetooth.bluetoothEnable()
        lvBtDev = findViewById<View>(R.id.lvDevices) as ListView
        lvBtDev.divider = null
        lvBtDev.dividerHeight = 0
        lvList = ArrayList(ArrayList())
        lvAdapter = BluetoothListviewAdapter(applicationContext, lvList)
        scanButton = findViewById(R.id.scan_bt)
        scanButton.setOnClickListener(this)
        messageBytes = ByteArray(Bluetooth.MAX_RCV_BUFFER_LENGTH)
        emptyTv = findViewById<View>(R.id.empty_textview) as TextView
        lvBtDev.emptyView = emptyTv
        lvBtDev.adapter = lvAdapter
        lvBtDev.onItemClickListener = this

        mPrefs = getSharedPreferences("my.app.packagename_preference",Context.MODE_PRIVATE)
        mEditor = mPrefs.edit()

        val name = mPrefs.getString("name", null)
        val address = mPrefs.getString("address", null)

        if (name != null && address != null) {
            lvList.add(ArrayList())
            lvList[max(lvList.size - 1, 0)].add(name)
            lvList[max(lvList.size - 1, 0)].add(address)
            lvAdapter!!.notifyDataSetChanged()
        }

        mMainView=findViewById(R.id.main_view)

    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            R.id.lvDevices -> {
                if (bluetooth.isEnabled()) {
                    bluetooth.stopSearching()
                    val o: Any = lvBtDev.getItemAtPosition(position)
                    var data = o.toString()
                    data = data.replace("\\[".toRegex(), "").replace("]".toRegex(), "")
                    val dataSplit = data.split(", ", ignoreCase = true, limit = 2)
                    val btName = dataSplit[0]
                    val btAddress = dataSplit[1]
                    mEditor.putString("name", btName).commit()
                    mEditor.putString("address", btAddress).commit()
                    val t = BlueToothConnectThread(btAddress)
                    t.start()
                    Toast.makeText(
                        applicationContext,
                        "Trying to connect to $btName",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Please Enable Bluetooth!!",
                        Toast.LENGTH_SHORT
                    ).show()
                    bluetooth.bluetoothEnable()
                }
            }
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.scan_bt -> {
                lvList.removeAll(lvList.toSet())
                if (bluetooth.isEnabled()) {
                    scanButton.isEnabled = false
                    bluetooth.stopSearching()
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
                lvAdapter!!.notifyDataSetChanged()
            }
        }
    }

    inner class BlueToothConnectThread(
        btAddress: String,
    ) :
        Thread() {
        private val btAddress: String

        init {
            this.btAddress = btAddress
        }

        override fun run() {
            val status: Boolean = bluetooth.connectToThisDevice(btAddress)
            if (!status) {
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Failed to connect",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Connected",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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

}


