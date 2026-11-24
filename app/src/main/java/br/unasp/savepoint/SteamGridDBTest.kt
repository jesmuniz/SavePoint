
package br.unasp.savepoint

import android.util.Log
import br.unasp.savepoint.data.remote.SteamGridDBDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun testSteamGridDB(application: SavePointApplication) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val source = SteamGridDBDataSource(
                br.unasp.savepoint.data.remote.HttpClientProvider.client
            )

            val cover = source.getCoverByGameName("Stardew Valley")

            Log.d(
                "SavePointGridTest",
                "Capa encontrada: $cover"
            )

        } catch (exception: Exception) {
            Log.e(
                "SavePointGridTest",
                "Erro no teste SteamGridDB",
                exception
            )
        }
    }
}
