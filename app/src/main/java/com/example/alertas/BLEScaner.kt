package com.example.alertas

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission

class BLEScanner(
    private val context: Context,
    private val onDeviceFound: () -> Unit
) {
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val scanner = bluetoothAdapter.bluetoothLeScanner
    private val handler = Handler(Looper.getMainLooper())

    // Dirección MAC exacta
    private val targetMac = "DC:0D:30:1E:79:28"

    private val scanCallback = object : ScanCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device: BluetoothDevice = result.device
            Log.d("BLE", "Dispositivo encontrado: ${device.name} - ${device.address}")

            if (device.address == targetMac) {
                Log.d("BLE", "✅ Pulsera encontrada por MAC")
                stopScan()
                onDeviceFound()
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BLE", "❌ Escaneo fallido: código $errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        Log.d("BLE", "🔍 Iniciando escaneo BLE...")
        scanner?.startScan(null, getScanSettings(), scanCallback)

        // Detener tras 15 segundos si no detecta
        handler.postDelayed({ stopScan() }, 15000)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScan() {
        scanner?.stopScan(scanCallback)
        Log.d("BLE", "🛑 Escaneo detenido")
    }

    private fun getScanSettings(): ScanSettings {
        return ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
    }
}
