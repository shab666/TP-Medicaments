package com.tonysely.medicamentapp.api

import com.tonysely.medicamentapp.model.Medicament
import retrofit2.http.*

interface ApiService {

    @GET("medicaments")
    suspend fun getMedicaments(): List<Medicament>

    @POST("medicaments")
    suspend fun createMedicament(
        @Body medicament: Medicament
    ): Medicament

    @DELETE("medicaments/{id}")
    suspend fun deleteMedicament(
        @Path("id") id: Int
    )
}
