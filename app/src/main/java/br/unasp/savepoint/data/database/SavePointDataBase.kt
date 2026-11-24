
package br.unasp.savepoint.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import br.unasp.savepoint.data.dao.GameDao
import br.unasp.savepoint.data.model.GameEntity
import br.unasp.savepoint.data.model.GamePlatformEntity

@Database(
    entities = [
        GameEntity::class,
        GamePlatformEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class SavePointDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao

    companion object {

        @Volatile
        private var INSTANCE: SavePointDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE games ADD COLUMN userRating REAL"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE games ADD COLUMN customCoverUrl TEXT"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS game_platforms (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        gameId INTEGER NOT NULL,
                        platform TEXT NOT NULL,
                        externalId TEXT NOT NULL,
                        hoursPlayed REAL NOT NULL,
                        lastPlayedAt INTEGER,
                        lastSyncedAt INTEGER,
                        FOREIGN KEY(gameId) REFERENCES games(id)
                        ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_game_platforms_gameId
                    ON game_platforms(gameId)
                """.trimIndent())

                db.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_game_platforms_platform_externalId
                    ON game_platforms(platform, externalId)
                """.trimIndent())
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL("""
            ALTER TABLE games
            ADD COLUMN statusManuallySet INTEGER NOT NULL DEFAULT 1
        """.trimIndent())

                db.execSQL("""
            ALTER TABLE games
            ADD COLUMN hoursManuallySet INTEGER NOT NULL DEFAULT 1
        """.trimIndent())
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {

                // A Steam passa a ser a fonte oficial das horas.
                db.execSQL("""
            UPDATE games
            SET hoursManuallySet = 0
        """.trimIndent())

                // Preserva status personalizados e libera os automáticos.
                db.execSQL("""
            UPDATE games
            SET statusManuallySet = CASE
                WHEN status IN ('JOGANDO', 'QUERO_JOGAR') THEN 0
                ELSE 1
            END
        """.trimIndent())
            }
        }

        fun getDatabase(context: Context): SavePointDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SavePointDatabase::class.java,
                    "savepoint_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6
                    )
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
