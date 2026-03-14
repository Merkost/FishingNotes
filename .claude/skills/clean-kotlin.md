# Clean Kotlin Code Style

You are writing Kotlin code following Clean Architecture principles with a minimal, professional style. Use FlowMVI for state management.

## Architecture Rules

### Layer Structure
- **Core**: Utilities, extensions, common types (no dependencies on other layers)
- **Domain**: Models, repository interfaces, use cases (depends only on Core)
- **Data**: DTOs, mappers, data sources, repository implementations (depends on Domain, Core)
- **Presentation**: UI, ViewModels, navigation (depends on Domain, Core)

### Dependency Rules
| Layer | Can Depend On | Cannot Depend On |
|-------|---------------|------------------|
| Presentation | Domain, Core | Data |
| Domain | Core | Presentation, Data |
| Data | Domain, Core | Presentation |
| Core | Nothing | Any other layer |

## Dependencies

Add FlowMVI to your project:
```toml
[versions]
flowmvi = "3.1.0"

[libraries]
flowmvi-core = { module = "pro.respawn.flowmvi:core", version.ref = "flowmvi" }
flowmvi-compose = { module = "pro.respawn.flowmvi:compose", version.ref = "flowmvi" }
```

---

## Code Style Rules

### NO Comments
- Do NOT write code comments
- Do NOT write KDoc unless it's a public API that requires documentation
- Let the code speak for itself through clear naming

### Self-Documenting Code
- Use descriptive function and variable names
- Prefer small, single-purpose functions
- Extract complex logic into well-named private functions

### Kotlin Idioms
```kotlin
// GOOD: Expressive, no comments needed
data class Question(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctIndex: Int
) {
    val correctAnswer: String get() = options[correctIndex]
    val isValid: Boolean get() = options.size >= 2 && correctIndex in options.indices
}

// BAD: Comments explaining obvious code
data class Question(
    val id: String, // The question ID
    val text: String, // The question text
    val options: List<String>, // List of answer options
    val correctIndex: Int // Index of correct answer
)
```

### Use Cases
- Single responsibility, single public method
- Use `operator fun invoke()` for execution
- Inject repository interfaces, not implementations

```kotlin
class GetRandomQuestionsUseCase(
    private val repository: QuestionRepository
) {
    suspend operator fun invoke(count: Int): Result<List<Question>> =
        repository.getRandomQuestions(count)
}
```

### FlowMVI State Management

Use FlowMVI library for MVI pattern. Each feature has:
- **State**: Immutable data class with `MVIState`
- **Intent**: Sealed interface with `MVIIntent` for user actions
- **Action**: Sealed interface with `MVIAction` for one-time side effects

#### Container Definition
```kotlin
class PracticeContainer(
    private val getRandomQuestions: GetRandomQuestionsUseCase,
    private val recordAnswer: RecordAnswerUseCase
) : Container<PracticeState, PracticeIntent, PracticeAction> {

    override val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun PipelineContext<PracticeState, PracticeIntent, PracticeAction>.reduce(intent: PracticeIntent) {
        when (intent) {
            is PracticeIntent.AnswerSelected -> handleAnswerSelected(intent.index)
            PracticeIntent.NextQuestion -> loadNextQuestion()
            PracticeIntent.Exit -> action(PracticeAction.NavigateBack)
        }
    }

    private fun PipelineContext<PracticeState, PracticeIntent, PracticeAction>.handleAnswerSelected(index: Int) {
        val question = state.currentQuestion ?: return
        val isCorrect = index == question.correctIndex

        launch {
            recordAnswer(question.id, isCorrect)
        }

        updateState {
            copy(
                selectedAnswer = index,
                answerRevealed = true,
                correctAnswers = if (isCorrect) correctAnswers + 1 else correctAnswers
            )
        }
    }

    private fun PipelineContext<PracticeState, PracticeIntent, PracticeAction>.loadNextQuestion() {
        launchForState {
            updateState { copy(isLoading = true, selectedAnswer = null, answerRevealed = false) }
            getRandomQuestions(1)
                .onSuccess { questions ->
                    updateState {
                        copy(
                            currentQuestion = questions.firstOrNull(),
                            isLoading = false,
                            questionsAnswered = questionsAnswered + 1
                        )
                    }
                }
                .onError { e ->
                    updateState { copy(error = e.message, isLoading = false) }
                }
        }
    }
}
```

