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
import android.view.Menu
import androidx.compose.foundation.shape.CircleShape
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.draw.scale
import android.widget.Toast
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight


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
                        onRegalos = { screen = "regalos" }, // 👈 AQUÍ
                        onTazas = { screen = "tazas" },     // 👈 AQUÍ
                        onCarrito = { screen = "carrito" },
                        onPerfil = { screen = "perfil" },
                        onMenu = { screen = "menu" }
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
                "regalos" -> {
                    BackHandler { screen = "menu" }
                    RegalosScreen()
                }

                "tazas" -> {
                    BackHandler { screen = "menu" }
                    TazasScreen()
                }
            }
        }

        // 🔻 🔥 BOTTOM BAR GLOBAL
        if (screen != "inicio" && screen != "login" && screen != "registro") {
            BottomBar(
                onCarrito = { screen = "carrito" },
                onMenu = { screen = "menu" }
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
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crear cuenta", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
        OutlinedTextField(value = apellidos, onValueChange = { apellidos = it }, label = { Text("Apellidos") })
        OutlinedTextField(value = edad, onValueChange = { edad = it }, label = { Text("Edad") })
        OutlinedTextField(value = direccion, onValueChange = { direccion = it }, label = { Text("Dirección") })
        OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") })
        OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo") })
        OutlinedTextField(value = contrasena, onValueChange = { contrasena = it }, label = { Text("Contraseña") })

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
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
    onRegalos: () -> Unit,
    onTazas: () -> Unit,
    onCarrito: () -> Unit,
    onPerfil: () -> Unit,
    onMenu: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF7FA)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {

            // 👤 ICONO PERFIL
            IconButton(
                onClick = onPerfil,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Perfil",
                    tint = Color(0xFF5B6EA6)
                )
            }

            // 🖼️ TÍTULO CENTRADO REAL
            Image(
                painter = painterResource(R.drawable.titulocategorias),
                contentDescription = "Categorias",
                modifier = Modifier
                    .fillMaxWidth(0.7f) // 👈 ESTO (tamaño más grande proporcional)
                    .align(Alignment.Center),
                contentScale = ContentScale.Fit
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Elige una categoría para encontrar el regalo ideal",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            BotonCategoria("Detalles", onDetalles)
            Spacer(modifier = Modifier.height(12.dp))

            BotonCategoria("Globos", onGlobos)
            Spacer(modifier = Modifier.height(12.dp))

            BotonCategoria("Peluches", onPeluches)
            Spacer(modifier = Modifier.height(12.dp))

            BotonCategoria("Regalos", onRegalos)
            Spacer(modifier = Modifier.height(12.dp))

            BotonCategoria("Tazas", onTazas)
        }
    }
}

@Composable
fun BotonCategoria(texto: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF5B6EA6)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = texto,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun BottomBar(
    onMenu: () -> Unit,
    onCarrito: () -> Unit
) {
    val size = carrito.size
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
        IconButton(onClick = onMenu) {
            Icon(Icons.Default.Menu, contentDescription = "Menú")
        }

        Box {
            IconButton(onClick = onCarrito) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
            }

            if (size > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp)
                        .scale(scale.value)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF7FA))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 🖼️ TÍTULO CON TU IMAGEN
        Image(
            painter = painterResource(R.drawable.tituloperfil),
            contentDescription = "Perfil",
            modifier = Modifier.height(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 👤 TARJETA USUARIO
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Icon(
                    Icons.Default.Person,
                    contentDescription = "Usuario",
                    modifier = Modifier
                        .size(70.dp)
                        .background(Color(0xFFEDE7F6), CircleShape)
                        .padding(12.dp),
                    tint = Color(0xFF5B6EA6)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    "Juan Pérez",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "6441234567",
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 📍 TÍTULO UBICACIONES
        Text(
            text = "Mis ubicaciones",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 📦 LISTA BONITA
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ubicaciones) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ➕ BOTÓN
        Button(
            onClick = { mostrarFormulario = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5B6EA6)
            )
        ) {
            Text("+ Agregar ubicación")
        }
    }

    // 🔥 FORMULARIO (EL MISMO QUE YA TENÍAS)
    if (mostrarFormulario) {
        AlertDialog(
            onDismissRequest = { mostrarFormulario = false },
            confirmButton = {
                Button(onClick = {
                    val nueva = "$calle #$numero, $ciudad, $estado, CP $cp ($entreCalles)"
                    ubicaciones = ubicaciones + nueva

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
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF4F1FA)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(producto.imagen),
                    contentDescription = producto.nombre,
                    modifier = Modifier
                        .size(82.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = producto.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2F2F2F),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = producto.precio,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF5B6EA6)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        carrito.add(producto)
                        CarritoStorage.guardar(context, carrito)
                        Toast.makeText(context, "Producto agregado al carrito", Toast.LENGTH_SHORT).show()
                        agregado = true
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B6EA6)
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Agregar",
                        maxLines = 1,
                        fontSize = 13.sp
                    )
                }
            }

            AnimatedVisibility(
                visible = agregado,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut()
            ) {
                Text(
                    text = "✔ Agregado al carrito",
                    color = Color(0xFF43A047),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 10.dp, start = 4.dp)
                )
            }
        }
    }
}

