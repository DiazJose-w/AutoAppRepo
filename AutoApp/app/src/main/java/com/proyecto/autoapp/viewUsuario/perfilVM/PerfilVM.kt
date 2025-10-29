package com.proyecto.autoapp.viewUsuario.perfilVM

import PerfilUiState
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.proyecto.autoapp.general.Coleccion
import com.proyecto.autoapp.general.modelo.dataClass.Vehiculo
import com.proyecto.autoapp.general.modelo.enumClass.Estado
import com.proyecto.autoapp.general.modelo.enumClass.RolUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class PerfilVM {
    var TAG = "jose"
    var usuario = Coleccion.Usuario
    var vehiculo = Coleccion.Vehiculo

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val usuarioActual = auth.currentUser

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState

    private val _vehiculos = mutableStateListOf<Vehiculo>()
    val vehiculos: List<Vehiculo> get() = _vehiculos


    // =============================================================
    // CARGAR LOS DATOS DEL USUARIO
    // =============================================================
    fun cargarUsuario() {
        val uid = usuarioActual?.uid ?: return

        db.collection(usuario)
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    try {
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
                            vehiculoDescripcion = vehiculoActivoId ?: "",
                            showVehiculoEditor = false,
                            isSaveEnabled = false
                        )
                        if (conductorEnabled) {
                            cargarVehiculosUsuario(uid)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parseando datos del usuario", e)
                    }
                } else {
                    Log.w(TAG, "No se encontró el documento del usuario con UID: $uid")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al obtener usuario actual", e)
            }
    }

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
                            anio = (doc.get("anio") as? Long)?.toInt() ?: 0,
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

    // Métodos para poder modificar los datos del usuario y del vehículo
    fun onEdadChange(nuevaEdad: String) {
        _uiState.update { curr ->
            // Convertimos a número si es posible
            val edadNum = nuevaEdad.toIntOrNull() ?: 0

            curr.copy(
                edad = nuevaEdad,
                // Si el usuario tiene el modo conductor activado y es menor de 18 → mostramos advertencia
                showEdadWarningConductor = curr.isConductorSelected && edadNum < 18,
                // El botón de "Guardar cambios" se habilita porque hubo modificación
                isSaveEnabled = true
            )
        }
    }

}