package com.sushi.app.data.dao

import androidx.room.*
import com.sushi.app.data.model.Affix
import kotlinx.coroutines.flow.Flow

@Dao
interface AffixDao {
    @Query("SELECT * FROM Affix")
    fun getAllAffixes(): Flow<List<Affix>>

    @Query("SELECT * FROM Affix")
    suspend fun getAllAffixesSync(): List<Affix>

    @Query("SELECT * FROM Affix WHERE requiredSkillId = :skillId")
    fun getAffixesBySkillId(skillId: String): Flow<List<Affix>>

    @Query("SELECT * FROM Affix WHERE requiredSkillId IN (:skillIds)")
    suspend fun getAffixesBySkillIds(skillIds: List<String>): List<Affix>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(affixes: List<Affix>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(affix: Affix)

    @Query("SELECT * FROM Affix WHERE id = :id")
    suspend fun getAffixById(id: String): Affix?

    @Query("DELETE FROM Affix WHERE id = :id")
    suspend fun deleteById(id: String)
}
