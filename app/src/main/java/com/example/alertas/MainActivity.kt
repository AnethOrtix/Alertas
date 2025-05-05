package com.example.alertas

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.alertas.ui.theme.AlertasTheme
import com.google.accompanist.permissions.*
import com.google.android.gms.location.*
import com.polidea.rxandroidble3.RxBleClient
import com.polidea.rxandroidble3.scan.ScanRecord
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers


class MainActivity : ComponentActivity() {

    private lateinit var rxBleClient: RxBleClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rxBleClient = RxBleClient.create(this)

        setContent {
            AlertasTheme {
                AlertScreen()
            }
        }

        escanearDispositivoEddystone()
    }

    private fun escanearDispositivoEddystone() {
        rxBleClient.scanBleDevices()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resultadoEscaneo ->
                val registroEscaneo = resultadoEscaneo.scanRecord
                if (registroEscaneo != null) {
                    val contenidoHex = scanRecordToHexString(registroEscaneo)
                    Log.d("BLE", "Datos del BLE: $contenidoHex")

                    if (contenidoHex.contains("10") && contenidoHex.contains("PHNSdm")) {
                        Log.d("BLE", "Botón de pulsera BLE detectado")
                        val mensaje = "¡Alerta activada desde la pulsera BLE!"
                        procesarYEnviarAlerta(this, mensaje)
                    }
                } else {
                    Log.w("BLE", "Resultado de escaneo sin registro")
                }
            }, { error ->
                Log.e("BLE", "Error durante escaneo BLE: ${error.message}")
            })

        // Función auxiliar para convertir el scanRecord a una cadena hexadecimal
        fun scanRecordToHexString(scanRecord: ScanRecord): String {
            val scanData = scanRecord.getBytes()
            return scanData.joinToString(" ") { valor ->
                "%02X".format(valor.toInt() and 0xFF)
            }
        }

    }

    fun scanRecordToHexString(scanRecord: ByteArray): String {
        val hexString = StringBuilder()
        for (byte in scanRecord) {
            hexString.append(String.format("%02X ", byte))
        }
        return hexString.toString().trim()
    }



    private fun procesarYEnviarAlerta(context: Context, mensajeBase: String) {
        obtenerUbicacionYEnviar(context, mensajeBase) { location ->
            val ubicacion = "Lat: ${location.latitude}, Lon: ${location.longitude}"
            val mensaje = "$mensajeBase\nUbicación: $ubicacion"
            enviarAlerta(context, mensaje)
            incrementarContadorAlertas(context)
        }
    }

    private fun obtenerUbicacionYEnviar(
        context: Context,
        mensajeBase: String,
        onLocationReceived: (Location) -> Unit
    ) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setMaxUpdates(1)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    onLocationReceived(location)
                } ?: Toast.makeText(context, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Toast.makeText(context, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enviarAlerta(context: Context, mensaje: String) {
        val contactos = cargarContactos(context)
        if (contactos.isEmpty()) {
            Toast.makeText(context, "No hay contactos registrados", Toast.LENGTH_SHORT).show()
            return
        }
        for (numero in contactos) {
            try {
                SmsManager.getDefault().sendTextMessage(numero, null, mensaje, null, null)
            } catch (e: Exception) {
                Toast.makeText(context, "Error enviando SMS a $numero", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enviarPorWhatsApp(context: Context, mensaje: String) {
        val contactos = cargarContactos(context)
        for (numero in contactos) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/$numero?text=${Uri.encode(mensaje)}")
                setPackage("com.whatsapp")
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "WhatsApp no disponible", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun incrementarContadorAlertas(context: Context) {
        val prefs = context.getSharedPreferences("alertas", Context.MODE_PRIVATE)
        val actual = prefs.getInt("contador", 0)
        prefs.edit().putInt("contador", actual + 1).apply()
    }

    private fun cargarContactos(context: Context): List<String> {
        val prefs = context.getSharedPreferences("contactos", Context.MODE_PRIVATE)
        return prefs.getStringSet("lista_contactos", emptySet())?.toList() ?: emptyList()
    }

    private fun guardarContactos(context: Context, lista: List<String>) {
        val prefs = context.getSharedPreferences("contactos", Context.MODE_PRIVATE)
        prefs.edit().putStringSet("lista_contactos", lista.toSet()).apply()
    }

    private fun getPhoneNumberFromUri(uri: Uri, context: Context): String? {
        val cursor = context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            null,
            null,
            null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                return it.getString(index).replace("[\\s-]".toRegex(), "")
            }
        }
        return null
    }

    @OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun AlertScreen() {
        val context = LocalContext.current
        var contactos by remember { mutableStateOf(cargarContactos(context)) }
        var contador by remember { mutableStateOf(context.getSharedPreferences("alertas", Context.MODE_PRIVATE).getInt("contador", 0)) }
        var enviarWhatsApp by remember { mutableStateOf(false) }

        val permissionState = rememberMultiplePermissionsState(
            listOf(
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.READ_CONTACTS
            )
        )

        val contactLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri ->
            uri?.let {
                val numero = getPhoneNumberFromUri(uri, context)
                numero?.let {
                    if (contactos.size < 3) {
                        contactos = contactos + numero
                        guardarContactos(context, contactos)
                    } else {
                        Toast.makeText(context, "Máximo 3 contactos", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("App de Alertas BLE") })
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Alertas enviadas: $contador")

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = enviarWhatsApp, onCheckedChange = { enviarWhatsApp = it })
                    Text("Enviar por WhatsApp")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    if (permissionState.allPermissionsGranted) {
                        procesarYEnviarAlerta(context, "Alerta manual desde la app")
                        if (enviarWhatsApp) enviarPorWhatsApp(context, "Alerta manual desde la app")
                        contador++
                    } else {
                        permissionState.launchMultiplePermissionRequest()
                    }
                }) {
                    Text("Enviar Alerta")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { contactLauncher.launch() }) {
                    Text("Agregar Contacto")
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    items(contactos) { contacto ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(contacto)
                            Button(onClick = {
                                contactos = contactos - contacto
                                guardarContactos(context, contactos)
                            }) {
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }
}
