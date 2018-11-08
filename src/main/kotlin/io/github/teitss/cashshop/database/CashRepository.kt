package io.github.teitss.cashshop.database

import io.github.teitss.cashshop.config.CashShopConfig
import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.living.player.User
import org.spongepowered.api.service.sql.SqlService
import java.util.*
import javax.sql.DataSource
import kotlin.properties.Delegates

object CashRepository {

    private const val CASH_SELECT_QUERY = "SELECT cash_AMOUNT FROM cashs WHERE cash_USERNAME=?"
    private const val CASH_UPDATE_QUERY = "UPDATE cashs SET cash_AMOUNT=? WHERE cash_USERNAME=?"
    private const val CASH_INSERT_QUERY = "INSERT INTO cashs (cash_USERNAME,cash_AMOUNT) VALUES (?,?)"

    private var dataSource: DataSource by Delegates.notNull()

    fun setupDatabase() {
        dataSource = Sponge.getServiceManager().provide(SqlService::class.java).get().getDataSource("jdbc:mysql://${CashShopConfig.cashDatabaseAddress}/${CashShopConfig.cashDatabaseName}?user=${CashShopConfig.cashDatabaseUser}&password=${CashShopConfig.cashDatabasePassword}")
    }

    fun selectPlayerCredits(player: User): Optional<Int> {
        dataSource.connection.use { conn ->
            conn.prepareStatement(CASH_SELECT_QUERY).use { pstmt ->
                pstmt.setString(1, player.name)
                val rs = pstmt.executeQuery()
                if (rs.isBeforeFirst) {
                    rs.next()
                    return Optional.of(rs.getInt("cash_AMOUNT"))
                } else
                    insertPlayerCredits(player, 0)
            }
        }
        return Optional.empty()
    }

    fun updatePlayerCredits(player: User, cash: Int) {
        dataSource.connection.use { conn ->
            conn.prepareStatement(CASH_UPDATE_QUERY).use { pstmt ->
                pstmt.setInt(1, cash)
                pstmt.setString(2, player.name)
                pstmt.execute()
            }
        }
    }

    fun insertPlayerCredits(player: User, cash: Int) {
        dataSource.connection.use { conn ->
            conn.prepareStatement(CASH_INSERT_QUERY).use { pstmt ->
                pstmt.setString(1, player.name)
                pstmt.setInt(2, cash)
                pstmt.execute()
            }
        }
    }

}
