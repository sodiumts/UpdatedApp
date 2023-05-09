package com.example.realapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.realapp.ui.Navigation
import com.example.realapp.ui.devicelist.DeviceListViewmodel
import com.example.realapp.ui.theme.AppTheme

@SuppressLint("MissingPermission")
class MainActivity : ComponentActivity() {
    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val viewModel: DeviceListViewmodel by viewModels {
        DeviceListViewmodel.provideFactory(bluetoothAdapter, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= 31) {
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            requestPermissionsLauncher.launch(permissions)
        } else {
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            requestPermissionsLauncher.launch(permissions)
        }



        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Navigation(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!bluetoothAdapter.isEnabled) {
            showBluetoothPermRequest()
        }
    }

    override fun onStart() {
        super.onStart()
        showBluetoothPermRequest()
    }


    private var isBluetoothDialogueShown = false

    private fun showBluetoothPermRequest() {
        if (!bluetoothAdapter.isEnabled) {
            if (!isBluetoothDialogueShown) {
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startBluetoothPermRequestForResult.launch(enableBluetoothIntent)
                isBluetoothDialogueShown = true
            }
        }
    }

    private val startBluetoothPermRequestForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            isBluetoothDialogueShown = false
            if (result.resultCode != Activity.RESULT_OK) {
                showBluetoothPermRequest()
            }
        }
}
