
package br.unasp.savepoint.ui.theme.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.unasp.savepoint.SavePointApplication
import br.unasp.savepoint.data.model.GameEntity
import br.unasp.savepoint.viewmodel.GameViewModel
import br.unasp.savepoint.viewmodel.ProfileViewModel
import br.unasp.savepoint.viewmodel.ProfileViewModelFactory
import coil3.compose.AsyncImage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close

private val LibraryBackground = Color(0xFF10101C)
private val LibraryCard = Color(0xFF1B1938)
private val LibraryPurple = Color(0xFF3020B5)
private val LibraryMint = Color(0xFF00E6A0)
private val LibraryMuted = Color(0xFFB8B6CC)
private val LibraryGold = Color(0xFFFFC107)

@Composable
fun LibraryScreen(
    viewModel: GameViewModel,
    onBackActionChanged: ((() -> Unit)?) -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as SavePointApplication

    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(
            application.userPreferences,
            application.authRepository,
            application.repository
        )
    )

    val games by viewModel.games.collectAsStateWithLifecycle()
    val gamePlatforms by viewModel.gamePlatforms.collectAsStateWithLifecycle()
    val userName by profileViewModel.userName.collectAsStateWithLifecycle()
    val avatarUri by profileViewModel.avatarUri.collectAsStateWithLifecycle()
    val isProfileLoaded by profileViewModel.isProfileLoaded.collectAsStateWithLifecycle()

    var selectedGame by remember {
        mutableStateOf<GameEntity?>(null)
    }

    var librarySearchQuery by remember {
        mutableStateOf("")
    }

    val searchResults = remember(games, librarySearchQuery) {
        games
            .filter {
                it.title.contains(
                    librarySearchQuery.trim(),
                    ignoreCase = true
                )
            }
            .sortedBy { it.title.lowercase() }
    }

    var showFullLibrary by remember {
        mutableStateOf(false)
    }

    var selectedStatus by remember {
        mutableStateOf("TODOS")
    }

    var showAllPlaying by remember {
        mutableStateOf(false)
    }

    var showAllRecent by remember {
        mutableStateOf(false)
    }

    // CONTROLE DO BOTÃO VOLTAR
    LaunchedEffect(selectedGame, showFullLibrary) {
        onBackActionChanged(
            when {
                selectedGame != null -> {
                    { selectedGame = null }
                }

                showFullLibrary -> {
                    { showFullLibrary = false }
                }

                else -> null
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            onBackActionChanged(null)
        }
    }

    BackHandler(
        enabled = selectedGame != null || showFullLibrary
    ) {
        if (selectedGame != null) {
            selectedGame = null
        } else {
            showFullLibrary = false
        }
    }

    // DETALHES DO JOGO
    if (selectedGame != null) {
        val game = games.firstOrNull {
            it.id == selectedGame!!.id
        } ?: selectedGame!!

        val isSteamLinked = gamePlatforms.any {
            it.gameId == game.id &&
                    it.platform.equals("Steam", ignoreCase = true)
        }

        LibraryGameDetailsScreen(
            game = game,
            onBack = {
                selectedGame = null
            },
            onUpdateGame = { updatedGame ->
                viewModel.updateGame(updatedGame)
                selectedGame = null
            },
            onDeleteGame = {
                viewModel.deleteGame(game)
                selectedGame = null
            },
            onOverrideSteamGame = if (isSteamLinked) {
                {
                    viewModel.overrideSteamGameById(game.id) { success, message ->
                        android.widget.Toast.makeText(
                            context,
                            message,
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else null
        )
        return
    }

    // COLEÇÃO COMPLETA
    if (showFullLibrary) {
        FullLibraryScreen(
            games = games,
            selectedStatus = selectedStatus,
            onStatusSelected = {
                selectedStatus = it
            },
            onBack = {
                showFullLibrary = false
            },
            onGameClick = {
                selectedGame = it
            }
        )
        return
    }

    // DADOS DA BIBLIOTECA
    val totalGames = games.size
    val finishedGames = games.count { it.status == "FINALIZADO" }
    val playingGames = games.filter { it.status == "JOGANDO" }
    val playingCount = playingGames.size

    val averageProgress = if (games.isNotEmpty()) {
        games.map {
            it.progress.coerceIn(0, 100)
        }.average().toInt()
    } else {
        0
    }

    val recentGames = games
        .sortedByDescending { it.id }
        .filter {
            selectedStatus == "TODOS" || it.status == selectedStatus
        }

    val visibleRecentGames = if (showAllRecent) {
        recentGames
    } else {
        recentGames.take(6)
    }

    val visiblePlayingGames = if (showAllPlaying) {
        playingGames
    } else {
        playingGames.take(2)
    }

    val firstName = userName
        .trim()
        .substringBefore(" ")
        .ifBlank { "jogador" }

    val initials = userName
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") {
            it.first().uppercase()
        }
        .ifBlank { "SP" }

    val currentDate = remember {
        LocalDate.now()
            .format(
                DateTimeFormatter.ofPattern(
                    "EEEE, dd 'de' MMMM",
                    Locale("pt", "BR")
                )
            )
            .uppercase(Locale("pt", "BR"))
    }

    val statusFilters = listOf(
        "TODOS" to "Todos",
        "QUERO_JOGAR" to "Quero jogar",
        "JOGANDO" to "Jogando",
        "FINALIZADO" to "Finalizado",
        "PAUSADO" to "Pausado",
        "ABANDONADO" to "Abandonado"
    )

    // PAINEL PRINCIPAL
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LibraryBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 14.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {

            // CABEÇALHO
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = currentDate,
                            color = LibraryMint,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.4.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Olá, $firstName!",
                            color = Color.White,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Pronto para a próxima aventura?",
                            color = LibraryMuted,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = librarySearchQuery,
                            onValueChange = {
                                librarySearchQuery = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "Buscar jogos na biblioteca...",
                                    color = LibraryMuted,
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar jogos",
                                    tint = LibraryMuted
                                )
                            },
                            trailingIcon = {
                                if (librarySearchQuery.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            librarySearchQuery = ""
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Limpar pesquisa",
                                            tint = LibraryMuted
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = LibraryMint,
                                unfocusedBorderColor = Color(0xFF393650),
                                cursorColor = LibraryMint,
                                focusedContainerColor = LibraryCard,
                                unfocusedContainerColor = LibraryCard
                            )
                        )
                    }

                    Box(
                        modifier = Modifier.size(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Avatar com aro em degradê
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF00E6A0),
                                            Color(0xFF398BFF)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(LibraryPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isProfileLoaded && avatarUri != null) {
                                AsyncImage(
                                    model = avatarUri,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = initials,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Indicador de online
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 1.dp, y = 1.dp)
                                .size(12.dp)
                                .background(
                                    color = LibraryBackground,
                                    shape = CircleShape
                                )
                                .padding(2.dp)
                                .background(
                                    color = LibraryMint,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }


            // RESULTADOS DA PESQUISA
            if (librarySearchQuery.isNotBlank()) {
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "${searchResults.size} jogos encontrados",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (searchResults.isEmpty()) {
                            Text(
                                text = "Nenhum jogo encontrado. Tente outro nome!",
                                color = LibraryMuted,
                                fontSize = 12.sp
                            )
                        } else {
                            searchResults.forEach { game ->
                                CollectionGameRow(
                                    game = game,
                                    onClick = {
                                        selectedGame = game
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (librarySearchQuery.isBlank()) {



            // CARD RESUMO DA BIBLIOTECA
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF3020B5),
                                    Color(0xFF5140D8)
                                )
                            )
                        )
                        .clickable {
                            showFullLibrary = true
                        }
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🎮",
                                    fontSize = 15.sp
                                )

                                Spacer(modifier = Modifier.width(7.dp))

                                Text(
                                    text = "SUA BIBLIOTECA",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = if (totalGames == 1) {
                                    "1 jogo"
                                } else {
                                    "$totalGames jogos"
                                },
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = "$playingCount em andamento · $finishedGames finalizado${if (finishedGames == 1) "" else "s"}",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp
                            )
                        }

                        Box(
                            modifier = Modifier.size(68.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { averageProgress / 100f },
                                modifier = Modifier.fillMaxSize(),
                                color = LibraryMint,
                                trackColor = Color.White.copy(alpha = 0.22f),
                                strokeWidth = 6.dp
                            )

                            Text(
                                text = "$averageProgress%",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // JOGANDO AGORA
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Jogando agora",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (showAllPlaying) {
                                "Ver menos ‹"
                            } else {
                                "Ver todos ›"
                            },
                            color = LibraryMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.clickable {
                                showAllPlaying = !showAllPlaying
                            }
                        )
                    }

                    if (playingGames.isEmpty()) {
                        Text(
                            text = "Nenhum jogo em andamento no momento.",
                            color = LibraryMuted,
                            fontSize = 12.sp
                        )
                    } else {
                        visiblePlayingGames.forEach { game ->
                            PlayingGameCard(
                                game = game,
                                onClick = {
                                    selectedGame = game
                                }
                            )
                        }
                    }
                }
            }

            // ADICIONADOS RECENTEMENTE
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Adicionados recentemente",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (showAllRecent) {
                                "Ver menos ‹"
                            } else {
                                "Ver todos ›"
                            },
                            color = LibraryMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.clickable {
                                showAllRecent = !showAllRecent
                            }
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(statusFilters) { status ->
                            FilterChip(
                                selected = selectedStatus == status.first,
                                onClick = {
                                    selectedStatus = status.first
                                    showAllRecent = false
                                },
                                label = {
                                    Text(
                                        text = status.second,
                                        fontSize = 10.sp
                                    )
                                },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LibraryPurple,
                                    selectedLabelColor = Color.White,
                                    containerColor = LibraryCard,
                                    labelColor = LibraryMuted
                                ),
                                border = null
                            )
                        }
                    }

                    if (visibleRecentGames.isEmpty()) {
                        Text(
                            text = if (games.isEmpty()) {
                                "Sua biblioteca está vazia. Explore novos jogos!"
                            } else {
                                "Nenhum jogo encontrado com esse status."
                            },
                            color = LibraryMuted,
                            fontSize = 12.sp
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = visibleRecentGames,
                                key = { it.id }
                            ) { game ->
                                RecentGameCard(
                                    game = game,
                                    onClick = {
                                        selectedGame = game
                                    }
                                )
                            }
                        }
                    }
                }
            }

            }
        }
    }
}

