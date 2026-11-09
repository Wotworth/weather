# Generates the corner scenes and decorated eggs (WX-6.12, WX-6.13) as vector drawables.
# Run from the repo root with the Fluent icons at fluent-icons/. Output: app/src/main/res/drawable/.
import random, re, math
OUT='app/src/main/res/drawable/'
ICONS='fluent-icons/'  # the unpacked @fluentui/svg-icons 1.1.345 package/icons folder
def fluent(n): return re.search(r'<path[^>]*\sd="([^"]+)"', open(ICONS+n+'_24_filled.svg').read()).group(1)
HDR='<?xml version="1.0" encoding="utf-8"?>\n'
def vec(name, w, h, body, note):
    open(OUT+name+'.xml','w').write(HDR+f'<!-- {note} -->\n<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="{w}dp" android:height="{h}dp" android:viewportWidth="{w}" android:viewportHeight="{h}">\n{body}</vector>\n')
def p(d, fill, alpha=None):
    a=f' android:fillAlpha="{alpha}"' if alpha is not None else ''
    return f'    <path android:fillColor="{fill}"{a} android:pathData="{d}" />\n'
def g(body, tx=0, ty=0, rot=0, sx=1, sy=1, px=0, py=0, clip=None):
    c=f'        <clip-path android:pathData="{clip}" />\n' if clip else ''
    return f'    <group android:translateX="{tx}" android:translateY="{ty}" android:rotation="{rot}" android:scaleX="{sx}" android:scaleY="{sy}" android:pivotX="{px}" android:pivotY="{py}">\n{c}{body}    </group>\n'

# ---------- eggs: 24 x 30 egg in its own box
EGG='M12,1C6.5,1 2.5,10 2.5,17.5C2.5,24 6.8,29 12,29C17.2,29 21.5,24 21.5,17.5C21.5,10 17.5,1 12,1Z'
def zig(y, amp, step=3):
    pts=[]; x=-2; up=True
    while x<=26: pts.append(f'{x},{y-amp if up else y+amp}'); x+=step; up=not up
    top=' L'.join(pts); bot=' L'.join(f'{q.split(",")[0]},{float(q.split(",")[1])+2.6}' for q in reversed(pts))
    return f'M{top} L{bot}Z'
def egg_body(kind):
    b=''
    if kind==0:   # pink, white zigzag band, yellow stripe
        b+=p(EGG,'#FFB3CF')+p('M-2,9h28v2.4h-28z','#FFE699')+p(zig(17,1.6),'#FFFFFF')+p('M-2,23h28v2h-28z','#E57FA8')
    elif kind==1: # sky blue, yellow stripes, white dots
        b+=p(EGG,'#9CCBFF')+p('M-2,11h28v3h-28z','#FFE699')+p('M-2,20h28v3h-28z','#FFE699')
        for (x,y) in [(7,6),(17,6),(12,17),(5,17),(19,17),(9,26),(15,26)]: b+=p(f'M{x-1.3},{y}a1.3,1.3 0 1,0 2.6,0a1.3,1.3 0 1,0 -2.6,0z','#FFFFFF')
    elif kind==2: # lilac, mint zigzag, pink dots
        b+=p(EGG,'#D7B8FF')+p(zig(12,1.8),'#B5F0CF')+p(zig(21,1.8),'#B5F0CF')
        for (x,y) in [(12,6),(6,16.5),(12,16.5),(18,16.5),(12,26)]: b+=p(f'M{x-1.4},{y}a1.4,1.4 0 1,0 2.8,0a1.4,1.4 0 1,0 -2.8,0z','#FF8FB8')
    else:         # butter yellow, pink band with white dots, mint stripe
        b+=p(EGG,'#FFE38A')+p('M-2,13h28v6h-28z','#FF9EC4')
        for x in [4,9,14,19]: b+=p(f'M{x-1.1},16a1.1,1.1 0 1,0 2.2,0a1.1,1.1 0 1,0 -2.2,0z','#FFFFFF')
        b+=p('M-2,23h28v2h-28z','#8EDDB5')+p('M-2,7h28v1.8h-28z','#8EDDB5')
    # soft highlight and shade
    b+=p('M7,6C5,9.5 4.2,13 4.4,16C5.5,12 7,9 9.5,6.8C8.8,6 7.8,5.6 7,6Z','#FFFFFF','0.45')
    b+=p('M21.5,17.5C21.5,24 17.2,29 12,29C15.5,27 18.5,23 19,17.5C19,13 18,9.5 16.5,6C19.5,9 21.5,13.5 21.5,17.5Z','#000000','0.12')
    return g(b, clip=EGG)
