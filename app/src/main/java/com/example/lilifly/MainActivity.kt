package com.example.lilifly
import androidx.credentials.Credential
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.credentials.GetCredentialException
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.example.lilifly.databinding.ActivityMainBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private val spotifyClientId = "87f8307bb500473c95c72766f33dadd6"
    private val redirectUri = "com.example.lilifly://callback"
    private val spotifyRequestCode = 1337 // Код для Spotify
    private val googleSignInRequestCode = 1338 // УНИКАЛЬНЫЙ код для Google Sign-In

    private var spotifyAppRemote: SpotifyAppRemote? = null
    private var spotifyAccessToken = "" // Переименовал для ясности
    private lateinit var requestQueue: RequestQueue
    private lateinit var sharedPreferences: SharedPreferences

    // Менеджер для нового Credentials API
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = this.getSharedPreferences("UserPreferences", MODE_PRIVATE)
        requestQueue = Volley.newRequestQueue(this)

        // Инициализация Credential Manager
        credentialManager = CredentialManager.create(this)

        auth = Firebase.auth

        // 1. Запускаем аутентификацию в Spotify при старте
        startSpotifyAuth()

        // 2. Настраиваем кнопку для входа через Google

            startGoogleSignIn()


        binding.userBtn?.setOnClickListener {
            val fragment = UserFragment().apply {}
            supportFragmentManager.beginTransaction()
                .replace(R.id.fgrm, fragment)
                .commit()
        }

        val fragment = PopularFragment().apply {}
        supportFragmentManager.beginTransaction()
            .replace(R.id.fgrm, fragment)
            .commit()
    }

    // --- НОВЫЙ МЕТОД для запуска Google Sign-In через Credentials API ---
    private fun startGoogleSignIn() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false) // Изменили на false
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val credentialResponse = credentialManager.getCredential(
                    context = this@MainActivity,
                    request = request
                )
                handleGoogleSignInCredential(credentialResponse.credential)

            } catch (e:Error) {
                // Специфичная обработка ошибок аутентификации
                when (e) {
                    is androidx.credentials.exceptions.NoCredentialException -> {
                        Log.w("GoogleSignIn", "No credentials found", e)
                        // Предложить пользователю добавить аккаунт Google или создать новый
                        Toast.makeText(this@MainActivity, "Please add a Google account to your device", Toast.LENGTH_LONG).show()
                    }
                    is androidx.credentials.exceptions.GetCredentialCancellationException -> {
                        // Пользователь отменил вход - это нормально, не показываем ошибку
                        Log.d("GoogleSignIn", "Sign-in cancelled by user")
                    }
                    else -> {
                        Log.e("GoogleSignIn", "Sign-in failed", e)
                        Toast.makeText(this@MainActivity, "Sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Unexpected error", e)
                Toast.makeText(this@MainActivity, "Unexpected error during sign-in", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- НОВЫЙ МЕТОД для обработки учетных данных от Google ---
    private fun handleGoogleSignInCredential(credential: Credential) { // androidx.credentials.Credential
        if (credential is CustomCredential) {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    firebaseAuthWithGoogle(idToken)
                } catch (e: Exception) {
                    Log.e("GoogleSignIn", "Failed to parse Google ID token", e)
                }
            }
        } else {
            Log.e("GoogleSignIn", "Unexpected credential type")
        }
    }

    // --- Метод для аутентификации в Firebase (БЕЗ ИЗМЕНЕНИЙ, но теперь он вызывается правильно) ---
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("Database", "Firebase signInWithCredential:success")
                    val user = auth.currentUser
                    // Здесь можно обновить UI, показать имя пользователя и т.д.
                } else {
                    Log.w("Database", "Firebase signInWithCredential:failure", task.exception)
                }
            }
    }

    // --- Существующий метод для аутентификации в Spotify (ПЕРЕИМЕНОВАН для ясности) ---
    private fun startSpotifyAuth() {
        val request = AuthorizationRequest.Builder(
            spotifyClientId,
            AuthorizationResponse.Type.TOKEN,
            redirectUri
        )
            .setScopes(arrayOf("streaming", "user-read-private"))
            .setShowDialog(true)
            .build()

        try {
            AuthorizationClient.openLoginActivity(this, spotifyRequestCode, request)
        } catch (e: Exception) {
            Log.e("MainActivity", "Spotify auth failed: ${e.message}")
        }
    }

    // --- onActivityResult - обрабатываем ОБА процесса ---
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 1. Обработка ответа от Spotify
        if (requestCode == this.spotifyRequestCode) {
            val response = AuthorizationClient.getResponse(resultCode, data)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    spotifyAccessToken = response.accessToken // Сохраняем токен Spotify
                    // Сохраняем токен Spotify в SharedPreferences
                    val editor = sharedPreferences.edit()
                    editor.putString("spotify_token", spotifyAccessToken) // Ключ изменен для ясности
                    editor.apply()
                    Log.d("MainActivity", "Spotify Success! Token: ${spotifyAccessToken.take(10)}...")
                    connectToSpotifyAppRemote()
                    // НЕ ВЫЗЫВАЕМ firebaseAuthWithGoogle ЗДЕСЬ!
                }

                AuthorizationResponse.Type.ERROR -> {
                    Log.e("MainActivity", "Spotify Error: ${response.error}")
                }

                else -> {
                    Log.d("MainActivity", "Spotify Auth cancelled")
                }
            }
        }
        // 2. Обработка ответа от Google Sign-In (если бы вы использовали старый API)
        // Для нового Credentials API это не нужно, т.к. он использует callbackи!
        // else if (requestCode == googleSignInRequestCode) { ... }
    }

    private fun connectToSpotifyAppRemote() {
        val connectionParams = ConnectionParams.Builder(spotifyClientId)
            .setRedirectUri(redirectUri)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(this, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                spotifyAppRemote = appRemote
                Log.d("MainActivity", "Connected to Spotify App Remote!")
            }

            override fun onFailure(throwable: Throwable) {
                Log.e("MainActivity", "Spotify Connection failed: ${throwable.message}")
            }
        })
    }
}