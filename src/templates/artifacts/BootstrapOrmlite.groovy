import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager

class BootstrapOrmlite {
    def init = { String databaseName, ConnectionSource connection ->
    }

    def destroy = { String databaseName, ConnectionSource connection ->
    }
} 