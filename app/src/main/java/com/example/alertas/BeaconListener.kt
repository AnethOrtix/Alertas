package com.example.alertas

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import java.util.*

class BeaconListener(
    private val context: Context,
    private val onBeaconDetected: () -> Unit
) {


    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val scanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private val handler = Handler(Looper.getMainLooper())

    private val targetDeviceName = "FSC-BP107D"
    private val targetMacAddress = "DC:0D:30:1E:79:28"

    private val scanCallback = object : ScanCallback() {
        @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device: BluetoothDevice = result.device
            val deviceName = device.name
            val deviceAddress = device.address

            Log.d("BLE", "Dispositivo detectado: $deviceName, $deviceAddress")

            if (deviceName == targetDeviceName && deviceAddress == targetMacAddress) {
                Log.d("BLE", "✅ Pulsera detectada")
                stopScan() // Detenemos el escaneo al detectar la pulsera
                onBeaconDetected()
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BLE", "❌ Falló el escaneo BLE: $errorCode")
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startScan() {
        Log.d("BLE", "🔍 Iniciando escaneo BLE")
        scanner?.startScan(null, buildScanSettings(), scanCallback)

        // Paramos el escaneo después de 10 segundos para ahorrar batería
        handler.postDelayed({ stopScan() }, 10000)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScan() {
        scanner?.stopScan(scanCallback)
        Log.d("BLE", "🛑 Escaneo detenido")
    }

    private fun buildScanSettings(): ScanSettings {
        return ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
    }
}
