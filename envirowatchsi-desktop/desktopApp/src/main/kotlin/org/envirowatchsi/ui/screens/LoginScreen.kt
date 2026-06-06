package org.envirowatchsi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.envirowatchsi.api.ApiClient
import org.envirowatchsi.api.LoginResult
import org.envirowatchsi.ui.components.EnviroPanel
import org.envirowatchsi.ui.components.StatusText
import org.envirowatchsi.ui.theme.EnviroColors

@Composable
fun LoginScreen(
    onLoginSuccess: (LoginResult) -> Unit
) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 72.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.widthIn(max = 520.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Prijava administratorja",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = EnviroColors.Ink
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Dostop do vnosa, posodabljanja in brisanja okoljskih meritev.",
                style = MaterialTheme.typography.bodyMedium,
                color = EnviroColors.Muted
            )

            Spacer(modifier = Modifier.height(22.dp))

            EnviroPanel(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Geslo") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
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

                StatusText(message)
            }
        }
    }
}
