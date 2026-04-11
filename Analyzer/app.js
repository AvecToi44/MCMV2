(() => {
  "use strict";

  if (!window.Plotly) {
    throw new Error("Plotly не найден. Убедитесь, что vendor/plotly.min.js доступен локально.");
  }

  const PRESET_TRACE = {
    REF: 0,
    UPPER: 1,
    LOWER: 2,
    CP_REF: 3,
    CP_UP: 4,
    CP_DOWN: 5,
    CP_SELECTED_SPAN: 6,
  };

  const ANALYSIS_TRACE = {
    UPPER: 0,
    LOWER: 1,
    TEST: 2,
    OK: 3,
    ALERT: 4,
    REF_MID: 5,
    EXTREMA: 6,
    CP_OK: 7,
    CP_ALERT: 8,
  };

  const CP_LIMIT_DEFAULT = 300;
  const CP_LIMIT_MIN = 12;
  const CP_LIMIT_MAX = 300;
  const PRESET_MERGE_TIME_EPS = 1e-3;
  const CP_TOL_STEP = 0.01;
  const SECTION_COUNT_DEFAULT = 12;
  const SECTION_COUNT_MIN = 2;
  const SECTION_COUNT_MAX = 200;

  const el = {
    viewMode: byId("viewMode"),
    viewPreset: byId("viewPreset"),
    viewSectionPreset: byId("viewSectionPreset"),
    viewAnalysis: byId("viewAnalysis"),
    alertBox: byId("alertBox"),

    goPresetEditor: byId("goPresetEditor"),
    goSectionPresetEditor: byId("goSectionPresetEditor"),
    goAnalysis: byId("goAnalysis"),

    presetRefFile: byId("presetRefFile"),
    presetChannelType: byId("presetChannelType"),
    presetChannelIndex: byId("presetChannelIndex"),
    presetMargin: byId("presetMargin"),
    presetAnchorCount: byId("presetAnchorCount"),
    loadPresetRef: byId("loadPresetRef"),
    rebuildBounds: byId("rebuildBounds"),
    presetName: byId("presetName"),
    presetJsonInput: byId("presetJsonInput"),
    savePreset: byId("savePreset"),
    presetPlot: byId("presetPlot"),
    presetSummary: byId("presetSummary"),
    presetEditMode: byId("presetEditMode"),
    presetDragTarget: byId("presetDragTarget"),
    autoMarkers: byId("autoMarkers"),
    resetControlPoints: byId("resetControlPoints"),
    cpAddTime: byId("cpAddTime"),
    cpAddTolUp: byId("cpAddTolUp"),
    cpAddTolDown: byId("cpAddTolDown"),
    cpAddBtn: byId("cpAddBtn"),
    cpRemoveSelectedBtn: byId("cpRemoveSelectedBtn"),
    controlPointsTableBody: byId("controlPointsTable").querySelector("tbody"),

    sectionRefFile: byId("sectionRefFile"),
    sectionChannelType: byId("sectionChannelType"),
    sectionChannelIndex: byId("sectionChannelIndex"),
    sectionCount: byId("sectionCount"),
    sectionBaseTol: byId("sectionBaseTol"),
    loadSectionRef: byId("loadSectionRef"),
    rebuildSections: byId("rebuildSections"),
    sectionSummary: byId("sectionSummary"),
    sectionPresetName: byId("sectionPresetName"),
    sectionPresetJsonInput: byId("sectionPresetJsonInput"),
    saveSectionPreset: byId("saveSectionPreset"),
    sectionSelectAll: byId("sectionSelectAll"),
    sectionClearSelection: byId("sectionClearSelection"),
    sectionSplitSelected: byId("sectionSplitSelected"),
    sectionDisableSelected: byId("sectionDisableSelected"),
    sectionEnableSelected: byId("sectionEnableSelected"),
    sectionBulkTolUp: byId("sectionBulkTolUp"),
    sectionBulkTolDown: byId("sectionBulkTolDown"),
    sectionBulkImportance: byId("sectionBulkImportance"),
    sectionApplyBulk: byId("sectionApplyBulk"),
    sectionTableBody: byId("sectionTable").querySelector("tbody"),
    sectionPlot: byId("sectionPlot"),

    analysisRslzInput: byId("analysisRslzInput"),
    analysisPresetInput: byId("analysisPresetInput"),
    analysisChannelType: byId("analysisChannelType"),
    analysisChannelIndex: byId("analysisChannelIndex"),
    valueThr: byId("valueThr"),
    timeThr: byId("timeThr"),
    runAnalysis: byId("runAnalysis"),
    goSectionEditorFromAnalysis: byId("goSectionEditorFromAnalysis"),
    analysisPlot: byId("analysisPlot"),
    analysisSummary: byId("analysisSummary"),

    healthScoreValue: byId("healthScoreValue"),
    healthScoreFill: byId("healthScoreFill"),
    scoreText: byId("scoreText"),

    segmentWeights: byId("segmentWeights"),
    extremaTableBody: byId("extremaTable").querySelector("tbody"),
    cpStatusTableBody: byId("controlPointsStatusTable").querySelector("tbody"),
    metricsBox: byId("metricsBox"),
    gemmaStub: byId("gemmaStub"),
  };

  const state = {
    activeView: "mode",
    preset: {
      refParsed: null,
      refFileName: "",
      channelType: "data",
      channelIndex: null,
      signal: null,
      extrema: [],
      controlPoints: [],
      selectedCpId: null,
      upper: [],
      lower: [],
      loadedPreset: null,
      editMode: "view",
      dragTarget: "up",
      drag: {
        active: false,
        cpId: null,
        target: "up",
        pointerId: null,
      },
      renderOrder: [],
      idCounter: 1,
    },
    sectionPreset: {
      refParsed: null,
      refFileName: "",
      channelType: "data",
      channelIndex: null,
      signal: null,
      sections: [],
      selectedIds: new Set(),
      sectionCount: SECTION_COUNT_DEFAULT,
      upper: [],
      lower: [],
      loadedPreset: null,
      idCounter: 1,
    },
    analysis: {
      testParsed: null,
      testFileName: "",
      presetObj: null,
      channelType: "data",
      channelIndex: null,
      result: null,
    },
  };

  init();

  function init() {
    el.presetDragTarget.value = state.preset.dragTarget;
    bindNavigation();
    bindPresetEditorEvents();
    bindSectionPresetEvents();
    bindAnalysisEvents();
    showView("mode");
    resetScore();

    if (!window.pako && !("DecompressionStream" in window)) {
      showAlert(
        "gzip декомпрессия не найдена: подключите vendor/pako.min.js или используйте браузер с DecompressionStream.",
        "warn",
        7000
      );
    }
  }

  function bindNavigation() {
    el.goPresetEditor.addEventListener("click", () => showView("preset"));
    el.goSectionPresetEditor.addEventListener("click", () => showView("sectionPreset"));
    el.goAnalysis.addEventListener("click", () => showView("analysis"));
    document.querySelectorAll("[data-back='mode']").forEach((btn) => {
      btn.addEventListener("click", () => showView("mode"));
    });
  }

  function bindPresetEditorEvents() {
    el.loadPresetRef.addEventListener("click", onLoadPresetReference);
    el.rebuildBounds.addEventListener("click", () => {
      if (!state.preset.signal) {
        showAlert("Сначала загрузите эталонный RSLZ.", "warn");
        return;
      }
      rebuildEnvelopeFromControlPoints();
      refreshPresetPlotData();
      renderControlPointsTable();
      updatePresetSummary();
    });

    el.presetChannelType.addEventListener("change", () => {
      state.preset.channelType = el.presetChannelType.value;
      refreshPresetChannelIndexes();
      applySeriesToPresetEditor();
    });

    el.presetChannelIndex.addEventListener("change", () => {
      state.preset.channelIndex = safeInt(el.presetChannelIndex.value);
      applySeriesToPresetEditor();
    });

    el.presetAnchorCount.addEventListener("change", () => {
      const limit = getControlPointLimit();
      el.presetAnchorCount.value = String(limit);
      if (state.preset.signal && state.preset.controlPoints.length > limit) {
        state.preset.controlPoints = reduceControlPointDensity(state.preset.controlPoints, limit);
        rebuildEnvelopeFromControlPoints();
        drawPresetPlot();
        renderControlPointsTable();
      }
      updatePresetSummary();
    });

    el.presetEditMode.addEventListener("change", () => {
      endPresetDrag();
      state.preset.editMode = el.presetEditMode.value;
      document.body.classList.toggle("preset-editing", state.preset.editMode === "edit");
      drawPresetPlot();
      updatePresetSummary();
      const modeText = state.preset.editMode === "edit" ? "режим редактирования" : "режим обзора";
      showAlert(`График переключен в ${modeText}.`, "ok", 1300);
    });

    el.presetDragTarget.addEventListener("change", () => {
      state.preset.dragTarget = el.presetDragTarget.value === "down" ? "down" : "up";
    });

    el.autoMarkers.addEventListener("click", () => {
      if (!state.preset.signal) {
        showAlert("Сначала загрузите эталонный RSLZ.", "warn");
        return;
      }
      buildAutoControlPointsFromSignal();
      drawPresetPlot();
      renderControlPointsTable();
      updatePresetSummary();
      showAlert("Авто-маркеры обновлены.", "ok", 1600);
    });

    el.resetControlPoints.addEventListener("click", () => {
      if (!state.preset.signal) {
        showAlert("Нет сигнала для сброса к авто-маркерам.", "warn");
        return;
      }
      buildAutoControlPointsFromSignal();
      drawPresetPlot();
      renderControlPointsTable();
      updatePresetSummary();
      showAlert("Control points сброшены к авто-режиму.", "ok", 1600);
    });

    el.cpAddBtn.addEventListener("click", onAddControlPointFromTable);
    el.cpRemoveSelectedBtn.addEventListener("click", removeSelectedControlPoint);

    el.presetJsonInput.addEventListener("change", onLoadPresetJsonInEditor);
    el.savePreset.addEventListener("click", onSavePresetJson);

    el.controlPointsTableBody.addEventListener("click", onControlPointTableClick);
    el.controlPointsTableBody.addEventListener("input", onControlPointTableChange);
    el.controlPointsTableBody.addEventListener("change", onControlPointTableChange);

    const plot = el.presetPlot;
    plot.addEventListener("pointerdown", onPresetPlotPointerDown);
    window.addEventListener("pointermove", onPresetDragMove);
    window.addEventListener("pointerup", endPresetDrag);
    window.addEventListener("pointercancel", endPresetDrag);
    plot.addEventListener(
      "wheel",
      (event) => {
        if (state.preset.editMode === "edit") {
          event.preventDefault();
        }
      },
      { passive: false }
    );
  }

  function bindAnalysisEvents() {
    el.analysisRslzInput.addEventListener("change", onLoadAnalysisRslz);
    el.analysisPresetInput.addEventListener("change", onLoadAnalysisPreset);

    el.analysisChannelType.addEventListener("change", () => {
      state.analysis.channelType = el.analysisChannelType.value;
      refreshAnalysisChannelIndexes();
    });

    el.analysisChannelIndex.addEventListener("change", () => {
      state.analysis.channelIndex = safeInt(el.analysisChannelIndex.value);
    });

    el.goSectionEditorFromAnalysis.addEventListener("click", () => showView("sectionPreset"));
    el.runAnalysis.addEventListener("click", runAnalysis);
  }

  function bindSectionPresetEvents() {
    el.loadSectionRef.addEventListener("click", onLoadSectionReference);
    el.sectionChannelType.addEventListener("change", () => {
      state.sectionPreset.channelType = el.sectionChannelType.value;
      refreshSectionChannelIndexes();
      applySeriesToSectionEditor();
    });
    el.sectionChannelIndex.addEventListener("change", () => {
      state.sectionPreset.channelIndex = safeInt(el.sectionChannelIndex.value);
      applySeriesToSectionEditor();
    });
    el.sectionCount.addEventListener("change", () => {
      const count = getSectionCount();
      el.sectionCount.value = String(count);
      state.sectionPreset.sectionCount = count;
      rebuildSectionsFromSignal();
    });
    el.rebuildSections.addEventListener("click", rebuildSectionsFromSignal);
    el.sectionPresetJsonInput.addEventListener("change", onLoadSectionPresetJson);
    el.saveSectionPreset.addEventListener("click", onSaveSectionPresetJson);
    el.sectionSelectAll.addEventListener("click", selectAllSections);
    el.sectionClearSelection.addEventListener("click", clearSectionSelection);
    el.sectionSplitSelected.addEventListener("click", splitSelectedSection);
    el.sectionDisableSelected.addEventListener("click", () => toggleSelectedSections(false));
    el.sectionEnableSelected.addEventListener("click", () => toggleSelectedSections(true));
    el.sectionApplyBulk.addEventListener("click", applyBulkToSelectedSections);
    el.sectionTableBody.addEventListener("input", onSectionTableChange);
    el.sectionTableBody.addEventListener("change", onSectionTableChange);
    el.sectionTableBody.addEventListener("click", onSectionTableClick);
  }

  function showView(name) {
    if (name !== "preset") {
      endPresetDrag();
    }
    state.activeView = name;
    setViewActive(el.viewMode, name === "mode");
    setViewActive(el.viewPreset, name === "preset");
    setViewActive(el.viewSectionPreset, name === "sectionPreset");
    setViewActive(el.viewAnalysis, name === "analysis");
    document.body.classList.toggle("preset-editing", name === "preset" && state.preset.editMode === "edit");

    if (name === "preset") {
      Plotly.Plots.resize(el.presetPlot);
    }
    if (name === "sectionPreset") {
      Plotly.Plots.resize(el.sectionPlot);
    }
    if (name === "analysis") {
      Plotly.Plots.resize(el.analysisPlot);
    }
  }

  async function onLoadPresetReference() {
    const file = el.presetRefFile.files?.[0];
    if (!file) {
      showAlert("Выберите RSLZ файл эталона.", "warn");
      return;
    }

    try {
      setBusy(el.loadPresetRef, true);
      const parsed = await parseRslzFile(file);
      state.preset.refParsed = parsed;
      state.preset.refFileName = file.name;
      state.preset.channelType = el.presetChannelType.value;
      refreshPresetChannelIndexes();
      if (state.preset.loadedPreset) {
        applySeriesToPresetEditor({ preset: state.preset.loadedPreset });
      } else {
        applySeriesToPresetEditor();
      }
      showAlert(`Эталон загружен: ${file.name}`, "ok", 2200);
    } catch (error) {
      showAlert(`Ошибка загрузки эталона: ${error.message}`);
    } finally {
      setBusy(el.loadPresetRef, false);
    }
  }

  async function onLoadPresetJsonInEditor() {
    const file = el.presetJsonInput.files?.[0];
    if (!file) {
      return;
    }

    try {
      const json = JSON.parse(await file.text());
      const preset = validatePresetObject(json);
      state.preset.loadedPreset = preset;
      if (preset.preset_name) {
        el.presetName.value = preset.preset_name;
      }
      if (preset.channel_type) {
        state.preset.channelType = preset.channel_type;
        el.presetChannelType.value = preset.channel_type;
      }

      if (state.preset.refParsed) {
        refreshPresetChannelIndexes();
        if (preset.channel_index != null) {
          const maybe = String(preset.channel_index);
          if (Array.from(el.presetChannelIndex.options).some((o) => o.value === maybe)) {
            el.presetChannelIndex.value = maybe;
            state.preset.channelIndex = preset.channel_index;
          }
        }
        applySeriesToPresetEditor({ preset });
      } else {
        hydratePresetWithoutReference(preset);
      }

      showAlert(`Preset загружен: ${file.name}`, "ok", 2200);
    } catch (error) {
      showAlert(`Ошибка пресета: ${error.message}`);
    }
  }

  function hydratePresetWithoutReference(preset) {
    const time = normalizeTimeArray(preset.time);
    const upper = preset.upper_limit.slice();
    const lower = preset.lower_limit.slice();
    const signal = midpointArray(upper, lower);

    state.preset.signal = { time, value: signal };
    state.preset.extrema = detectExtrema(time, signal, smoothSignal(signal, 9), {
      distance: 8,
      prominence: null,
    });

    if (preset.control_points.length) {
      state.preset.controlPoints = sanitizeControlPoints(
        preset.control_points,
        time,
        Math.max(avgArray(diffArray(upper, lower)) * 0.5, 0.05)
      );
    } else {
      state.preset.controlPoints = buildControlPointsFromLegacyBounds(time, upper, lower, getControlPointLimit());
    }

    rebuildEnvelopeFromControlPoints();
    drawPresetPlot();
    renderControlPointsTable();
    updatePresetSummary();
  }

  function onSavePresetJson() {
    try {
      if (!state.preset.signal || !state.preset.upper.length || !state.preset.lower.length) {
        throw new Error("Нет данных для сохранения. Загрузите эталон или пресет.");
      }

      const cps = sortedControlPoints().map((cp) => ({
        id: cp.id,
        time: roundNum(cp.time, 6),
        ref_value: roundNum(cp.ref_value, 8),
        tol_up: roundNum(cp.tol_up, 8),
        tol_down: roundNum(cp.tol_down, 8),
        kind: cp.kind,
        importance: roundNum(cp.importance, 4),
        weight: roundNum(cp.importance, 4),
        criticality: 1,
      }));

      const preset = {
        preset_name: (el.presetName.value || "АКПП_тип1").trim() || "АКПП_тип1",
        time: state.preset.signal.time.slice(),
        upper_limit: state.preset.upper.slice(),
        lower_limit: state.preset.lower.slice(),
        control_points: cps,
        channel_type: state.preset.channelType,
        channel_index: state.preset.channelIndex,
        segment_weights: state.preset.loadedPreset?.segment_weights || [],
        meta: {
          source_file: state.preset.refFileName || "",
          saved_at: new Date().toISOString(),
          control_points_count: cps.length,
          edit_mode_last: state.preset.editMode,
        },
      };

      downloadJson(`${sanitizeFileName(preset.preset_name)}.json`, preset);
      showAlert("Preset JSON сохранен.", "ok", 2200);
    } catch (error) {
      showAlert(`Не удалось сохранить пресет: ${error.message}`);
    }
  }

  function refreshPresetChannelIndexes() {
    if (!state.preset.refParsed) {
      populateSelect(el.presetChannelIndex, []);
      return;
    }

    const type = state.preset.channelType;
    const indexes = state.preset.refParsed.channels[type].indexes;
    populateSelect(el.presetChannelIndex, indexes);
    if (indexes.length) {
      const current = state.preset.channelIndex;
      if (current == null || !indexes.includes(current)) {
        const presetIdx = state.preset.loadedPreset?.channel_index;
        if (Number.isFinite(presetIdx) && indexes.includes(presetIdx)) {
          state.preset.channelIndex = presetIdx;
        } else {
          state.preset.channelIndex = indexes[0];
        }
      }
      if (state.preset.channelIndex == null) {
        state.preset.channelIndex = indexes[0];
      }
      el.presetChannelIndex.value = String(state.preset.channelIndex);
    }
  }

  function applySeriesToPresetEditor(options = {}) {
    if (!state.preset.refParsed) {
      return;
    }

    const type = state.preset.channelType;
    const idx = state.preset.channelIndex ?? safeInt(el.presetChannelIndex.value);
    const series = state.preset.refParsed.channels[type].seriesByIndex[idx];

    if (!series) {
      showAlert(`Канал ${type}:${idx} отсутствует в файле.`, "warn");
      return;
    }

    const time = normalizeTimeArray(series.time);
    const value = series.value.slice();

    if (time.length < 3) {
      showAlert("Недостаточно точек в канале для построения пресета.");
      return;
    }

    state.preset.signal = { time, value };
    state.preset.extrema = detectExtrema(time, value, smoothSignal(value, 9), {
      distance: 8,
      prominence: null,
    });

    if (options.preset) {
      hydrateControlPointsFromPreset(options.preset, time, value);
    } else {
      buildAutoControlPointsFromSignal();
    }

    rebuildEnvelopeFromControlPoints();
    drawPresetPlot();
    renderControlPointsTable();
    updatePresetSummary();
  }

  function hydrateControlPointsFromPreset(preset, time, value) {
    if (preset.control_points.length) {
      const avgTol = inferDefaultToleranceFromSignal(value);
      state.preset.controlPoints = sanitizeControlPoints(preset.control_points, time, avgTol);
      return;
    }

    const upper = interpolateLinear(normalizeTimeArray(preset.time), preset.upper_limit, time);
    const lower = interpolateLinear(normalizeTimeArray(preset.time), preset.lower_limit, time);
    state.preset.controlPoints = buildControlPointsFromLegacyBounds(time, upper, lower, getControlPointLimit());
  }

  function buildAutoControlPointsFromSignal() {
    const signal = state.preset.signal;
    if (!signal) {
      return;
    }

    const margin = Math.max(safeFloat(el.presetMargin.value) || 0, inferDefaultToleranceFromSignal(signal.value));
    const limit = getControlPointLimit();

    state.preset.controlPoints = generateAutoControlPoints(signal.time, signal.value, {
      limit,
      margin,
    });
    state.preset.selectedCpId = null;
  }

  function rebuildEnvelopeFromControlPoints() {
    const signal = state.preset.signal;
    if (!signal) {
      return;
    }

    if (!state.preset.controlPoints.length) {
      buildAutoControlPointsFromSignal();
    }

    const cps = sortedControlPoints();
    const cpTime = cps.map((cp) => cp.time);
    const cpUpper = cps.map((cp) => cp.ref_value + cp.tol_up);
    const cpLower = cps.map((cp) => cp.ref_value - cp.tol_down);

    state.preset.upper = interpolateLinear(cpTime, cpUpper, signal.time);
    state.preset.lower = interpolateLinear(cpTime, cpLower, signal.time);
    enforceBoundsOrder(state.preset.lower, state.preset.upper);

    for (const cp of state.preset.controlPoints) {
      const refSignal = interpolateAt(signal.time, signal.value, cp.time);
      const upper = cp.ref_value + cp.tol_up;
      const lower = cp.ref_value - cp.tol_down;
      cp.status = refSignal <= upper + 1e-9 && refSignal >= lower - 1e-9 ? "OK" : "Alert";
    }
  }

  function drawPresetPlot() {
    const signal = state.preset.signal;
    if (!signal) {
      Plotly.purge(el.presetPlot);
      return;
    }

    const cps = sortedControlPoints();
    state.preset.renderOrder = cps.map((cp) => cp.id);

    const cpTime = cps.map((cp) => cp.time);
    const cpRef = cps.map((cp) => cp.ref_value);
    const cpUp = cps.map((cp) => cp.ref_value + cp.tol_up);
    const cpDown = cps.map((cp) => cp.ref_value - cp.tol_down);
    const cpRefColor = cps.map((cp) => (cp.id === state.preset.selectedCpId ? "#ffe066" : "#62b0ff"));
    const cpRefSize = cps.map((cp) => (cp.id === state.preset.selectedCpId ? 14 : 11));
    const cpRefLineWidth = cps.map((cp) => (cp.id === state.preset.selectedCpId ? 2.2 : 1.4));
    const cpUpColor = cps.map((cp) => (cp.id === state.preset.selectedCpId ? "#ffd57a" : "#ffc857"));
    const cpDownColor = cps.map((cp) => (cp.id === state.preset.selectedCpId ? "#ff94b8" : "#ff7aa8"));
    const cpToleranceSize = cps.map((cp) => (cp.id === state.preset.selectedCpId ? 12 : 10));
    const selectedSpan = selectedControlSpanPoints(cps);

    const traces = [
      {
        name: "Эталон",
        x: signal.time,
        y: signal.value,
        type: "scattergl",
        mode: "lines",
        line: { color: "#74d0ff", width: 1.8 },
      },
      {
        name: "Верхний допуск",
        x: signal.time,
        y: state.preset.upper,
        type: "scattergl",
        mode: "lines",
        line: { color: "#ffb347", width: 1.6 },
      },
      {
        name: "Нижний допуск",
        x: signal.time,
        y: state.preset.lower,
        type: "scattergl",
        mode: "lines",
        line: { color: "#f78fb3", width: 1.6 },
      },
      {
        name: "Control points (ref)",
        x: cpTime,
        y: cpRef,
        type: "scatter",
        mode: "markers",
        marker: {
          color: cpRefColor,
          size: cpRefSize,
          symbol: "circle",
          line: { color: "#0f2034", width: cpRefLineWidth },
        },
      },
      {
        name: "Control +",
        x: cpTime,
        y: cpUp,
        type: "scatter",
        mode: "markers",
        marker: {
          color: cpUpColor,
          size: cpToleranceSize,
          symbol: "triangle-up",
          line: { color: "#704300", width: 1.5 },
        },
      },
      {
        name: "Control -",
        x: cpTime,
        y: cpDown,
        type: "scatter",
        mode: "markers",
        marker: {
          color: cpDownColor,
          size: cpToleranceSize,
          symbol: "triangle-down",
          line: { color: "#651d3d", width: 1.5 },
        },
      },
      {
        name: "Selected control span",
        x: selectedSpan.x,
        y: selectedSpan.y,
        type: "scatter",
        mode: "lines",
        hoverinfo: "skip",
        line: { color: "rgba(255,224,102,0.8)", width: 2.2, dash: "dot" },
      },
    ];

    const isEdit = state.preset.editMode === "edit";
    const layout = {
      margin: { t: 18, r: 24, b: 45, l: 58 },
      paper_bgcolor: "rgba(0,0,0,0)",
      plot_bgcolor: "rgba(255,255,255,0.02)",
      font: { color: "#dce6f2" },
      dragmode: isEdit ? false : "pan",
      xaxis: {
        title: "Time (ms, normalized from 0)",
        zeroline: true,
        zerolinecolor: "rgba(255,255,255,0.24)",
        gridcolor: "rgba(255,255,255,0.12)",
        tickformat: ",.0f",
        exponentformat: "none",
      },
      yaxis: {
        title: "Value",
        zeroline: true,
        zerolinecolor: "rgba(255,255,255,0.24)",
        gridcolor: "rgba(255,255,255,0.12)",
      },
      legend: { orientation: "h", x: 0, y: 1.14 },
    };

    const config = {
      responsive: true,
      displaylogo: false,
      scrollZoom: !isEdit,
      modeBarButtonsToRemove: ["select2d", "lasso2d"],
    };
    el.presetPlot.style.touchAction = isEdit ? "none" : "auto";
    el.presetPlot.style.cursor = isEdit ? "crosshair" : "";

    Plotly.react(el.presetPlot, traces, layout, config).then(() => {
      bindPresetPlotEvents();
    });
  }

  function refreshPresetPlotData() {
    const plot = el.presetPlot;
    const signal = state.preset.signal;
    if (!signal || !plot.data || plot.data.length < 7) {
      drawPresetPlot();
      return;
    }

    const cps = sortedControlPoints();
    state.preset.renderOrder = cps.map((cp) => cp.id);
    const cpTime = cps.map((cp) => cp.time);
    const cpRef = cps.map((cp) => cp.ref_value);
    const cpUp = cps.map((cp) => cp.ref_value + cp.tol_up);
    const cpDown = cps.map((cp) => cp.ref_value - cp.tol_down);
    const cpRefColor = cps.map((cp) => (cp.id === state.preset.selectedCpId ? "#ffe066" : "#62b0ff"));
    const cpRefSize = cps.map((cp) => (cp.id === state.preset.selectedCpId ? 14 : 11));
    const cpRefLineWidth = cps.map((cp) => (cp.id === state.preset.selectedCpId ? 2.2 : 1.4));
    const cpUpColor = cps.map((cp) => (cp.id === state.preset.selectedCpId ? "#ffd57a" : "#ffc857"));
    const cpDownColor = cps.map((cp) => (cp.id === state.preset.selectedCpId ? "#ff94b8" : "#ff7aa8"));
    const cpToleranceSize = cps.map((cp) => (cp.id === state.preset.selectedCpId ? 12 : 10));
    const selectedSpan = selectedControlSpanPoints(cps);

    Plotly.restyle(plot, { y: [state.preset.upper] }, [PRESET_TRACE.UPPER]);
    Plotly.restyle(plot, { y: [state.preset.lower] }, [PRESET_TRACE.LOWER]);
    Plotly.restyle(
      plot,
      { x: [cpTime], y: [cpRef], "marker.color": [cpRefColor], "marker.size": [cpRefSize], "marker.line.width": [cpRefLineWidth] },
      [PRESET_TRACE.CP_REF]
    );
    Plotly.restyle(plot, { x: [cpTime], y: [cpUp], "marker.color": [cpUpColor], "marker.size": [cpToleranceSize] }, [PRESET_TRACE.CP_UP]);
    Plotly.restyle(
      plot,
      { x: [cpTime], y: [cpDown], "marker.color": [cpDownColor], "marker.size": [cpToleranceSize] },
      [PRESET_TRACE.CP_DOWN]
    );
    Plotly.restyle(plot, { x: [selectedSpan.x], y: [selectedSpan.y] }, [PRESET_TRACE.CP_SELECTED_SPAN]);
  }

  function bindPresetPlotEvents() {
    const plot = el.presetPlot;
    if (typeof plot.removeAllListeners === "function") {
      plot.removeAllListeners("plotly_click");
    }

    plot.on("plotly_click", (ev) => {
      const point = ev.points?.[0];
      if (!point) {
        return;
      }

      const cpId = cpIdFromRenderedPoint(point.pointNumber);
      if (!cpId) {
        return;
      }

      if (
        point.curveNumber === PRESET_TRACE.CP_REF ||
        point.curveNumber === PRESET_TRACE.CP_UP ||
        point.curveNumber === PRESET_TRACE.CP_DOWN
      ) {
        state.preset.selectedCpId = cpId;
        if (point.curveNumber === PRESET_TRACE.CP_UP) {
          state.preset.dragTarget = "up";
        } else if (point.curveNumber === PRESET_TRACE.CP_DOWN) {
          state.preset.dragTarget = "down";
        }
        el.presetDragTarget.value = state.preset.dragTarget;
        refreshPresetPlotData();
        renderControlPointsTable();
      }
    });
  }

  function onPresetPlotPointerDown(event) {
    if (event.pointerType === "mouse" && event.button !== 0) {
      return;
    }
    if (state.preset.editMode !== "edit") {
      return;
    }
    if (!state.preset.signal) {
      return;
    }

    if (!state.preset.controlPoints.length) {
      refreshPresetPlotData();
      event.preventDefault();
      return;
    }

    const dragTarget = state.preset.dragTarget === "down" ? "down" : "up";
    const picked = findNearestControlPointOnPlot(el.presetPlot, event.clientX, event.clientY, dragTarget);
    if (!picked) {
      refreshPresetPlotData();
      renderControlPointsTable();
      event.preventDefault();
      return;
    }

    state.preset.selectedCpId = picked.cpId;
    state.preset.drag.active = true;
    state.preset.drag.cpId = picked.cpId;
    state.preset.drag.target = picked.target;
    state.preset.drag.pointerId = event.pointerId;
    if (typeof el.presetPlot.setPointerCapture === "function") {
      try {
        el.presetPlot.setPointerCapture(event.pointerId);
      } catch (error) {
        // ignore
      }
    }
    document.body.style.userSelect = "none";
    document.body.style.cursor = "ns-resize";
    document.body.style.overflow = "hidden";

    refreshPresetPlotData();
    renderControlPointsTable();
    event.preventDefault();
  }

  function onPresetDragMove(event) {
    const drag = state.preset.drag;
    if (!drag.active || !drag.cpId) {
      return;
    }
    if (drag.pointerId != null && event.pointerId != null && drag.pointerId !== event.pointerId) {
      return;
    }

    const cp = state.preset.controlPoints.find((item) => item.id === drag.cpId);
    if (!cp) {
      return;
    }

    const y = clientYToDataY(el.presetPlot, event.clientY);
    if (!Number.isFinite(y)) {
      return;
    }

    if (drag.target === "up") {
      cp.tol_up = Math.max(0, y - cp.ref_value);
    } else if (drag.target === "down") {
      cp.tol_down = Math.max(0, cp.ref_value - y);
    } else {
      return;
    }

    normalizeControlPoint(cp, state.preset.signal.time);
    rebuildEnvelopeFromControlPoints();
    refreshPresetPlotData();
    renderControlPointsTable();
    updatePresetSummary();
    event.preventDefault();
  }

  function endPresetDrag(event) {
    const drag = state.preset.drag;
    if (drag.pointerId != null && event?.pointerId != null && drag.pointerId !== event.pointerId) {
      return;
    }
    if (!drag.active) {
      return;
    }
    if (drag.pointerId != null && typeof el.presetPlot.releasePointerCapture === "function") {
      try {
        el.presetPlot.releasePointerCapture(drag.pointerId);
      } catch (error) {
        // ignore
      }
    }
    drag.active = false;
    drag.cpId = null;
    drag.target = state.preset.dragTarget;
    drag.pointerId = null;
    document.body.style.userSelect = "";
    document.body.style.cursor = "";
    document.body.style.overflow = "";
  }

  function findNearestControlPointOnPlot(plot, clientX, clientY, target) {
    const layout = plot?._fullLayout;
    if (!layout?.xaxis || !layout?.yaxis) {
      return null;
    }

    const rect = plot.getBoundingClientRect();
    const xAxis = layout.xaxis;
    const yAxis = layout.yaxis;
    const cps = sortedControlPoints();

    let best = null;
    let bestDist = 28;

    for (const cp of cps) {
      const xPx = rect.left + xAxis._offset + xAxis.l2p(cp.time);
      const yValue = target === "down" ? cp.ref_value - cp.tol_down : cp.ref_value + cp.tol_up;
      const yPx = rect.top + yAxis._offset + yAxis.l2p(yValue);
      const dist = Math.hypot(clientX - xPx, clientY - yPx);
      if (dist < bestDist) {
        bestDist = dist;
        best = { cpId: cp.id, target: target === "down" ? "down" : "up" };
      }
    }

    return best;
  }

  function onControlPointTableClick(event) {
    const actionBtn = event.target.closest("button[data-action]");
    if (actionBtn) {
      const cpId = actionBtn.dataset.id || null;
      const action = actionBtn.dataset.action || "";
      if (action === "cp-remove" && cpId) {
        removeControlPointById(cpId);
        return;
      }
      if ((action === "tol-plus" || action === "tol-minus") && cpId) {
        const cp = state.preset.controlPoints.find((item) => item.id === cpId);
        if (!cp || !state.preset.signal) {
          return;
        }
        const field = actionBtn.dataset.field === "tol_down" ? "tol_down" : "tol_up";
        const sign = action === "tol-minus" ? -1 : 1;
        cp[field] = Math.max(0, cp[field] + sign * CP_TOL_STEP);
        normalizeControlPoint(cp, state.preset.signal.time);
        state.preset.selectedCpId = cp.id;
        applyControlPointUpdates();
      }
      return;
    }

    const row = event.target.closest("tr[data-id]");
    if (!row) {
      return;
    }
    const cpId = row.dataset.id;
    if (!cpId) {
      return;
    }
    state.preset.selectedCpId = cpId;
    refreshPresetPlotData();
    renderControlPointsTable();
  }

  function onControlPointTableChange(event) {
    const input = event.target.closest("input[data-field]");
    if (!input) {
      return;
    }
    const cpId = input.dataset.id;
    const field = input.dataset.field;
    const cp = state.preset.controlPoints.find((item) => item.id === cpId);
    if (!cp || !state.preset.signal) {
      return;
    }

    const domain = state.preset.signal.time;
    const value = safeFloat(input.value);
    if (!Number.isFinite(value)) {
      return;
    }

    if (field === "time") {
      cp.time = clamp(Math.round(value), domain[0], domain[domain.length - 1]);
    } else if (field === "ref_value") {
      cp.ref_value = value;
    } else if (field === "tol_up") {
      cp.tol_up = Math.max(0, value);
    } else if (field === "tol_down") {
      cp.tol_down = Math.max(0, value);
    } else if (field === "importance") {
      cp.importance = clamp(value, 0.1, 10);
    }

    normalizeControlPoint(cp, domain);
    applyControlPointUpdates();
  }

  function renderControlPointsTable() {
    const tbody = el.controlPointsTableBody;
    tbody.innerHTML = "";

    const cps = sortedControlPoints();
    if (!cps.length) {
      const tr = document.createElement("tr");
      tr.innerHTML = '<td colspan="9">Control points не сформированы.</td>';
      tbody.appendChild(tr);
      return;
    }

    cps.forEach((cp, idx) => {
      const tr = document.createElement("tr");
      tr.dataset.id = cp.id;
      if (cp.id === state.preset.selectedCpId) {
        tr.classList.add("cp-selected");
      }
      tr.innerHTML = `
        <td>${idx + 1}</td>
        <td><input class="cp-cell-input" type="number" step="1" data-id="${cp.id}" data-field="time" value="${fmtNum(cp.time, 0)}" /></td>
        <td><input class="cp-cell-input" type="number" step="0.0001" data-id="${cp.id}" data-field="ref_value" value="${fmtNum(cp.ref_value, 5)}" /></td>
        <td>
          <div class="cp-tol-cell">
            <button class="cp-step-btn secondary" type="button" data-action="tol-minus" data-field="tol_up" data-id="${cp.id}">-</button>
            <input class="cp-cell-input" type="number" min="0" step="0.0001" data-id="${cp.id}" data-field="tol_up" value="${fmtNum(cp.tol_up, 5)}" />
            <button class="cp-step-btn secondary" type="button" data-action="tol-plus" data-field="tol_up" data-id="${cp.id}">+</button>
          </div>
        </td>
        <td>
          <div class="cp-tol-cell">
            <button class="cp-step-btn secondary" type="button" data-action="tol-minus" data-field="tol_down" data-id="${cp.id}">-</button>
            <input class="cp-cell-input" type="number" min="0" step="0.0001" data-id="${cp.id}" data-field="tol_down" value="${fmtNum(cp.tol_down, 5)}" />
            <button class="cp-step-btn secondary" type="button" data-action="tol-plus" data-field="tol_down" data-id="${cp.id}">+</button>
          </div>
        </td>
        <td><span class="cp-kind">${escapeHtml(cp.kind)}</span></td>
        <td><input class="cp-cell-input" type="number" min="0.1" max="10" step="0.1" data-id="${cp.id}" data-field="importance" value="${fmtNum(cp.importance, 2)}" /></td>
        <td class="${cp.status === "OK" ? "status-ok" : "status-alert"}">${cp.status || "OK"}</td>
        <td><button class="cp-remove-btn secondary" type="button" data-action="cp-remove" data-id="${cp.id}">-</button></td>
      `;
      tbody.appendChild(tr);
    });
  }

  function onAddControlPointFromTable() {
    const signal = state.preset.signal;
    if (!signal) {
      showAlert("Сначала загрузите эталонный сигнал.", "warn");
      return;
    }

    const timeVal = safeFloat(el.cpAddTime.value);
    if (!Number.isFinite(timeVal)) {
      showAlert("Укажите Time (ms) для добавления control point.", "warn");
      return;
    }

    const margin = Math.max(safeFloat(el.presetMargin.value) || 0, inferDefaultToleranceFromSignal(signal.value));
    const t = clamp(Math.round(timeVal), signal.time[0], signal.time[signal.time.length - 1]);
    const tolUp = Number.isFinite(safeFloat(el.cpAddTolUp.value)) ? Math.max(0, safeFloat(el.cpAddTolUp.value)) : margin;
    const tolDown = Number.isFinite(safeFloat(el.cpAddTolDown.value)) ? Math.max(0, safeFloat(el.cpAddTolDown.value)) : margin;
    const ref = interpolateAt(signal.time, signal.value, t);

    const cpData = {
      time: t,
      ref_value: ref,
      tol_up: tolUp,
      tol_down: tolDown,
      kind: "manual",
      importance: 1,
      status: "OK",
    };

    const nearCp = findControlPointNearTime(t, signal.time);
    if (nearCp) {
      nearCp.time = cpData.time;
      nearCp.ref_value = cpData.ref_value;
      nearCp.tol_up = cpData.tol_up;
      nearCp.tol_down = cpData.tol_down;
      nearCp.importance = Number.isFinite(nearCp.importance) ? nearCp.importance : 1;
      nearCp.kind = "manual";
      state.preset.selectedCpId = nearCp.id;
    } else {
      const cp = { id: nextControlPointId(), ...cpData };
      state.preset.controlPoints.push(cp);
      state.preset.selectedCpId = cp.id;
    }

    state.preset.controlPoints = reduceControlPointDensity(state.preset.controlPoints, getControlPointLimit());
    applyControlPointUpdates();
    showAlert(nearCp ? "Control point обновлен по времени." : "Control point добавлен по времени.", "ok", 1300);
  }

  function removeSelectedControlPoint() {
    if (!state.preset.selectedCpId) {
      showAlert("Сначала выберите control point.", "warn", 1500);
      return;
    }
    removeControlPointById(state.preset.selectedCpId);
  }

  function removeControlPointById(cpId) {
    const oldLen = state.preset.controlPoints.length;
    state.preset.controlPoints = state.preset.controlPoints.filter((cp) => cp.id !== cpId);
    if (state.preset.selectedCpId === cpId) {
      state.preset.selectedCpId = null;
    }
    if (state.preset.controlPoints.length !== oldLen) {
      applyControlPointUpdates();
    }
  }

  function applyControlPointUpdates() {
    if (!state.preset.signal) {
      return;
    }
    for (const cp of state.preset.controlPoints) {
      normalizeControlPoint(cp, state.preset.signal.time);
    }
    state.preset.controlPoints = reduceControlPointDensity(
      dedupeControlPointsByTime(state.preset.controlPoints),
      getControlPointLimit()
    );
    if (
      state.preset.selectedCpId &&
      !state.preset.controlPoints.some((cp) => cp.id === state.preset.selectedCpId)
    ) {
      state.preset.selectedCpId = null;
    }
    rebuildEnvelopeFromControlPoints();
    refreshPresetPlotData();
    renderControlPointsTable();
    updatePresetSummary();
  }

  async function onLoadSectionReference() {
    const file = el.sectionRefFile.files?.[0];
    if (!file) {
      showAlert("Выберите RSLZ файл эталона для секций.", "warn");
      return;
    }

    try {
      setBusy(el.loadSectionRef, true);
      const parsed = await parseRslzFile(file);
      state.sectionPreset.refParsed = parsed;
      state.sectionPreset.refFileName = file.name;
      state.sectionPreset.channelType = el.sectionChannelType.value;
      refreshSectionChannelIndexes();
      if (state.sectionPreset.loadedPreset) {
        applySeriesToSectionEditor({ preset: state.sectionPreset.loadedPreset });
      } else {
        applySeriesToSectionEditor();
      }
      showAlert(`Эталон секций загружен: ${file.name}`, "ok", 2200);
    } catch (error) {
      showAlert(`Ошибка загрузки эталона секций: ${error.message}`);
    } finally {
      setBusy(el.loadSectionRef, false);
    }
  }

  function refreshSectionChannelIndexes() {
    if (!state.sectionPreset.refParsed) {
      populateSelect(el.sectionChannelIndex, []);
      return;
    }
    const type = state.sectionPreset.channelType;
    const indexes = state.sectionPreset.refParsed.channels[type].indexes;
    populateSelect(el.sectionChannelIndex, indexes);
    if (indexes.length) {
      const current = state.sectionPreset.channelIndex;
      if (current == null || !indexes.includes(current)) {
        const loadedIdx = state.sectionPreset.loadedPreset?.channel_index;
        state.sectionPreset.channelIndex =
          Number.isFinite(loadedIdx) && indexes.includes(loadedIdx) ? loadedIdx : indexes[0];
      }
      el.sectionChannelIndex.value = String(state.sectionPreset.channelIndex);
    }
  }

  function applySeriesToSectionEditor(options = {}) {
    if (!state.sectionPreset.refParsed) {
      return;
    }
    const type = state.sectionPreset.channelType;
    const idx = state.sectionPreset.channelIndex ?? safeInt(el.sectionChannelIndex.value);
    const series = state.sectionPreset.refParsed.channels[type].seriesByIndex[idx];
    if (!series) {
      showAlert(`Канал ${type}:${idx} отсутствует в файле.`, "warn");
      return;
    }

    const time = normalizeTimeArray(series.time);
    const value = series.value.slice();
    if (time.length < 3) {
      showAlert("Недостаточно точек в канале для секционного пресета.", "warn");
      return;
    }

    state.sectionPreset.signal = { time, value };
    if (options.preset) {
      state.sectionPreset.sections = sanitizeSectionsArray(
        options.preset.sections,
        time,
        inferDefaultToleranceFromSignal(value)
      );
      state.sectionPreset.sectionCount = state.sectionPreset.sections.length || getSectionCount();
      el.sectionCount.value = String(state.sectionPreset.sectionCount);
    } else {
      state.sectionPreset.sectionCount = getSectionCount();
      state.sectionPreset.sections = buildEqualSections(time, state.sectionPreset.sectionCount, getSectionBaseTolerance());
      if (state.sectionPreset.sections.length !== state.sectionPreset.sectionCount) {
        state.sectionPreset.sectionCount = state.sectionPreset.sections.length;
        el.sectionCount.value = String(state.sectionPreset.sectionCount);
        showAlert(
          "Количество секций ограничено целочисленным диапазоном времени сигнала.",
          "warn",
          2200
        );
      }
    }

    state.sectionPreset.selectedIds.clear();
    rebuildSectionEnvelope();
    drawSectionPlot();
    renderSectionTable();
    updateSectionSummary();
  }

  function rebuildSectionsFromSignal() {
    const signal = state.sectionPreset.signal;
    if (!signal) {
      showAlert("Сначала загрузите эталонный сигнал для секций.", "warn");
      return;
    }
    state.sectionPreset.sectionCount = getSectionCount();
    state.sectionPreset.sections = buildEqualSections(signal.time, state.sectionPreset.sectionCount, getSectionBaseTolerance());
    if (state.sectionPreset.sections.length !== state.sectionPreset.sectionCount) {
      state.sectionPreset.sectionCount = state.sectionPreset.sections.length;
      el.sectionCount.value = String(state.sectionPreset.sectionCount);
      showAlert("Количество секций скорректировано под целочисленную шкалу времени.", "warn", 2200);
    }
    state.sectionPreset.selectedIds.clear();
    rebuildSectionEnvelope();
    drawSectionPlot();
    renderSectionTable();
    updateSectionSummary();
  }

  function getSectionCount() {
    const raw = safeInt(el.sectionCount.value);
    return clamp(Number.isFinite(raw) ? raw : SECTION_COUNT_DEFAULT, SECTION_COUNT_MIN, SECTION_COUNT_MAX);
  }

  function getSectionBaseTolerance() {
    const signal = state.sectionPreset.signal;
    const fallback = signal ? inferDefaultToleranceFromSignal(signal.value) : 0.5;
    const v = safeFloat(el.sectionBaseTol.value);
    return Math.max(0, Number.isFinite(v) ? v : fallback);
  }

  function buildEqualSections(time, count, baseTol) {
    if (!time.length || count < 1) {
      return [];
    }
    const t0 = Math.round(time[0]);
    const t1 = Math.round(time[time.length - 1]);
    const span = Math.max(0, t1 - t0);
    const maxCountByIntegerMs = Math.max(1, span);
    const safeCount = clamp(Math.trunc(count), 1, maxCountByIntegerMs);
    const out = [];
    for (let i = 0; i < safeCount; i += 1) {
      const start = t0 + Math.floor((i * span) / safeCount);
      const end = i === safeCount - 1 ? t1 : t0 + Math.floor(((i + 1) * span) / safeCount);
      out.push({
        id: nextSectionId(),
        start_time: start,
        end_time: end,
        tol_up: baseTol,
        tol_down: baseTol,
        importance: 1,
        active: true,
      });
    }
    return out;
  }

  function nextSectionId() {
    const id = `sec_${Date.now()}_${state.sectionPreset.idCounter}`;
    state.sectionPreset.idCounter += 1;
    return id;
  }

  function sanitizeSectionsArray(sections, domainTime, defaultTol) {
    if (!Array.isArray(sections) || !sections.length) {
      return [];
    }
    const t0 = Math.round(domainTime[0]);
    const t1 = Math.round(domainTime[domainTime.length - 1]);
    const out = [];
    for (const item of sections) {
      if (!item || typeof item !== "object") {
        continue;
      }
      let start = clamp(Math.round(safeFloat(item.start_time)), t0, t1);
      let end = clamp(Math.round(safeFloat(item.end_time)), t0, t1);
      if (!Number.isFinite(start) || !Number.isFinite(end)) {
        continue;
      }
      if (end < start) {
        const tmp = start;
        start = end;
        end = tmp;
      }
      if (end <= start) {
        end = Math.min(t1, start + 1);
      }
      if (end <= start) {
        continue;
      }

      out.push({
        id: typeof item.id === "string" && item.id ? item.id : nextSectionId(),
        start_time: start,
        end_time: end,
        tol_up: Math.max(0, Number.isFinite(safeFloat(item.tol_up)) ? safeFloat(item.tol_up) : defaultTol),
        tol_down: Math.max(0, Number.isFinite(safeFloat(item.tol_down)) ? safeFloat(item.tol_down) : defaultTol),
        importance: clamp(Number.isFinite(safeFloat(item.importance)) ? safeFloat(item.importance) : 1, 0.1, 10),
        active: item.active !== false,
      });
    }
    out.sort((a, b) => a.start_time - b.start_time);
    return out;
  }

  function getSectionForTime(sections, t) {
    if (!sections.length) {
      return null;
    }
    for (let i = 0; i < sections.length; i += 1) {
      const sec = sections[i];
      const isLast = i === sections.length - 1;
      if (t >= sec.start_time && (t < sec.end_time || (isLast && t <= sec.end_time + 1e-9))) {
        return sec;
      }
    }
    if (t <= sections[0].start_time) {
      return sections[0];
    }
    return sections[sections.length - 1];
  }

  function findSectionInRange(sections, t) {
    if (!sections.length) {
      return null;
    }
    if (t < sections[0].start_time || t > sections[sections.length - 1].end_time + 1e-9) {
      return null;
    }
    return getSectionForTime(sections, t);
  }

  function rebuildSectionEnvelope() {
    const signal = state.sectionPreset.signal;
    if (!signal) {
      return;
    }
    const upper = [];
    const lower = [];
    for (let i = 0; i < signal.time.length; i += 1) {
      const t = signal.time[i];
      const ref = signal.value[i];
      const sec = getSectionForTime(state.sectionPreset.sections, t);
      if (!sec || !sec.active) {
        upper.push(ref);
        lower.push(ref);
      } else {
        upper.push(ref + sec.tol_up);
        lower.push(ref - sec.tol_down);
      }
    }
    state.sectionPreset.upper = upper;
    state.sectionPreset.lower = lower;
    enforceBoundsOrder(state.sectionPreset.lower, state.sectionPreset.upper);
  }

  function drawSectionPlot() {
    const signal = state.sectionPreset.signal;
    if (!signal) {
      Plotly.purge(el.sectionPlot);
      return;
    }

    const shapes = state.sectionPreset.sections.map((sec, idx) => {
      const selected = state.sectionPreset.selectedIds.has(sec.id);
      const base = sec.active
        ? idx % 2 === 0
          ? "rgba(98,176,255,0.08)"
          : "rgba(98,176,255,0.05)"
        : "rgba(130,130,130,0.08)";
      return {
        type: "rect",
        xref: "x",
        yref: "paper",
        x0: sec.start_time,
        x1: sec.end_time,
        y0: 0,
        y1: 1,
        fillcolor: base,
        line: {
          color: selected ? "rgba(255,224,102,0.95)" : "rgba(255,255,255,0.12)",
          width: selected ? 2 : 1,
        },
      };
    });

    const traces = [
      {
        name: "Эталон",
        x: signal.time,
        y: signal.value,
        type: "scattergl",
        mode: "lines",
        line: { color: "#74d0ff", width: 1.9 },
      },
      {
        name: "Верхний допуск",
        x: signal.time,
        y: state.sectionPreset.upper,
        type: "scattergl",
        mode: "lines",
        line: { color: "#ffb347", width: 1.5 },
      },
      {
        name: "Нижний допуск",
        x: signal.time,
        y: state.sectionPreset.lower,
        type: "scattergl",
        mode: "lines",
        line: { color: "#f78fb3", width: 1.5 },
      },
    ];

    const layout = {
      margin: { t: 18, r: 24, b: 45, l: 58 },
      paper_bgcolor: "rgba(0,0,0,0)",
      plot_bgcolor: "rgba(255,255,255,0.02)",
      font: { color: "#dce6f2" },
      dragmode: "pan",
      shapes,
      xaxis: {
        title: "Time (ms, normalized from 0)",
        zeroline: true,
        zerolinecolor: "rgba(255,255,255,0.24)",
        gridcolor: "rgba(255,255,255,0.12)",
        tickformat: ",.0f",
        exponentformat: "none",
      },
      yaxis: {
        title: "Value",
        zeroline: true,
        zerolinecolor: "rgba(255,255,255,0.24)",
        gridcolor: "rgba(255,255,255,0.12)",
      },
      legend: { orientation: "h", x: 0, y: 1.14 },
    };

    Plotly.react(el.sectionPlot, traces, layout, {
      responsive: true,
      displaylogo: false,
      scrollZoom: true,
      modeBarButtonsToRemove: ["select2d", "lasso2d"],
    });
  }

  function renderSectionTable() {
    const tbody = el.sectionTableBody;
    tbody.innerHTML = "";
    const sections = state.sectionPreset.sections;
    if (!sections.length) {
      const tr = document.createElement("tr");
      tr.innerHTML = '<td colspan="8">Секции не сформированы.</td>';
      tbody.appendChild(tr);
      return;
    }

    sections.forEach((sec, idx) => {
      const tr = document.createElement("tr");
      tr.dataset.id = sec.id;
      if (state.sectionPreset.selectedIds.has(sec.id)) {
        tr.classList.add("cp-selected");
      }
      tr.innerHTML = `
        <td><input type="checkbox" data-id="${sec.id}" data-field="selected" ${state.sectionPreset.selectedIds.has(sec.id) ? "checked" : ""} /></td>
        <td>${idx + 1}</td>
        <td>${fmtNum(sec.start_time, 0)}</td>
        <td>${fmtNum(sec.end_time, 0)}</td>
        <td><input class="cp-cell-input" type="number" min="0" step="0.01" data-id="${sec.id}" data-field="tol_up" value="${fmtNum(sec.tol_up, 4)}" /></td>
        <td><input class="cp-cell-input" type="number" min="0" step="0.01" data-id="${sec.id}" data-field="tol_down" value="${fmtNum(sec.tol_down, 4)}" /></td>
        <td><input class="cp-cell-input" type="number" min="0.1" step="0.1" data-id="${sec.id}" data-field="importance" value="${fmtNum(sec.importance, 2)}" /></td>
        <td><input type="checkbox" data-id="${sec.id}" data-field="active" ${sec.active ? "checked" : ""} /></td>
      `;
      tbody.appendChild(tr);
    });
  }

  function onSectionTableClick(event) {
    const row = event.target.closest("tr[data-id]");
    if (!row || event.target.closest("input")) {
      return;
    }
    const id = row.dataset.id;
    if (!id) {
      return;
    }
    if (state.sectionPreset.selectedIds.has(id)) {
      state.sectionPreset.selectedIds.delete(id);
    } else {
      state.sectionPreset.selectedIds.add(id);
    }
    renderSectionTable();
    drawSectionPlot();
    updateSectionSummary();
  }

  function onSectionTableChange(event) {
    const input = event.target.closest("input[data-field]");
    if (!input) {
      return;
    }
    const id = input.dataset.id;
    const field = input.dataset.field;
    const section = state.sectionPreset.sections.find((sec) => sec.id === id);
    if (!section) {
      return;
    }

    if (field === "selected") {
      if (input.checked) {
        state.sectionPreset.selectedIds.add(id);
      } else {
        state.sectionPreset.selectedIds.delete(id);
      }
      drawSectionPlot();
      updateSectionSummary();
      return;
    }

    if (field === "active") {
      section.active = Boolean(input.checked);
      rebuildSectionEnvelope();
      drawSectionPlot();
      updateSectionSummary();
      return;
    }

    const value = safeFloat(input.value);
    if (!Number.isFinite(value)) {
      return;
    }
    if (field === "tol_up") {
      section.tol_up = Math.max(0, value);
    } else if (field === "tol_down") {
      section.tol_down = Math.max(0, value);
    } else if (field === "importance") {
      section.importance = clamp(value, 0.1, 10);
    }

    rebuildSectionEnvelope();
    drawSectionPlot();
    updateSectionSummary();
  }

  function selectAllSections() {
    state.sectionPreset.selectedIds = new Set(state.sectionPreset.sections.map((sec) => sec.id));
    renderSectionTable();
    drawSectionPlot();
    updateSectionSummary();
  }

  function clearSectionSelection() {
    state.sectionPreset.selectedIds.clear();
    renderSectionTable();
    drawSectionPlot();
    updateSectionSummary();
  }

  function toggleSelectedSections(active) {
    let changed = 0;
    for (const sec of state.sectionPreset.sections) {
      if (state.sectionPreset.selectedIds.has(sec.id)) {
        sec.active = active;
        changed += 1;
      }
    }
    if (!changed) {
      showAlert("Не выбраны секции для изменения.", "warn", 1400);
      return;
    }
    rebuildSectionEnvelope();
    renderSectionTable();
    drawSectionPlot();
    updateSectionSummary();
  }

  function splitSelectedSection() {
    if (state.sectionPreset.sections.length >= SECTION_COUNT_MAX) {
      showAlert(`Достигнут лимит секций (${SECTION_COUNT_MAX}).`, "warn", 1700);
      return;
    }
    const selected = Array.from(state.sectionPreset.selectedIds);
    if (selected.length !== 1) {
      showAlert("Для разделения выберите ровно одну секцию.", "warn", 1700);
      return;
    }

    const sectionId = selected[0];
    const index = state.sectionPreset.sections.findIndex((sec) => sec.id === sectionId);
    if (index < 0) {
      showAlert("Выбранная секция не найдена.", "warn", 1700);
      return;
    }

    const source = state.sectionPreset.sections[index];
    const start = Math.round(source.start_time);
    const end = Math.round(source.end_time);
    const span = end - start;
    if (span < 2) {
      showAlert("Секцию нельзя разделить: ширина меньше 2 ms.", "warn", 1900);
      return;
    }

    const mid = start + Math.floor(span / 2);
    const left = {
      id: nextSectionId(),
      start_time: start,
      end_time: mid,
      tol_up: source.tol_up,
      tol_down: source.tol_down,
      importance: source.importance,
      active: source.active,
    };
    const right = {
      id: nextSectionId(),
      start_time: mid,
      end_time: end,
      tol_up: source.tol_up,
      tol_down: source.tol_down,
      importance: source.importance,
      active: source.active,
    };

    if (!(left.end_time > left.start_time) || !(right.end_time > right.start_time)) {
      showAlert("Секцию нельзя разделить: некорректная ширина после midpoint.", "warn", 1900);
      return;
    }

    state.sectionPreset.sections.splice(index, 1, left, right);
    state.sectionPreset.selectedIds = new Set([left.id, right.id]);
    state.sectionPreset.sectionCount = state.sectionPreset.sections.length;
    el.sectionCount.value = String(state.sectionPreset.sectionCount);

    rebuildSectionEnvelope();
    renderSectionTable();
    drawSectionPlot();
    updateSectionSummary();
    showAlert("Секция разделена на две.", "ok", 1300);
  }

  function applyBulkToSelectedSections() {
    const tolUp = safeFloat(el.sectionBulkTolUp.value);
    const tolDown = safeFloat(el.sectionBulkTolDown.value);
    const importance = safeFloat(el.sectionBulkImportance.value);
    const hasTolUp = Number.isFinite(tolUp);
    const hasTolDown = Number.isFinite(tolDown);
    const hasImportance = Number.isFinite(importance);
    if (!hasTolUp && !hasTolDown && !hasImportance) {
      showAlert("Укажите хотя бы один bulk-параметр.", "warn", 1400);
      return;
    }

    let changed = 0;
    for (const sec of state.sectionPreset.sections) {
      if (!state.sectionPreset.selectedIds.has(sec.id)) {
        continue;
      }
      if (hasTolUp) {
        sec.tol_up = Math.max(0, tolUp);
      }
      if (hasTolDown) {
        sec.tol_down = Math.max(0, tolDown);
      }
      if (hasImportance) {
        sec.importance = clamp(importance, 0.1, 10);
      }
      changed += 1;
    }
    if (!changed) {
      showAlert("Не выбраны секции для bulk-изменения.", "warn", 1400);
      return;
    }
    rebuildSectionEnvelope();
    renderSectionTable();
    drawSectionPlot();
    updateSectionSummary();
  }

  async function onLoadSectionPresetJson() {
    const file = el.sectionPresetJsonInput.files?.[0];
    if (!file) {
      return;
    }

    try {
      const json = JSON.parse(await file.text());
      const preset = validateSectionPresetObject(json);
      state.sectionPreset.loadedPreset = preset;
      if (preset.preset_name) {
        el.sectionPresetName.value = preset.preset_name;
      }
      if (preset.channel_type) {
        state.sectionPreset.channelType = preset.channel_type;
        el.sectionChannelType.value = preset.channel_type;
      }

      if (state.sectionPreset.refParsed) {
        refreshSectionChannelIndexes();
        if (preset.channel_index != null) {
          const maybe = String(preset.channel_index);
          if (Array.from(el.sectionChannelIndex.options).some((o) => o.value === maybe)) {
            el.sectionChannelIndex.value = maybe;
            state.sectionPreset.channelIndex = preset.channel_index;
          }
        }
        applySeriesToSectionEditor({ preset });
      } else {
        hydrateSectionPresetWithoutReference(preset);
      }
      showAlert(`Section preset загружен: ${file.name}`, "ok", 2200);
    } catch (error) {
      showAlert(`Ошибка section preset: ${error.message}`);
    }
  }

  function hydrateSectionPresetWithoutReference(preset) {
    const time = normalizeTimeArray(preset.reference.time);
    const value = preset.reference.value.slice();
    state.sectionPreset.signal = { time, value };
    state.sectionPreset.sections = sanitizeSectionsArray(
      preset.sections,
      time,
      inferDefaultToleranceFromSignal(value)
    );
    state.sectionPreset.sectionCount = state.sectionPreset.sections.length || SECTION_COUNT_DEFAULT;
    el.sectionCount.value = String(state.sectionPreset.sectionCount);
    state.sectionPreset.selectedIds.clear();
    rebuildSectionEnvelope();
    drawSectionPlot();
    renderSectionTable();
    updateSectionSummary();
  }

  function onSaveSectionPresetJson() {
    try {
      const signal = state.sectionPreset.signal;
      if (!signal || !state.sectionPreset.sections.length) {
        throw new Error("Нет данных для сохранения. Загрузите эталон и сформируйте секции.");
      }
      const sections = state.sectionPreset.sections.map((sec) => ({
        id: sec.id,
        start_time: Math.round(sec.start_time),
        end_time: Math.round(sec.end_time),
        tol_up: roundNum(sec.tol_up, 8),
        tol_down: roundNum(sec.tol_down, 8),
        importance: roundNum(sec.importance, 4),
        active: sec.active !== false,
      }));

      const preset = {
        preset_type: "section_v1",
        preset_name: (el.sectionPresetName.value || "АКПП_секции_v1").trim() || "АКПП_секции_v1",
        channel_type: state.sectionPreset.channelType,
        channel_index: state.sectionPreset.channelIndex,
        reference: {
          time: signal.time.slice(),
          value: signal.value.slice(),
        },
        sections,
        meta: {
          source_file: state.sectionPreset.refFileName || "",
          saved_at: new Date().toISOString(),
          section_count: sections.length,
        },
      };
      downloadJson(`${sanitizeFileName(preset.preset_name)}.json`, preset);
      showAlert("Section preset JSON сохранен.", "ok", 2200);
    } catch (error) {
      showAlert(`Не удалось сохранить section preset: ${error.message}`);
    }
  }

  function updateSectionSummary() {
    const signal = state.sectionPreset.signal;
    if (!signal) {
      el.sectionSummary.textContent = "Ожидание данных...";
      return;
    }
    const sections = state.sectionPreset.sections;
    const activeCount = sections.filter((sec) => sec.active).length;
    const selected = state.sectionPreset.selectedIds.size;
    const avgTol = avgArray(
      sections.map((sec) => 0.5 * (Math.max(0, sec.tol_up) + Math.max(0, sec.tol_down)))
    );
    el.sectionSummary.textContent = [
      `Source: ${state.sectionPreset.refFileName || "(manual/preset)"}`,
      `Channel: ${state.sectionPreset.channelType}:${state.sectionPreset.channelIndex ?? "-"}`,
      `Points: ${signal.time.length}`,
      `Sections: ${sections.length} (active ${activeCount})`,
      `Selected: ${selected}`,
      `Average Tol: ${fmtNum(avgTol, 4)}`,
    ].join("\n");
  }

  async function onLoadAnalysisRslz() {
    const file = el.analysisRslzInput.files?.[0];
    if (!file) {
      return;
    }

    try {
      setBusy(el.runAnalysis, true, "Загрузка...");
      const parsed = await parseRslzFile(file);
      state.analysis.testParsed = parsed;
      state.analysis.testFileName = file.name;
      state.analysis.channelType = el.analysisChannelType.value;

      refreshAnalysisChannelIndexes();
      showAlert(`Тестовый RSLZ загружен: ${file.name}`, "ok", 2000);
      updateAnalysisSummary();
    } catch (error) {
      showAlert(`Ошибка загрузки тестового RSLZ: ${error.message}`);
    } finally {
      setBusy(el.runAnalysis, false);
    }
  }

  async function onLoadAnalysisPreset() {
    const file = el.analysisPresetInput.files?.[0];
    if (!file) {
      return;
    }

    try {
      const json = JSON.parse(await file.text());
      const preset = parseAnyPresetObject(json);
      state.analysis.presetObj = preset;

      if (preset.channel_type) {
        state.analysis.channelType = preset.channel_type;
        el.analysisChannelType.value = preset.channel_type;
      }

      if (state.analysis.testParsed) {
        refreshAnalysisChannelIndexes();
        if (preset.channel_index != null) {
          const v = String(preset.channel_index);
          if (Array.from(el.analysisChannelIndex.options).some((opt) => opt.value === v)) {
            state.analysis.channelIndex = preset.channel_index;
            el.analysisChannelIndex.value = v;
          }
        }
      }

      showAlert(`Preset загружен: ${file.name}`, "ok", 2000);
      updateAnalysisSummary();
    } catch (error) {
      showAlert(`Ошибка пресета: ${error.message}`);
    }
  }

  function refreshAnalysisChannelIndexes() {
    if (!state.analysis.testParsed) {
      populateSelect(el.analysisChannelIndex, []);
      return;
    }

    const type = state.analysis.channelType;
    const indexes = state.analysis.testParsed.channels[type].indexes;
    populateSelect(el.analysisChannelIndex, indexes);

    if (indexes.length) {
      const current = state.analysis.channelIndex;
      if (current == null || !indexes.includes(current)) {
        const presetIdx = state.analysis.presetObj?.channel_index;
        if (Number.isFinite(presetIdx) && indexes.includes(presetIdx)) {
          state.analysis.channelIndex = presetIdx;
        } else {
          state.analysis.channelIndex = indexes[0];
        }
      }
      el.analysisChannelIndex.value = String(state.analysis.channelIndex);
    }
  }

  function runAnalysis() {
    try {
      if (!state.analysis.testParsed) {
        throw new Error("Загрузите тестовый RSLZ.");
      }
      if (!state.analysis.presetObj) {
        throw new Error("Загрузите preset JSON.");
      }

      const channelType = el.analysisChannelType.value;
      const channelIndex = safeInt(el.analysisChannelIndex.value);
      const series = state.analysis.testParsed.channels[channelType].seriesByIndex[channelIndex];

      if (!series) {
        throw new Error(`Канал ${channelType}:${channelIndex} отсутствует в тестовом файле.`);
      }

      const valueThr = Math.max(1e-9, safeFloat(el.valueThr.value) || 0.5);
      const timeThr = Math.max(1e-9, safeFloat(el.timeThr.value) || 50);

      const result =
        state.analysis.presetObj?.preset_type === "section_v1"
          ? analyzeSignalWithSectionPreset({
              series,
              preset: state.analysis.presetObj,
            })
          : analyzeSignalWithPreset({
              series,
              preset: state.analysis.presetObj,
              valueThr,
              timeThr,
            });

      state.analysis.result = result;
      state.analysis.channelType = channelType;
      state.analysis.channelIndex = channelIndex;

      drawAnalysisPlot(result);
      renderExtremaTable(result.extremaRows);
      renderControlPointsStatusTable(result.controlPointRows);
      renderSegmentWeightsEditor(result);
      renderMetrics(result.metrics, result.cpLoss);
      renderScore(result.score, result.mode);
      renderGemmaStub(result);
      updateAnalysisSummary();

      if (result.mode === "section" && result.durationMismatch) {
        showAlert(`Анализ выполнен. ${result.durationMismatch}`, "warn", 4200);
      } else {
        showAlert("Анализ выполнен.", "ok", 1500);
      }
    } catch (error) {
      showAlert(`Ошибка анализа: ${error.message}`);
    }
  }

  function analyzeSignalWithPreset({ series, preset, valueThr, timeThr }) {
    const testTime = normalizeTimeArray(series.time);
    const testValue = series.value.slice();
    if (testTime.length < 3) {
      throw new Error("В тестовом канале слишком мало точек.");
    }

    const presetTime = normalizeTimeArray(preset.time);
    const upper = interpolateLinear(presetTime, preset.upper_limit, testTime);
    const lower = interpolateLinear(presetTime, preset.lower_limit, testTime);
    enforceBoundsOrder(lower, upper);

    const refMid = midpointArray(upper, lower);
    const smoothTest = smoothSignal(testValue, 11);
    const smoothRef = smoothSignal(refMid, 11);

    const refExtrema = detectExtrema(testTime, refMid, smoothRef, {
      distance: 10,
      prominence: null,
    });
    const testExtrema = detectExtrema(testTime, testValue, smoothTest, {
      distance: 10,
      prominence: null,
    });

    const extremaRows = compareExtrema(refExtrema, testExtrema, valueThr, timeThr);

    const segments = buildSegmentsFromExtrema(
      testTime,
      testExtrema,
      Array.isArray(preset.segment_weights) ? preset.segment_weights : []
    );

    const pointPenalties = new Array(testTime.length).fill(0);
    const alertX = [];
    const alertY = [];

    for (let i = 0; i < testTime.length; i += 1) {
      const p = pointPenalty(testValue[i], lower[i], upper[i]);
      pointPenalties[i] = p;
      if (p > 0) {
        alertX.push(testTime[i]);
        alertY.push(testValue[i]);
      }
    }

    const okPoints = sampleOkPoints(testTime, testValue, pointPenalties, 2500);

    const controlPoints = getControlPointsForAnalysis(preset, testTime, refMid, upper, lower);
    const controlPointRows = evaluateControlPoints(controlPoints, testTime, testValue);
    const cpLoss = computeControlPointsLoss(controlPointRows);

    const extremaStatusByTime = buildExtremaStatusMap(extremaRows);
    const extremaX = [];
    const extremaY = [];
    const extremaColor = [];
    for (const point of testExtrema) {
      extremaX.push(point.time);
      extremaY.push(point.value);
      const key = `${roundNum(point.time, 3)}|${roundNum(point.value, 3)}`;
      extremaColor.push(extremaStatusByTime.get(key) === "Alert" ? "#ff4f5e" : "#2bd67b");
    }

    const cpOk = { x: [], y: [] };
    const cpAlert = { x: [], y: [] };
    for (const row of controlPointRows) {
      if (!Number.isFinite(row.TestValue)) {
        continue;
      }
      if (row.Status === "OK") {
        cpOk.x.push(row.Time);
        cpOk.y.push(row.TestValue);
      } else {
        cpAlert.x.push(row.Time);
        cpAlert.y.push(row.TestValue);
      }
    }

    const metrics = {
      test: calcSignalMetrics(testTime, testValue),
      reference: calcSignalMetrics(testTime, refMid),
    };

    const result = {
      mode: "classic",
      channel: {
        type: state.analysis.channelType,
        index: state.analysis.channelIndex,
      },
      testTime,
      testValue,
      upper,
      lower,
      refMid,
      segments,
      extremaRows,
      refExtrema,
      testExtrema,
      pointPenalties,
      okPoints,
      alerts: {
        x: alertX,
        y: alertY,
      },
      extremaMarks: {
        x: extremaX,
        y: extremaY,
        color: extremaColor,
      },
      controlPointRows,
      cpLoss,
      cpMarkers: {
        ok: cpOk,
        alert: cpAlert,
      },
      metrics,
      thresholds: {
        valueThr,
        timeThr,
      },
    };

    recomputeAnalysisScore(result);
    return result;
  }

  function analyzeSignalWithSectionPreset({ series, preset }) {
    const testTime = normalizeTimeArray(series.time);
    const testValue = series.value.slice();
    if (testTime.length < 3) {
      throw new Error("В тестовом канале слишком мало точек.");
    }

    const refTime = normalizeTimeArray(preset.reference.time);
    const refValue = preset.reference.value.slice();
    const refMid = interpolateLinear(refTime, refValue, testTime);
    const sections = sanitizeSectionsArray(
      preset.sections,
      refTime.length ? refTime : testTime,
      Math.max(inferDefaultToleranceFromSignal(refValue), 0.05)
    );
    if (!sections.length) {
      throw new Error("Section preset не содержит корректных секций.");
    }

    const testEnd = testTime[testTime.length - 1];
    const refEnd = refTime.length ? refTime[refTime.length - 1] : testEnd;
    const sectionsEnd = sections.reduce((maxT, sec) => Math.max(maxT, sec.end_time), 0);
    const presetEnd = Math.max(refEnd, sectionsEnd);
    const analysisEnd = Math.min(testEnd, presetEnd);
    if (!Number.isFinite(analysisEnd)) {
      throw new Error("Не удалось определить диапазон анализа для section preset.");
    }

    const upper = [];
    const lower = [];
    const pointPenalties = new Array(testTime.length).fill(0);
    const alertX = [];
    const alertY = [];
    const sectionAlertMap = new Map(sections.map((sec) => [sec.id, false]));
    const excludedX = [];
    const excludedY = [];
    let analyzedPoints = 0;

    for (let i = 0; i < testTime.length; i += 1) {
      const t = testTime[i];
      const ref = refMid[i];
      if (t > analysisEnd + 1e-9) {
        upper.push(ref);
        lower.push(ref);
        excludedX.push(t);
        excludedY.push(testValue[i]);
        continue;
      }
      analyzedPoints += 1;
      const sec = findSectionInRange(sections, t);
      if (!sec || !sec.active) {
        upper.push(ref);
        lower.push(ref);
        continue;
      }
      const up = ref + sec.tol_up;
      const low = ref - sec.tol_down;
      upper.push(up);
      lower.push(low);
      const penalty = pointPenalty(testValue[i], low, up);
      pointPenalties[i] = penalty;
      if (penalty > 0) {
        alertX.push(t);
        alertY.push(testValue[i]);
        sectionAlertMap.set(sec.id, true);
      }
    }
    enforceBoundsOrder(lower, upper);
    if (analyzedPoints < 3) {
      throw new Error("Пересечение времени теста и пресета слишком короткое для анализа.");
    }

    const segments = sections.map((sec) => ({
      id: sec.id,
      start: sec.start_time,
      end: sec.end_time,
      weight: sec.active ? sec.importance : 0,
      criticality: 1,
      avgPenalty: 0,
      active: sec.active,
    }));
    const segmentLoss = computeSegmentLosses(testTime, pointPenalties, segments);

    const sectionOverlays = sections.map((sec, idx) => ({
      id: sec.id,
      start: sec.start_time,
      end: sec.end_time,
      active: sec.active,
      hasAlert: sectionAlertMap.get(sec.id) === true,
      index: idx,
    }));
    const sectionScore = computeSectionWeightedScore(sectionOverlays, segments, {
      start: 0,
      end: analysisEnd,
    });
    if (sectionScore.activeWeight <= 0) {
      throw new Error("Нет активных секций в анализируемом диапазоне времени.");
    }

    const mismatchParts = [];
    let ignoredRange = null;
    if (testEnd > presetEnd + 1e-9) {
      const firstIgnored = testTime.find((t) => t > analysisEnd + 1e-9);
      if (Number.isFinite(firstIgnored)) {
        ignoredRange = {
          start: Math.round(firstIgnored),
          end: Math.round(testEnd),
        };
        mismatchParts.push(`Исключено время теста ${ignoredRange.start}..${ignoredRange.end} ms (вне пресета).`);
      }
    }
    if (presetEnd > testEnd + 1e-9) {
      mismatchParts.push(
        `Пресет длиннее теста: не покрыт диапазон ${Math.round(testEnd)}..${Math.round(presetEnd)} ms.`
      );
    }

    const result = {
      mode: "section",
      channel: {
        type: state.analysis.channelType,
        index: state.analysis.channelIndex,
      },
      testTime,
      testValue,
      upper,
      lower,
      refMid,
      segments,
      sectionOverlays,
      sectionScore,
      analysisRange: {
        start: 0,
        end: Math.round(analysisEnd),
      },
      ignoredRange,
      durationMismatch: mismatchParts.join(" "),
      extremaRows: [],
      refExtrema: [],
      testExtrema: [],
      pointPenalties,
      okPoints: sampleOkPoints(testTime, testValue, pointPenalties, 2500),
      alerts: {
        x: alertX,
        y: alertY,
      },
      extremaMarks: {
        x: [],
        y: [],
        color: [],
      },
      controlPointRows: [],
      cpLoss: { normalized: 0, weightedLoss: 0, weightNorm: 1 },
      cpMarkers: {
        ok: { x: [], y: [] },
        alert: { x: [], y: [] },
      },
      excludedPoints: {
        x: excludedX,
        y: excludedY,
      },
      metrics: {
        test: calcSignalMetrics(testTime, testValue),
        reference: calcSignalMetrics(testTime, refMid),
      },
      thresholds: {
        valueThr: 1,
        timeThr: 1,
      },
      segmentLoss,
      extremaLoss: { normalized: 0 },
      baseLoss: sectionScore.redWeightRatio,
      totalLoss: sectionScore.redWeightRatio,
      score: sectionScore.score,
    };

    return result;
  }

  function getControlPointsForAnalysis(preset, time, refMid, upper, lower) {
    if (Array.isArray(preset.control_points) && preset.control_points.length) {
      const avgTol = Math.max(avgArray(diffArray(upper, lower)) * 0.5, 0.05);
      return sanitizeControlPoints(preset.control_points, time, avgTol);
    }
    return buildControlPointsFromLegacyBounds(time, upper, lower, 120);
  }

  function recomputeAnalysisScore(result) {
    if (result.mode === "section") {
      result.segmentLoss = computeSegmentLosses(result.testTime, result.pointPenalties, result.segments);
      result.sectionScore = computeSectionWeightedScore(
        result.sectionOverlays || [],
        result.segments || [],
        result.analysisRange || { start: 0, end: result.testTime[result.testTime.length - 1] }
      );
      result.extremaLoss = { normalized: 0 };
      result.baseLoss = result.sectionScore.redWeightRatio;
      result.totalLoss = result.sectionScore.redWeightRatio;
      result.score = result.sectionScore.score;
      return;
    }
    result.segmentLoss = computeSegmentLosses(result.testTime, result.pointPenalties, result.segments);
    result.extremaLoss = computeExtremaPenalty(
      result.extremaRows,
      result.thresholds.valueThr,
      result.thresholds.timeThr
    );
    result.baseLoss = clamp(0.75 * result.segmentLoss.normalized + 0.25 * result.extremaLoss.normalized, 0, 1);
    result.totalLoss = clamp(0.7 * result.baseLoss + 0.3 * result.cpLoss.normalized, 0, 1);
    result.score = clamp(100 * (1 - result.totalLoss), 0, 100);
  }

  function drawAnalysisPlot(result) {
    const shapes = [];
    if (result.mode === "section" && Array.isArray(result.sectionOverlays)) {
      for (const sec of result.sectionOverlays) {
        const baseFill = sec.active
          ? sec.index % 2 === 0
            ? "rgba(98,176,255,0.05)"
            : "rgba(98,176,255,0.03)"
          : "rgba(120,120,120,0.07)";
        const fill = sec.hasAlert ? "rgba(255,79,94,0.18)" : baseFill;
        shapes.push({
          type: "rect",
          xref: "x",
          yref: "paper",
          x0: sec.start,
          x1: sec.end,
          y0: 0,
          y1: 1,
          fillcolor: fill,
          line: { width: 0 },
        });
      }
      if (result.ignoredRange && result.ignoredRange.end > result.ignoredRange.start) {
        shapes.push({
          type: "rect",
          xref: "x",
          yref: "paper",
          x0: result.ignoredRange.start,
          x1: result.ignoredRange.end,
          y0: 0,
          y1: 1,
          fillcolor: "rgba(145, 158, 171, 0.14)",
          line: { width: 0 },
        });
      }
    } else {
      for (const seg of result.segments) {
        const alpha = clamp(seg.avgPenalty * 0.35, 0, 0.38);
        if (alpha <= 0) {
          continue;
        }
        shapes.push({
          type: "rect",
          xref: "x",
          yref: "paper",
          x0: seg.start,
          x1: seg.end,
          y0: 0,
          y1: 1,
          fillcolor: `rgba(255,79,94,${alpha.toFixed(3)})`,
          line: { width: 0 },
        });
      }
    }

    const traces = [
      {
        name: "Upper Limit",
        x: result.testTime,
        y: result.upper,
        type: "scattergl",
        mode: "lines",
        line: { color: "#ffb347", width: 1.5 },
      },
      {
        name: "Lower Limit",
        x: result.testTime,
        y: result.lower,
        type: "scattergl",
        mode: "lines",
        line: { color: "#f78fb3", width: 1.5 },
      },
      {
        name: "Test Signal",
        x: result.testTime,
        y: result.testValue,
        type: "scattergl",
        mode: "lines",
        line: { color: "#6ce4ff", width: 1.8 },
      },
      {
        name: "Excluded from score",
        x: result.excludedPoints?.x || [],
        y: result.excludedPoints?.y || [],
        type: "scattergl",
        mode: "lines",
        line: { color: "#95a3b3", width: 2.6, dash: "dot" },
      },
      {
        name: "OK markers",
        x: result.okPoints.x,
        y: result.okPoints.y,
        type: "scattergl",
        mode: "markers",
        marker: { color: "#2bd67b", size: 4, opacity: 0.6 },
      },
      {
        name: "Alert markers",
        x: result.alerts.x,
        y: result.alerts.y,
        type: "scattergl",
        mode: "markers",
        marker: { color: "#ff4f5e", size: 6, opacity: 0.82 },
      },
      {
        name: "Reference Mid",
        x: result.testTime,
        y: result.refMid,
        type: "scattergl",
        mode: "lines",
        line: { color: "#9aa8b8", width: 1, dash: "dot" },
      },
      {
        name: "Extrema",
        x: result.extremaMarks.x,
        y: result.extremaMarks.y,
        type: "scatter",
        mode: "markers",
        marker: {
          color: result.extremaMarks.color,
          size: 9,
          line: { color: "#111", width: 1 },
        },
      },
      {
        name: "Control points OK",
        x: result.cpMarkers.ok.x,
        y: result.cpMarkers.ok.y,
        type: "scatter",
        mode: "markers",
        marker: { color: "#38d9a9", size: 9, symbol: "diamond", line: { color: "#083f34", width: 1 } },
      },
      {
        name: "Control points Alert",
        x: result.cpMarkers.alert.x,
        y: result.cpMarkers.alert.y,
        type: "scatter",
        mode: "markers",
        marker: { color: "#ff6b6b", size: 10, symbol: "diamond", line: { color: "#5f0f16", width: 1 } },
      },
    ];

    const layout = {
      margin: { t: 18, r: 24, b: 45, l: 58 },
      paper_bgcolor: "rgba(0,0,0,0)",
      plot_bgcolor: "rgba(255,255,255,0.02)",
      font: { color: "#dce6f2" },
      dragmode: "pan",
      shapes,
      xaxis: {
        title: "Time (ms, normalized from 0)",
        zeroline: true,
        zerolinecolor: "rgba(255,255,255,0.24)",
        gridcolor: "rgba(255,255,255,0.12)",
        tickformat: ",.0f",
        exponentformat: "none",
      },
      yaxis: {
        title: "Value",
        zeroline: true,
        zerolinecolor: "rgba(255,255,255,0.24)",
        gridcolor: "rgba(255,255,255,0.12)",
      },
      legend: { orientation: "h", x: 0, y: 1.14 },
    };

    const config = {
      responsive: true,
      displaylogo: false,
      scrollZoom: true,
      modeBarButtonsToRemove: ["select2d", "lasso2d"],
    };

    Plotly.react(el.analysisPlot, traces, layout, config);
  }

  function renderExtremaTable(rows) {
    el.extremaTableBody.innerHTML = "";
    for (const row of rows) {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${row.Phase}</td>
        <td>${escapeHtml(row.RefType || "")}</td>
        <td>${escapeHtml(row.TestType || "")}</td>
        <td>${fmtNum(row.DeltaValue, 4)}</td>
        <td>${fmtNum(row.DeltaTime, 0)}</td>
        <td class="${row.Status === "OK" ? "status-ok" : "status-alert"}">${row.Status}</td>
      `;
      el.extremaTableBody.appendChild(tr);
    }
  }

  function renderControlPointsStatusTable(rows) {
    el.cpStatusTableBody.innerHTML = "";
    if (!rows.length) {
      const tr = document.createElement("tr");
      tr.innerHTML = '<td colspan="7">Control points не найдены.</td>';
      el.cpStatusTableBody.appendChild(tr);
      return;
    }

    rows.forEach((row, idx) => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${idx + 1}</td>
        <td>${fmtNum(row.Time, 0)}</td>
        <td>${fmtNum(row.RefValue, 5)}</td>
        <td>${fmtNum(row.TestValue, 5)}</td>
        <td>${fmtNum(row.TolUp, 5)}</td>
        <td>${fmtNum(row.TolDown, 5)}</td>
        <td class="${row.Status === "OK" ? "status-ok" : "status-alert"}">${row.Status}</td>
      `;
      el.cpStatusTableBody.appendChild(tr);
    });
  }

  function renderSegmentWeightsEditor(result) {
    const container = el.segmentWeights;
    container.classList.remove("empty");
    container.innerHTML = "";

    if (result.mode === "section") {
      if (!result.segments.length) {
        container.classList.add("empty");
        container.textContent = "Секции не обнаружены.";
        return;
      }
      result.segments.forEach((segment, idx) => {
        const row = document.createElement("div");
        row.className = "segment-row";
        const alerted = result.sectionOverlays?.find((s) => s.id === segment.id)?.hasAlert;
        row.innerHTML = `
          <div class="segment-row-header">
            <span>Section ${idx + 1}: ${fmtNum(segment.start, 0)} - ${fmtNum(segment.end, 0)}</span>
            <span>${alerted ? "Alert" : "OK"}</span>
          </div>
          <div class="slider-row">
            <label>Importance</label>
            <span>${fmtNum(segment.weight, 2)}</span>
            <span>avgPenalty: ${fmtNum(segment.avgPenalty, 3)}</span>
          </div>
        `;
        container.appendChild(row);
      });
      return;
    }

    if (!result.segments.length) {
      container.classList.add("empty");
      container.textContent = "Сегменты не обнаружены.";
      return;
    }

    result.segments.forEach((segment, idx) => {
      const row = document.createElement("div");
      row.className = "segment-row";
      row.innerHTML = `
        <div class="segment-row-header">
          <span>Segment ${idx + 1}: ${fmtNum(segment.start, 0)} - ${fmtNum(segment.end, 0)}</span>
          <span>avgPenalty: ${fmtNum(segment.avgPenalty, 3)}</span>
        </div>
        <div class="slider-row">
          <label>Weight</label>
          <input type="range" min="0.1" max="5" step="0.1" value="${segment.weight}" data-seg="${idx}" data-kind="weight" />
          <span>${fmtNum(segment.weight, 1)}</span>
        </div>
        <div class="slider-row">
          <label>Criticality</label>
          <input type="range" min="0.5" max="3" step="0.1" value="${segment.criticality}" data-seg="${idx}" data-kind="criticality" />
          <span>${fmtNum(segment.criticality, 1)}</span>
        </div>
      `;
      container.appendChild(row);
    });

    container.querySelectorAll("input[type='range']").forEach((input) => {
      input.addEventListener("input", () => {
        const segIndex = safeInt(input.dataset.seg);
        const kind = input.dataset.kind;
        const value = safeFloat(input.value);
        if (!Number.isFinite(segIndex) || segIndex < 0 || segIndex >= result.segments.length) {
          return;
        }

        if (kind === "weight") {
          result.segments[segIndex].weight = value;
        } else if (kind === "criticality") {
          result.segments[segIndex].criticality = value;
        }

        const span = input.parentElement.querySelector("span");
        if (span) {
          span.textContent = fmtNum(value, 1);
        }

        recomputeAnalysisScore(result);
        drawAnalysisPlot(result);
        renderScore(result.score, result.mode);
        renderMetrics(result.metrics, result.cpLoss);
        updateAnalysisSummary();
        renderGemmaStub(result);
      });
    });
  }

  function renderMetrics(metrics, cpLoss) {
    el.metricsBox.textContent = [
      `Reference mean|dV/dt|: ${fmtNum(metrics.reference.mean_abs_derivative, 6)}`,
      `Reference integral:     ${fmtNum(metrics.reference.integral, 6)}`,
      `Test mean|dV/dt|:      ${fmtNum(metrics.test.mean_abs_derivative, 6)}`,
      `Test integral:          ${fmtNum(metrics.test.integral, 6)}`,
      `Control points loss:    ${fmtNum(cpLoss.normalized, 5)}`,
    ].join("\n");
  }

  function renderScore(score, mode = "classic") {
    const val = clamp(score, 0, 100);
    el.healthScoreValue.textContent = `${fmtNum(val, 1)} / 100`;
    el.healthScoreFill.style.width = `${val}%`;

    let text = "Сигнал в хорошем состоянии.";
    if (mode === "section") {
      if (val < 60) {
        text = "Critical: слишком много важных секций с отклонениями.";
      } else if (val < 85) {
        text = "Attention: есть отклонения по важным секциям.";
      } else {
        text = "Healthy: критичных секционных отклонений не обнаружено.";
      }
    } else if (val < 40) {
      text = "Критическое отклонение: рекомендуется углубленная диагностика.";
    } else if (val < 70) {
      text = "Есть заметные отклонения, нужна проверка критичных фаз.";
    }
    el.scoreText.textContent = text;
  }

  function renderGemmaStub(result) {
    const alertCount = result.extremaRows.filter((row) => row.Status === "Alert").length;
    const cpAlertCount = result.controlPointRows.filter((row) => row.Status === "Alert").length;

    const data = {
      model_target: "gemma-4 (future)",
      generated_at: new Date().toISOString(),
      channel: {
        type: state.analysis.channelType,
        index: state.analysis.channelIndex,
      },
      score: {
        value: roundNum(result.score, 2),
        base_loss: roundNum(result.baseLoss, 5),
        cp_loss: roundNum(result.cpLoss.normalized, 5),
        total_loss: roundNum(result.totalLoss, 5),
        interpretation:
          result.mode === "section"
            ? result.score >= 85
              ? "Healthy"
              : result.score >= 60
              ? "Attention"
              : "Critical"
            : result.score >= 70
            ? "Состояние ближе к норме"
            : result.score >= 40
            ? "Умеренные отклонения"
            : "Высокий риск деградации",
      },
      section_score:
        result.mode === "section"
          ? {
              active_section_count: result.sectionScore?.activeSectionCount || 0,
              red_section_count: result.sectionScore?.redSectionCount || 0,
              active_weight: roundNum(result.sectionScore?.activeWeight || 0, 4),
              red_weight: roundNum(result.sectionScore?.redWeight || 0, 4),
              red_weight_ratio: roundNum(result.sectionScore?.redWeightRatio || 0, 6),
            }
          : null,
      extrema_alerts: alertCount,
      control_point_alerts: cpAlertCount,
      segment_hotspots: result.segments
        .filter((seg) => seg.avgPenalty > 0)
        .sort((a, b) => b.avgPenalty - a.avgPenalty)
        .slice(0, 5)
        .map((seg) => ({
          start: roundNum(seg.start, 2),
          end: roundNum(seg.end, 2),
          avg_penalty: roundNum(seg.avgPenalty, 4),
          weight: roundNum(seg.weight, 2),
          criticality: roundNum(seg.criticality, 2),
        })),
      control_point_hotspots: result.controlPointRows
        .filter((row) => row.penalty > 0)
        .sort((a, b) => b.penalty - a.penalty)
        .slice(0, 8)
        .map((row) => ({
          time: roundNum(row.Time, 2),
          ref: roundNum(row.RefValue, 5),
          test: roundNum(row.TestValue, 5),
          penalty: roundNum(row.penalty, 4),
          kind: row.Kind,
        })),
      metrics: {
        test_mean_abs_derivative: roundNum(result.metrics.test.mean_abs_derivative, 6),
        test_integral: roundNum(result.metrics.test.integral, 6),
        reference_mean_abs_derivative: roundNum(result.metrics.reference.mean_abs_derivative, 6),
        reference_integral: roundNum(result.metrics.reference.integral, 6),
      },
      explanation_stub:
        "Gemma может использовать control_point_hotspots, segment_hotspots и alerts для пояснения отклонений простым языком персоналу без высокой квалификации.",
      recommendations_stub: [
        "Проверить участки с максимальными штрафами по control points и сегментам.",
        "Сопоставить обнаруженные отклонения с температурой и давлением в соседних каналах.",
      ],
    };

    el.gemmaStub.textContent = JSON.stringify(data, null, 2);
  }

  function updatePresetSummary() {
    const signal = state.preset.signal;
    if (!signal) {
      el.presetSummary.textContent = "Ожидание данных...";
      return;
    }

    const cps = sortedControlPoints();
    const alertPotential = countPotentialCrossings(signal.value, state.preset.lower, state.preset.upper);
    const cpAlerts = cps.filter((cp) => cp.status === "Alert").length;

    el.presetSummary.textContent = [
      `Source: ${state.preset.refFileName || "(manual/preset)"}`,
      `Channel: ${state.preset.channelType}:${state.preset.channelIndex ?? "-"}`,
      `Points: ${signal.time.length}`,
      `Control points: ${cps.length} (limit ${getControlPointLimit()})`,
      `Edit mode: ${state.preset.editMode === "edit" ? "Редактировать точки" : "Обзор"}`,
      `Control status alerts: ${cpAlerts}`,
      `Envelope width avg: ${fmtNum(avgArray(diffArray(state.preset.upper, state.preset.lower)), 4)}`,
      `Reference border crossings: ${alertPotential}`,
    ].join("\n");
  }

  function updateAnalysisSummary() {
    const lines = [];

    lines.push(`Test file: ${state.analysis.testFileName || "-"}`);
    lines.push(`Preset: ${state.analysis.presetObj?.preset_name || "-"}`);
    lines.push(
      `Preset type: ${state.analysis.presetObj?.preset_type === "section_v1" ? "section_v1" : "classic"}`
    );
    lines.push(`Channel: ${state.analysis.channelType}:${state.analysis.channelIndex ?? "-"}`);

    if (state.analysis.result) {
      if (state.analysis.result.mode === "section") {
        const secCount = state.analysis.result.sectionOverlays?.length || 0;
        const secAlert = (state.analysis.result.sectionOverlays || []).filter((sec) => sec.hasAlert).length;
        lines.push(`Sections checked: ${secCount}, Alert sections: ${secAlert}`);
        if (state.analysis.result.sectionScore) {
          const s = state.analysis.result.sectionScore;
          lines.push(`Red sections: ${s.redSectionCount} / ${s.activeSectionCount}`);
          lines.push(
            `Red importance: ${fmtNum(s.redWeight, 3)} / ${fmtNum(s.activeWeight, 3)} (${fmtNum(
              s.redWeightRatio * 100,
              1
            )}%)`
          );
        }
        if (state.analysis.result.analysisRange) {
          lines.push(
            `Analyzed time: ${fmtNum(state.analysis.result.analysisRange.start, 0)}..${fmtNum(
              state.analysis.result.analysisRange.end,
              0
            )} ms`
          );
        }
        if (state.analysis.result.ignoredRange) {
          lines.push(
            `Ignored time: ${fmtNum(state.analysis.result.ignoredRange.start, 0)}..${fmtNum(
              state.analysis.result.ignoredRange.end,
              0
            )} ms`
          );
        }
        if (state.analysis.result.durationMismatch) {
          lines.push(`Warning: ${state.analysis.result.durationMismatch}`);
        }
      } else {
        const rows = state.analysis.result.extremaRows;
        const okCount = rows.filter((row) => row.Status === "OK").length;
        const alertCount = rows.length - okCount;
        const cpAlert = state.analysis.result.controlPointRows.filter((row) => row.Status === "Alert").length;
        lines.push(`Phases checked: ${rows.length}, OK: ${okCount}, Alert: ${alertCount}`);
        lines.push(`Control point alerts: ${cpAlert}`);
      }
      lines.push(`Alert points: ${state.analysis.result.alerts.x.length}`);
      lines.push(`Score: ${fmtNum(state.analysis.result.score, 2)}/100`);
    }

    el.analysisSummary.textContent = lines.join("\n");
  }

  function resetScore() {
    el.healthScoreValue.textContent = "--";
    el.healthScoreFill.style.width = "0%";
    el.scoreText.textContent = "Загрузите данные и выполните анализ.";
    el.metricsBox.textContent = "Ожидание результатов...";
    el.gemmaStub.textContent = "{}";
    el.cpStatusTableBody.innerHTML = "";
  }

  function generateAutoControlPoints(time, value, opts) {
    const limit = clamp(safeInt(opts.limit) || 120, CP_LIMIT_MIN, CP_LIMIT_MAX);
    const margin = Math.max(1e-6, safeFloat(opts.margin) || inferDefaultToleranceFromSignal(value));
    const n = time.length;

    if (n < 3) {
      return [];
    }

    const smooth = smoothSignal(value, 9);
    const range = Math.max(arrMax(smooth) - arrMin(smooth), 1e-9);
    const minSampleGap = Math.max(2, Math.floor((n / limit) * 0.7));
    const signThr = range * 0.001;

    const candidates = [];
    const idxUsed = new Set();

    addCandidate(0, "manual", 100);
    addCandidate(n - 1, "manual", 100);

    const extrema = detectExtrema(time, value, smooth, { distance: Math.max(4, Math.floor(minSampleGap * 0.8)), prominence: null });
    for (const ex of extrema) {
      addCandidate(ex.index, "extremum", 80);
    }

    for (let i = 2; i < n - 2; i += 1) {
      const dPrev = smooth[i] - smooth[i - 1];
      const dNext = smooth[i + 1] - smooth[i];
      const s1 = signWithThreshold(dPrev, signThr);
      const s2 = signWithThreshold(dNext, signThr);
      if (s1 !== 0 && s2 !== 0 && s1 !== s2) {
        const score = 55 + Math.abs(dNext - dPrev) / Math.max(range, 1e-9) * 18;
        addCandidate(i, "turn", score);
      }
    }

    const curvatureRaw = [];
    for (let i = 1; i < n - 1; i += 1) {
      const c = Math.abs(smooth[i + 1] - 2 * smooth[i] + smooth[i - 1]);
      curvatureRaw.push({ idx: i, c });
    }
    curvatureRaw.sort((a, b) => b.c - a.c);
    const curvatureTake = Math.min(curvatureRaw.length, Math.max(60, limit * 3));
    const cNorm = curvatureRaw[0]?.c || 1;
    for (let i = 0; i < curvatureTake; i += 1) {
      const item = curvatureRaw[i];
      const score = 30 + (item.c / Math.max(cNorm, 1e-9)) * 25;
      addCandidate(item.idx, "curvature", score);
    }

    const selected = [];
    const sortedCandidates = candidates.slice().sort((a, b) => b.score - a.score);
    for (const cand of sortedCandidates) {
      if (selected.length >= limit) {
        break;
      }
      if (selected.some((idx) => Math.abs(idx - cand.idx) < minSampleGap)) {
        continue;
      }
      selected.push(cand.idx);
    }

    selected.sort((a, b) => a - b);

    const targetMin = Math.min(limit, Math.max(24, Math.floor(limit * 0.35)));
    if (selected.length < targetMin) {
      const needed = targetMin - selected.length;
      for (let i = 0; i < needed; i += 1) {
        const idx = Math.round((i / Math.max(1, needed - 1)) * (n - 1));
        if (!selected.some((v) => Math.abs(v - idx) < minSampleGap)) {
          selected.push(idx);
        }
      }
    }

    selected.sort((a, b) => a - b);

    const points = selected.map((idx) => {
      const kind = candidateKindByIndex(candidates, idx) || "curvature";
      return {
        id: nextControlPointId(),
        time: time[idx],
        ref_value: value[idx],
        tol_up: margin,
        tol_down: margin,
        kind,
        importance: kind === "extremum" ? 1.3 : 1,
        status: "OK",
      };
    });

    return reduceControlPointDensity(points, limit);

    function addCandidate(idx, kind, score) {
      if (idx < 0 || idx >= n) {
        return;
      }
      if (idxUsed.has(idx)) {
        candidates.push({ idx, kind, score: score - 0.1 });
      } else {
        idxUsed.add(idx);
        candidates.push({ idx, kind, score });
      }
    }
  }

  function candidateKindByIndex(candidates, idx) {
    const kinds = candidates.filter((c) => c.idx === idx).sort((a, b) => b.score - a.score);
    return kinds[0]?.kind;
  }

  function signWithThreshold(v, thr) {
    if (v > thr) {
      return 1;
    }
    if (v < -thr) {
      return -1;
    }
    return 0;
  }

  function buildControlPointsFromLegacyBounds(time, upper, lower, limit) {
    const ref = midpointArray(upper, lower);
    const cp = generateAutoControlPoints(time, ref, {
      limit,
      margin: Math.max(avgArray(diffArray(upper, lower)) * 0.5, 0.05),
    });

    for (const point of cp) {
      const up = interpolateAt(time, upper, point.time);
      const low = interpolateAt(time, lower, point.time);
      point.ref_value = interpolateAt(time, ref, point.time);
      point.tol_up = Math.max(0, up - point.ref_value);
      point.tol_down = Math.max(0, point.ref_value - low);
      point.kind = point.kind || "curvature";
    }

    return cp;
  }

  function sanitizeControlPoints(points, domainTime, defaultTol) {
    const t0 = domainTime[0];
    const t1 = domainTime[domainTime.length - 1];
    const out = [];

    for (const item of points) {
      if (!item || typeof item !== "object") {
        continue;
      }
      const time = clamp(safeFloat(item.time), t0, t1);
      const ref = safeFloat(item.ref_value);
      if (!Number.isFinite(time) || !Number.isFinite(ref)) {
        continue;
      }

      const cp = {
        id: typeof item.id === "string" && item.id ? item.id : nextControlPointId(),
        time,
        ref_value: ref,
        tol_up: Math.max(0, Number.isFinite(safeFloat(item.tol_up)) ? safeFloat(item.tol_up) : defaultTol),
        tol_down: Math.max(0, Number.isFinite(safeFloat(item.tol_down)) ? safeFloat(item.tol_down) : defaultTol),
        kind:
          item.kind === "extremum" || item.kind === "turn" || item.kind === "curvature" || item.kind === "manual"
            ? item.kind
            : "manual",
        importance: clamp(
          Number.isFinite(safeFloat(item.importance))
            ? safeFloat(item.importance)
            : clamp((safeFloat(item.weight) || 1) * (safeFloat(item.criticality) || 1), 0.1, 10),
          0.1,
          10
        ),
        status: "OK",
      };

      normalizeControlPoint(cp, domainTime);
      out.push(cp);
    }

    if (!out.length) {
      return [];
    }

    const dedup = dedupeControlPointsByTime(out);
    return reduceControlPointDensity(dedup, getControlPointLimit());
  }

  function dedupeControlPointsByTime(points) {
    const sorted = points.slice().sort((a, b) => a.time - b.time);
    const out = [];
    const minGap = 1e-6;

    for (const cp of sorted) {
      const last = out[out.length - 1];
      if (!last || Math.abs(last.time - cp.time) > minGap) {
        out.push(cp);
      } else {
        out[out.length - 1] = cp;
      }
    }
    return out;
  }

  function reduceControlPointDensity(points, limit) {
    if (points.length <= limit) {
      return points.slice().sort((a, b) => a.time - b.time);
    }
    const sorted = points.slice().sort((a, b) => a.time - b.time);
    const out = [];
    for (let i = 0; i < limit; i += 1) {
      const idx = Math.round((i / (limit - 1)) * (sorted.length - 1));
      out.push(sorted[idx]);
    }
    return dedupeControlPointsByTime(out);
  }

  function normalizeControlPoint(cp, domainTime) {
    cp.tol_up = Math.max(0, safeFloat(cp.tol_up));
    cp.tol_down = Math.max(0, safeFloat(cp.tol_down));
    cp.importance = clamp(Number.isFinite(safeFloat(cp.importance)) ? safeFloat(cp.importance) : 1, 0.1, 10);
    cp.time = clamp(Math.round(cp.time), domainTime[0], domainTime[domainTime.length - 1]);
  }

  function sortedControlPoints() {
    return state.preset.controlPoints.slice().sort((a, b) => a.time - b.time);
  }

  function cpIdFromRenderedPoint(pointNumber) {
    if (!Number.isFinite(pointNumber)) {
      return null;
    }
    return state.preset.renderOrder[pointNumber] || null;
  }

  function nextControlPointId() {
    const id = `cp_${Date.now()}_${state.preset.idCounter}`;
    state.preset.idCounter += 1;
    return id;
  }

  function getControlPointLimit() {
    const v = safeInt(el.presetAnchorCount.value);
    return clamp(Number.isFinite(v) ? v : CP_LIMIT_DEFAULT, CP_LIMIT_MIN, CP_LIMIT_MAX);
  }

  function inferDefaultToleranceFromSignal(values) {
    const valueRange = arrMax(values) - arrMin(values);
    return Math.max(valueRange * 0.07, 0.05);
  }

  function selectedControlSpanPoints(cps) {
    const selected = cps.find((cp) => cp.id === state.preset.selectedCpId);
    if (!selected) {
      return { x: [], y: [] };
    }
    return {
      x: [selected.time, selected.time],
      y: [selected.ref_value - selected.tol_down, selected.ref_value + selected.tol_up],
    };
  }

  function getControlPointMergeEpsilon(domainTime) {
    if (!domainTime || domainTime.length < 2) {
      return PRESET_MERGE_TIME_EPS;
    }
    const dt = Math.abs((domainTime[domainTime.length - 1] - domainTime[0]) / Math.max(1, domainTime.length - 1));
    return Math.max(PRESET_MERGE_TIME_EPS, dt * 0.45);
  }

  function findControlPointNearTime(time, domainTime) {
    const eps = getControlPointMergeEpsilon(domainTime);
    let best = null;
    let bestDist = Number.POSITIVE_INFINITY;
    for (const cp of state.preset.controlPoints) {
      const dist = Math.abs(cp.time - time);
      if (dist <= eps && dist < bestDist) {
        bestDist = dist;
        best = cp;
      }
    }
    return best;
  }

  function evaluateControlPoints(controlPoints, testTime, testValue) {
    const rows = [];
    const cps = controlPoints.slice().sort((a, b) => a.time - b.time);

    for (const cp of cps) {
      const testVal = interpolateAt(testTime, testValue, cp.time);
      const upper = cp.ref_value + cp.tol_up;
      const lower = cp.ref_value - cp.tol_down;
      const penalty = asymControlPointPenalty(testVal, cp.ref_value, cp.tol_up, cp.tol_down);
      rows.push({
        Id: cp.id,
        Time: cp.time,
        RefValue: cp.ref_value,
        TestValue: testVal,
        TolUp: cp.tol_up,
        TolDown: cp.tol_down,
        Kind: cp.kind,
        Importance: cp.importance,
        penalty,
        Status: penalty > 0 ? "Alert" : "OK",
      });
    }

    return rows;
  }

  function asymControlPointPenalty(testVal, refVal, tolUp, tolDown) {
    if (testVal > refVal + tolUp) {
      return clamp((testVal - (refVal + tolUp)) / Math.max(tolUp, 1e-9), 0, 1);
    }
    if (testVal < refVal - tolDown) {
      return clamp(((refVal - tolDown) - testVal) / Math.max(tolDown, 1e-9), 0, 1);
    }
    return 0;
  }

  function computeControlPointsLoss(rows) {
    if (!rows.length) {
      return { normalized: 0, weightedLoss: 0, weightNorm: 1 };
    }

    let loss = 0;
    let norm = 0;
    for (const row of rows) {
      const w = row.Importance;
      loss += row.penalty * w;
      norm += w;
    }

    return {
      normalized: clamp(norm > 0 ? loss / norm : 0, 0, 1),
      weightedLoss: loss,
      weightNorm: norm,
    };
  }

  function buildSegmentsFromExtrema(time, extrema, presetSegmentWeights) {
    const boundaries = [time[0], ...extrema.map((x) => x.time), time[time.length - 1]];
    const uniq = uniqueSorted(boundaries);

    const segments = [];
    for (let i = 0; i < uniq.length - 1; i += 1) {
      const start = uniq[i];
      const end = uniq[i + 1];
      if (!(end > start)) {
        continue;
      }

      const presetW = presetSegmentWeights[i] || {};
      segments.push({
        start,
        end,
        weight: clamp(safeFloat(presetW.weight) || 1, 0.1, 5),
        criticality: clamp(safeFloat(presetW.criticality) || 1, 0.5, 3),
        avgPenalty: 0,
      });
    }

    if (!segments.length) {
      segments.push({
        start: time[0],
        end: time[time.length - 1],
        weight: 1,
        criticality: 1,
        avgPenalty: 0,
      });
    }

    return segments;
  }

  function computeSegmentLosses(time, pointPenalties, segments) {
    let weightedLoss = 0;
    let weightedNorm = 0;

    for (const seg of segments) {
      let sum = 0;
      let count = 0;
      for (let i = 0; i < time.length; i += 1) {
        const t = time[i];
        if (t < seg.start || t > seg.end) {
          continue;
        }
        sum += pointPenalties[i];
        count += 1;
      }

      seg.avgPenalty = count ? sum / count : 0;
      const w = seg.weight * seg.criticality;
      weightedLoss += seg.avgPenalty * w;
      weightedNorm += w;
    }

    const normalized = weightedNorm > 0 ? weightedLoss / weightedNorm : 0;
    return {
      weightedLoss,
      weightedNorm,
      normalized: clamp(normalized, 0, 1),
    };
  }

  function computeExtremaPenalty(rows, valueThr, timeThr) {
    if (!rows.length) {
      return { normalized: 0 };
    }

    let total = 0;
    for (const row of rows) {
      const dv = Number.isFinite(row.DeltaValue) ? row.DeltaValue / valueThr : 1;
      const dt = Number.isFinite(row.DeltaTime) ? row.DeltaTime / timeThr : 1;
      const typeMismatch = row.RefType && row.TestType && row.RefType !== row.TestType ? 1 : 0;

      const local = (clamp(dv, 0, 2) + clamp(dt, 0, 2) + typeMismatch) / 3;
      total += local;
    }

    return {
      normalized: clamp(total / rows.length, 0, 1),
    };
  }

  function pointPenalty(value, lower, upper) {
    const width = Math.max(upper - lower, 1e-9);
    if (value > upper) {
      return clamp((value - upper) / width, 0, 1);
    }
    if (value < lower) {
      return clamp((lower - value) / width, 0, 1);
    }
    return 0;
  }

  function sampleOkPoints(time, value, penalties, targetCount) {
    const okIdx = [];
    for (let i = 0; i < penalties.length; i += 1) {
      if (penalties[i] === 0) {
        okIdx.push(i);
      }
    }

    if (!okIdx.length) {
      return { x: [], y: [] };
    }

    const step = Math.max(1, Math.floor(okIdx.length / targetCount));
    const x = [];
    const y = [];

    for (let i = 0; i < okIdx.length; i += step) {
      const idx = okIdx[i];
      x.push(time[idx]);
      y.push(value[idx]);
    }

    return { x, y };
  }

  function compareExtrema(refExtrema, testExtrema, valueThr, timeThr) {
    const len = Math.max(refExtrema.length, testExtrema.length);
    const rows = [];

    for (let i = 0; i < len; i += 1) {
      const ref = refExtrema[i] || null;
      const test = testExtrema[i] || null;

      const deltaValue = ref && test ? Math.abs(ref.value - test.value) : NaN;
      const deltaTime = ref && test ? Math.abs(ref.time - test.time) : NaN;
      const sameType = ref && test ? ref.type === test.type : false;

      let status = "Alert";
      if (
        Number.isFinite(deltaValue) &&
        Number.isFinite(deltaTime) &&
        deltaValue <= valueThr &&
        deltaTime <= timeThr &&
        sameType
      ) {
        status = "OK";
      }

      rows.push({
        Phase: i + 1,
        RefType: ref ? ref.type : "",
        TestType: test ? test.type : "",
        DeltaValue: deltaValue,
        DeltaTime: deltaTime,
        Status: status,
        Time_test: test ? test.time : NaN,
        Value_test: test ? test.value : NaN,
      });
    }

    return rows;
  }

  function buildExtremaStatusMap(rows) {
    const map = new Map();
    for (const row of rows) {
      if (!Number.isFinite(row.Time_test) || !Number.isFinite(row.Value_test)) {
        continue;
      }
      const key = `${roundNum(row.Time_test, 3)}|${roundNum(row.Value_test, 3)}`;
      map.set(key, row.Status);
    }
    return map;
  }

  function detectExtrema(time, valueRaw, valueSmooth, opts) {
    const n = time.length;
    if (n < 3) {
      return [];
    }

    const distance = Math.max(1, safeInt(opts.distance) || 10);
    const signalRange = arrMax(valueSmooth) - arrMin(valueSmooth);
    const prominenceThr = Number.isFinite(opts.prominence)
      ? opts.prominence
      : Math.max(signalRange * 0.05, 1e-9);

    const candidatesMax = [];
    const candidatesMin = [];

    for (let i = 1; i < n - 1; i += 1) {
      const prev = valueSmooth[i - 1];
      const curr = valueSmooth[i];
      const next = valueSmooth[i + 1];

      if (curr > prev && curr >= next) {
        const prom = localProminence(valueSmooth, i, distance, "max");
        if (prom >= prominenceThr) {
          candidatesMax.push({ idx: i, prominence: prom });
        }
      }
      if (curr < prev && curr <= next) {
        const prom = localProminence(valueSmooth, i, distance, "min");
        if (prom >= prominenceThr) {
          candidatesMin.push({ idx: i, prominence: prom });
        }
      }
    }

    const maxPicked = pickWithDistance(candidatesMax, distance);
    const minPicked = pickWithDistance(candidatesMin, distance);

    const extrema = [];
    for (const idx of maxPicked) {
      extrema.push({
        type: "max",
        index: idx,
        time: time[idx],
        value: valueRaw[idx],
      });
    }
    for (const idx of minPicked) {
      extrema.push({
        type: "min",
        index: idx,
        time: time[idx],
        value: valueRaw[idx],
      });
    }

    extrema.sort((a, b) => a.time - b.time);
    return extrema;
  }

  function smoothSignal(values, windowSize) {
    const n = values.length;
    if (n < 5) {
      return values.slice();
    }

    let w = Math.max(3, safeInt(windowSize) || 11);
    if (w % 2 === 0) {
      w += 1;
    }
    if (w > n) {
      w = n % 2 === 0 ? n - 1 : n;
    }
    if (w < 3) {
      return values.slice();
    }

    const half = Math.floor(w / 2);
    const out = new Array(n);

    for (let i = 0; i < n; i += 1) {
      let sum = 0;
      let count = 0;
      for (let k = -half; k <= half; k += 1) {
        const idx = clamp(i + k, 0, n - 1);
        sum += values[idx];
        count += 1;
      }
      out[i] = count ? sum / count : values[i];
    }

    return out;
  }

  function localProminence(arr, idx, distance, mode) {
    const radius = Math.max(2, distance);
    const start = Math.max(0, idx - radius);
    const end = Math.min(arr.length - 1, idx + radius);

    let localMin = Number.POSITIVE_INFINITY;
    let localMax = Number.NEGATIVE_INFINITY;

    for (let i = start; i <= end; i += 1) {
      localMin = Math.min(localMin, arr[i]);
      localMax = Math.max(localMax, arr[i]);
    }

    if (mode === "max") {
      return arr[idx] - localMin;
    }
    return localMax - arr[idx];
  }

  function pickWithDistance(candidates, distance) {
    const sorted = candidates.slice().sort((a, b) => b.prominence - a.prominence);
    const picked = [];

    for (const c of sorted) {
      let near = false;
      for (const p of picked) {
        if (Math.abs(c.idx - p) < distance) {
          near = true;
          break;
        }
      }
      if (!near) {
        picked.push(c.idx);
      }
    }

    picked.sort((a, b) => a - b);
    return picked;
  }

  async function parseRslzFile(file) {
    const xmlText = await readGzipXmlText(file);
    const doc = parseXml(xmlText);

    const dataChannels = collectResultChannels(doc, "DataResult");
    const pwmChannels = collectResultChannels(doc, "PWMResult");

    if (!dataChannels.indexes.length && !pwmChannels.indexes.length) {
      throw new Error("Не найдены DataResult/PWMResult каналы.");
    }

    return {
      channels: {
        data: dataChannels,
        pwm: pwmChannels,
      },
    };
  }

  async function readGzipXmlText(file) {
    const bytes = new Uint8Array(await file.arrayBuffer());

    if (window.pako && typeof window.pako.ungzip === "function") {
      try {
        return window.pako.ungzip(bytes, { to: "string" });
      } catch (error) {
        // fallback to browser-native
      }
    }

    if ("DecompressionStream" in window) {
      try {
        const stream = new Blob([bytes]).stream().pipeThrough(new DecompressionStream("gzip"));
        const ab = await new Response(stream).arrayBuffer();
        return new TextDecoder("utf-8").decode(ab);
      } catch (error) {
        throw new Error(`Не удалось декомпрессировать RSLZ: ${error.message}`);
      }
    }

    throw new Error("Нет декомпрессора gzip: подключите vendor/pako.min.js.");
  }

  function parseXml(xmlText) {
    const doc = new DOMParser().parseFromString(xmlText, "application/xml");
    const err = doc.querySelector("parsererror");
    if (err) {
      throw new Error("Некорректный XML внутри RSLZ.");
    }
    return doc;
  }

  function collectResultChannels(doc, tagName) {
    const all = Array.from(doc.getElementsByTagName("*"));
    const nodes = all.filter((node) => nodeName(node) === tagName);

    const seriesByIndex = {};
    const indexes = [];

    nodes.forEach((node, orderIndex) => {
      let idx = extractIndex(node);
      if (!Number.isFinite(idx)) {
        idx = orderIndex + 1;
      }

      const datapoints = collectDataPoints(node);
      if (!datapoints.length) {
        return;
      }

      datapoints.sort((a, b) => a.time - b.time);
      const dedup = dedupeByTime(datapoints);

      seriesByIndex[idx] = {
        time: dedup.map((p) => p.time),
        value: dedup.map((p) => p.value),
      };
      indexes.push(idx);
    });

    indexes.sort((a, b) => a - b);
    return {
      indexes,
      seriesByIndex,
    };
  }

  function collectDataPoints(node) {
    const out = [];
    const all = Array.from(node.getElementsByTagName("*"));
    const points = all.filter((n) => nodeName(n) === "DataPoint");

    points.forEach((point) => {
      const tRaw = point.getAttribute("Time") ?? childText(point, "Time");
      const vRaw = point.getAttribute("Value") ?? childText(point, "Value");
      const time = parseLocaleNumber(tRaw);
      const value = parseLocaleNumber(vRaw);

      if (Number.isFinite(time) && Number.isFinite(value)) {
        out.push({ time, value });
      }
    });

    return out;
  }

  function dedupeByTime(points) {
    if (!points.length) {
      return points;
    }

    const out = [];
    for (const p of points) {
      const last = out[out.length - 1];
      if (last && Math.abs(last.time - p.time) < 1e-9) {
        out[out.length - 1] = p;
      } else {
        out.push(p);
      }
    }
    return out;
  }

  function extractIndex(node) {
    const attrs = ["Index", "index", "No", "no", "Channel", "channel"];
    for (const key of attrs) {
      const v = parseLocaleNumber(node.getAttribute(key));
      if (Number.isFinite(v)) {
        return Math.trunc(v);
      }
    }

    const byChild = parseLocaleNumber(childText(node, "Index") ?? childText(node, "No") ?? childText(node, "Channel"));
    if (Number.isFinite(byChild)) {
      return Math.trunc(byChild);
    }
    return NaN;
  }

  function parseAnyPresetObject(data) {
    if (data?.preset_type === "section_v1" || (data?.reference && Array.isArray(data?.sections))) {
      return validateSectionPresetObject(data);
    }
    return validatePresetObject(data);
  }

  function validateSectionPresetObject(data) {
    if (!data || typeof data !== "object") {
      throw new Error("Section preset должен быть JSON объектом.");
    }
    if (data.preset_type !== "section_v1") {
      throw new Error("Неверный формат section preset: нужен preset_type=section_v1.");
    }
    const name = typeof data.preset_name === "string" ? data.preset_name.trim() : "";
    if (!name) {
      throw new Error("preset_name обязателен.");
    }
    const reference = data.reference;
    if (!reference || typeof reference !== "object") {
      throw new Error("Section preset должен содержать reference.time/reference.value.");
    }
    const refTime = normalizeNumericArray(reference.time, "reference.time");
    const refValue = normalizeNumericArray(reference.value, "reference.value");
    if (refTime.length !== refValue.length || refTime.length < 3) {
      throw new Error("reference.time/reference.value должны иметь одинаковую длину >= 3.");
    }
    for (let i = 1; i < refTime.length; i += 1) {
      if (refTime[i] < refTime[i - 1]) {
        throw new Error(`reference.time должен быть неубывающим (ошибка на позиции ${i}).`);
      }
    }

    const sectionsRaw = Array.isArray(data.sections) ? data.sections : [];
    if (!sectionsRaw.length) {
      throw new Error("Section preset должен содержать sections[].");
    }
    const sections = sanitizeSectionsArray(sectionsRaw, normalizeTimeArray(refTime), inferDefaultToleranceFromSignal(refValue));
    if (!sections.length) {
      throw new Error("sections[] содержит некорректные значения.");
    }
    for (const sec of sections) {
      if (!(sec.end_time > sec.start_time)) {
        throw new Error("В sections[] найден интервал с end_time <= start_time.");
      }
    }

    return {
      preset_type: "section_v1",
      preset_name: name,
      channel_type: data.channel_type === "pwm" ? "pwm" : data.channel_type === "data" ? "data" : undefined,
      channel_index:
        Number.isFinite(parseLocaleNumber(data.channel_index)) && parseLocaleNumber(data.channel_index) >= 0
          ? Math.trunc(parseLocaleNumber(data.channel_index))
          : undefined,
      reference: {
        time: normalizeTimeArray(refTime),
        value: refValue,
      },
      sections,
      meta: data.meta || {},
    };
  }

  function validatePresetObject(data) {
    if (!data || typeof data !== "object") {
      throw new Error("Preset должен быть JSON объектом.");
    }

    const name = typeof data.preset_name === "string" ? data.preset_name.trim() : "";
    const time = normalizeNumericArray(data.time, "time");
    const upper = normalizeNumericArray(data.upper_limit, "upper_limit");
    const lower = normalizeNumericArray(data.lower_limit, "lower_limit");

    if (!name) {
      throw new Error("preset_name обязателен.");
    }

    if (time.length !== upper.length || time.length !== lower.length) {
      throw new Error("time/upper_limit/lower_limit должны иметь одинаковую длину.");
    }

    if (time.length < 3) {
      throw new Error("В пресете должно быть минимум 3 точки.");
    }

    for (let i = 1; i < time.length; i += 1) {
      if (time[i] < time[i - 1]) {
        throw new Error(`time должен быть неубывающим (ошибка на позиции ${i}).`);
      }
    }

    for (let i = 0; i < time.length; i += 1) {
      if (upper[i] < lower[i]) {
        throw new Error(`upper_limit[${i}] < lower_limit[${i}]`);
      }
    }

    const controlPoints = Array.isArray(data.control_points) ? data.control_points : [];

    return {
      preset_name: name,
      time,
      upper_limit: upper,
      lower_limit: lower,
      control_points: controlPoints,
      channel_type: data.channel_type === "pwm" ? "pwm" : data.channel_type === "data" ? "data" : undefined,
      channel_index:
        Number.isFinite(parseLocaleNumber(data.channel_index)) && parseLocaleNumber(data.channel_index) >= 0
          ? Math.trunc(parseLocaleNumber(data.channel_index))
          : undefined,
      segment_weights: Array.isArray(data.segment_weights) ? data.segment_weights : [],
      meta: data.meta || {},
    };
  }

  function normalizeNumericArray(arr, fieldName) {
    if (!Array.isArray(arr)) {
      throw new Error(`${fieldName} должен быть массивом.`);
    }
    const out = arr.map((v) => parseLocaleNumber(v));
    if (out.some((v) => !Number.isFinite(v))) {
      throw new Error(`${fieldName} содержит некорректные числа.`);
    }
    return out;
  }

  function normalizeTimeArray(time) {
    if (!time.length) {
      return [];
    }
    const t0 = time[0];
    const out = new Array(time.length);
    let prev = 0;
    for (let i = 0; i < time.length; i += 1) {
      const normalized = Math.round(time[i] - t0);
      let clamped;
      if (i === 0) {
        clamped = 0;
      } else {
        const hadProgress = time[i] > time[i - 1];
        clamped = Math.max(prev, normalized);
        if (hadProgress && clamped <= prev) {
          clamped = prev + 1;
        }
      }
      out[i] = clamped;
      prev = clamped;
    }
    return out;
  }

  function enforceBoundsOrder(lower, upper) {
    for (let i = 0; i < lower.length; i += 1) {
      if (upper[i] < lower[i]) {
        const m = 0.5 * (upper[i] + lower[i]);
        lower[i] = m - 1e-6;
        upper[i] = m + 1e-6;
      }
    }
  }

  function interpolateLinear(xSrc, ySrc, xTgt) {
    if (!xSrc.length || !ySrc.length || xSrc.length !== ySrc.length) {
      return xTgt.map(() => NaN);
    }

    const out = new Array(xTgt.length);
    let j = 0;

    for (let i = 0; i < xTgt.length; i += 1) {
      const x = xTgt[i];
      if (x <= xSrc[0]) {
        out[i] = ySrc[0];
        continue;
      }
      if (x >= xSrc[xSrc.length - 1]) {
        out[i] = ySrc[ySrc.length - 1];
        continue;
      }

      while (j < xSrc.length - 2 && xSrc[j + 1] < x) {
        j += 1;
      }

      const x0 = xSrc[j];
      const x1 = xSrc[j + 1];
      const y0 = ySrc[j];
      const y1 = ySrc[j + 1];

      if (Math.abs(x1 - x0) < 1e-12) {
        out[i] = y0;
      } else {
        const k = (x - x0) / (x1 - x0);
        out[i] = y0 + (y1 - y0) * k;
      }
    }

    return out;
  }

  function interpolateAt(xSrc, ySrc, x) {
    return interpolateLinear(xSrc, ySrc, [x])[0];
  }

  function midpointArray(a, b) {
    const out = new Array(Math.min(a.length, b.length));
    for (let i = 0; i < out.length; i += 1) {
      out[i] = 0.5 * (a[i] + b[i]);
    }
    return out;
  }

  function diffArray(a, b) {
    const out = new Array(Math.min(a.length, b.length));
    for (let i = 0; i < out.length; i += 1) {
      out[i] = a[i] - b[i];
    }
    return out;
  }

  function countPotentialCrossings(value, lower, upper) {
    let count = 0;
    const n = Math.min(value.length, lower.length, upper.length);
    for (let i = 0; i < n; i += 1) {
      if (value[i] < lower[i] || value[i] > upper[i]) {
        count += 1;
      }
    }
    return count;
  }

  function calcSignalMetrics(time, value) {
    if (time.length < 2) {
      return {
        mean_abs_derivative: 0,
        integral: 0,
      };
    }

    let absDerivSum = 0;
    let derivCount = 0;
    for (let i = 0; i < time.length - 1; i += 1) {
      const dt = time[i + 1] - time[i];
      if (Math.abs(dt) < 1e-12) {
        continue;
      }
      const dv = value[i + 1] - value[i];
      absDerivSum += Math.abs(dv / dt);
      derivCount += 1;
    }

    let integral = 0;
    for (let i = 0; i < time.length - 1; i += 1) {
      const dt = time[i + 1] - time[i];
      integral += 0.5 * (value[i] + value[i + 1]) * dt;
    }

    return {
      mean_abs_derivative: derivCount ? absDerivSum / derivCount : 0,
      integral,
    };
  }

  function populateSelect(select, values) {
    select.innerHTML = "";
    for (const v of values) {
      const option = document.createElement("option");
      option.value = String(v);
      option.textContent = String(v);
      select.appendChild(option);
    }
  }

  function parseLocaleNumber(v) {
    if (v == null) {
      return NaN;
    }
    if (typeof v === "number") {
      return v;
    }
    const s = String(v).trim().replace(/,/g, ".");
    if (!s) {
      return NaN;
    }
    const num = Number(s);
    return Number.isFinite(num) ? num : NaN;
  }

  function childText(node, localName) {
    const children = Array.from(node.children || []);
    const found = children.find((c) => nodeName(c) === localName);
    return found ? found.textContent : null;
  }

  function nodeName(node) {
    return (node.localName || node.nodeName || "").replace(/^.*:/, "");
  }

  function setViewActive(node, active) {
    node.classList.toggle("active", active);
    node.classList.toggle("hidden", !active);
  }

  function showAlert(message, level = "error", ttl = 3500) {
    el.alertBox.textContent = message;
    el.alertBox.classList.remove("hidden", "warn", "ok");
    if (level === "warn") {
      el.alertBox.classList.add("warn");
    } else if (level === "ok") {
      el.alertBox.classList.add("ok");
    }

    if (ttl > 0) {
      window.clearTimeout(showAlert._timer);
      showAlert._timer = window.setTimeout(() => {
        el.alertBox.classList.add("hidden");
      }, ttl);
    }
  }

  function downloadJson(fileName, object) {
    const blob = new Blob([JSON.stringify(object, null, 2)], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = fileName;
    a.click();
    URL.revokeObjectURL(url);
  }

  function sanitizeFileName(s) {
    return s.replace(/[^\w\-а-яА-ЯёЁ]+/g, "_").replace(/^_+|_+$/g, "") || "preset";
  }

  function clientYToDataY(plot, clientY) {
    const rect = plot.getBoundingClientRect();
    const yAxis = plot._fullLayout?.yaxis;
    if (!yAxis) {
      return NaN;
    }
    const yPx = clientY - rect.top - yAxis._offset;
    if (!Number.isFinite(yPx)) {
      return NaN;
    }
    return yAxis.p2l(yPx);
  }

  function byId(id) {
    const node = document.getElementById(id);
    if (!node) {
      throw new Error(`Element not found: #${id}`);
    }
    return node;
  }

  function escapeHtml(value) {
    return String(value)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/\"/g, "&quot;")
      .replace(/'/g, "&#039;");
  }

  function setBusy(button, busy, busyLabel = "Обработка...") {
    if (!button.dataset.defaultLabel) {
      button.dataset.defaultLabel = button.textContent;
    }
    button.disabled = busy;
    button.textContent = busy ? busyLabel : button.dataset.defaultLabel;
  }

  function safeFloat(value) {
    const n = Number(value);
    return Number.isFinite(n) ? n : NaN;
  }

  function safeInt(value) {
    const n = Number.parseInt(String(value), 10);
    return Number.isFinite(n) ? n : NaN;
  }

  function clamp(v, lo, hi) {
    return Math.min(hi, Math.max(lo, v));
  }

  function arrMin(arr) {
    let m = Number.POSITIVE_INFINITY;
    for (const x of arr) {
      if (x < m) {
        m = x;
      }
    }
    return m;
  }

  function arrMax(arr) {
    let m = Number.NEGATIVE_INFINITY;
    for (const x of arr) {
      if (x > m) {
        m = x;
      }
    }
    return m;
  }

  function avgArray(arr) {
    if (!arr.length) {
      return 0;
    }
    let s = 0;
    for (const x of arr) {
      s += x;
    }
    return s / arr.length;
  }

  function uniqueSorted(arr) {
    const sorted = arr.slice().sort((a, b) => a - b);
    const out = [];
    for (const x of sorted) {
      if (!out.length || Math.abs(out[out.length - 1] - x) > 1e-9) {
        out.push(x);
      }
    }
    return out;
  }

  function fmtNum(value, digits) {
    return Number.isFinite(value) ? Number(value).toFixed(digits) : "-";
  }

  function roundNum(value, digits) {
    if (!Number.isFinite(value)) {
      return NaN;
    }
    const p = 10 ** digits;
    return Math.round(value * p) / p;
  }

  function computeSectionWeightedScore(sectionOverlays, segments, analysisRange) {
    const start = Number.isFinite(analysisRange?.start) ? analysisRange.start : 0;
    const end = Number.isFinite(analysisRange?.end) ? analysisRange.end : Number.POSITIVE_INFINITY;
    const segById = new Map((segments || []).map((seg) => [seg.id, seg]));

    let activeSectionCount = 0;
    let redSectionCount = 0;
    let activeWeight = 0;
    let redWeight = 0;

    for (const overlay of sectionOverlays || []) {
      const seg = segById.get(overlay.id);
      if (!seg || !overlay.active) {
        continue;
      }
      const intersects = seg.end > start && seg.start < end + 1e-9;
      if (!intersects) {
        continue;
      }
      const importance = clamp(Number.isFinite(seg.weight) ? seg.weight : 0, 0, 1000);
      if (importance <= 0) {
        continue;
      }
      activeSectionCount += 1;
      activeWeight += importance;
      if (overlay.hasAlert) {
        redSectionCount += 1;
        redWeight += importance;
      }
    }

    const redWeightRatio = activeWeight > 0 ? clamp(redWeight / activeWeight, 0, 1) : 0;
    return {
      activeSectionCount,
      redSectionCount,
      activeWeight,
      redWeight,
      redWeightRatio,
      score: clamp(100 * (1 - redWeightRatio), 0, 100),
    };
  }
})();
