package com.example.lilifly

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.credentials.Credential
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import kotlinx.coroutines.launch
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.lilifly.databinding.ActivityMainBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.core.view.View
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import org.json.JSONObject
import java.util.jar.Manifest


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private val spotifyClientId = "87f8307bb500473c95c72766f33dadd6"
    private val redirectUri = "com.example.lilifly://callback"
    private val spotifyRequestCode = 1337

    private var spotifyAppRemote: SpotifyAppRemote? = null
    private lateinit var requestQueue: RequestQueue
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var credentialManager: CredentialManager

    val CHANNEL_ID = "my_channel_id"
    val CHANNEL_NAME = "My Notifications"
    val CHANNEL_DESCRIPTION = "Notifications from my app"
    val NOTIFICATION_ID = 1
//    private lateinit var mDataBase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        requestQueue = Volley.newRequestQueue(this)
        credentialManager = CredentialManager.create(this)
        requestNotificationPermission()
        val USER_KEY = "User"
//        mDataBase = FirebaseDatabase.getInstance().getReference("User")

        // Check if we have a valid Spotify token
        val token = sharedPreferences.getString("token", "")
        if (token.isNullOrEmpty() || isTokenExpired()) {
            // No valid token, start Spotify authentication
            startSpotifyAuth()
        } else {
            startGoogleSignIn()
            startSpotifyAuth()
            // We have a valid token, connect to Spotify and show content
            connectToSpotifyAppRemote()
//            showPopularFragment()
        }


