package com.example.liftingbuddy

class Exercise(val name : String, val tags : MutableSet<String>) {
    constructor(line : String) : this(
        line.split(delimiter)[1],
        line.split(delimiter).drop(2).toMutableSet()
    )

    fun fileString() : String {
        return "$exerciseSignal$delimiter$name$delimiter" + tags.joinToString(delimiter)
    }

    fun getAttrString() : String {
        return tags.joinToString(" | ")
    }

    override fun hashCode(): Int {
        return name.hashCode() + tags.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if(other == null || other !is Exercise) {
            return false
        }
        return name == other.name && tags == other.tags
    }
}