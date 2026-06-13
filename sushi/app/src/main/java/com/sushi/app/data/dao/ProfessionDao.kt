package com.sushi.app.data.dao

import androidx.room.*
import com.sushi.app.data.model.Profession
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfessionDao {
    @Query("SELECT * FROM Profession")
    fun getAllProfessions(): Flow<List<Profession>>

    @Query("SELECT * FROM Profession")
    suspend fun getAllProfessionsSync(): List<Profession>

    @Query("SELECT * FROM Profession WHERE id = :id")
    suspend fun getProfessionById(id: String): Profession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(professions: List<Profession>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profession: Profession)

    @Update
    suspend fun update(profession: Profession)

    @Query("UPDATE Profession SET totalExp = :totalExp WHERE id = :id")
    suspend fun updateExp(id: String, totalExp: Int)

    @Query("DELETE FROM Profession")
    suspend fun deleteAll()

    @Query("DELETE FROM Profession WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE Profession SET name = :name WHERE id = :id")
    suspend fun updateName(id: String, name: String)
}
