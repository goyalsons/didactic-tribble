package com.goyalsons.epc2ean

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

data class RowItem(val epc:String,val gtin14:String?,val ean13:String?,val serial:String?,val status:String,val error:String?)

class MainActivity:ComponentActivity(){
    private var pendingCsv:ByteArray?=null
    private val createCsv=registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")){uri:Uri?->
        uri?.let{contentResolver.openOutputStream(it)?.use{os-> pendingCsv?.let{os.write(it)}}}
        pendingCsv=null
    }
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContent{AppScreen{csv-> pendingCsv=csv.toByteArray(); createCsv.launch("epc_results.csv")}}
    }
}

@Composable
fun AppScreen(onExport:(String)->Unit){
    var epcInput by remember{mutableStateOf("")}
    val focusRequester=remember{FocusRequester()}
    val rows= remember{ mutableStateListOf<RowItem>()}
    LaunchedEffect(Unit){ delay(200); focusRequester.requestFocus() }
    fun decodeAndAdd(epc:String){ val r=Decoder.decodeSgtin96(epc); rows.add(RowItem(epc,r.gtin14,r.ean13,r.serial,r.status,r.error)) }
    Scaffold(
        topBar={ TopAppBar(title={Text("EPC→EAN Converter")}) },
        bottomBar={ BottomAppBar{ Button(onClick={ val header="EPC,GTIN-14,EAN-13,Serial,Status,Error\n"; val body=rows.joinToString("\n"){listOf(it.epc,it.gtin14?:"",it.ean13?:"",it.serial?:"",it.status,it.error?:"").joinToString(",")}; onExport(header+body)}, enabled=rows.isNotEmpty()){Text("Export CSV")} Spacer(Modifier.width(8.dp)); OutlinedButton(onClick={rows.clear()}){Text("Clear")} } }
    ){ pad-> Column(Modifier.padding(pad).padding(8.dp)){ OutlinedTextField(value=epcInput,onValueChange={epcInput=it},label={Text("Scan EPC hex")},modifier=Modifier.fillMaxWidth().focusRequester(focusRequester).onKeyEvent{ke-> if(ke.key==Key.Enter||ke.key==Key.NumPadEnter){ if(epcInput.isNotBlank()){ decodeAndAdd(epcInput); epcInput="" }; true } else false } ); Spacer(Modifier.height(8.dp)); LazyColumn{ items(rows){r-> Text("${r.epc} -> ${r.gtin14?:"?"}/${r.ean13?:""} (S:${r.serial}) ${if(r.status=="ok")"" else "Error: "+(r.error?:"")}") } } } }
}
