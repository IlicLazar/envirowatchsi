package org.envirowatchsi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.envirowatchsi.api.ApiClient
import org.envirowatchsi.api.LoginResult

@Composable
fun LoginScreen(
    onLoginSuccess: (LoginResult) -> Unit
) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Prijava",
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.width(360.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Geslo") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.width(360.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            enabled = !isLoading,
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    message = "Email in geslo sta obvezna."
                    return@Button
                }

                scope.launch {
                    isLoading = true
                    message = "Prijava je v teku..."

                    try {
                        val result = withContext(Dispatchers.IO) {
                            ApiClient.login(email.trim(), password)
                        }

                        if (result.role != "admin") {
                            ApiClient.logout()
                            message = "Prijava je uspela, vendar uporabnik nima administratorske vloge."
                            return@launch
                        }

                        message = "Prijava je uspela. Token je shranjen za to sejo."
                        onLoginSuccess(result)
                    } catch (e: Exception) {
                        message = "Prijava ni uspela: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            }
        ) {
            Text(if (isLoading) "Prijava..." else "Prijava")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(message)
    }
}
