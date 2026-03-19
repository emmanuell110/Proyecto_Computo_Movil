package mx.edu.proyecto.happybox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.proyecto.happybox.ui.theme.HappyboxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HappyboxTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var screen by remember { mutableStateOf("inicio") }

    when (screen) {
        "inicio" -> InicioScreen { screen = "menu" }
        "menu" -> MenuScreen { screen = "detalles" }
        "detalles" -> DetallesScreen()
    }
}

@Composable
fun InicioScreen(onEntrar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF5F7)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(R.drawable.logo_happy_box),
            contentDescription = "logo",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Envolturas, detalles y globos",
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onEntrar) {
            Text("Entrar")
        }
    }
}

@Composable
fun MenuScreen(onDetalles: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Categorías", fontSize = 22.sp)

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onDetalles) {
            Text("Detalles")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = { }) {
            Text("Globos")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = { }) {
            Text("Peluches")
        }
    }
}

data class Producto(
    val nombre: String,
    val precio: String,
    val imagen: Int
)

@Composable
fun DetallesScreen() {

    val productos = listOf(
        Producto("Caja sorpresa", "$250", R.drawable.regalo1),
        Producto("Detalle cumpleaños", "$300", R.drawable.regalo2),
        Producto("Caja romántica", "$400", R.drawable.regalo3)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Detalles", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn {
            items(productos) { producto ->
                ProductoItem(producto)
            }
        }
    }
}
@Composable
fun ProductoItem(producto: Producto) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(10.dp)) {

            Image(
                painter = painterResource(producto.imagen),
                contentDescription = producto.nombre,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(producto.nombre, fontSize = 18.sp)
                Text(producto.precio, color = Color.Gray)
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    HappyboxTheme {
        AppNavigation()
    }
}