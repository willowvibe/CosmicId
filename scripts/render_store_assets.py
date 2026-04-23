#!/usr/bin/env python3
"""
Generates Play Store marketing assets for AgeReveal v1.0:

  - store_listing/icon_512.png            (512 x 512)   launcher icon
  - store_listing/feature_graphic.png     (1024 x 500)  banner
  - store_listing/screenshots/01_hero.png … 07_share.png  (1080 x 2400)

All rendered in the app's warm dark palette to match what a user sees in the
real app. Replace any of these with real device captures when you have them —
file names are pre-sized to Play Console expectations.
"""

from PIL import Image, ImageDraw, ImageFilter, ImageFont
import os, math, random

OUT = "/app/store_listing"
SHOTS = f"{OUT}/screenshots"
os.makedirs(SHOTS, exist_ok=True)

# ── Palette (matches app/src/main/.../ui/theme/Color.kt) ──────────────────
WARM_BLACK      = (20, 18, 15)
WARM_SURFACE    = (31, 27, 22)
WARM_SURFACE_2  = (39, 34, 25)
WARM_INK        = (242, 234, 223)
WARM_INK_MUTE   = (168, 155, 134)
WARM_INK_DIM    = (110, 101, 84)
WARM_TEAL       = (61, 122, 110)
WARM_TEAL_DEEP  = (31, 90, 82)
WARM_AMBER      = (222, 184, 74)
WARM_AMBER_DEEP = (176, 120, 40)

# Fonts — Liberation is always present in Debian
F_SERIF_REG = "/usr/share/fonts/truetype/liberation/LiberationSerif-Regular.ttf"
F_SERIF_BLD = "/usr/share/fonts/truetype/liberation/LiberationSerif-Bold.ttf"
F_SANS      = "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf"
F_SANS_BLD  = "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf"
F_MONO      = "/usr/share/fonts/truetype/liberation/LiberationMono-Regular.ttf"
F_DEVA      = "/usr/share/fonts/truetype/noto/NotoSansDevanagari-Bold.ttf"
F_DEVA_REG  = "/usr/share/fonts/truetype/noto/NotoSansDevanagari-Regular.ttf"

def font(path, size):
    return ImageFont.truetype(path, size)

# ── Shared helpers ────────────────────────────────────────────────────────
def rounded_rect(draw, xy, radius, fill=None, outline=None, width=1):
    draw.rounded_rectangle(xy, radius=radius, fill=fill, outline=outline, width=width)

def text_w(draw, txt, f):
    bbox = draw.textbbox((0, 0), txt, font=f)
    return bbox[2] - bbox[0]

def center_text(draw, xy, txt, f, fill):
    w = text_w(draw, txt, f)
    draw.text((xy[0] - w / 2, xy[1]), txt, font=f, fill=fill)

def radial_glow(size, center, radius, color, alpha_max=80):
    """Return an RGBA image with a soft radial glow."""
    w, h = size
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    pixels = img.load()
    cx, cy = center
    for y in range(max(0, cy - radius), min(h, cy + radius)):
        for x in range(max(0, cx - radius), min(w, cx + radius)):
            dx, dy = x - cx, y - cy
            d = math.sqrt(dx * dx + dy * dy)
            if d < radius:
                a = int(alpha_max * (1 - d / radius) ** 2)
                pixels[x, y] = (*color, a)
    return img.filter(ImageFilter.GaussianBlur(8))

def paste_grain(img, amount=6):
    """Adds subtle monochrome noise for texture."""
    w, h = img.size
    noise = Image.new("L", (w, h))
    n = noise.load()
    rng = random.Random(42)
    for y in range(h):
        for x in range(w):
            n[x, y] = rng.randint(0, amount * 2)
    noise = noise.filter(ImageFilter.GaussianBlur(0.5))
    grain = Image.merge("RGBA", (noise, noise, noise, noise))
    img.alpha_composite(grain)
    return img

