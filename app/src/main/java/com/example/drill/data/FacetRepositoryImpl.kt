package com.example.drill.data

import android.content.Context
import com.example.drill.domain.FacetRepository
import com.example.drill.model.FacetProfile
import com.example.drill.network.SecureHttp
import com.example.drill.storage.FacetDao
import com.example.drill.storage.FacetDatabase
import com.example.drill.storage.FacetEntity
import com.example.drill.storage.FacetFileStore
import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject

class FacetRepositoryImpl(
        context: Context,
        private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : FacetRepository {
    private val dao: FacetDao = FacetDatabase.get(context.applicationContext).facetDao()
    private val store = FacetFileStore(context.applicationContext)
    private val _facets = MutableStateFlow<List<FacetEntity>>(emptyList())
    override val facets: StateFlow<List<FacetEntity>> = _facets

    override suspend fun refresh() {
        val rows = withContext(ioDispatcher) { dao.getAll() }
        _facets.value = rows
    }

    override suspend fun getFacet(id: String): FacetEntity? {
        return withContext(ioDispatcher) { dao.getById(id) }
    }

    override suspend fun deleteFacet(id: String) {
        withContext(ioDispatcher) {
            dao.delete(id)
            store.deleteFacet(id)
        }
        refresh()
    }

    override suspend fun markOpened(id: String, atMillis: Long) {
        withContext(ioDispatcher) { dao.updateLastOpened(id, atMillis) }
        refresh()
    }

    override suspend fun importFacetFromUrl(url: String): FacetEntity {
        return withContext(ioDispatcher) {
            SecureHttp.requireHttps(url)
            val id = IdGenerator.idFromUrl(url)
            val facetDir = store.facetDir(id)

            try {
                val jsonString = SecureHttp.fetchUtf8Text(url = url, maxBytes = 256_000)
                val json = JSONObject(jsonString)
                val profile = FacetProfile.parse(json, id)
                SecureHttp.requireHttps(profile.glbUrl)

                val jsonFile = store.writeJson(id, profile.toProfileJsonString())
                val glbFile = store.glbFile(id)
                downloadGlb(profile.glbUrl, glbFile)

                val now = System.currentTimeMillis()
                val entity =
                        FacetEntity(
                                id = id,
                                type = profile.type,
                                name = profile.name,
                                version = profile.version,
                                localJsonPath = jsonFile.absolutePath,
                                localGlbPath = glbFile.absolutePath,
                                lastOpened = now
                        )
                dao.upsert(entity)
                entity
            } catch (e: Exception) {
                facetDir.deleteRecursively()
                throw e
            }
        }
                .also { refresh() }
    }

    private fun downloadGlb(glbUrl: String, destination: File) {
        SecureHttp.downloadToFile(
                url = glbUrl,
                destination = destination,
                maxBytes = 20L * 1024L * 1024L
        )
    }
}
