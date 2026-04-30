package mx.edu.proyecto.happybox.auth

import android.content.Context
import android.util.Patterns

object AuthManager {

    fun validarLogin(email: String, password: String): String? {
        if (email.isBlank() || password.isBlank()) {
            return "Completa todos los campos"
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Correo inválido"
        }

        if (password.length < 6) {
            return "La contraseña debe tener mínimo 6 caracteres"
        }

        return null
    }

    fun validarRegistro(
        nombre: String,
        correo: String,
        telefono: String,
        password: String,
        confirmPassword: String
    ): String? {

        if (nombre.isBlank() || correo.isBlank() || telefono.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            return "Completa todos los campos"
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            return "Correo inválido"
        }

        if (telefono.length < 10) {
            return "Teléfono inválido"
        }

        if (password.length < 6) {
            return "La contraseña debe tener mínimo 6 caracteres"
        }

        if (password != confirmPassword) {
            return "Las contraseñas no coinciden"
        }

        return null
    }
}