for k in range(4):
    vec(f'season_egg_{k+1}', 24, 30, egg_body(k), 'WX-6.12: a decorated Easter egg, drawn for this app (Fluent has none). Not tinted.')

# ---------- grass blades
def blades(seed, n, x0, x1, base, hmin, hmax, color, alpha=None):
    r=random.Random(seed); out=''
    for i in range(n):
        x=x0+(x1-x0)*i/(n-1)+r.uniform(-1.5,1.5); h=r.uniform(hmin,hmax); w=r.uniform(2.2,3.6); lean=r.uniform(-7,7)
        d=f'M{x-w/2:.1f},{base}C{x-w/2+lean*0.3:.1f},{base-h*0.5:.1f} {x+lean*0.7:.1f},{base-h*0.85:.1f} {x+lean:.1f},{base-h:.1f}C{x+lean*0.6+w*0.2:.1f},{base-h*0.6:.1f} {x+w/2:.1f},{base-h*0.4:.1f} {x+w/2:.1f},{base}Z'
        out+=p(d,color,alpha)
    return out

# ---------- spring scene 120 x 90
b=''
b+=p('M0,90L0,78C30,72 70,70 120,68L120,90Z','#3E9B4F')
b+=blades(1,34,2,118,86,14,34,'#2F8A44')
for (tx,ty,rot,k,s) in [(14,40,-16,2,1.45),(46,30,4,0,1.7),(78,40,18,1,1.45),(32,52,-6,3,1.25),(62,54,10,2,1.2)]:
    b+=g(egg_body(k), tx=tx, ty=ty, rot=rot, sx=s, sy=s, px=12, py=15)
b+=blades(2,32,0,120,90,10,24,'#7CCF5B')
b+=blades(3,20,0,120,90,6,16,'#A6E37A')
SPRING_BODY=b
vec('scene_spring',120,90,b,'WX-6.12 (Chris, 9 Nov): spring corner, decorated Easter eggs in young grass. Drawn for this app.')

# ---------- winter pine scene 120 x 110
def pine(cx, top, bottom, width, dark, mid, snow='#FFFFFF', tiers=4):
    out=''; th=(bottom-top)/tiers
    for i in range(tiers):
        y0=top+i*th*0.78; y1=y0+th*1.25; w=width*(0.35+0.65*(i+1)/tiers)/2
        tri=f'M{cx},{y0}Q{cx-w*0.35},{y0+(y1-y0)*0.55} {cx-w},{y1}L{cx+w},{y1}Q{cx+w*0.35},{y0+(y1-y0)*0.55} {cx},{y0}Z'
        out+=p(tri,mid)
        out+=p(f'M{cx},{y0}Q{cx+w*0.35},{y0+(y1-y0)*0.55} {cx+w},{y1}L{cx},{y1}Z',dark)
        # snow ridge with scalloped edge along the bottom of the tier
        n=5; seg=2*w/n; d=f'M{cx-w},{y1}'
        for k in range(n):
            xa=cx-w+seg*k; d+=f'Q{xa+seg/2:.1f},{y1-4.5:.1f} {xa+seg:.1f},{y1}'
        d+=f'L{cx+w*0.9:.1f},{y1-1.2}Q{cx},{y1-6} {cx-w*0.9:.1f},{y1-1.2}Z'
        out+=p(d,snow)
        # snow cap on the upper slope
        out+=p(f'M{cx},{y0}Q{cx-w*0.18},{y0+(y1-y0)*0.3} {cx-w*0.32},{y0+(y1-y0)*0.42}Q{cx},{y0+(y1-y0)*0.3} {cx+w*0.3},{y0+(y1-y0)*0.4}Q{cx+w*0.15},{y0+(y1-y0)*0.25} {cx},{y0}Z',snow,'0.9')
    out=p(f'M{cx-width*0.05},{bottom-2}h{width*0.1}v{8}h{-width*0.1}z','#4A3020')+out
    return out
