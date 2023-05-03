package com.example.realapp.ui.elements

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
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
                viewModel = viewModel,
            )
        }
        if (disconnectCall.value) {
            disconnectCall.value = false
            viewModel.disconnect()
            Toast.makeText(navController.context,"Device Disconnected", Toast.LENGTH_SHORT).show()
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

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun DeviceDetailsScreen(
    viewModel: DeviceListViewmodel,
) {
    var deviceName = viewModel.selectedDevice?.name
    val deviceAddress = viewModel.selectedDevice?.address

    var text1 by remember { mutableStateOf(TextFieldValue("")) }
    var text2 by remember { mutableStateOf(TextFieldValue("")) }
    var text3 by remember { mutableStateOf(TextFieldValue("")) }

    val checkState1 = remember { mutableStateOf(false) }
    val checkState2 = remember { mutableStateOf(false) }
    val checkState3 = remember { mutableStateOf(false) }




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
//        Button(onClick = { viewModel.writeData() })
        Card(
            modifier = Modifier
                .padding(top = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checkState1.value,
                        onCheckedChange = { checkState1.value = it }
                    )
                    OutlinedTextField(
                        value = text1,
                        singleLine = true,
                        label = { Text(text= "1st task")},
                        placeholder = {Text("Input a task")},
                        onValueChange = { textValue ->
                            if(textValue.text.length <= 10) text1 = textValue
                        }
                        )
                }
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Checkbox(
                        checked = checkState2.value,
                        onCheckedChange = { checkState2.value = it }
                    )
                    OutlinedTextField(
                        value = text2,
                        singleLine = true,
                        label = { Text(text= "2nd task")},
                        onValueChange = { textValue ->
                            if(textValue.text.length <= 10) text2 = textValue
                        })
                }
                Row (
                    verticalAlignment = Alignment.CenterVertically
                        ){
                    Checkbox(
                        checked = checkState3.value,
                        onCheckedChange = { checkState3.value = it }
                    )
                    OutlinedTextField(
                        value = text3,
                        singleLine = true,
                        label = { Text(text= "3rd task")},
                        onValueChange = { textValue ->
                            if(textValue.text.length <= 10) text3 = textValue
                        })
                }
            }
        }
        Button(modifier = Modifier.align(Alignment.End),
            onClick = {
                val sendableData = mutableMapOf<String, Boolean>()

                Log.d("Text", text1.text)
                Log.d("Text", text2.text)
                Log.d("Text", text3.text)

                if (text1.text != "") {
                    sendableData[text1.text] = checkState1.value
                }
                if (text2.text != "") {
                    sendableData[text2.text] = checkState2.value
                }
                if (text3.text != "") {
                    sendableData[text3.text] = checkState3.value
                }
                if (sendableData.isNotEmpty()) {
                    Log.d("Button", sendableData.toString())
                    viewModel.writeTodoList(sendableData)
                }
            }) {
            Text(text = "Send Input")
        }
    }
}

