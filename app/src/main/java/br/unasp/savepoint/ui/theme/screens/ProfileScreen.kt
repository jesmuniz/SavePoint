
package br.unasp.savepoint.ui.theme.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlin.math.roundToInt
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.horizontalScroll
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Refresh


private val ProfileBackground = Color(0xFF10101C)
private val ProfileCard = Color(0xFF1B1938)
private val ProfileMint = Color(0xFF00E6A0)
private val ProfilePurple = Color(0xFF3020B5)
private val ProfileMuted = Color(0xFFB8B6CC)
private val ProfilePink = Color(0xFFFF4081)
private val ProfileGold = Color(0xFFFFC107)

@Composable
fun ProfileScreen(
    gameViewModel: GameViewModel
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

    val userName by profileViewModel.userName.collectAsStateWithLifecycle()
    val userTag by profileViewModel.userTag.collectAsStateWithLifecycle()
    val avatarUri by profileViewModel.avatarUri.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val isProfileLoaded by profileViewModel.isProfileLoaded.collectAsStateWithLifecycle()

    val steamGames by profileViewModel.steamGames.collectAsStateWithLifecycle()
    val isLoadingSteamGames by profileViewModel.isLoadingSteamGames.collectAsStateWithLifecycle()
    val steamError by profileViewModel.steamError.collectAsStateWithLifecycle()
    val steamGameCovers by profileViewModel.steamGameCovers.collectAsStateWithLifecycle()


    // ESTADO DA AUTENTICAÇÃO STEAM
    var isSteamConnected by remember {
        mutableStateOf(false)
    }

    var steamId by remember {
        mutableStateOf<String?>(null)
    }

    var isAuthChecking by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(Unit) {
        isSteamConnected = application.authRepository.isLoggedIn()
        steamId = application.authRepository.getSteamId()
        isAuthChecking = false
    }

    LaunchedEffect(isSteamConnected) {
        if (isSteamConnected) {
            profileViewModel.loadSteamLibrary()
        }
    }

    val games by gameViewModel.games.collectAsStateWithLifecycle()
    val gamePlatforms by gameViewModel.gamePlatforms.collectAsStateWithLifecycle()
    val favoriteGames = games.filter { it.isFavorite }

    var showEditDialog by remember { mutableStateOf(false) }
    var nameDraft by remember { mutableStateOf("") }
    var tagDraft by remember { mutableStateOf("") }

    var showAllFavorites by remember { mutableStateOf(false) }
    var selectedFavorite by remember { mutableStateOf<GameEntity?>(null) }


    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                profileViewModel.saveAvatar(it.toString())
            } catch (exception: SecurityException) {
                exception.printStackTrace()
            }
        }
    }

    // ESTATÍSTICAS
    val totalGames = games.size
    val finishedGames = games.count { it.status == "FINALIZADO" }
    val playingGames = games.count { it.status == "JOGANDO" }
    val wantToPlayGames = games.count { it.status == "QUERO_JOGAR" }
    val otherGames = games.count {
        it.status == "PAUSADO" || it.status == "ABANDONADO"
    }

    val totalHours = games.sumOf { it.hoursPlayed.toDouble() }

    val averageProgress = if (games.isNotEmpty()) {
        games.map { it.progress.coerceIn(0, 100) }.average().roundToInt()
    } else {
        0
    }

    val ratedGames = games.filter {
        it.userRating != null && it.userRating > 0f
    }

    val averageRating = if (ratedGames.isNotEmpty()) {
        ratedGames.map { it.userRating!! }.average()
    } else {
        0.0
    }

    val totalRatedGames = ratedGames.size

    val initials = userName
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "SP" }

    // CONTROLE DO BOTÃO VOLTAR
    BackHandler(
        enabled = showAllFavorites || selectedFavorite != null
    ) {
        if (selectedFavorite != null) {
            selectedFavorite = null
        } else {
            showAllFavorites = false
        }
    }

    // DETALHES DO JOGO FAVORITO
    if (selectedFavorite != null) {
        val game = games.firstOrNull {
            it.id == selectedFavorite!!.id
        } ?: selectedFavorite!!

        val isSteamLinked = gamePlatforms.any {
            it.gameId == game.id &&
                    it.platform.equals("Steam", ignoreCase = true)
        }

        LibraryGameDetailsScreen(
            game = game,
            onBack = {
                selectedFavorite = null
            },
            onUpdateGame = { updatedGame ->
                gameViewModel.updateGame(updatedGame)
                selectedFavorite = null
            },
            onDeleteGame = {
                gameViewModel.deleteGame(game)
                selectedFavorite = null
            },
            onOverrideSteamGame = if (isSteamLinked) {
                {
                    gameViewModel.overrideSteamGameById(game.id) { success, message ->
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

    // LISTA COMPLETA DE FAVORITOS
    if (showAllFavorites) {
        ProfileFavoritesScreen(
            games = favoriteGames,
            onBack = {
                showAllFavorites = false
            },
            onGameClick = {
                selectedFavorite = it
            }
        )
        return
    }

    // TELA PRINCIPAL DO PERFIL
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 18.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // CABEÇALHO
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .border(
                            width = 4.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    ProfileMint,
                                    Color(0xFF398BFF)
                                )
                            ),
                            shape = CircleShape
                        )
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(ProfileCard)
                        .clickable {
                            imagePicker.launch(arrayOf("image/*"))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        !isProfileLoaded -> {
                            CircularProgressIndicator(
                                color = ProfileMint,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        avatarUri != null -> {
                            AsyncImage(
                                model = avatarUri,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        else -> {
                            Text(
                                text = initials,
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // INDICADOR DE CONEXÃO STEAM
                Box(
                    modifier = Modifier
                        .offset(x = 29.dp, y = (-11).dp)
                        .size(24.dp)
                        .background(
                            color = Color(0xFF0F1019),
                            shape = CircleShape
                        )
                        .padding(4.dp)
                        .background(
                            color = when {
                                isAuthChecking -> ProfileMuted
                                isSteamConnected -> Color(0xFFFF9800)
                                else -> ProfilePink
                            },
                            shape = CircleShape
                        )
                )

                Text(
                    text = userName.ifBlank { "Jogador" },
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = userTag,
                    color = ProfileMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

// CONEXÃO COM A STEAM
                if (isAuthChecking) {
                    Text(
                        text = "Verificando conexão...",
                        color = ProfileMuted,
                        fontSize = 12.sp
                    )
                } else if (isSteamConnected) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        Color(0xFFFF9800),
                                        CircleShape
                                    )
                            )

                            Text(
                                text = "Conectado à Steam",
                                color = Color(0xFFFF9800),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Steam ID: ${steamId ?: "Não disponível"}",
                            color = ProfileMuted,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    Text(
                        text = "Steam não conectada",
                        color = ProfilePink,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        nameDraft = userName
                        tagDraft = userTag
                        showEditDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 7.dp
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ProfileMuted
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        ProfileMuted.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Editar perfil",
                            tint = Color(0xFFD8D5E5),
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "Editar perfil",
                            fontSize = 12.sp
                        )
                    }
                }

                if (isSteamConnected && !isAuthChecking) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                application.authRepository.logout()
                                isSteamConnected = false
                                steamId = null
                            }
                        }
                    ) {
                        Text(
                            text = "Desconectar Steam",
                            color = ProfilePink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (isProfileLoaded && avatarUri != null) {
                    TextButton(
                        onClick = {
                            profileViewModel.saveAvatar(null)
                        }
                    ) {
                        Text(
                            text = "Remover foto",
                            color = ProfileMuted,
                            fontSize = 11.sp
                        )
                    }
                } else if (isProfileLoaded) {
                    TextButton(
                        onClick = {
                            imagePicker.launch(arrayOf("image/*"))
                        }
                    ) {
                        Text(
                            text = "Adicionar foto",
                            color = ProfileMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // BIBLIOTECA STEAM
        if (isSteamConnected) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = ProfileCard
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "Biblioteca Steam",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    when {
                        isLoadingSteamGames -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    color = ProfileMint,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = "Carregando seus jogos...",
                                    color = ProfileMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        steamError != null -> {
                            Text(
                                text = steamError ?: "Erro ao carregar biblioteca.",
                                color = ProfilePink,
                                fontSize = 12.sp
                            )
                        }

                        else -> {
                            Text(
                                text = "${steamGames.size} jogos encontrados na Steam",
                                color = ProfileMint,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                steamGames.take(10).forEach { game ->
                                    Card(
                                        modifier = Modifier.width(130.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF252347)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column {
                                            AsyncImage(
                                                model = steamGameCovers[game.appId],
                                                contentDescription = "Capa de ${game.name ?: "jogo"}",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(155.dp),
                                                contentScale = ContentScale.Crop
                                            )

                                            Column(
                                                modifier = Modifier.padding(10.dp)
                                            ) {
                                                Text(
                                                    text = game.name ?: "Jogo ${game.appId}",
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 2
                                                )

                                                Spacer(modifier = Modifier.height(5.dp))

                                                Text(
                                                    text = "${game.playtimeForeverMinutes / 60}h jogadas",
                                                    color = ProfileMuted,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (steamGames.isEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Nenhum jogo encontrado na biblioteca.",
                                    color = ProfileMuted,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    profileViewModel.syncSteamLibrary()
                                },
                                enabled = !isLoadingSteamGames,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ProfileMint,
                                    contentColor = ProfileBackground,
                                    disabledContainerColor = ProfileMint.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isLoadingSteamGames) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = ProfileBackground,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Sincronizar biblioteca"
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "Sincronizar biblioteca",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }


        // ESTATÍSTICAS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            ProfileStatCard(
                title = "Na biblioteca",
                value = totalGames.toString(),
                icon = {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = Color(0xFF9B8CFF),
                        modifier = Modifier.size(19.dp)
                    )
                },
                iconBackground = Color(0xFF292344),
                modifier = Modifier.weight(1f)
            )

            ProfileStatCard(
                title = "Finalizados",
                value = finishedGames.toString(),
                icon = {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = ProfileMint,
                        modifier = Modifier.size(19.dp)
                    )
                },
                iconBackground = Color(0xFF153A36),
                modifier = Modifier.weight(1f)
            )

            ProfileStatCard(
                title = "Favoritos",
                value = favoriteGames.size.toString(),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = ProfilePink,
                        modifier = Modifier.size(19.dp)
                    )
                },
                iconBackground = Color(0xFF392137),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // PROGRESSO DA BIBLIOTECA
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = ProfileCard
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "Seu progresso",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    ProfileProgressDonut(
                        progress = averageProgress,
                        finished = finishedGames,
                        playing = playingGames,
                        wantToPlay = wantToPlayGames,
                        other = otherGames,
                        modifier = Modifier.size(126.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProfileLegendItem(
                            color = ProfileMint,
                            count = finishedGames,
                            title = "Finalizado"
                        )

                        ProfileLegendItem(
                            color = Color(0xFF7560FF),
                            count = playingGames,
                            title = "Jogando"
                        )

                        ProfileLegendItem(
                            color = Color(0xFF918B9F),
                            count = wantToPlayGames,
                            title = "Na fila"
                        )

                        if (otherGames > 0) {
                            ProfileLegendItem(
                                color = ProfileGold,
                                count = otherGames,
                                title = "Outros"
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // FAVORITOS
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Favoritos",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.clickable {
                        showAllFavorites = true
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ver todos",
                        color = ProfileMuted,
                        fontSize = 11.sp
                    )

                    Text(
                        text = "›",
                        color = ProfileMuted,
                        fontSize = 19.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (favoriteGames.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = ProfileCard
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Você ainda não adicionou jogos aos favoritos.",
                        color = ProfileMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                favoriteGames.take(4).forEach { game ->
                    ProfileFavoriteRow(
                        game = game,
                        onClick = {
                            selectedFavorite = game
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // MINHA JORNADA
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = ProfileCard
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "Minha jornada",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Horas jogadas",
                    color = ProfileMuted,
                    fontSize = 14.sp
                )

                Text(
                    text = "%.1f h".format(totalHours),
                    color = ProfileMint,
                    fontSize = 29.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(
                    color = Color.LightGray.copy(alpha = 0.18f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Avaliação média",
                    color = ProfileMuted,
                    fontSize = 14.sp
                )

                if (totalRatedGames > 0) {
                    val roundedRating = kotlin.math.round(averageRating)
                        .toInt()
                        .coerceIn(0, 5)

                    Text(
                        text = "%.1f / 5 estrelas".format(averageRating),
                        color = ProfileMint,
                        fontSize = 23.sp
                    )

                    Text(
                        text = (1..5).joinToString(" ") { star ->
                            if (star <= roundedRating) "★" else "☆"
                        },
                        color = ProfileGold,
                        fontSize = 23.sp
                    )

                    Text(
                        text = "$totalRatedGames jogos avaliados",
                        color = ProfileMuted,
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text = "Nenhum jogo avaliado ainda",
                        color = ProfileMuted,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Jogos na biblioteca: $totalGames",
                    color = ProfileMuted,
                    fontSize = 13.sp
                )

                Text(
                    text = "Jogos finalizados: $finishedGames",
                    color = ProfileMuted,
                    fontSize = 13.sp
                )
            }
        }
    }

    // DIÁLOGO DE EDIÇÃO
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = {
                showEditDialog = false
            },
            containerColor = ProfileCard,
            title = {
                Text(
                    text = "Editar perfil",
                    color = Color.White
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = nameDraft,
                        onValueChange = { nameDraft = it },
                        label = { Text("Nickname") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ProfileMint,
                            unfocusedBorderColor = ProfileMuted,
                            focusedLabelColor = ProfileMint,
                            unfocusedLabelColor = ProfileMuted,
                            cursorColor = ProfileMint
                        )
                    )

                    OutlinedTextField(
                        value = tagDraft,
                        onValueChange = { tagDraft = it },
                        label = { Text("Tag") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ProfileMint,
                            unfocusedBorderColor = ProfileMuted,
                            focusedLabelColor = ProfileMint,
                            unfocusedLabelColor = ProfileMuted,
                            cursorColor = ProfileMint
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = nameDraft.isNotBlank() &&
                            tagDraft.isNotBlank(),
                    onClick = {
                        val newName = nameDraft.trim()
                        val cleanTag = tagDraft.trim()

                        val newTag = if (cleanTag.startsWith("@")) {
                            cleanTag
                        } else {
                            "@$cleanTag"
                        }

                        profileViewModel.saveProfile(
                            name = newName,
                            tag = newTag
                        )

                        showEditDialog = false
                    }
                ) {
                    Text("Salvar", color = ProfileMint)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                    }
                ) {
                    Text("Cancelar", color = ProfileMuted)
                }
            }
        )
    }
}

// CARD DE ESTATÍSTICA
@Composable
private fun ProfileStatCard(
    title: String,
    value: String,
    icon: @Composable () -> Unit,
    iconBackground: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = ProfileCard
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.06f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = value,
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = title,
                color = ProfileMuted,
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
}

// GRÁFICO CIRCULAR
@Composable
private fun ProfileProgressDonut(
    progress: Int,
    finished: Int,
    playing: Int,
    wantToPlay: Int,
    other: Int,
    modifier: Modifier = Modifier
) {
    val segments = listOf(
        finished to ProfileMint,
        playing to Color(0xFF7560FF),
        wantToPlay to Color(0xFF918B9F),
        other to ProfileGold
    ).filter { it.first > 0 }

    val total = segments.sumOf { it.first }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val strokeWidth = 12.dp.toPx()

            drawArc(
                color = Color(0xFF343044),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )

            if (total > 0) {
                var startAngle = -90f

                segments.forEach { (count, color) ->
                    val sweep = count.toFloat() / total * 360f

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    )

                    startAngle += sweep
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$progress%",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "concluído",
                color = ProfileMuted,
                fontSize = 9.sp
            )
        }
    }
}

// LEGENDA DO GRÁFICO
@Composable
private fun ProfileLegendItem(
    color: Color,
    count: Int,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )

        Text(
            text = count.toString(),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = title,
            color = ProfileMuted,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// LINHA DE JOGO FAVORITO
@Composable
private fun ProfileFavoriteRow(
    game: GameEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = ProfileCard
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = game.coverUrl,
                contentDescription = "Capa de ${game.title}",
                modifier = Modifier
                    .size(width = 44.dp, height = 52.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = game.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = game.genre ?: "Jogo",
                    color = ProfileMuted,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Favorito",
                tint = ProfilePink,
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .size(19.dp)
            )
        }
    }
}

// TELA COMPLETA DE FAVORITOS
@Composable
private fun ProfileFavoritesScreen(
    games: List<GameEntity>,
    onBack: () -> Unit,
    onGameClick: (GameEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileBackground)
            .padding(horizontal = 16.dp)
            .padding(top = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "‹",
                color = ProfileMint,
                fontSize = 34.sp,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(end = 14.dp)
            )

            Column {
                Text(
                    text = "Meus favoritos",
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (games.size == 1) {
                        "1 jogo favorito"
                    } else {
                        "${games.size} jogos favoritos"
                    },
                    color = ProfileMuted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (games.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Você ainda não tem jogos favoritos.",
                    color = ProfileMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                games.forEach { game ->
                    ProfileFavoriteRow(
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
