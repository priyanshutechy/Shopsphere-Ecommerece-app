package com.example.shopsphere.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopsphere.ShopSphereApplication
import com.example.shopsphere.data.User
import com.example.shopsphere.util.RegisterValidation
import com.example.shopsphere.util.Resource
import com.example.shopsphere.util.validateEmail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class UserAccountViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: StorageReference,
    app: Application // we need this for contentResolver
) : AndroidViewModel(app) {

    private val _user = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val user = _user.asStateFlow()

    private val _updateInfo = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val updateInfo = _updateInfo.asStateFlow()

    private val _resetPassword = MutableSharedFlow<Resource<String>>()
    val resetPassword = _resetPassword.asSharedFlow()

    init {
        getUser()
    }

    fun getUser() {
        viewModelScope.launch { _user.emit(Resource.Loading()) }

        firestore.collection("user").document(auth.uid!!).get()
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)
                user?.let {
                    viewModelScope.launch { _user.emit(Resource.Success(it)) }
                }
            }
            .addOnFailureListener {
                viewModelScope.launch { _user.emit(Resource.Error(it.message.toString())) }
            }
    }

    fun updateUser(user: User, imageUri: Uri?) {
        val areInputsValid = validateEmail(user.email) is RegisterValidation.Success
                && user.firstName.trim().isNotEmpty()
                && user.lastName.trim().isNotEmpty()

        if (!areInputsValid) {
            viewModelScope.launch { _updateInfo.emit(Resource.Error("Check your inputs!")) }
            return
        }

        viewModelScope.launch { _updateInfo.emit(Resource.Loading()) }

        if (imageUri == null) {
            saveUserInformation(user, true)
        } else {
            saveUserInformationWithNewImage(user, imageUri)
        }
    }

    private fun saveUserInformationWithNewImage(user: User, imageUri: Uri) {
        viewModelScope.launch {
            try {
                val contentResolver = getApplication<ShopSphereApplication>().contentResolver
//                val imageBitmap = MediaStore.Images.Media.getBitmap(
//                    contentResolver,
//                    imageUri
//                )

                val imageOrientation = getImageOrientation(contentResolver, imageUri)
                Log.d("orientation", imageOrientation.toString())

                // Fix rotation if necessary
                val rotatedBitmap = rotateImage(contentResolver, imageUri, imageOrientation)

                val byteArrayOutputStream = ByteArrayOutputStream()
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 96, byteArrayOutputStream)
                val imageByteArray = byteArrayOutputStream.toByteArray()
                val imageDirectory = storage.child("profileImages/${auth.uid}/${UUID.randomUUID()}")


                val result = imageDirectory.putBytes(imageByteArray).await()
                val imageUrl = result.storage.downloadUrl.await().toString()
                saveUserInformation(user.copy(imagePath = imageUrl), false)
            } catch (e: Exception) {
                viewModelScope.launch { _user.emit(Resource.Error(e.message.toString())) }
            }
        }
    }

    private fun getImageOrientation(contentResolver: ContentResolver, imageUri: Uri): Int {
        var orientation = ExifInterface.ORIENTATION_UNDEFINED

        try {
            // Create an ExifInterface instance to read the metadata of the image
            val exifInterface = contentResolver.openInputStream(imageUri)?.let { ExifInterface(it) }

            // Get the orientation tag from the metadata
            orientation = exifInterface!!.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return orientation

    }

    // Function to rotate the image based on its orientation
    fun rotateImage(contentResolver: ContentResolver, imageUri: Uri, orientation: Int): Bitmap {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.preScale(1f, -1f)
            }

            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.preScale(1f, -1f)
            }

            else -> return bitmap
        }

        Log.d("orientation", matrix.toString())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }


    private fun saveUserInformation(user: User, shouldRetrieveOldImage: Boolean) {
        firestore.runTransaction { transaction ->
            val documentRef = firestore.collection("user").document(auth.uid!!)

            if (shouldRetrieveOldImage) {
                val currentUser = transaction.get(documentRef).toObject(User::class.java)
                val newUser = user.copy(imagePath = currentUser?.imagePath ?: "")
                transaction.set(documentRef, newUser)
            } else {
                transaction.set(documentRef, user)
            }
        }
            .addOnSuccessListener {
                viewModelScope.launch { _updateInfo.emit(Resource.Success(user)) }
            }
            .addOnFailureListener {
                viewModelScope.launch { _updateInfo.emit(Resource.Error(it.message.toString())) }
            }
    }

    fun resetPassword(email:String){
        viewModelScope.launch {
            _resetPassword.emit(Resource.Loading())
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                viewModelScope.launch {
                    _resetPassword.emit(Resource.Success(email))
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _resetPassword.emit(Resource.Error(it.message.toString()))
                }
            }
    }
}