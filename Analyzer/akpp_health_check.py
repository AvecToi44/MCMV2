#!/usr/bin/env python3
"""AKPP first-loop health check against a reference RSLZ file.

Features:
- Load gzipped XML (.rslz)
- Extract Time/Value series from DataResult or PWMResult channels
- Smooth signal and detect extrema (max/min)
- Compare test extrema against reference extrema
- Export CSV report with statuses
- Build comparison plot with extrema and alerts
"""

from __future__ import annotations

import argparse
import csv
import gzip
import math
import sys
from itertools import zip_longest
from pathlib import Path
from typing import Dict, List, Optional, Sequence, Tuple
import xml.etree.ElementTree as ET

try:
    import numpy as np
except ImportError:
    np = None

try:
    from scipy.signal import find_peaks, savgol_filter
except ImportError:
    find_peaks = None
    savgol_filter = None


def _strip_ns(tag: str) -> str:
    if "}" in tag:
        return tag.rsplit("}", 1)[-1]
    return tag


def _safe_float(value: Optional[str]) -> Optional[float]:
    if value is None:
        return None
    text = str(value).strip().replace(",", ".")
    if not text:
        return None
    try:
        return float(text)
    except ValueError:
        return None


def _get_child_text(node: ET.Element, names: Sequence[str]) -> Optional[str]:
    expected = {n.lower() for n in names}
    for child in list(node):
        if _strip_ns(child.tag).lower() in expected:
            return child.text
    return None


def _extract_index(node: ET.Element) -> Optional[int]:
    attr_candidates = ("Index", "index", "No", "no", "Channel", "channel")
    for key in attr_candidates:
        if key in node.attrib:
            val = _safe_float(node.attrib[key])
            if val is not None:
                return int(val)

    text = _get_child_text(node, ("Index", "No", "Channel"))
    parsed = _safe_float(text)
    return int(parsed) if parsed is not None else None


def load_rslz(path: Path) -> ET.Element:
    if not path.exists():
        raise FileNotFoundError(f"File not found: {path}")

    with gzip.open(path, "rb") as handle:
        raw = handle.read()

    try:
        root = ET.fromstring(raw)
    except ET.ParseError as exc:
        raise ValueError(f"Invalid XML in {path}: {exc}") from exc

    return root


def _find_nodes_by_tag(root: ET.Element, tag_name: str) -> List[ET.Element]:
    target = tag_name.lower()
    return [node for node in root.iter() if _strip_ns(node.tag).lower() == target]


def _pick_channel_node(nodes: Sequence[ET.Element], index: int) -> ET.Element:
    indexed_map: Dict[int, ET.Element] = {}
    without_index: List[ET.Element] = []

    for node in nodes:
        idx = _extract_index(node)
        if idx is None:
            without_index.append(node)
        else:
            indexed_map[idx] = node

    if index in indexed_map:
        return indexed_map[index]

    if 1 <= index <= len(without_index):
        return without_index[index - 1]

    available = sorted(indexed_map.keys())
    raise ValueError(
        f"Channel index {index} not found. Available indexed channels: {available}; "
        f"unnumbered channels: {len(without_index)}"
    )


def extract_series(root: ET.Element, channel: str, index: int) -> Tuple[np.ndarray, np.ndarray]:
    if channel == "data":
        nodes = _find_nodes_by_tag(root, "DataResult")
    elif channel == "pwm":
        nodes = _find_nodes_by_tag(root, "PWMResult")
    else:
        raise ValueError(f"Unsupported channel type: {channel}")

    if not nodes:
        raise ValueError(f"No nodes found for channel type '{channel}'")

    selected = _pick_channel_node(nodes, index)

    datapoints = [node for node in selected.iter() if _strip_ns(node.tag).lower() == "datapoint"]
    if not datapoints:
        raise ValueError(f"No DataPoint entries found in channel '{channel}' index {index}")

    pairs: List[Tuple[float, float]] = []
    for point in datapoints:
        time_raw = point.attrib.get("Time")
        value_raw = point.attrib.get("Value")

        if time_raw is None:
            time_raw = _get_child_text(point, ("Time",))
        if value_raw is None:
            value_raw = _get_child_text(point, ("Value",))

        t = _safe_float(time_raw)
        v = _safe_float(value_raw)

        if t is None or v is None:
            continue
        pairs.append((t, v))

    if not pairs:
        raise ValueError(f"No valid Time/Value pairs found in channel '{channel}' index {index}")

    pairs.sort(key=lambda item: item[0])
    time = np.array([p[0] for p in pairs], dtype=float)
    value = np.array([p[1] for p in pairs], dtype=float)

    return time, value


