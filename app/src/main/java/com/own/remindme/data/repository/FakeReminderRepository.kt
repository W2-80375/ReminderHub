package com.own.remindme.data.repository

import com.own.remindme.presentation.home.components.ReminderUiModel
import com.own.remindme.ui.theme.BillsColor
import com.own.remindme.ui.theme.DocumentColor
import com.own.remindme.ui.theme.MedicineColor
import com.own.remindme.ui.theme.VehicleColor


class FakeReminderRepository {

    fun getToday() = listOf(

        ReminderUiModel(

            1,

            "Vitamin D",

            "Medicine",

            "Today • 9:00 AM",

            "Daily",

            MedicineColor,

            false

        ),

        ReminderUiModel(

            2,

            "Electricity Bill",

            "Bills",

            "Today • 6:00 PM",

            "Monthly",

            BillsColor,

            false

        )

    )

    fun getUpcoming() = listOf(

        ReminderUiModel(

            3,

            "Bike Service",

            "Vehicle",

            "Tomorrow",

            "Every 6 Months",

            VehicleColor,

            false

        ),

        ReminderUiModel(

            4,

            "Passport Renewal",

            "Documents",

            "15 Aug",

            "Yearly",

            DocumentColor,

            false

        )

    )

}