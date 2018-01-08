package cn.edu.buaa.baomingkun.agc

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.EXTRA_DEVICE
import android.content.*
import android.os.*
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    companion object {
        val ACTIVITY_TAG = "MAIN_ACTIVITY"
        val REQUEST_ENABLE_BT = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(receiver, filter)

        fab.setOnClickListener { view ->
            if(!mAdapter.isDiscovering && connectedDevice == null) {
                menu = null
                devices.clear()
                Snackbar.make(view, "start scanning bluetooth device", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                val ret = mAdapter.startDiscovery()
                Log.i(ACTIVITY_TAG, "scan begin: $ret")
                menuOpened = view.showContextMenu()
                if(ret){
                    view.isClickable = false
                }
            }
            if(connectedDevice != null){
                binder?.closeSocket(connectedDevice!!)
                connectedDevice = null
                Snackbar.make(view, "shutdown bluetooth socket, releasing connection",
                        Snackbar.LENGTH_LONG).setAction("Action", null).show()
            }

        }

        registerForContextMenu(fab)

        val intent = Intent(this, SocketService::class.java)
        bindService(intent, serviceConn, Context.BIND_AUTO_CREATE)

        if(!mAdapter.isEnabled){
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
        }
    }

    var menuOpened = false
    var connectedDevice: BluetoothDevice? = null
    val mAdapter = BluetoothAdapter.getDefaultAdapter()
    var menu: ContextMenu? = null

    val devices: ArrayList<BluetoothDevice> = arrayListOf()

    private val handler: Handler = signalHandler()

    private val messenger: Messenger = Messenger(handler)
    private var binder: SocketService.LocalBinder? = null

    private var serviceConn: ServiceConnection = object :ServiceConnection{
        override fun onServiceDisconnected(name: ComponentName?) {
            Toast.makeText(application, "socket service disconnect", Toast.LENGTH_LONG).show()
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as SocketService.LocalBinder
            this@MainActivity.binder = binder
        }

    }

    val receiver = object :BroadcastReceiver(){

        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent!!.action
            if(action == BluetoothDevice.ACTION_FOUND){
                val device = intent.getParcelableExtra<BluetoothDevice>(EXTRA_DEVICE)
                val index = devices.size
                Log.i(ACTIVITY_TAG, "Device searched ${device.name}")
                devices.add(device)
                menu?.add(0, index, index, device.name)
                if(!menuOpened){
                    menuOpened = fab.showContextMenu()
                }

            } else if(action == BluetoothAdapter.ACTION_DISCOVERY_FINISHED){
                Snackbar.make(fab, "bluetooth scan finish", Snackbar.LENGTH_LONG).
                        setAction("Action", null).show()
                fab.isClickable = true
                Log.i(ACTIVITY_TAG, "device search finished")
            }
        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu?.setHeaderTitle("select device to connect")
        devices.forEach { device -> menu?.add(device.name) }
        Log.i(ACTIVITY_TAG,"context menu ready")
        this.menu = menu
    }

    override fun onContextMenuClosed(menu: Menu?) {
        if (connectedDevice == null){
            mAdapter.cancelDiscovery()
        }
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        menuOpened = false
        if(item != null){
            val device = devices[item.itemId]
            connectedDevice = device
            mAdapter.cancelDiscovery()
            addSocket(device)
        }
        return true
    }


    private fun addSocket(device: BluetoothDevice){
        binder?.addSocket(device, messenger)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            REQUEST_ENABLE_BT -> {
                if(resultCode == Activity.RESULT_OK)
                    super.onActivityResult(requestCode, resultCode, data)
                else {
                    exitProcess(-1)
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    inner class signalHandler: Handler(){

        var remain: String = "";
        override fun handleMessage(msg: Message?) {
            if(msg == null) return
            val rawMsg = msg.obj as String
            if(!rawMsg.contains("|value,")) {
                remain += rawMsg
                return
            }
            val content = remain + rawMsg
            val split = content.split("|value,")
            remain = split.last()
            for(i in split.dropLast(1)){
                try {
                    val v = Integer.parseInt(i)
                    Log.i(ACTIVITY_TAG, "parsed signal $v")
                    chartView.addValue(v)
                } catch (e: NumberFormatException) {
                    continue
                }
            }
        }
    }
}

