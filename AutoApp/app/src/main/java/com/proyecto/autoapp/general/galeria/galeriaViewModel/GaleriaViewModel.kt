package com.proyecto.autoapp.general.galeria.galeriaViewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File
import java.net.URL

class GaleriaViewModel {
    val TAG = "Jose"

    private val _urlPfp = MutableLiveData<URL?>()
    val urlPfp: LiveData<URL?> = _urlPfp

    private val _imageUri = MutableLiveData<Uri>(Uri.EMPTY)
    val imageUri: LiveData<Uri> get() = _imageUri

    private val _imageFile = MutableLiveData<File?>()
    val imageFile: LiveData<File?> get() = _imageFile


    fun updateImageUri(uri: Uri) {
        _imageUri.value = uri
    }

    fun setImageFile(file: File) {
        _imageFile.value = file
    }

    fun setUrlPfp(url: URL) {
        this._urlPfp.value = url
    }
}