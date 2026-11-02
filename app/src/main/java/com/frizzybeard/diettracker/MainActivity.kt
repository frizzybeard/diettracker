package com.frizzybeard.diettracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
//import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
//import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.ui.platform.LocalContext
import com.frizzybeard.diettracker.data.DatabaseProvider
import com.frizzybeard.diettracker.data.DishEntity


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
    val name: String,
    val quantity: Int
)

enum class AppTab {
    HOME,
    MEALS,
    DISHES,

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
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = selectedTab == AppTab.MEALS,
                    onClick = { selectedTab = AppTab.MEALS },
                    icon = {
                        Icon(
                            Icons.Default.Restaurant,
                            contentDescription = "Meals"
                        )
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


            }
        }
    ) { padding ->

        when (selectedTab) {

            AppTab.HOME -> HomeScreen(
                modifier = Modifier.padding(padding)
            )

            AppTab.MEALS -> MealsScreen(
                modifier = Modifier.padding(padding)
            )

            AppTab.DISHES -> DishesScreen(
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

    var mealType by remember {
        mutableStateOf("Breakfast")
    }

    var mealMenuExpanded by remember {
        mutableStateOf(false)
    }

    var dishes by remember {
        mutableStateOf(
            emptyList<DishEntry>())
    }

    var showAddDishDialog by remember {
        mutableStateOf(false)
    }

    var preMeal by remember {
        mutableStateOf("")
    }


    var twoHour by remember {
        mutableStateOf("")
    }

    var lyumjev by remember {
        mutableStateOf("")
    }

    var basalog by remember {
        mutableStateOf("")
    }

    val availableDishes = listOf(
        "Multi-grain Dosa",
        "Double Omelette",
        "Rice",
        "Vegetable Curry",
        "Vegetable Poriyal",
        "Coconut Chutney",
        "Buttermilk",
        "Guava"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Record Meal",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        /* Meal type */

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

                    androidx.compose.material3.DropdownMenuItem(
                        text = {
                            Text(type)
                        },
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

        /* Dish list */

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            items(
                items = dishes,
                key = { it.name }
            ) { dish ->

                DishQuantityRow(
                    dish = dish,
                    onDecrease = {

                        dishes = dishes.map {

                            if (it.name == dish.name) {
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

                            if (it.name == dish.name) {
                                it.copy(
                                    quantity = it.quantity + 1
                                )
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
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Icon(
                        Icons.Default.Add,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text("Add Dish")
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = "Glucose",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                GlucoseField(
                    label = "Before meal",
                    value = preMeal,
                    onValueChange = {
                        preMeal = it
                    }
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                GlucoseField(
                    label = "2 hours",
                    value = twoHour,
                    onValueChange = {
                        twoHour = it
                    }
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = "Insulin",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                OutlinedTextField(
                    value = lyumjev,
                    onValueChange = {
                        lyumjev = it
                    },
                    label = {
                        Text("Lyumjev")
                    },
                    suffix = {
                        Text("units")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                OutlinedTextField(
                    value = basalog,
                    onValueChange = {
                        basalog = it
                    },
                    label = {
                        Text("Basalog")
                    },
                    suffix = {
                        Text("units")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Button(
                    onClick = {
                        // Database will be added later.
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("SAVE")
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )
            }
        }
    }

    /* Add dish dialog */

    if (showAddDishDialog) {

        AddDishDialog(
            availableDishes = availableDishes,
            existingDishes = dishes.map { it.name },
            onDismiss = {
                showAddDishDialog = false
            },
            onDishSelected = { selectedDish ->

                if (dishes.none { it.name == selectedDish }) {

                    dishes = dishes + DishEntry(
                        name = selectedDish,
                        quantity = 1
                    )
                }

                showAddDishDialog = false
            }
        )
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
                .padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = dish.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )

            IconButton(
                onClick = onDecrease
            ) {

                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Decrease"
                )
            }

            Text(
                text = dish.quantity.toString(),
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(
                onClick = onIncrease
            ) {

                Icon(
                    Icons.Default.Add,
                    contentDescription = "Increase"
                )
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
        label = {
            Text(label)
        },
        suffix = {
            Text("mg/dL")
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

/* -------------------------------------------------------------------------- */
/* ADD DISH                                                                    */
/* -------------------------------------------------------------------------- */

@Composable
fun AddDishDialog(
    availableDishes: List<String>,
    existingDishes: List<String>,
    onDismiss: () -> Unit,
    onDishSelected: (String) -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("Add Dish")
        },

        text = {

            LazyColumn {

                items(
                    availableDishes.filter {
                        it !in existingDishes
                    }
                ) { dish ->

                    TextButton(
                        onClick = {
                            onDishSelected(dish)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text = dish,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },

        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

/* -------------------------------------------------------------------------- */
/* OTHER SCREENS                                                               */
/* -------------------------------------------------------------------------- */

@Composable
fun MealsScreen(
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = "Meals",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = "Your recorded meals will appear here."
        )
    }
}



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

    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var dishName by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }

    suspend fun loadDishes() {
        DatabaseProvider.ensureDishesSeeded(context)
        dishes = database.dishDao().getAllDishes()
    }

    LaunchedEffect(Unit) {
        loadDishes()
        isLoading = false
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

            Button(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Add")
            }
        }

        Spacer(Modifier.height(12.dp))

        when {
            isLoading -> Text("Loading dishes...")

            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(dishes, key = { it.id }) { dish ->
                    Surface(
                        tonalElevation = 2.dp,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(
                                dish.dishName,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                dish.mainIngredients.ifBlank {
                                    "Ingredients not added"
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add New Dish") },
            text = {
                Column {
                    OutlinedTextField(
                        value = dishName,
                        onValueChange = { dishName = it },
                        label = { Text("Dish name") },
                        singleLine = true
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = ingredients,
                        onValueChange = { ingredients = it },
                        label = { Text("Main ingredients") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (dishName.isNotBlank()) {
                            LaunchedEffect(Unit) {
                                database.dishDao().insertDish(
                                    DishEntity(
                                        dishName = dishName.trim(),
                                        mainIngredients = ingredients.trim()
                                    )
                                )
                                loadDishes()
                            }
                            dishName = ""
                            ingredients = ""
                            showDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}