// TELA DA COLEÇÃO COMPLETA
@Composable
private fun FullLibraryScreen(
    games: List<GameEntity>,
    selectedStatus: String,
    onStatusSelected: (String) -> Unit,
    onBack: () -> Unit,
    onGameClick: (GameEntity) -> Unit
) {
    val statusFilters = listOf(
        "TODOS" to "Todos",
        "QUERO_JOGAR" to "Quero jogar",
        "JOGANDO" to "Jogando",
        "FINALIZADO" to "Finalizado",
        "PAUSADO" to "Pausado",
        "ABANDONADO" to "Abandonado"
    )

    val filteredGames = games
        .sortedByDescending { it.id }
        .filter {
            selectedStatus == "TODOS" ||
                    it.status == selectedStatus
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LibraryBackground)
    ) {
        Column(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 18.dp,
                bottom = 12.dp
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Text(
                        text = "‹",
                        color = LibraryMint,
                        fontSize = 34.sp
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Minha coleção",
                        color = Color.White,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${filteredGames.size} jogos",
                        color = LibraryMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(statusFilters) { status ->
                    FilterChip(
                        selected = selectedStatus == status.first,
                        onClick = {
                            onStatusSelected(status.first)
                        },
                        label = {
                            Text(
                                text = status.second,
                                fontSize = 11.sp
                            )
                        },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LibraryPurple,
                            selectedLabelColor = Color.White,
                            containerColor = LibraryCard,
                            labelColor = LibraryMuted
                        ),
                        border = null
                    )
                }
            }
        }

        if (filteredGames.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (games.isEmpty()) {
                        "Sua biblioteca ainda está vazia."
                    } else {
                        "Nenhum jogo encontrado nesse filtro."
                    },
                    color = LibraryMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = filteredGames,
                    key = { it.id }
                ) { game ->
                    CollectionGameRow(
                        game = game,
                        onClick = {
                            onGameClick(game)
                        }
                    )
                }
            }
        }
    }
}