b=''
b+=pine(48,38,96,44,'#1B4D3A','#24604A')
b+=pine(88,8,100,66,'#16432F','#1F5A40')
b+=p('M0,110L0,99C25,93 55,95 80,91C100,88 112,89 120,87L120,110Z','#DCEAF7')
b+=p('M0,110L0,103C30,98 60,101 90,97C105,95 114,96 120,95L120,110Z','#FFFFFF')
vec('scene_winter',120,110,b,'WX-6.12 (Chris, 9 Nov): winter corner, a snow-covered pine. Drawn for this app.')

# ---------- summer: cedar + maple, 130 x 110
b=''
# maple: trunk, branches, lobed canopy of circles
b+=p('M44,108L44,74Q40,66 32,62L34,60Q42,64 46,70L48,58L52,58L52,72Q56,64 64,60L66,62Q58,66 55,74L55,108Z','#5B3A24')
for (x,y,r,c) in [(30,58,16,'#4CAF50'),(64,56,17,'#4CAF50'),(48,40,20,'#66BB6A'),(34,44,14,'#7CC576'),(62,42,14,'#7CC576'),(48,58,15,'#5DAE5A'),(40,30,11,'#9BD98F'),(56,30,11,'#9BD98F'),(48,24,9,'#B9E8AE')]:
    b+=p(f'M{x-r},{y}a{r},{r} 0 1,0 {2*r},0a{r},{r} 0 1,0 {-2*r},0z',c)
# cedar: tall, drooping tiers
cx=100; b+=p(f'M{cx-2.5},100h5v10h-5z','#4A3020')
for i,(y,w) in enumerate([(14,6),(22,10),(31,14),(40,17),(50,21),(60,24),(71,27),(83,29),(96,30)]):
    col='#0B3D24' if i%2==0 else '#0F4A2C'
    b+=p(f'M{cx},{y-9}Q{cx-w*0.4},{y-2} {cx-w},{y+3}Q{cx-w*0.5},{y+1} {cx},{y+2}Q{cx+w*0.5},{y+1} {cx+w},{y+3}Q{cx+w*0.4},{y-2} {cx},{y-9}Z',col)
    b+=p(f'M{cx-w},{y+3}Q{cx-w*0.5},{y+1} {cx},{y+2}Q{cx+w*0.5},{y+1} {cx+w},{y+3}Q{cx+w*0.5},{y+0.2} {cx},{y+0.6}Q{cx-w*0.5},{y+0.2} {cx-w},{y+3}Z','#4FA36A')
b+=p('M0,110L0,100C40,96 90,95 130,92L130,110Z','#5DB55F')
b+=blades(4,34,0,130,110,5,13,'#8BD67A')
SUMMER_BODY=b
vec('scene_summer',130,110,b,'WX-6.12 (Chris, 9 Nov): summer corner, a green maple and cedar on grass. Drawn for this app.')

# ---------- maple leaf, 24 x 24, stem at the bottom
_r=[(12,0.8),(13.4,4.8),(15.6,3.6),(15.0,8.4),(18.2,5.8),(19.0,7.6),(22.6,6.8),(21.2,10.4),(23.2,11.6),(18.4,15.0),(19.2,16.8),(13.6,15.8),(12.7,16.4),(12.7,23.2)]
MAPLE='M'+' L'.join(f'{x},{y}' for x,y in _r)+' L11.3,23.2 '+' L'.join(f'{24-x:.1f},{y}' for x,y in reversed(_r[1:-1]))+'Z'
MAPLE_VEINS='M12,22.6L12,6M12,14L17.5,9.5M12,14L6.5,9.5M12,16L18,14.4M12,16L6,14.4'
def maple(color, vein='#000000'):
    return p(MAPLE,color)+f'    <path android:strokeColor="{vein}" android:strokeAlpha="0.25" android:strokeWidth="0.6" android:pathData="{MAPLE_VEINS}" />\n'
