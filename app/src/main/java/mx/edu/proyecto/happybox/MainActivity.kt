package mx.edu.proyecto.happybox

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch
import mx.edu.proyecto.happybox.Domain.Producto
import mx.edu.proyecto.happybox.auth.AuthManager
import mx.edu.proyecto.happybox.auth.AuthRepository
import mx.edu.proyecto.happybox.ui.theme.HappyboxTheme
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random
import mx.edu.proyecto.happybox.auth.CarritoRepository

data class CartItem(
    val producto: Producto = Producto(),
    val cantidad: Int = 0
)

data class TarjetaInfo(
    val alias: String,
    val titular: String,
    val numero: String
)

fun generarCodigoVenta(): String {
    return Random.nextInt(100000, 1000000).toString()
}

fun enmascararTarjeta(numero: String): String {
    val limpia = numero.filter { it.isDigit() }
    val ultimos4 = limpia.takeLast(4)
    return if (ultimos4.isNotEmpty()) "**** **** **** $ultimos4" else "****"
}

val carrito = mutableStateListOf<CartItem>()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        setContent {
            HappyboxTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFFF7FA)
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val isLoggedIn = AuthRepository.getCurrentUser() != null
    var screen by rememberSaveable { mutableStateOf(if (isLoggedIn) "menu" else "inicio") }

    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var pantallaAnteriorProducto by rememberSaveable { mutableStateOf("menu") }

    val direcciones = remember { mutableStateListOf<String>() }
    val tarjetas = remember { mutableStateListOf<TarjetaInfo>() }

    var direccionSeleccionada by rememberSaveable { mutableStateOf("") }
    var tarjetaSeleccionada by rememberSaveable { mutableStateOf("") }
    var codigoVenta by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {

        // 🔥 CARGAR CARRITO FIREBASE
        CarritoRepository.obtenerCarrito { lista ->

            carrito.clear()
            carrito.addAll(lista)
        }

        // LOCAL
        direcciones.clear()
        direcciones.addAll(DireccionesStorage.cargar(context))

        tarjetas.clear()
        tarjetas.addAll(TarjetasStorage.cargar(context))
    }

    val pantallasConBottomBar = setOf(
        "menu", "detalles", "globos", "peluches", "regalos", "tazas", "todo", "carrito", "perfil"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFFFF7FA),
        contentWindowInsets = WindowInsets.systemBars,
        bottomBar = {
            if (screen in pantallasConBottomBar) {
                BottomBar(
                    currentScreen = screen,
                    onMenu = { screen = "menu" },
                    onCarrito = { screen = "carrito" }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = screen,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                },
                label = "screen_animation"
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
                        RegisterScreen { screen = "login" }
                    }

                    "menu" -> {
                        BackHandler { screen = "login" }
                        MenuScreen(
                            onDetalles = { screen = "detalles" },
                            onGlobos = { screen = "globos" },
                            onPeluches = { screen = "peluches" },
                            onRegalos = { screen = "regalos" },
                            onTazas = { screen = "tazas" },
                            onMostrarTodo = { screen = "todo" },
                            onPerfil = { screen = "perfil" }
                        )
                    }

                    "detalles" -> {
                        BackHandler { screen = "menu" }
                        DetallesScreen(
                            onProductoClick = {
                                productoSeleccionado = it
                                pantallaAnteriorProducto = "detalles"
                                screen = "detalle_producto"
                            }
                        )
                    }

                    "globos" -> {
                        BackHandler { screen = "menu" }
                        GlobosScreen(
                            onProductoClick = {
                                productoSeleccionado = it
                                pantallaAnteriorProducto = "globos"
                                screen = "detalle_producto"
                            }
                        )
                    }

                    "peluches" -> {
                        BackHandler { screen = "menu" }
                        PeluchesScreen(
                            onProductoClick = {
                                productoSeleccionado = it
                                pantallaAnteriorProducto = "peluches"
                                screen = "detalle_producto"
                            }
                        )
                    }

                    "regalos" -> {
                        BackHandler { screen = "menu" }
                        RegalosScreen(
                            onProductoClick = {
                                productoSeleccionado = it
                                pantallaAnteriorProducto = "regalos"
                                screen = "detalle_producto"
                            }
                        )
                    }

                    "tazas" -> {
                        BackHandler { screen = "menu" }
                        TazasScreen(
                            onProductoClick = {
                                productoSeleccionado = it
                                pantallaAnteriorProducto = "tazas"
                                screen = "detalle_producto"
                            }
                        )
                    }

                    "todo" -> {
                        BackHandler { screen = "menu" }
                        MostrarTodoScreen(
                            onProductoClick = {
                                productoSeleccionado = it
                                pantallaAnteriorProducto = "todo"
                                screen = "detalle_producto"
                            }
                        )
                    }

                    "detalle_producto" -> {
                        BackHandler { screen = pantallaAnteriorProducto }
                        productoSeleccionado?.let { producto ->
                            DetalleProductoScreen(
                                producto = producto,
                                onBack = { screen = pantallaAnteriorProducto },
                                onAgregar = { cantidad ->
                                    agregarAlCarrito(producto, cantidad, context)
                                    Toast.makeText(
                                        context,
                                        "Se agregaron $cantidad unidad(es) al carrito",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    screen = "carrito"
                                }
                            )
                        }
                    }

                    "carrito" -> {
                        BackHandler { screen = "menu" }
                        CarritoScreen(
                            onBack = { screen = "menu" },
                            onProcederCompra = { screen = "punto_entrega" }
                        )
                    }

                    "punto_entrega" -> {
                        BackHandler { screen = "carrito" }
                        PuntoEntregaScreen(
                            direcciones = direcciones,
                            direccionSeleccionada = direccionSeleccionada,
                            onSeleccionar = { direccionSeleccionada = it },
                            onAgregarDireccion = {
                                direcciones.add(it)
                                DireccionesStorage.guardar(context, direcciones)
                            },
                            onEliminarDireccion = {
                                direcciones.remove(it)
                                if (direccionSeleccionada == it) direccionSeleccionada = ""
                                DireccionesStorage.guardar(context, direcciones)
                            },
                            onBack = { screen = "carrito" },
                            onContinuar = { screen = "metodo_pago" }
                        )
                    }

                    "metodo_pago" -> {
                        BackHandler { screen = "punto_entrega" }
                        MetodoPagoScreen(
                            onBack = { screen = "punto_entrega" },
                            onEfectivo = {
                                codigoVenta = generarCodigoVenta()
                                screen = "pago_efectivo"
                            },
                            onTarjeta = {
                                tarjetaSeleccionada = ""
                                screen = "pago_tarjeta"
                            }
                        )
                    }

                    "pago_efectivo" -> {
                        BackHandler { screen = "metodo_pago" }
                        PagoEfectivoScreen(
                            codigoVenta = codigoVenta,
                            direccionSeleccionada = direccionSeleccionada,
                            onBack = { screen = "metodo_pago" },
                            onFinalizarCompra = {
                                carrito.clear()
                                CarritoRepository.vaciarCarrito()
                                Toast.makeText(
                                    context,
                                    "Compra finalizada con éxito",
                                    Toast.LENGTH_SHORT
                                ).show()
                                screen = "menu"
                            }
                        )
                    }

                    "pago_tarjeta" -> {
                        BackHandler { screen = "metodo_pago" }
                        PagoTarjetaScreen(
                            tarjetas = tarjetas,
                            tarjetaSeleccionada = tarjetaSeleccionada,
                            onSeleccionar = { tarjetaSeleccionada = it },
                            onAgregarTarjeta = {
                                tarjetas.add(it)
                                TarjetasStorage.guardar(context, tarjetas)
                            },
                            onEliminarTarjeta = { numero ->
                                val tarjetaAEliminar = tarjetas.firstOrNull { it.numero == numero }
                                if (tarjetaAEliminar != null) {
                                    tarjetas.remove(tarjetaAEliminar)
                                    if (tarjetaSeleccionada == numero) tarjetaSeleccionada = ""
                                    TarjetasStorage.guardar(context, tarjetas)
                                }
                            },
                            onBack = { screen = "metodo_pago" },
                            onFinalizarCompra = {
                                carrito.clear()
                                CarritoRepository.vaciarCarrito()
                                Toast.makeText(
                                    context,
                                    "Pago realizado con éxito",
                                    Toast.LENGTH_SHORT
                                ).show()
                                screen = "menu"
                            }
                        )
                    }

                    "perfil" -> {
                        BackHandler { screen = "menu" }
                        PerfilScreen(
                            onConfig = { screen = "config" }
                        )
                    }

                    "config" -> {
                        BackHandler { screen = "perfil" }
                        ConfigScreen(
                            onPerfilEdit = { screen = "editarPerfil" },
                            onSoporte = { screen = "soporte" },
                            onAyuda = { screen = "ayuda" },
                            onLogout = {

                                // 🔥 limpiar carrito local
                                carrito.clear()

                                // logout firebase
                                AuthRepository.logout()

                                screen = "login"
                            }
                        )
                    }

                    "soporte" -> {
                        BackHandler { screen = "config" }
                        SoporteScreen()
                    }

                    "ayuda" -> {
                        BackHandler { screen = "config" }
                        AyudaScreen()
                    }

                    "editarPerfil" -> {
                        BackHandler { screen = "config" }
                        EditarPerfilScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun InicioScreen(onEntrar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF7FA))
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_happy_box),
                    contentDescription = "logo",
                    modifier = Modifier.size(190.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Envolturas, detalles y globos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2F2F2F)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sorprende con regalos especiales para cualquier ocasión",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = onEntrar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B6EA6)
                    )
                ) {
                    Text("Entrar", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun LoginScreen(onLogin: () -> Unit, onRegistro: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF7FA))
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Bienvenido",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F2F2F)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    "Inicia sesión para continuar",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = mostrarPassword,
                        onCheckedChange = { mostrarPassword = it }
                    )
                    Text("Mostrar contraseña")
                }

                if (error.isNotBlank()) {
                    Text(error, color = Color.Red)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val errorMsg = AuthManager.validarLogin(email, password)
                        if (errorMsg != null) {
                            error = errorMsg
                        } else {
                            error = ""
                            AuthRepository.login(email, password) { success, msg ->
                                if (success) {

                                    // 🔥 cargar carrito del usuario
                                    CarritoRepository.obtenerCarrito { lista ->

                                        carrito.clear()
                                        carrito.addAll(lista)

                                        onLogin()
                                    }

                                } else {
                                    error = msg
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B6EA6))
                ) {
                    Text("Iniciar sesión")
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("¿No tienes cuenta? ")
                    TextButton(onClick = onRegistro) {
                        Text("Registrarse")
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(onBack: () -> Unit) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    var mostrarPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF7FA))
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Crear cuenta",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F2F2F)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Regístrate para descubrir regalos especiales",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    visualTransformation = if (mostrarPassword) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar contraseña") },
                    visualTransformation = if (mostrarPassword) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = mostrarPassword,
                        onCheckedChange = { mostrarPassword = it }
                    )
                    Text("Mostrar contraseñas")
                }

                if (error.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val errorMsg = AuthManager.validarRegistro(
                            nombre, correo, telefono, password, confirmPassword
                        )

                        if (errorMsg != null) {
                            error = errorMsg
                        } else {
                            error = ""

                            AuthRepository.register(
                                nombre,
                                correo,
                                telefono,
                                password
                            ) { success, msg ->
                                if (success) {
                                    onBack()
                                } else {
                                    error = msg
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B6EA6)
                    )
                ) {
                    Text("Registrarse")
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(onClick = onBack) {
                    Text("Ya tengo cuenta")
                }
            }
        }
    }
}

