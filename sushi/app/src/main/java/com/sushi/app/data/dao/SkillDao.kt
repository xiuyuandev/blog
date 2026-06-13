package com.sushi.app.data.dao

import androidx.room.*
import com.sushi.app.data.model.Skill
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillDao {
    @Query("SELECT * FROM Skill")
    fun getAllSkills(): Flow<List<Skill>>

    @Query("SELECT * FROM Skill WHERE id = :id")
    suspend fun getSkillById(id: String): Skill?

    @Query("SELECT * FROM Skill WHERE category = :category")
    fun getSkillsByCategory(category: String): Flow<List<Skill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(skills: List<Skill>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(skill: Skill)

    @Update
    suspend fun update(skill: Skill)

    @Query("UPDATE Skill SET totalExp = :totalExp WHERE id = :id")
    suspend fun updateExp(id: String, totalExp: Int)

    @Query("UPDATE Skill SET linkedProfessionIds = :professionIds WHERE id = :id")
    suspend fun updateLinkedProfessions(id: String, professionIds: List<String>)
}
