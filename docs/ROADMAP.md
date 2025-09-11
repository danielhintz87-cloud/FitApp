# FitApp Development Roadmap

## Priority Definitions

- **P0 (Blocker)**: Critical issues preventing stable app functionality - must be completed first
- **P1 (High)**: Core features and stability improvements - next in priority after P0
- **P2 (Medium)**: Feature enhancements and optimizations - planned for subsequent iterations  
- **P3 (Low)**: Nice-to-have features and polish - long-term roadmap items

## Status Definitions

- **INITIAL**: Planned but not started
- **SKELETON**: Basic structure/interfaces implemented, core logic pending
- **IN_PROGRESS**: Actively being developed
- **DONE**: Completed and tested
- **BLOCKED**: Waiting on dependencies or external factors

## Phase 1: Stabilization (P0/P1) - Current Focus

| Issue | Priority | Status | Description | Dependencies |
|-------|----------|--------|-------------|--------------|
| Build Stability & Migrations | P0 | DONE | Complete Room migration chain (v5-17), fix compilation errors | None |
| Threading/Dispatcher Hardening | P0 | DONE | DispatcherProvider interface, replace direct Dispatchers usage | None |
| DataStore Proto Foundation | P1 | DONE | Proto schema, repository, migration skeleton | Threading |
| API Health Checker Skeleton | P1 | SKELETON | HealthCheckable interface, Gemini checker, registry | Threading |
| Migration Tests | P0 | SKELETON | Test migration chain completeness | Build Stability |
| AI Provider Threading Tests | P0 | INITIAL | Verify no NetworkOnMainThreadException | Threading |
| Basic Documentation | P1 | SKELETON | ROADMAP.md, CONTRIBUTING.md updates | None |

## Phase 2: Core Features (P1)

| Issue | Priority | Status | Description | Dependencies |
|-------|----------|--------|-------------|--------------|
| Collections Persistence | P1 | INITIAL | Recipe collections database integration | DataStore |
| SyncQueue Persistence | P1 | INITIAL | Cloud sync queue with offline support | DataStore |
| Health Checker Extensions | P1 | INITIAL | Perplexity, Health Connect checkers | Health Checker Skeleton |
| Enhanced Migration Logic | P1 | INITIAL | Complete SharedPreferences to DataStore migration | DataStore Foundation |
| API Keys Screen Health Status | P1 | INITIAL | Display health status in settings UI | Health Checker |

## Phase 3: Advanced Features (P2)

| Issue | Priority | Status | Description | Dependencies |
|-------|----------|--------|-------------|--------------|
| Barcode Database | P2 | INITIAL | OpenFoodFacts integration for nutrition | Collections |
| Meal Planner Pro | P2 | INITIAL | Advanced meal planning with preferences | Collections |
| ML Inference Pipeline | P2 | INITIAL | Real ML model integration (pose detection) | Core Features |
| Video Caching System | P2 | INITIAL | Workout video offline caching | Storage |
| Coach Triggers | P2 | INITIAL | AI-driven workout coaching prompts | AI Provider Health |
| Timer & Countdown Logic | P2 | INITIAL | Advanced workout timing features | Core Features |

## Phase 4: Polish & Optimization (P3)

| Issue | Priority | Status | Description | Dependencies |
|-------|----------|--------|-------------|--------------|
| Accessibility Completion | P3 | INITIAL | Full a11y compliance and testing | UI Components |
| Performance Optimization | P3 | INITIAL | Database queries, UI rendering optimization | All Core Features |
| Advanced Analytics | P3 | INITIAL | User behavior tracking and insights | Data Pipeline |
| Social Features | P3 | INITIAL | Sharing, challenges, community features | Core Features |
| Widget Support | P3 | INITIAL | Home screen widgets for quick access | Core Features |

## Dependency Graph

```
Build Stability ──┐
                  ├──→ Threading ──┐
                  │                ├──→ DataStore ──┐
                  │                │                ├──→ Collections ──┐
                  │                │                │                  ├──→ ML/Video/Coach
                  │                │                ├──→ SyncQueue ────┘
                  │                │                │
                  │                ├──→ Health Checker ──→ API Health Extensions
                  │                │
Migration Tests ──┘                ├──→ AI Provider Tests
                                   │
                                   └──→ Core Features ──→ Advanced Features ──→ Polish
```

## Next Wave Planning

### Immediate Post-Phase 1 (Next 2-4 weeks)
1. **Collections Persistence**: Complete recipe collections with database backing
2. **SyncQueue Implementation**: Offline-first sync queue for cloud operations
3. **Health Checker Extensions**: Add Perplexity and Health Connect status monitoring
4. **Enhanced Error Handling**: Unified error states and user feedback

### Medium Term (1-3 months)
1. **Barcode Database Integration**: OpenFoodFacts API with local caching
2. **Advanced Meal Planning**: Pro features with dietary preferences
3. **ML Pipeline Hardening**: Real pose detection and form analysis
4. **Performance Optimization**: Database indexing, query optimization

### Long Term (3-6 months)
1. **Accessibility Excellence**: Full WCAG compliance
2. **Social Platform**: Community features and challenges
3. **Advanced Analytics**: Usage insights and recommendations
4. **Cross-Platform**: Wear OS integration, tablet optimization

## Notes

- **Testing Strategy**: Each phase includes comprehensive testing (unit, integration, UI)
- **Migration Safety**: All database changes are additive with rollback capability
- **Performance Monitoring**: Continuous performance tracking introduced in Phase 2
- **Documentation**: Living documentation updated with each major feature completion

## Maintenance Commitments

- **Security Updates**: Monthly dependency updates and security patches
- **Bug Fixes**: Critical bugs addressed within 48 hours, others within 1 week
- **Performance**: Maintain <3 second cold start, <500ms navigation transitions
- **Compatibility**: Support Android API 28+ (Android 9.0+)