package com.example.liftingbuddy

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date

class SessionEntry(val parent : String,
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
        str.split(delimiter)[0],
        str.split(delimiter)[1].toInt(),
        str.split(delimiter)[2].toInt(),
        str.split(delimiter)[3].toInt(),
        str.split(delimiter)[4]
    )

    fun fileString() : String {
        return "$parent$delimiter$sets$delimiter$reps$delimiter$weight$delimiter$date"
    }

    fun displayParts() : List<String> {
        return listOf(
            date,
            "$sets x $reps",
            "$weight lbs"
        )
    }

    override fun equals(other : Any?) : Boolean {
        if(other == null || other !is SessionEntry) {
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