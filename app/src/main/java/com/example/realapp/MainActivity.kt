package com.example.realapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            for(each in it){
                if(!each.value){
                    exitProcess(1)
                }
            }
        }
    private val bluetoothAdapter:BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)

        requestPermissionsLauncher.launch(permissions)
        super.onCreate(savedInstanceState)
        setContent {
                MaterialTheme{
                    Surface(modifier = Modifier.fillMaxSize()) {
                        BluetoothDeviceList()
                    }
                }
            }
        }

    override fun onResume() {
        super.onResume()
        if (!bluetoothAdapter.isEnabled){
            showBluetoothPermRequest()
        }
    }

    override fun onStart() {
        super.onStart()
        showBluetoothPermRequest()
        }
    @SuppressLint("MissingPermission")
    @Composable
    fun BluetoothDeviceList() {
        val context = LocalContext.current
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        val bleDevices = remember { mutableStateListOf<BluetoothDevice>() } // Keep track of the discovered BLE devices
        val scanning = remember { mutableStateOf(false) } // Keep track of the scanning state

        var scanCallback: ScanCallback? = null

        LaunchedEffect(Unit) {
            // Start scanning for BLE devices when the composable is first launched
            scanning.value = true



            scanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    if (result.device !in bleDevices){
                        bleDevices.add(result.device)
                    }
                }
            }

            bluetoothAdapter.bluetoothLeScanner.startScan(scanCallback)
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Text(text = "Scanning for BLE devices...")
            if (scanning.value && bleDevices.isEmpty()) {
                // Show a progress indicator if we're still scanning and haven't found any devices yet
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                // Display the list of discovered devices in a lazy column
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(bleDevices) { device ->
                        if (device.name == null){
                            Text(text = "Unknown device")
                            Text(text = "Address: ${device.address}")
                        }else{
                            Text(text = "Name: ${device.name}")
                            Text(text = "Address: ${device.address}")
                        }
                    }
                }
            }
        }

        @Composable
        fun DeviceCard(){

        }



    }





    private var isBluetoothDialogueShown = false

    private fun showBluetoothPermRequest(){
        if(!bluetoothAdapter.isEnabled){
            if(!isBluetoothDialogueShown){
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startBluetoothPermRequestForResult.launch(enableBluetoothIntent)
                isBluetoothDialogueShown = true
            }
        }
    }

    private val startBluetoothPermRequestForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            isBluetoothDialogueShown = false
            if(result.resultCode != Activity.RESULT_OK){
                showBluetoothPermRequest()
            }
        }
}
