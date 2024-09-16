package com.example.liftingbuddy

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.liftingbuddy.ui.theme.LiftingBuddyTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : ComponentActivity() {

    private var file = File("")
    private val map : MutableMap<String, ArrayList<Exercise>> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setContent {
            LiftingBuddyTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val exercise = remember { mutableStateOf("") }
                    val editing = remember { mutableStateOf(false) }
                    if(!editing.value) {
                        HomeScreen(onAdd = {editing.value = true; exercise.value=""},
                                   onEdit = {editing.value=true; exercise.value=it})
                    } else {
                        EditScreen(exercise.value, onBack = {editing.value = false})
                    }
                }
            }
        }
    }

    private fun init() {
        file = File(this.filesDir, "entries.txt")
        if(!file.exists()) {
            file.createNewFile()
        } else {
            for (line in file.readLines()) {
                val parent = line.split("::")[0]
                if(map[parent] == null) {
                    map[parent] = arrayListOf()
                }
                map[parent]!!.add(Exercise(line))
            }
        }
    }

    @Composable
    fun HomeScreen(onAdd : () -> Unit, onEdit : (String) -> Unit, modifier : Modifier = Modifier) {
        LazyColumn {
            items(items=map.keys.toList()) { i ->
                ExerciseEntry(i, onEdit)
            }
            items(1) {
                Row(modifier.fillMaxWidth(),
                    horizontalArrangement=Arrangement.Center) {
                    Button(onClick=onAdd) {
                        Text("Add exercise")
                    }
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Composable
    fun ExerciseEntry(name : String, onEdit : (String) -> Unit, modifier : Modifier = Modifier, editing : Boolean = false, ref : () -> Unit = {}) {
        val expanded = remember { mutableStateOf(false) }
//        val refresh = remember{ mutableIntStateOf(0) }
        Column {
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (!editing) {
                    Button(onClick = { onEdit(name) }) {
                        Text("+")
                    }
                }
                Text(
                    text = " ${name.uppercase()} ", style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                )
                if (!editing) {
                    Button(onClick = { expanded.value = !expanded.value }) {
                        if (expanded.value) {
                            Text("^")
                        } else {
                            Text("v")
                        }
                    }
                }
            }

            if (editing || expanded.value) {
                for (i in map[name]!!) {
                    Row(
                        modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        val items = i.displayParts()
                        Box(modifier.border(width = 4.dp, color = Color.White).padding(12.dp)) {
                            Text(items[0])
                        }
                        Box(modifier.border(width = 4.dp, color = Color.White).padding(12.dp)) {
                            Text(items[1])
                        }
                        Box(modifier.border(width = 4.dp, color = Color.White).padding(12.dp)) {
                            Text(items[2])
                        }
                        if (editing) {
                            Box(modifier.border(width = 4.dp, color = Color.White).padding(8.dp)) {
                                Button(onClick = { map[name]!!.remove(i); save(); ref() }) {
                                    Text("-")
                                }
                            }
                        }
                    }
                }
                if (editing) {

                    Row(
                        modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val sets = remember { mutableStateOf("") }
                        val reps = remember { mutableStateOf("") }
                        val weight = remember { mutableStateOf("") }
                        Box(modifier.border(width = 4.dp, color = Color.White).padding(12.dp)) {
                            Text(SimpleDateFormat("EEE, MMM dd").format(Date()))
                        }
                        Box(modifier.border(width = 4.dp, color = Color.White).padding(12.dp)) {
                            Row {
                                BasicTextField(
                                    value = sets.value,
                                    onValueChange = { sets.value = it },
                                    modifier = modifier.background(Color.White).widthIn(max=24.dp)
                                )
                                Text(" x ")
                                BasicTextField(
                                    value = reps.value,
                                    onValueChange = { reps.value = it },
                                    modifier = modifier.background(Color.White).widthIn(max=24.dp)
                                )
                            }
                        }
                        Box(modifier.border(width = 4.dp, color = Color.White).padding(12.dp)) {
                            Row {
                                BasicTextField(
                                    value = weight.value,
                                    onValueChange = { weight.value = it },
                                    modifier = modifier.background(Color.White).widthIn(max=24.dp)
                                )
                                Text(" lbs")
                            }
                        }
                        Box(modifier.border(width = 4.dp, color = Color.White).padding(8.dp)) {
                            Button(onClick = {
                                map[name]!!.add(
                                    Exercise(
                                        name,
                                        sets.value.toInt(),
                                        reps.value.toInt(),
                                        weight.value.toInt()
                                    )
                                )
                                save()
                                ref()
                            }) {
                                Text("Add")
                            }
                        }
                    }

                }
            }
        }
    }

    private fun save() {
        var fil = ""
        for(key in map.keys) {
            for(item in map[key]!!) {
                fil += item.fileString() + "\n"
            }
        }
        file.writeText(fil)
    }

    @Composable
    fun EditScreen(exercise : String, onBack : () -> Unit, modifier : Modifier = Modifier) {
        if(exercise == "") {
            Row(modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment=Alignment.CenterVertically) {
                val name = remember{mutableStateOf("")}
                Text("Name of exercise: ")
                BasicTextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    modifier = modifier.background(Color.White)
                )
                Button(onClick={
                    map[name.value] = arrayListOf()
                    onBack()
                }) {
                    Text("Submit")
                }
            }
        } else {
            Column(modifier.fillMaxSize()) {
                Button(onClick = onBack) {
                    Text("Back")
                }
                val ref = remember {mutableIntStateOf(0)}
                key(ref.intValue) {
                    ExerciseEntry(exercise, onEdit = {}, editing = true, ref={ref.intValue++})
                }
            }
        }
    }

}


class Exercise(private val parent : String, private val sets : Int, private val reps : Int, private val weight : Int, private val date : String) {

    @SuppressLint("SimpleDateFormat")
    constructor(parent : String, sets : Int, reps : Int, weight : Int) : this (
        parent,
        sets,
        reps,
        weight,
        SimpleDateFormat("EEE, MMM dd").format(Date())
    )

    constructor(str : String) : this(
        str.split("::")[0],
        str.split("::")[1].toInt(),
        str.split("::")[2].toInt(),
        str.split("::")[3].toInt(),
        str.split("::")[4]
    )

    fun fileString() : String {
        val del = "::"
        return "$parent$del$sets$del$reps$del$weight$del$date"
    }

    fun displayParts() : List<String> {
        return listOf(
            date,
            "$sets x $reps",
            "$weight lbs"
        )
    }

    override fun equals(other : Any?) : Boolean {
        if(other == null || other !is Exercise) {
            return false
        }
        return parent == other.parent && sets == other.sets
                && reps == other.reps && weight == other.weight && date == other.date
    }

    override fun hashCode(): Int {
        var result = parent.hashCode()
        result = 31 * result + sets
        result = 31 * result + reps
        result = 31 * result + weight
        result = 31 * result + date.hashCode()
        return result
    }

}