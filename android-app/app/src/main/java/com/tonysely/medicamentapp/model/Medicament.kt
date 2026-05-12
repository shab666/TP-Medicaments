package com.tonysely.medicamentapp.model

data class Medicament(
    val id: Int = 0,
    val nom: String = "",
    val dosage: String = "",
    val forme: String = "",
    val fabricant: String = "",
    val date_expiration: String = "",
    val image_url: String = "" // Ajout de l'URL de l'image pour le catalogue
)
