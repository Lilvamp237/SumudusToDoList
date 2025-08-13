package lk.kdu.ac.mc.sumudustodolist.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

//Sealed class, shows different states of user authentication
sealed class AuthState {
    object Loading : AuthState() //Indicates an authentication operation is in progress
    data class Authenticated(val user: FirebaseUser) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

//ViewModel responsible for handling user authentication
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val auth: FirebaseAuth = Firebase.auth
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init { //AuthStateListener to react to changes in Firebase's authentication state
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUser.value = user
            _authState.value = if (user != null) AuthState.Authenticated(user) else AuthState.Unauthenticated
        }
        // Initial check
        val user = auth.currentUser
        _authState.value = if (user != null) AuthState.Authenticated(user) else AuthState.Unauthenticated
    }

    //Sign up a new user with the provided email and password
    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                result.user?.sendEmailVerification()?.await() // Send verification email
            } catch (e: FirebaseAuthUserCollisionException) {
                _authState.value = AuthState.Error("Email already in use.")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Sign up failed", e)
                _authState.value = AuthState.Error(e.message ?: "Sign up failed.")
            }
        }
    }

    //Log in an existing user with given email and password
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                // Updated by the listener
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login failed", e)
                _authState.value = AuthState.Error(e.message ?: "Login failed.")
            }
        }
    }

    //Signs out the currently authenticated user
    fun signOut() {
        auth.signOut()
        // Updated by the listener
    }

    fun resetAuthStateToIdle() {
        if (_authState.value is AuthState.Error || _authState.value is AuthState.Loading) {
            val user = auth.currentUser
            _authState.value = if (user != null) AuthState.Authenticated(user) else AuthState.Unauthenticated
        }
    }
}