@Composable
fun MenuScreen(
    onDetalles: () -> Unit,
    onGlobos: () -> Unit,
    onPeluches: () -> Unit,
    onRegalos: () -> Unit,
    onTazas: () -> Unit,
    onMostrarTodo: () -> Unit,
    onPerfil: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF7FA))
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
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

                Image(
                    painter = painterResource(R.drawable.titulocategorias),
                    contentDescription = "Categorias",
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .align(Alignment.Center),
                    contentScale = ContentScale.Fit
                )
            }

            Text(
                text = "Elige una categoría para encontrar el regalo ideal",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Descubre regalos únicos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF2F2F2F)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Encuentra detalles especiales para cumpleaños, aniversarios y más",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            BotonCategoria(
                texto = "Mostrar todo",
                subtitulo = "Todos los productos disponibles",
                onClick = onMostrarTodo
            )
            Spacer(modifier = Modifier.height(12.dp))

            BotonCategoria(
                texto = "Detalles",
                subtitulo = "Cajas, sorpresas y regalos",
                onClick = onDetalles
            )
            Spacer(modifier = Modifier.height(12.dp))

            BotonCategoria(
                texto = "Globos",
                subtitulo = "Decoración y celebraciones",
                onClick = onGlobos
            )
            Spacer(modifier = Modifier.height(12.dp))

            BotonCategoria(
                texto = "Peluches",
                subtitulo = "Regalos tiernos y especiales",
                onClick = onPeluches
            )
            Spacer(modifier = Modifier.height(12.dp))

            BotonCategoria(
                texto = "Regalos",
                subtitulo = "Opciones para cada ocasión",
                onClick = onRegalos
            )
            Spacer(modifier = Modifier.height(12.dp))

            BotonCategoria(
                texto = "Tazas",
                subtitulo = "Detalles personalizados",
                onClick = onTazas
            )
        }
    }
}
@Composable
fun BotonCategoria(
    texto: String,
    subtitulo: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(Color(0xFFEAE4F7), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (texto) {
                        "Detalles" -> "🎁"
                        "Globos" -> "🎈"
                        "Peluches" -> "🧸"
                        "Regalos" -> "💝"
                        "Tazas" -> "☕"
                        "Mostrar todo" -> "✨"
                        else -> "✨"
                    },
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = texto,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F2F2F)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitulo,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = "›",
                fontSize = 26.sp,
                color = Color(0xFF5B6EA6),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BottomBar(
    currentScreen: String,
    onMenu: () -> Unit,
    onCarrito: () -> Unit
) {
    val totalItems = carrito.sumOf { it.cantidad }

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 10.dp,
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.navigationBars.only(
                androidx.compose.foundation.layout.WindowInsetsSides.Bottom
            )
        )
    ) {
        NavigationBarItem(
            selected = currentScreen == "menu",
            onClick = onMenu,
            icon = {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menú",
                    tint = if (currentScreen == "menu") Color(0xFF5B6EA6) else Color(0xFF9A9A9A),
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Menú",
                    fontSize = 12.sp,
                    fontWeight = if (currentScreen == "menu") FontWeight.SemiBold else FontWeight.Normal,
                    color = if (currentScreen == "menu") Color(0xFF5B6EA6) else Color(0xFF9A9A9A)
                )
            }
        )

        NavigationBarItem(
            selected = currentScreen == "carrito",
            onClick = onCarrito,
            icon = {
                Box {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "Carrito",
                        tint = if (currentScreen == "carrito") Color(0xFF5B6EA6) else Color(0xFF9A9A9A),
                        modifier = Modifier.size(24.dp)
                    )

                    if (totalItems > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 9.dp, y = (-6).dp)
                                .background(Color(0xFFE53935), CircleShape)
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = totalItems.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            label = {
                Text(
                    text = "Carrito",
                    fontSize = 12.sp,
                    fontWeight = if (currentScreen == "carrito") FontWeight.SemiBold else FontWeight.Normal,
                    color = if (currentScreen == "carrito") Color(0xFF5B6EA6) else Color(0xFF9A9A9A)
                )
            }
        )
    }
}

