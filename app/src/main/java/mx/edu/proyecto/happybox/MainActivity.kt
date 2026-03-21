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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.proyecto.happybox.ui.theme.HappyboxTheme
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.foundation.shape.CircleShape
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.draw.scale


// 🔥 carrito global
val carrito = mutableStateListOf<Producto>()

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

// 🔁 navegación
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation() {

    var screen by remember { mutableStateOf("inicio") }

    Column(modifier = Modifier.fillMaxSize()) {

        // 🔥 CONTENIDO CON ANIMACIÓN
        AnimatedContent(
            targetState = screen,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
            }
        ) { target ->

            when (target) {

                "inicio" -> InicioScreen { screen = "login" }

                "login" -> {
                    BackHandler { screen = "inicio" }

                    LoginScreen(
                        onLogin = { screen = "menu" },
                        onRegistro = { screen = "registro" }
                    )
                }

                "registro" -> {
                    BackHandler { screen = "login" }

                    RegisterScreen {
                        screen = "login"
                    }
                }

                "menu" -> {
                    BackHandler { screen = "login" }

                    MenuScreen(
                        onDetalles = { screen = "detalles" },
                        onGlobos = { screen = "globos" },
                        onPeluches = { screen = "peluches" },
                        onCarrito = { screen = "carrito" },
                        onPerfil = { screen = "perfil" }
                    )
                }

                "detalles" -> {
                    BackHandler { screen = "menu" }
                    DetallesScreen()
                }

                "globos" -> {
                    BackHandler { screen = "menu" }
                    GlobosScreen()
                }

                "peluches" -> {
                    BackHandler { screen = "menu" }
                    PeluchesScreen()
                }

                "carrito" -> {
                    BackHandler { screen = "menu" }
                    CarritoScreen { screen = "menu" }
                }

                "perfil" -> {
                    BackHandler { screen = "menu" }
                    PerfilScreen()
                }
            }
        }

        // 🔻 🔥 BOTTOM BAR GLOBAL
        if (screen != "inicio" && screen != "login" && screen != "registro") {
            BottomBar(
                onMenu = { screen = "menu" },
                onPerfil = { screen = "perfil" }
            )
        }
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

// 🔐 LOGIN
@Composable
fun LoginScreen(onLogin: () -> Unit, onRegistro: () -> Unit) {

    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text("Iniciar Sesión", fontSize = 22.sp)

        OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Correo o Teléfono") })
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Contraseña") })

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = onLogin) { Text("Entrar") }

        TextButton(onClick = onRegistro) {
            Text("Crear cuenta")
        }
    }
}

// 📝 REGISTRO
@Composable
fun RegisterScreen(onBack: () -> Unit) {

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text("Crear cuenta", fontSize = 22.sp)

        listOf("Nombre","Apellidos","Edad","Dirección","Teléfono","Correo","Contraseña").forEach {
            OutlinedTextField(value = "", onValueChange = {}, label = { Text(it) })
        }

        Button(onClick = onBack) {
            Text("Registrarse")
        }
    }
}

// 🏠 MENU
@Composable
fun MenuScreen(
    onDetalles: () -> Unit,
    onGlobos: () -> Unit,
    onPeluches: () -> Unit,
    onCarrito: () -> Unit,
    onPerfil: () -> Unit
) {

    Box(modifier = Modifier.fillMaxSize()) {

        // 🛒 icono carrito
        IconButton(
            onClick = onCarrito,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Categorías", fontSize = 22.sp)

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = onDetalles, modifier = Modifier.fillMaxWidth()) { Text("Detalles") }
            Button(onClick = onGlobos, modifier = Modifier.fillMaxWidth()) { Text("Globos") }
            Button(onClick = onPeluches, modifier = Modifier.fillMaxWidth()) { Text("Peluches") }

            Spacer(modifier = Modifier.weight(1f))

        }
    }
}
@Composable
fun BottomBar(
    onMenu: () -> Unit,
    onPerfil: () -> Unit
) {

    val size = carrito.size

    // 🔥 animación escala
    val scale = remember { Animatable(1f) }

    LaunchedEffect(size) {
        if (size > 0) {
            scale.snapTo(1.3f)
            scale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        // 🛒 carrito con badge animado
        Box {

            IconButton(onClick = onMenu) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "Menu")
            }

            if (size > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp)
                        .scale(scale.value) // 🔥 animación
                        .background(Color.Red, shape = CircleShape)
                        .padding(4.dp)
                ) {
                    Text(
                        text = "$size",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // 👤 perfil
        IconButton(onClick = onPerfil) {
            Icon(Icons.Default.Person, contentDescription = "Perfil")
        }
    }
}
@Composable
fun PerfilScreen() {

    var ubicaciones by remember {
        mutableStateOf(listOf("Casa - Calle 123"))
    }

    var mostrarFormulario by remember { mutableStateOf(false) }

    // 🔥 campos del formulario
    var cp by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("") }
    var calle by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var entreCalles by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text("Perfil", fontSize = 22.sp)

        Text("Nombre: Juan Pérez")
        Text("Teléfono: 6441234567")

        Spacer(modifier = Modifier.height(10.dp))

        Text("Ubicaciones:")

        ubicaciones.forEach {
            Text("• $it")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = {
            mostrarFormulario = true
        }) {
            Text("+ Agregar ubicación")
        }
    }

    // 🔥 FORMULARIO MODAL
    if (mostrarFormulario) {
        AlertDialog(
            onDismissRequest = { mostrarFormulario = false },
            confirmButton = {
                Button(onClick = {
                    val nueva = "$calle #$numero, $ciudad, $estado, CP $cp ($entreCalles)"
                    ubicaciones = ubicaciones + nueva

                    // limpiar campos
                    cp = ""
                    estado = ""
                    ciudad = ""
                    calle = ""
                    numero = ""
                    entreCalles = ""

                    mostrarFormulario = false
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Button(onClick = { mostrarFormulario = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Nueva ubicación") },
            text = {

                Column {

                    OutlinedTextField(cp, { cp = it }, label = { Text("CP") })
                    OutlinedTextField(estado, { estado = it }, label = { Text("Estado") })
                    OutlinedTextField(ciudad, { ciudad = it }, label = { Text("Ciudad") })
                    OutlinedTextField(calle, { calle = it }, label = { Text("Calle principal") })
                    OutlinedTextField(numero, { numero = it }, label = { Text("Número exterior") })
                    OutlinedTextField(entreCalles, { entreCalles = it }, label = { Text("Entre qué calles") })

                }
            }
        )
    }
}

// 📦 DATA CLASS
data class Producto(
    val nombre: String,
    val precio: String,
    val imagen: Int
)

// 🛍️ PRODUCTOS
@Composable
fun ProductoItem(producto: Producto) {

    var agregado by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {

        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = painterResource(producto.imagen),
                contentDescription = producto.nombre,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f) // 🔥 clave para que no se rompa
            ) {
                Text(producto.nombre, fontSize = 18.sp)
                Text(producto.precio, color = Color.Gray)
            }

            Button(
                onClick = {
                    carrito.add(producto)
                    CarritoStorage.guardar(context, carrito)
                    agregado = true
                },
                modifier = Modifier.width(100.dp) // 🔥 tamaño fijo
            ) {
                Text("Agregar", maxLines = 1)
            }
        }

        AnimatedVisibility(
            visible = agregado,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut()
        ) {
            Text(
                "✔ Agregado",
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)
            )
        }
    }
}