//        setupNavigation()
        createNotificationChannel()



    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(POST_NOTIFICATIONS),
                    123 // произвольный request code
                )
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Lilifly Notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH // Измените на HIGH для лучшей видимости

            val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
                description = CHANNEL_DESCRIPTION
                // Дополнительные настройки
                setShowBadge(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 300, 400)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    @SuppressLint("MissingPermission")
    fun runNotify(context: Context) {
        // Проверяем разрешение
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Please allow notifications permission", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ava99) // Убедитесь что этот ресурс существует
            .setContentTitle("Lilifly Music")
            .setContentText("Your favorite music is waiting for you!")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Высокий приоритет
            .setAutoCancel(true) // Уведомление исчезает при клике
            .setContentIntent(createPendingIntent()) // Добавляем действие при клике

        with(NotificationManagerCompat.from(this)) {
            try {
                notify(NOTIFICATION_ID, builder.build())
                Log.d("Notification", "Notification shown successfully")
            } catch (e: Exception) {
                Log.e("Notification", "Failed to show notification: ${e.message}")
            }
        }
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
//    @SuppressLint("MissingPermission")
//    private fun setupNavigation() {
//        binding.userBtn?.setOnClickListener @androidx.annotation.RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS) {
//                runNotify(this)
////                val fragment = UserFragment()
////                supportFragmentManager.beginTransaction()
////                    .replace(R.id.fgrm, fragment)
////                    .commit()
////            runNotify(this)
//        }

//        binding.popularBtn?.setOnClickListener {
//            showPopularFragment()
//        }
//    }






//    private fun showPopularFragment() {
//        val fragment = PopularFragment()
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fgrm, fragment)
//            .commit()
//    }

    private fun isTokenValid(): Boolean {
        val token = sharedPreferences.getString("token", "")
        return !token.isNullOrEmpty() && !isTokenExpired()
    }

    private fun isTokenExpired(): Boolean {
        val expiryTime = sharedPreferences.getLong("token_expiry", 0)
        return System.currentTimeMillis() >= expiryTime
    }

    private fun refreshAccessToken() {
        val refreshToken = sharedPreferences.getString("refresh_token", null)
        if (refreshToken.isNullOrEmpty()) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            startSpotifyAuth()
            return
        }

        val url = "https://accounts.spotify.com/api/token"
        val params = HashMap<String, String>()
        params["grant_type"] = "refresh_token"
        params["refresh_token"] = refreshToken
        params["client_id"] = spotifyClientId

        val request = object : JsonObjectRequest(
            Request.Method.POST, url, JSONObject(params as Map<*, *>),
            { response ->
                try {
                    val newAccessToken = response.getString("access_token")
                    val expiresIn = response.getInt("expires_in")

                    with(sharedPreferences.edit()) {
                        putString("token", newAccessToken)
                        putLong("token_expiry", System.currentTimeMillis() + (expiresIn * 1000) - 60000)
                        apply()
                    }

                    Log.i("TokenRefresh", "Token refreshed successfully")
                    connectToSpotifyAppRemote()

                } catch (e: Exception) {
                    Log.e("TokenRefresh", "Error parsing refresh response: ${e.message}")
                    Toast.makeText(this, "Token refresh failed", Toast.LENGTH_SHORT).show()
                    startSpotifyAuth()
                }
            },
            { error ->
                Log.e("TokenRefresh", "Refresh Error: ${error.message}")
                Toast.makeText(this, "Token refresh failed", Toast.LENGTH_SHORT).show()
                startSpotifyAuth()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/x-www-form-urlencoded"
                return headers
            }
        }

        requestQueue.add(request)
    }

    private fun startSpotifyAuth() {
        val request = AuthorizationRequest.Builder(
            spotifyClientId,
            AuthorizationResponse.Type.TOKEN,
            redirectUri
        )
            .setScopes(arrayOf("streaming", "user-read-private", "user-read-email"))
            .setShowDialog(true)
            .build()

        try {
            AuthorizationClient.openLoginActivity(this, spotifyRequestCode, request)
        } catch (e: Exception) {
            Log.e("MainActivity", "Spotify auth failed: ${e.message}")
            Toast.makeText(this, "Spotify authentication failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == spotifyRequestCode) {
            val response = AuthorizationClient.getResponse(resultCode, data)

            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    val accessToken = response.accessToken
                    val expiresIn = response.expiresIn


                    with(sharedPreferences.edit()) {
                        putString("token", accessToken)
                        putString("refresh_token", response.accessToken)
                        putLong("token_expiry", System.currentTimeMillis() + (expiresIn * 1000) - 60000)
                        apply()
                    }

                    Log.d("MainActivity", "Spotify Success! Token received")
                    connectToSpotifyAppRemote()
//                    showPopularFragment()
                }

                AuthorizationResponse.Type.ERROR -> {
                    Log.e("MainActivity", "Spotify Error: ${response.error}")
                    Toast.makeText(this, "Spotify authentication failed: ${response.error}", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    Log.d("MainActivity", "Spotify Auth cancelled")
                    Toast.makeText(this, "Authentication cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
                Toast.makeText(this@MainActivity, "Spotify connection failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        // Reconnect to Spotify when activity resumes
        if (isTokenValid() && spotifyAppRemote == null) {
            connectToSpotifyAppRemote()
        }
    }

    override fun onStop() {
        super.onStop()
        SpotifyAppRemote.disconnect(spotifyAppRemote)
    }

    // Google Sign-In methods (optional - only if you need both auth methods)
    private fun startGoogleSignIn() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false)
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
            } catch (e: GetCredentialException) {
                handleGoogleSignInError(e)
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Unexpected error", e)
                Toast.makeText(this@MainActivity, "Unexpected error during sign-in", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleGoogleSignInError(e: GetCredentialException) {
        when (e) {
            is NoCredentialException -> {
                Log.w("GoogleSignIn", "No credentials found", e)
                Toast.makeText(this, "Please add a Google account to your device", Toast.LENGTH_LONG).show()
            }
            is GetCredentialCancellationException -> {
                Log.d("GoogleSignIn", "Sign-in cancelled by user")
            }
            else -> {
                Log.e("GoogleSignIn", "Sign-in failed", e)
                Toast.makeText(this, "Sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleGoogleSignInCredential(credential: Credential) {
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                firebaseAuthWithGoogle(idToken)
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Failed to parse Google ID token", e)
            }
        } else {
            Log.e("GoogleSignIn", "Unexpected credential type")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("Database", "Firebase signInWithCredential:success")

//                    val database = Firebase.database
//                    val myRef = database.getReference("message")
//
//                    myRef.setValue("Hello, World!")







                    val user = auth.currentUser
                    Toast.makeText(this, "Google sign-in successful", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w("Database", "Firebase signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}