@Composable
fun PerfilScreen(onConfig: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(true) }

    var ubicaciones by remember {
        mutableStateOf(listOf("Casa - Calle 123"))
    }

    var mostrarFormulario by remember { mutableStateOf(false) }

    var cp by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("") }
    var calle by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var entreCalles by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        AuthRepository.obtenerDatosUsuario {
            if (it != null) {
                nombre = it["nombre"] ?: ""
                telefono = it["telefono"] ?: ""
            }
            cargando = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF7FA))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.tituloperfil),
                contentDescription = "Perfil",
                modifier = Modifier
                    .height(80.dp)
                    .align(Alignment.Center)
            )

            IconButton(
                onClick = onConfig,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configuración",
                    tint = Color(0xFF5B6EA6)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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

                if (cargando) {
                    Text("Cargando...", color = Color.Gray)
                } else {
                    Text(
                        text = nombre.ifBlank { "Sin nombre" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = telefono.ifBlank { "Sin teléfono" },
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Mis ubicaciones",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

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

@Composable
fun ConfigScreen(
    onSoporte: () -> Unit,
    onAyuda: () -> Unit,
    onPerfilEdit: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Configuración", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onPerfilEdit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Mi perfil")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onSoporte,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Soporte")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onAyuda,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ayuda")
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión", color = Color.White)
        }
    }
}

