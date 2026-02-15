# User Guide

This guide explains how to run and use `eof-android` for day-to-day testing.

## 1) Start the app

Option A (Android Studio):
1. Open project `eof-android`.
2. Start an emulator from Device Manager.
3. Click Run.

Option B (scripted):
```bash
./scripts/run_emulator.sh
```

## 2) Main screens

- `Fetch`: AOI input and fetch/compare actions.
- `Sources`: enable/disable data sources.
- `Phenology`: run fit and inspect fitted parameters.
- `Settings`: configure runtime pipeline options.

## 3) Typical workflow

1. Open `Sources` and enable at least two pixel-capable sources.
2. Open `Fetch`, enter AOI lat/lon (or GeoJSON URL), then tap `Fetch`.
3. Tap `Compare Sources` to compute NDVI bias/RMSE/R2.
4. Open `Phenology` and tap `Fit Phenology`.
5. Tune settings and re-run fit.

## 4) Understanding outputs

- `Median NDVI Time Series`: median NDVI by date over current observations.
- `Source comparison metrics`:
  - `Bias`: mean NDVI difference (A-B).
  - `RMSE`: root mean square difference.
  - `R2`: agreement coefficient.
- `Phenology fit`:
  - `mn/mx`: low/high NDVI baseline and peak.
  - `SOS/EOS`: start/end of season (day of year).
  - `rsp/rau`: green-up and senescence rates.

## 5) Current limitations

- Fetch/compare uses synthetic data generation right now.
- Per-pixel summaries are synthetic placeholders.
- Real STAC/COG and token-auth backends are planned and tracked in docs.

## 6) Troubleshooting

- `SDK location not found`: run `./scripts/install_android_sdk.sh` and ensure `local.properties` has `sdk.dir`.
- `No enabled pixel-capable source`: enable AWS/Planetary/Earthdata/GEE in `Sources`.
- `Need at least N observations`: reduce `Min observations` in Settings or fetch broader date ranges.
