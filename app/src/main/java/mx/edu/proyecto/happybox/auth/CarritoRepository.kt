package mx.edu.proyecto.happybox.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import mx.edu.proyecto.happybox.CartItem
import mx.edu.proyecto.happybox.Domain.Producto

object CarritoRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    fun guardarCarrito(carrito: List<CartItem>) {

        val userId = auth.currentUser?.uid ?: return

        db.child("users")
            .child(userId)
            .child("carrito")
            .setValue(carrito)
    }

    fun vaciarCarrito() {

        val userId = auth.currentUser?.uid ?: return

        db.child("users")
            .child(userId)
            .child("carrito")
            .removeValue()
    }
    fun obtenerCarrito(
        onResult: (List<CartItem>) -> Unit
    ) {

        val userId = auth.currentUser?.uid ?: return onResult(emptyList())

        db.child("users")
            .child(userId)
            .child("carrito")
            .get()
            .addOnSuccessListener { snapshot ->

                val lista = mutableListOf<CartItem>()

                snapshot.children.forEach { item ->

                    val carritoItem = item.getValue(CartItem::class.java)

                    if (carritoItem != null) {
                        lista.add(carritoItem)
                    }
                }

                onResult(lista)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}