@Composable
fun SoporteScreen() {
    var problema by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Reportar un problema",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = problema,
            onValueChange = { problema = it },
            label = { Text("Describe el problema") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enviar reporte")
        }
    }
}

@Composable
fun EditarPerfilScreen() {

    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    var cargando by remember { mutableStateOf(true) }
    var guardando by remember { mutableStateOf(false) }

    var mensaje by remember { mutableStateOf("") }

    // 🔥 cargar datos reales
    LaunchedEffect(Unit) {

        mx.edu.proyecto.happybox.auth.AuthRepository.obtenerDatosUsuario {

            if (it != null) {

                nombre = it["nombre"] ?: ""
                telefono = it["telefono"] ?: ""
            }

            cargando = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF7FA))
            .padding(16.dp)
    ) {

        Text(
            "Editar perfil",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (cargando) {

            Text("Cargando datos...")

        } else {

            // 👤 NOMBRE
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 📱 TELÉFONO
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 🔥 MENSAJE
            if (mensaje.isNotBlank()) {

                Text(
                    text = mensaje,
                    color = if (mensaje.contains("correctamente")) {
                        Color(0xFF2E7D32)
                    } else {
                        Color.Red
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // 💾 GUARDAR
            Button(
                onClick = {

                    if (nombre.isBlank() || telefono.isBlank()) {

                        mensaje = "Completa todos los campos"
                        return@Button
                    }

                    guardando = true

                    mx.edu.proyecto.happybox.auth.AuthRepository.actualizarUsuario(
                        nombre,
                        telefono
                    ) { success ->

                        guardando = false

                        mensaje = if (success) {
                            "Perfil actualizado correctamente"
                        } else {
                            "Error al actualizar"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !guardando
            ) {

                Text(
                    if (guardando) {
                        "Guardando..."
                    } else {
                        "Guardar cambios"
                    }
                )
            }
        }
    }
}

@Composable
fun AyudaScreen() {
    val preguntas = listOf(
        "¿Qué métodos de pago hay?" to "Efectivo y tarjeta",
        "¿Hacen envíos?" to "Sí, a domicilio",
        "¿Cuánto tarda el pedido?" to "De 1 a 3 días",
        "¿Puedo personalizar productos?" to "Sí, algunos productos"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Ayuda",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(preguntas) { (pregunta, respuesta) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            pregunta,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(respuesta, color = Color.Gray)
                    }
                }
            }
        }
    }
}

fun todosLosProductos(): List<Producto> {
    return listOf(
        Producto("Caja sorpresa", "$250", R.drawable.regalo1),
        Producto("Detalle cumpleaños", "$300", R.drawable.regalo2),
        Producto("Globo cumpleaños", "$150", R.drawable.globo1),
        Producto("Oso de peluche", "$300", R.drawable.peluche1),
        Producto("Caja premium con globos", "$450", R.drawable.regalo1),
        Producto("Taza personalizada", "$180", R.drawable.taza1)
    )
}

@Composable
fun DetallesScreen(onProductoClick: (Producto) -> Unit) {
    val productos = listOf(
        Producto("Caja sorpresa", "$250", R.drawable.regalo1),
        Producto("Detalle cumpleaños", "$300", R.drawable.regalo2)
    )
    ListaProductos("Detalles", productos, onProductoClick)
}

@Composable
fun GlobosScreen(onProductoClick: (Producto) -> Unit) {
    val productos = listOf(
        Producto("Globo cumpleaños", "$150", R.drawable.globo1)
    )
    ListaProductos("Globos", productos, onProductoClick)
}

@Composable
fun PeluchesScreen(onProductoClick: (Producto) -> Unit) {
    val productos = listOf(
        Producto("Oso de peluche", "$300", R.drawable.peluche1)
    )
    ListaProductos("Peluches", productos, onProductoClick)
}

@Composable
fun RegalosScreen(onProductoClick: (Producto) -> Unit) {
    val productos = listOf(
        Producto("Caja premium con globos", "$450", R.drawable.regalo1)
    )
    ListaProductos("Regalos", productos, onProductoClick)
}

@Composable
fun TazasScreen(onProductoClick: (Producto) -> Unit) {
    val productos = listOf(
        Producto("Taza personalizada", "$180", R.drawable.taza1)
    )
    ListaProductos("Tazas", productos, onProductoClick)
}

@Composable
fun MostrarTodoScreen(onProductoClick: (Producto) -> Unit) {
    ListaProductos("Mostrar todo", todosLosProductos(), onProductoClick)
}

@Composable
fun ListaProductos(
    titulo: String,
    productos: List<Producto>,
    onProductoClick: (Producto) -> Unit
) {
    var busqueda by rememberSaveable { mutableStateOf("") }

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
            text = if (titulo == "Mostrar todo") {
                "Aquí puedes ver todos los productos disponibles"
            } else {
                "Encuentra un detalle especial para esa ocasión"
            },
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
                    ProductoItem(
                        producto = producto,
                        onClick = { onProductoClick(producto) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductoItem(
    producto: Producto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F1FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = producto.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2F2F2F)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = producto.precio,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF5B6EA6)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B6EA6))
            ) {
                Text("Ver detalle")
            }
        }
    }
}

