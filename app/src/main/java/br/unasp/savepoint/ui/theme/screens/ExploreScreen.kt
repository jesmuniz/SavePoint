
package br.unasp.savepoint.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.unasp.savepoint.data.model.GameEntity
import br.unasp.savepoint.data.remote.RemoteGame
import br.unasp.savepoint.viewmodel.GameViewModel
import coil3.compose.AsyncImage
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

import br.unasp.savepoint.data.model.GamePlatformEntity


private val BackgroundDark = Color(0xFF10101C)
private val CardDark = Color(0xFF1B1938)
private val Purple = Color(0xFF3020B5)
private val Mint = Color(0xFF00E6A0)
private val MutedText = Color(0xFFB8B6CC)
private val Gold = Color(0xFFFFC107)

@Composable
fun ExploreScreen(
    viewModel: GameViewModel,
    onBackActionChanged: ((() -> Unit)?) -> Unit
) {
    var searchText by remember { mutableStateOf("") }

    var isDebouncing by remember {
        mutableStateOf(false)
    }

    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchError by viewModel.searchError.collectAsState()

    var selectedCategory by remember {
        mutableStateOf("Todos")
    }

    val featuredGames by viewModel.featuredGames.collectAsState()
    val isLoadingFeatured by viewModel.isLoadingFeatured.collectAsState()
    val featuredError by viewModel.featuredError.collectAsState()

    val gameDetails by viewModel.gameDetails.collectAsState()
    val games by viewModel.games.collectAsState()
    val gamePlatforms by viewModel.gamePlatforms.collectAsState()

    val isLoadingGameDetails by viewModel.isLoadingGameDetails.collectAsState()

    // CAPAS ALTERNATIVAS DO STEAMGRIDDB
    val alternativeCover by viewModel.alternativeCover.collectAsState()

    val isLoadingAlternativeCover by
    viewModel.isLoadingAlternativeCover.collectAsState()

    var selectedGame by remember {
        mutableStateOf<RemoteGame?>(null)
    }

    // PESQUISA AUTOMÁTICA COM DEBOUNCE
    LaunchedEffect(searchText) {
        if (searchText.isBlank()) {
            isDebouncing = false
            viewModel.clearSearchResults()
            return@LaunchedEffect
        }

        isDebouncing = true
        delay(500)
        isDebouncing = false
        viewModel.searchGames(searchText.trim())
    }

    val categories = listOf(
        "Todos",
        "Ação",
        "Aventura",
        "RPG",
        "Estratégia",
        "Indie"
    )

    // CARREGA OS JOGOS EM DESTAQUE AO ABRIR A TELA
    LaunchedEffect(Unit) {
        if (featuredGames.isEmpty()) {
            viewModel.loadFeaturedGames()
        }
    }

    // CONTROLE DA NAVEGAÇÃO DE RETORNO
    LaunchedEffect(selectedGame) {
        onBackActionChanged(
            if (selectedGame != null) {
                { selectedGame = null }
            } else {
                null
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            onBackActionChanged(null)
        }
    }

    // TELA DE DETALHES
    if (selectedGame != null) {

        val currentGame = if (
            gameDetails?.externalId == selectedGame!!.externalId
        ) {
            gameDetails!!.copy(
                coverUrl = selectedGame!!.coverUrl
            )
        } else {
            selectedGame!!
        }

        val currentApiId = currentGame.externalId.toIntOrNull()

        val isAlreadyInLibrary =
            games.any { it.apiId == currentApiId && currentApiId != null } ||
                    gamePlatforms.any {
                        it.platform.equals("Steam", ignoreCase = true) &&
                                it.externalId == currentGame.externalId
                    }

        // CARREGA OS DETALHES DA STEAM
        LaunchedEffect(selectedGame!!.externalId) {
            viewModel.loadGameDetails(selectedGame!!.externalId)
        }

        GameDetailsScreen(
            game = currentGame,
            isLoading = isLoadingGameDetails,
            alternativeCover = alternativeCover,
            isLoadingAlternativeCover = isLoadingAlternativeCover,
            onBack = {
                selectedGame = null
            },
            onAddToLibrary = {
                if (!isAlreadyInLibrary) {
                    viewModel.addGame(
                        GameEntity(
                            apiId = currentGame.externalId.toIntOrNull(),
                            title = currentGame.title,
                            coverUrl = currentGame.coverUrl,
                            customCoverUrl = alternativeCover,
                            genre = currentGame.genre,
                            platform = currentGame.platform,
                            description = currentGame.description,
                            rating = currentGame.rating
                        )

                    )

                    selectedGame = null
                }
            },
            isAlreadyInLibrary = isAlreadyInLibrary
        )

        return
    }

    // PESQUISA E CATEGORIAS
    val displayedGames = if (searchText.isNotBlank()) {
        searchResults.distinctBy { it.externalId }
    } else {
        featuredGames.distinctBy { it.externalId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 20.dp)
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        // CABEÇALHO
        Text(
            text = "DESCUBRA SEU PRÓXIMO JOGO",
            color = Mint,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Explorar",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // CAMPO DE PESQUISA
        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                selectedCategory = "Todos"
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "Buscar jogos...",
                    color = MutedText,
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Pesquisar",
                    tint = MutedText
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    // A pesquisa é executada automaticamente pelo LaunchedEffect.
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Mint,
                unfocusedBorderColor = Color(0xFF38364D),
                cursorColor = Mint,
                focusedContainerColor = CardDark,
                unfocusedContainerColor = CardDark
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // CATEGORIAS
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories.size) { index ->
                val category = categories[index]

                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        selectedCategory = category

                        if (category == "Todos") {
                            searchText = ""
                            viewModel.clearSearchResults()
                            viewModel.loadFeaturedGames()
                        } else {
                            searchText = category
                        }
                    },
                    label = {
                        Text(
                            category,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Mint,
                        selectedLabelColor = BackgroundDark,
                        containerColor = CardDark,
                        labelColor = MutedText
                    ),
                    border = null
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // TÍTULO DA SEÇÃO
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = if (
                        searchResults.isNotEmpty() ||
                        searchText.isNotBlank()
                    ) {
                        "Resultados da busca"
                    } else {
                        "Populares agora"
                    },
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = when {
                        isDebouncing || isSearching -> "Pesquisando na Steam..."
                        searchText.isNotBlank() -> "${searchResults.size} jogos encontrados"
                        else -> "Mais vendidos na Steam"
                    },
                    color = MutedText,
                    fontSize = 11.sp
                )
            }

            Text(
                text = "STEAM",
                color = Mint,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // CONTEÚDO
        when {
            isDebouncing || isSearching || (searchText.isBlank() && isLoadingFeatured) -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Mint)

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            if (searchText.isNotBlank()) "Pesquisando na Steam..." else "Carregando jogos...",
                            color = MutedText,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            searchError != null && searchText.isNotBlank() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        searchError ?: "",
                        color = Color(0xFFFF7676)
                    )
                }
            }

            featuredError != null &&
                    featuredGames.isEmpty() &&
                    searchText.isBlank() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            featuredError ?: "",
                            color = MutedText,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.loadFeaturedGames()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Purple
                            )
                        ) {
                            Text("Tentar novamente")
                        }
                    }
                }
            }

            displayedGames.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (searchText.isNotBlank()) {
                            "Nenhum jogo encontrado."
                        } else {
                            "Nenhum jogo disponível no momento."
                        },
                        color = MutedText,
                        fontSize = 13.sp
                    )
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    itemsIndexed(
                        items = displayedGames,
                        key = { _, game -> game.externalId }
                    ) { index, game ->

                        FeaturedGameCard(
                            game = game,
                            rank = if (searchText.isBlank()) index + 1 else null,
                            onClick = {
                                viewModel.loadAlternativeCover(game.title)
                                selectedGame = game
                            }
                        )
                    }
                }
            }
        }
    }
}




@Composable
private fun FeaturedGameCard(
    game: RemoteGame,
    rank: Int? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    val isPressed by interactionSource.collectIsPressedAsState()

    val imageScale by animateFloatAsState(
        targetValue = if (isPressed) 1.05f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "coverZoom"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {

        // CAPA DO JOGO
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.80f)
                .clip(RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = game.coverUrl,
                contentDescription = "Capa de ${game.title}",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = imageScale
                        scaleY = imageScale
                    },
                contentScale = ContentScale.Crop
            )

            // NUMERAÇÃO DO RANKING
            if (rank != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    color = Color(0xDD10101C),
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Text(
                        text = "#$rank",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            horizontal = 11.dp,
                            vertical = 7.dp
                        )
                    )
                }
            }
        }

        // INFORMAÇÕES DO JOGO (SEM FUNDO SEPARADO)
        Column(
            modifier = Modifier.padding(
                start = 5.dp,
                end = 5.dp,
                top = 5.dp,
                bottom = 3.dp
            )
        ) {
            Text(
                text = game.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = game.genre
                    ?.takeIf { it.isNotBlank() }
                    ?: "Steam",
                color = MutedText,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (game.rating != null) {
                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "★ ${game.rating}",
                    color = Gold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
