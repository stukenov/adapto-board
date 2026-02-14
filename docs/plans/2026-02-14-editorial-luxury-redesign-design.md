# Editorial Luxury Redesign — Design Document

## Direction
Stripe-inspired Editorial Luxury aesthetic. CSS-first approach — rewrite `styles.css` + minimal template changes (sidebar nav in `AdminLayout.kt`).

## Design Tokens

### Fonts
- **DM Sans** (400, 500, 700) — UI, headings, navigation
- **DM Mono** (400, 500) — codes, table data, timestamps

### Colors
| Token | Value | Usage |
|-------|-------|-------|
| `--primary` | `#1e293b` | Primary text, dark accents |
| `--accent` | `#d97706` | Active states, CTA, focus rings |
| `--accent-hover` | `#b45309` | Accent hover |
| `--accent-light` | `#fffbeb` | Highlight backgrounds |
| `--surface` | `#fafaf9` | Page background |
| `--card` | `#ffffff` | Card/panel background |
| `--border` | `#e7e5e4` | Borders |
| `--muted` | `#78716c` | Secondary text |
| `--danger` | `#dc2626` | Error states |
| `--success` | `#166534` | Success states |
| `--warning` | `#d97706` | Warning states |

### Shadows (warm, multi-layer)
```
--shadow-sm: 0 1px 2px rgba(28,25,23,0.04)
--shadow:    0 1px 3px rgba(28,25,23,0.06), 0 1px 2px rgba(28,25,23,0.04)
--shadow-md: 0 4px 12px rgba(28,25,23,0.08)
--shadow-lg: 0 12px 24px rgba(28,25,23,0.1)
```

### Radii
`--radius-sm: 6px`, `--radius: 10px`, `--radius-lg: 14px`, `--radius-full: 9999px`

## Navigation — Sidebar

Replace horizontal nav with fixed left sidebar:
- Width: `260px`, background: `#1c1917` (stone-900)
- Nav text: `#a8a29e`, hover → `#fafaf9`
- Active: `rgba(217,119,6,0.15)` bg + `#fbbf24` text + `3px` left amber border
- Logo: "Playout Edge" DM Sans 700, white, amber dot

### Groups
1. **Core**: Dashboard, Channels, Devices, Assets
2. **Broadcast**: Schedules, Overlays
3. **Monitor**: Reports, Alerts, Audit
4. **System**: Settings (bottom)

### Top bar
`48px` height, white bg, contains: breadcrumbs + search + notifications

### Mobile
Sidebar hidden, hamburger toggle, slide-in overlay

### Template change
`AdminLayout.kt`: `<nav class="admin-nav">` + `<main>` → `<aside class="sidebar">` + `<div class="main-wrapper"><header class="topbar">` + `<main>`

## Components

### Cards
White bg, `1px solid var(--border)`, `--shadow-sm`, hover → `--shadow-md`

### Stat Cards
- Number: DM Sans 700, 32px, slate-800
- Label: 12px, uppercase, letter-spacing 0.05em, stone-500
- Icon: amber circle 40px

### Tables
- Headers: 11px, uppercase, letter-spacing 0.06em, stone-400
- Rows: alternating transparent/stone-50, hover → amber-50
- Mono data: DM Mono for IDs, timestamps

### Buttons
- Primary: `#1e293b` bg, white text
- Accent CTA: `#d97706` bg, white text
- Secondary: transparent, border stone-300
- All: 38px height, 8px radius, font-weight 500

### Forms
- Focus: `border-color: #d97706` + `box-shadow: 0 0 0 3px rgba(217,119,6,0.1)`
- Labels: 11px, uppercase, letter-spacing, stone-500

### Badges
Pill shape, 11px, DM Sans 500, soft bg + saturated text

## Landing Pages

### Hero
- Heading: DM Sans 700, 56px, line-height 1.1
- Subtitle: 20px, stone-500, max-width 560px
- CTA: amber accent, 48px height
- Background: warm gradient + CSS noise grain

### Features
3-column grid, amber icon circles, alternating section backgrounds

### Pricing
3 cards, featured → amber border-top 3px + scale 1.02, DM Mono prices

### Footer
stone-900 bg, stone-400 text, amber hover links

### Animations
Fade-in on scroll via `@keyframes` + `IntersectionObserver`, section padding 80-120px