@Composable
fun DetalleProductoScreen(
    producto: Producto,
    onBack: () -> Unit,
    onAgregar: (Int) -> Unit
) {
    var cantidad by rememberSaveable { mutableIntStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF7FA))
            .padding(16.dp)
    ) {
        OutlinedButton(
            onClick = onBack,
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Volver")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(producto.imagen),
                    contentDescription = producto.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = producto.nombre,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F2F2F)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = producto.precio,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF5B6EA6)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Un detalle especial ideal para sorprender en cumpleaños, celebraciones y momentos importantes.",
                    fontSize = 15.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Cantidad",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilledTonalButton(
                        onClick = { if (cantidad > 1) cantidad-- }
                    ) {
                        Text("-")
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    Text(
                        text = cantidad.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(18.dp))

                    FilledTonalButton(
                        onClick = { cantidad++ }
                    ) {
                        Text("+")
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = { onAgregar(cantidad) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B6EA6)
                    )
                ) {
                    Text("Agregar al carrito")
                }
            }
        }
    }
}

fun agregarAlCarrito(producto: Producto, cantidad: Int, context: Context) {

    if (cantidad <= 0) return

    val index = carrito.indexOfFirst {
        it.producto.nombre == producto.nombre
    }

    if (index >= 0) {

        val itemActual = carrito[index]

        carrito[index] = itemActual.copy(
            cantidad = itemActual.cantidad + cantidad
        )

    } else {

        carrito.add(
            CartItem(
                producto = producto,
                cantidad = cantidad
            )
        )
    }

    // 🔥 GUARDAR FIREBASE
    CarritoRepository.guardarCarrito(carrito)
}

