
package br.unasp.savepoint.ui.theme.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.unasp.savepoint.data.model.GameEntity
import br.unasp.savepoint.viewmodel.GameViewModel
import coil3.compose.AsyncImage
import java.util.Locale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val BackgroundDark = Color(0xFF10101C)
private val CardDark = Color(0xFF1B1938)
private val Purple = Color(0xFF3020B5)
private val PurpleLight = Color(0xFF5543E8)
private val Mint = Color(0xFF00E6A0)
private val MutedText = Color(0xFFB8B6CC)

@Composable
fun BacklogScreen(
    viewModel: GameViewModel,
    onBackActionChanged: ((() -> Unit)?) -> Unit
) {
    val context = LocalContext.current

    val games by viewModel.games.collectAsStateWithLifecycle()
    val gamePlatforms by viewModel.gamePlatforms.collectAsStateWithLifecycle()

    val backlogGames = games.filter {
        it.status == "QUERO_JOGAR"
    }

    // Jogo sorteado: aparece no card de resultado
    var drawnGame by remember {
        mutableStateOf<GameEntity?>(null)
    }

    // Jogo selecionado: abre a tela de detalhes
    var selectedGame by remember {
        mutableStateOf<GameEntity?>(null)
    }

    // Comunica à navegação quando os detalhes estão abertos
    LaunchedEffect(selectedGame) {
        onBackActionChanged(
            if (selectedGame != null) {
                { selectedGame = null }
            } else {
                null
            }
        )
    }

    // Limpa a ação quando o Backlog deixa de estar ativo
    DisposableEffect(Unit) {
        onDispose {
            onBackActionChanged(null)
        }
    }

    // Tela de detalhes do jogo
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 16.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Cabeçalho
        item {
            Column {
                Text(
                    text = "SUA PRÓXIMA AVENTURA",
                    color = Mint,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "Backlog",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${backlogGames.size} jogos",
                        color = Mint,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = " esperando por você",
                        color = MutedText,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Card de sorteio
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Purple,
                                PurpleLight
                            )
                        )
                    )
                    .clickable(
                        enabled = backlogGames.isNotEmpty()
                    ) {
                        drawnGame = backlogGames.randomOrNull()
                    }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(
                                Color.White.copy(alpha = 0.13f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✦",
                            color = Mint,
                            fontSize = 25.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "O que jogar hoje?",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (backlogGames.isEmpty()) {
                                "Adicione jogos ao seu backlog"
                            } else {
                                "Deixe o SavePoint escolher por você"
                            },
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = "›",
                        color = Color.White,
                        fontSize = 28.sp
                    )
                }
            }
        }

        // Resultado do sorteio
        if (drawnGame != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardDark
                    ),
                    border = BorderStroke(
                        1.dp,
                        Mint.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = drawnGame!!.coverUrl,
                            contentDescription = "Capa de ${drawnGame!!.title}",
                            modifier = Modifier
                                .size(width = 68.dp, height = 86.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "ESCOLHIDO PARA VOCÊ",
                                color = Mint,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.7.sp
                            )

                            Spacer(modifier = Modifier.height(5.dp))

                            Text(
                                text = drawnGame!!.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = drawnGame!!.platform
                                    ?: "Plataforma não informada",
                                color = MutedText,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    drawnGame?.let { game ->
                                        val updatedGame = game.copy(
                                            status = "JOGANDO"
                                        )

                                        viewModel.updateGame(updatedGame)

                                        // Abre os detalhes somente após o clique
                                        selectedGame = updatedGame
                                        drawnGame = null
                                    }
                                },
                                contentPadding = PaddingValues(
                                    horizontal = 12.dp,
                                    vertical = 5.dp
                                ),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Mint,
                                    contentColor = BackgroundDark
                                ),
                                shape = RoundedCornerShape(9.dp)
                            ) {
                                Text(
                                    text = "Começar a jogar",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Título da fila
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Na fila",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = CardDark,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.08f)
                    )
                ) {
                    Text(
                        text = "${backlogGames.size} jogos",
                        color = MutedText,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        )
                    )
                }
            }
        }

        // Estado vazio
        if (backlogGames.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎮",
                        fontSize = 38.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Sua fila está vazia!",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "Adicione jogos com o status Quero jogar.",
                        color = MutedText,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            // Cards compactos da fila
            itemsIndexed(
                items = backlogGames,
                key = { _, game -> game.id }
            ) { index, game ->

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardDark
                    ),
                    border = BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.06f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = (index + 1)
                                .toString()
                                .padStart(2, '0'),
                            color = MutedText.copy(alpha = 0.65f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        AsyncImage(
                            model = game.coverUrl,
                            contentDescription = "Capa de ${game.title}",
                            modifier = Modifier
                                .size(width = 58.dp, height = 72.dp)
                                .clip(RoundedCornerShape(9.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

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

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = game.genre
                                    ?.takeIf { it.isNotBlank() }
                                    ?: "Jogo",
                                color = MutedText,
                                fontSize = 10.sp
                            )

                            Spacer(modifier = Modifier.height(5.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (
                                    game.userRating != null &&
                                    game.userRating > 0f
                                ) {
                                    Text(
                                        text = "★ ${
                                            String.format(
                                                Locale("pt", "BR"),
                                                "%.1f",
                                                game.userRating
                                            )
                                        }",
                                        color = Color(0xFFFFC107),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.width(7.dp))
                                }

                                Text(
                                    text = game.platform
                                        ?: "Plataforma não informada",
                                    color = MutedText,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
