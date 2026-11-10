
package com.frizzybeard.diettracker

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.frizzybeard.diettracker.data.DatabaseProvider
import com.frizzybeard.diettracker.data.DishEntity
import com.frizzybeard.diettracker.data.MealDishDetail
import com.frizzybeard.diettracker.data.MealDishEntity
import com.frizzybeard.diettracker.data.MealEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GlucoseTrackerApp()
                }
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* DATA                                                                        */
/* -------------------------------------------------------------------------- */

data class DishEntry(
    val dishId: Int,
    val name: String,
    val quantity: Int
)

data class MealHistoryEntry(
    val meal: MealEntity,
    val dishes: List<MealDishDetail>
)

enum class AppTab {
    HOME,
    MEALS,
    DISHES,
    SETTINGS
}

/* -------------------------------------------------------------------------- */
/* APP                                                                         */
/* -------------------------------------------------------------------------- */

@Composable
fun GlucoseTrackerApp() {

    var selectedTab by remember {
        mutableStateOf(AppTab.HOME)
    }

    Scaffold(
        bottomBar = {
            NavigationBar {

                NavigationBarItem(
                    selected = selectedTab == AppTab.HOME,
                    onClick = { selectedTab = AppTab.HOME },
                    icon = {
                        Icon(Icons.Default.Home, contentDescription = "Home")
                    },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = selectedTab == AppTab.MEALS,
                    onClick = { selectedTab = AppTab.MEALS },
                    icon = {
                        Icon(Icons.Default.Restaurant, contentDescription = "Meals")
                    },
                    label = { Text("Meals") }
                )

                NavigationBarItem(
                    selected = selectedTab == AppTab.DISHES,
                    onClick = { selectedTab = AppTab.DISHES },
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "Dishes"
                        )
                    },
                    label = { Text("Dishes") }
                )

                NavigationBarItem(
                    selected = selectedTab == AppTab.SETTINGS,
                    onClick = { selectedTab = AppTab.SETTINGS },
                    icon = {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->

        when (selectedTab) {
            AppTab.HOME -> HomeScreen(
                modifier = Modifier.padding(padding)
            )

            AppTab.MEALS -> MealsTableScreen(
                modifier = Modifier.padding(padding)
            )

            AppTab.DISHES -> DishesScreen(
                modifier = Modifier.padding(padding)
            )

            AppTab.SETTINGS -> SettingsScreen(
                modifier = Modifier.padding(padding)
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/* HOME                                                                        */
/* -------------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val database = remember {
        DatabaseProvider.getDatabase(context)
    }

    var mealType by remember { mutableStateOf("Breakfast") }
    var mealMenuExpanded by remember { mutableStateOf(false) }

    var dishes by remember {
        mutableStateOf(emptyList<DishEntry>())
    }

    var availableDishes by remember {
        mutableStateOf(emptyList<DishEntity>())
    }

    var isLoadingDishes by remember {
        mutableStateOf(true)
    }

    var showAddDishDialog by remember {
        mutableStateOf(false)
    }

    var preMeal by remember { mutableStateOf("") }
    var twoHour by remember { mutableStateOf("") }
    var lyumjev by remember { mutableStateOf("") }
    var basalog by remember { mutableStateOf("") }

    var selectedDateTime by remember {
        mutableStateOf(Calendar.getInstance())
    }

    val dateFormat = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }

    val timeFormat = remember {
        SimpleDateFormat("hh:mm a", Locale.getDefault())
    }

    var isSaving by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            DatabaseProvider.ensureDishesSeeded(context)
            availableDishes = database.dishDao().getAllDishes()
        } finally {
            isLoadingDishes = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Record Meal",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Date",
                    style = MaterialTheme.typography.labelSmall
                )

                TextButton(
                    onClick = {
                        val current = selectedDateTime.clone() as Calendar

                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                selectedDateTime =
                                    (selectedDateTime.clone() as Calendar).apply {
                                        set(year, month, day)
                                    }
                            },
                            current.get(Calendar.YEAR),
                            current.get(Calendar.MONTH),
                            current.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                ) {
                    Text(dateFormat.format(selectedDateTime.time))
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Time",
                    style = MaterialTheme.typography.labelSmall
                )

                TextButton(
                    onClick = {
                        val current = selectedDateTime.clone() as Calendar

                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                selectedDateTime =
                                    (selectedDateTime.clone() as Calendar).apply {
                                        set(Calendar.HOUR_OF_DAY, hour)
                                        set(Calendar.MINUTE, minute)
                                    }
                            },
                            current.get(Calendar.HOUR_OF_DAY),
                            current.get(Calendar.MINUTE),
                            false
                        ).show()
                    }
                ) {
                    Text(timeFormat.format(selectedDateTime.time))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = mealMenuExpanded,
            onExpandedChange = {
                mealMenuExpanded = !mealMenuExpanded
            }
        ) {

            OutlinedTextField(
                value = mealType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Meal") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = mealMenuExpanded
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(
                        ExposedDropdownMenuAnchorType.PrimaryNotEditable
                    )
            )

            ExposedDropdownMenu(
                expanded = mealMenuExpanded,
                onDismissRequest = {
                    mealMenuExpanded = false
                }
            ) {

                listOf(
                    "Breakfast",
                    "Lunch",
                    "Dinner",
                    "Snack"
                ).forEach { type ->

                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            mealType = type
                            mealMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Dishes",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            items(
                items = dishes,
                key = { it.dishId }
            ) { dish ->

                DishQuantityRow(
                    dish = dish,
                    onDecrease = {
                        dishes = dishes.map {
                            if (it.dishId == dish.dishId) {
                                it.copy(
                                    quantity = (it.quantity - 1)
                                        .coerceAtLeast(0)
                                )
                            } else {
                                it
                            }
                        }
                    },
                    onIncrease = {
                        dishes = dishes.map {
                            if (it.dishId == dish.dishId) {
                                it.copy(quantity = it.quantity + 1)
                            } else {
                                it
                            }
                        }
                    }
                )
            }

            item {

                OutlinedButton(
                    onClick = {
                        showAddDishDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoadingDishes
                ) {

                    Icon(Icons.Default.Add, contentDescription = null)

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        if (isLoadingDishes) "Loading dishes..."
                        else "Add Dish"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Glucose",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                GlucoseField(
                    label = "Before meal",
                    value = preMeal,
                    onValueChange = {
                        preMeal = it
                        saveMessage = null
                        errorMessage = null
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                GlucoseField(
                    label = "2 hours",
                    value = twoHour,
                    onValueChange = {
                        twoHour = it
                        saveMessage = null
                        errorMessage = null
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Insulin",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = lyumjev,
                    onValueChange = {
                        lyumjev = it
                        saveMessage = null
                        errorMessage = null
                    },
                    label = { Text("Lyumjev") },
                    suffix = { Text("units") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = basalog,
                    onValueChange = {
                        basalog = it
                        saveMessage = null
                        errorMessage = null
                    },
                    label = { Text("Basalog") },
                    suffix = { Text("units") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                saveMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = {

                        val selectedDishes = dishes.filter {
                            it.quantity > 0
                        }

                        if (selectedDishes.isEmpty()) {
                            errorMessage = "Please add at least one dish."
                            saveMessage = null
                            return@Button
                        }

                        val glucoseBefore = preMeal.toDoubleOrNull()
                        val glucoseAfter = twoHour.toDoubleOrNull()
                        val lyumjevDose = lyumjev.toDoubleOrNull()
                        val basalogDose = basalog.toDoubleOrNull()

                        if (
                            (preMeal.isNotBlank() && glucoseBefore == null) ||
                            (twoHour.isNotBlank() && glucoseAfter == null) ||
                            (lyumjev.isNotBlank() && lyumjevDose == null) ||
                            (basalog.isNotBlank() && basalogDose == null)
                        ) {
                            errorMessage = "Please enter valid numeric values."
                            saveMessage = null
                            return@Button
                        }

                        if (
                            listOf(
                                glucoseBefore,
                                glucoseAfter,
                                lyumjevDose,
                                basalogDose
                            ).any { it != null && it < 0 }
                        ) {
                            errorMessage = "Values cannot be negative."
                            saveMessage = null
                            return@Button
                        }

                        isSaving = true
                        errorMessage = null
                        saveMessage = null

                        scope.launch {
                            try {

                                val meal = MealEntity(
                                    mealType = mealType,
                                    timestamp = System.currentTimeMillis(),
                                    preMealGlucose = glucoseBefore,
                                    twoHourGlucose = glucoseAfter,
                                    lyumjevUnits = lyumjevDose,
                                    basalogUnits = basalogDose
                                )

                                val mealDishes = selectedDishes.map {
                                    MealDishEntity(
                                        mealId = 0,
                                        dishId = it.dishId,
                                        quantity = it.quantity
                                    )
                                }

                                database.mealDao().insertMealWithDishes(
                                    meal = meal,
                                    dishes = mealDishes
                                )

                                dishes = emptyList()
                                preMeal = ""
                                twoHour = ""
                                lyumjev = ""
                                basalog = ""

                                saveMessage = "Meal saved successfully!"

                            } catch (e: Exception) {
                                errorMessage = "Unable to save meal. Please try again."
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                ) {
                    Text(if (isSaving) "SAVING..." else "SAVE")
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showAddDishDialog) {

        AddDishDialog(
            availableDishes = availableDishes,
            existingDishIds = dishes.map { it.dishId },
            onDismiss = {
                showAddDishDialog = false
            },
            onDishSelected = { selectedDish ->

                if (dishes.none {
                        it.dishId == selectedDish.id
                    }) {
                    dishes = dishes + DishEntry(
                        dishId = selectedDish.id,
                        name = selectedDish.dishName,
                        quantity = 1
                    )
                }

                showAddDishDialog = false
            }
        )
    }
}

/* -------------------------------------------------------------------------- */
/* SETTINGS SCREEN                                                             */
/* -------------------------------------------------------------------------- */

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Meal reminders will be configured here.")
    }
}

/* -------------------------------------------------------------------------- */
/* DISH QUANTITY                                                               */
/* -------------------------------------------------------------------------- */

@Composable
fun DishQuantityRow(
    dish: DishEntry,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {

    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = dish.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )

            IconButton(onClick = onDecrease) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease")
            }

            Text(
                text = dish.quantity.toString(),
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(onClick = onIncrease) {
                Icon(Icons.Default.Add, contentDescription = "Increase")
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* GLUCOSE FIELD                                                               */
/* -------------------------------------------------------------------------- */

@Composable
fun GlucoseField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        suffix = { Text("mg/dL") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        ),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

/* -------------------------------------------------------------------------- */
/* ADD DISH DIALOG                                                             */
/* -------------------------------------------------------------------------- */

@Composable
fun AddDishDialog(
    availableDishes: List<DishEntity>,
    existingDishIds: List<Int>,
    onDismiss: () -> Unit,
    onDishSelected: (DishEntity) -> Unit
) {

    val selectableDishes = availableDishes.filter {
        it.id !in existingDishIds
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("Add Dish")
        },

        text = {

            if (selectableDishes.isEmpty()) {
                Text("All available dishes have been added.")
            } else {

                LazyColumn {

                    items(
                        items = selectableDishes,
                        key = { it.id }
                    ) { dish ->

                        TextButton(
                            onClick = {
                                onDishSelected(dish)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Text(
                                    text = dish.dishName,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                if (dish.mainIngredients.isNotBlank()) {
                                    Text(
                                        text = dish.mainIngredients,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },

        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/* -------------------------------------------------------------------------- */
/* MEALS SCREEN                                                                */
/* -------------------------------------------------------------------------- */

@Composable
fun MealsScreen(
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

    suspend fun loadMeals() {

        val mealRecords = database.mealDao().getAllMeals()

        meals = mealRecords.map { meal ->
            MealHistoryEntry(
                meal = meal,
                dishes = database.mealDao().getMealDishDetails(meal.id)
            )
        }
    }

    LaunchedEffect(Unit) {
        try {
            loadMeals()
        } catch (e: Exception) {
            errorMessage = "Unable to load meal history."
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
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

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(
                        items = meals,
                        key = { it.meal.id }
                    ) { entry ->

                        MealHistoryCard(entry)
                    }
                }
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* MEAL HISTORY CARD                                                           */
/* -------------------------------------------------------------------------- */

@Composable
fun MealHistoryCard(
    entry: MealHistoryEntry
) {

    val meal = entry.meal

    val dateFormat = remember {
        SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.getDefault()
        )
    }

    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = meal.mealType,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = dateFormat.format(Date(meal.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp)
            )

            Text(
                text = "Dishes",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            entry.dishes.forEach { dish ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(dish.dishName)

                    Text("Qty: ${dish.quantity}")
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp)
            )

            Text(
                text = "Glucose",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            MealDetailRow(
                label = "Before meal",
                value = meal.preMealGlucose?.let {
                    "$it mg/dL"
                } ?: "Not recorded"
            )

            MealDetailRow(
                label = "2 hours",
                value = meal.twoHourGlucose?.let {
                    "$it mg/dL"
                } ?: "Not recorded"
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp)
            )

            Text(
                text = "Insulin",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            MealDetailRow(
                label = "Lyumjev",
                value = meal.lyumjevUnits?.let {
                    "$it units"
                } ?: "Not recorded"
            )

            MealDetailRow(
                label = "Basalog",
                value = meal.basalogUnits?.let {
                    "$it units"
                } ?: "Not recorded"
            )
        }
    }
}

@Composable
fun MealDetailRow(
    label: String,
    value: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(text = value)
    }
}

/* -------------------------------------------------------------------------- */
/* DISHES SCREEN                                                               */
/* -------------------------------------------------------------------------- */

@Composable
fun DishesScreen(
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val database = remember {
        DatabaseProvider.getDatabase(context)
    }

    var dishes by remember {
        mutableStateOf(emptyList<DishEntity>())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var showDialog by remember {
        mutableStateOf(false)
    }

    var dishName by remember {
        mutableStateOf("")
    }

    var ingredients by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    suspend fun loadDishes() {
        DatabaseProvider.ensureDishesSeeded(context)
        dishes = database.dishDao().getAllDishes()
    }

    LaunchedEffect(Unit) {
        try {
            loadDishes()
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Dishes",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    dishName = ""
                    ingredients = ""
                    errorMessage = null
                    showDialog = true
                }
            ) {

                Icon(Icons.Default.Add, contentDescription = null)

                Spacer(modifier = Modifier.width(4.dp))

                Text("Add")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {

            isLoading -> {
                Text("Loading dishes...")
            }

            dishes.isEmpty() -> {
                Text("No dishes found.")
            }

            else -> {

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    items(
                        items = dishes,
                        key = { it.id }
                    ) { dish ->

                        Surface(
                            tonalElevation = 2.dp,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Column(
                                modifier = Modifier.padding(14.dp)
                            ) {

                                Text(
                                    text = dish.dishName,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = dish.mainIngredients.ifBlank {
                                        "Ingredients not added"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {

        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },

            title = {
                Text("Add New Dish")
            },

            text = {

                Column {

                    OutlinedTextField(
                        value = dishName,
                        onValueChange = { dishName = it },
                        label = { Text("Dish name") },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = ingredients,
                        onValueChange = { ingredients = it },
                        label = { Text("Main ingredients") }
                    )

                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        if (dishName.isBlank()) {
                            errorMessage = "Dish name is required."
                        } else {

                            scope.launch {
                                try {
                                    database.dishDao().insertDish(
                                        DishEntity(
                                            dishName = dishName.trim(),
                                            mainIngredients = ingredients.trim()
                                        )
                                    )

                                    loadDishes()
                                    showDialog = false

                                } catch (e: Exception) {
                                    errorMessage = "Unable to save dish."
                                }
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
