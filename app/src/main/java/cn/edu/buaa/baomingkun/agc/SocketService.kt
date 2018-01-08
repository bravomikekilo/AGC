package cn.edu.buaa.baomingkun.agc

import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.*
import android.widget.Toast
import java.util.*

class SocketService : Service() {

    companion object {
            val SPP_UUID: String = "00001101-0000-1000-8000-00805F9B34FB"
    }

    inner class socketMonitor(device: BluetoothDevice, messenger: Messenger): Thread(){

        val messenger = messenger
        val socket = device.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID))

        @Volatile
        var stopFlag  = false
        override fun run() {
            socket.connect()
            val reader = socket.inputStream.reader()
            while(!stopFlag){
                val buffer = CharArray(10, {i -> ' '})
                reader.read(buffer, 0, 10)
                val msg = Message.obtain(null, 0, String(buffer).trim())
                messenger.send(msg)
            }
            socket.close()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        Toast.makeText(application, "service on bind", Toast.LENGTH_LONG).show()
        return LocalBinder()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    private var mMessager: MutableMap<BluetoothDevice, socketMonitor> = mutableMapOf()

    inner class LocalBinder: Binder(){
        fun getService(): SocketService{
            return this@SocketService
        }

        fun addSocket(device: BluetoothDevice, messenger: Messenger){
            val monitor = socketMonitor(device, messenger)
            mMessager[device] = monitor
            monitor.start()
        }

        fun closeSocket(device: BluetoothDevice){
            val monitor = mMessager[device]
            mMessager.remove(device)
            monitor?.stopFlag = true
        }
    }

}
