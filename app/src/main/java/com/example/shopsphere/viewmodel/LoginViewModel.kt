package com.example.shopsphere.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopsphere.util.Constants.WEB_CLIENT_ID
import com.example.shopsphere.util.Resource
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) : ViewModel() {

    private val _login = MutableSharedFlow<Resource<FirebaseUser>>()
    val login = _login.asSharedFlow()

    private val _resetPassword = MutableSharedFlow<Resource<String>>()
    val resetPassword = _resetPassword.asSharedFlow()

    fun login(email: String, password: String) {

        viewModelScope.launch {
            _login.emit(Resource.Loading())
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                viewModelScope.launch {
                    it.user?.let {
                        _login.emit(Resource.Success(it))
                    }
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _login.emit(Resource.Error(it.message.toString()))
                }
            }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetPassword.emit(Resource.Loading())
        }

        firebaseAuth.sendPasswordResetEmail(email)
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

    fun googleLogin(context: Context) {

        Log.d("google","in googleLogin")
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetSignInWithGoogleOption.Builder(WEB_CLIENT_ID)
            .build()
        Log.d("google","in googleIdOption: $googleIdOption")


        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        Log.d("google","in request: $request")


        viewModelScope.launch {
            try {
                Log.d("google","in googleLogin TRY ")

                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )
                Log.d("google","in googleLogin after result - $result ")

                val credential = result.credential
                Log.d("google","in googleLogin after credential - $credential ")

                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(credential.data)
                Log.d("google","in googleLogin after googleIdTokenCredential ")

                val googleIdToken = googleIdTokenCredential.idToken
                Log.d("google","in googleLogin after googleIdToken ")

                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Log.d("google","in googleLogin after firebaseCredential ")

                Log.d("google","in googleLogin BEFORE SIGNINWITHCREDENTIALS")

                firebaseAuth.signInWithCredential(firebaseCredential)
                    .addOnSuccessListener {
                        viewModelScope.launch {
                            it.user?.let {
                                Log.d("google","in googleLogin ADDONSUCCESS")
                                _login.emit(Resource.Success(it))
                            }
                        }
                    }
                    .addOnFailureListener {
                        viewModelScope.launch {
                            Log.d("google","in googleLogin ADDONFAILFURE")
                            _login.emit(Resource.Error(it.message.toString()))
                        }
                    }
            } catch (e: Exception) {
                Log.d("google","in googleLogin CATCH")

                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                e.printStackTrace()
            }
        }
    }

}