fun aumentarCantidad(producto: Producto, context: Context) {

    val index = carrito.indexOfFirst {
        it.producto.nombre == producto.nombre
    }

    if (index >= 0) {

        val itemActual = carrito[index]

        carrito[index] = itemActual.copy(
            cantidad = itemActual.cantidad + 1
        )

        CarritoRepository.guardarCarrito(carrito)
    }
}

fun disminuirCantidad(producto: Producto, context: Context) {

    val index = carrito.indexOfFirst {
        it.producto.nombre == producto.nombre
    }

    if (index >= 0) {

        val itemActual = carrito[index]

        if (itemActual.cantidad <= 1) {
            carrito.removeAt(index)
        } else {

            carrito[index] = itemActual.copy(
                cantidad = itemActual.cantidad - 1
            )
        }

        CarritoRepository.guardarCarrito(carrito)
    }
}

fun precioNumerico(precio: String): Int {
    return precio.replace("$", "").trim().toIntOrNull() ?: 0
}

@Composable
fun CarritoScreen(
    onBack: () -> Unit,
    onProcederCompra: () -> Unit
) {
    val context = LocalContext.current

    val envio = 49
    val subtotal = carrito.sumOf { precioNumerico(it.producto.precio) * it.cantidad }
    val iva = (subtotal * 0.16).toInt()
    val total = subtotal + iva + if (carrito.isNotEmpty()) envio else 0

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
                items(carrito, key = { it.producto.nombre }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F1FA)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(item.producto.imagen),
                                contentDescription = item.producto.nombre,
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.producto.nombre,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2F2F2F)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = item.producto.precio,
                                    color = Color(0xFF5B6EA6),
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Subtotal: $${precioNumerico(item.producto.precio) * item.cantidad}",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                FilledTonalButton(
                                    onClick = { aumentarCantidad(item.producto, context) },
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text("+")
                                }

                                Text(
                                    text = item.cantidad.toString(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )

                                FilledTonalButton(
                                    onClick = { disminuirCantidad(item.producto, context) },
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text("-")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                    if (carrito.isNotEmpty()) {
                        onProcederCompra()
                    }
                },
                enabled = carrito.isNotEmpty(),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5B6EA6),
                    disabledContainerColor = Color(0xFFB8C0D9)
                )
            ) {
                Text("Proceder compra")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = {
                carrito.clear()
                CarritoRepository.vaciarCarrito()
                Toast.makeText(context, "Carrito vaciado", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Vaciar carrito")
        }
    }
}

