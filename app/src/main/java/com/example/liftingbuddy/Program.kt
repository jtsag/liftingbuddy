package com.example.liftingbuddy

class Program(val exercise : String, val max : Int) {
    val weeks : Array<Triple<Int, Int, Int>> = Array(16) { i -> Triple(i, i, i)}

    init {
        weeks[0] = Triple(4, 10, roundToFive(0.65 * max))
        weeks[2] = Triple(4, 8, roundToFive(0.7 * max))
        weeks[4] = Triple(3, 8, roundToFive(0.75 * max))
        weeks[6] = Triple(4, 5, roundToFive(0.8 * max))
        weeks[8] = Triple(3, 5, roundToFive(0.85 * max))
        weeks[10] = Triple(4, 3, roundToFive(0.9 * max))
        weeks[12] = Triple(3, 3, roundToFive(0.95 * max))
        weeks[14] = Triple(3, 2, max)

        for(i in 1 until 16 step 2) {
            weeks[i] = weeks[i-1]
        }
    }

    private fun roundToFive(num : Double) : Int {
        return (Math.round(num / 5.0) * 5).toInt()
    }

    constructor(str : String) : this(
        str.split(delimiter)[1],
        str.split(delimiter)[2].toInt()
    )

    fun fileString() : String {
        return "$programSignal$delimiter$exercise$delimiter$max"
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