package com.example.movietracker.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.movietracker.data.mapper.toMovie
import com.example.movietracker.data.remote.dto.PagedResponseDto
import com.example.movietracker.data.remote.dto.ShowDto
import com.example.movietracker.domain.model.Movie

class ShowPagingSource(
    private val apiCall: suspend (page: Int) -> PagedResponseDto<ShowDto>
) : PagingSource<Int, Movie>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: 1
        return try {
            val response = apiCall(page)
            LoadResult.Page(
                data = response.results.map { it.toMovie() },
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (page < response.totalPages) page + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? =
        state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
}
