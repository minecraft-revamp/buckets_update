#!/usr/bin/env python3
# =============================================================================
# Génère la texture du SEAU EN CUIVRE DE NEIGE POUDREUSE.
#
# Principe : on part du seau de fer enneigé du vanilla
# (powder_snow_bucket.png) et on remplace le MÉTAL GRIS du seau par les pixels
# du seau en cuivre (copper_bucket.png). La NEIGE, elle, est conservée.
#
# >>> DEUX RÉGLAGES, tout en haut, c'est tout ce que tu touches : <<<
#
#   SNOWIFY :
#     False = neige EXACTEMENT comme le seau de fer (grise-blanche).
#             ⚠ sur du cuivre, le gris ressort et fait « moitié fer ».
#     True  = on reteinte le gris de la neige en bleu-glacé pour que ça
#             ressemble vraiment à de la neige sur le cuivre. (recommandé)
#
#   MODE :
#     'faithful' = toute la neige du vanilla (tas du dessus + coulée gauche
#                  ET traînée droite, comme le fer).
#     'right'    = tas du dessus + UNIQUEMENT la petite traînée de droite ;
#                  la grosse colonne de gauche redevient cuivre. (recommandé)
#     'dome'     = seulement le tas de neige sur le dessus, corps tout cuivre.
#
# Lancer :  python3 tools/make_powder_snow_texture.py
# (nécessite Python + Pillow :  pip install pillow)
# Ça écrit la PNG dans les DEUX arbres (neoforge/ et fabric/).
# =============================================================================

SNOWIFY = False         # <-- mets True pour reteinter la neige en bleu-glacé
MODE    = 'right'       # <-- 'faithful' | 'right' (tas + traînée droite) | 'dome' (tas seul)
RIGHT_SPLIT = 8         # en mode 'right' : colonne à partir de laquelle on garde la neige sous le rebord

# --- en dessous, pas besoin de toucher ---------------------------------------
from PIL import Image
import pathlib

BASE = pathlib.Path(__file__).resolve().parent.parent   # racine du dépôt
VANILLA = next(BASE.glob('neoforge/build/neoForm/**/assets/minecraft/textures/item/powder_snow_bucket.png'))
TREES = ['neoforge', 'fabric']
DOME_BOTTOM = 5   # en mode 'dome', on garde la neige jusqu'à cette ligne (le rebord)

def is_snow(p):
    """Vrai si le pixel est de la neige (bleuté, ou clair) ; faux = métal du seau."""
    r, g, b, a = p
    if a == 0:
        return False
    lum = 0.299 * r + 0.587 * g + 0.114 * b
    return (b > r) or (lum > 190)

def snowify(p):
    """Reteinte un gris neutre de la neige vers un bleu-blanc glacé."""
    r, g, b, a = p
    neutral = abs(r - g) <= 10 and abs(g - b) <= 10
    lum = 0.299 * r + 0.587 * g + 0.114 * b
    if neutral and lum < 245:               # gris (pas le blanc pur des reflets)
        return (int(r * 0.90), min(255, int(g * 1.05)), min(255, int(b * 1.12)), a)
    return p

vsnow = Image.open(VANILLA).convert('RGBA')
W, H = vsnow.size
vp = vsnow.load()

for tree in TREES:
    copper = Image.open(BASE / tree / 'src/main/resources/assets/buckets_update/textures/item/copper_bucket.png').convert('RGBA')
    cp = copper.load()
    out = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    op = out.load()
    for y in range(H):
        for x in range(W):
            p = vp[x, y]
            if MODE == 'faithful':
                keep_snow = is_snow(p)
            elif MODE == 'right':
                keep_snow = is_snow(p) and (y <= DOME_BOTTOM or x >= RIGHT_SPLIT)
            else:  # 'dome'
                keep_snow = is_snow(p) and y <= DOME_BOTTOM
            if keep_snow:
                op[x, y] = snowify(p) if SNOWIFY else p     # neige
            elif cp[x, y][3] > 0:
                op[x, y] = cp[x, y]                          # corps -> cuivre
            elif p[3] > 0:
                op[x, y] = cp[x, y] if cp[x, y][3] > 0 else p
    dest = BASE / tree / 'src/main/resources/assets/buckets_update/textures/item/copper_powder_snow_bucket.png'
    out.save(dest)
    print('écrit :', dest)

# aperçu ASCII (S = neige, c = cuivre, . = vide)
im = Image.open(BASE / 'neoforge/src/main/resources/assets/buckets_update/textures/item/copper_powder_snow_bucket.png').convert('RGBA')
ip = im.load()
print(f"\naperçu (SNOWIFY={SNOWIFY}, MODE={MODE}) :")
for y in range(H):
    print(''.join('.' if ip[x, y][3] == 0 else ('S' if is_snow(ip[x, y]) else 'c') for x in range(W)))
