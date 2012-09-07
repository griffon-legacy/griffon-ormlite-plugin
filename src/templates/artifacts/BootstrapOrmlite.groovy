import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager

class BootstrapOrmlite {
    def init = { String databaseName, JdbcConnectionSource connection ->
    }

    def destroy = { String databaseName, JdbcConnectionSource connection ->
    }
} 