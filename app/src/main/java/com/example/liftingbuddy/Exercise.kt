package com.example.liftingbuddy

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

//class Exercise(val name : String, val tags : MutableSet<String>) {
//    constructor(line : String) : this(
//        line.split(delimiter)[1],
//        line.split(delimiter).drop(2).toMutableSet()
//    ) {
//        tags.remove("") // Catch empty set case
//    }
//
//    fun fileString() : String {
//        return "$exerciseSignal$delimiter$name$delimiter" + tags.joinToString(delimiter)
//    }
//
//    fun getAttrString() : String {
//        return tags.joinToString(" | ")
//    }
//
//    override fun hashCode(): Int {
//        return name.hashCode() + tags.hashCode()
//    }
//
//    override fun equals(other: Any?): Boolean {
//        if(other == null || other !is Exercise) {
//            return false
//        }
//        return name == other.name && tags == other.tags
//    }
//}
//
//fun makeExerciseObject(name : String) : Exercise {
//    return Exercise(name, mutableSetOf())
//}

class Exercise(
    val name: String,
    initialTags: Set<String>
) {
    // Compose-safe state-backed Set
    var tags by mutableStateOf(initialTags)
        private set

    constructor(line: String) : this(
        name = line.split(delimiter)[1],
        initialTags = line.split(delimiter)
            .drop(2)
            .filter { it.isNotBlank() }
            .toSet()
    )

    fun addTag(tag: String) {
        tags = tags + tag
    }

    fun removeTag(tag: String) {
        tags = tags - tag
    }

    fun fileString(): String {
        return "$exerciseSignal$delimiter$name$delimiter" +
                tags.joinToString(delimiter)
    }

    fun getAttrString(): String {
        return tags.joinToString(" | ")
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Exercise) return false
        return name == other.name && tags == other.tags
    }

    override fun hashCode(): Int {
        return name.hashCode() * 31 + tags.hashCode()
    }
}

fun makeExerciseObject(name: String): Exercise {
    return Exercise(name, emptySet())
}