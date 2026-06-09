package com.bitacora.pro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Date picker dialog for selecting agenda item due dates.
 * Displays a calendar with month/year navigation.
 */
@Composable
fun DatePickerDialog(
    onDateSelected: (Long, String) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = remember { Calendar.getInstance() }
    val currentMonth = remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    val currentYear = remember { mutableStateOf(calendar.get(Calendar.YEAR)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Seleccionar Fecha")
                Spacer(modifier = Modifier.height(8.dp))
                MonthYearSelector(
                    month = currentMonth.value,
                    year = currentYear.value,
                    onMonthYearChange = { month, year ->
                        currentMonth.value = month
                        currentYear.value = year
                    }
                )
            }
        },
        text = {
            CalendarGrid(
                month = currentMonth.value,
                year = currentYear.value,
                onDateSelected = { day ->
                    val selectedCalendar = Calendar.getInstance().apply {
                        set(currentYear.value, currentMonth.value, day)
                    }
                    val timestamp = selectedCalendar.timeInMillis
                    val dateText = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
                    onDateSelected(timestamp, dateText)
                }
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

/**
 * Month and year selector with navigation arrows.
 */
@Composable
private fun MonthYearSelector(
    month: Int,
    year: Int,
    onMonthYearChange: (Int, Int) -> Unit
) {
    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                if (month == 0) {
                    onMonthYearChange(11, year - 1)
                } else {
                    onMonthYearChange(month - 1, year)
                }
            }
        ) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Mes anterior")
        }

        Text(
            "${monthNames[month]} $year",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        IconButton(
            onClick = {
                if (month == 11) {
                    onMonthYearChange(0, year + 1)
                } else {
                    onMonthYearChange(month + 1, year)
                }
            }
        ) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Próximo mes")
        }
    }
}

/**
 * Calendar grid showing days of the month.
 */
@Composable
private fun CalendarGrid(
    month: Int,
    year: Int,
    onDateSelected: (Int) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        set(year, month, 1)
    }

    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val daysList = mutableListOf<Int?>()
    // Add empty slots for days before month starts
    repeat(firstDayOfWeek) {
        daysList.add(null)
    }
    // Add days of month
    repeat(daysInMonth) { day ->
        daysList.add(day + 1)
    }

    val weekDays = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Week day headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weekDays.forEach { day ->
                Text(
                    day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Calendar days grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(daysList) { day ->
                if (day != null) {
                    DayCell(
                        day = day,
                        isToday = isToday(day, month, year),
                        onClick = { onDateSelected(day) }
                    )
                } else {
                    Box(modifier = Modifier.size(40.dp))
                }
            }
        }
    }
}

/**
 * Individual day cell in the calendar.
 */
@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                color = if (isToday) Color(0xFF00897B) else Color.Transparent,
                shape = MaterialTheme.shapes.small
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            day.toString(),
            textAlign = TextAlign.Center,
            color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * Check if a given day is today.
 */
private fun isToday(day: Int, month: Int, year: Int): Boolean {
    val today = Calendar.getInstance()
    return day == today.get(Calendar.DAY_OF_MONTH) &&
            month == today.get(Calendar.MONTH) &&
            year == today.get(Calendar.YEAR)
}
