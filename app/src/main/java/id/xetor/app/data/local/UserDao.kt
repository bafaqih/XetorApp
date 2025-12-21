// UserDao.kt
package id.xetor.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // Menyimpan user, jika sudah ada akan diganti (UPSERT)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    // Mengambil data user berdasarkan ID
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUser(userId: String): Flow<User?> // Flow akan otomatis update UI jika data berubah
}