#### State, Intent, Action Definitions
```kotlin
@Immutable
data class PracticeState(
    val currentQuestion: Question? = null,
    val selectedAnswer: Int? = null,
    val answerRevealed: Boolean = false,
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
) : MVIState

sealed interface PracticeIntent : MVIIntent {
    data class AnswerSelected(val index: Int) : PracticeIntent
    data object NextQuestion : PracticeIntent
    data object Exit : PracticeIntent
}

sealed interface PracticeAction : MVIAction {
    data object NavigateBack : PracticeAction
}
```

#### Screen Integration
```kotlin
@Composable
fun PracticeScreen(
    onNavigateBack: () -> Unit
) {
    val container = koinInject<PracticeContainer>()

    subscribe(container) { action ->
        when (action) {
            PracticeAction.NavigateBack -> onNavigateBack()
        }
    }

    val state by container.states.collectAsStateWithLifecycle()

    PracticeContent(
        state = state,
        onIntent = container::intent
    )
}

@Composable
private fun PracticeContent(
    state: PracticeState,
    onIntent: (PracticeIntent) -> Unit
) {
    // UI implementation
}
```

#### Koin DI for Containers
```kotlin
val presentationModule = module {
    factory { PracticeContainer(get(), get()) }
    factory { HomeContainer(get(), get()) }
    factory { MockTestContainer(get(), get()) }
}
```

### Composables
- Extract reusable components to `components/` package
- Use `Modifier` as first optional parameter
- Keep composables focused and small

```kotlin
@Composable
fun HonqButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: HonqButtonVariant = HonqButtonVariant.Primary,
    enabled: Boolean = true
)
```

### Koin DI Modules
- Separate module per layer: `coreModule`, `dataModule`, `domainModule`, `presentationModule`
- Use `single` for singletons, `factory` for use cases and containers

```kotlin
val domainModule = module {
    factory { GetRandomQuestionsUseCase(get()) }
    factory { RecordAnswerUseCase(get()) }
}

val presentationModule = module {
    factory { HomeContainer(get(), get()) }
    factory { PracticeContainer(get(), get()) }
    factory { MockTestContainer(get(), get()) }
}
```

### Mappers
- Use extension functions for mapping
- Keep mappers in `mapper/` subpackage of data layer

```kotlin
fun QuestionDto.toDomain(): Question = Question(
    id = id,
    text = text,
    options = options,
    correctIndex = correctIndex
)
```

### Result Handling
- Use sealed `Result<T>` type for operations that can fail
- Provide `onSuccess`/`onError` extension functions

```kotlin
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable) : Result<Nothing>
}
```

## File Organization

```
feature/
├── di/
│   └── FeatureModule.kt
├── model/
│   └── FeatureModel.kt
├── repository/
│   └── FeatureRepository.kt (interface in domain, impl in data)
├── usecase/
│   └── FeatureUseCase.kt
└── ui/
    ├── FeatureScreen.kt
    ├── FeatureContainer.kt
    ├── FeatureState.kt
    ├── FeatureIntent.kt
    └── FeatureAction.kt
```

Or combine contract in single file:
```
feature/ui/
├── FeatureScreen.kt
├── FeatureContainer.kt
└── FeatureContract.kt  (State, Intent, Action)
```

## What NOT to Do
- Do NOT add explanatory comments
- Do NOT add TODO comments (use task tracking instead)
- Do NOT add region comments
- Do NOT add file header comments
- Do NOT add KDoc for private functions
- Do NOT add obvious parameter documentation
- Do NOT import data layer in presentation
- Do NOT import presentation in domain
- Do NOT use repositories directly in Containers (use Use Cases)
- Do NOT mutate state outside `updateState` block
- Do NOT use `MutableStateFlow` directly (use FlowMVI Container)
- Do NOT trigger side effects from Composables (use Actions)
- Do NOT pass Container to child Composables (pass state + onIntent lambda)
