package com.tonysely.medicamentapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonysely.medicamentapp.api.RetrofitInstance
import com.tonysely.medicamentapp.model.Medicament
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MedicamentUiState {
    object Loading : MedicamentUiState()
    data class Success(val medicaments: List<Medicament>) : MedicamentUiState()
    data class Error(val message: String) : MedicamentUiState()
}

class MedicamentViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MedicamentUiState>(MedicamentUiState.Loading)
    val uiState: StateFlow<MedicamentUiState> = _uiState

    private val api = RetrofitInstance.api

    init {
        charger()
    }

    fun charger() {
        viewModelScope.launch {
            _uiState.value = MedicamentUiState.Loading
            try {
                val result = api.getMedicaments()
                _uiState.value = MedicamentUiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = MedicamentUiState.Error("Erreur de connexion au serveur Laravel. Vérifiez 'php artisan serve'.")
            }
        }
    }

    fun ajouter(medicament: Medicament) {
        viewModelScope.launch {
            try {
                api.createMedicament(medicament)
                charger()
            } catch (e: Exception) {
                _uiState.value = MedicamentUiState.Error("Impossible d'ajouter le médicament.")
            }
        }
    }

    fun supprimer(id: Int) {
        viewModelScope.launch {
            try {
                api.deleteMedicament(id)
                charger()
            } catch (e: Exception) {
                _uiState.value = MedicamentUiState.Error("Erreur lors de la suppression.")
            }
        }
    }
}
