# AKPP Health Analyzer (Offline)

Interactive offline HTML/JS tool for automatic transmission (AKPP) log diagnostics with editable tolerance presets, health scoring, and visual analysis.

## What this project is

This repository contains a **fully local** analyzer that runs in a browser without backend services:

- Preset editor for reference `.rslz` signals
- Auto marker engine (extremum-like and direction-change points)
- Asymmetric per-point tolerances (`tol_up`, `tol_down`)
- Log analysis against a preset with visual alerts
- Composite `Health Score` (0..100)
- Extrema table + control-point status table
- Derivative/integral metrics
- Gemma-ready explanation stub JSON

It is designed for reliability in workshop/offline environments where internet access is not guaranteed.

## Current scope

Implemented and supported:

- Local file inputs: `.rslz` and `.json`
- Channel selection: `DataResult` / `PWMResult`
- Preset creation, editing, save/load
- Backward compatibility with older presets (without `control_points`)
- Interactive Plotly charts (`zoom`, `pan`, `hover`)

Not implemented:

- Backend or cloud sync
- Real Gemma model call (only structured stub output)
- Exact SciPy parity for extrema detection (JS deterministic algorithm is used)

## Repository structure

- `index.html` - app layout and controls
- `styles.css` - visual styles and responsive layout
- `app.js` - all application logic (parsing, editing, analysis, scoring)
- `vendor/plotly.min.js` - local Plotly runtime
- `vendor/pako.min.js` - local gzip inflate for `.rslz`
- `akpp_health_check.py` - legacy Python prototype (kept for reference)
- `dp0-*.rslz` - sample logs

## How to run

Use a static local server (recommended for browser file/module behavior):

```bash
cd /Users/arsenx/Dev/MCMV2/Analyzer
python3 -m http.server 8080
```

Open:

- [http://localhost:8080/index.html](http://localhost:8080/index.html)

Stop server with `Ctrl + C`.

## Typical workflow

### 1) Build or edit a preset

1. Open **Preset Editor**
2. Load reference `.rslz`
3. Select channel type and index
4. Click **Auto-markers** (or load existing preset)
5. Switch to **Edit points** mode if manual correction is needed
6. Drag selected handle type:
   - `Control point` (`ref_value`)
   - `Upper tolerance (+)` (`tol_up`)
   - `Lower tolerance (-)` (`tol_down`)
7. Save preset JSON

### 2) Analyze a test log

1. Open **Log Analysis**
2. Load test `.rslz`
3. Load preset JSON
4. Select channel type and index
5. Set `DeltaValue` / `DeltaTime` thresholds
6. Click **Run analysis**

You will get:

- Main signal plot with limits
- Red/green markers (out/in tolerance)
- Highlighted penalty regions
- Extrema comparison table
- Control points status table
- Health Score and metrics

## Data contracts

### `.rslz` parsing notes

The parser supports common XML variants inside gzipped `.rslz`:

- `DataPoint` values from either attributes or child tags (`Time`, `Value`)
- Channel index from attribute or child tags (`Index`, `No`, `Channel`)
- Duplicate timestamps are deduplicated (latest point wins)
- Time is normalized to start from `0`

### Preset JSON (current format)

Required (legacy compatibility kept):

- `preset_name: string`
- `time: number[]`
- `upper_limit: number[]`
- `lower_limit: number[]`

Recommended (v2):

- `control_points: Array<{ id, time, ref_value, tol_up, tol_down, kind, weight, criticality }>`

Optional:

- `channel_type: "data" | "pwm"`
- `channel_index: number`
- `segment_weights: Array<{ weight, criticality }>`
- `meta: object`

`kind` values:

- `extremum`
- `turn`
- `curvature`
- `manual`

Validation rules:

- Equal lengths for `time`, `upper_limit`, `lower_limit`
- Non-decreasing `time`
- `upper_limit[i] >= lower_limit[i]`
- `tol_up >= 0`, `tol_down >= 0`

Backward compatibility:

- If `control_points` is missing, points are auto-generated from legacy bounds.

## Auto marker engine (v2)

The engine creates control points from reference signal behavior:

- extrema candidates (max/min)
- visible direction-change candidates (derivative sign changes)
- high-curvature candidates (second-difference magnitude)
- adaptive de-duplication and density reduction

Default safety constraints:

- Control-point limit: up to `300`
- Minimum spacing logic to avoid overly dense unstable point sets

## Edit interaction model

Two explicit modes are used to avoid drag conflicts:

- **View mode**
  - `pan` and `scroll zoom` enabled
  - no drag editing
- **Edit points mode**
  - pan/scroll zoom disabled
  - nearest control handle drag enabled

This solves the previous issue where chart scrolling/panning intercepted drag operations.

## Health Score model

Final score is clamped to `[0, 100]`.

Loss components:

1. **Base loss** (`~70%` of total)
   - segment penalties from border violations across the full signal
   - extrema mismatch penalties (`DeltaValue`, `DeltaTime`, type mismatch)
2. **Control-point loss** (`~30%` of total)
   - asymmetric tolerance check at control-point times
   - weighted by `weight * criticality`

Asymmetric control-point penalty:

- if test is above `ref_value + tol_up` -> normalized by `tol_up`
- if test is below `ref_value - tol_down` -> normalized by `tol_down`

## Reliability notes

What is done for reliability:

- Local vendor libraries (`Plotly`, `pako`), no CDN dependency
- Input validation and explicit error messaging
- Bounds ordering enforcement (`upper >= lower`)
- Backward preset compatibility path
- Deterministic JS algorithms (no runtime SciPy dependency)

Known limits / tradeoffs:

- Extremely dense control-point sets may reduce UI responsiveness
- Extrema detection is deterministic JS, not mathematically identical to SciPy
- Browser performance depends on machine/GPU and log size

If strict 1:1 scientific parity with Python/SciPy is required, this should be treated as a separate higher-cost workstream (WASM or backend compute path).

## Troubleshooting

### Page opens but chart is blank

- Check that `vendor/plotly.min.js` exists
- Open browser console for missing file errors

### `.rslz` does not load

- Ensure file is gzipped XML
- If corrupted, parser will show explicit XML/decompression error

### Drag still does not move points

- Ensure **Edit points** mode is selected
- Select correct drag target (`Control point`, `Upper +`, `Lower -`)
- Click near a control marker and drag vertically

### Preset from older version fails

- Ensure legacy required arrays are present: `time`, `upper_limit`, `lower_limit`

## Development notes

- No build pipeline is required; plain static files
- Main logic is intentionally concentrated in `app.js` for portable local deployment
- Before changing scoring logic, keep compatibility with saved presets and include migration fallback

## Sample data in repository

You can start with:

- reference: `dp0-E1.rslz`
- test: `dp0-E2.rslz`

Then save preset as JSON and run full analysis.

