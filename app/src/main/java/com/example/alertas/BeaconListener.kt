package com.example.alertas

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.Handler
import android.widget.Toast
import java.util.*

class BeaconListener(
    private val context: Context,
    private val onBeaconDetected: () -> Unit
) {

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val scanner: BluetoothLeScanner? get() = bluetoothAdapter.bluetoothLeScanner
    private var isScanning = false
    private var detectionCooldown = false

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                val mac = device.address
                val name = device.name ?: "Desconocido"

                if (mac == "DC:0D:30:1E:79:28" || name.lowercase(Locale.ROOT).contains("eddystone")) {
                    if (!detectionCooldown) {
                        detectionCooldown = true
                        Toast.makeText(context, "Se detectó la pulsera BLE", Toast.LENGTH_SHORT).show()
                        onBeaconDetected()
                        Handler().postDelayed({ detectionCooldown = false }, 5000)
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Toast.makeText(context, "Error en escaneo BLE: $errorCode", Toast.LENGTH_LONG).show()
        }
    }

    fun startScan() {
        if (isScanning) return
        scanner?.startScan(scanCallback)
        isScanning = true
        Toast.makeText(context, "Escaneo BLE iniciado", Toast.LENGTH_SHORT).show()
    }

    fun stopScan() {
        if (!isScanning) return
        scanner?.stopScan(scanCallback)
        isScanning = false
        Toast.makeText(context, "Escaneo BLE detenido", Toast.LENGTH_SHORT).show()
    }
}
