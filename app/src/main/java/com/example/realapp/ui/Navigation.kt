package com.example.realapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.realapp.Screen
import com.example.realapp.ui.devicelist.DeviceListViewmodel
import com.example.realapp.ui.elements.BleDeviceDetails
import com.example.realapp.ui.elements.BluetoothDeviceList


@Composable
fun Navigation(viewModel: DeviceListViewmodel){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.DeviceListScreen.route){
        composable(route = Screen.DeviceListScreen.route){
            BluetoothDeviceList(viewModel = viewModel,navController = navController)
        }
        composable(route = Screen.DetailsScreen.route){
            BleDeviceDetails(navController = navController,viewModel = viewModel)
        }


    }
}