def preprocess_signal(
    value: np.ndarray,
    smooth_window: int,
    polyorder: int,
) -> np.ndarray:
    if value.size < 5:
        return value.copy()

    window = max(3, int(smooth_window))
    if window % 2 == 0:
        window += 1
    if window > value.size:
        window = value.size if value.size % 2 == 1 else value.size - 1
    if window < 3:
        return value.copy()

    poly = min(int(polyorder), window - 1)
    if poly < 1:
        return value.copy()

    try:
        return savgol_filter(value, window_length=window, polyorder=poly)
    except ValueError:
        return value.copy()


def detect_extrema(
    time: np.ndarray,
    value_raw: np.ndarray,
    value_smooth: np.ndarray,
    prominence: Optional[float],
    distance: int,
    width: int,
) -> List[Dict[str, float]]:
    if time.size != value_raw.size or time.size != value_smooth.size:
        raise ValueError("Time and value arrays must have the same length")

    if time.size == 0:
        return []

    if prominence is None:
        signal_range = float(np.max(value_smooth) - np.min(value_smooth))
        prominence = max(signal_range * 0.05, 1e-6)

    kwargs = {
        "prominence": float(prominence),
        "distance": max(1, int(distance)),
        "width": max(1, int(width)),
    }

    max_idx, _ = find_peaks(value_smooth, **kwargs)
    min_idx, _ = find_peaks(-value_smooth, **kwargs)

    extrema: List[Dict[str, float]] = []
    for idx in max_idx:
        extrema.append(
            {
                "type": "max",
                "index": int(idx),
                "time": float(time[idx]),
                "value": float(value_raw[idx]),
            }
        )

    for idx in min_idx:
        extrema.append(
            {
                "type": "min",
                "index": int(idx),
                "time": float(time[idx]),
                "value": float(value_raw[idx]),
            }
        )

    extrema.sort(key=lambda item: item["time"])
    return extrema


def _fmt_number(value: Optional[float], digits: int = 6) -> str:
    if value is None or (isinstance(value, float) and (math.isnan(value) or math.isinf(value))):
        return ""
    return f"{value:.{digits}f}"


def compare_extrema(
    ref_extrema: Sequence[Dict[str, float]],
    test_extrema: Sequence[Dict[str, float]],
    value_thr: float,
    time_thr: float,
) -> List[Dict[str, object]]:
    rows: List[Dict[str, object]] = []
    phase = 1

    for ref_point, test_point in zip_longest(ref_extrema, test_extrema):
        ref_value = ref_point["value"] if ref_point else None
        test_value = test_point["value"] if test_point else None
        ref_time = ref_point["time"] if ref_point else None
        test_time = test_point["time"] if test_point else None

        delta_value = None
        delta_time = None
        if ref_value is not None and test_value is not None:
            delta_value = abs(ref_value - test_value)
        if ref_time is not None and test_time is not None:
            delta_time = abs(ref_time - test_time)

        same_type = True
        if ref_point is not None and test_point is not None:
            same_type = ref_point.get("type") == test_point.get("type")

        status = "Alert"
        if (
            delta_value is not None
            and delta_time is not None
            and delta_value <= value_thr
            and delta_time <= time_thr
            and same_type
        ):
            status = "OK"

        rows.append(
            {
                "Phase": phase,
                "Value_ref": ref_value,
                "Value_test": test_value,
                "DeltaValue": delta_value,
                "Time_ref": ref_time,
                "Time_test": test_time,
                "DeltaTime": delta_time,
                "Status": status,
                "RefType": ref_point["type"] if ref_point else "",
                "TestType": test_point["type"] if test_point else "",
                "RefIndex": ref_point["index"] if ref_point else None,
                "TestIndex": test_point["index"] if test_point else None,
            }
        )
        phase += 1

    return rows


def export_csv(rows: Sequence[Dict[str, object]], csv_path: Path) -> None:
    csv_path.parent.mkdir(parents=True, exist_ok=True)
    with csv_path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(
            handle,
            fieldnames=[
                "Phase",
                "Value_ref",
                "Value_test",
                "DeltaValue",
                "Time_ref",
                "Time_test",
                "DeltaTime",
                "Status",
            ],
        )
        writer.writeheader()
        for row in rows:
            writer.writerow(
                {
                    "Phase": row["Phase"],
                    "Value_ref": _fmt_number(row["Value_ref"], 6),
                    "Value_test": _fmt_number(row["Value_test"], 6),
                    "DeltaValue": _fmt_number(row["DeltaValue"], 6),
                    "Time_ref": _fmt_number(row["Time_ref"], 3),
                    "Time_test": _fmt_number(row["Time_test"], 3),
                    "DeltaTime": _fmt_number(row["DeltaTime"], 3),
                    "Status": row["Status"],
                }
            )


