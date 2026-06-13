# Kotlin + Android: Learning From Flutter — MovieTracker Project

> Every concept here was learned by building a real app, not from reading docs.
> Each section links to where in the project you used it.

---

## Table of Contents
1. [Project Structure & Gradle](#1-project-structure--gradle)
2. [Kotlin: Data Classes](#2-kotlin-data-classes)
3. [Kotlin: Sealed Classes](#3-kotlin-sealed-classes)
4. [Kotlin: Null Safety](#4-kotlin-null-safety)
5. [Kotlin: Extension Functions](#5-kotlin-extension-functions)
6. [Kotlin: Coroutines & suspend](#6-kotlin-coroutines--suspend)
7. [Kotlin: Flow & StateFlow](#7-kotlin-flow--stateflow)
8. [Kotlin: Higher-Order Functions & Lambdas](#8-kotlin-higher-order-functions--lambdas)
9. [Android: Application Class](#9-android-application-class)
10. [Android: Activity](#10-android-activity)
11. [Jetpack Compose: Composables & Recomposition](#11-jetpack-compose-composables--recomposition)
12. [Jetpack Compose: State — remember & collectAsState](#12-jetpack-compose-state--remember--collectasstate)
13. [Architecture: Clean Architecture + MVVM](#13-architecture-clean-architecture--mvvm)
14. [Architecture: ViewModel & viewModelScope](#14-architecture-viewmodel--viewmodelscope)
15. [Architecture: Use Cases](#15-architecture-use-cases)
16. [Architecture: Repository Pattern](#16-architecture-repository-pattern)
17. [Manual DI: AppContainer (Service Locator)](#17-manual-di-appcontainer-service-locator)
24. [Android: Intents (Launching Other Apps)](#24-android-intents-launching-other-apps)
25. [UI: Pull-to-Refresh (PullToRefreshBox)](#25-ui-pull-to-refresh-pulltorefreshbox)
18. [Navigation: Navigation Compose](#18-navigation-navigation-compose)
19. [Networking: Retrofit + OkHttp](#19-networking-retrofit--okhttp)
20. [Networking: DTOs & Mappers](#20-networking-dtos--mappers)
21. [Paging 3: Infinite Scroll](#21-paging-3-infinite-scroll)
22. [Room: Local Database](#22-room-local-database)
23. [Coil: Image Loading & Caching](#23-coil-image-loading--caching)

---

## 1. Project Structure & Gradle

### Android Concept
An Android project has two `build.gradle.kts` files:
- **Project-level** (`/build.gradle.kts`): declares plugins with `apply false` — available but not applied yet.
- **Module-level** (`/app/build.gradle.kts`): the actual app module. Applies plugins, declares dependencies, configures `android {}` block.

`libs.versions.toml` is the **version catalog** — a central file that pins every library version. Avoids version conflicts across modules.

`BuildConfig` is a generated class (enabled with `buildConfig = true`) that exposes values from your gradle file to Kotlin code. We use it to securely expose the API key.

### Flutter Equivalent
- `pubspec.yaml` = `build.gradle.kts` (dependencies) + `libs.versions.toml` (versions)
- `--dart-define=KEY=VALUE` at build time = `buildConfigField(...)` in gradle

### Key Differences
- Android has two separate gradle files (project + module); Flutter has one `pubspec.yaml`
- Android versions are centralized in TOML; Flutter uses `^1.2.3` inline
- Sensitive values go in `local.properties` (git-ignored), referenced via `BuildConfig` — never hardcode them

### When to Use
Every Android project. The TOML + BuildConfig pattern is mandatory for production apps.

### Example From This Project
- [app/build.gradle.kts](app/build.gradle.kts) — plugins, dependencies, BuildConfig field for API key
- [gradle/libs.versions.toml](gradle/libs.versions.toml) — all version pins
- [local.properties](local.properties) — API key (git-ignored)

### Further Reading
- https://developer.android.com/build/migrate-to-catalogs
- https://developer.android.com/build/gradle-overview

---

## 2. Kotlin: Data Classes

### Android Concept
```kotlin
data class Movie(
    val id: Int,
    val title: String,
    val posterPath: String?,
)
```
`data class` auto-generates: `equals()`, `hashCode()`, `toString()`, `copy()`.
`val` = immutable property (like `final` in Java).
`var` = mutable property.

### Flutter Equivalent
```dart
class Movie {
  final int id;
  final String title;
  const Movie({required this.id, required this.title});
}
```
Kotlin's `data class` saves you from writing `==`, `hashCode`, and `copyWith` manually.

### Key Differences
- `copy(title = "New Title")` in Kotlin = `copyWith(title: "New Title")` in Dart
- Kotlin `data class` is immutable by default (`val`); Dart requires `final` keyword
- No `const` constructor in Kotlin — compile-time constants use `object` or `const val`

### When to Use
For any model/DTO/state class where value equality matters. Essentially every model.

### Common Mistakes
- Using `var` instead of `val` for model fields (breaks immutability)
- Forgetting that `data class` equality is structural, not referential

### Example From This Project
- [domain/model/Movie.kt](app/src/main/java/com/example/movietracker/domain/model/Movie.kt)
- [domain/model/MovieDetail.kt](app/src/main/java/com/example/movietracker/domain/model/MovieDetail.kt)

### Further Reading
- https://kotlinlang.org/docs/data-classes.html

---

## 3. Kotlin: Sealed Classes

### Android Concept
```kotlin
sealed class Resource<T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String) : Resource<T>()
    class Loading<T> : Resource<T>()
}
```
A sealed class is a **closed hierarchy** — the compiler knows every possible subtype at compile time. This makes `when` expressions exhaustive (no forgotten cases).

### Flutter Equivalent
```dart
// Dart doesn't have sealed classes natively (until Dart 3's sealed keyword)
// In Flutter you'd use: freezed package or manual abstract classes
sealed class Resource<T> {} // Dart 3+
```
Riverpod's `AsyncValue` is the direct equivalent of our `Resource<T>`.

### Key Differences
- Kotlin's `when` on a sealed class is exhaustive — the compiler forces you to handle every case
- In Flutter/Dart 3, you get the same with `sealed` + pattern matching; older Dart requires the `freezed` package

### When to Use
- Any time you have a fixed set of states: `Loading | Success | Error`, `Screen.Home | Screen.Detail`, `MediaFilter.ALL | MediaFilter.MOVIES`
- Navigation routes (see `Screen.kt`)

### Production Tips
- Always use `when` exhaustively (no `else` branch) so adding a new subtype causes a compile error
- Prefer `data class` subtypes so you get equality comparison for free

### Example From This Project
- [util/Resource.kt](app/src/main/java/com/example/movietracker/util/Resource.kt) — network state
- [presentation/navigation/Screen.kt](app/src/main/java/com/example/movietracker/presentation/navigation/Screen.kt) — routes

### Further Reading
- https://kotlinlang.org/docs/sealed-classes.html

---

## 4. Kotlin: Null Safety

### Android Concept
Kotlin has compile-time null safety. Every type is non-null by default.
```kotlin
val name: String = "Alice"    // cannot be null — compile error if you try
val name: String? = null      // nullable — must handle null before using
val length = name?.length     // safe call — returns null instead of crashing
val length = name ?: 0        // Elvis operator — default if null
val length = name!!.length    // force unwrap — crashes if null (avoid this)
```

### Flutter Equivalent
```dart
String name = "Alice";         // non-null (Dart null-safety)
String? name = null;           // nullable
int? length = name?.length;    // safe call
int length = name?.length ?? 0; // null coalescing
```
The syntax is almost identical. Kotlin uses `?:` where Dart uses `??`.

### Key Differences
- Kotlin `?:` (Elvis) = Dart `??` (null coalescing)
- Kotlin `?.` = Dart `?.`
- Kotlin `!!` = Dart `!` (both force-unwrap, both dangerous)
- `.orEmpty()` is a Kotlin extension that returns `""` if the string is null — very common

### When to Use
Everywhere. Kotlin null safety is one of its best features — lean into it.

### Common Mistakes
- Using `!!` excessively — it defeats the purpose of null safety
- Not handling nullable fields from API responses (poster_path can be null for some movies)

### Example From This Project
- `posterPath: String?` in every Movie model
- `releaseDate.orEmpty()` in [data/mapper/MovieMapper.kt](app/src/main/java/com/example/movietracker/data/mapper/MovieMapper.kt)

---

## 5. Kotlin: Extension Functions

### Android Concept
Extension functions add methods to existing classes without modifying them.
```kotlin
// In MovieMapper.kt
fun MovieDto.toMovie(): Movie { ... }

// Usage — called as if it's a method on MovieDto
val movie = movieDto.toMovie()
```

### Flutter Equivalent
Dart has extension methods (since Dart 2.7):
```dart
extension MovieDtoExtension on MovieDto {
  Movie toMovie() { ... }
}
```

### Key Differences
- Syntax is cleaner in Kotlin — no wrapper name required
- Kotlin extensions can be placed in any file; Dart needs a named extension block
- In this project, mappers are all extension functions — keeps DTO classes clean

### When to Use
- DTO → Domain model conversions (our mappers)
- Adding utility methods to classes you don't own (e.g., `String.isEmailValid()`)
- `Extensions.kt` for cross-cutting utilities like `MovieDetail.toMovie()`

### Production Tips
- Group related extensions in the same file (all movie mappings in `MovieMapper.kt`)
- Don't go overboard — if it's complex logic, it belongs in a use case, not an extension

### Example From This Project
- [data/mapper/MovieMapper.kt](app/src/main/java/com/example/movietracker/data/mapper/MovieMapper.kt)
- [util/Extensions.kt](app/src/main/java/com/example/movietracker/util/Extensions.kt)

### Further Reading
- https://kotlinlang.org/docs/extensions.html

---

## 6. Kotlin: Coroutines & suspend

### Android Concept
```kotlin
// suspend = this function can be paused without blocking the thread
suspend fun getMovieDetail(id: Int): MovieDetail { ... }

// Call it inside a coroutine scope
viewModelScope.launch {
    val detail = getMovieDetail(123) // suspends here, resumes when done
}
```
Coroutines are Kotlin's concurrency primitive — lightweight, structured, and cancellable.

`viewModelScope` is a CoroutineScope tied to the ViewModel lifecycle — auto-cancelled when ViewModel is cleared.

`coroutineScope { }` + `async { }` + `.await()` = parallel execution:
```kotlin
coroutineScope {
    val a = async { apiCall1() }
    val b = async { apiCall2() }
    val result1 = a.await()   // both calls run at the same time
    val result2 = b.await()
}
```

### Flutter Equivalent
```dart
// async/await in Dart
Future<MovieDetail> getMovieDetail(int id) async { ... }
await getMovieDetail(123);

// Parallel: Future.wait
final results = await Future.wait([apiCall1(), apiCall2()]);
```

### Key Differences
- Kotlin's `async`/`await` inside `coroutineScope` = Dart's `Future.wait([...])`
- Kotlin coroutines have structured concurrency — a child coroutine failure cancels the parent
- `suspend` functions look synchronous but are non-blocking — same as Dart's `async` functions
- Kotlin has `Dispatchers.IO`, `Dispatchers.Main`, `Dispatchers.Default` — explicit thread pools

### When to Use
- Any network call, DB operation, or long-running work
- `suspend` for one-shot operations (fetch data once)
- `Flow` for streams (see next section)

### Production Tips
- Always use `try/catch` around suspend calls at the repository boundary
- `viewModelScope.launch` is safe for ViewModel work — it auto-cancels
- Avoid `GlobalScope` — it has no lifecycle and can leak

### Example From This Project
- [data/repository/MovieRepositoryImpl.kt](app/src/main/java/com/example/movietracker/data/repository/MovieRepositoryImpl.kt) — `async`/`await` for parallel API calls
- [presentation/screens/detail/DetailViewModel.kt](app/src/main/java/com/example/movietracker/presentation/screens/detail/DetailViewModel.kt) — `viewModelScope.launch`

### Further Reading
- https://kotlinlang.org/docs/coroutines-overview.html
- https://developer.android.com/kotlin/coroutines

---

## 7. Kotlin: Flow & StateFlow

### Android Concept
`Flow<T>` = an async stream of values. Cold by default (only runs when collected).
```kotlin
// Emitting a stream of DB updates
fun getWatchlist(): Flow<List<Movie>>

// Consuming in ViewModel
viewModelScope.launch {
    repository.getWatchlist().collect { movies ->
        // called every time DB changes
    }
}
```

`StateFlow<T>` = a hot Flow that always holds the latest value. Used for UI state.
```kotlin
private val _uiState = MutableStateFlow(DetailUiState())
val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

// Update
_uiState.update { it.copy(isLoading = true) }
```

`combine` merges two flows:
```kotlin
combine(flow1, flow2) { a, b -> /* transform */ }
```

`stateIn` converts a cold Flow to a hot StateFlow:
```kotlin
.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue)
```

### Flutter Equivalent
```dart
// Flow = Stream<T> in Dart
Stream<List<Movie>> getWatchlist();

// StateFlow = StreamController<T> + BehaviorSubject (rxdart)
//           = ValueNotifier<T> + Riverpod's StateProvider
final stateController = StreamController<UiState>.broadcast();
```

### Key Differences
- Kotlin Flow is cold (lazy); Dart Streams are cold by default too
- StateFlow is always hot and always has a value (like BehaviorSubject)
- `.update {}` on MutableStateFlow is the Kotlin equivalent of `setState(() {})` or `state = state.copyWith(...)`
- `WhileSubscribed(5000)` — keeps the upstream alive 5s after no subscribers, surviving screen rotations

### When to Use
- `Flow` for reactive DB streams (Room always returns `Flow`)
- `StateFlow` for ViewModel UI state
- `SharedFlow` for one-time events (navigation, snackbars)

### Example From This Project
- [data/local/dao/WatchlistDao.kt](app/src/main/java/com/example/movietracker/data/local/dao/WatchlistDao.kt) — `Flow` from Room
- [presentation/screens/detail/DetailViewModel.kt](app/src/main/java/com/example/movietracker/presentation/screens/detail/DetailViewModel.kt) — `MutableStateFlow` + `.update`
- [presentation/screens/watchlist/WatchlistViewModel.kt](app/src/main/java/com/example/movietracker/presentation/screens/watchlist/WatchlistViewModel.kt) — `combine` + `stateIn`

### Further Reading
- https://kotlinlang.org/docs/flow.html
- https://developer.android.com/kotlin/flow/stateflow-and-sharedflow

---

## 8. Kotlin: Higher-Order Functions & Lambdas

### Android Concept
Functions that take other functions as parameters:
```kotlin
// Our PagingSource takes a lambda — the actual API call is injected
class MoviePagingSource(
    private val apiCall: suspend (page: Int) -> PagedResponseDto<MovieDto>
) : PagingSource<Int, Movie>()

// Usage
MoviePagingSource { page -> api.getTrendingMovies(page) }
```

`operator fun invoke()` lets you call a class like a function:
```kotlin
class GetTrendingMoviesUseCase {
    operator fun invoke(): Flow<PagingData<Movie>> = ...
}

// Usage — looks like a function call, not a method
getTrendingMovies()  // instead of getTrendingMovies.execute()
```

### Flutter Equivalent
```dart
// Higher-order functions are the same concept in Dart
typedef ApiCall = Future<PagedResponse> Function(int page);

class MoviePagingSource {
  final ApiCall apiCall;
  MoviePagingSource(this.apiCall);
}
```
`operator fun invoke()` = making a class callable like `__call__` in Python, no exact Dart equivalent.

### Example From This Project
- [data/paging/MoviePagingSource.kt](app/src/main/java/com/example/movietracker/data/paging/MoviePagingSource.kt) — lambda for API call
- All use cases use `operator fun invoke()`

---

## 9. Android: Application Class

### Android Concept
`Application` is the first object created when the app starts — before any Activity.
Used to initialize app-wide singletons (databases, analytics, crash reporting).

```kotlin
class MovieTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)  // seed our manual DI container with the app context
    }
}
```

Must be declared in `AndroidManifest.xml`:
```xml
<application android:name=".MovieTrackerApp" ...>
```

### Flutter Equivalent
The `main()` function in `main.dart` — the entry point where you initialize providers/services.

### When to Use
Any time you need to initialize something once at app start: DI setup, Firebase, Timber (logging), etc.

### Example From This Project
- [MovieTrackerApp.kt](app/src/main/java/com/example/movietracker/MovieTrackerApp.kt)

---

## 10. Android: Activity

### Android Concept
`Activity` = a single screen entry point in Android (before Compose, each screen was its own Activity).
In modern Android with Jetpack Compose, you usually have **one Activity** that hosts the entire Compose UI.

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovieTrackerTheme {
                NavGraph()
            }
        }
    }
}
```

`enableEdgeToEdge()` lets the app draw behind the status and navigation bars for a modern full-bleed look.

### Flutter Equivalent
- `Activity` = your `MaterialApp`'s scaffold shell
- `setContent { }` = `runApp(MyApp())`

### Key Differences
- Flutter has no concept of Activities — navigation is all within one widget tree
- In Android pre-Compose, each screen was a separate Activity/Fragment
- With Compose, you work like Flutter: one root entry point + declarative navigation inside

### Example From This Project
- [MainActivity.kt](app/src/main/java/com/example/movietracker/MainActivity.kt)

---

## 11. Jetpack Compose: Composables & Recomposition

### Android Concept
A `@Composable` function describes UI. Compose re-runs (recomposes) it whenever its state changes.

```kotlin
@Composable
fun MovieCard(movie: Movie, onClick: () -> Unit) {
    Card(modifier = Modifier.clickable(onClick = onClick)) {
        Text(movie.title)
    }
}
```

Key Compose layout primitives:
| Compose | Flutter |
|---------|---------|
| `Column` | `Column` |
| `Row` | `Row` |
| `Box` | `Stack` |
| `LazyColumn` | `ListView.builder` |
| `LazyVerticalGrid` | `GridView.builder` |
| `Scaffold` | `Scaffold` |
| `Card` | `Card` |

**Modifiers**: chain layout/drawing operations:
```kotlin
Modifier
    .fillMaxWidth()   // = width: double.infinity
    .padding(16.dp)   // = Padding widget
    .clip(CircleShape) // = ClipOval
    .clickable { }    // = GestureDetector / InkWell
```

### Flutter Equivalent
`@Composable` function = `Widget build(BuildContext context)`.
Recomposition = `setState` triggering a rebuild.

### Key Differences
- In Flutter, everything is a Widget (even padding). In Compose, layout is via `Modifier` chains
- Compose's recomposition is smarter — it only recomposes the composable whose state changed, not the whole tree
- No `BuildContext` in Compose — composables read state directly

### Example From This Project
- [presentation/components/MovieCard.kt](app/src/main/java/com/example/movietracker/presentation/components/MovieCard.kt)
- [presentation/screens/home/HomeScreen.kt](app/src/main/java/com/example/movietracker/presentation/screens/home/HomeScreen.kt)

### Further Reading
- https://developer.android.com/develop/ui/compose/mental-model

---

## 12. Jetpack Compose: State — remember & collectAsState

### Android Concept
```kotlin
// Local UI state that survives recomposition (but NOT configuration changes)
var selectedTab by remember { mutableIntStateOf(0) }

// Read a StateFlow from ViewModel as Compose state
val uiState by viewModel.uiState.collectAsState()
```

`remember` = stores a value across recompositions within the same composable.
`rememberSaveable` = same but also survives screen rotation (saves to Bundle).
`collectAsState()` = subscribes to a Flow/StateFlow and triggers recomposition on each emission.

### Flutter Equivalent
```dart
// remember = local variable in StatefulWidget's State
int selectedTab = 0;

// collectAsState = watching a Riverpod provider
final uiState = ref.watch(viewModelProvider);
```

### Key Differences
- `remember` is just for ephemeral local UI state — like which tab is selected
- For state that must survive rotation, use `rememberSaveable` or ViewModel state
- ViewModel `StateFlow` + `collectAsState` is the standard pattern for screen-level state

### Example From This Project
- `var selectedTab by remember { mutableIntStateOf(0) }` in [HomeScreen.kt](app/src/main/java/com/example/movietracker/presentation/screens/home/HomeScreen.kt)
- `val uiState by viewModel.uiState.collectAsState()` in [DetailScreen.kt](app/src/main/java/com/example/movietracker/presentation/screens/detail/DetailScreen.kt)

---

## 13. Architecture: Clean Architecture + MVVM

### Android Concept
Three layers with a strict one-way dependency rule:

```
Presentation  →  Domain  ←  Data
(UI/ViewModel)  (models,    (Retrofit,
                interfaces, Room, repo
                use cases)  impls)
```

The Domain layer is pure Kotlin — no Android imports. This makes it testable without a device.

### Flutter Equivalent
This is identical to the Clean Architecture you'd use with Flutter:
- Domain = your plain Dart models + abstract repository interfaces
- Data = your http/Hive implementations
- Presentation = your widgets + providers/ViewModels

### Why This Matters For Interviews
You can explain: "The domain layer has zero Android dependencies. I can unit-test every use case with pure Kotlin. The data layer implements the domain interfaces — if I swap Retrofit for GraphQL tomorrow, nothing in domain or presentation changes."

### Example From This Project
- [domain/](app/src/main/java/com/example/movietracker/domain/) — pure Kotlin, no Android
- [data/](app/src/main/java/com/example/movietracker/data/) — Android/library implementations
- [presentation/](app/src/main/java/com/example/movietracker/presentation/) — Compose UI

---

## 14. Architecture: ViewModel & viewModelScope

### Android Concept
`ViewModel` survives configuration changes (screen rotation). This is crucial on Android — without it, rotating the screen would restart your API calls.

```kotlin
class HomeViewModel(
    getTrendingMovies: GetTrendingMoviesUseCase,
    getTrendingShows: GetTrendingShowsUseCase
) : ViewModel() {
    val trendingMovies = getTrendingMovies().cachedIn(viewModelScope)
    val trendingShows = getTrendingShows().cachedIn(viewModelScope)
}
```

`viewModelScope` is a `CoroutineScope` automatically cancelled when the ViewModel is cleared (when the user permanently leaves the screen).

**Creating a ViewModel with manual DI (no Hilt):**
```kotlin
// In a @Composable, pass dependencies from AppContainer via the viewModel { } factory lambda
val vm: HomeViewModel = viewModel {
    HomeViewModel(AppContainer.getTrendingMovies, AppContainer.getTrendingShows)
}
```
The `viewModel { }` factory block runs once and is retained across recompositions and rotations — same lifecycle guarantee as Hilt's `hiltViewModel()`.

### Flutter Equivalent
`ChangeNotifier` / `StateNotifier` / Riverpod `NotifierProvider` — Flutter doesn't have the rotation problem because it rebuilds the whole widget tree from scratch on rotation and manages state in providers that outlive widgets.

### Key Differences
- In Android, `ViewModel` is THE official solution to surviving rotation
- `cachedIn(viewModelScope)` on a `PagingData` Flow is mandatory — without it paging restarts on every recomposition
- `viewModelScope` vs `GlobalScope`: always use `viewModelScope` — it auto-cancels, GlobalScope leaks

### Example From This Project
- [presentation/screens/home/HomeViewModel.kt](app/src/main/java/com/example/movietracker/presentation/screens/home/HomeViewModel.kt)

---

## 15. Architecture: Use Cases

### Android Concept
A Use Case (also called Interactor) is a single-responsibility class that wraps one business operation. It sits in the domain layer.

```kotlin
class GetTrendingMoviesUseCase(
    private val repository: MovieRepository
) {
    operator fun invoke(): Flow<PagingData<Movie>> = repository.getTrendingMovies()
}
```

### Flutter Equivalent
In Flutter with Clean Architecture you'd also write use cases. With Riverpod they're sometimes inlined in providers.

### Why They Exist
- ViewModels stay thin — they orchestrate, they don't contain business logic
- Use cases are independently testable
- If two ViewModels need the same logic, they share the use case

### Production Tips
For simple delegating use cases like these, some teams skip them and call the repository directly from the ViewModel. Both are valid — use cases shine when the logic is more complex (combining multiple repos, validation, transformations).

### Example From This Project
- All files in [domain/usecase/](app/src/main/java/com/example/movietracker/domain/usecase/)

---

## 16. Architecture: Repository Pattern

### Android Concept
The repository is the **single source of truth** for data. It decides: should I fetch from the network or return cached data?

```kotlin
// Interface in domain — the contract (pure Kotlin, no Android dependencies)
interface MovieRepository {
    suspend fun getMovieDetail(id: Int, mediaType: String): Resource<MovieDetail>
}

// Implementation in data — the actual logic
class MovieRepositoryImpl(private val api: TmdbApi) : MovieRepository {
    override suspend fun getMovieDetail(id: Int, mediaType: String): Resource<MovieDetail> = try {
        Resource.Success(api.getMovieDetail(id).toMovieDetail(...))
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Error")
    }
}
```

The interface lives in domain. The implementation lives in data. Our `AppContainer` object wires them together (previously Hilt's job).

### Flutter Equivalent
Same pattern. You'd have `abstract class MovieRepository` in domain and `MovieRepositoryImpl` in data.

### Example From This Project
- [domain/repository/MovieRepository.kt](app/src/main/java/com/example/movietracker/domain/repository/MovieRepository.kt)
- [data/repository/MovieRepositoryImpl.kt](app/src/main/java/com/example/movietracker/data/repository/MovieRepositoryImpl.kt)

---

## 17. Manual DI: AppContainer (Service Locator)

### Android Concept
Dependency Injection (DI) is the practice of passing dependencies into a class rather than constructing them inside it. This makes classes testable and swappable.

We use a **manual service locator** (`AppContainer`) instead of Hilt because Hilt (the standard Android DI framework) had compatibility issues with AGP 9.x during this project. The pattern is identical conceptually.

```kotlin
// di/AppContainer.kt
object AppContainer {
    private lateinit var appContext: Context
    fun init(context: Context) { appContext = context.applicationContext }

    // by lazy = the object is only created on first access, then cached forever
    private val retrofit by lazy { /* Retrofit setup */ }
    val tmdbApi: TmdbApi by lazy { retrofit.create(TmdbApi::class.java) }
    val database: MovieDatabase by lazy { Room.databaseBuilder(...).build() }

    val movieRepository: MovieRepository by lazy { MovieRepositoryImpl(tmdbApi) }

    val getTrendingMovies by lazy { GetTrendingMoviesUseCase(movieRepository) }
    // ... all use cases
}
```

Initialized once at app start in `MovieTrackerApp.onCreate()`, then every screen reads from it.

### Flutter Equivalent
- `get_it` package — exactly the same pattern: register singletons once at startup, `GetIt.instance<T>()` to retrieve
- `AppContainer.init(this)` = `GetIt.instance.registerLazySingleton(() => ...)`

### Key Differences vs Hilt
| | AppContainer (manual) | Hilt |
|---|---|---|
| Type safety | ✅ compile-time | ✅ compile-time |
| AGP 9.x compatible | ✅ yes | ⚠️ requires Hilt 2.59+ |
| Scoping | manual (`object` = app scope) | `@Singleton`, `@ViewModelScoped` |
| Boilerplate | medium | low (annotations do the work) |
| Testability | good (swap impl in test) | great (automatic test component) |

### by lazy — The Key Kotlin Detail
```kotlin
val tmdbApi: TmdbApi by lazy { retrofit.create(TmdbApi::class.java) }
```
`by lazy` is a Kotlin property delegate. The lambda runs only on first access, result is cached.
Flutter equivalent: `late final tmdbApi = retrofit.create(TmdbApi);`

### Example From This Project
- [di/AppContainer.kt](app/src/main/java/com/example/movietracker/di/AppContainer.kt)
- [MovieTrackerApp.kt](app/src/main/java/com/example/movietracker/MovieTrackerApp.kt)

---

## 18. Navigation: Navigation Compose

### Android Concept
```kotlin
val navController = rememberNavController()

NavHost(navController, startDestination = "home") {
    composable("home") { HomeScreen() }
    composable("detail/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) {
        val id = it.arguments?.getInt("id") ?: 0
        DetailScreen(movieId = id)
    }
}

// Navigate
navController.navigate("detail/123")
// Back
navController.popBackStack()
```

**Back stack management** — `popUpTo` with `saveState`/`restoreState` prevents duplicate screens and restores scroll position when switching tabs.

### Flutter Equivalent
- `navController` = `GoRouter` / `NavigatorState`
- Route strings = GoRouter's paths
- `navArgument` = GoRouter's `:id` path params + `.params`
- `popBackStack()` = `Navigator.pop(context)` / `context.pop()`

### Key Differences
- In Flutter (GoRouter), routes are declared as a tree. In Navigation Compose, they're declared as a flat list inside `NavHost`
- Type-safe navigation arguments in Navigation Compose are verbose — you must declare `navArgument` explicitly
- Newer Navigation 2.8+ supports type-safe routes with Kotlin serialization (worth learning next)

### Example From This Project
- [presentation/navigation/NavGraph.kt](app/src/main/java/com/example/movietracker/presentation/navigation/NavGraph.kt)
- [presentation/navigation/Screen.kt](app/src/main/java/com/example/movietracker/presentation/navigation/Screen.kt)

### Further Reading
- https://developer.android.com/develop/ui/compose/navigation

---

## 19. Networking: Retrofit + OkHttp

### Android Concept
**Retrofit** turns a Kotlin interface into a fully functional HTTP client:
```kotlin
interface TmdbApi {
    @GET("movie/{id}")
    suspend fun getMovieDetail(@Path("id") id: Int): MovieDetailDto
}

// Usage
val detail = api.getMovieDetail(123)  // that's it — Retrofit does the rest
```

**OkHttp** is the HTTP engine underneath Retrofit. You customize it via Interceptors.

**Interceptors** run on every request — like middleware:
```kotlin
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url.newBuilder()
            .addQueryParameter("api_key", apiKey)
            .build()
        return chain.proceed(chain.request().newBuilder().url(url).build())
    }
}
```

### Flutter Equivalent
- Retrofit = `dio` with generated code, or `http` package with manual parsing
- OkHttp Interceptor = `dio` Interceptor / `http` middleware
- `@GET`, `@POST`, `@Path`, `@Query` = dio's path/query parameters

### Key Differences
- Retrofit uses annotations + code generation; dio uses method calls
- `suspend fun` in Retrofit = `async` in Dart — both are non-blocking
- Gson (`@SerializedName`) = `json_serializable` / `fromJson` in Flutter

### Example From This Project
- [data/remote/api/TmdbApi.kt](app/src/main/java/com/example/movietracker/data/remote/api/TmdbApi.kt)
- [data/remote/interceptor/AuthInterceptor.kt](app/src/main/java/com/example/movietracker/data/remote/interceptor/AuthInterceptor.kt)
- [di/NetworkModule.kt](app/src/main/java/com/example/movietracker/di/NetworkModule.kt)

---

## 20. Networking: DTOs & Mappers

### Android Concept
**DTO** (Data Transfer Object): a class shaped exactly like the API JSON. Lives in the data layer. Uses `@SerializedName` to map snake_case JSON to camelCase Kotlin.

**Mapper**: an extension function that converts DTO → Domain model.

```kotlin
// DTO — shaped like the API
data class MovieDto(
    @SerializedName("vote_average") val voteAverage: Double,
)

// Mapper — converts to clean domain model
fun MovieDto.toMovie() = Movie(voteAverage = voteAverage)
```

### Flutter Equivalent
```dart
// DTO = your model with fromJson factory
factory MovieDto.fromJson(Map<String, dynamic> json) =>
    MovieDto(voteAverage: json['vote_average']);

// Mapper = a method that converts to your domain model
Movie toMovie() => Movie(voteAverage: voteAverage);
```

### Why Separate DTOs From Domain Models?
If TMDB renames `vote_average` to `rating` tomorrow, you only change the DTO. The domain model and all presentation code is unaffected.

### Example From This Project
- [data/remote/dto/](app/src/main/java/com/example/movietracker/data/remote/dto/) — all DTOs
- [data/mapper/MovieMapper.kt](app/src/main/java/com/example/movietracker/data/mapper/MovieMapper.kt)

---

## 21. Paging 3: Infinite Scroll

### Android Concept
Paging 3 handles: page loading, error/retry states, loading indicators, deduplication, and caching — automatically.

```kotlin
// 1. PagingSource: defines how to load a page
class MoviePagingSource(
    private val apiCall: suspend (Int) -> PagedResponseDto<MovieDto>
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
}

// 2. Pager: wraps PagingSource into a Flow
Pager(PagingConfig(pageSize = 20)) { MoviePagingSource(...) }.flow

// 3. ViewModel caches it
val movies = getTrendingMovies().cachedIn(viewModelScope)

// 4. UI consumes it
val movies = viewModel.movies.collectAsLazyPagingItems()
items(count = movies.itemCount) { index -> movies[index]?.let { MovieCard(it) } }
```

### Flutter Equivalent
No direct equivalent in Flutter core. You'd build this manually:
- Track `page` variable
- `ScrollController` listener to detect bottom
- Load next page, append to list
- Handle loading/error states manually

Paging 3 replaces all of that boilerplate.

### Key States
- `loadState.refresh` = first load (or pull-to-refresh)
- `loadState.append` = loading next page while scrolling
- `LoadState.Loading` / `LoadState.Error` / `LoadState.NotLoading` = the three possible states

### Pull-to-Refresh With Paging 3
Paging 3 has built-in support for refresh — call `items.refresh()` and the library re-fetches from page 1:
```kotlin
// isRefreshing = loading AND we already have content (skip on initial load)
val isRefreshing = items.loadState.refresh is LoadState.Loading && items.itemCount > 0

PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = { items.refresh() }) {
    LazyVerticalGrid(...) { /* same as normal */ }
}
```

### Production Tips
- `cachedIn(viewModelScope)` is MANDATORY — without it, paging restarts on every recomposition
- `enablePlaceholders = false` simplifies the item count logic for most use cases
- Check `items.itemCount > 0` before treating `LoadState.Loading` as a pull-to-refresh — otherwise the initial load also triggers the spinner twice

### Example From This Project
- [data/paging/MoviePagingSource.kt](app/src/main/java/com/example/movietracker/data/paging/MoviePagingSource.kt)
- [presentation/screens/home/HomeScreen.kt](app/src/main/java/com/example/movietracker/presentation/screens/home/HomeScreen.kt) — `loadState` handling + pull-to-refresh

---

## 22. Room: Local Database

### Android Concept
Room is an SQLite wrapper that uses annotations to generate all boilerplate SQL.

```kotlin
// 1. Entity = table definition
@Entity(tableName = "watchlist")
data class WatchlistEntity(@PrimaryKey val id: Int, val title: String)

// 2. DAO = query interface
@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist")
    fun getAll(): Flow<List<WatchlistEntity>>  // reactive! auto-updates on DB change

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WatchlistEntity)
}

// 3. Database
@Database(entities = [WatchlistEntity::class], version = 1)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao
}
```

Room auto-generates the SQL implementation at compile time (via KSP).

### Flutter Equivalent
| Room | Flutter |
|------|---------|
| `@Entity` | Hive `@HiveType` / Isar `@collection` |
| `@Dao` | Hive `Box<T>` methods / Isar `IsarCollection` |
| `@Database` | `Hive.openBox()` / `Isar.open()` |
| `Flow<List<T>>` | `Hive.watch()` / `Isar.watchLazy()` |

### Key Difference
Room's DAO returns `Flow<List<T>>` — when you insert/delete, all collectors are automatically notified. In Hive you'd use `box.watch()` for similar behavior.

### When to Add a Migration
When you add/remove columns or tables, increment `version` in `@Database` and provide a `Migration` object. Room will throw an error at runtime if the schema changed without a migration.

### Example From This Project
- [data/local/entity/WatchlistEntity.kt](app/src/main/java/com/example/movietracker/data/local/entity/WatchlistEntity.kt)
- [data/local/dao/WatchlistDao.kt](app/src/main/java/com/example/movietracker/data/local/dao/WatchlistDao.kt)
- [data/local/MovieDatabase.kt](app/src/main/java/com/example/movietracker/data/local/MovieDatabase.kt)
- [di/DatabaseModule.kt](app/src/main/java/com/example/movietracker/di/DatabaseModule.kt)

### Further Reading
- https://developer.android.com/training/data-storage/room

---

## 23. Coil: Image Loading & Caching

### Android Concept
Coil loads images from URLs, caches them (memory + disk), and integrates with Compose:

```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data("https://image.tmdb.org/t/p/w500/poster.jpg")
        .crossfade(true)
        .build(),
    contentDescription = null,
    contentScale = ContentScale.Crop,
    modifier = Modifier.fillMaxWidth()
)
```

### Flutter Equivalent
- `AsyncImage` = `CachedNetworkImage` widget
- `crossfade(true)` = `fadeInDuration`
- `contentScale = ContentScale.Crop` = `BoxFit.cover`
- `LocalContext.current` = just how you access Android context in Compose — no Flutter equivalent needed

### Production Tips
- Always set `contentDescription` for accessibility (we pass `null` for decorative images)
- For large lists, Coil's memory cache prevents re-loading already-seen posters
- `crossfade(true)` prevents the jarring "pop" when an image loads

### Example From This Project
- [presentation/components/PosterImage.kt](app/src/main/java/com/example/movietracker/presentation/components/PosterImage.kt)
- [presentation/screens/detail/DetailScreen.kt](app/src/main/java/com/example/movietracker/presentation/screens/detail/DetailScreen.kt) — backdrop + cast profile images

### Further Reading
- https://coil-kt.github.io/coil/compose/

---

---

## 24. Android: Intents (Launching Other Apps)

### Android Concept
An `Intent` is Android's inter-component messaging system. You use it to start another app, open a URL, share content, or start a camera. It's Android's equivalent of a deep-link or URL scheme handler.

```kotlin
// Launch the YouTube app with a specific video key
val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$key"))
// Fallback: open in the browser if YouTube isn't installed
val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$key"))

try {
    context.startActivity(appIntent)
} catch (e: ActivityNotFoundException) {
    context.startActivity(webIntent)
}
```

`Intent.ACTION_VIEW` = "open/display this data". The OS finds the right app based on the URI scheme.
`ActivityNotFoundException` = the declared app to handle this intent isn't installed.

### Flutter Equivalent
```dart
// url_launcher package
if (await canLaunchUrl(Uri.parse('vnd.youtube:$key'))) {
    await launchUrl(Uri.parse('vnd.youtube:$key'));
} else {
    await launchUrl(Uri.parse('https://www.youtube.com/watch?v=$key'));
}
```
Flutter's `url_launcher` does the same job, but Android's native Intent system is built-in — no package needed.

### Key Differences
- Android: `context.startActivity(intent)` — synchronous call, OS resolves the handler
- Flutter: `url_launcher` async with `canLaunchUrl` check — cross-platform abstraction
- Android gives you more control: explicit intents (target specific app) vs implicit intents (OS picks)

### When to Use
- Opening links, maps, phone dialer, email, other apps
- Sharing content (`Intent.ACTION_SEND`)
- Camera/gallery (`MediaStore` intents)

### Example From This Project
- [presentation/screens/detail/DetailScreen.kt](app/src/main/java/com/example/movietracker/presentation/screens/detail/DetailScreen.kt) — YouTube trailer launch

---

## 25. UI: Pull-to-Refresh (PullToRefreshBox)

### Android Concept
Material3 provides `PullToRefreshBox` — a container that intercepts an overscroll gesture and shows a circular refresh indicator at the top.

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyScreen() {
    PullToRefreshBox(
        isRefreshing = viewModel.isRefreshing,   // drives the spinner
        onRefresh = { viewModel.refresh() }       // called when user releases
    ) {
        LazyColumn { /* content */ }  // must be a scrollable composable
    }
}
```

The `isRefreshing` flag controls the spinner animation. Set it to `false` in your ViewModel once the data load completes, or the spinner spins forever.

### Flutter Equivalent
```dart
RefreshIndicator(
    onRefresh: () async {
        await viewModel.refresh();
    },
    child: ListView.builder(...),
)
```
Essentially identical API. Flutter's `RefreshIndicator` = Compose's `PullToRefreshBox`.

### Key Differences
- Both wrap a scrollable child and intercept the overscroll gesture
- `@OptIn(ExperimentalMaterial3Api::class)` is needed — the API is stable but officially still experimental in Material3
- For Paging 3: use `items.refresh()` as the onRefresh action, but gate `isRefreshing` with `items.itemCount > 0` so the initial load uses a full-screen spinner instead

### The isRefreshing Pattern for Detail Screens
When a screen already has content and the user pulls to refresh, you want to:
1. Keep the existing content visible underneath the spinner
2. Only replace the content once the new data arrives

```kotlin
// In ViewModel:
val isRefresh = _uiState.value.movieDetail != null  // already have data?
_uiState.update {
    if (isRefresh) it.copy(isRefreshing = true)   // spinner at top, keep content
    else it.copy(isLoading = true)                 // full-screen spinner on first load
}
```

### Example From This Project
- [presentation/screens/home/HomeScreen.kt](app/src/main/java/com/example/movietracker/presentation/screens/home/HomeScreen.kt) — paging pull-to-refresh
- [presentation/screens/detail/DetailScreen.kt](app/src/main/java/com/example/movietracker/presentation/screens/detail/DetailScreen.kt) — content-preserving refresh
- [presentation/screens/detail/DetailViewModel.kt](app/src/main/java/com/example/movietracker/presentation/screens/detail/DetailViewModel.kt) — `isRefreshing` vs `isLoading` logic

---

## 26. Material3 Theming: Color Schemes, Typography & SplashScreen

### Android Concept

Material3 theming in Compose has three layers:
1. **Color scheme** — a `darkColorScheme()` / `lightColorScheme()` object that maps semantic roles (`primary`, `background`, `surface`, `onSurface`, etc.) to actual colors
2. **Typography** — a `Typography` object with 15 named text styles (`displayLarge` through `labelSmall`)
3. **Theme composable** — `MaterialTheme(colorScheme = ..., typography = ...) { content() }`

The key insight: **disable dynamic color**. Android 12+ auto-generates a color scheme from the user's wallpaper. For a branded app (like a movie tracker with a cinematic gold palette), you always want to remove `dynamicColor = true` and control the scheme yourself.

```kotlin
// ❌ Default boilerplate — overrides your palette on Android 12+
val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        dynamicDarkColorScheme(context)  // wallpaper colors — ignores your design
    }
    ...
}

// ✓ Correct — always use your own scheme
val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
```

**SideEffect for status bar**: The status bar color must be set imperatively via `WindowCompat`. Since this is a platform side effect (outside Compose), you do it inside a `SideEffect` block — which runs after every successful recomposition:

```kotlin
val view = LocalView.current
SideEffect {
    val window = (view.context as Activity).window
    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
}
```

**Theme toggle**: Hoist a `Boolean?` state in `MainActivity` (`null` = follow system, `true/false` = user override). Pass `isDark` and `onToggleTheme` down to wherever you need the toggle button.

```kotlin
var themeOverride by rememberSaveable { mutableStateOf<Boolean?>(null) }
val isDark = themeOverride ?: isSystemInDarkTheme()
MovieTrackerTheme(darkTheme = isDark) {
    NavGraph(isDark = isDark, onToggleTheme = { themeOverride = !isDark })
}
```

**SplashScreen API** (`androidx.core:core-splashscreen`): Call `installSplashScreen()` *before* `super.onCreate()`. Point the activity theme at `Theme.SplashScreen` in `themes.xml`. The splash screen briefly shows your icon on a solid background, then transitions to your app automatically.

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()   // must be first
    super.onCreate(savedInstanceState)
    ...
}
```

### Flutter Equivalent
- `ThemeData(colorScheme: ColorScheme.dark(...))` = Compose's `darkColorScheme(...)`
- `ThemeData.brightness == Brightness.dark` = `isSystemInDarkTheme()` in Compose
- Flutter's `ValueNotifier<ThemeMode>` at the top of the widget tree = Compose's hoisted `Boolean?` state in `MainActivity`
- Flutter's `flutter_native_splash` package = Android's SplashScreen API
- Flutter's `SystemChrome.setSystemUIOverlayStyle()` = Compose's `SideEffect { WindowCompat.getInsetsController(...) }`

### Key Differences
- In Flutter, a `TextTheme` has ~10 named styles; Material3 Compose `Typography` has 15 (3 levels each of display, headline, title, body, label)
- Android's `FontFamily.Serif` / `FontFamily.SansSerif` are system font stacks — similar to Flutter's `GoogleFonts` fallback behavior
- `rememberSaveable` survives process death and config changes (rotation); `remember` doesn't — always use `rememberSaveable` for user preferences like theme choice

### Design System Built for This App
- **Dark background**: `#0B0C14` (deep midnight blue-black)
- **Primary (gold)**: `#F0A500` dark / `#7A5000` light — gold star/accent, status bar, FABs
- **Surface layers**: 4 steps (`#12131F` → `#1C1D2E` → `#212235`) for card depth
- **Typography**: Serif for headlines/display (cinematic), SansSerif for body/labels

### Example From This Project
- [ui/theme/Color.kt](app/src/main/java/com/example/movietracker/ui/theme/Color.kt)
- [ui/theme/Theme.kt](app/src/main/java/com/example/movietracker/ui/theme/Theme.kt)
- [ui/theme/Type.kt](app/src/main/java/com/example/movietracker/ui/theme/Type.kt)
- [MainActivity.kt](app/src/main/java/com/example/movietracker/MainActivity.kt)

---

## 27. Compose UI Polish: Full-Bleed Cards, Gradient Overlays & Logo Components

### Android Concept

**Full-bleed cards with gradient scrim**: Instead of a `Column(poster + text below)`, use a `Box` where the poster fills the entire card and text is overlaid with a gradient:

```kotlin
Box(modifier = Modifier.aspectRatio(2f/3f)) {
    AsyncImage(modifier = Modifier.matchParentSize(), ...)  // full-bleed poster

    Box(
        modifier = Modifier
            .matchParentSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                    startY = 0.45f * Float.MAX_VALUE  // start gradient ~45% down
                )
            )
    )

    // White text at bottom-left, readable over any poster
    Column(modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)) {
        Text(movie.title, color = Color.White)
        RatingBar(rating = movie.voteAverage, textColor = Color.White)
    }
}
```

`Float.MAX_VALUE` as gradient `startY`/`endY` means "use the full height of the composable" — the gradient engine normalises it. Flutter uses `Alignment` + `LinearGradient` the same way.

**Composable logo component**: When branding requires a specific typographic treatment (different fonts, spacing, colors for different words), extract it as its own composable rather than repeating the styling everywhere:

```kotlin
@Composable
fun MovieTrackerLogo(modifier: Modifier = Modifier) {
    Column(modifier) {
        Text("MOVIE", style = ..., color = MaterialTheme.colorScheme.primary)   // gold, serif, wide
        Text("TRACKER", style = ..., color = MaterialTheme.colorScheme.onSurfaceVariant)  // grey, light
    }
}
```

**Outlined vs Filled nav icons**: The standard Material pattern is filled icon = selected tab, outlined = unselected. Use `Icons.Filled.*` and `Icons.Outlined.*` (from `material-icons-extended`):

```kotlin
if (selected) Icons.Filled.Home else Icons.Outlined.Home
```

### Flutter Equivalent
- Compose `Box` with `Modifier.matchParentSize()` = Flutter `Stack` with `Positioned.fill()`
- Compose `Brush.verticalGradient` = Flutter `LinearGradient` in a `BoxDecoration`
- Compose `Modifier.background(brush)` = Flutter `Container(decoration: BoxDecoration(gradient: ...))`
- Nav icons: Flutter's `BottomNavigationBarItem(icon: Icon(...), activeIcon: Icon(...))` = Compose's conditional `if (selected) filledIcon else outlinedIcon`

### Example From This Project
- [presentation/components/MovieCard.kt](app/src/main/java/com/example/movietracker/presentation/components/MovieCard.kt)
- [presentation/components/MovieTrackerLogo.kt](app/src/main/java/com/example/movietracker/presentation/components/MovieTrackerLogo.kt)
- [presentation/navigation/NavGraph.kt](app/src/main/java/com/example/movietracker/presentation/navigation/NavGraph.kt)

---

*This document grows as the project grows. Next up: animations, testing, DataStore preferences.*
