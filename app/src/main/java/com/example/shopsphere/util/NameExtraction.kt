package com.example.shopsphere.util

fun lastNameExtraction(displayName:String):String {
    val list = displayName.split(" ")
    var lastName = ""
    for (i in 1..list.size-1){
        lastName+=list[i]+" "
    }

    return lastName
}

fun firstNameExtraction(displayName:String):String {
    val list = displayName.split(" ")

    return list[0]
}