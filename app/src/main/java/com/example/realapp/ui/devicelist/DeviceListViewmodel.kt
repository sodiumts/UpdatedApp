package com.example.realapp.ui.devicelist

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
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
import java.nio.ByteBuffer
import java.nio.ByteOrder
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
            if (result != null){
                if (result.device !in composeBluetoothDevices){
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
    }




    fun scanBleDevices(){
        bluetoothScanning.value = true
        composeBluetoothDevices.clear()
        //launch a corutine that will perform the BLE scan for 5 seconds
        viewModelScope.launch {
            bluetoothLeScanner.startScan(scanFilters,scanSetting,scanCallback)
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
    private val targetCharacteristicUUID:UUID? = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
    fun writeData(){
        val targetService = connectedGatt?.getService(targetServiceUUID)
        val targetChar = targetService?.getCharacteristic(targetCharacteristicUUID)

        fun Int.toByteArray() = byteArrayOf(
            this.toByte(),
            (this ushr 8).toByte(),
            (this ushr 16).toByte(),
            (this ushr 24).toByte()
        )


        val currentTime = (System.currentTimeMillis() /1000L).toInt().toByteArray()



        fun toInt32(bytes: ByteArray, index: Int): Int {
            require(bytes.size == 4) { "length must be 4, got: ${bytes.size}" }
            return ByteBuffer.wrap(bytes, index, 4).order(ByteOrder.LITTLE_ENDIAN).int
        }
        val timeConverted = toInt32(currentTime,0)

        Log.d("CurrentTime","$timeConverted")
        val writable = currentTime

        if(targetChar != null){
            val status = connectedGatt?.writeCharacteristic(targetChar,writable,BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
            Log.d("GattWriteStatus", "Status of write operation ${status}")
        }
//        if(targetChar?.permissions ==  BluetoothGattCharacteristic.PERMISSION_WRITE){

//        }else{
//            Log.d("CharPermission", "Characteristic doesn't have write permission")
//        }

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