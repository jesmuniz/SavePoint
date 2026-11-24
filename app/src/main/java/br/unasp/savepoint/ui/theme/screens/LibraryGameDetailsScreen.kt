
package br.unasp.savepoint.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.unasp.savepoint.data.model.GameEntity
import coil3.compose.AsyncImage
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.MoreVert

private val BackgroundDark = Color(0xFF10101C)
private val CardDark = Color(0xFF1B1938)
private val Purple = Color(0xFF3020B5)
private val Mint = Color(0xFF00E6A0)
private val MutedText = Color(0xFFB8B6CC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryGameDetailsScreen(
    game: GameEntity,
    onBack: () -> Unit,
    onUpdateGame: (GameEntity) -> Unit,
    onDeleteGame: () -> Unit,
    onOverrideSteamGame: (() -> Unit)? = null
) {
    var selectedStatus by remember(game.id) {
        mutableStateOf(game.status)
    }

    var progressText by remember(game.id) {
        mutableStateOf(game.progress.toString())
    }

    var isEditingProgress by remember {
        mutableStateOf(false)
    }

    var hoursPlayedText by remember(game.id, game.hoursPlayed) {
        mutableStateOf(game.hoursPlayed.toString())
    }

    var clearHoursOnFocus by remember(game.id) {
        mutableStateOf(game.hoursPlayed == 0f)
    }

    var userRating by remember(game.id) {
        mutableFloatStateOf(game.userRating ?: 0f)
    }

    var notesText by remember(game.id) {
        mutableStateOf(game.notes.orEmpty())
    }

    var isFavorite by remember(game.id) {
        mutableStateOf(game.isFavorite)
    }

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    var showGameMenu by remember {
        mutableStateOf(false)
    }

    var showOverrideDialog by remember {
        mutableStateOf(false)
    }

    val progressValue = progressText.toIntOrNull()
        ?.coerceIn(0, 100)
        ?: game.progress.coerceIn(0, 100)

    val statuses = listOf(
        "QUERO_JOGAR" to "Quero jogar",
        "JOGANDO" to "Jogando",
        "FINALIZADO" to "Finalizado",
        "PAUSADO" to "Pausado",
        "ABANDONADO" to "Abandonado"
    )

    val statusColors = mapOf(
        "QUERO_JOGAR" to Color(0xFF00E6A0),
        "JOGANDO" to Color(0xFF00E6A0),
        "FINALIZADO" to Color(0xFF64B5F6),
        "PAUSADO" to Color(0xFFFFB74D),
        "ABANDONADO" to Color(0xFFFF7676)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
    ) {


// Cabeçalho com capa
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF29283D),
                            BackgroundDark
                        )
                    )
                ),
            contentAlignment = Alignment.BottomCenter
        ) {

            AsyncImage(
                model = game.customCoverUrl ?: game.coverUrl,
                contentDescription = "Capa de ${game.title}",
                modifier = Modifier
                    .width(180.dp)
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(15.dp)),
                contentScale = ContentScale.Crop
            )

            // TRÊS PONTINHOS - CANTO SUPERIOR ESQUERDO
            if (onOverrideSteamGame != null) {

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {

                    IconButton(
                        onClick = {
                            showGameMenu = true
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                Color(0x8010101C),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Mais opções",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showGameMenu,
                        onDismissRequest = {
                            showGameMenu = false
                        }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text("Sobrescrever com dados da Steam")
                            },
                            onClick = {
                                showGameMenu = false
                                showOverrideDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }

            // FAVORITOS - CANTO SUPERIOR DIREITO
            IconButton(
                onClick = {
                    isFavorite = !isFavorite
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(42.dp)
                    .background(
                        Color(0x8010101C),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isFavorite) {
                        Icons.Filled.Favorite
                    } else {
                        Icons.Outlined.FavoriteBorder
                    },
                    contentDescription = if (isFavorite) {
                        "Remover dos favoritos"
                    } else {
                        "Adicionar aos favoritos"
                    },
                    tint = if (isFavorite) {
                        Color(0xFFFF4081)
                    } else {
                        MutedText
                    },
                    modifier = Modifier.size(21.dp)
                )
            }
        }

// DIÁLOGO DE SOBRESCRITA
        if (showOverrideDialog) {
            AlertDialog(
                onDismissRequest = {
                    showOverrideDialog = false
                },
                title = {
                    Text("Sobrescrever dados?")
                },
                text = {
                    Text(
                        "Os dados de horas jogadas e status de " +
                                "${game.title} serão atualizados com as " +
                                "informações disponíveis na Steam. " +
                                "Suas notas, avaliações, favoritos e capas " +
                                "personalizadas serão preservados.\n\n" +
                                "Deseja continuar?"
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showOverrideDialog = false
                            onOverrideSteamGame?.invoke()
                        }
                    ) {
                        Text(
                            "Sobrescrever",
                            color = Color(0xFFFF7676)
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showOverrideDialog = false
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {

            Spacer(modifier = Modifier.height(14.dp))

            // Gênero
            Text(
                text = game.genre
                    ?.takeIf { it.isNotBlank() }
                    ?.uppercase()
                    ?: "JOGO",
                color = Mint,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.8.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Título
            Text(
                text = game.title,
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Plataforma e nota Metacritic
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (game.rating != null) {
                    Text(
                        text = "★ ${
                            String.format(
                                java.util.Locale.US,
                                "%.1f",
                                game.rating/ 10f
                            )
                        }",
                        color = Color(0xFFFFC107),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(10.dp))
                }

                Surface(
                    color = CardDark,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = game.platform ?: "Plataforma não informada",
                        color = MutedText,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(
                            horizontal = 9.dp,
                            vertical = 5.dp
                        )
                    )
                }
            }


            Spacer(modifier = Modifier.height(22.dp))

            // Meu status
            Text(
                text = "Meu status",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    statuses.take(3).forEach { status ->
                        StatusOption(
                            label = status.second,
                            selected = selectedStatus == status.first,
                            selectedColor = statusColors[status.first] ?: Mint,
                            onClick = {
                                selectedStatus = status.first
                            }
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    statuses.drop(3).forEach { status ->
                        StatusOption(
                            label = status.second,
                            selected = selectedStatus == status.first,
                            selectedColor = statusColors[status.first] ?: Mint,
                            onClick = {
                                selectedStatus = status.first
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Meu progresso
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardDark
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Meu progresso",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (isEditingProgress) {
                            OutlinedTextField(
                                value = progressText,
                                onValueChange = { newValue ->
                                    if (
                                        newValue.isEmpty() ||
                                        newValue.all { it.isDigit() }
                                    ) {
                                        val number = newValue.toIntOrNull()
                                        if (number == null || number in 0..100) {
                                            progressText = newValue
                                        }
                                    }
                                },
                                modifier = Modifier.width(88.dp),
                                singleLine = true,
                                suffix = {
                                    Text("%")
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Mint,
                                    unfocusedBorderColor = MutedText
                                )
                            )
                        } else {
                            TextButton(
                                onClick = {
                                    isEditingProgress = true
                                },
                                contentPadding = PaddingValues(
                                    horizontal = 4.dp,
                                    vertical = 0.dp
                                )
                            ) {
                                Text(
                                    text = "$progressValue%",
                                    color = Mint,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Slider(
                        value = progressValue.toFloat(),
                        onValueChange = { value ->
                            progressText = value.toInt().toString()
                            isEditingProgress = false
                        },
                        valueRange = 0f..100f,
                        modifier = Modifier.fillMaxWidth(),
                        thumb = {
                            // Bolinha mint com centro branco
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Mint, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color.White, CircleShape)
                                )
                            }
                        },
                        track = { sliderState ->
                            val range = sliderState.valueRange
                            val fraction = (
                                    (sliderState.value - range.start) /
                                            (range.endInclusive - range.start)
                                    ).coerceIn(0f, 1f)

                            // Trilho fino e arredondado
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3A3850))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction)
                                        .fillMaxHeight()
                                        .background(Mint)
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Horas jogadas
                    OutlinedTextField(
                        value = hoursPlayedText,
                        onValueChange = {
                            hoursPlayedText = it
                            clearHoursOnFocus = false
                        },
                        label = {
                            Text("Atualizar horas jogadas")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (
                                    focusState.isFocused &&
                                    clearHoursOnFocus
                                ) {
                                    hoursPlayedText = ""
                                    clearHoursOnFocus = false
                                }
                            },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Mint,
                            unfocusedBorderColor = Color(0xFF454258),
                            focusedLabelColor = Mint,
                            unfocusedLabelColor = MutedText
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Notas pessoais
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notas pessoais",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Editar",
                    tint = MutedText,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = notesText,
                onValueChange = {
                    notesText = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 110.dp),
                placeholder = {
                    Text(
                        text = "Escreva suas impressões, dicas, objetivos ou observações sobre este jogo...",
                        color = MutedText
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Mint,
                    unfocusedBorderColor = Color(0xFF454258),
                    cursorColor = Mint,
                    focusedContainerColor = CardDark,
                    unfocusedContainerColor = CardDark
                ),
                shape = RoundedCornerShape(13.dp)
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Avaliação pessoal
            Text(
                text = "Sua avaliação",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (star in 1..5) {
                    IconButton(
                        onClick = {
                            userRating = star.toFloat()
                        }
                    ) {
                        Icon(
                            imageVector = if (star <= userRating) {
                                Icons.Filled.Star
                            } else {
                                Icons.Outlined.Star
                            },
                            contentDescription = "Avaliar com $star estrelas",
                            tint = if (star <= userRating) {
                                Color(0xFFFFC107)
                            } else {
                                MutedText
                            }
                        )
                    }
                }

                Text(
                    text = if (userRating > 0f) {
                        "${userRating.toInt()}/5"
                    } else {
                        "Não avaliado"
                    },
                    color = MutedText,
                    fontSize = 12.sp
                )
            }

            if (game.rating != null) {
                Text(
                    text = "Nota Metacritic: ${game.rating.toInt()}/100",
                    color = MutedText,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Salvar alterações
            Button(
                onClick = {
                    val parsedProgress = progressText.toIntOrNull()
                    val parsedHours = hoursPlayedText
                        .replace(",", ".")
                        .toFloatOrNull()

                    if (
                        parsedProgress != null &&
                        parsedProgress in 0..100 &&
                        parsedHours != null &&
                        parsedHours >= 0f
                    ) {
                        onUpdateGame(
                            game.copy(
                                status = selectedStatus,
                                progress = parsedProgress,
                                hoursPlayed = parsedHours,
                                userRating = userRating.takeIf {
                                    it > 0f
                                },
                                isFavorite = isFavorite,
                                notes = notesText
                            )
                        )
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Salvar alterações",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Excluir jogo
            OutlinedButton(
                onClick = {
                    showDeleteDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFFF7676)
                )
            ) {
                Text("Excluir jogo")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Confirmação de exclusão
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text("Excluir jogo?")
            },
            text = {
                Text(
                    "Tem certeza de que deseja remover " +
                            "${game.title} da sua biblioteca? " +
                            "Essa ação não poderá ser desfeita."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteGame()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun StatusOption(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (selected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                }
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = selectedColor.copy(alpha = 0.13f),
            selectedLabelColor = selectedColor,
            containerColor = CardDark,
            labelColor = MutedText
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            selectedBorderColor = selectedColor.copy(alpha = 0.7f),
            borderColor = Color(0xFF38364D)
        )
    )
}
