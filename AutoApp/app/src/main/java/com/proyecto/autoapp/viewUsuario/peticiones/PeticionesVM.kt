package com.proyecto.autoapp.viewUsuario.peticiones

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.proyecto.autoapp.general.Coleccion
import com.proyecto.autoapp.general.modelo.peticiones.Peticion
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PeticionesVM : ViewModel(){
    val TAG = "Jose"
    val auth = FirebaseAuth.getInstance()
    var peticion = Coleccion.PeticionViaje

    val isLoadingPeticion = mutableStateOf(false)
    val errorPeticion = mutableStateOf<String?>(null)

    private val _miPeticion = MutableStateFlow<Peticion?>(null)
    val miPeticion: StateFlow<Peticion?> = _miPeticion

    private val _posicionViajero = MutableStateFlow<LatLng?>(null)
    val posicionViajero: StateFlow<LatLng?> = _posicionViajero

    // Para escuchar las peticiones lanzadas
    var peticionesPendientes by mutableStateOf<List<Peticion>>(emptyList())
        private set

    private var listenerPeticiones: ListenerRegistration? = null
    private var listenerMiPeticion: ListenerRegistration? = null
    private var listenerTracking: ListenerRegistration? = null

    // Tiempo máximo petición
    private val TIEMPO_MAX_PETICION_MS = 30_000L

    /**
     *  Funciones para escuchar las peticiones por parte del conductor
     */
    fun observarMiPeticion(uidUsuario: String) {
        val db = FirebaseFirestore.getInstance()
        listenerMiPeticion?.remove()

        listenerMiPeticion = db.collection(peticion)
            .whereEqualTo("uidUsuario", uidUsuario)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    Log.e(TAG, "Error escuchando mi petición", e)
                    return@addSnapshotListener
                }

                if (snap != null && !snap.isEmpty) {
                    val peticiones = snap.documents
                        .mapNotNull { it.toObject(Peticion::class.java) }

                    val activas = peticiones.filter { pet ->
                        pet.estado != "cancelada" && pet.estado != "finalizada"
                    }

                    val peticionMasReciente = activas.maxByOrNull { it.timestamp }
                    _miPeticion.value = peticionMasReciente
                } else {
                    _miPeticion.value = null
                }
            }
    }

    fun observarPeticionesPendientesAceptadas(uidConductor: String) {
        val db = FirebaseFirestore.getInstance()
        listenerPeticiones?.remove()

        listenerPeticiones = db.collection(peticion)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error escuchando peticiones", e)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                val visibles = snapshot.documents
                    .mapNotNull { it.toObject(Peticion::class.java) }
                    .onEach { pet ->
                        Log.d(
                            TAG,
                            "id=${pet.id}, estado=${pet.estado}, can=${pet.uidConductorCan}, infoConductor=${pet.infoConductor}"
                        )
                    }
                    .filter { pet ->
                        // Controlo que no me salga mi propia petición
                        val esMia = pet.uidUsuario == uidConductor
                        val yoLaHeRechazado = pet.uidConductorCan.contains(uidConductor)

                        val visibleSegunEstado = when (pet.estado) {
                            "pendiente" -> true                                    // Cualquiera la puede ver
                            "ofertaConductor" -> pet.infoConductor?.uid == uidConductor
                            "aceptada" -> pet.infoConductor?.uid == uidConductor   // Solo el que la tiene aceptada
                            "enCurso" -> pet.infoConductor?.uid == uidConductor
                            else -> false                                          // Deja de mostarse
                        }

                        !esMia && !yoLaHeRechazado && visibleSegunEstado
                    }

                peticionesPendientes = visibles

            }
    }

    /**
     * Funciones para gestionar las peticiones
     * */
    fun enviarPeticion(uidUsuario: String,inicioTexto: String, inicioLatLng: LatLng?, inicioPlaceId: String?, destinoTexto: String, destinoLatLng: LatLng?,
                       destinoPlaceId: String?, onResult: (Boolean) -> Unit) {
        isLoadingPeticion.value = true
        errorPeticion.value = null

        val db = FirebaseFirestore.getInstance()
        val idPeticion = db.collection(peticion).document().id

        // Map para el punto de inicio
        val inicioMap = mapOf(
            "texto" to inicioTexto,
            "lat" to inicioLatLng?.latitude,
            "lng" to inicioLatLng?.longitude,
            "placeId" to inicioPlaceId
        )

        // Map para el punto de destino
        val destinoMap = mapOf(
            "texto" to destinoTexto,
            "lat" to destinoLatLng?.latitude,
            "lng" to destinoLatLng?.longitude,
            "placeId" to destinoPlaceId
        )

        val datosPeticion: Map<String, Any?> = mapOf(
            "id" to idPeticion,
            "uidUsuario" to uidUsuario,

            // conductores que han rechazado
            "uidConductorCan" to emptyList<String>(),

            // bloques inicio/destino
            "inicio" to inicioMap,
            "destino" to destinoMap,

            "estado" to "pendiente",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection(peticion)
            .document(idPeticion)
            .set(datosPeticion)
            .addOnSuccessListener {
                Log.e(TAG, "Petición enviada correctamente: $datosPeticion")
                isLoadingPeticion.value = false
                programarCancelacionPorTimeout(idPeticion)
                onResult(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "ERROR al enviar petición", e)
                errorPeticion.value = e.message ?: "Error desconocido al guardar petición"
                isLoadingPeticion.value = false
                onResult(false)
            }
    }

    fun rechazarPeticionConductor(pet: Peticion, uidConductor: String, onResult: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(peticion).document(pet.id)

        /**
         * Una vez el conductor rechaza se almacena en la lista de conductores que rechazaron llevar al viajero.
         * Automáticamente deja de aparecer el viaje en su vista
         * */
        docRef.update(
            mapOf(
                "estado" to "pendiente",
                "uidConductorCan" to FieldValue.arrayUnion(uidConductor)
            )
        )
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun aceptarPeticionConductor(pet: Peticion, uidConductor: String, nombreDelConductor: String, fotoConductor: String, onResult: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(peticion).document(pet.id)

        Log.e(TAG, "Foto de perfil == $fotoConductor")

        db.runTransaction { tx ->
            val snap = tx.get(docRef)
            val estadoActual = snap.getString("estado")
            val infoConductorActual = snap.get("infoConductor") as? Map<*, *>

            // Solo actualizamos si sigue pendiente y nadie la ha aceptado aún
            if (estadoActual == "pendiente" && infoConductorActual == null) {
                val infoConductorMap = mapOf(
                    "uid" to uidConductor,
                    "nombre" to nombreDelConductor,
                    "foto" to fotoConductor
                )

                tx.update(
                    docRef,
                    mapOf(
                        "estado" to "ofertaConductor",
                        "infoConductor" to infoConductorMap,
                        "timestampAceptacion" to System.currentTimeMillis()
                    )
                )
            }
        }
            .addOnSuccessListener {
                programarTimeoutRespuestaViajero(pet.id)
                onResult(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error aceptando petición", e)
                onResult(false)
            }
    }

    fun aceptarOfertaViajero(pet: Peticion, onResult: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection(peticion)
            .document(pet.id)
            .update(
                mapOf(
                    "estado" to "aceptada",
                    "timestampConfirmacionViajero" to System.currentTimeMillis()
                )
            )
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun rechazarOfertaViajero(pet: Peticion, onResult: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(peticion).document(pet.id)

        docRef.get()
            .addOnSuccessListener { snap ->
                val estadoActual = snap.getString("estado")
                val infoConductor = snap.get("infoConductor") as? Map<*, *>
                val uidConductor = infoConductor?.get("uid") as? String ?: ""

                if (estadoActual == "ofertaConductor" && uidConductor.isNotEmpty()) {
                    val trackingMap = mapOf(
                        "compartiendo" to false,
                        "lat" to null,
                        "lng" to null,
                        "ultimaActualizacion" to System.currentTimeMillis()
                    )

                    db.runTransaction { tx ->
                        tx.update(
                            docRef,
                            mapOf(
                                "estado" to "pendiente",
                                "infoConductor" to null,
                                "uidConductorCan" to FieldValue.arrayUnion(uidConductor),
                                "trackingViajero" to trackingMap
                            )
                        )
                    }
                        .addOnSuccessListener { onResult(true) }
                        .addOnFailureListener { onResult(false) }
                } else {
                    onResult(false)
                }
            }
            .addOnFailureListener { onResult(false) }
    }

    fun cancelarViajeViajero(pet: Peticion, onResult: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(peticion).document(pet.id)

        docRef.get()
            .addOnSuccessListener { snap ->
                val estadoActual = snap.getString("estado")

                if (estadoActual == "aceptada" || estadoActual == "ofertaConductor" || estadoActual == "pendiente") {

                    val trackingMap = mapOf(
                        "compartiendo" to false,
                        "lat" to null,
                        "lng" to null,
                        "ultimaActualizacion" to System.currentTimeMillis()
                    )

                    docRef.update(
                        mapOf(
                            "estado" to "cancelada",
                            "trackingViajero" to trackingMap
                        )
                    )
                        .addOnSuccessListener { onResult(true) }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error al cancelar viaje", e)
                            onResult(false)
                        }
                } else {
                    onResult(false)
                }
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun marcarViajeEnCursoViajero(pet: Peticion, onResult: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(peticion).document(pet.id)

        // No tocamos el tracking salvo para actualizar la marca temporal
        val trackingMap = mapOf(
            "compartiendo" to true,
            "lat" to pet.trackingViajero?.lat,
            "lng" to pet.trackingViajero?.lng,
            "ultimaActualizacion" to System.currentTimeMillis()
        )

        docRef.update(
            mapOf(
                "estado" to "enCurso",
                "trackingViajero" to trackingMap
            )
        )
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al pasar viaje a enCurso", e)
                onResult(false)
            }
    }

    fun finalizarViajeViajero(pet: Peticion, onResult: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(peticion).document(pet.id)

        // Marcamos que el viajero ya no comparte ubicación
        val trackingMap = mapOf(
            "compartiendo" to false,
            "lat" to null,
            "lng" to null,
            "ultimaActualizacion" to System.currentTimeMillis()
        )

        docRef.update(
            mapOf(
                "estado" to "finalizada",
                "trackingViajero" to trackingMap
            )
        )
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al finalizar viaje", e)
                onResult(false)
            }
    }

    fun observarTrackingPeticion(idPeticion: String) {
        val db = FirebaseFirestore.getInstance()
        listenerTracking?.remove()

        listenerTracking = db.collection(peticion)
            .document(idPeticion)
            .addSnapshotListener { snap, e ->
                if (e != null || snap == null || !snap.exists()) {
                    Log.e(TAG, "Error escuchando tracking de la petición", e)
                    _posicionViajero.value = null
                    return@addSnapshotListener
                }

                val pet = snap.toObject(Peticion::class.java)
                val tracking = pet?.trackingViajero

                if (pet?.estado == "aceptada" && tracking?.compartiendo == true &&
                    tracking.lat != null && tracking.lng != null
                ) {
                    _posicionViajero.value = LatLng(tracking.lat, tracking.lng)
                } else {
                    _posicionViajero.value = null
                }
            }
    }

    fun onLocationChangedViajero(latLng: LatLng) {
        // Si tengo una petición mía y está aceptada, envío mi posición
        val pet = _miPeticion.value
        if (pet != null && (pet.estado == "aceptada" || pet.estado == "enCurso")) {
            actualizarUbicacionViajero(pet, latLng)
        }
    }

    /**
     * Métodos que gestionan el tiempo de cancelación por inactividad.
     * */
    private fun programarCancelacionPorTimeout(idPeticion: String) {
        val db = FirebaseFirestore.getInstance()

        viewModelScope.launch {
            delay(TIEMPO_MAX_PETICION_MS)

            db.collection(peticion)
                .document(idPeticion)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val estadoActual = doc.getString("estado")
                        if (estadoActual == "pendiente") {
                            db.collection(peticion)
                                .document(idPeticion)
                                .update("estado", "cancelada")
                        }
                    }
                }
        }
    }

    private fun programarTimeoutRespuestaViajero(idPeticion: String) {
        val db = FirebaseFirestore.getInstance()

        viewModelScope.launch {
            delay(TIEMPO_MAX_PETICION_MS)
            db.collection(peticion)
                .document(idPeticion)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val estadoActual = doc.getString("estado")
                        if (estadoActual == "pendiente" || estadoActual == "ofertaConductor") {
                            db.collection(peticion)
                                .document(idPeticion)
                                .update("estado", "cancelada")
                        }
                    }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerPeticiones?.remove()
        listenerMiPeticion?.remove()
        listenerTracking?.remove()
    }

    /**
     * Métodos para la ubicación en tiempo real
     * */
    fun actualizarUbicacionViajero(pet: Peticion, posicion: LatLng) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(peticion).document(pet.id)

        val trackingMap = mapOf(
            "compartiendo" to true,
            "lat" to posicion.latitude,
            "lng" to posicion.longitude,
            "ultimaActualizacion" to System.currentTimeMillis()
        )

        docRef.update(
            mapOf(
                "trackingViajero" to trackingMap
            )
        ).addOnFailureListener { e ->
            Log.e(TAG, "Error actualizando tracking del viajero", e)
        }
    }

    fun detenerTracking() {
        listenerTracking?.remove()
        listenerTracking = null
        _posicionViajero.value = null
    }

}