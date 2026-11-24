
package br.unasp.savepoint.ui.theme.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.unasp.savepoint.data.remote.RemoteGame
import coil3.compose.AsyncImage

private val BackgroundDark = Color(0xFF10101C)
private val CardDark = Color(0xFF1B1938)
private val Purple = Color(0xFF3020B5)
private val Mint = Color(0xFF00E6A0)
private val MutedText = Color(0xFFB8B6CC)
private val Placeholder = Color(0xFF302D48)

@Composable
fun GameDetailsScreen(
    game: RemoteGame,
    isLoading: Boolean,
    onBack: () -> Unit,
    onAddToLibrary: () -> Unit,
    isAlreadyInLibrary: Boolean,
    alternativeCover: String? = null,
    isLoadingAlternativeCover: Boolean = false
) {
    val detailsAvailable = !isLoading &&
            (!game.genre.isNullOrBlank() ||
                    !game.description.isNullOrBlank() ||
                    game.rating != null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
    ) {

        // CAPA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF29283D),
                            BackgroundDark
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(245.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF29283D),
                                BackgroundDark
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoadingAlternativeCover) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Placeholder),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Mint,
                            strokeWidth = 2.dp
                        )
                    }
                } else {
                    AsyncImage(
                        model = alternativeCover ?: game.coverUrl,
                        contentDescription = "Capa de ${game.title}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // GÊNERO
            AnimatedVisibility(
                visible = detailsAvailable,
                enter = fadeIn(animationSpec = tween(450))
            ) {
                Text(
                    text = game.genre
                        ?.takeIf { it.isNotBlank() }
                        ?.uppercase() ?: "GÊNERO NÃO INFORMADO",
                    color = Mint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
            }

            if (!detailsAvailable) {
                SkeletonLine(width = 120.dp, height = 12.dp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // TÍTULO
            Text(
                text = game.title,
                color = Color.White,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // INFORMAÇÕES
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (detailsAvailable && game.rating != null) {
                    Text(
                        text = "★ ${game.rating.toInt()}/100",
                        color = Color(0xFFFFC107),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(12.dp))
                }

                InfoChip(text = game.platform ?: "Steam")
            }

            Spacer(modifier = Modifier.height(30.dp))

            // DESCRIÇÃO
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Sobre o jogo",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedVisibility(
                    visible = detailsAvailable,
                    enter = fadeIn(animationSpec = tween(550))
                ) {
                    Text(
                        text = game.description
                            ?.takeIf { it.isNotBlank() }
                            ?: "Descrição não disponível.",
                        color = MutedText,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }

                if (!detailsAvailable) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        SkeletonLine(width = 300.dp)
                        SkeletonLine(width = 270.dp)
                        SkeletonLine(width = 290.dp)
                        SkeletonLine(width = 190.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // BOTÃO PRINCIPAL
            Button(
                onClick = onAddToLibrary,
                enabled = !isAlreadyInLibrary && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple,
                    contentColor = Color.White,
                    disabledContainerColor = CardDark,
                    disabledContentColor = Mint
                )
            ) {
                Text(
                    text = when {
                        isAlreadyInLibrary -> "✓ Já está na sua biblioteca"
                        isLoading -> "Carregando detalhes..."
                        else -> "+  Adicionar à biblioteca"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SkeletonLine(
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp = 10.dp
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(CircleShape)
            .background(Placeholder)
    )
}

@Composable
private fun InfoChip(text: String) {
    Surface(
        color = CardDark,
        shape = RoundedCornerShape(9.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(
                horizontal = 13.dp,
                vertical = 8.dp
            )
        )
    }
}
