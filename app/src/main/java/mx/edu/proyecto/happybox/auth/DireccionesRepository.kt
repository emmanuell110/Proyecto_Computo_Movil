package mx.edu.proyecto.happybox.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object DireccionesRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    // 🔥 GUARDAR DIRECCIONES
    fun guardarDirecciones(
        direcciones: List<String>
    ) {

        val userId = auth.currentUser?.uid ?: return

        db.child("users")
            .child(userId)
            .child("direcciones")
            .setValue(direcciones)
    }

    // 🔥 OBTENER DIRECCIONES
    fun obtenerDirecciones(
        onResult: (List<String>) -> Unit
    ) {

        val userId = auth.currentUser?.uid ?: return onResult(emptyList())

        db.child("users")
            .child(userId)
            .child("direcciones")
            .get()
            .addOnSuccessListener { snapshot ->

                val lista = mutableListOf<String>()

                snapshot.children.forEach {

                    val direccion =
                        it.getValue(String::class.java)

                    if (direccion != null) {
                        lista.add(direccion)
                    }
                }

                onResult(lista)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}