package br.unasp.savepoint.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.unasp.savepoint.data.repository.AuthRepository
import br.unasp.savepoint.data.repository.GameRepository

class GameViewModelFactory(
    private val repository: GameRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(
                repository,
                authRepository
            ) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconhecido"
        )
    }
}