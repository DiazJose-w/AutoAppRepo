import com.proyecto.autoapp.general.modelo.dataClass.Vehiculo
import com.proyecto.autoapp.general.modelo.enumClass.Estado

/**
 * Clase externa para tratar los valores de los campos en la view perfil.
 * */
data class PerfilUiState(
    // Datos básicos del usuario
    val nombre: String = "",
    val apellidos: String = "",
    val edad: String = "",
    val email: String = "",

    // Foto perfil
    val fotoPerfilUrl: String? = null,
    val tieneMasFotos: Boolean = false,

    // Selección de roles en la UI
    val isPasajeroSelected: Boolean = false,
    val isConductorSelected: Boolean = false,

    // Info pasajero
    val pasajeroEnabled: Estado = Estado.PENDIENTE,
    val pasajeroRatingAvg: Double = 0.0,
    val pasajeroRatingCount: Long = 0,

    // Info conductor
    val conductorEnabled: Estado = Estado.PENDIENTE,
    val conductorRatingAvg: Double = 0.0,
    val conductorRatingCount: Long = 0,

    // Licencia / verificación conductor
    val licenciaSubida: Boolean = false,
    val licenciaVerificada: Boolean = false,

    // Vehículo (editor temporal)
    val vehiculoFotoUrl: String? = null,
    val vehiculoDescripcion: String = "",
    val vehiculoModelo: String = "",
    val vehiculoMatricula: String = "",
    val vehiculoColor: String = "",

    // Lista de vehículos ya guardados
    val vehiculosGuardados: List<Vehiculo> = emptyList(),

    // Control de UI
    val isSaveEnabled: Boolean = true,
    val isSaveEnableCar: Boolean = true,
    val showEdadWarningConductor: Boolean = false,
    val showVehiculoEditor: Boolean = false
)
