package com.example.shopsphere.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopsphere.data.CartProduct
import com.example.shopsphere.firebase.FirebaseCommon
import com.example.shopsphere.util.Resource
import com.example.shopsphere.util.evalProductId
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommon
) : ViewModel() {

    private val _addToCart = MutableStateFlow<Resource<CartProduct>>(Resource.Unspecified())
    val addToCart = _addToCart.asStateFlow()

    fun addUpdateProductInCart(cartProduct: CartProduct) {
        Log.d("TAG", "Inside addUpdateProductInCart");
        viewModelScope.launch { _addToCart.emit(Resource.Loading()) }
        firestore.collection("user").document(auth.uid!!).collection("cart").document(
            evalProductId(
                cartProduct.product.id, cartProduct.selectedSize, cartProduct.selectedColour
            )
        ).get().addOnSuccessListener {
            if (!it.exists()) //Add product
            {
                addNewProduct(cartProduct)
                Log.d("DetailsVM", "No match found, new product added")
            } else {    //Add product
                val documentId = it.id
                increaseQuantity(documentId, cartProduct)
                Log.d(" Log.d(\"TAG\",\"Button clicked\");", "outside increaseQuantity()")
            }
        }.addOnFailureListener {
            viewModelScope.launch { _addToCart.emit(Resource.Error(it.message.toString())) }
        }
    }

    private fun addNewProduct(cartProduct: CartProduct) {
        firebaseCommon.addProductToCart(cartProduct) { addedProduct, e ->
            viewModelScope.launch {
                if (e == null) _addToCart.emit(Resource.Success(addedProduct!!))
                else _addToCart.emit(Resource.Error(e.message.toString()))
            }
        }
    }

    private fun increaseQuantity(documentId: String, cartProduct: CartProduct) {
        Log.d("DetailsVM", "Inside increase qty")
        firebaseCommon.increaseQuantity(documentId) { _, e ->
            viewModelScope.launch {
                Log.d("DetailsVM", "inside viewmodelscope")
                if (e == null) {
                    _addToCart.emit(Resource.Success(cartProduct))
                    Log.d("DetailsVM", "emit Success")
                } else {
                    _addToCart.emit(Resource.Error(e.message.toString()))
                    Log.d("DetailsVM", "emit Error")
                }
            }

        }
    }
}