# ──────────────────────────────────────────────────────────────────────────
# 1. Launcher icon — 512 x 512
# ──────────────────────────────────────────────────────────────────────────
def make_icon():
    size = 512
    img = Image.new("RGBA", (size, size), WARM_BLACK)
    d = ImageDraw.Draw(img, "RGBA")

    # Corner rounding for a modern squircle feel (Google's adaptive-icon safe zone)
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, size, size], radius=120, fill=255)

    # Base teal-to-amber gradient fill on a separate layer
    base = Image.new("RGBA", (size, size), WARM_TEAL_DEEP)
    bd = ImageDraw.Draw(base, "RGBA")
    for y in range(size):
        t = y / size
        r = int(WARM_TEAL_DEEP[0] * (1 - t) + WARM_AMBER_DEEP[0] * t)
        g = int(WARM_TEAL_DEEP[1] * (1 - t) + WARM_AMBER_DEEP[1] * t)
        b = int(WARM_TEAL_DEEP[2] * (1 - t) + WARM_AMBER_DEEP[2] * t)
        bd.line([(0, y), (size, y)], fill=(r, g, b, 255))

    # Soft amber glow top-right
    glow = radial_glow((size, size), (int(size * 0.72), int(size * 0.3)),
                       220, WARM_AMBER, alpha_max=110)
    base.alpha_composite(glow)

    img.paste(base, (0, 0), mask)

    d = ImageDraw.Draw(img, "RGBA")

    # Inner dial — a subtle ring, not a full outline, for a premium feel
    cx, cy = size // 2, size // 2
    # 4 corner dials at 12/3/6/9 (the "anchor ticks")
    for ang_deg, length in [(-90, 44), (0, 28), (90, 28), (180, 28)]:
        ang = math.radians(ang_deg)
        r1 = size * 0.37
        r2 = r1 + length * 0.65
        x1 = cx + r1 * math.cos(ang);  y1 = cy + r1 * math.sin(ang)
        x2 = cx + r2 * math.cos(ang);  y2 = cy + r2 * math.sin(ang)
        w = 10 if ang_deg == -90 else 6
        d.line([x1, y1, x2, y2], fill=(*WARM_INK, 220), width=w)

    # Secondary fine ticks (every 30°)
    for i in range(12):
        if i in (0, 3, 6, 9):
            continue
        ang = math.radians(i * 30 - 90)
        r1 = size * 0.37
        r2 = r1 + 16
        x1 = cx + r1 * math.cos(ang);  y1 = cy + r1 * math.sin(ang)
        x2 = cx + r2 * math.cos(ang);  y2 = cy + r2 * math.sin(ang)
        d.line([x1, y1, x2, y2], fill=(*WARM_INK, 140), width=3)

    # "A" — serif, centred — the signature AgeReveal numeral
    serif_big = font(F_SERIF_BLD, 290)
    txt = "A"
    bbox = d.textbbox((0, 0), txt, font=serif_big)
    tw = bbox[2] - bbox[0]; th = bbox[3] - bbox[1]
    # Subtle drop shadow for depth
    for dx, dy, a in [(0, 6, 110), (0, 3, 70)]:
        d.text((cx - tw // 2 - bbox[0] + dx, cy - th // 2 - bbox[1] + dy),
               txt, font=serif_big, fill=(0, 0, 0, a))
    d.text((cx - tw // 2 - bbox[0], cy - th // 2 - bbox[1]),
           txt, font=serif_big, fill=WARM_INK)

    # Fine grain for premium texture
    img = paste_grain(img, amount=3)
    img.save(f"{OUT}/icon_512.png", "PNG", optimize=True)
    print(f"✓ icon_512.png  ({size}x{size})")

# ──────────────────────────────────────────────────────────────────────────
# 2. Feature graphic — 1024 x 500
# ──────────────────────────────────────────────────────────────────────────
def make_feature():
    W, H = 1024, 500
    img = Image.new("RGBA", (W, H), WARM_BLACK)
    d = ImageDraw.Draw(img, "RGBA")

    # Amber glow behind headline
    glow = radial_glow((W, H), (300, 260), 320, WARM_AMBER, alpha_max=110)
    img.alpha_composite(glow)
    glow2 = radial_glow((W, H), (830, 250), 280, WARM_TEAL, alpha_max=90)
    img.alpha_composite(glow2)

    headline_f = font(F_SERIF_BLD, 86)
    sub_f      = font(F_SANS_BLD, 24)
    label_f    = font(F_SANS_BLD, 20)
    brand_f    = font(F_SANS_BLD, 22)
    deva_sub_f = font(F_DEVA, 22)

    # Top label — amber
    d.text((60, 50), "A G E   R E V E A L", font=label_f, fill=WARM_AMBER)

    # Headline — two-line serif, slight amber accent on last word
    d.text((60, 85),  "Live your age", font=headline_f, fill=WARM_INK)
    d.text((60, 185), "to the ",       font=headline_f, fill=WARM_INK)
    w = text_w(d, "to the ", headline_f)
    d.text((60 + w, 185), "second.", font=headline_f, fill=WARM_AMBER)

    # Subtitle — brighter off-white with labelled chips below
    d.text((60, 310), "Western · Vedic · Nakshatra · Chinese zodiac",
           font=sub_f, fill=WARM_INK)

    # Feature chips row — pill-shaped, coloured
    chips = [
        ("Milestones",    WARM_TEAL),
        ("Widgets",       WARM_AMBER),
        ("EN",            WARM_INK_MUTE),
    ]
    cx_chip = 60
    for label, col in chips:
        tw2 = text_w(d, label, sub_f)
        rounded_rect(d, [cx_chip, 360, cx_chip + tw2 + 36, 402], 22,
                     fill=(col[0], col[1], col[2], 60),
                     outline=col, width=2)
        d.text((cx_chip + 18, 368), label, font=sub_f, fill=col)
        cx_chip += tw2 + 48
    # Devanagari chip (must use Noto)
    hi_tw = text_w(d, "हिन्दी", deva_sub_f)
    rounded_rect(d, [cx_chip, 360, cx_chip + hi_tw + 36, 402], 22,
                 fill=(WARM_AMBER[0], WARM_AMBER[1], WARM_AMBER[2], 60),
                 outline=WARM_AMBER, width=2)
    d.text((cx_chip + 18, 371), "हिन्दी", font=deva_sub_f, fill=WARM_AMBER)

    # Brand footer (bigger + more prominent)
    d.text((60, 450), "AGEREVEAL · ANDROID", font=brand_f, fill=WARM_INK_MUTE)

    # ── Right-side clock dial — bolder ring + numeral ─────────────
    cx, cy, r = 830, 250, 170
    # Outer ring — thick and glowy
    d.ellipse([cx - r, cy - r, cx + r, cy + r], outline=WARM_TEAL, width=10)
    # Inner fill for contrast
    d.ellipse([cx - r + 18, cy - r + 18, cx + r - 18, cy + r - 18],
              fill=(*WARM_SURFACE, 220))
    # 12 major ticks + bigger numerals
    for i in range(12):
        ang = math.radians(i * 30 - 90)
        r1 = r - 24
        r2 = r - 6
        x1 = cx + r1 * math.cos(ang);  y1 = cy + r1 * math.sin(ang)
        x2 = cx + r2 * math.cos(ang);  y2 = cy + r2 * math.sin(ang)
        d.line([x1, y1, x2, y2], fill=WARM_TEAL, width=5)
    # Minute ticks (60)
    for i in range(60):
        if i % 5 == 0:
            continue
        ang = math.radians(i * 6 - 90)
        r1 = r - 14
        r2 = r - 6
        x1 = cx + r1 * math.cos(ang);  y1 = cy + r1 * math.sin(ang)
        x2 = cx + r2 * math.cos(ang);  y2 = cy + r2 * math.sin(ang)
        d.line([x1, y1, x2, y2], fill=WARM_INK_DIM, width=2)

    # Hour/minute hands — amber + cream, thick
    d.line([cx, cy, cx + 90, cy - 20], fill=WARM_AMBER, width=10)
    d.line([cx, cy, cx - 40, cy - 110], fill=WARM_INK, width=6)
    d.ellipse([cx - 12, cy - 12, cx + 12, cy + 12], fill=WARM_AMBER)

    # "35" numeral inside dial
    age_f = font(F_SERIF_BLD, 68)
    center_text(d, (cx - 2, cy + 22), "35", age_f, WARM_INK)
    small_f = font(F_SANS_BLD, 16)
    center_text(d, (cx, cy + 96), "YEARS · 10M · 08D", small_f, WARM_INK_MUTE)

    img = paste_grain(img, amount=4)
    img.convert("RGB").save(f"{OUT}/feature_graphic.png", "PNG", optimize=True)
    print(f"✓ feature_graphic.png ({W}x{H})")

# ──────────────────────────────────────────────────────────────────────────
# 3. Phone screenshots — 1080 x 2400
# ──────────────────────────────────────────────────────────────────────────
PHONE_W, PHONE_H = 1080, 2400

def phone_canvas():
    img = Image.new("RGBA", (PHONE_W, PHONE_H), WARM_BLACK)
    return img, ImageDraw.Draw(img, "RGBA")

def top_bar(d, title="AgeReveal", live=True):
    # Clock glyph + brand
    d.ellipse([60, 90, 92, 122], fill=WARM_AMBER)
    d.text((120, 82), "AgeReveal", font=font(F_SERIF_REG, 44), fill=WARM_INK)
    if live:
        d.text((PHONE_W - 260, 96), "LIVE", font=font(F_SANS_BLD, 26), fill=WARM_INK_DIM)
        rounded_rect(d, [PHONE_W - 180, 80, PHONE_W - 112, 148], radius=34, fill=WARM_SURFACE)
        d.text((PHONE_W - 168, 98), "⚙", font=font(F_SANS, 32), fill=WARM_INK_MUTE)

def bottom_nav(d, selected="Age"):
    # Fake nav with 7 icons
    y = PHONE_H - 170
    d.rectangle([0, y - 10, PHONE_W, PHONE_H], fill=WARM_BLACK)
    d.line([40, y, PHONE_W - 40, y], fill=WARM_SURFACE_2, width=2)
    labels = ["Age", "Profile", "Compare", "Match", "Bdays", "Settings", "Timeline"]
    icons  = ["🧮",  "★",      "↔",       "♥",     "🎂",    "⚙",         "≡"]
    cw = PHONE_W // len(labels)
    for i, (lab, ic) in enumerate(zip(labels, icons)):
        cx = i * cw + cw // 2
        is_sel = (lab == selected)
        col = WARM_INK if is_sel else WARM_INK_DIM
        if is_sel:
            rounded_rect(d, [cx - 42, y + 14, cx + 42, y + 60], radius=22, fill=WARM_SURFACE)
        d.text((cx - 12, y + 20), ic, font=font(F_SANS, 32), fill=col)
        lw = text_w(d, lab, font(F_SANS, 22))
        d.text((cx - lw // 2, y + 70), lab, font=font(F_SANS, 22), fill=col)

def banner_ad(d):
    y = PHONE_H - 290
    rounded_rect(d, [60, y, PHONE_W - 60, y + 100], 14, fill=WARM_SURFACE_2)
    d.text((100, y + 30), "Ad · supported by AdMob",
           font=font(F_SANS, 28), fill=WARM_INK_DIM)

# ── Shot 1: Hero (Calculator)
def shot_01_hero():
    img, d = phone_canvas()
    top_bar(d)

    # Name input
    rounded_rect(d, [60, 210, PHONE_W - 60, 320], 14, outline=WARM_INK_DIM, width=2)
    d.text((90, 222), "NAME", font=font(F_SANS, 22), fill=WARM_INK_DIM)
    d.text((90, 250), "Arjun",  font=font(F_SANS, 44), fill=WARM_INK)

    # Born row
    d.text((60, 360), "BORN",       font=font(F_SANS_BLD, 22), fill=WARM_INK_DIM)
    d.text((60, 400), "15 June 1990", font=font(F_SERIF_REG, 44), fill=WARM_INK)
    rounded_rect(d, [PHONE_W - 140, 380, PHONE_W - 60, 460], 40, fill=WARM_SURFACE)
    d.text((PHONE_W - 118, 402), "✏", font=font(F_SANS, 34), fill=WARM_INK_MUTE)
    d.line([60, 480, PHONE_W - 60, 480], fill=WARM_SURFACE_2, width=2)

    # Birth time row
    d.text((60, 510), "BIRTH TIME (OPTIONAL)", font=font(F_SANS_BLD, 20), fill=WARM_INK_DIM)
    d.text((60, 545), "04:37 AM", font=font(F_SANS, 34), fill=WARM_TEAL)
    rounded_rect(d, [PHONE_W - 140, 520, PHONE_W - 60, 600], 40, fill=WARM_SURFACE)
    d.text((PHONE_W - 118, 540), "⏰", font=font(F_SANS, 34), fill=WARM_TEAL)
    d.line([60, 620, PHONE_W - 60, 620], fill=WARM_SURFACE_2, width=2)

    # Clock hero
    d.text((60, 720), "35",  font=font(F_SERIF_BLD, 280), fill=WARM_INK)
    d.text((640, 870), "10", font=font(F_SERIF_BLD, 150), fill=WARM_INK)
    d.text((860, 870), "08", font=font(F_SERIF_BLD, 150), fill=WARM_INK)
    d.text((60, 1020),  "YEARS",  font=font(F_SANS_BLD, 22), fill=WARM_INK_DIM)
    d.text((640, 1020), "MONTHS", font=font(F_SANS_BLD, 22), fill=WARM_INK_DIM)
    d.text((860, 1020), "DAYS",   font=font(F_SANS_BLD, 22), fill=WARM_INK_DIM)

    # Seconds alive strip
    rounded_rect(d, [60, 1100, PHONE_W - 60, 1240], 18, fill=WARM_SURFACE)
    d.ellipse([96, 1150, 116, 1170], fill=WARM_AMBER)
    d.text((140, 1125), "SECONDS ALIVE", font=font(F_SANS_BLD, 22), fill=WARM_INK_DIM)
    d.text((140, 1160), "1,118,592,340", font=font(F_MONO, 44), fill=WARM_AMBER)
    d.text((PHONE_W - 260, 1145), "+1 per\nsecond",
           font=font(F_SANS, 22), fill=WARM_INK_DIM)

    # Mini stat chips
    chips = [("DAYS", "12,948", WARM_INK), ("HOURS", "310.7K", WARM_INK),
             ("NEXT BDAY", "48d", WARM_AMBER)]
    for i, (k, v, c) in enumerate(chips):
        x1 = 60 + i * 330
        rounded_rect(d, [x1, 1280, x1 + 310, 1420], 14, fill=WARM_SURFACE)
        d.text((x1 + 20, 1296), k, font=font(F_SANS_BLD, 20), fill=WARM_INK_DIM)
        d.text((x1 + 20, 1332), v, font=font(F_SANS_BLD, 40), fill=c)

    # Next milestone chip
    rounded_rect(d, [60, 1470, PHONE_W - 60, 1600], 14,
                 fill=(WARM_AMBER[0], WARM_AMBER[1], WARM_AMBER[2], 40))
    d.text((90, 1486), "✦", font=font(F_SANS_BLD, 40), fill=WARM_AMBER)
    d.text((140, 1490), "NEXT MILESTONE", font=font(F_SANS_BLD, 22), fill=WARM_AMBER)
    d.text((140, 1528), "13,000 days alive — in 52 days",
           font=font(F_SANS_BLD, 30), fill=WARM_INK)
    d.text((PHONE_W - 140, 1498), "52", font=font(F_SERIF_BLD, 54), fill=WARM_AMBER)

    # Profile tease
    rounded_rect(d, [60, 1640, PHONE_W - 60, 1820], 18, fill=WARM_SURFACE)
    d.text((90, 1660), "YOUR VEDIC & COSMIC PROFILE",
           font=font(F_SANS_BLD, 22), fill=WARM_INK_DIM)
    d.text((90, 1700), "Tap to reveal your profile",
           font=font(F_SANS, 28), fill=WARM_INK_MUTE)
    d.ellipse([PHONE_W - 180, 1680, PHONE_W - 100, 1760], fill=WARM_TEAL)
    d.text((PHONE_W - 158, 1696), "★", font=font(F_SANS_BLD, 48), fill=WARM_BLACK)

    banner_ad(d)
    bottom_nav(d, "Age")
    img.convert("RGB").save(f"{SHOTS}/01_hero.png", "PNG", optimize=True)
    print("✓ 01_hero.png")

# ── Shot 2: Unlocked cosmic profile
def shot_02_profile():
    img, d = phone_canvas()
    top_bar(d, live=False)
    d.text((60, 200), "Your profile", font=font(F_SERIF_REG, 58), fill=WARM_INK)
    d.text((60, 270), "Born under a waning gibbous moon",
           font=font(F_SANS, 24), fill=WARM_INK_MUTE)

    # Big astro tile with glow
    rounded_rect(d, [60, 340, PHONE_W - 60, 860], 18, fill=WARM_SURFACE)
    glow = radial_glow((PHONE_W, 520), (PHONE_W - 180, 50), 300, WARM_TEAL, alpha_max=90)
    tile_glow = Image.new("RGBA", (PHONE_W, 520), (0, 0, 0, 0))
    tile_glow.alpha_composite(glow)
    img.alpha_composite(tile_glow, (0, 340))

    d.text((90, 370), "WESTERN · VEDIC · CHINESE",
           font=font(F_SANS_BLD, 22), fill=WARM_INK_DIM)
    d.text((90, 420), "Gemini ♊ · Vrishabha",
           font=font(F_SERIF_BLD, 60), fill=WARM_INK)
    d.text((90, 510), "Year of the Metal Horse",
           font=font(F_SERIF_REG, 42), fill=WARM_INK)
    d.text((90, 580), "Nakshatra",
           font=font(F_SANS, 26), fill=WARM_INK_MUTE)
    d.text((90, 618), "Rohini (रोहिणी)",
           font=font(F_SERIF_BLD, 38), fill=WARM_INK)
    d.text((90, 680), "Heartbeat estimate",
           font=font(F_SANS, 26), fill=WARM_INK_MUTE)
    d.text((90, 718), "♥  1.34 B and counting",
           font=font(F_SERIF_BLD, 34), fill=WARM_AMBER)

    # Life timeline mini
    rounded_rect(d, [60, 900, PHONE_W - 60, 1260], 18, fill=WARM_SURFACE)
    d.text((90, 930), "LIFE TIMELINE", font=font(F_SANS_BLD, 22), fill=WARM_INK_DIM)
    # Progress
    rounded_rect(d, [90, 980, PHONE_W - 90, 1010], 6, fill=WARM_SURFACE_2)
    rounded_rect(d, [90, 980, 500, 1010], 6, fill=WARM_TEAL)
    d.text((90, 1028), "LIFE LIVED · 43% · ~35 yrs of 80",
           font=font(F_SANS_BLD, 22), fill=WARM_INK_MUTE)

    # milestone rows
    rows = [("10,000th day", "18 Feb 2018", "✓", WARM_TEAL),
            ("12,500th day", "31 Aug 2024", "✓", WARM_TEAL),
            ("13,000th day", "12 Jan 2026", "IN 52D", WARM_AMBER)]
    for i, (name, date, status, col) in enumerate(rows):
        y = 1080 + i * 60
        d.ellipse([100, y, 120, y + 20], fill=col)
        d.text((150, y - 6), name, font=font(F_SANS_BLD, 26), fill=WARM_INK)
        d.text((150, y + 22), date, font=font(F_SANS, 22), fill=WARM_INK_DIM)
        d.text((PHONE_W - 230, y - 4), status, font=font(F_SANS_BLD, 24), fill=col)

    # Share card CTA
    rounded_rect(d, [60, 1310, PHONE_W - 60, 1440], 18, fill=WARM_SURFACE)
    d.text((100, 1338), "Share your cosmic profile card",
           font=font(F_SANS_BLD, 28), fill=WARM_INK)
    d.text((100, 1378), "Three themes · 900×900 square",
           font=font(F_SANS, 22), fill=WARM_INK_MUTE)
    d.ellipse([PHONE_W - 170, 1332, PHONE_W - 100, 1402], fill=WARM_TEAL)
    d.text((PHONE_W - 158, 1344), "↗", font=font(F_SANS_BLD, 42), fill=WARM_BLACK)

    bottom_nav(d, "Profile")
    img.convert("RGB").save(f"{SHOTS}/02_profile.png", "PNG", optimize=True)
    print("✓ 02_profile.png")

# ── Shot 3: Life Timeline
def shot_03_timeline():
    img, d = phone_canvas()
    top_bar(d, live=False)
    d.text((60, 210), "Life Timeline", font=font(F_SERIF_REG, 58), fill=WARM_INK)
    d.text((60, 286), "Every milestone, past and future.",
           font=font(F_SANS, 26), fill=WARM_INK_MUTE)

    milestones = [
        ("500 days",    "29 Oct 1991", "✓",      True),
        ("1,000 days",  "12 Mar 1993", "✓",      True),
        ("5,000 days",  "5 Apr 2004",  "✓",      True),
        ("10,000 days", "18 Feb 2018", "✓",      True),
        ("12,500 days", "31 Aug 2024", "✓",      True),
        ("13,000 days", "12 Jan 2026", "IN 52D", False),
        ("15,000 days", "5 May 2031",  "IN 1975D", False),
        ("20,000 days", "17 Jan 2045", "IN 6930D", False),
    ]
    y = 370
    for name, date, status, past in milestones:
        rounded_rect(d, [60, y, PHONE_W - 60, y + 160], 16, fill=WARM_SURFACE)
        # dot
        bg = (*WARM_TEAL, 80) if past else WARM_SURFACE_2
        d.ellipse([90, y + 50, 160, y + 120], fill=bg)
        ic = "🎂" if past else name.split(',')[0].replace(" days", "d").replace(" d", "d")
        d.text((102, y + 58), ic, font=font(F_SANS_BLD, 38),
               fill=WARM_TEAL if past else WARM_INK_DIM)
        # Text
        d.text((200, y + 40), name,
               font=font(F_SERIF_BLD, 38), fill=WARM_INK)
        d.text((200, y + 92), date,
               font=font(F_SANS, 22), fill=WARM_INK_DIM)
        col = WARM_TEAL if past else WARM_AMBER
        sw = text_w(d, status, font(F_SANS_BLD, 28))
        d.text((PHONE_W - 100 - sw, y + 60), status,
               font=font(F_SANS_BLD, 28), fill=col)
        y += 180

    bottom_nav(d, "Timeline")
    img.convert("RGB").save(f"{SHOTS}/03_timeline.png", "PNG", optimize=True)
    print("✓ 03_timeline.png")

# ── Shot 4: Compatibility
def shot_04_match():
    img, d = phone_canvas()
    top_bar(d, live=False)
    d.text((60, 210), "Cosmic Match", font=font(F_SERIF_REG, 58), fill=WARM_INK)

    # Two date picker cards
    for i, (name, date, emoji) in enumerate([("You", "15 Jun 1990", "🌞"),
                                             ("Priya", "3 Aug 1992", "🌙")]):
        y = 330 + i * 220
        rounded_rect(d, [60, y, PHONE_W - 60, y + 180], 18, fill=WARM_SURFACE)
        d.text((110, y + 40), emoji, font=font(F_SANS_BLD, 64), fill=WARM_INK)
        d.text((220, y + 40), name, font=font(F_SERIF_BLD, 40), fill=WARM_INK)
        d.text((220, y + 100), date, font=font(F_SANS, 26), fill=WARM_INK_MUTE)

    # Big score
    glow = radial_glow((PHONE_W, 400), (PHONE_W // 2, 200), 280, WARM_TEAL, alpha_max=120)
    tg = Image.new("RGBA", (PHONE_W, 400), (0, 0, 0, 0))
    tg.alpha_composite(glow)
    img.alpha_composite(tg, (0, 830))
    center_text(d, (PHONE_W // 2, 900), "87%",
                font(F_SERIF_BLD, 240), WARM_TEAL)
    center_text(d, (PHONE_W // 2, 1180),
                "A fire-earth union — she grounds your drive.",
                font(F_SERIF_REG, 32), WARM_INK)

    # Sub scores
    for i, (lab, val) in enumerate([("Western", "81%"), ("Element", "Earth · Fire"),
                                     ("Chinese", "92%")]):
        x = 60 + i * 330
        rounded_rect(d, [x, 1280, x + 310, 1420], 14, fill=WARM_SURFACE)
        d.text((x + 20, 1296), lab.upper(), font=font(F_SANS_BLD, 20), fill=WARM_INK_DIM)
        d.text((x + 20, 1332), val, font=font(F_SANS_BLD, 36), fill=WARM_INK)

    # Share row
    rounded_rect(d, [60, 1470, PHONE_W - 60, 1600], 16, fill=WARM_TEAL)
    center_text(d, (PHONE_W // 2, 1512), "Share match card ↗",
                font(F_SANS_BLD, 34), WARM_BLACK)

    banner_ad(d)
    bottom_nav(d, "Match")
    img.convert("RGB").save(f"{SHOTS}/04_match.png", "PNG", optimize=True)
    print("✓ 04_match.png")

# ── Shot 5: Saved birthdays + widget
def shot_05_birthdays():
    img, d = phone_canvas()
    top_bar(d, live=False)
    d.text((60, 210), "Birthdays", font=font(F_SERIF_REG, 58), fill=WARM_INK)
    d.text((60, 286), "4 saved · next in 12d",
           font=font(F_SANS, 26), fill=WARM_INK_MUTE)

    # + button
    rounded_rect(d, [PHONE_W - 180, 210, PHONE_W - 90, 300], 50, fill=WARM_TEAL)
    d.text((PHONE_W - 155, 222), "+", font=font(F_SANS_BLD, 60), fill=WARM_BLACK)

    # Hero (up next)
    rounded_rect(d, [60, 360, PHONE_W - 60, 620], 20, fill=WARM_AMBER_DEEP)
    d.text((100, 390), "UP NEXT · IN 12D", font=font(F_SANS_BLD, 22), fill=WARM_INK)
    d.text((100, 440), "🎂", font=font(F_SANS, 68), fill=WARM_INK)
    d.text((210, 446), "Amma",
           font=font(F_SERIF_BLD, 64), fill=WARM_INK)
    d.text((100, 552), "Thu, 5 Feb · turning 62",
           font=font(F_SANS, 28), fill=WARM_INK)

    # Other saved birthdays list
    list_y = 680
    rows = [("Arjun Jr", "❤️", "Jun 15",  "in 148d"),
            ("Priya",   "🌟", "Aug 3",   "in 197d"),
            ("Dhruv",   "🚀", "Dec 22",  "in 338d")]
    for name, emoji, date, away in rows:
        d.text((90, list_y + 10), emoji, font=font(F_SANS, 40), fill=WARM_INK)
        d.text((180, list_y), name, font=font(F_SANS_BLD, 38), fill=WARM_INK)
        d.text((180, list_y + 50), away,
               font=font(F_SANS_BLD, 22), fill=WARM_INK_DIM)
        sw = text_w(d, date, font(F_SERIF_REG, 34))
        d.text((PHONE_W - 100 - sw, list_y + 10), date,
               font=font(F_SERIF_REG, 34), fill=WARM_INK_MUTE)
        d.line([60, list_y + 110, PHONE_W - 60, list_y + 110], fill=WARM_SURFACE_2, width=2)
        list_y += 130

    # Widget preview card
    rounded_rect(d, [60, 1220, PHONE_W - 60, 1600], 20, fill=(26, 26, 46, 255))
    d.text((100, 1258), "🎂 UPCOMING BIRTHDAYS",
           font=font(F_SANS_BLD, 22), fill=(255, 255, 255, 153))
    rows2 = [("🎂 Amma",     "12d", True,  (134, 239, 172)),
             ("❤️ Arjun Jr", "148d", False, (255, 183, 77)),
             ("🌟 Priya",    "197d", False, (255, 183, 77))]
    for i, (nm, dl, hi, col) in enumerate(rows2):
        y = 1310 + i * 80
        d.text((100, y), nm,
               font=font(F_SANS_BLD if hi else F_SANS, 30 if hi else 26),
               fill=(255, 255, 255, 200 if hi else 140))
        dw = text_w(d, dl, font(F_SANS_BLD, 40 if hi else 32))
        d.text((PHONE_W - 120 - dw, y - 8), dl,
               font=font(F_SANS_BLD, 40 if hi else 32), fill=col)
    d.text((100, 1560), "4×2 home-screen widget",
           font=font(F_SANS, 22), fill=WARM_INK_DIM)

    bottom_nav(d, "Bdays")
    img.convert("RGB").save(f"{SHOTS}/05_birthdays.png", "PNG", optimize=True)
    print("✓ 05_birthdays.png")

# ── Shot 6: Settings (Hindi + theme + milestone toggles)
def shot_06_settings():
    img, d = phone_canvas()
    top_bar(d, live=False)
    d.text((60, 200), "Settings", font=font(F_SERIF_REG, 58), fill=WARM_INK)

    # Notifications section
    d.text((60, 310), "NOTIFICATIONS",
           font=font(F_SANS_BLD, 22), fill=WARM_INK_DIM)
    rounded_rect(d, [60, 350, PHONE_W - 60, 560], 18, fill=WARM_SURFACE)
    d.text((90, 380), "Birthday reminders",
           font=font(F_SANS_BLD, 30), fill=WARM_INK)
    d.text((90, 420), "Enable or disable all birthday notifications",
           font=font(F_SANS, 22), fill=WARM_INK_DIM)
    # Switch (on)
    rounded_rect(d, [PHONE_W - 210, 380, PHONE_W - 100, 444], 32, fill=WARM_TEAL)
    d.ellipse([PHONE_W - 158, 388, PHONE_W - 102, 440], fill=WARM_BLACK)
    d.line([90, 480, PHONE_W - 90, 480], fill=WARM_SURFACE_2, width=2)
    d.text((90, 500), "Reminder time · 9:00 AM",
           font=font(F_SANS_BLD, 24), fill=WARM_INK)

    # Milestone grid (3x4)
    d.text((60, 610), "MILESTONE NOTIFICATIONS",
           font=font(F_SANS_BLD, 22), fill=WARM_INK_DIM)
    rounded_rect(d, [60, 650, PHONE_W - 60, 990], 18, fill=WARM_SURFACE)
    d.text((90, 680), "Pick which life-day milestones will notify you.",
           font=font(F_SANS, 22), fill=WARM_INK_DIM)
    targets = [(500, True), (1000, True), (2000, True), (3000, False),
               (5000, True), (7000, True), (10000, True), (12500, True),
               (15000, True), (20000, False), (25000, True), (30000, False)]
    cw = (PHONE_W - 60 * 2 - 40) // 3
    for i, (t, on) in enumerate(targets):
        r, c = i // 3, i % 3
        x = 90 + c * (cw + 12)
        y = 740 + r * 58
        bg = (WARM_TEAL[0], WARM_TEAL[1], WARM_TEAL[2], 46) if on else WARM_SURFACE_2
        rounded_rect(d, [x, y, x + cw, y + 48], 10, fill=bg)
        if on:
            rounded_rect(d, [x, y, x + cw, y + 48], 10, outline=WARM_TEAL, width=2)
        lab = f"{t:,}"
        fw = text_w(d, lab, font(F_SANS_BLD, 26))
        d.text((x + (cw - fw) // 2, y + 10), lab,
               font=font(F_SANS_BLD, 26),
               fill=WARM_TEAL if on else WARM_INK_DIM)

    # Appearance
    d.text((60, 1040), "APPEARANCE",
           font=font(F_SANS_BLD, 22), fill=WARM_INK_DIM)
    rounded_rect(d, [60, 1080, PHONE_W - 60, 1310], 18, fill=WARM_SURFACE)
    options = [("System Default", False), ("Light", False), ("Dark", True)]
    for i, (lab, sel) in enumerate(options):
        y = 1110 + i * 66
        bg = (WARM_TEAL[0], WARM_TEAL[1], WARM_TEAL[2], 46) if sel else WARM_SURFACE_2
        rounded_rect(d, [90, y, PHONE_W - 90, y + 56], 12, fill=bg)
        if sel:
            rounded_rect(d, [90, y, PHONE_W - 90, y + 56], 12, outline=WARM_TEAL, width=2)
        d.text((120, y + 14), lab, font=font(F_SANS_BLD, 26),
               fill=WARM_TEAL if sel else WARM_INK_MUTE)

    # Language
    d.text((60, 1360), "LANGUAGE",
           font=font(F_SANS_BLD, 22), fill=WARM_INK_DIM)
    rounded_rect(d, [60, 1400, PHONE_W - 60, 1640], 18, fill=WARM_SURFACE)
    lang = [("System Default", False), ("English", False), ("हिन्दी (Hindi)", True)]
    for i, (lab, sel) in enumerate(lang):
        y = 1430 + i * 66
        bg = (WARM_TEAL[0], WARM_TEAL[1], WARM_TEAL[2], 46) if sel else WARM_SURFACE_2
        rounded_rect(d, [90, y, PHONE_W - 90, y + 56], 12, fill=bg)
        if sel:
            rounded_rect(d, [90, y, PHONE_W - 90, y + 56], 12, outline=WARM_TEAL, width=2)
        # Use Noto Sans Devanagari when rendering Hindi glyphs
        fnt = font(F_DEVA, 28) if "ह" in lab else font(F_SANS_BLD, 26)
        d.text((120, y + 12), lab, font=fnt,
               fill=WARM_TEAL if sel else WARM_INK_MUTE)

    # Data / CSV export row
    d.text((60, 1700), "DATA",
           font=font(F_SANS_BLD, 22), fill=WARM_INK_DIM)
    rounded_rect(d, [60, 1740, PHONE_W - 60, 1860], 18, fill=WARM_SURFACE)
    rounded_rect(d, [90, 1770, PHONE_W - 90, 1830], 12, fill=WARM_SURFACE_2)
    d.text((120, 1780), "Export birthdays (CSV)",
           font=font(F_SANS_BLD, 28), fill=WARM_INK)
    d.text((PHONE_W - 170, 1780), "⇣",
           font=font(F_SANS_BLD, 32), fill=WARM_TEAL)

    bottom_nav(d, "Settings")
    img.convert("RGB").save(f"{SHOTS}/06_settings.png", "PNG", optimize=True)
    print("✓ 06_settings.png")

# ── Shot 7: Share card preview (Dark Cosmos)
def shot_07_share():
    img, d = phone_canvas()
    top_bar(d, live=False)
    d.text((60, 210), "Share your card", font=font(F_SERIF_REG, 56), fill=WARM_INK)
    d.text((60, 286), "Three themes, 900×900 — never cropped.",
           font=font(F_SANS, 26), fill=WARM_INK_MUTE)

    # Theme tabs
    themes = ["Dark Cosmos", "Minimal Light", "Festive India"]
    for i, t in enumerate(themes):
        x = 60 + i * 330
        sel = (i == 0)
        bg = WARM_SURFACE if sel else WARM_SURFACE_2
        rounded_rect(d, [x, 370, x + 310, 440], 12, fill=bg)
        if sel:
            rounded_rect(d, [x, 370, x + 310, 440], 12, outline=WARM_TEAL, width=2)
        tw = text_w(d, t, font(F_SANS_BLD, 24))
        d.text((x + (310 - tw) // 2, 388), t,
               font=font(F_SANS_BLD, 24),
               fill=WARM_INK if sel else WARM_INK_DIM)

    # Big card preview — mimic DARK_COSMOS share card
    cx, cy = 540, 1120
    card = 880
    x0, y0 = cx - card // 2, cy - card // 2
    # Gradient background
    card_img = Image.new("RGBA", (card, card), (26, 26, 46, 255))
    cd = ImageDraw.Draw(card_img, "RGBA")
    for y in range(card):
        c = int(26 + (16 - 26) * (y / card))
        cd.line([(0, y), (card, y)], fill=(26, c, 62))
    cd.text((60, 90), "MY AGE TODAY",
            font=font(F_SANS_BLD, 28), fill=(255, 255, 255, 140))
    cd.text((60, 150), "35 yrs  10 mo  08 days",
            font=font(F_SERIF_BLD, 74), fill=(255, 255, 255, 255))
    cd.text((60, 240), "Born Friday, 15 June 1990",
            font=font(F_SANS, 26), fill=(255, 255, 255, 180))
    # Stat cards
    stats = [("Total days", "12,948"), ("To birthday", "48d"),
             ("Zodiac", "Gemini ♊"), ("Rashi", "Vrishabha")]
    for i, (lab, val) in enumerate(stats):
        r, c = i // 2, i % 2
        sx = 60 + c * 420
        sy = 360 + r * 180
        cd.rounded_rectangle([sx, sy, sx + 380, sy + 150], 16, fill=(255, 255, 255, 30))
        cd.text((sx + 22, sy + 14), val,
                font=font(F_SERIF_BLD, 44), fill=(134, 239, 172, 255))
        cd.text((sx + 22, sy + 80), lab,
                font=font(F_SANS, 22), fill=(255, 255, 255, 140))
    cd.text((card - 290, card - 36), "Made with AgeReveal",
            font=font(F_SANS, 18), fill=(255, 255, 255, 80))
    card_img = card_img.resize((880, 880))
    img.alpha_composite(card_img, (x0, y0))

    # Share pill
    rounded_rect(d, [60, 2110, PHONE_W - 60, 2240], 20, fill=WARM_TEAL)
    center_text(d, (PHONE_W // 2, 2148), "Share via WhatsApp, Instagram, X",
                font(F_SANS_BLD, 32), WARM_BLACK)

    bottom_nav(d, "Age")
    img.convert("RGB").save(f"{SHOTS}/07_share.png", "PNG", optimize=True)
    print("✓ 07_share.png")

# ── Run all ────────────────────────────────────────────────────────────────
if __name__ == "__main__":
    make_icon()
    make_feature()
    shot_01_hero()
    shot_02_profile()
    shot_03_timeline()
    shot_04_match()
    shot_05_birthdays()
    shot_06_settings()
    shot_07_share()
    print("\nAll Play Store assets rendered.")