def calculate_health_score(
    rows: Sequence[Dict[str, object]],
    value_thr: float,
    time_thr: float,
) -> float:
    if not rows:
        return 0.0

    value_norms: List[float] = []
    time_norms: List[float] = []

    for row in rows:
        dv = row.get("DeltaValue")
        dt = row.get("DeltaTime")
        if isinstance(dv, (int, float)):
            value_norms.append(float(dv) / max(value_thr, 1e-9))
        if isinstance(dt, (int, float)):
            time_norms.append(float(dt) / max(time_thr, 1e-9))

    if not value_norms and not time_norms:
        return 0.0

    avg_v = float(np.mean(value_norms)) if value_norms else 1.0
    avg_t = float(np.mean(time_norms)) if time_norms else 1.0
    combined = 0.5 * (avg_v + avg_t)
    score = max(0.0, 100.0 * (1.0 - combined))
    return min(100.0, score)


def signal_metrics(time: np.ndarray, value: np.ndarray) -> Dict[str, float]:
    if time.size < 2:
        return {"mean_abs_derivative": 0.0, "integral": 0.0}

    derivative = np.gradient(value, time)
    integral = float(np.trapz(value, time))
    return {
        "mean_abs_derivative": float(np.mean(np.abs(derivative))),
        "integral": integral,
    }


def plot_comparison(
    time_ref: np.ndarray,
    value_ref: np.ndarray,
    time_test: np.ndarray,
    value_test: np.ndarray,
    ref_extrema: Sequence[Dict[str, float]],
    test_extrema: Sequence[Dict[str, float]],
    rows: Sequence[Dict[str, object]],
    plot_path: Optional[Path],
    show_plot: bool,
    channel_title: str,
) -> None:
    try:
        import matplotlib.pyplot as plt
    except ImportError as exc:
        raise RuntimeError(
            "matplotlib is not installed. Install it with 'python -m pip install matplotlib' "
            "or run with --no-plot to skip visualization."
        ) from exc

    fig, ax = plt.subplots(figsize=(13, 6))

    ax.plot(time_ref, value_ref, color="#1f77b4", linewidth=1.4, label="Reference")
    ax.plot(time_test, value_test, color="#ff7f0e", linewidth=1.4, alpha=0.9, label="Test")

    if ref_extrema:
        ref_x = [p["time"] for p in ref_extrema]
        ref_y = [p["value"] for p in ref_extrema]
        ax.scatter(ref_x, ref_y, color="#0052cc", marker="o", s=36, label="Ref extrema")

    if test_extrema:
        test_x = [p["time"] for p in test_extrema]
        test_y = [p["value"] for p in test_extrema]
        ax.scatter(test_x, test_y, color="#e67e00", marker="^", s=40, label="Test extrema")

    alert_rows = [row for row in rows if row.get("Status") == "Alert"]
    if alert_rows:
        alert_x = [row.get("Time_test") for row in alert_rows if isinstance(row.get("Time_test"), (int, float))]
        alert_y = [row.get("Value_test") for row in alert_rows if isinstance(row.get("Value_test"), (int, float))]
        if alert_x and alert_y:
            ax.scatter(alert_x, alert_y, color="red", marker="x", s=95, linewidths=2.0, label="Alert")

        for row in alert_rows:
            t = row.get("Time_test")
            v = row.get("Value_test")
            phase = row.get("Phase")
            if isinstance(t, (int, float)) and isinstance(v, (int, float)):
                ax.annotate(
                    f"A{phase}",
                    xy=(float(t), float(v)),
                    xytext=(6, 6),
                    textcoords="offset points",
                    color="red",
                    fontsize=8,
                    weight="bold",
                )

    ax.set_title(f"AKPP Health Check: {channel_title}")
    ax.set_xlabel("Time")
    ax.set_ylabel("Value")
    ax.grid(True, linestyle="--", alpha=0.35)
    ax.legend(loc="best")
    fig.tight_layout()

    if plot_path is not None:
        plot_path.parent.mkdir(parents=True, exist_ok=True)
        fig.savefig(plot_path, dpi=150)

    if show_plot:
        plt.show()

    plt.close(fig)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="AKPP health check by comparing test RSLZ against reference.",
    )
    parser.add_argument("--ref", required=True, help="Path to reference RSLZ file")
    parser.add_argument("--test", required=True, help="Path to test RSLZ file")

    parser.add_argument(
        "--channel",
        choices=("data", "pwm"),
        default="data",
        help="Channel type to analyze",
    )
    parser.add_argument("--data-index", type=int, default=1, help="DataResult index")
    parser.add_argument("--pwm-index", type=int, default=1, help="PWMResult index")

    parser.add_argument("--value-thr", type=float, default=0.5, help="Allowed value deviation")
    parser.add_argument("--time-thr", type=float, default=50.0, help="Allowed time deviation")

    parser.add_argument("--smooth-window", type=int, default=21, help="Savitzky-Golay window")
    parser.add_argument("--polyorder", type=int, default=3, help="Savitzky-Golay polynomial order")
    parser.add_argument(
        "--prominence",
        type=float,
        default=None,
        help="Peak prominence for extrema detection (auto if omitted)",
    )
    parser.add_argument("--distance", type=int, default=10, help="Minimum sample distance between peaks")
    parser.add_argument("--width", type=int, default=1, help="Minimum peak width")

    parser.add_argument("--out", default="akpp_health_report.csv", help="Output CSV file path")
    parser.add_argument("--plot", default="akpp_health_plot.png", help="Output PNG file path")
    parser.add_argument("--no-plot", action="store_true", help="Skip plot generation")
    parser.add_argument("--show-plot", action="store_true", help="Show plot window")
    parser.add_argument(
        "--metrics",
        action="store_true",
        help="Compute extra signal metrics (derivative/integral)",
    )

    return parser.parse_args()


