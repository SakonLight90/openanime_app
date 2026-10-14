package com.savage.anime.data.mapper

import com.savage.anime.data.network.dto.StreamResponseDto
import com.savage.anime.domain.models.StreamResponse

fun StreamResponseDto.toDomain(): StreamResponse {
    return StreamResponse(
        success = success,
        url = url,
        error = error
    )
}
