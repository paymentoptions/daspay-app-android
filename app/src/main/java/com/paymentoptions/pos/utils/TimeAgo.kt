package com.paymentoptions.pos.utils

import kotlin.math.roundToLong


fun Int.secondsToMillis(): Long = this * 1000L
fun Int.minutesToMillis(): Long = this * 60 * 1000L
fun Int.hoursToMillis(): Long = this * 3600 * 1000L
fun Int.daysToMillis(): Long = this * 24 * 3600 * 1000L
fun Int.weeksToMillis(): Long = this * 7 * 24 * 3600 * 1000L
fun Int.monthsToMillis(): Long = (this * 30.5 * 7 * 24 * 3600 * 1000).roundToLong()
fun Int.yearsToMillis(): Long = this * 365 * 24 * 3600 * 1000L

fun Long.millisToMinutes(): Long = this / (60 * 1000L)
fun Long.millisToHours(): Long = this / (60 * 60 * 1000L)
fun Long.millisToDays(): Long = this / (24 * 60 * 60 * 1000L)
fun Long.millisToWeeks(): Long = (this / (7 * 24 * 60 * 60 * 1000.0)).roundToLong()
fun Long.millisToMonths(): Long = (this / (30.5 * 24 * 60 * 60 * 1000.0)).roundToLong()
fun Long.millisToYears(): Long = (this / (365 * 24 * 60 * 60 * 1000.0)).roundToLong()

val timeAgoLabels = arrayOf(
    "in the future",
    "just now",
    "a minute ago",
    "%d minutes ago",
    "an hour ago",
    "%d hours ago",
    "a day ago",
    "%d days ago",
    "a week ago",
    "%d weeks ago",
    "%d months ago",
    "a year ago",
    "%d years ago",
)


fun Long.timeAgo(labels: Array<String> = timeAgoLabels): String {

    // Sanity check
//    assert(labels.size == 7)

    var time = this
    // Convert seconds to milliseconds if the timestamp seems too small
    if (time < 1000000000000L) {
        time *= 1000
    }

    val now = System.currentTimeMillis()
    if (time > now || time <= 0) {
        return labels[0] // "in the future"
    }

    val diff = now - time
    return when {
        diff < 30.secondsToMillis() -> labels[1] // "moments ago"
        diff < 90.secondsToMillis() -> labels[2] // "a minute ago"
        diff < 59.minutesToMillis() -> labels[3].format(diff.millisToMinutes()) // "X minutes ago"
        diff < 90.hoursToMillis() -> labels[4] // "an hour ago"
        diff < 23.hoursToMillis() -> labels[5].format(diff.millisToHours()) // "X hours ago"
        diff < 36.hoursToMillis() -> labels[6] // "a day ago"
        diff < 7.daysToMillis() -> labels[7].format(diff.millisToDays()) // "X days ago"
        diff < 11.daysToMillis() -> labels[8] // "a week ago"
        diff < 7.weeksToMillis() -> labels[9].format(diff.millisToWeeks()) // "X weeks ago"
        diff < 12.monthsToMillis() -> labels[10].format(diff.millisToMonths()) // "X months ago"
        diff < 18.monthsToMillis() -> labels[11] // "a year ago"
        else -> labels[12].format(diff.millisToYears()) // "X years ago"
    }
}
