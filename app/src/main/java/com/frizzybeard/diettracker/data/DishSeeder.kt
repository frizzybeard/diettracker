package com.frizzybeard.diettracker.data

object DishSeeder {

    fun getInitialDishes(): List<DishEntity> {
        return listOf(
            DishEntity(
                dishName = "MG Dosa_1",
                mainIngredients = "Urad, Kambu, Cholam, Ragi, Rice"
            ),
            DishEntity(
                dishName = "MG Dosa_2",
                mainIngredients = "Urad, Kambu, Cholam, Rice"
            ),
            DishEntity(
                dishName = "Adai Dosa",
                mainIngredients = "Urad, Toor, Broken Moong, Rice"
            ),
            DishEntity(
                dishName = "Moong Dosa",
                mainIngredients = "Urad, Moong, Rice"
            ),
            DishEntity(
                dishName = "Kambu Koozh",
                mainIngredients = "Kambu"
            ),
            DishEntity(
                dishName = "Samba Rava Kitchdi",
                mainIngredients = "Samba Rava, Vegetables"
            ),
            DishEntity(
                dishName = "MG Bread Sandwich",
                mainIngredients = "MG Bread, Lettuce, Cheese"
            ),
            DishEntity(
                dishName = "Matta Rice",
                mainIngredients = ""
            ),
            DishEntity(
                dishName = "White Rice",
                mainIngredients = ""
            ),
            DishEntity(
                dishName = "Foxtail Millet Kitchdi",
                mainIngredients = ""
            ),
            DishEntity(
                dishName = "Butter Milk",
                mainIngredients = ""
            )
        )
    }
}