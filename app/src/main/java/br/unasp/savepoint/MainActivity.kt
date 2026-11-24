
package br.unasp.savepoint

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import br.unasp.savepoint.ui.theme.SavePointTheme
import br.unasp.savepoint.ui.theme.navigation.SavePointNavigation
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SavePointTheme {
                SavePointNavigation()
            }
        }

        handleAuthDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        handleAuthDeepLink(intent)
    }

    private fun handleAuthDeepLink(intent: Intent?) {

        val uri = intent?.data ?: return

        if (
            uri.scheme != "br.unasp.savepoint" ||
            uri.host != "auth" ||
            uri.path != "/callback"
        ) {
            return
        }

        val code = uri.getQueryParameter("code")

        if (code.isNullOrBlank()) {
            Toast.makeText(
                this,
                "Código de autenticação não encontrado.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Evita processar novamente o mesmo Intent.
        intent.data = null

        val application = application as SavePointApplication

        lifecycleScope.launch {

            val result = application.authRepository.loginWithSteam(code)

            result.onSuccess {
                Toast.makeText(
                    this@MainActivity,
                    "Login com Steam realizado!",
                    Toast.LENGTH_LONG
                ).show()
            }

            result.onFailure { exception ->
                Toast.makeText(
                    this@MainActivity,
                    "Falha no login: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
