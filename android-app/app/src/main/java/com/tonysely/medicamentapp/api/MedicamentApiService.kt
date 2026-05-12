package com.tonysely.medicamentapp.api

import com.tonysely.medicamentapp.model.Medicament
import retrofit2.http.GET

interface MedicamentApiService {
    @GET("medicaments") // À adapter selon votre URL réelle
    suspend fun getMedicaments(): List<Medicament>
}
