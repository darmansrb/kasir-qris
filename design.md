---
name: Modern Retail Command
colors:
  surface: '#f9f9fe'
  surface-dim: '#d9dade'
  surface-bright: '#f9f9fe'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f3f3f8'
  surface-container: '#ededf2'
  surface-container-high: '#e8e8ed'
  surface-container-highest: '#e2e2e7'
  on-surface: '#1a1c1f'
  on-surface-variant: '#494551'
  inverse-surface: '#2e3034'
  inverse-on-surface: '#f0f0f5'
  outline: '#7a7582'
  outline-variant: '#cbc4d2'
  surface-tint: '#6750a4'
  primary: '#4f378a'
  on-primary: '#ffffff'
  primary-container: '#6750a4'
  on-primary-container: '#e0d2ff'
  inverse-primary: '#cfbcff'
  secondary: '#635a76'
  on-secondary: '#ffffff'
  secondary-container: '#eaddff'
  on-secondary-container: '#69607c'
  tertiary: '#4e388a'
  on-tertiary: '#ffffff'
  tertiary-container: '#6650a4'
  on-tertiary-container: '#dfd2ff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e9ddff'
  primary-fixed-dim: '#cfbcff'
  on-primary-fixed: '#22005d'
  on-primary-fixed-variant: '#4f378a'
  secondary-fixed: '#eaddff'
  secondary-fixed-dim: '#cdc1e2'
  on-secondary-fixed: '#1f1730'
  on-secondary-fixed-variant: '#4b425d'
  tertiary-fixed: '#e8ddff'
  tertiary-fixed-dim: '#cebdff'
  on-tertiary-fixed: '#21005d'
  on-tertiary-fixed-variant: '#4e388a'
  background: '#f9f9fe'
  on-background: '#1a1c1f'
  surface-variant: '#e2e2e7'
typography:
  display-price:
    fontFamily: Work Sans
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Work Sans
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
  headline-md:
    fontFamily: Work Sans
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  body-lg:
    fontFamily: Work Sans
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Work Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-bold:
    fontFamily: Work Sans
    fontSize: 14px
    fontWeight: '700'
    lineHeight: 20px
  label-sm:
    fontFamily: Work Sans
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  gutter: 16px
  margin-mobile: 16px
  margin-desktop: 24px
  touch-target-min: 48px
---

# 🎨 Design System: Modern Retail Command

## Brand & Style
This design system is engineered for high-velocity retail environments where speed, precision, and clarity are paramount. The brand personality is **Professional, Energetic, and Trustworthy**, moving away from passive pastels toward a high-contrast, "active" aesthetic that reduces cognitive load for cashiers.

The visual style follows a **Modern Corporate** direction with a focus on **Tonal Layering**. It prioritizes functional ergonomics—ensuring that the most critical information, like transaction totals and "Pay" buttons, commands immediate attention through vibrant Indigo accents. The result is a UI that feels reliable enough for enterprise finance but energetic enough for a fast-paced boutique or hospitality setting.

---

## Colors
The palette is built on a foundation of high-contrast Indigo and Lavender to create a clear interactive hierarchy. 

*   **Primary Indigo (`#6750A4`):** Reserved for primary action buttons (Pay, Complete, Search) and active states.
*   **Secondary Lavender (`#EADDFF`):** Used for large container backgrounds, such as the "current order" sidebar or category filters, providing a soft but distinct separation from the main workspace.
*   **Neutral High-Contrast:** Surfaces use a crisp white (`#FFFFFF`) with a very light grey (`#F4F4F9`) for background scaffolding to ensure zero glare and maximum readability under bright store lighting.
*   **Semantic Accents:** Energetic Green and Orange are utilized strictly for status communication (e.g., "Paid", "Refunded", "Table Pending"), ensuring they pop against the cooler primary palette.

---

## Typography
**Work Sans** is used across all levels to maintain a grounded, professional feel. The system relies on aggressive weight distribution to create a "glanceable" hierarchy:

*   **Totals & Headers:** Use Bold (700) or Semi-Bold (600) weights. The `display-price` role is specifically designed for the final transaction amount, ensuring it is the most visible element on the screen.
*   **Line Items:** `body-lg` is used for product names in the cart to ensure legibility during fast-paced scanning.
*   **Metadata:** Labels for SKUs or taxes use a smaller, medium-weight font (`label-sm`) to recede visually compared to the primary product data.

---

## Layout & Spacing
The layout uses a **Hybrid Grid** system optimized for touch-screen POS terminals and tablets:

*   **The Receipt Column:** A fixed-width sidebar (typically 320px-400px) on the right or left, using the Lavender secondary color to frame the current transaction.
*   **The Product Workspace:** A fluid 12-column grid for the product catalog, allowing cards to reflow based on screen orientation (Landscape vs. Portrait).
*   **Rhythm:** A strict **8px base unit** is used for all padding and margins. Interactive elements maintain a minimum **48px touch target** to prevent accidental inputs during high-volume periods.

---

## Elevation & Depth
This design system utilizes **Tonal Layering** combined with high-contrast outlines rather than heavy shadows to maintain a clean, "flat-plus" look:

*   **Level 0 (Background):** Light grey (`#F4F4F9`) defines the canvas.
*   **Level 1 (Cards/Work area):** Pure white surfaces with a subtle 1px border (`#E0E0E0`).
*   **Level 2 (Active Elements):** Primary Indigo elements use a soft, diffused ambient shadow (12% opacity) to appear "pressed" or "active."
*   **Modals & Overlays:** Use a heavy backdrop blur (10px) to pull focus to cash-count or split-payment dialogues, ensuring the background order remains visible but non-interactive.

---

## Shapes
The shape language is **Rounded (0.5rem)**. This provides a modern, approachable feel that softens the "industrial" nature of POS hardware. 

*   **Standard Buttons:** `0.5rem` (8px) corner radius.
*   **Product Cards:** `1rem` (16px) corner radius to differentiate "items" from "actions."
*   **Search Inputs:** `0.5rem` (8px) to match the button language, creating a cohesive input row.

---

## Components
*   **Buttons:** Primary buttons use solid Indigo (`#6750A4`) with white text. Secondary buttons use an Indigo outline or a Lavender background with dark purple text.
*   **Product Cards:** Feature a top-aligned image or category icon, with the price anchored to the bottom-right in Work Sans Bold.
*   **Input Fields:** High-contrast 2px borders when focused. Numeric keypads use large, centered typography for rapid data entry.
*   **Chips:** Used for order tags (e.g., "Dine In", "Takeaway"). These use the Secondary Lavender color with a dark Indigo label.
*   **Transaction List:** Zebra-striping is avoided; instead, 1px horizontal dividers and generous vertical padding (16px) are used to separate cart items.
*   **Totals Bar:** A high-elevation, "sticky" footer at the bottom of the receipt column with the `display-price` token.