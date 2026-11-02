
package com.frizzybeard.diettracker

import android.content.Intent
import androidx.core.content.FileProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.frizzybeard.diettracker.data.DatabaseProvider
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MealsTableScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val database = remember {
        DatabaseProvider.getDatabase(context)
    }

    var meals by remember {
        mutableStateOf(emptyList<MealHistoryEntry>())
    }

    var isLoading by remember { mutableStateOf(true) }
    var isExporting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dateFormat = remember {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    }

    LaunchedEffect(Unit) {
        try {
            val records = database.mealDao().getAllMeals()

            meals = records.map { meal ->
                MealHistoryEntry(
                    meal = meal,
                    dishes = database.mealDao()
                        .getMealDishDetails(meal.id)
                )
            }
        } catch (e: Exception) {
            errorMessage = "Unable to load meal history."
        } finally {
            isLoading = false
        }
    }

    fun exportMeals() {
        scope.launch {
            isExporting = true
            errorMessage = null

            try {
                val exportDateFormat = SimpleDateFormat(
                    "dd MMM yyyy, hh:mm a",
                    Locale.getDefault()
                )

                val rows = meals.joinToString("\n") { entry ->
                    val meal = entry.meal

                    val dishes = entry.dishes.joinToString("<br>") {
                        "${escapeHtml(it.dishName)} × ${it.quantity}"
                    }.ifBlank { "—" }

                    val glucose =
                        "Before: ${meal.preMealGlucose ?: "—"}<br>" +
                                "2 hrs: ${meal.twoHourGlucose ?: "—"}"

                    val insulin =
                        "Lyumjev: ${meal.lyumjevUnits ?: "—"} U<br>" +
                                "Basalog: ${meal.basalogUnits ?: "—"} U"

                    """
                    <tr>
                        <td>${exportDateFormat.format(Date(meal.timestamp))}</td>
                        <td>${escapeHtml(meal.mealType)}</td>
                        <td>$dishes</td>
                        <td>$glucose</td>
                        <td>$insulin</td>
                    </tr>
                    """.trimIndent()
                }

                val html = """
                    <html>
                    <head>
                    <meta charset="UTF-8">
                    <style>
                        table {
                            border-collapse: collapse;
                            font-family: Arial, sans-serif;
                            font-size: 11pt;
                        }
                        th {
                            background-color: #D9EAF7;
                            font-weight: bold;
                            border: 1px solid #808080;
                            padding: 8px;
                            text-align: left;
                        }
                        td {
                            border: 1px solid #808080;
                            padding: 8px;
                            vertical-align: top;
                            mso-number-format: "\@";
                        }
                    </style>
                    </head>
                    <body>
                    <table>
                        <thead>
                            <tr>
                                <th>Date</th>
                                <th>Meal</th>
                                <th>Dish</th>
                                <th>Glucose</th>
                                <th>Insulin</th>
                            </tr>
                        </thead>
                        <tbody>
                            $rows
                        </tbody>
                    </table>
                    </body>
                    </html>
                """.trimIndent()

                val exportDirectory = File(
                    context.cacheDir,
                    "exports"
                ).apply {
                    mkdirs()
                }

                val fileName = "DietTracker_Meals_" +
                        SimpleDateFormat(
                            "yyyyMMdd_HHmm",
                            Locale.getDefault()
                        ).format(Date()) + ".xls"

                val exportFile = File(exportDirectory, fileName)
                exportFile.writeText(html, Charsets.UTF_8)

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    exportFile
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.ms-excel"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        "Diet Tracker - Meal History"
                    )
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Attached is the complete meal history exported from Diet Tracker."
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(
                    Intent.createChooser(
                        shareIntent,
                        "Share meal history"
                    )
                )

            } catch (e: Exception) {
                errorMessage = "Export failed: ${e.message}"
            } finally {
                isExporting = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = "Meals",
                style = MaterialTheme.typography.headlineSmall
            )

            Button(
                onClick = { exportMeals() },
                enabled = meals.isNotEmpty() && !isExporting
            ) {
                Text(if (isExporting) "Exporting..." else "Export")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        when {
            isLoading -> {
                Text("Loading meals...")
            }

            meals.isEmpty() -> {
                Text("No meals recorded yet.")
            }

            else -> {
                val horizontalState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(horizontalState)
                ) {
                    // Fixed header
                    Row(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(vertical = 12.dp)
                    ) {
                        TableCell("Date", 120, true)
                        TableCell("Meal", 100, true)
                        TableCell("Dish", 240, true)
                        TableCell("Glucose", 140, true)
                        TableCell("Insulin", 140, true)
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(
                            items = meals,
                            key = { it.meal.id }
                        ) { entry ->
                            val meal = entry.meal

                            val dishText = entry.dishes.joinToString(
                                separator = "\n"
                            ) {
                                "${it.dishName} × ${it.quantity}"
                            }.ifBlank { "—" }

                            val glucoseText =
                                "Before: ${meal.preMealGlucose ?: "—"}\n" +
                                        "2 hrs: ${meal.twoHourGlucose ?: "—"}"

                            val insulinText =
                                "L: ${meal.lyumjevUnits ?: "—"} U\n" +
                                        "B: ${meal.basalogUnits ?: "—"} U"

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp)
                            ) {
                                TableCell(
                                    dateFormat.format(Date(meal.timestamp)),
                                    120
                                )
                                TableCell(meal.mealType, 100)
                                TableCell(dishText, 240)
                                TableCell(glucoseText, 140)
                                TableCell(insulinText, 140)
                            }

                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

private fun escapeHtml(value: String): String {
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")
}

@Composable
private fun TableCell(
    value: String,
    widthDp: Int,
    isHeader: Boolean = false
) {
    Text(
        text = value,
        modifier = Modifier
            .width(widthDp.dp)
            .padding(horizontal = 8.dp),
        style = if (isHeader) {
            MaterialTheme.typography.titleSmall
        } else {
            MaterialTheme.typography.bodySmall
        },
        fontWeight = if (isHeader) {
            FontWeight.Bold
        } else {
            FontWeight.Normal
        }
    )
}