// 🛒 CARRITO
@Composable
fun CarritoScreen(onBack: () -> Unit) {

    val envio = 49
    val subtotal = carrito.sumOf { it.precio.replace("$", "").toInt() }
    val iva = (subtotal * 0.16).toInt()
    val total = subtotal + iva + if (carrito.isNotEmpty()) envio else 0

    var expanded by remember { mutableStateOf(false) }
    var direccion by remember { mutableStateOf("Casa - Calle 123") }

    val direcciones = listOf("Casa - Calle 123", "Trabajo - Oficina centro")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // 👤 info usuario
        Text("Juan Pérez", fontSize = 20.sp)
        Text("6441234567")
        Text(direccion)

        Spacer(modifier = Modifier.height(10.dp))

        // 📍 dropdown direcciones
        Box {
            Button(onClick = { expanded = true }) {
                Text("Seleccionar dirección")
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                direcciones.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            direccion = it
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("Carrito", fontSize = 22.sp)

        LazyColumn {
            items(carrito) {
                Text("${it.nombre} - ${it.precio}")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("Subtotal: $${subtotal}")
        Text("IVA: $${iva}")

        if (carrito.isNotEmpty()) {
            Text("Envío: $${envio}")
        }

        Text("Total: $${total}", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = onBack) {
            Text("Volver")
        }

        val context = LocalContext.current

        Button(onClick = {
            carrito.clear()
            CarritoStorage.guardar(context, carrito)
        }) {
            Text("Vaciar carrito")
        }
    }
}

// 🎁 CATEGORÍAS
@Composable
fun DetallesScreen() {
    val productos = listOf(
        Producto("Caja sorpresa", "$250", R.drawable.regalo1),
        Producto("Detalle cumpleaños", "$300", R.drawable.regalo2)
    )
    ListaProductos("Detalles", productos)
}

@Composable
fun GlobosScreen() {
    val productos = listOf(
        Producto("Globo cumpleaños", "$150", R.drawable.globo1)
    )
    ListaProductos("Globos", productos)
}

@Composable
fun PeluchesScreen() {
    val productos = listOf(
        Producto("Oso de peluche", "$300", R.drawable.peluche1)
    )
    ListaProductos("Peluches", productos)
}

// 🔁 REUTILIZABLE
@Composable
fun ListaProductos(titulo: String, productos: List<Producto>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text(titulo, fontSize = 24.sp)

        LazyColumn {
            items(productos) {
                ProductoItem(it)
            }
        }
    }
}

object CarritoStorage {

    fun guardar(context: Context, lista: List<Producto>) {
        val prefs = context.getSharedPreferences("carrito", Context.MODE_PRIVATE)
        val jsonArray = JSONArray()

        lista.forEach {
            val obj = JSONObject()
            obj.put("nombre", it.nombre)
            obj.put("precio", it.precio)
            obj.put("imagen", it.imagen)
            jsonArray.put(obj)
        }

        prefs.edit().putString("data", jsonArray.toString()).apply()
    }

    fun cargar(context: Context): MutableList<Producto> {
        val prefs = context.getSharedPreferences("carrito", Context.MODE_PRIVATE)
        val data = prefs.getString("data", null) ?: return mutableListOf()

        val lista = mutableListOf<Producto>()
        val jsonArray = JSONArray(data)

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            lista.add(
                Producto(
                    obj.getString("nombre"),
                    obj.getString("precio"),
                    obj.getInt("imagen")
                )
            )
        }
        return lista
    }
}