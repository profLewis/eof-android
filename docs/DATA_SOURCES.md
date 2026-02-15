# Data Sources

This document explains each data source mirrored from `eof-ios` and how Android uses it now.

## Source summary

| Source | Pixel support in Android baseline | Auth | Planned backend |
|---|---|---|---|
| AWS Earth Search | Yes (synthetic currently) | None | STAC + COG reader |
| Planetary Computer | Yes (synthetic currently) | SAS signing (future) | STAC + COG reader + SAS token service |
| Copernicus Data Space (CDSE) | Marked search-only | Bearer or S3 keys (future) | STAC search + JP2 handling strategy |
| NASA Earthdata HLS | Yes (synthetic currently) | Bearer token (future) | STAC + HLS COG reader |
| Google Earth Engine | Yes (synthetic currently) | OAuth2 (future) | computePixels client + token manager |

## Notes by source

### AWS Earth Search
- Public STAC endpoint in iOS flow.
- No authentication needed.
- Target Android plan: direct STAC search + COG range reads.

### Planetary Computer
- STAC with signed asset URLs.
- Android will need SAS token manager equivalent to iOS behavior.

### CDSE
- Included for parity and configuration completeness.
- Marked non-pixel-capable in current Android model because JP2 pipeline is not yet implemented.

### NASA Earthdata (HLS)
- Uses HLS conventions and cloud mask fields that differ from Sentinel SCL.
- Android plan: dedicated adapter with HLS band naming and scaling.

### Google Earth Engine
- Uses server-side pixel computation flow in iOS.
- Android plan: authenticated computePixels requests + GeoTIFF parsing.

## Cross-source behavior to preserve

Android should keep parity with iOS for:
- Source toggling and selection.
- Compare-mode pairwise metrics.
- Consistent reflectance scaling and offset handling once real backends are wired.

See `docs/CAPABILITY_MATRIX.md` for status and `docs/IOS_SYNC.md` for update process.