// 🛒 CARRITO
@Composable
fun CarritoScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    val envio = 49
    val subtotal = carrito.sumOf { it.precio.replace("$", "").toInt() }
    val iva = (subtotal * 0.16).toInt()
    val total = subtotal + iva + if (carrito.isNotEmpty()) envio else 0

    var expanded by remember { mutableStateOf(false) }
    var direccion by remember { mutableStateOf("Casa - Calle 123") }

    val direcciones = listOf("Casa - Calle 123", "Trabajo - Oficina centro")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF7FA))
            .padding(16.dp)
    ) {
        Text(
            text = "Carrito",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F2F2F)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Juan Pérez",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2F2F2F)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("6441234567", color = Color.Gray)
                Text(direccion, color = Color.Gray)

                Spacer(modifier = Modifier.height(12.dp))

                Box {
                    Button(
                        onClick = { expanded = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5B6EA6)
                        )
                    ) {
                        Text("Seleccionar dirección")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
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
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (carrito.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tu carrito está vacío",
                    fontSize = 18.sp,
                    color = Color.Gray
                )
            }
        } else {
            Text(
                text = "Productos agregados",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2F2F2F)
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(carrito) { producto ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF4F1FA)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = producto.nombre,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2F2F2F)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = producto.precio,
                                    color = Color(0xFF5B6EA6),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Resumen de compra",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Subtotal: $${subtotal}", color = Color.Gray)
                    Text("IVA: $${iva}", color = Color.Gray)

                    if (carrito.isNotEmpty()) {
                        Text("Envío: $${envio}", color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Total: $${total}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5B6EA6)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Volver")
            }

            Button(
                onClick = {
                    carrito.clear()
                    CarritoStorage.guardar(context, carrito)
                    Toast.makeText(context, "Carrito vaciado", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5B6EA6)
                )
            ) {
                Text("Vaciar carrito")
            }
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
@Composable
fun RegalosScreen() {
    val productos = listOf(
        Producto("Caja premium con globos", "$450", R.drawable.regalo1)
    )
    ListaProductos("Regalos", productos)
}
@Composable
fun TazasScreen() {
    val productos = listOf(
        Producto("Taza personalizada", "$180", R.drawable.taza1)
    )
    ListaProductos("Tazas", productos)
}

// 🔁 REUTILIZABLE
@Composable
fun ListaProductos(titulo: String, productos: List<Producto>) {
    var busqueda by remember { mutableStateOf("") }

    val filtrados = productos.filter {
        it.nombre.contains(busqueda, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF7FA))
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(
                when (titulo) {
                    "Globos" -> R.drawable.tituloglobos
                    "Peluches" -> R.drawable.titulopeluches
                    "Detalles" -> R.drawable.titulodetalles
                    "Regalos" -> R.drawable.tituloregalos
                    "Tazas" -> R.drawable.titulotazas
                    else -> R.drawable.titulocategorias
                }
            ),
            contentDescription = titulo,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Encuentra un detalle especial para esa ocasión",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            label = { Text("Buscar producto") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filtrados.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontraron productos",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtrados) { producto ->
                    ProductoItem(producto)
                }
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