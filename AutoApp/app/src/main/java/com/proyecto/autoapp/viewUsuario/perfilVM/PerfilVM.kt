package com.proyecto.autoapp.viewUsuario.perfilVM

import PerfilUiState
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.Timestamp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.proyecto.autoapp.general.Coleccion
import com.proyecto.autoapp.general.DirectorioStorage
import com.proyecto.autoapp.general.funcionesComunes.formatE164
import com.proyecto.autoapp.general.modelo.dataClass.Vehiculo
import com.proyecto.autoapp.general.modelo.enumClass.Estado
import com.proyecto.autoapp.general.modelo.enumClass.RolUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.util.Calendar

class PerfilVM {
    var TAG = "Jose"
    var usuario = Coleccion.Usuario
    var vehiculo = Coleccion.Vehiculo
    private var nuevaFotoFile: File? = null

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState

    private val _vehiculos = mutableStateListOf<Vehiculo>()
    val vehiculos: List<Vehiculo> get() = _vehiculos

    private val _usuarioNuevo = mutableStateOf(false)
    val usuarioNuevo: Boolean get() = _usuarioNuevo.value

    fun modPerfilUsuario(usuarioActual: String, success: (Boolean) -> Unit) {
        val state = _uiState.value
        val edadInt = calcularEdad(state.fechaNacimiento)

        if (edadInt <= 0) {
            success(false)
            return
        }

        val telNormalizado = if (state.telefono.isBlank()) {
            ""
        } else {
            formatE164(state.telefono, defaultRegion = "ES")
        }

        if (state.telefono.isNotBlank() && telNormalizado == null) {
            Log.e(TAG, "Teléfono inválido: ${state.telefono}")
            success(false)
            return
        }

        val fechaNacimientoTimestamp = state.fechaNacimiento?.let { millis ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = millis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            Timestamp(cal.time)
        }

        val baseUpdates = mutableMapOf<String, Any>(
            "perfilPasajero.enabled" to state.isPasajeroSelected,
            "perfilConductor.enabled" to state.isConductorSelected,
            "edad" to edadInt,
            "telefono" to (telNormalizado ?: ""),
            "fechaNacimiento" to (fechaNacimientoTimestamp ?: Timestamp.now())
        )

        val file = nuevaFotoFile
        if (file == null) {
            db.collection(usuario)
                .document(usuarioActual)
                .update(baseUpdates as Map<String, Any>)
                .addOnSuccessListener {
                    _uiState.value = state.copy(
                        pasajeroEnabled = if (state.isPasajeroSelected) Estado.ACTIVO else Estado.PENDIENTE,
                        conductorEnabled = if (state.isConductorSelected) Estado.ACTIVO else Estado.PENDIENTE,
                        isSaveEnabled = false
                    )
                    success(true)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al actualizar perfil en Firestore", e)
                    success(false)
                }
        } else {
            val rutaStorage = "${DirectorioStorage.FotoPerfil}/$usuarioActual/${file.name}"
            val refStorage = FirebaseStorage.getInstance().reference.child(rutaStorage)
            val fileUri = Uri.fromFile(file)

            refStorage.putFile(fileUri)
                .continueWithTask { refStorage.downloadUrl }
                .addOnSuccessListener { uri ->
                    val url = uri.toString()
                    baseUpdates["fotoUrl"] = url

                    db.collection(usuario)
                        .document(usuarioActual)
                        .update(baseUpdates as Map<String, Any>)
                        .addOnSuccessListener {
                            nuevaFotoFile = null

                            _uiState.value = state.copy(
                                fotoPerfilUrl = url,
                                pasajeroEnabled = if (state.isPasajeroSelected) Estado.ACTIVO else Estado.PENDIENTE,
                                conductorEnabled = if (state.isConductorSelected) Estado.ACTIVO else Estado.PENDIENTE,
                                isSaveEnabled = false
                            )
                            success(true)
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error al actualizar perfil + foto en Firestore", e)
                            success(false)
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error subiendo foto de perfil a Storage", e)
                    success(false)
                }
        }
    }

    fun modEstadoConductor(usuarioActual: String, success: (Boolean) -> Unit) {
        val updates = mapOf(
            "perfilConductor.enabled" to true
        )

        db.collection(usuario)
            .document(usuarioActual)
            .update(updates)
            .addOnSuccessListener {
                _uiState.update {
                    it.copy(conductorEnabled = Estado.ACTIVO)
                }
                success(true)
            }
            .addOnFailureListener { e ->
                Log.e("PerfilVM", "Error al actualizar conductorEnabled en Firestore", e)
                success(false)
            }
    }

    fun modEstadoPasajero(usuarioActual: String, success: (Boolean) -> Unit) {
        val updates = mapOf(
            "perfilPasajero.enabled" to true
        )

        db.collection(usuario)
            .document(usuarioActual)
            .update(updates)
            .addOnSuccessListener {
                _uiState.update {
                    it.copy(
                        isPasajeroSelected = true,
                        pasajeroEnabled = Estado.ACTIVO
                    )
                }
                success(true)
            }
            .addOnFailureListener { e ->
                Log.e("PerfilVM", "Error al actualizar pasajeroEnabled en Firestore", e)
                success(false)
            }
    }

    fun cargarUsuario(usuActual: String) {
        db.collection(usuario)
            .document(usuActual)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    try {
                        val nombre = document.getString("nombre").orEmpty()
                        val apellidos = document.getString("apellidos").orEmpty()
                        val edad = (document.get("edad") as? Number)?.toInt() ?: 0
                        val email = document.getString("email").orEmpty()
                        val fotoUrl = document.getString("fotoUrl")
                        val telefono = document.getString("telefono").orEmpty()
                        val fechaNacimiento = document.getTimestamp("fechaNacimiento")?.toDate()?.time
                        val edadCalculada = calcularEdad(fechaNacimiento)

                        val perfilPasajeroMap = document.get("perfilPasajero") as? Map<*, *>
                        val pasajeroEnabled = (perfilPasajeroMap?.get("enabled") as? Boolean) ?: false
                        val pasajeroRatingAvg = (perfilPasajeroMap?.get("ratingAvg") as? Number)?.toDouble() ?: 0.0
                        val pasajeroRatingCount = (perfilPasajeroMap?.get("ratingCount") as? Number)?.toLong() ?: 0L

                        val perfilConductorMap = document.get("perfilConductor") as? Map<*, *>
                        val conductorEnabled = (perfilConductorMap?.get("enabled") as? Boolean) ?: false
                        val conductorRatingAvg = (perfilConductorMap?.get("ratingAvg") as? Number)?.toDouble() ?: 0.0
                        val conductorRatingCount = (perfilConductorMap?.get("ratingCount") as? Number)?.toLong() ?: 0L
                        val licenciaSubida = (perfilConductorMap?.get("licenciaSubida") as? Boolean) ?: false
                        val licenciaVerificada = (perfilConductorMap?.get("licenciaVerificada") as? Boolean) ?: false

                        val vehiculoActivoId = perfilConductorMap?.get("vehiculoActivoId") as? String

                        val rolStr = document.getString("rol")
                        val rol = try {
                            RolUsuario.valueOf(rolStr ?: RolUsuario.CUSTOMER.name)
                        } catch (_: Exception) {
                            RolUsuario.CUSTOMER
                        }

                        _uiState.value = PerfilUiState(
                            nombre = nombre,
                            apellidos = apellidos,
                            edad = if (edadCalculada <= 0) "" else edadCalculada.toString(),
                            email = email,
                            fotoPerfilUrl = fotoUrl,
                            telefono = telefono,
                            fechaNacimiento = fechaNacimiento,
                            isPasajeroSelected = pasajeroEnabled,
                            isConductorSelected = conductorEnabled,
                            pasajeroEnabled = if (pasajeroEnabled) Estado.ACTIVO else Estado.PENDIENTE,
                            pasajeroRatingAvg = pasajeroRatingAvg,
                            pasajeroRatingCount = pasajeroRatingCount,
                            conductorEnabled = if (conductorEnabled) Estado.ACTIVO else Estado.PENDIENTE,
                            conductorRatingAvg = conductorRatingAvg,
                            conductorRatingCount = conductorRatingCount,
                            licenciaSubida = licenciaSubida,
                            licenciaVerificada = licenciaVerificada,
                            showVehiculoEditor = false,
                            isSaveEnabled = false
                        )

                        val nuevo = document.getBoolean("nuevo") ?: false
                        _usuarioNuevo.value = nuevo
                        Log.e(TAG, "verás como es nulo 2 $_usuarioNuevo")

                        if (conductorEnabled) {
                            cargarVehiculosUsuario(usuActual)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parseando datos del usuario", e)
                    }
                } else {
                    Log.w(TAG, "No se encontró el documento del usuario con UID: $usuActual")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al obtener usuario actual", e)
            }
    }

    fun cargarFotoPerfil(usuarioActual: String, fotoPerfil: (String) -> Unit) {
        db.collection(Coleccion.Usuario)
            .document(usuarioActual)
            .get()
            .addOnSuccessListener { doc ->
                fotoPerfil(doc.getString("fotoUrl").toString())
            }
            .addOnFailureListener {
                Log.e(TAG, "Error al cargar la imagen del usuario")
            }
    }

    fun cambiarPassword(passwordActual: String, passwordNueva: String, passwordConfirmacion: String, onResult: (Boolean, String) -> Unit) {
        val user = auth.currentUser

        if (user == null) {
            onResult(false, "No se ha encontrado el usuario actual.")
        } else {
            val email = user.email
            val tienePassword = user.providerData.any { info ->
                info.providerId == EmailAuthProvider.PROVIDER_ID
            }

            if (email.isNullOrBlank()) {
                onResult(false, "No se ha podido obtener el email del usuario.")
            } else if (!tienePassword) {
                onResult(false, "Tu cuenta está vinculada a Google. La contraseña se gestiona desde tu cuenta de Google.")
            } else if (passwordActual.isBlank() || passwordNueva.isBlank() || passwordConfirmacion.isBlank()) {
                onResult(false, "Debes rellenar todos los campos.")
            } else if (passwordNueva != passwordConfirmacion) {
                onResult(false, "Las contraseñas nuevas no coinciden.")
            } else if (passwordNueva.length < 8) {
                onResult(false, "La nueva contraseña debe tener al menos 8 caracteres.")
            } else {
                val credential = EmailAuthProvider.getCredential(email, passwordActual)

                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        user.updatePassword(passwordNueva)
                            .addOnSuccessListener {
                                onResult(true, "Contraseña actualizada correctamente.")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error al actualizar la contraseña", e)
                                onResult(false, "Error al actualizar la contraseña.")
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error al reautenticar al usuario", e)
                        onResult(false, "La contraseña actual no es correcta.")
                    }
            }
        }
    }

    fun cargarVehiculosUsuario(uid: String) {
        if (uid.isBlank()) return

        db.collection(usuario)
            .document(uid)
            .collection(vehiculo)
            .get()
            .addOnSuccessListener { results ->
                val listaVehiculos = results.documents.mapNotNull { doc ->
                    try {
                        Vehiculo(
                            id = doc.id,
                            modelo = doc.getString("modelo").orEmpty(),
                            matricula = doc.getString("matricula").orEmpty(),
                            color = doc.getString("color").orEmpty(),
                            plazas = (doc.get("plazas") as? Long)?.toInt() ?: 4,
                            fotoUrl = doc.getString("fotoUrl"),
                            verificado = doc.getBoolean("verificado") ?: false,
                            activo = doc.getBoolean("activo") ?: false
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parseando vehículo ${doc.id}", e)
                        null
                    }
                }

                _vehiculos.clear()
                _vehiculos.addAll(listaVehiculos)

                _uiState.value = _uiState.value.copy(
                    vehiculosGuardados = listaVehiculos
                )
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error cargando vehículos", e)
            }
    }

    fun comprobarRolesServidor(uid: String, onResult: (esPasajeroSrv: Boolean, esConductorSrv: Boolean) -> Unit) {
        db.collection(usuario)
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val perfilPasajero = doc.get("perfilPasajero") as? Map<*, *>
                val perfilConductor = doc.get("perfilConductor") as? Map<*, *>

                val pasajeroEnabled = perfilPasajero?.get("enabled") as? Boolean ?: false
                val conductorEnabled = perfilConductor?.get("enabled") as? Boolean ?: false

                onResult(pasajeroEnabled, conductorEnabled)
            }
            .addOnFailureListener {
                onResult(false, false)
            }
    }

    fun onPasajeroToggle(checked: Boolean) {
        _uiState.update { curr ->
            curr.copy(
                isPasajeroSelected = checked,
                pasajeroEnabled = if (checked) Estado.ACTIVO else Estado.PENDIENTE,
                isSaveEnabled = true
            )
        }
    }

    fun onConductorToggle(checked: Boolean) {
        _uiState.update { curr ->
            val edadNum = calcularEdad(curr.fechaNacimiento)
            val permitido = if (checked && edadNum < 18) false else checked

            curr.copy(
                isConductorSelected = permitido,
                conductorEnabled = if (permitido) Estado.PENDIENTE else Estado.PENDIENTE,
                showEdadWarningConductor = checked && edadNum < 18,
                isSaveEnabled = true
            )
        }
    }

    fun onShowEditorVehiculo() {
        _uiState.update {
            it.copy(showVehiculoEditor = !it.showVehiculoEditor)
        }
    }

    fun onTelefonoChange(nuevoTelefono: String) {
        _uiState.update { estadoActual ->
            estadoActual.copy(
                telefono = nuevoTelefono,
                isSaveEnabled = true
            )
        }
    }

    fun onFechaNacimientoSeleccionada(fechaMillis: Long) {
        _uiState.update { curr ->
            val edadCalculada = calcularEdad(fechaMillis)

            curr.copy(
                fechaNacimiento = fechaMillis,
                edad = if (edadCalculada <= 0) "" else edadCalculada.toString(),
                showEdadWarningConductor = curr.isConductorSelected && edadCalculada < 18,
                isSaveEnabled = true
            )
        }
    }

    fun onModeloChange(modelo: String) {
        _uiState.update {
            it.copy(
                vehiculoModelo = modelo,
                isSaveEnableCar = true
            )
        }
    }

    fun onMatriculaChange(matricula: String) {
        _uiState.update {
            it.copy(
                vehiculoMatricula = matricula,
                isSaveEnableCar = true
            )
        }
    }

    fun onColorChange(color: String) {
        _uiState.update {
            it.copy(
                vehiculoColor = color,
                isSaveEnableCar = true
            )
        }
    }

    fun setNuevaFotoFile(file: File?) {
        nuevaFotoFile = file
    }

    fun getNuevaFotoFile(): File? {
        return nuevaFotoFile
    }

    fun onFotoPerfilSeleccionadaLocal(nuevaUri: String) {
        _uiState.value = _uiState.value.copy(
            fotoPerfilUrl = nuevaUri,
            isSaveEnabled = true
        )
    }

    fun calcularEdad(fechaNacimiento: Long?): Int {
        var edad = 0

        if (fechaNacimiento != null) {
            val nacimiento = Calendar.getInstance().apply {
                timeInMillis = fechaNacimiento
            }

            val hoy = Calendar.getInstance()

            edad = hoy.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR)

            val mesHoy = hoy.get(Calendar.MONTH)
            val diaHoy = hoy.get(Calendar.DAY_OF_MONTH)

            val mesNac = nacimiento.get(Calendar.MONTH)
            val diaNac = nacimiento.get(Calendar.DAY_OF_MONTH)

            if (mesHoy < mesNac || (mesHoy == mesNac && diaHoy < diaNac)) {
                edad--
            }
        } else {
            edad = 0
        }

        return edad
    }
}