vec('season_maple', 24, 24, p(MAPLE,'#FFFFFF'), 'WX-6.13: a maple leaf drawn for this app (Chris, 9 Nov: maple, not Fluent leaves).')

# ---------- fall: leaf pile from Fluent leaves, 120 x 80
L=[fluent('leaf_one'),fluent('leaf_two'),fluent('leaf_three')]
cols=['#FFC24D','#B71C1C','#FFE2A8','#5A2A0E','#E8752A','#F2C14E','#8E1B1B','#FF9A3D']
r=random.Random(7); b=''
b+=p('M0,90L0,84C30,80 70,78 120,74L120,90Z','#5A2A0E','0.55')
spots=[(40,66,2.0),(62,60,2.2),(84,58,2.2),(104,64,2.0),(30,76,1.8),(52,74,2.1),(74,72,2.2),(96,76,2.1),(114,78,1.8),(42,84,1.9),(64,86,2.0),(86,86,2.0),(108,88,1.8),(72,50,1.7),(94,46,1.6)]
for i,(x,y,s) in enumerate(spots):
    b+=g(maple(cols[i%len(cols)]), tx=x-12, ty=y-12, rot=r.uniform(-60,60), sx=s, sy=s, px=12, py=12)
# two tumbling above
b+=g(maple('#FFE2A8'), tx=40-12, ty=22-12, rot=35, sx=1.6, sy=1.6, px=12, py=12)
b+=g(maple('#B71C1C'), tx=92-12, ty=14-12, rot=-25, sx=1.5, sy=1.5, px=12, py=12)
FALL_BODY=b
vec('scene_fall',120,90,b,'WX-6.13 (Chris, 9 Nov): fall corner, a pile of maple leaves. Drawn for this app.')

# ---------- snow-coated versions (Chris, 9 Nov: snow in spring, summer or fall)
def drift(w, h, top, color='#FFFFFF'):
    return p(f'M0,{h}L0,{top+4}C{w*0.25},{top-1} {w*0.55},{top+2} {w*0.8},{top-2}C{w*0.9},{top-3} {w*0.96},{top-2} {w},{top-3}L{w},{h}Z', color)
def caps(spots, color='#FFFFFF', alpha='0.95'):
    return ''.join(p(f'M{x-rx},{y}Q{x},{y-ry*2.4} {x+rx},{y}Q{x+rx*0.5},{y+ry*0.9} {x},{y+ry*0.5}Q{x-rx*0.5},{y+ry*0.9} {x-rx},{y}Z', color, alpha) for (x,y,rx,ry) in spots)
# spring: caps on the eggs, a drift over the ground
sp=SPRING_BODY+caps([(31,46,9,3.2),(66,37,10,3.6),(95,46,9,3.2),(47,59,7,2.6),(76,60,7,2.6)])+drift(120,90,82,'#F4F8FF')+drift(120,90,86)
vec('scene_spring_snow',120,90,sp,'WX-6.13: the spring corner with a light coat of snow. Drawn for this app.')
# summer: snow on the maple's crown and the cedar's tiers, a drift
su=SUMMER_BODY+caps([(48,8,9,3),(40,22,9,3),(56,22,9,3),(32,32,10,3),(64,32,10,3),(18,48,8,2.6),(80,44,8,2.6)])
su+=caps([(100,6,4,2),(100,14,7,2),(100,23,11,2.2),(100,32,14,2.4),(100,42,17,2.4),(100,52,20,2.6),(100,63,23,2.6),(100,75,25,2.6),(100,88,27,2.6)])
su+=drift(130,110,100,'#F4F8FF')+drift(130,110,104)
vec('scene_summer_snow',130,110,su,'WX-6.13: the summer corner with a light coat of snow. Drawn for this app.')
# fall: snow dusting the pile, a drift
fa=FALL_BODY+caps([(48,58,10,3),(68,52,12,3.4),(90,48,12,3.4),(108,54,10,3),(36,70,8,2.6),(60,66,10,3),(84,66,10,3),(104,68,9,2.6)])+drift(120,90,84,'#F4F8FF')+drift(120,90,88)
vec('scene_fall_snow',120,90,fa,'WX-6.13: the fall corner with a light coat of snow. Drawn for this app.')
print('ok')
