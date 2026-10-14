package com.savage.anime.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.savage.anime.data.local.AppDatabase
import com.savage.anime.data.local.entity.SettingsEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SettingsDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: SettingsDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.settingsDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `setValue and getValue round trip`() = runBlocking {
        dao.setValue(SettingsEntity("auto_play_next", "true"))

        val result = dao.getValue("auto_play_next")
        assertEquals("true", result)
    }

    @Test
    fun `getValue returns null for missing key`() = runBlocking {
        val result = dao.getValue("nonexistent")
        assertNull(result)
    }

    @Test
    fun `setValue overwrites existing key`() = runBlocking {
        dao.setValue(SettingsEntity("auto_play_next", "true"))
        dao.setValue(SettingsEntity("auto_play_next", "false"))

        val result = dao.getValue("auto_play_next")
        assertEquals("false", result)
    }
}
