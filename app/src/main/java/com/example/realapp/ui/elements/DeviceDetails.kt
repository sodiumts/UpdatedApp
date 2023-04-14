package com.example.realapp.ui.elements

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.realapp.ui.devicelist.DeviceListViewmodel
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@Composable
fun BleDeviceDetails(
    navController: NavController,
    viewModel: DeviceListViewmodel
){
    val bluetoothServicesCompose = viewModel.gattServices

    LaunchedEffect(Unit){
        viewModel.viewModelScope.launch {
            viewModel.connectDevice(viewModel.selectedDevice,navController.context)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize()
        ){ items(bluetoothServicesCompose){ service ->
            Text(text = service.uuid.toString())
            Text(text = service.characteristics.toString())
        }
    }


}