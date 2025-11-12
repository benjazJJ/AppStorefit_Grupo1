package com.example.appstorefit_grupo1.ui.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appstorefit_grupo1.data.remote.dto.PostDto
import com.example.appstorefit_grupo1.data.repository.PostsRepository
import kotlinx.coroutines.launch

data class PostsUiState(
    val isloading: Boolean = false,
    val posts: List<PostDto> = emptyList(),
    val error: String? = null
)

class PostsViewModel(
    private val repository: PostsRepository = PostsRepository()
): ViewModel(){

    var uiState by mutableStateOf(PostsUiState())
    private set

    fun loadPost(){
        uiState = uiState.copy(isloading = true, error = null)
        viewModelScope.launch {
            val result = repository.fetchPost()
            uiState = result.fold(
                onSuccess = {data -> uiState.copy(isloading = false, posts = data)},
                onFailure = {e -> uiState.copy(isloading = false, error = e.message ?:"Error desconocido")}
            )
        }

    }
}