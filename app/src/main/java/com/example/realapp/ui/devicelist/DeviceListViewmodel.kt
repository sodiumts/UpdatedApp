package com.example.realapp.ui.devicelist

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.SavedStateHandle
import androidx.savedstate.SavedStateRegistryOwner
import java.util.*

@SuppressLint("MissingPermission")
class DeviceListViewmodel(
    private val bluetoothAdapter: BluetoothAdapter
): ViewModel() {
    //Define observable data
    val composeBluetoothDevices = mutableStateListOf<BluetoothDevice>()
    val bluetoothScanning = mutableStateOf(false)

    val gattServices = mutableStateListOf<BluetoothGattService>()


    var selectedDevice:BluetoothDevice? = null
    var connectedGatt:BluetoothGatt? = null

    val disconnect = mutableStateOf(false)
    val connectionState = mutableStateOf("Disconnected")
    //Create a bluetoothLE scanner
    private val bluetoothLeScanner:BluetoothLeScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    //Set properties for the bluetooth scanner

    //Set the filters to only show inkBuddy devices
    private val scanFilters =  mutableListOf<ScanFilter>(
        ScanFilter.Builder()
            .setDeviceName("inkBuddy")
            .build()
    )

    //Set scan settings
    private val scanSetting = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

    //Define a callback for the BLE scanner
    private val scanCallback = object:ScanCallback(){
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
//            Log.d("Device",result?.device?.name.toString())
            if (result != null){
                if (result.device !in composeBluetoothDevices){
//                    Log.d("Device", result.device.name)
                    composeBluetoothDevices.add(result.device)
                }
            }
        }
    }


    private val bluetoothGattCallback = object : BluetoothGattCallback(){
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if(newState == BluetoothProfile.STATE_CONNECTED){
                    connectedGatt = gatt
                    gatt?.discoverServices()
                    connectionState.value = "Discovering_services"
                }
                else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                    connectionState.value = "Disconnected"
                    disconnect.value = true
                    connectedGatt = null
                }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS){
                if(gatt != null){
                    connectionState.value = "Connected"
                    Log.d("services", "Added ${gatt.services}")
                    gattServices.addAll(gatt.services)
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            connectionState.value = "Connected"
        }
    }




    fun scanBleDevices(){
        bluetoothScanning.value = true
        composeBluetoothDevices.clear()
        //launch a corutine that will perform the BLE scan for 5 seconds
        viewModelScope.launch {
            bluetoothLeScanner.startScan(scanFilters,scanSetting,scanCallback)
//            bluetoothLeScanner.stopScan(scanCallback)
            delay(5000L)
            bluetoothLeScanner.stopScan(scanCallback)

            bluetoothScanning.value = false
        }
    }

    fun connectDevice(device:BluetoothDevice?,context: Context){
        device?.connectGatt(context,false,bluetoothGattCallback)
    }

    fun disconnect(){
        connectedGatt?.disconnect()
    }

    private val targetServiceUUID: UUID? = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
    private val targetCharacteristicUUID:UUID? = UUID.fromString("271a1d53-5650-456d-9423-9cfc97be8e28")

    fun clearTodoList(){
        writeCharacteristic(ByteArray(245))
    }


    fun writeTodoList(writableData:Map<String,Boolean>){
        val keys = writableData.keys
        val values = writableData.values
        var dataBytes:ByteArray = byteArrayOf()

        dataBytes += keys.size.toByte()

        for(value in values){
            dataBytes += if(value){
                1.toByte()
            }else{
                0.toByte()
            }
        }

        for(key in keys){
            dataBytes += key.toByteArray(Charsets.US_ASCII)
            dataBytes += 254.toByte()
        }


        writeCharacteristic(dataBytes)
    }


    private fun writeCharacteristic(writableData:ByteArray){
        val targetService = connectedGatt?.getService(targetServiceUUID)
        val targetChar = targetService?.getCharacteristic(targetCharacteristicUUID)
        connectionState.value = "Writing"

        if(targetChar != null){
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                targetChar.value = writableData
                connectedGatt?.writeCharacteristic(targetChar)
            }else {
                val status = connectedGatt?.writeCharacteristic(
                    targetChar,
                    writableData,
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                )
                if (status != BluetoothStatusCodes.SUCCESS) {
                    Log.e("Write error", status.toString())
                }
            }
        }
    }




    companion object {
        fun provideFactory(
            bluetoothAdapter: BluetoothAdapter,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null,
            ):AbstractSavedStateViewModelFactory =
                object: AbstractSavedStateViewModelFactory(owner,defaultArgs){
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(
                        key: String,
                        modelClass: Class<T>,
                        handle: SavedStateHandle
                    ): T {
                        return DeviceListViewmodel(bluetoothAdapter) as T
                    }

                }
    }
}