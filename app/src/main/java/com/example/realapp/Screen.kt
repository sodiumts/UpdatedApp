package com.example.realapp

sealed class Screen(val route:String){
    object DeviceListScreen : Screen("device_list_screen")
    object DetailsScreen : Screen("details_screen")
}
