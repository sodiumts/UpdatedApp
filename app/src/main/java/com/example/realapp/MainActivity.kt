package com.example.realapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Scanner
import kotlin.system.exitProcess

@SuppressLint("MissingPermission")
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



    private var servicesList: MutableList<BluetoothGattService?>? = null
    private val bluetoothGattCallback = object : BluetoothGattCallback(){
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if(newState == BluetoothProfile.STATE_CONNECTED){
                gatt?.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                servicesList = gatt?.services
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DeviceCard(
        modifier:Modifier = Modifier,
        device: BluetoothDevice){
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            onClick = {
                device.connectGatt(this,false,bluetoothGattCallback)
            }
        ) {
            if (device.name == null){
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Unknown device")
                    Text(text = "Address: ${device.address}")
                }

            }else{
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Name: ${device.name}")
                    Text(text = "Address: ${device.address}")
                }

            }
        }
    }

    @Composable
    fun BluetoothDeviceList() {
        val context = LocalContext.current
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        val bleDevices = remember { mutableStateListOf<BluetoothDevice>() } // Keep track of the discovered BLE devices
        val scanning = remember { mutableStateOf(false) } // Keep track of the scanning state


        //Set the filters to only show inkBuddy devices
        val scanFilters =  mutableListOf<ScanFilter>()
        val deviceFilter = ScanFilter.Builder().setDeviceName("inkBuddy").build()
        scanFilters.add(deviceFilter)
        //Set scan settings
        val scanSetting = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        var scanCallback: ScanCallback?


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

            lifecycleScope.launch {
                bluetoothAdapter.bluetoothLeScanner.startScan(scanFilters,scanSetting,scanCallback)
//                bluetoothAdapter.bluetoothLeScanner.startScan(scanCallback)
                delay(10000)
                bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
                scanning.value = false
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Text("Devices",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(15.dp)
                )

            // Display the list of discovered devices in a lazy column
            LazyColumn(modifier = Modifier.fillMaxWidth()
                .weight(1f)
            ) {
                items(bleDevices) { device ->
                    DeviceCard(device = device)
                }
            }
            Log.d("Scan value", "${scanning.value}")
            if(scanning.value){
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
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
