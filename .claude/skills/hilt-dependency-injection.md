# Skill: Hilt DI Patterns

## When to use
When adding new repositories, ViewModels, UseCases, data sources, or billing components.

## Rules
- All ViewModels: annotate with @HiltViewModel, inject via hiltViewModel()
- Repositories: bind via @Binds in a Module, never instantiate directly
- Modules live in: app/src/main/java/.../di/
- Do not use @Inject constructor on classes that have Android lifecycle dependencies
- DataStore: inject as singleton via @Provides in DataModule
- BillingManager: inject into Activity/ViewModel; must call `startConnection()`/`endConnection()` in Activity lifecycle
- When adding a new feature: create UseCase → Repository interface → RepositoryImpl → Module binding

## File locations
- app/src/main/java/.../di/
