package com.example.movietracker.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.movietracker.data.mapper.toMovie
import com.example.movietracker.data.remote.dto.MovieDto
import com.example.movietracker.data.remote.dto.PagedResponseDto
import com.example.movietracker.domain.model.Movie

// PagingSource is the engine behind Paging 3's infinite scroll.
// Flutter equivalent: a custom ScrollController + page tracking variable,
// but here Android handles all the page bookkeeping and retry logic for you.
//
// The lambda pattern means one class works for both trending movies AND search results.
class MoviePagingSource(
    private val apiCall: suspend (page: Int) -> PagedResponseDto<MovieDto>
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

    // Called when the list is refreshed and Paging needs to know which page to start from.
    // anchorPosition = the last visible item index before refresh.
    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? =
        state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
}
