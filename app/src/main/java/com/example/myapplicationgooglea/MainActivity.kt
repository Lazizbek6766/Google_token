package com.example.myapplicationgooglea

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val coroutineScope = rememberCoroutineScope()
            val context = LocalContext.current
            val credentialManager = remember { CredentialManager.create(context) }

            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId("Web Aplication clent id")
                .setFilterByAuthorizedAccounts(false)
                .build()

            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Ko'rish uchun qo'shimcha logika
            val signInButtonEnabled = true // Bu qiymatni kerakli shartlarga qarab o'zgartiring
            val buttonText = if (signInButtonEnabled) "Sign In" else "Sign In Disabled"

            // Compose UI tuzish
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Button(
                    onClick = {
                            coroutineScope.launch {

                                    try {
                                        val result = credentialManager.getCredential(
                                            context = context, request = request
                                        )
                                        handleSignIn(
                                            result,
                                            onTokenIdReceived = { tokenId ->
                                                Log.d("TAG", "LoginScreen: $tokenId")
                                            })
                                    } catch (e: GetCredentialException) {
                                        Log.d("TAG", "onCreate: ${e.message}")
                                    } catch (e: Exception) {
                                        Log.e("TAG", "Exception: Unexpected error", e)
                                    }
                                }

//                            }
                    },
                ) {
                    Text(text = buttonText)
                }
            }
        }
    }
}

private fun handleSignIn(
    credentialResponse: GetCredentialResponse,
    onTokenIdReceived: (String) -> Unit,
) {
    when (val credential = credentialResponse.credential) {
        is CustomCredential -> {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential
                        .createFrom(credential.data)
                    onTokenIdReceived(googleIdTokenCredential.idToken)
                } catch (e: GoogleIdTokenParsingException) {
                    Log.d("TAG", "handleSignIn: Invalid Google tokenId response: ${e.message}")
                }
            } else {
                Log.d("TAG", "handleSignIn: Invalid Google tokenId response: ")
            }
        }

        else -> {
            Log.d("TAG", "handleSignIn: Invalid Google tokenId response:")
        }
    }
}