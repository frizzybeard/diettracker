
package com.frizzybeard.diettracker

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.frizzybeard.diettracker.data.DatabaseProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MealsTableScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val database = remember {
        DatabaseProvider.getDatabase(context)
    }

    var meals by remember {
        mutableStateOf(emptyList<MealHistoryEntry>())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val dateFormat = remember {
        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Meals",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                Text("Loading meals...")
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error
                )
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
                    // Fixed header: remains visible while rows scroll.
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
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
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
                            }.ifBlank {
                                "—"
                            }

                            val glucoseText =
                                "Before: ${meal.preMealGlucose?.toString() ?: "—"}\n" +
                                        "2 hrs: ${meal.twoHourGlucose?.toString() ?: "—"}"

                            val insulinText =
                                "L: ${meal.lyumjevUnits?.toString() ?: "—"} U\n" +
                                        "B: ${meal.basalogUnits?.toString() ?: "—"} U"

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp)
                            ) {
                                TableCell(
                                    dateFormat.format(Date(meal.timestamp)),
                                    120
                                )

                                TableCell(
                                    meal.mealType,
                                    100
                                )

                                TableCell(
                                    dishText,
                                    240
                                )

                                TableCell(
                                    glucoseText,
                                    140
                                )

                                TableCell(
                                    insulinText,
                                    140
                                )
                            }

                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
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
