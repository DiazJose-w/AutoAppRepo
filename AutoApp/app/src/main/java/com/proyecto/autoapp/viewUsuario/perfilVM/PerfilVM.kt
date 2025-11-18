package com.proyecto.autoapp.viewUsuario.perfilVM

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import PerfilUiState
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.proyecto.autoapp.general.Coleccion
import com.proyecto.autoapp.general.DirectorioStorage
import com.proyecto.autoapp.general.modelo.dataClass.Vehiculo
import com.proyecto.autoapp.general.modelo.enumClass.Estado
import com.proyecto.autoapp.general.modelo.enumClass.RolUsuario
import com.proyecto.autoapp.general.modelo.usuarios.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.io.File

class PerfilVM {
    var TAG = "jose"
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


    // =============================================================
    // GUARDAR MODIFICACIONES EN EL PERFIL
    // =============================================================
    fun modPerfilUsuario(usuarioActual: String, success: (Boolean) -> Unit) {
        val state = _uiState.value

        // Si la edad está vacía, no seguimos
        if (state.edad.isBlank()) {
            success(false)
        } else {
            val edadInt = state.edad.toIntOrNull() ?: 0

            // Campos que SIEMPRE vamos a actualizar
            val baseUpdates = mutableMapOf<String, Any>(
                "perfilPasajero.enabled"  to state.isPasajeroSelected,
                "perfilConductor.enabled" to state.isConductorSelected,
                "edad" to edadInt
            )

            val file = nuevaFotoFile
            if (file == null) {
                db.collection(usuario)
                    .document(usuarioActual)
                    .update(baseUpdates as Map<String, Any>)
                    .addOnSuccessListener {
                        _uiState.value = state.copy(
                            pasajeroEnabled  = if (state.isPasajeroSelected) Estado.ACTIVO else Estado.PENDIENTE,
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
                                    pasajeroEnabled  = if (state.isPasajeroSelected) Estado.ACTIVO else Estado.PENDIENTE,
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


    // =============================================================
    // CARGAR LOS DATOS DEL USUARIO
    // =============================================================
    fun cargarUsuario(usuActual: String) {
        Log.e("jose", "verás como es nulo $usuActual")
        db.collection(usuario)
            .document(usuActual)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    try {
                        //DATOS DEL USUARIO
                        val nombre = document.getString("nombre").orEmpty()
                        val apellidos = document.getString("apellidos").orEmpty()
                        val edad = (document.get("edad") as? Long)?.toInt() ?: 0
                        val email = document.getString("email").orEmpty()
                        val fotoUrl = document.getString("fotoUrl")

                        // PERFIL PASAJERO
                        val perfilPasajeroMap = document.get("perfilPasajero") as? Map<*, *>
                        val pasajeroEnabled = (perfilPasajeroMap?.get("enabled") as? Boolean) ?: false
                        val pasajeroRatingAvg = (perfilPasajeroMap?.get("ratingAvg") as? Number)?.toDouble() ?: 0.0
                        val pasajeroRatingCount = (perfilPasajeroMap?.get("ratingCount") as? Number)?.toLong() ?: 0L

                        // PERFIL CONDUCTOR
                        val perfilConductorMap = document.get("perfilConductor") as? Map<*, *>
                        val conductorEnabled = (perfilConductorMap?.get("enabled") as? Boolean) ?: false
                        val conductorRatingAvg = (perfilConductorMap?.get("ratingAvg") as? Number)?.toDouble() ?: 0.0
                        val conductorRatingCount = (perfilConductorMap?.get("ratingCount") as? Number)?.toLong() ?: 0L
                        val licenciaSubida = (perfilConductorMap?.get("licenciaSubida") as? Boolean) ?: false
                        val licenciaVerificada = (perfilConductorMap?.get("licenciaVerificada") as? Boolean) ?: false

                        // Vehículo activo (por ahora null si no tiene)
                        val vehiculoActivoId = perfilConductorMap?.get("vehiculoActivoId") as? String

                        // ROL
                        val rolStr = document.getString("rol")
                        val rol = try {
                            RolUsuario.valueOf(rolStr ?: RolUsuario.CUSTOMER.name)
                        } catch (_: Exception) {
                            RolUsuario.CUSTOMER
                        }

                        // MAPEAMOS AL ESTADO DEL PERFIL
                        _uiState.value = PerfilUiState(
                            nombre = nombre,
                            apellidos = apellidos,
                            edad = if (edad == 0) "" else edad.toString(),
                            email = email,
                            fotoPerfilUrl = fotoUrl,
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

    fun cargarFotoPerfil(usuarioActual: String, fotoPerfil: (String) -> Unit){
        FirebaseFirestore.getInstance()
            .collection(Coleccion.Usuario)
            .document(usuarioActual)
            .get()
            .addOnSuccessListener { doc ->
                fotoPerfil(doc.getString("fotoUrl").toString())
            }
            .addOnFailureListener {
                Log.e(TAG, "Error al cargar la imagen del usuario")
            }
    }

    /**
     * MODIFICAR ESTE CÓDIGO.
     */
    fun cargarVehiculosUsuario(uid: String? = auth.currentUser?.uid) {
        if (uid == null) return

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

    // =============================================================
    // MÉTODOS QUE TRABAJAN LAS VARIABLES DE LA VISTA. MODIFICACIÓN INSTANTÁNEA
    // =============================================================
    // Métodos para los switch
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
            val edadNum = curr.edad.toIntOrNull() ?: 0
            val permitido = if (checked && edadNum < 18) false else checked

            curr.copy(
                isConductorSelected = permitido,
                conductorEnabled = if (permitido) Estado.PENDIENTE else Estado.PENDIENTE,
                showEdadWarningConductor = checked && edadNum < 18,
                isSaveEnabled = true
            )
        }
    }

    // Métodos para poder modificar los datos del usuario
    fun onEdadChange(edad: String) {
        _uiState.update {
            // Convertimos a número si es posible
            val edadNum = edad.toIntOrNull() ?: 0

            it.copy(
                edad = edad,
                showEdadWarningConductor = it.isConductorSelected && edadNum < 18,
                isSaveEnabled = true
            )
        }
    }

    fun onShowEditorVehiculo(){
        _uiState.update {
            it.copy(showVehiculoEditor = !it.showVehiculoEditor)
        }
    }

    // Métodos para poder modificar los datos del vehículo
    fun onModeloChange(modelo: String){
        _uiState.update {
            it.copy(
                vehiculoModelo = modelo,
                isSaveEnableCar = true
            )
        }
    }

    fun onMatriculaChange(matricula: String){
        _uiState.update {
            it.copy(
                vehiculoMatricula = matricula,
                isSaveEnableCar = true
            )
        }
    }

    fun onColorChange(color: String){
        _uiState.update {
            it.copy(
                vehiculoColor = color,
                isSaveEnableCar = true
            )
        }
    }

    // Métodos para poder modificar la foto de perfil

    fun setNuevaFotoFile(file: File?) {
        nuevaFotoFile = file
    }

    fun getNuevaFotoFile(): File? {
        return nuevaFotoFile
    }

    fun onFotoPerfilSeleccionadaLocal(nuevaUri: String) {
        _uiState.value = _uiState.value.copy(
            fotoPerfilUrl = nuevaUri,
            isSaveEnabled = true   // activamos el botón "Guardar cambios"
        )
    }
}