package com.example.shopsphere.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopsphere.data.User
import com.example.shopsphere.util.Constants.USER_COLLECTION
import com.example.shopsphere.util.Constants.WEB_CLIENT_ID
import com.example.shopsphere.util.RegisterFieldState
import com.example.shopsphere.util.RegisterValidation
import com.example.shopsphere.util.Resource
import com.example.shopsphere.util.firstNameExtraction
import com.example.shopsphere.util.lastNameExtraction
import com.example.shopsphere.util.validateEmail
import com.example.shopsphere.util.validatePassword
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _register = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val register: Flow<Resource<User>> = _register

    private val _googleRegister = MutableStateFlow<Resource<FirebaseUser>>(Resource.Unspecified())
    val googleRegister: Flow<Resource<FirebaseUser>> = _googleRegister

    private val _validation = Channel<RegisterFieldState>()
    val validation = _validation.receiveAsFlow()

    fun createAccountWithEmailAndPassword(user: User, password: String) {
        if (checkValidation(user, password)) {
            runBlocking {
                _register.emit(Resource.Loading())
            }
            firebaseAuth.createUserWithEmailAndPassword(user.email, password)
                .addOnSuccessListener {
                    it.user?.let {
                        saveUserInfo(it.uid, user)
                        Log.d("registerButtom", "User created")
                    }
                }
                .addOnFailureListener {
                    _register.value = Resource.Error(it.message.toString())
                }
        } else {
            val registerFieldState = RegisterFieldState(
                validateEmail(user.email),
                validatePassword(password)
            )
            runBlocking {
                _validation.send(registerFieldState)
            }
        }
    }

    private fun saveUserInfo(userUid: String, user: User) {
        db.collection(USER_COLLECTION)
            .document(userUid)
            .set(user)
            .addOnSuccessListener {
                _register.value = Resource.Success(user)
            }
            .addOnFailureListener {
                _register.value = Resource.Error(it.message.toString())

            }
    }

    private fun checkValidation(user: User, password: String): Boolean {
        val emailValidation = validateEmail(user.email)
        val passwordValidation = validatePassword(password)
        val shouldRegister =
            emailValidation is RegisterValidation.Success && passwordValidation is RegisterValidation.Success

        return shouldRegister
    }

    fun googleLogin(context: Context) {

        Log.d("google", "in googleLogin")
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetSignInWithGoogleOption.Builder(WEB_CLIENT_ID)
            .build()
        Log.d("google", "in googleIdOption: $googleIdOption")


        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        Log.d("google", "in request: $request")


        viewModelScope.launch {
            try {
                Log.d("google", "in googleLogin TRY ")

                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )
                Log.d("google", "in googleLogin after result - $result ")

                val credential = result.credential
                Log.d("google", "in googleLogin after credential - $credential ")

                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(credential.data)
                Log.d("google", "in googleLogin after googleIdTokenCredential ")

                val googleIdToken = googleIdTokenCredential.idToken
                Log.d("google", "in googleLogin after googleIdToken ")

                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Log.d("google", "in googleLogin after firebaseCredential ")


                Log.d("google", "in googleLogin BEFORE SIGNINWITHCREDENTIALS")

                firebaseAuth.signInWithCredential(firebaseCredential)
                    .addOnSuccessListener {
                        viewModelScope.launch {
                            it.user?.let {
                                Log.d("google", "in googleLogin ADDONSUCCESS")
                                _googleRegister.emit(Resource.Success(it))
                            }
                            val currentUser = firebaseAuth.currentUser
                            currentUser?.let {
                                val user = User(
                                    firstNameExtraction(currentUser.displayName.toString()),
                                    lastNameExtraction(currentUser.displayName.toString()),
                                    currentUser.email.toString(),
                                    currentUser.photoUrl.toString()
                                )
                                saveUserInfo(currentUser.uid, user)
                            }
                        }
                    }
                    .addOnFailureListener {
                        viewModelScope.launch {
                            Log.d("google", "in googleLogin ADDONFAILFURE")
                            _googleRegister.emit(Resource.Error(it.message.toString()))
                        }
                    }
            } catch (e: Exception) {
                Log.d("google", "in googleLogin CATCH")

                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                e.printStackTrace()
            }
        }
    }


}