@Composable
fun PuntoEntregaScreen(
    direcciones: List<String>,
    direccionSeleccionada: String,
    onSeleccionar: (String) -> Unit,
    onAgregarDireccion: (String) -> Unit,
    onEliminarDireccion: (String) -> Unit,
    onBack: () -> Unit,
    onContinuar: () -> Unit
) {
    var mostrarFormulario by remember { mutableStateOf(false) }

    var cp by rememberSaveable { mutableStateOf("") }
    var estado by rememberSaveable { mutableStateOf("") }
    var ciudad by rememberSaveable { mutableStateOf("") }
    var calle by rememberSaveable { mutableStateOf("") }
    var numero by rememberSaveable { mutableStateOf("") }
    var entreCalles by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF7FA))
            .padding(16.dp)
    ) {
        Text(
            text = "Punto de entrega",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F2F2F)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Selecciona una dirección registrada o agrega una nueva.",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (direcciones.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay puntos de entrega registrados",
                    color = Color.Gray,
                    fontSize = 18.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(direcciones) { direccion ->
                    val seleccionada = direccionSeleccionada == direccion

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (seleccionada) Color(0xFFE7ECF8) else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = direccion,
                                fontSize = 15.sp,
                                fontWeight = if (seleccionada) FontWeight.SemiBold else FontWeight.Normal,
                                color = Color(0xFF2F2F2F)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onSeleccionar(direccion) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(if (seleccionada) "Seleccionada" else "Elegir")
                                }

                                TextButton(
                                    onClick = { onEliminarDireccion(direccion) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { mostrarFormulario = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B6EA6))
        ) {
            Text("Agregar punto de entrega")
        }

        Spacer(modifier = Modifier.height(10.dp))

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
                onClick = onContinuar,
                enabled = direccionSeleccionada.isNotBlank(),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5B6EA6),
                    disabledContainerColor = Color(0xFFB8C0D9)
                )
            ) {
                Text("Proceder compra")
            }
        }
    }

    if (mostrarFormulario) {
        AlertDialog(
            onDismissRequest = { mostrarFormulario = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (
                            calle.isNotBlank() &&
                            numero.isNotBlank() &&
                            ciudad.isNotBlank() &&
                            estado.isNotBlank() &&
                            cp.isNotBlank()
                        ) {
                            val nueva = "$calle #$numero, $ciudad, $estado, CP $cp ($entreCalles)"
                            onAgregarDireccion(nueva)
                            onSeleccionar(nueva)

                            cp = ""
                            estado = ""
                            ciudad = ""
                            calle = ""
                            numero = ""
                            entreCalles = ""
                            mostrarFormulario = false
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { mostrarFormulario = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Nuevo punto de entrega") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = cp, onValueChange = { cp = it }, label = { Text("CP") })
                    OutlinedTextField(value = estado, onValueChange = { estado = it }, label = { Text("Estado") })
                    OutlinedTextField(value = ciudad, onValueChange = { ciudad = it }, label = { Text("Ciudad") })
                    OutlinedTextField(value = calle, onValueChange = { calle = it }, label = { Text("Calle principal") })
                    OutlinedTextField(value = numero, onValueChange = { numero = it }, label = { Text("Número exterior") })
                    OutlinedTextField(value = entreCalles, onValueChange = { entreCalles = it }, label = { Text("Entre qué calles") })
                }
            }
        )
    }
}

@Composable
fun MetodoPagoScreen(
    onBack: () -> Unit,
    onEfectivo: () -> Unit,
    onTarjeta: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF7FA))
            .padding(16.dp)
    ) {
        Text(
            text = "Seleccionar método de pago",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F2F2F)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Elige cómo deseas finalizar tu compra.",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(22.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(5.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Efectivo",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Se generará un código de venta para presentarlo en caja.",
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onEfectivo,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B6EA6))
                ) {
                    Text("Pagar en efectivo")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(5.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Tarjeta de crédito / débito",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Selecciona una tarjeta registrada o agrega una nueva.",
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onTarjeta,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B6EA6))
                ) {
                    Text("Pagar con tarjeta")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Volver")
        }
    }
}

@Composable
fun PagoEfectivoScreen(
    codigoVenta: String,
    direccionSeleccionada: String,
    onBack: () -> Unit,
    onFinalizarCompra: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF7FA))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pago en efectivo",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F2F2F)
        )

        Spacer(modifier = Modifier.height(22.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(5.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Punto de entrega",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = direccionSeleccionada,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Código de venta",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = codigoVenta,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5B6EA6)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Presentar código de pedido en caja",
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

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
                onClick = onFinalizarCompra,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B6EA6))
            ) {
                Text("Finalizar compra")
            }
        }
    }
}

