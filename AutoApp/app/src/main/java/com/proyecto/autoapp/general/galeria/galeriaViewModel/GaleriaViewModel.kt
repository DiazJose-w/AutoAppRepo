package com.proyecto.autoapp.general.galeria.galeriaViewModel

import android.R
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import com.proyecto.autoapp.general.Coleccion
import com.proyecto.autoapp.general.DirectorioStorage
import java.io.File
import java.net.URL

class GaleriaViewModel {
    val TAG = "Jose"
    val usuario = Coleccion.Usuario
    val storage = Firebase.storage
    var storageRef = storage.reference

    private val _urlPfp = MutableLiveData<URL?>()
    val urlPfp: LiveData<URL?> = _urlPfp

    private val _imageUri = MutableLiveData<Uri>(Uri.EMPTY)
    val imageUri: LiveData<Uri> get() = _imageUri

    private val _imageFile = MutableLiveData<File?>()
    val imageFile: LiveData<File?> get() = _imageFile

    private val _fileList = MutableLiveData<List<String>>(emptyList())
    val fileList: LiveData<List<String>> get() = _fileList

    private val _isUploading = MutableLiveData<Boolean>(false)
    val isUploading: LiveData<Boolean> get() = _isUploading

    private val _uploadSuccess = MutableLiveData<Boolean>()
    val uploadSuccess: LiveData<Boolean> get() = _uploadSuccess

    private val _fotosPerfil = MutableLiveData<List<String>>(emptyList())
    val fotosPerfil: LiveData<List<String>> = _fotosPerfil

    // Detecta si hay cambios pendientes para avisar cuando el usuario vaya a salir sin guardar
    private val _hayCambiosPendientes = MutableLiveData(false)
    val hayCambiosPendientes: LiveData<Boolean> get() = _hayCambiosPendientes

    /**
     * Métodos que trabajan las variables de la clase externa
     * */
    fun updateImageUri(uri: Uri) {
        _imageUri.value = uri
    }

    fun setImageFile(file: File) {
        _imageFile.value = file
    }

    fun setUrlPfp(url: URL) {
        this._urlPfp.value = url
    }

    fun setFotosPerfil(list: List<String>) {
        _fotosPerfil.value = list
    }
    /** ====================================================== */


    fun uploadImage(context: Context, onComplete: (Boolean) -> Unit) {
        val file = _imageFile.value ?: return onComplete(false)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return onComplete(false)

        val ref = storageRef.child("${DirectorioStorage.FotoPerfil}/$uid/${file.name}")
        val fileUri = Uri.fromFile(file)

        _isUploading.value = true

        ref.putFile(fileUri)
            .continueWithTask { ref.downloadUrl }
            .addOnSuccessListener { uri ->
                FirebaseFirestore.getInstance()
                    .collection(Coleccion.Usuario)
                    .document(uid)
                    .update("fotoUrl", uri.toString())
                    .addOnSuccessListener {
                        _isUploading.value = false
                        _uploadSuccess.value = true
                        _hayCambiosPendientes.value = false
                        _imageUri.value = uri
                        loadFileList()
                        onComplete(true)
                    }
                    .addOnFailureListener {
                        _isUploading.value = false
                        _uploadSuccess.value = false
                        onComplete(false)
                    }
            }
            .addOnFailureListener {
                _isUploading.value = false
                _uploadSuccess.value = false
                onComplete(false)
            }
    }

    //Carga la lista de archivos desde Firebase Storage.
    fun loadFileList() {
        storageRef.child("images").listAll()
            .addOnSuccessListener { result ->
                val urls = mutableListOf<String>()
                val tasks = result.items.map { ref ->
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        urls.add(uri.toString())
                        if (urls.size == result.items.size) {
                            _fileList.value = urls
                        }
                    }
                }
                tasks.forEach { it }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error al cargar la lista: ${exception.message}")
            }
    }

    fun deleteFile(fileUrl: String, context: Context) {
        val ref = storage.getReferenceFromUrl(fileUrl)
        ref.delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Archivo eliminado correctamente", Toast.LENGTH_SHORT).show()
                loadFileList() //Recargamos la lista de archivos tras eliminar uno.
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error al eliminar archivo: ${exception.message}")
                Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    //Actualiza la URI de la imagen seleccionada
    fun selectImage(uri: String) {
        _imageUri.value = Uri.parse(uri)
        _hayCambiosPendientes.value = true
    }

    fun marcarCambiosPendientes() {
        _hayCambiosPendientes.value = true
    }

    fun descartarCambios() {
        _imageUri.value = Uri.EMPTY
        _imageFile.value = null
        _hayCambiosPendientes.value = false
    }
}