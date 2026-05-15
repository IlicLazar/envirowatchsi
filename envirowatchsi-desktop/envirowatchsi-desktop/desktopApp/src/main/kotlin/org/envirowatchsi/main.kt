package org.envirowatchsi

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.envirowatchsi.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "envirowatchsi-desktop",
    ) {
        App()
    }
}