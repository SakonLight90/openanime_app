package com.savage.anime.ui.screens.home

import com.savage.anime.domain.models.Anime
import com.savage.anime.domain.models.ContinueWatchingItem
import com.savage.anime.domain.models.Episode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeUiStateTest {

    @Test
    fun `default state is loading with no error`() {
        val state = HomeUiState()
        assertTrue(state.isLoading)
        assertNull(state.error)
        assertTrue(state.hero.isEmpty())
        assertTrue(state.popular.isEmpty())
        assertTrue(state.continueWatching.isEmpty())
    }

    @Test
    fun `state with data`() {
        val state = HomeUiState(
            hero = listOf(Anime(1, "A", "img", "TV", 12)),
            popular = listOf(Anime(2, "B", "img", "TV", 24)),
            updated = listOf(Anime(3, "C", "img", "TV", 10)),
            isLoading = false
        )
        assertEquals(1, state.hero.size)
        assertEquals(1, state.popular.size)
        assertEquals(1, state.updated.size)
        assertEquals("A", state.hero[0].title)
    }
}
