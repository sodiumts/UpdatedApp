package com.example.realapp.data

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner

class BluetoothManager (
    private val bluetoothAdapter: BluetoothAdapter
){
    private val bluetoothLeScanner: BluetoothLeScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    fun getScanner():BluetoothLeScanner{
        return bluetoothLeScanner
    }
}