def ensure_core_dependencies() -> None:
    missing = []
    if np is None:
        missing.append("numpy")
    if find_peaks is None or savgol_filter is None:
        missing.append("scipy")

    if missing:
        pkg_list = " ".join(sorted(set(missing)))
        print(
            "Missing required packages: "
            f"{', '.join(sorted(set(missing)))}. "
            f"Install with: python -m pip install {pkg_list}"
        )
        sys.exit(1)


def main() -> None:
    args = parse_args()
    ensure_core_dependencies()

    ref_path = Path(args.ref)
    test_path = Path(args.test)
    csv_path = Path(args.out)
    plot_path = Path(args.plot) if args.plot else None

    channel_index = args.data_index if args.channel == "data" else args.pwm_index

    root_ref = load_rslz(ref_path)
    root_test = load_rslz(test_path)

    time_ref, value_ref = extract_series(root_ref, args.channel, channel_index)
    time_test, value_test = extract_series(root_test, args.channel, channel_index)

    smooth_ref = preprocess_signal(value_ref, args.smooth_window, args.polyorder)
    smooth_test = preprocess_signal(value_test, args.smooth_window, args.polyorder)

    ref_extrema = detect_extrema(
        time_ref,
        value_ref,
        smooth_ref,
        prominence=args.prominence,
        distance=args.distance,
        width=args.width,
    )
    test_extrema = detect_extrema(
        time_test,
        value_test,
        smooth_test,
        prominence=args.prominence,
        distance=args.distance,
        width=args.width,
    )

    rows = compare_extrema(ref_extrema, test_extrema, args.value_thr, args.time_thr)
    export_csv(rows, csv_path)

    if not args.no_plot:
        plot_comparison(
            time_ref,
            value_ref,
            time_test,
            value_test,
            ref_extrema,
            test_extrema,
            rows,
            plot_path,
            args.show_plot,
            channel_title=f"{args.channel.upper()} index {channel_index}",
        )

    ok_count = sum(1 for row in rows if row["Status"] == "OK")
    alert_count = len(rows) - ok_count

    print(f"Reference extrema: {len(ref_extrema)}")
    print(f"Test extrema: {len(test_extrema)}")
    print(f"Phases checked: {len(rows)} | OK: {ok_count} | Alert: {alert_count}")
    print(f"CSV report: {csv_path}")
    if (not args.no_plot) and plot_path is not None:
        print(f"Plot saved: {plot_path}")

    score = calculate_health_score(rows, args.value_thr, args.time_thr)
    print(f"Health Score: {score:.2f}/100")

    if args.metrics:
        ref_metrics = signal_metrics(time_ref, value_ref)
        test_metrics = signal_metrics(time_test, value_test)

        print("Extra metrics:")
        print(
            "  Reference: "
            f"mean|dV/dt|={ref_metrics['mean_abs_derivative']:.6f}, "
            f"integral={ref_metrics['integral']:.6f}"
        )
        print(
            "  Test: "
            f"mean|dV/dt|={test_metrics['mean_abs_derivative']:.6f}, "
            f"integral={test_metrics['integral']:.6f}"
        )


if __name__ == "__main__":
    main()
