package com.savage.anime.data.mapper

import com.savage.anime.data.network.dto.GenreDto
import com.savage.anime.domain.models.Genre

fun GenreDto.toDomain(): Genre = Genre(
    id = id,
    name = name,
    slug = slug
)
