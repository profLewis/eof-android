# Testing Strategy

## Current automated tests
- `NdviMathTest`: NDVI math and edge cases.
- `PhenologyServiceTest`: double-logistic fitting returns valid parameters.
- `SyntheticEOServiceTest`: synthetic fetch and source comparison metrics.
- `EORepositoryTest`: end-to-end state transitions for fetch/compare/fit flows.

## Test gates for parity
1. Math gate:
   - NDVI/DVI correctness and denominator edge cases.
   - Phenology fitter residual threshold checks.
2. Pipeline gate:
   - Source enable/disable behavior.
   - Compare flow requires two enabled pixel sources.
3. Regression gate:
   - For each iOS behavior addition, add a matching Android test case before or with implementation.

## Manual test checklist (Android Studio)
1. Launch app on emulator/device.
2. Toggle data sources and verify source cards update.
3. Fetch data and verify time series draws.
4. Run compare and verify bias/RMSE/R2 appear.
5. Run phenology fit and verify parameter block + pixel summary appear.
6. Change settings sliders and re-run fit.

## Near-term additions
- Contract tests for real STAC responses and auth token handling.
- Golden-file tests using fixed EO scenes shared with iOS.
- Instrumentation tests for key UI workflows.
