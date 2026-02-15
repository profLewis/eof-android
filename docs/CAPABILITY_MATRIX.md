# Capability Matrix (Derived from `../eof-ios`)

| Capability from iOS | Android status | Notes / next implementation step |
|---|---|---|
| Multi-source configuration (AWS/PC/CDSE/Earthdata/GEE) | Baseline implemented | Source toggles and metadata in UI; add real auth + token storage next. |
| AOI controls (lat/lon, GeoJSON) | Baseline implemented | Inputs are present; add map draw/select and GeoJSON ingestion next. |
| Fetch pipeline (STAC search + raster read + VI compute) | Scaffold implemented | Synthetic service currently; replace with STAC + COG adapters behind same interfaces. |
| NDVI / DVI computation | Implemented | `NdviMath` tested. |
| Source comparison metrics (bias/RMSE/R2) | Baseline implemented | Metrics and compare flow implemented; add band-wise scatter and per-date tables next. |
| Double logistic phenology fit | Baseline implemented | Fitter and tests in place; upgrade optimizer to Nelder-Mead + Huber for parity. |
| Per-pixel fit quality summary | Baseline implemented | Summary is synthetic; wire real per-pixel fitter and maps next. |
| Settings for concurrency and fit parameters | Baseline implemented | UI controls present; connect to real pipeline scheduler. |
| Progress and diagnostics | Partial | Status line is present; add per-source progress bars and logs next. |
| Offline/regression testing harness | Baseline implemented | Unit tests active; add golden-data tests with real scene fixtures. |
