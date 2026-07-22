package com.own.remindme.domain.model

enum class RepeatType {

    NONE,

    DAILY,

    ALTERNATE,

    WEEKLY,

    TWO_WEEKS,

    THREE_WEEKS,

    MONTHLY,

    THREE_MONTHS,

    SIX_MONTHS,

    YEARLY,

    CUSTOM
}

val RepeatType.label: String
    get() = when (this) {
        RepeatType.NONE -> "None"
        RepeatType.DAILY -> "Daily"
        RepeatType.ALTERNATE -> "Alternate"
        RepeatType.WEEKLY -> "In a week"
        RepeatType.TWO_WEEKS -> "In 2 weeks"
        RepeatType.THREE_WEEKS -> "In 3 weeks"
        RepeatType.MONTHLY -> "In a month"
        RepeatType.THREE_MONTHS -> "In 3 months"
        RepeatType.SIX_MONTHS -> "In 6 months"
        RepeatType.YEARLY -> "In a year"
        RepeatType.CUSTOM -> "Custom"
    }