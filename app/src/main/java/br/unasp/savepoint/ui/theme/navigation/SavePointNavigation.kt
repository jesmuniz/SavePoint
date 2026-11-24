
package br.unasp.savepoint.ui.theme.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import br.unasp.savepoint.SavePointApplication
import br.unasp.savepoint.ui.theme.screens.ExploreScreen
import br.unasp.savepoint.ui.theme.screens.LibraryScreen
import br.unasp.savepoint.ui.theme.screens.BacklogScreen
import br.unasp.savepoint.ui.theme.screens.ProfileScreen
import br.unasp.savepoint.viewmodel.GameViewModel
import br.unasp.savepoint.viewmodel.GameViewModelFactory
import androidx.compose.foundation.layout.navigationBarsPadding

private val BackgroundDark = Color(0xFF10101C)
private val CardDark = Color(0xFF1B1938)
private val Mint = Color(0xFF00E6A0)
private val MutedText = Color(0xFF89869E)

private data class NavigationItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun SavePointNavigation() {

    val context = LocalContext.current
    val application =
        context.applicationContext as SavePointApplication

    val gameViewModel: GameViewModel = viewModel(
        factory = GameViewModelFactory(
            application.repository,
            application.authRepository
        )
    )

    var selectedScreen by remember {
        mutableStateOf("explore")
    }

    // Guarda a ação de voltar informada pela tela atual
    var currentBackAction by remember {
        mutableStateOf<(() -> Unit)?>(null)
    }

    // Intercepta o voltar somente quando existe uma tela interna aberta
    BackHandler(enabled = currentBackAction != null) {
        currentBackAction?.invoke()
    }

    val navigationItems = listOf(
        NavigationItem(
            route = "explore",
            label = "Explorar",
            icon = Icons.Default.Explore
        ),
        NavigationItem(
            route = "library",
            label = "Biblioteca",
            icon = Icons.Default.LibraryBooks
        ),
        NavigationItem(
            route = "backlog",
            label = "Backlog",
            icon = Icons.Default.VideogameAsset
        ),
        NavigationItem(
            route = "profile",
            label = "Perfil",
            icon = Icons.Default.Person
        )
    )

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                shape = RoundedCornerShape(24.dp),
                color = CardDark,
                shadowElevation = 10.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp)
                        .padding(horizontal = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navigationItems.forEach { item ->

                        val selected = selectedScreen == item.route

                        NavigationButton(
                            item = item,
                            selected = selected,
                            onClick = {
                                selectedScreen = item.route
                                currentBackAction = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier.padding(paddingValues)
        ) {
            when (selectedScreen) {

                "explore" -> ExploreScreen(
                    viewModel = gameViewModel,
                    onBackActionChanged = { action ->
                        currentBackAction = action
                    }
                )

                "library" -> LibraryScreen(
                    viewModel = gameViewModel,
                    onBackActionChanged = { action ->
                        currentBackAction = action
                    }
                )

                "backlog" -> BacklogScreen(
                    viewModel = gameViewModel,
                    onBackActionChanged = { action ->
                        currentBackAction = action
                    }
                )

                "profile" -> ProfileScreen(
                    gameViewModel = gameViewModel
                )
            }
        }
    }
}

@Composable
private fun NavigationButton(
    item: NavigationItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val iconColor by animateColorAsState(
        targetValue = if (selected) Mint else MutedText,
        label = "navigationIconColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) Mint else MutedText,
        label = "navigationTextColor"
    )

    val iconBackground by animateColorAsState(
        targetValue = if (selected) {
            Mint.copy(alpha = 0.12f)
        } else {
            Color.Transparent
        },
        label = "navigationIconBackground"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Box(
            modifier = Modifier
                .size(width = 62.dp, height = 39.dp)
                .clip(RoundedCornerShape(50))
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier.size(25.dp)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = item.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = if (selected) {
                FontWeight.Bold
            } else {
                FontWeight.Medium
            },
            maxLines = 1
        )
    }
}
