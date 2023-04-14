package com.example.realapp.ui.elements

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.realapp.Screen
import com.example.realapp.ui.devicelist.DeviceListViewmodel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@SuppressLint("MissingPermission")
@Composable
fun BluetoothDeviceList(
    viewModel: DeviceListViewmodel,
    navController: NavController
){
    Surface(modifier = Modifier.fillMaxSize()) {
//                        BluetoothDeviceList()
        val composeBluetoothDevices = viewModel.composeBluetoothDevices
        val bluetoothScanning = viewModel.bluetoothScanning

        val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = bluetoothScanning.value)

        LaunchedEffect(Unit){
            viewModel.ScanBleDevices()
        }




            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    "Devices",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.padding(15.dp)
                )

                // Display the list of discovered devices in a lazy column
                SwipeRefresh(
                    state = swipeRefreshState,
                    onRefresh = { viewModel.ScanBleDevices() },
                    indicator = { state, trigger ->
                        SwipeRefreshIndicator(
                            state = state,
                            refreshTriggerDistance = trigger,
                            scale = true,
                            backgroundColor = MaterialTheme.colorScheme.inversePrimary,

                        )
                    }
                ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    items(composeBluetoothDevices) { device ->
                        DeviceCard(device = device, viewModel = viewModel, navController = navController)
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun DeviceCard(
    modifier: Modifier = Modifier,
    device: BluetoothDevice?,
    viewModel: DeviceListViewmodel,
    navController: NavController
){
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        shape = MaterialTheme.shapes.medium,
        onClick = {
            if(device != null){
                viewModel.selectedDevice = device
                navController.navigate(Screen.DetailsScreen.route)
            }
        }
    ) {
        if (device?.name == null){
            Column(modifier = Modifier.padding(10.dp)) {
                Text(text = "Unknown device", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.titleLarge)
                Text(text = "Address: ${device?.address}", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.titleMedium)
            }

        }else{
            Column(modifier = Modifier.padding(10.dp)) {
                Text(text = device.name,color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.titleLarge)
                Text(text = "Address: ${device.address}", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.titleMedium)
            }

        }
    }
}