# Methods

This document describes implemented and planned analytical methods.

## 1) Vegetation indices

### NDVI
Formula:
```text
NDVI = (NIR - Red) / (NIR + Red)
```
Implementation: `app/src/main/java/uk/ac/ucl/eof/android/util/NdviMath.kt`.

Edge case:
- If denominator is zero, baseline returns `0.0`.

### DVI
Formula:
```text
DVI = NIR - Red
```
Implementation: same utility as NDVI.

## 2) Source comparison metrics

Current compare output includes:
- `Bias` (mean NDVI difference)
- `RMSE` (root mean square NDVI difference)
- `R2` (fit agreement coefficient)

Implementation: `SyntheticEOService.compare(...)`.

## 3) Phenology model (double logistic)

Model form:
```text
f(t) = mn + (mx - mn) * (spring(t) + autumn(t) - 1)
```
where `spring` and `autumn` are logistic terms controlled by:
- `sos`, `rsp` (green-up timing/rate)
- `eos`, `rau` (senescence timing/rate)

Current Android fitter:
- Multi-start local search from perturbed initial parameters.
- Parameter bounds applied each step.
- RMSE objective for model selection.

Implementation: `app/src/main/java/uk/ac/ucl/eof/android/service/PhenologyService.kt`.

## 4) Current baseline vs iOS-target parity

Current Android:
- Synthetic observation generation.
- Baseline phenology fitter.
- Baseline compare metrics.

Planned parity upgrades:
- Real STAC/COG data ingest.
- iOS-equivalent robust fitting behavior (Nelder-Mead + robust loss + restarts).
- Per-pixel parameter maps and richer diagnostics.
