
package br.unasp.savepoint.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.unasp.savepoint.data.preferences.UserPreferencesDataStore
import br.unasp.savepoint.data.repository.AuthRepository
import br.unasp.savepoint.data.repository.GameRepository

class ProfileViewModelFactory(
    private val userPreferences: UserPreferencesDataStore,
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(
                userPreferences,
                authRepository,
                gameRepository
            ) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconhecido: ${modelClass.name}"
        )
    }
}