@Composable
fun PagoTarjetaScreen(
    tarjetas: List<TarjetaInfo>,
    tarjetaSeleccionada: String,
    onSeleccionar: (String) -> Unit,
    onAgregarTarjeta: (TarjetaInfo) -> Unit,
    onEliminarTarjeta: (String) -> Unit,
    onBack: () -> Unit,
    onFinalizarCompra: () -> Unit
) {
    var mostrarFormulario by remember { mutableStateOf(false) }

    var alias by rememberSaveable { mutableStateOf("") }
    var titular by rememberSaveable { mutableStateOf("") }
    var numero by rememberSaveable { mutableStateOf("") }
    var errorTarjeta by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF7FA))
            .padding(16.dp)
    ) {
        Text(
            text = "Tarjetas registradas",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F2F2F)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Selecciona una tarjeta para continuar o registra una nueva.",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (tarjetas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay tarjetas registradas",
                    color = Color.Gray,
                    fontSize = 18.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tarjetas) { tarjeta ->
                    val seleccionada = tarjetaSeleccionada == tarjeta.numero

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (seleccionada) Color(0xFFE7ECF8) else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = tarjeta.alias.ifBlank { "Tarjeta" },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = tarjeta.titular,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = enmascararTarjeta(tarjeta.numero),
                                color = Color(0xFF5B6EA6),
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onSeleccionar(tarjeta.numero) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(if (seleccionada) "Seleccionada" else "Elegir")
                                }

                                TextButton(
                                    onClick = { onEliminarTarjeta(tarjeta.numero) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { mostrarFormulario = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B6EA6))
        ) {
            Text("Registrar tarjeta")
        }

        Spacer(modifier = Modifier.height(10.dp))

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
                onClick = onFinalizarCompra,
                enabled = tarjetaSeleccionada.isNotBlank(),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5B6EA6),
                    disabledContainerColor = Color(0xFFB8C0D9)
                )
            ) {
                Text("Finalizar compra")
            }
        }
    }

    if (mostrarFormulario) {
        AlertDialog(
            onDismissRequest = { mostrarFormulario = false },
            confirmButton = {
                Button(
                    onClick = {
                        val numeroLimpio = numero.filter { it.isDigit() }

                        when {
                            alias.isBlank() || titular.isBlank() || numeroLimpio.isBlank() -> {
                                errorTarjeta = "Completa todos los campos."
                            }
                            numeroLimpio.length != 16 -> {
                                errorTarjeta = "La tarjeta debe tener 16 dígitos."
                            }
                            else -> {
                                val nueva = TarjetaInfo(
                                    alias = alias,
                                    titular = titular,
                                    numero = numeroLimpio
                                )
                                onAgregarTarjeta(nueva)
                                onSeleccionar(numeroLimpio)

                                alias = ""
                                titular = ""
                                numero = ""
                                errorTarjeta = ""
                                mostrarFormulario = false
                            }
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { mostrarFormulario = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Nueva tarjeta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = alias,
                        onValueChange = { alias = it },
                        label = { Text("Alias") }
                    )
                    OutlinedTextField(
                        value = titular,
                        onValueChange = { titular = it },
                        label = { Text("Titular") }
                    )
                    OutlinedTextField(
                        value = numero,
                        onValueChange = { nuevo ->
                            numero = nuevo.filter { it.isDigit() }.take(16)
                            if (errorTarjeta.isNotBlank()) errorTarjeta = ""
                        },
                        label = { Text("Número de tarjeta") }
                    )

                    if (errorTarjeta.isNotBlank()) {
                        Text(
                            text = errorTarjeta,
                            color = Color.Red,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        )
    }
}

object DireccionesStorage {
    fun guardar(context: Context, direcciones: List<String>) {
        val prefs = context.getSharedPreferences("direcciones", Context.MODE_PRIVATE)
        val jsonArray = JSONArray()

        direcciones.forEach { direccion ->
            jsonArray.put(direccion)
        }

        prefs.edit().putString("data", jsonArray.toString()).apply()
    }

    fun cargar(context: Context): MutableList<String> {
        val prefs = context.getSharedPreferences("direcciones", Context.MODE_PRIVATE)
        val data = prefs.getString("data", null)

        if (data.isNullOrBlank()) {
            return mutableListOf()
        }

        val jsonArray = JSONArray(data)
        val lista = mutableListOf<String>()

        for (i in 0 until jsonArray.length()) {
            lista.add(jsonArray.getString(i))
        }

        return lista
    }
}

object TarjetasStorage {
    fun guardar(context: Context, tarjetas: List<TarjetaInfo>) {
        val prefs = context.getSharedPreferences("tarjetas", Context.MODE_PRIVATE)
        val jsonArray = JSONArray()

        tarjetas.forEach { tarjeta ->
            val obj = JSONObject()
            obj.put("alias", tarjeta.alias)
            obj.put("titular", tarjeta.titular)
            obj.put("numero", tarjeta.numero)
            jsonArray.put(obj)
        }

        prefs.edit().putString("data", jsonArray.toString()).apply()
    }

    fun cargar(context: Context): MutableList<TarjetaInfo> {
        val prefs = context.getSharedPreferences("tarjetas", Context.MODE_PRIVATE)
        val data = prefs.getString("data", null)

        if (data.isNullOrBlank()) {
            return mutableListOf()
        }

        val jsonArray = JSONArray(data)
        val lista = mutableListOf<TarjetaInfo>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            lista.add(
                TarjetaInfo(
                    alias = obj.optString("alias"),
                    titular = obj.optString("titular"),
                    numero = obj.optString("numero")
                )
            )
        }

        return lista
    }
}
