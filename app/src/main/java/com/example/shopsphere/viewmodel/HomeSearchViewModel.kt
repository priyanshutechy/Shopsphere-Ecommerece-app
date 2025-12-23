package com.example.shopsphere.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopsphere.data.Product
import com.example.shopsphere.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeSearchViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _search = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val search = _search.asStateFlow()


    fun searchProducts(query: String) {
            viewModelScope.launch { _search.emit(Resource.Loading()) }
        if (query.isBlank()) {
            viewModelScope.launch { _search.emit(Resource.Success(emptyList())) }
            return
        }
        val searchQuery = firestore.collection("Products")
            .orderBy("name")
            .startAt(query)
            .endAt(query + "\uf8ff")

        searchQuery.addSnapshotListener { snapshots, error ->
            if (error != null) {
                viewModelScope.launch {
                    _search.emit(Resource.Error(error.message.toString()))
                }
                return@addSnapshotListener
            }

            val products = snapshots?.toObjects(Product::class.java)
            viewModelScope.launch { _search.emit(Resource.Success(products!!)) }
        }
    }
}