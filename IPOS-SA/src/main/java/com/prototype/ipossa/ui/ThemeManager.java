package com.prototype.ipossa.ui;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Theme manager.
 */
public class ThemeManager {

    private static final ThemeManager INSTANCE = new ThemeManager();

    /**
     * Get theme manager.
     *
     * @return the theme manager
     */
    public static ThemeManager get() { return INSTANCE; }

    private final SimpleBooleanProperty darkMode = new SimpleBooleanProperty(false);
    private final List<Scene> scenes = new ArrayList<>();

    private ThemeManager() {
        darkMode.addListener((o, ov, nv) -> applyAll());
    }

    /**
     * Dark mode property simple boolean property.
     *
     * @return the simple boolean property
     */
    public SimpleBooleanProperty darkModeProperty() { return darkMode; }

    /**
     * Is dark boolean.
     *
     * @return the boolean
     */
    public boolean isDark() { return darkMode.get(); }

    /**
     * Toggle.
     */
    public void toggle() { darkMode.set(!darkMode.get()); }

    /**
     * Register.
     *
     * @param scene the scene
     */
    public void register(Scene scene) {
        if (!scenes.contains(scene)) {
            scenes.add(scene);
            apply(scene);
        }
    }

    /**
     * Unregister.
     *
     * @param scene the scene
     */
    public void unregister(Scene scene) { scenes.remove(scene); }

    private void applyAll() { for (Scene s : scenes) apply(s); }

    private void apply(Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add("data:text/css;base64," +
                java.util.Base64.getEncoder().encodeToString(css().getBytes()));
        if (scene.getRoot() != null) {
            scene.getRoot().getStyleClass().removeAll("theme-light", "theme-dark");
            scene.getRoot().getStyleClass().add(darkMode.get() ? "theme-dark" : "theme-light");
        }
    }