// CARD INDIVIDUAL DA COLEÇÃO
@Composable
private fun CollectionGameRow(
    game: GameEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = LibraryCard
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = game.coverUrl,
                contentDescription = "Capa de ${game.title}",
                modifier = Modifier
                    .size(width = 62.dp, height = 82.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = game.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = game.genre ?: "Jogo",
                    color = LibraryMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = when (game.status) {
                        "QUERO_JOGAR" -> "Quero jogar"
                        "JOGANDO" -> "Jogando"
                        "FINALIZADO" -> "Finalizado"
                        "PAUSADO" -> "Pausado"
                        "ABANDONADO" -> "Abandonado"
                        else -> game.status
                    },
                    color = when (game.status) {
                        "JOGANDO" -> LibraryMint
                        "FINALIZADO" -> LibraryGold
                        else -> LibraryMuted
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${game.progress}% de progresso",
                    color = LibraryMuted,
                    fontSize = 10.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Abrir detalhes",
                tint = LibraryMuted
            )
        }
    }
}

// CARD DE JOGO EM ANDAMENTO
@Composable
private fun PlayingGameCard(
    game: GameEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor = LibraryCard
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = game.coverUrl,
                contentDescription = "Capa de ${game.title}",
                modifier = Modifier
                    .size(width = 55.dp, height = 70.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(11.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = game.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "◷  ${"%.1f".format(game.hoursPlayed)}h jogadas",
                    color = LibraryMuted,
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.height(9.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progresso",
                        color = LibraryMuted,
                        fontSize = 9.sp
                    )

                    Text(
                        text = "${game.progress}%",
                        color = LibraryMint,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.13f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(
                                game.progress.coerceIn(0, 100) / 100f
                            )
                            .fillMaxHeight()
                            .background(LibraryMint)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF153A3B))
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▶",
                    color = LibraryMint,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// CARD DE JOGO RECENTE
@Composable
private fun RecentGameCard(
    game: GameEntity,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(104.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = game.coverUrl,
            contentDescription = "Capa de ${game.title}",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.76f)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = game.title,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = game.genre ?: "Jogo",
            color = LibraryMuted,
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
