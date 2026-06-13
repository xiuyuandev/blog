package com.sushi.app.data.dao

import androidx.room.*
import com.sushi.app.data.model.Skill
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillDao {
    @Query("SELECT * FROM Skill ORDER BY sortOrder ASC, name ASC")
    fun getAllSkills(): Flow<List<Skill>>

    @Query("SELECT * FROM Skill")
    suspend fun getAllSkillsSync(): List<Skill>

    @Query("SELECT * FROM Skill WHERE id = :id")
    suspend fun getSkillById(id: String): Skill?

    @Query("SELECT * FROM Skill WHERE category = :category ORDER BY sortOrder ASC, name ASC")
    fun getSkillsByCategory(category: String): Flow<List<Skill>>

    @Query("SELECT * FROM Skill WHERE isGraduated = 1 ORDER BY graduatedAt DESC")
    fun getGraduatedSkills(): Flow<List<Skill>>

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

    @Query("UPDATE Skill SET prerequisiteSkillIds = :prereqIds WHERE id = :id")
    suspend fun updatePrerequisites(id: String, prereqIds: List<String>)

    @Query("UPDATE Skill SET isGraduated = 1, graduatedAt = :graduatedAt, graduationMessage = :message WHERE id = :id")
    suspend fun graduate(id: String, graduatedAt: Long, message: String)

    @Query("DELETE FROM Skill")
    suspend fun deleteAll()

    @Query("DELETE FROM Skill WHERE id = :id")
    suspend fun deleteById(id: String)
}
