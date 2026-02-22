package com.example.drill.storage

import android.content.Context
import java.io.File

class FacetFileStore(private val context: Context) {

    fun facetDir(id: String): File {
        val dir = File(context.filesDir, "facet/$id")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun jsonFile(id: String): File = File(facetDir(id), "profile.json")

    fun glbFile(id: String): File = File(facetDir(id), "model.glb")

    fun writeJson(id: String, json: String): File {
        val f = jsonFile(id)
        f.writeText(json, Charsets.UTF_8)
        return f
    }

    fun deleteFacet(id: String): Boolean {
        val dir = File(context.filesDir, "facet/$id")
        return dir.deleteRecursively()
    }
}

