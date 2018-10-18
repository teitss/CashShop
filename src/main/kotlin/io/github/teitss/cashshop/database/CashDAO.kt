package io.github.teitss.cashshop.database

import io.github.teitss.cashshop.config.CashShopConfig
import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.service.sql.SqlService
import java.util.*
import javax.sql.DataSource
import kotlin.properties.Delegates

object CashDAO {

    private const val CASH_SELECT_QUERY = "SELECT cash_AMOUNT FROM cashs WHERE cash_USERNAME=?"
    private const val CASH_UPDATE_QUERY = "UPDATE cashs SET cash_AMOUNT=? WHERE cash_USERNAME=?"

    private var dataSource: DataSource by Delegates.notNull()

    fun setupDatabase() {
        dataSource = Sponge.getServiceManager().provide(SqlService::class.java).get().getDataSource("jdbc:mysql://${CashShopConfig.cashDatabaseAddress}/${CashShopConfig.cashDatabaseName}?user=${CashShopConfig.cashDatabaseUser}&password=${CashShopConfig.cashDatabasePassword}")
    }

    fun selectPlayerCredits(player: Player): Optional<Int> {
        dataSource.connection.use { conn ->
            conn.prepareStatement(CASH_SELECT_QUERY).use { pstmt ->
                pstmt.setString(1, player.name)
                val rs = pstmt.executeQuery()
                if (rs.isBeforeFirst) {
                    rs.next()
                    return Optional.of(rs.getInt("cash_AMOUNT"))
                }
            }
        }
        return Optional.empty()
    }

    fun updatePlayerCredits(player: Player, cash: Int) {
        dataSource.connection.use { conn ->
            conn.prepareStatement(CASH_UPDATE_QUERY).use { pstmt ->
                pstmt.setInt(1, cash)
                pstmt.setString(2, player.name)
                pstmt.execute()
            }
        }
    }

}
