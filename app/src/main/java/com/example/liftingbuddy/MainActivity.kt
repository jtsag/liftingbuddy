package com.example.liftingbuddy

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.liftingbuddy.ui.theme.LiftingBuddyTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

const val delimiter = "::"
const val programSignal = "~"
const val exerciseSignal = "|"
const val saveFile = "entries.txt"
const val TAG = "DEBUG"

class MainActivity : ComponentActivity() {

    private var file = File("")
    private val map : MutableMap<String, ArrayList<SessionEntry>> = mutableMapOf()
    private val progs : MutableList<Program> = mutableListOf()
    private val exerciseAttrs : MutableMap<String, Exercise> = mutableMapOf()
    private val allTags : MutableSet<String> = mutableSetOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setContent {
            LiftingBuddyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black//MaterialTheme.colorScheme.background
                ) {
                    val exercise = remember { mutableStateOf("") }
                    val editing = remember { mutableStateOf(false) }
                    val selectedFilters = remember {mutableStateSetOf<String>()}

                    if(!editing.value) {
                        HomeScreen(
                            onAdd = {editing.value = true; exercise.value=""},
                            onEdit = {editing.value=true; exercise.value=it},
                            selectedFilters = selectedFilters
                        )
                    } else {
                        EditScreen(exercise.value, onBack = {editing.value = false})
                    }
                }
            }
        }
    }

    private fun init() {
        Log.d(TAG,"INIT")
        file = File(this.filesDir, saveFile)
        if(!file.exists()) {
            file.createNewFile()
        } else {
            for (line in file.readLines()) {
                when (val parent = line.split(delimiter)[0]) {
                    programSignal -> {
                        progs.add(Program(line))
                    }
                    exerciseSignal -> {
                        exerciseAttrs[line.split(delimiter)[1]] = Exercise(line)
                    }
                    else -> {
                        if (map[parent] == null) {
                            map[parent] = arrayListOf()
                        }
                        map[parent]!!.add(SessionEntry(line))
                    }
                }
            }
        }

        // Make sure all exercises have attributes
        for(exerciseStr in map.keys) {
            if (exerciseStr in exerciseAttrs.keys) continue
            exerciseAttrs[exerciseStr] = makeExerciseObject(exerciseStr)
        }

        // Init all the tags
        for(exerString in exerciseAttrs.keys) {
            allTags.addAll(exerciseAttrs[exerString]!!.tags)
        }
        //Prebuilt tags
        allTags.add("Favorite")
    }

    @Composable
    fun HomeScreen(onAdd: () -> Unit, onEdit: (String) -> Unit, selectedFilters: SnapshotStateSet<String>, modifier: Modifier = Modifier) {
        LazyColumn {
            // Title
            items(1) {
                Row(modifier.fillMaxWidth()) {
                    Spacer(modifier.weight(0.4f))
                    Text(
                        "LIFTING BUDDY",
                        style=TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            color = Color.White
                        ),
                        modifier=modifier.weight(1f)
                    )

                    CheckboxDropdownMenu(
                        allTags.toList(),
                        selectedFilters,
                        modifier.weight(0.4f)
                    )
                }
            }
            //Each exercise gets an "entry"
            items(items=map.keys.toList()) { i ->
                if(selectedFilters.isEmpty() || isIncludedInFilter(i, selectedFilters)) {
                    ExerciseEntryDisplay(i, onEdit)
                }
            }

            //The add exercise at the bottom
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

    @Composable
    fun CheckboxDropdownMenu(
        items: List<String>,
        selected: MutableSet<String>,
        modifier : Modifier = Modifier
    ) {
        val expanded = remember { mutableStateOf(false) }

        Box(modifier) {
            Button(
                onClick = {expanded.value = true},
                Modifier.align(Alignment.CenterEnd)
            ) {
                Text("\u25BC")
            }

            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false }
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = item in selected,
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) selected += item
                                        else selected -= item
                                    }
                                )
                                Text(item)
                            }
                        },
                        onClick = {
                            if (item in selected) selected -= item
                            else selected += item
                        }
                    )
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Composable
    fun ExerciseEntryEdit(name : String, ref : () -> Unit, modifier : Modifier = Modifier) {
        val subtitleStyle = TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color.White
        )
        val titleStyle = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color.White
        )
        LazyColumn {
            //The exercise name
            items(1) {
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = " ${name.uppercase()} ",
                        style = titleStyle
                    )
                }
                if(progExists(name)) {
                    Row(
                        modifier = modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "MAX: ${getProg(name)?.max} LBS",
                            style = subtitleStyle
                        )
                    }
                }
                if(exerciseAttrs[name] != null) {
                    Row(
                        modifier = modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Tags: ${exerciseAttrs[name]!!.getAttrString()}",
                            style = subtitleStyle,
                            modifier = modifier.weight(1f)
                        )

                    }
                }

            //The suggestion and "add" rows
                Row(
                    modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val sets = remember { mutableStateOf("") }
                    val reps = remember { mutableStateOf("") }
                    val weight = remember { mutableStateOf("") }
                    Box(modifier.border(width = 4.dp, color = Color(getColor(R.color.border_color))).padding(12.dp).weight(1.2f)) {
                        Text(SimpleDateFormat("EEE, MMM dd").format(Date()))
                    }
                    Box(modifier.border(width = 4.dp, color = Color(getColor(R.color.border_color))).padding(12.dp).weight(1f)) {
                        Row(modifier = modifier.align(Alignment.Center)) {
                            BasicTextField(
                                value = sets.value,
                                onValueChange = { sets.value = it },
                                modifier = modifier.background(Color.White).widthIn(max=24.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Text(" x ")
                            BasicTextField(
                                value = reps.value,
                                onValueChange = { reps.value = it },
                                modifier = modifier.background(Color.White).widthIn(max=24.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                    Box(modifier.border(width = 4.dp, color = Color(getColor(R.color.border_color))).padding(12.dp).weight(1f)) {
                        Row(modifier = modifier.align(Alignment.Center)) {
                            BasicTextField(
                                value = weight.value,
                                onValueChange = { weight.value = it },
                                modifier = modifier.background(Color.White).widthIn(max=24.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Text(" lbs")
                        }
                    }
                    Box(modifier.weight(0.75f)) {
                        Button(onClick = {
                            try {
                                map[name]!!.add(
                                    SessionEntry(
                                        name,
                                        sets.value.toInt(),
                                        reps.value.toInt(),
                                        weight.value.toInt()
                                    )
                                )
                            } catch (_ : Exception) {
                                //Do nothing
                            }
                            save()
                            ref()
                        }, modifier = modifier.align(Alignment.CenterEnd)) {
                            Text("Add")
                        }
                    }
                }
                SuggestionRow(name, firstWeight = 0.2f, lastAlignment = Alignment.Center, editing = true,
                    onSubmit = {
                        val lst = findSuggestion(name)
                        map[name]!!.add(
                            SessionEntry(
                                name,
                                lst.second[1].split("x")[0].trim().toInt(),
                                lst.second[1].split("x")[1].trim().toInt(),
                                lst.second[2].split("lbs")[0].trim().toInt()
                            )
                        )
                        save()
                        ref()})
            }

            //The main item/history rows
            items(items=map[name]!!.reversed()) {i ->
                Row(
                    modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    val items = i.displayParts()
                    Box(modifier.border(width = 4.dp,
                        color = Color(getColor(R.color.border_color))).padding(12.dp).weight(1.2f)) {
                        Text(items[0], style = TextStyle(color= getEntryColor(i)))
                    }
                    Box(modifier.border(width = 4.dp, color = Color(getColor(R.color.border_color))).padding(12.dp).weight(1f)) {
                        Text(items[1], modifier = modifier.align(Alignment.Center), style = TextStyle(color= getEntryColor(i)))
                    }
                    Box(modifier.border(width = 4.dp, color = Color(getColor(R.color.border_color))).padding(12.dp).weight(1f)) {
                        Text(items[2], modifier = modifier.align(Alignment.Center), style = TextStyle(color= getEntryColor(i)))
                    }
                    Box(modifier.weight(0.75f)) {
                        Button(onClick = { map[name]!!.remove(i); save(); ref() },
                            modifier = modifier.align(Alignment.CenterEnd)) {
                            Text("-")
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
            //Just the header for each exercise
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Button(onClick = { onEdit(name) }) {
                    Text("\u270E") // Pencil icon
                }
                val append = if(progExists(name)) "*" else ""
                Text(
                    text = " $append${name.uppercase()} ", style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                )

                Button(onClick = { expanded.value = !expanded.value }) {
                    if (expanded.value) {
                        Text("∧")
                    } else {
                        Text("∨")
                    }
                }
            }

            //Then the entries themselves if expanded
            if (expanded.value) {
                SuggestionRow(name)
                for (i in map[name]!!.reversed()) {
                    Row(
                        modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        val items = i.displayParts()
                        Box(modifier.border(width = 4.dp, color = Color(getColor(R.color.border_color))).padding(12.dp).weight(1f)) {
                            Text(items[0])
                        }
                        Box(modifier.border(width = 4.dp, color = Color(getColor(R.color.border_color))).padding(12.dp).weight(1f)) {
                            Text(items[1], modifier = modifier.align(Alignment.Center))
                        }
                        Box(modifier.border(width = 4.dp, color = Color(getColor(R.color.border_color))).padding(12.dp).weight(1f)) {
                            Text(items[2], modifier = modifier.align(Alignment.CenterEnd))
                        }
                    }
                }

            }
        }
    }

    @Composable
    fun SuggestionRow(name : String, modifier : Modifier = Modifier, firstWeight : Float = 1f, lastAlignment : Alignment = Alignment.CenterEnd, editing : Boolean = false, onSubmit : () -> Unit = {}) {
        val addSuggestion : Pair<Boolean, List<String>> = findSuggestion(name)
        if(!addSuggestion.first) return

        Row(
            modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        )
        {
            val items = addSuggestion.second
            Box(modifier.border(width = 4.dp, color = Color(getColor(R.color.border_color))).padding(12.dp).weight(firstWeight)) {
                Text(items[0],
                    modifier = modifier.align(Alignment.Center),
                    color = Color(getColor(R.color.suggestion_color)))
            }
            Box(modifier.border(width = 4.dp, color = Color(getColor(R.color.border_color))).padding(12.dp).weight(1f)) {
                Text(items[1],
                    modifier = modifier.align(Alignment.Center),
                    color = Color(getColor(R.color.suggestion_color)))
            }
            Box(modifier.border(width = 4.dp, color = Color(getColor(R.color.border_color))).padding(12.dp).weight(1f)) {
                Text(items[2],
                    modifier = modifier.align(lastAlignment),
                    color = Color(getColor(R.color.suggestion_color)))
            }
            if(editing && addSuggestion.second[0] != "") {
                Box(modifier.weight(0.5f)) {
                    Button(onClick = onSubmit,
                        modifier = modifier.align(Alignment.CenterEnd)) {
                        Text("+")
                    }
                }
            }
        }
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
            val screenSelect = remember { mutableIntStateOf(0) }
            Column(modifier.fillMaxSize()) {
                when (screenSelect.intValue) {
                    0 -> {
                        Row(
                            modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(onClick = onBack) {
                                Text("Back")
                            }

                            val expanded = remember { mutableStateOf(false) }

                            Box {
                                Button(onClick = { expanded.value = true }) {
                                    Text("\u2630")
                                }

                                DropdownMenu(
                                    expanded = expanded.value,
                                    onDismissRequest = { expanded.value = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Edit Tags") },
                                        onClick = { screenSelect.intValue = 1; expanded.value = false }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            if (progExists(exercise)) {
                                                Text("Delete Program")
                                            } else {
                                                Text("Add Program")
                                            }
                                        },
                                        onClick = { screenSelect.intValue = 2; expanded.value = false }
                                    )
                                }
                            }
                        }
                        val ref = remember { mutableIntStateOf(0) }
                        key(ref.intValue) {
                            ExerciseEntryEdit(
                                exercise,
                                ref = { ref.intValue++ })
                        }
                    }
                    1 -> {
                        val ref = remember { mutableIntStateOf(-2000) }
                        key(ref.intValue) {
                            TagEdit(
                                exercise,
                                onBack={screenSelect.intValue = 0},
                                ref={ref.intValue++}
                            )
                        }

                    }
                    2 -> {
                        ProgramEdit(exercise, { screenSelect.intValue = 0 })
                    }
                }
            }
        }
    }

    @Composable
    fun TagEdit(exercise : String, onBack : () -> Unit, ref : () -> Unit, modifier : Modifier = Modifier) {
        Button(onClick = onBack) {
            Text("Back")
        }
        //have a list of current tags at the top
        val exerTags = exerciseAttrs[exercise]!!.tags
        Row(modifier.fillMaxWidth()) {
            for(tag in exerTags) {
                Button(onClick = {exerTags.remove(tag); save(); ref()}) {
                    Row {
                        Text(tag)
                        Text(" x", color = Color.Red)
                    }
                }
            }
        }
        //A separator
        Divider(color = Color(getColor(R.color.border_color)))
        //Other tags not applied to this object
        Row(modifier.fillMaxWidth()) {
            for(tag in (allTags - exerTags)) {
                Button(onClick = {exerTags.add(tag); save(); ref()}) {
                    Row {
                        Text(tag)
                        Text(" +", color = Color.Green)
                    }
                }
            }
        }
        Divider(color = Color(getColor(R.color.border_color)))
        //Add new tag option
        Row(
            modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        )
        {
            Text("New tag: ")
            val tagName = remember{mutableStateOf("")}
            BasicTextField(
                value = tagName.value,
                onValueChange = { tagName.value = it },
                modifier = modifier.background(Color.White)
            )
            Button(onClick={
                if(tagName.value.trim() != "") {
                    exerTags.add(tagName.value)
                    allTags.add(tagName.value)
                    save()
                    ref()
                }
            }) {
                Text("Submit")
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

                val hl = if(lastMatch % 2 == 0) "L" else "H"
                val setRep = "" + arr[lastMatch + 1].first + " x " + arr[lastMatch + 1].second
                val weight = "" + arr[lastMatch + 1].third + " lbs"

                return Pair(true, listOf(hl, setRep, weight))
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
        for(exStr in exerciseAttrs.keys) {
            fil += exerciseAttrs[exStr]!!.fileString() + "\n"
        }
        file.writeText(fil)
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

    private fun getProg(name : String) : Program? {
        for(prog in progs) {
            if(prog.exercise == name) {
                return prog
            }
        }
        return null
    }

    private fun getEntryColor(item : SessionEntry) : Color {
        val prog : Program = getProg(item.parent) ?: return Color.White

        return if(Triple(item.sets, item.reps, item.weight) in prog.weeks) {
            Color(getColor(R.color.match_color))
        } else {
            Color.White
        }
    }

    private fun isIncludedInFilter(name: String, filter: SnapshotStateSet<String>) : Boolean {
        // Right now adding multiple filters is an "or"
        for(attr in filter) {
            if(exerciseAttrs[name]!!.tags.contains(attr)) {
                return true
            }
        }
        return false
    }
}