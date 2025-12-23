package com.example.shopsphere.util

fun evalProductId(pid:String, psize:String?, pcolor:Int? ):String {

    return "${pid}_${psize?:""}_${pcolor?:""}"
}