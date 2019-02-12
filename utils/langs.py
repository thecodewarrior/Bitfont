#!/usr/bin/env python3
import sys

lines = []
with open('langs.txt', 'r') as f:
    lines = [x.strip() for x in f.readlines()]

languages = []
current = None

def newLang(line):
    global current, languages
    if current != None:
        languages.append(current)
    split = line.split(':', 1)
    current = {
            'name': split[0],
            'lang': split[1], 
            'speakers': 0,
            'letters': [],
            'marks': [],
            'punct': [],
            'nums': [],
            'symbols': [],
            'other': [],
            'infreq': []
            }

def try_parse_int(s, base=10, val=None):
  try:
    return int(s, base)
  except ValueError:
    return val

# http://code.activestate.com/recipes/496682-make-ranges-of-contiguous-numbers-from-a-list-of-i/
def list2range(lst):
    '''make iterator of ranges of contiguous numbers from a list of integers'''

    tmplst = lst[:]
    tmplst.sort()
    start = tmplst[0]

    currentrange = [start, start]

    for item in tmplst[1:]:
        if currentrange[1]+1 == item:
            # contiguous
            currentrange[1] += 1
        else:
            # new range start
            yield tuple(currentrange)
            currentrange = [item, item]

    # last range
    yield tuple(currentrange)

i = 0
try:
    for line in lines:
        i += 1
        split = line.split(':', 1)
        if split[0] == 'lang':
            newLang(split[1])
        elif split[0] == 'speakers':
            current['speakers'] = try_parse_int(split[1].replace(',', ''), 10, current['speakers'])
        else:
            current[split[0]] = [int(it, 16) for it in split[1].strip().split(' ')]
except Exception:
    print("error on line " + str(i))
    raise
languages.append(current)
langMap = {}

for lang in languages:
    lang['all'] = sorted(set(lang['letters'] + lang['marks'] + lang['punct'] + lang['nums'] + lang['symbols'] + lang['other'] + lang['infreq']))
    langMap[lang['lang']] = lang


bySize = sorted(languages, key=lambda it: it['speakers'], reverse=True)
# for i in range(0, 1):
#     lang = bySize[i]
#     print('%s (%d): %s' % (lang['lang'], lang['speakers'], ' '.join(['(%04X %04X)' % it for it in list2range(lang['all'])])))

print('\n'.join(['%s (%s): %d' % (it['lang'], it['name'], it['speakers']) for it in bySize]))

allLangs = []
allPoints = []
for l in sys.argv[1:]:
    lang = langMap[l]
    allLangs.append(lang['lang'] + ": " + lang['name'])
    allPoints += lang['all']

print(''.join(['> %s\n' % it for it in allLangs]))
print(' '.join([(('%04X' % it[0]) if (it[0] == it[1]) else ('(%04X %04X)' % it)) for it in list2range(sorted(set(allPoints)))]))
