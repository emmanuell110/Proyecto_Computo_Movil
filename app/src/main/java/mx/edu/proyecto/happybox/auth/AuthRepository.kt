package mx.edu.proyecto.happybox.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlin.collections.getValue
import kotlin.text.get

object AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    fun register(
        nombre: String,
        correo: String,
        telefono: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {

        auth.createUserWithEmailAndPassword(correo, password)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    val userId = auth.currentUser?.uid ?: ""

                    val userMap = mapOf(
                        "nombre" to nombre,
                        "correo" to correo,
                        "telefono" to telefono,
                        "uid" to userId
                    )

                    db.child("users").child(userId)
                        .setValue(userMap)
                        .addOnCompleteListener {
                            onResult(true, "Registro exitoso")
                        }

                } else {
                    onResult(false, task.exception?.message ?: "Error")
                }
            }
    }

    fun login(
        correo: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(correo, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Login correcto")
                } else {
                    onResult(false, task.exception?.message ?: "Error")
                }
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUser
    fun obtenerDatosUsuario(
        onResult: (Map<String, String>?) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return onResult(null)

        db.child("users").child(userId)
            .get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.exists()) {

                    val nombre = snapshot.child("nombre").getValue(String::class.java) ?: ""
                    val telefono = snapshot.child("telefono").getValue(String::class.java) ?: ""

                    val usuario = mapOf(
                        "nombre" to nombre,
                        "telefono" to telefono
                    )

                    onResult(usuario)

                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }
}
