package com.example.realapp.ui.elements

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattService
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.realapp.Screen
import com.example.realapp.ui.devicelist.DeviceListViewmodel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun BleDeviceDetails(
    navController: NavController,
    viewModel: DeviceListViewmodel
) {
    val bluetoothServicesCompose = viewModel.gattServices
    val bluetoothGattStatusCompose = viewModel.connectionState
    val disconnectCall = viewModel.disconnect


    //Launch a connection for the connected device
    LaunchedEffect(Unit) {
        bluetoothGattStatusCompose.value = "Connecting"
        viewModel.viewModelScope.launch {
            viewModel.connectDevice(viewModel.selectedDevice, navController.context)
        }
    }

    Scaffold{ innerPadding ->
        if (bluetoothGattStatusCompose.value == "Discovering_services") {
            DeviceConnectingScreen(innerPadding = innerPadding)

        }
        if (bluetoothGattStatusCompose.value == "Connected") {
            DeviceDetailsScreen(
                innerPadding = innerPadding,
                bluetoothServicesCompose = bluetoothServicesCompose,
                viewModel = viewModel
            )
        }
        if (disconnectCall.value) {
            disconnectCall.value = false
            viewModel.disconnect()
            navController.navigate(Screen.DeviceListScreen.route)

        }
    }
}


@Composable
fun DeviceConnectingScreen(
    innerPadding:PaddingValues
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Connecting to device",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.titleLarge
        )
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DeviceDetailsScreen(
    innerPadding: PaddingValues,
    bluetoothServicesCompose: SnapshotStateList<BluetoothGattService>,
    viewModel: DeviceListViewmodel

){
    var deviceName = viewModel.selectedDevice?.name
    val deviceAddress = viewModel.selectedDevice?.address



    if (deviceName == null) deviceName = "Unknown Device"
    Column(
        modifier = Modifier
            .padding(10.dp),
    ) {
        Column {
            Text(
                text = deviceName,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "$deviceAddress",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.secondary,

                )
        }
        Button(onClick = { viewModel.writeData() }) {

        }




        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(bluetoothServicesCompose) { service ->
                Text(text = service.uuid.toString())
                Text(text = service.characteristics.toString())
            }
        }
    }




}