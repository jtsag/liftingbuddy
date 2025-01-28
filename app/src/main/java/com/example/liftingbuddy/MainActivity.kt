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
    private val progs : MutableList<Program> = mutableListOf()

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
                if(parent == "~") {
                    progs.add(Program(line))
                } else {
                    if(map[parent] == null) {
                        map[parent] = arrayListOf()
                    }
                    map[parent]!!.add(Exercise(line))
                }

            }
        }
    }

    @Composable
    fun HomeScreen(onAdd : () -> Unit, onEdit : (String) -> Unit, modifier : Modifier = Modifier) {
        LazyColumn {
            items(items=map.keys.toList()) { i ->
                ExerciseEntryDisplay(i, onEdit)
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
    fun ExerciseEntryEdit(name : String, modifier : Modifier = Modifier, ref : () -> Unit = {}) {
        LazyColumn {
            items(1) {
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = " ${name.uppercase()} ", style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color.White
                        )
                    )

                }
                for (i in map[name]!!) {
                    Row(
                        modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        val items = i.displayParts()
                        Box(modifier.border(width = 4.dp, color = Color.White).padding(12.dp).weight(1f)) {
                            Text(items[0])
                        }
                        Box(modifier.border(width = 4.dp, color = Color.White).padding(12.dp).weight(1f)) {
                            Text(items[1], modifier = modifier.align(Alignment.Center))
                        }
                        Box(modifier.border(width = 4.dp, color = Color.White).padding(12.dp).weight(1f)) {
                            Text(items[2], modifier = modifier.align(Alignment.Center))
                        }
                        //.border(width = 4.dp, color = Color.White).padding(8.dp).
                        Box(modifier.weight(0.75f)) {
                            Button(onClick = { map[name]!!.remove(i); save(); ref() },
                                   modifier = modifier.align(Alignment.CenterEnd)) {
                                Text("-")
                            }
                        }
                    }
                }

                val addSuggestion : Pair<Boolean, List<String>> = findSuggestion(name)
                if(addSuggestion.first) {
                    Row(
                        modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        val items = addSuggestion.second
                        Box(modifier.border(width = 4.dp, color = Color.White).padding(12.dp).weight(1f)) {
                            Text(items[0])
                        }
                        Box(modifier.border(width = 4.dp, color = Color.White).padding(12.dp).weight(1f)) {
                            Text(items[1], modifier = modifier.align(Alignment.Center), color = Color(0xFFD32F2F))
                        }
                        Box(modifier.border(width = 4.dp, color = Color.White).padding(12.dp).weight(1f)) {
                            Text(items[2], modifier = modifier.align(Alignment.CenterEnd), color = Color(0xFFD32F2F))
                        }
                    }
                }

                Row(
                    modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val sets = remember { mutableStateOf("") }
                    val reps = remember { mutableStateOf("") }
                    val weight = remember { mutableStateOf("") }
                    Box(modifier.border(width = 4.dp, color = Color.White).padding(12.dp).weight(1f)) {
                        Text(SimpleDateFormat("EEE, MMM dd").format(Date()))
                    }
                    Box(modifier.border(width = 4.dp, color = Color.White).padding(12.dp).weight(1f)) {
                        Row(modifier = modifier.align(Alignment.Center)) {
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
                    Box(modifier.border(width = 4.dp, color = Color.White).padding(12.dp).weight(1f)) {
                        Row(modifier = modifier.align(Alignment.Center)) {
                            BasicTextField(
                                value = weight.value,
                                onValueChange = { weight.value = it },
                                modifier = modifier.background(Color.White).widthIn(max=24.dp)
                            )
                            Text(" lbs")
                        }
                    }
                    Box(modifier.weight(0.75f)) {
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
                        }, modifier = modifier.align(Alignment.CenterEnd)) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ExerciseEntryDisplay(name : String, onEdit : (String) -> Unit, modifier : Modifier = Modifier) {
        val expanded = remember{ mutableStateOf(false) }
        Column {
            Row(
                modifier = modifier.fillMaxWidth().background(Color.LightGray),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Button(onClick = { onEdit(name) }) {
                    Text("+")
                }
                Text(
                    text = " ${name.uppercase()} ", style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.DarkGray
                    )
                )

                Button(onClick = { expanded.value = !expanded.value }) {
                    if (expanded.value) {
                        Text("^")
                    } else {
                        Text("v")
                    }
                }
            }

            if (expanded.value) {
                for (i in map[name]!!) {
                    Row(
                        modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        val items = i.displayParts()
                        Box(modifier.border(width = 4.dp, color = Color.White).padding(12.dp).weight(1f)) {
                            Text(items[0])
                        }
                        Box(modifier.border(width = 4.dp, color = Color.White).padding(12.dp).weight(1f)) {
                            Text(items[1], modifier = modifier.align(Alignment.Center))
                        }
                        Box(modifier.border(width = 4.dp, color = Color.White).padding(12.dp).weight(1f)) {
                            Text(items[2], modifier = modifier.align(Alignment.CenterEnd))
                        }
                    }
                }

                val addSuggestion : Pair<Boolean, List<String>> = findSuggestion(name)
                if(addSuggestion.first) {
                    Row(
                        modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        val items = addSuggestion.second
                        Box(modifier.border(width = 4.dp, color = Color.White).padding(12.dp).weight(1f)) {
                            Text(items[0])
                        }
                        Box(modifier.border(width = 4.dp, color = Color.White).padding(12.dp).weight(1f)) {
                            Text(items[1], modifier = modifier.align(Alignment.Center), color = Color(0xFFD32F2F))
                        }
                        Box(modifier.border(width = 4.dp, color = Color.White).padding(12.dp).weight(1f)) {
                            Text(items[2], modifier = modifier.align(Alignment.CenterEnd), color = Color(0xFFD32F2F))
                        }
                    }
                }
            }
        }
    }

    private fun findSuggestion(exercise : String) : Pair<Boolean, List<String>> {
        for(p in progs) {
            if(p.exercise == exercise) {
                val arr = p.weeks
                var lastMatch = -1
                for(entry in map[exercise]!!) {
                    for(idx in lastMatch+1 until arr.size) {
                        if(entry.sets == arr[idx].first
                            && entry.reps == arr[idx].second
                            && entry.weight == arr[idx].third) {
                            lastMatch = idx
                            break
                        }
                    }
                }

                if(lastMatch == arr.size - 1) {
                    return Pair(true, listOf("", "1 x 1", "max lbs"))
                }

                val setRep = "" + arr[lastMatch + 1].first + " x " + arr[lastMatch + 1].second
                val weight = "" + arr[lastMatch + 1].third + " lbs"

                return Pair(true, listOf("", setRep, weight))
            }
        }
        return Pair(false, listOf())
    }

    private fun save() {
        var fil = ""
        for(key in map.keys) {
            for(item in map[key]!!) {
                fil += item.fileString() + "\n"
            }
        }
        for(prog in progs) {
            fil += prog.fileString() + "\n"
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
            val program = remember { mutableStateOf(false) }
            Column(modifier.fillMaxSize()) {
                if (!program.value) {
                    Row(modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Button(onClick = onBack) {
                            Text("Back")
                        }
                        Button(onClick = {program.value = true}) {
                            if(progExists(exercise)) {
                                Text("Delete Program")
                            } else {
                                Text("Add Program")
                            }
                        }
                    }
                    val ref = remember { mutableIntStateOf(0) }
                    key(ref.intValue) {
                        ExerciseEntryEdit(
                            exercise,
                            ref = { ref.intValue++ })
                    }
                } else {
                    ProgramEdit(exercise, {program.value = false})
                }
            }
        }
    }

    @Composable
    fun ProgramEdit(exercise : String, onBack : () -> Unit, modifier : Modifier = Modifier) {
        if (!progExists(exercise)) {
            Row(modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment=Alignment.CenterVertically) {
                val max = remember{mutableStateOf("")}
                Button(onClick = onBack) {
                    Text("Cancel")
                }
                Text("Enter max: ")
                BasicTextField(
                    value = max.value,
                    onValueChange = { max.value = it },
                    modifier = modifier.background(Color.White)
                )
                Button(onClick={
                    progs.add(Program(exercise, max.value.toInt()))
                    save()
                    onBack()
                }) {
                    Text("Submit")
                }
            }
        } else {
            Column(modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.Start) {
                Text("Are you sure you want to delete the current program?")
                Row(modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(onClick = onBack) {
                        Text("No")
                    }
                    Button(onClick = {deleteProg(exercise); onBack()}) {
                        Text("Yes")
                    }
                }

            }
        }
    }

    private fun deleteProg(name : String) {
        for(idx in progs.indices) {
            if(progs[idx].exercise == name) {
                progs.removeAt(idx)
                save()
                return
            }
        }
    }

    private fun progExists(name : String) : Boolean {
        for(prog in progs) {
            if(prog.exercise == name) {
                return true
            }
        }
        return false
    }

}



class Program(val exercise : String, private val max : Int) {
    val weeks : Array<Triple<Int, Int, Int>> //Sets, reps, weight

    init {
        weeks = Array(10) {i -> Triple(i, i, max)}
        weeks[1] = Triple(0, 0, 0)
        //TODO: Update with meaningful weight logic
    }

    constructor(str : String) : this(
        str.split("::")[1],
        str.split("::")[2].toInt()
    )

    fun fileString() : String {
        val del = "::"
        val signal = "~"
        return "$signal$del$exercise$del$max"
    }

    override fun hashCode(): Int {
        return exercise.hashCode() + 31 * max + weeks.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if(other == null || other !is Program) {
            return false
        }
        return exercise == other.exercise && max == other.max
    }
}

class Exercise(private val parent : String,
               val sets : Int,
               val reps : Int,
               val weight : Int,
               private val date : String) {

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