    private String css() {

        String light = """
            .root.theme-light {
              -bg: #f4f6f9; -panel: #ffffff; -panel-2: #f3f5f8;
              -text: #0e1116; -text-dim: #5c6470; -border: #e3e7ec;
              -accent: #bbfa34; -accent-hover: #a8e624; -accent-text: #0a0c10;
              -success: #16a34a; -warn: #d97706; -danger: #dc2626;
              -sidebar-bg: #000000; -sidebar-text: #f5f5f5; -sidebar-hover: #000000;
              -sidebar-active-bar: #bbfa34; -sidebar-active-text: #ffffff;
              -sidebar-section: #6b7280;
            }
            """;
        String dark = """
            .root.theme-dark {
              -bg: #000000; -panel: #0a0a0a; -panel-2: #121212;
              -text: #f0f0f0; -text-dim: #9aa0a8; -border: #1f1f1f;
              -accent: #bbfa34; -accent-hover: #cdfd5a; -accent-text: #0a0c10;
              -success: #22c55e; -warn: #f59e0b; -danger: #ef4444;
              -sidebar-bg: #000000; -sidebar-text: #f5f5f5; -sidebar-hover: #000000;
              -sidebar-active-bar: #bbfa34; -sidebar-active-text: #ffffff;
              -sidebar-section: #6b7280;
            }
            """;
        String base = """
            .root { -fx-background-color: -bg; -fx-font-family: 'Segoe UI', 'Inter', 'Helvetica Neue', Arial, sans-serif; -fx-font-size: 13px; }
            .root * { -fx-text-fill: -text; }
            .panel { -fx-background-color: -panel; -fx-background-radius: 12; -fx-border-color: -border; -fx-border-radius: 12; -fx-border-width: 1; }
            .card { -fx-background-color: -panel; -fx-background-radius: 12; -fx-border-color: transparent; -fx-border-radius: 12; -fx-border-width: 0; -fx-padding: 18; }
            .stat-card { -fx-background-color: -panel; -fx-background-radius: 12; -fx-border-color: -border; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 20; }
            .stat-card-accent { -fx-background-color: -accent; -fx-background-radius: 12; -fx-padding: 20; }
            .stat-card-accent .stat-value, .stat-card-accent .stat-label { -fx-text-fill: -accent-text; }
            .stat-value { -fx-font-size: 28px; -fx-font-weight: bold; }
            .stat-label { -fx-text-fill: -text-dim; -fx-font-size: 12px; }
            .h1 { -fx-font-size: 22px; -fx-font-weight: bold; }
            .h2 { -fx-font-size: 16px; -fx-font-weight: bold; }
            .h3 { -fx-font-size: 14px; -fx-font-weight: bold; }
            .dim { -fx-text-fill: -text-dim; }
            .label { -fx-text-fill: -text; }

            /* ── Sidebar ───────────────────────────────────── */
            .sidebar { -fx-background-color: -sidebar-bg; -fx-padding: 24 0 18 0; }
            .sidebar .label { -fx-text-fill: -sidebar-text; }
            .sidebar-brand { -fx-text-fill: -sidebar-text; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 0 24 18 24; }
            .sidebar-section { -fx-text-fill: -sidebar-section; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 18 24 8 24; -fx-letter-spacing: 0.1em; }
            /* nav buttons span the full sidebar width with no rounding so the
               active state looks like a continuous bar (matching the reference). */
            .nav-button {
                -fx-background-color: transparent;
                -fx-text-fill: -sidebar-text;
                -fx-alignment: center-left;
                -fx-padding: 12 24 12 24;
                -fx-background-radius: 0;
                -fx-cursor: hand;
                -fx-font-size: 13px;
                -fx-graphic-text-gap: 14;
                -fx-border-color: transparent transparent transparent transparent;
                -fx-border-width: 0 0 0 4;
            }
            .nav-button:hover { -fx-background-color: -sidebar-hover; }
            .nav-button-active {
                -fx-background-color: -sidebar-hover;
                -fx-text-fill: -sidebar-active-text;
                -fx-font-weight: bold;
                -fx-border-color: transparent transparent transparent -sidebar-active-bar;
                -fx-border-width: 0 0 0 4;
            }
            .nav-button-active:hover { -fx-background-color: -sidebar-hover; }
            .sidebar-logout { -fx-text-fill: -accent; -fx-font-weight: bold; }

            /* ── Top bar ───────────────────────────────────── */
            .topbar { -fx-background-color: -sidebar-bg; -fx-border-color: -sidebar-bg; -fx-border-width: 0 0 1 0; -fx-padding: 14 22 14 22; }
            .topbar .label { -fx-text-fill: -sidebar-text; }
            .topbar .h1 { -fx-text-fill: -sidebar-text; }
            .topbar .dim { -fx-text-fill: derive(-sidebar-text, -30%); }
            .topbar .toggle-button { -fx-background-color: transparent; -fx-text-fill: -sidebar-text; -fx-border-color: derive(-sidebar-text, -60%); }
            .topbar .toggle-button:selected { -fx-background-color: -accent; -fx-text-fill: -accent-text; -fx-border-color: -accent; }

            /* ── Buttons ───────────────────────────────────── */
            .button {
                -fx-background-color: #000000; -fx-text-fill: #ffffff;
                /*-fx-border-color: -border; -fx-border-radius: 8; -fx-background-radius: 8;
                -fx-padding: 8 14 8 14; -fx-cursor: hand;*/
            }
            .button:hover { -fx-background-color: -accent; -fx-text-fill: #000000; }
            .button-primary {
                -fx-background-color: -accent; -fx-text-fill: -accent-text;
                -fx-border-color: -accent; -fx-font-weight: bold;
            }
            .button-primary:hover { -fx-background-color: -accent-hover; -fx-border-color: -accent-hover; }
            .button-primary .text { -fx-fill: -accent-text; }
            .button-danger {
                -fx-background-color: -danger; -fx-text-fill: white;
                -fx-border-color: -danger;
            }
            .button-danger .text { -fx-fill: white; }
            .button-danger:hover { -fx-background-color: derive(-danger, -10%); }
            .button-ghost { -fx-background-color: transparent; -fx-border-color: transparent; }
            .button-ghost:hover { -fx-background-color: -panel-2; }
            .toggle-button { -fx-background-color: -panel-2; -fx-text-fill: -text; -fx-border-color: -border; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 14 8 14; }
            .toggle-button:selected { -fx-background-color: -accent; -fx-text-fill: -accent-text; -fx-border-color: -accent; }

            /* ── Inputs ───────────────────────────────────── */
            .text-field, .password-field, .text-area, .combo-box, .choice-box, .date-picker, .spinner {
              -fx-background-color: -panel; -fx-border-color: -border; -fx-border-radius: 8; -fx-background-radius: 8;
              -fx-text-fill: -text; -fx-prompt-text-fill: -text-dim; -fx-padding: 8 12 8 12;
              -fx-control-inner-background: -panel;
            }
            .text-area { -fx-padding: 0; }
            .text-area .content { -fx-background-color: -panel; }
            .text-area .scroll-pane { -fx-background-color: -panel; }
            .text-area .scroll-pane .viewport { -fx-background-color: -panel; }
            .text-field:focused, .password-field:focused, .text-area:focused, .combo-box:focused {
              -fx-border-color: -accent;
            }
            .combo-box .list-cell, .combo-box-popup .list-view .list-cell { -fx-text-fill: -text; -fx-background-color: -panel; }
            .combo-box-popup .list-view { -fx-background-color: -panel; -fx-border-color: -border; }
            .combo-box-popup .list-view .list-cell:hover, .combo-box-popup .list-view .list-cell:filled:selected { -fx-background-color: -accent; -fx-text-fill: -accent-text; }
            .combo-box .arrow, .combo-box .arrow-button { -fx-background-color: -text-dim; }

            /* ── Tables ───────────────────────────────────── */
            .table-view { -fx-background-color: -panel; -fx-border-color: -border; -fx-border-radius: 10; -fx-background-radius: 10; }
            .table-view .column-header, .table-view .filler { -fx-background-color: -panel-2; -fx-border-color: -border; -fx-border-width: 0 0 1 0; }
            .table-view .column-header .label { -fx-text-fill: -text; -fx-font-weight: bold; -fx-alignment: center-left; }
            .table-view .table-cell { -fx-text-fill: -text; -fx-border-color: transparent; -fx-padding: 10 10 10 10; }
            .table-row-cell { -fx-background-color: -panel; -fx-border-color: -border; -fx-border-width: 0 0 1 0; }
            .table-row-cell:odd { -fx-background-color: derive(-panel, -2%); }
            .table-row-cell:selected { -fx-background-color: -accent; }
            .table-row-cell:selected .table-cell { -fx-text-fill: -accent-text; }
            .table-row-cell:empty { -fx-background-color: -panel; }
            .table-view .placeholder .label { -fx-text-fill: -text-dim; }

            /* ── Scrollbars ───────────────────────────────── */
            .scroll-bar { -fx-background-color: transparent; }
            .scroll-bar .thumb { -fx-background-color: -border; -fx-background-radius: 6; }
            .scroll-bar .thumb:hover { -fx-background-color: -text-dim; }
            .scroll-bar .track, .scroll-bar .track-background { -fx-background-color: transparent; }
            .scroll-bar .increment-button, .scroll-bar .decrement-button { -fx-background-color: transparent; -fx-padding: 0; }
            .scroll-pane { -fx-background-color: transparent; -fx-background: transparent; }
            .scroll-pane > .viewport { -fx-background-color: transparent; }

            /* ── Tabs ───────────────────────────────────── */
            .tab-pane .tab-header-background { -fx-background-color: transparent; }
            .tab-pane .tab { -fx-background-color: -panel-2; -fx-background-radius: 8 8 0 0; -fx-padding: 8 16 8 16; }
            .tab-pane .tab:selected { -fx-background-color: -accent; }
            .tab-pane .tab:selected .tab-label { -fx-text-fill: -accent-text; }
            .tab-pane .tab .tab-label { -fx-text-fill: -text; }

            /* ── Badges ───────────────────────────────────── */
            .badge { -fx-background-radius: 12; -fx-padding: 3 10 3 10; -fx-font-size: 11px; -fx-font-weight: bold; }
            .badge-normal { -fx-background-color: derive(-success, 70%); -fx-text-fill: -success; }
            .badge-suspended { -fx-background-color: derive(-warn, 70%); -fx-text-fill: -warn; }
            .badge-default { -fx-background-color: derive(-danger, 70%); -fx-text-fill: -danger; }
            .theme-dark .badge-normal { -fx-background-color: #0e2417; -fx-text-fill: #4ade80; }
            .theme-dark .badge-suspended { -fx-background-color: #2b1d04; -fx-text-fill: #fbbf24; }
            .theme-dark .badge-default { -fx-background-color: #2a0a0a; -fx-text-fill: #f87171; }

            /* ── Login ───────────────────────────────────── */
            .login-root { -fx-background-color: -bg; }
            .login-card { -fx-background-color: -panel; -fx-background-radius: 16; -fx-border-color: -border; -fx-border-radius: 16; -fx-border-width: 1; -fx-padding: 36; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 28, 0.2, 0, 6); }
            .login-title { -fx-font-size: 26px; -fx-font-weight: bold; }
            .login-subtitle { -fx-text-fill: -text-dim; -fx-font-size: 13px; }

            /* ── Banner ───────────────────────────────────── */
            .warning-banner { -fx-background-color: -sidebar-bg; -fx-border-color: -accent; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 12 16 12 16; -fx-text-fill: -accent; -fx-font-weight: bold; }
            .theme-dark .warning-banner { -fx-background-color: #0a0a0a; -fx-border-color: -accent; -fx-text-fill: -accent; }

            /* ── Dialogs ───────────────────────────────────── */
            .dialog-pane { -fx-background-color: -bg; }
            .dialog-pane .header-panel { -fx-background-color: -panel-2; }
            .dialog-pane .content.label { -fx-text-fill: -text; }
            .dialog-pane .button-bar .button { -fx-background-color: -panel-2; }
            .dialog-pane .button-bar .button:default { -fx-background-color: -accent; -fx-text-fill: -accent-text; -fx-border-color: -accent; }

            /* ── Charts ───────────────────────────────────── */
            .chart { -fx-background-color: transparent; -fx-padding: 8; }
            .chart-plot-background { -fx-background-color: transparent; }
            .chart-content { -fx-padding: 8; }
            .chart-vertical-grid-lines, .chart-horizontal-grid-lines { -fx-stroke: derive(-border, 0%); -fx-stroke-dash-array: 2 4; }
            .chart-alternative-row-fill, .chart-alternative-column-fill { -fx-fill: transparent; -fx-stroke: transparent; }
            .axis { -fx-tick-label-fill: -text-dim; -fx-font-size: 11px; }
            .axis-label, .chart-title { -fx-text-fill: -text; }
            .axis-tick-mark, .axis-minor-tick-mark { -fx-stroke: -text-dim; }
            .chart-legend { -fx-background-color: transparent; }
            .chart-legend-item { -fx-text-fill: -text; }

            .default-color0.chart-bar { -fx-bar-fill: -accent; }
            .default-color1.chart-bar { -fx-bar-fill: #60a5fa; }
            .default-color2.chart-bar { -fx-bar-fill: #f472b6; }
            .default-color3.chart-bar { -fx-bar-fill: #fb923c; }
            .default-color4.chart-bar { -fx-bar-fill: #a78bfa; }

            .default-color0.chart-pie { -fx-pie-color: -accent; }
            .default-color1.chart-pie { -fx-pie-color: #60a5fa; }
            .default-color2.chart-pie { -fx-pie-color: #f472b6; }
            .default-color3.chart-pie { -fx-pie-color: #fb923c; }
            .default-color4.chart-pie { -fx-pie-color: #a78bfa; }
            .default-color5.chart-pie { -fx-pie-color: #34d399; }

            .default-color0.chart-series-line { -fx-stroke: -accent; -fx-stroke-width: 2.5; }
            .default-color0.chart-line-symbol { -fx-background-color: -accent, -panel; }

            /* check boxes */
            .check-box .box { -fx-background-color: -panel; -fx-border-color: -border; -fx-border-radius: 4; -fx-background-radius: 4; }
            .check-box:selected .mark { -fx-background-color: -accent-text; }
            .check-box:selected .box { -fx-background-color: -accent; -fx-border-color: -accent; }

            .separator .line { -fx-border-color: -border; -fx-border-width: 0 0 1 0; }
            .menu-item { -fx-background-color: -panel; }
            .menu-item .label { -fx-text-fill: -text; }
            .menu-item:focused { -fx-background-color: -accent; }
            .menu-item:focused .label { -fx-text-fill: -accent-text; }
            .context-menu { -fx-background-color: -panel; -fx-border-color: -border; }

            .tooltip { -fx-background-color: #000; -fx-text-fill: #f0f0f0; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 6 10 6 10; }
            """;
        return light + dark + base;
    }
}
