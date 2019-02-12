#!/usr/bin/env python3

import sys
from subprocess import Popen, PIPE, STDOUT

ranges = sys.argv[1]
splitRanges = [int(s, 16) for s in ranges.split('-')]

points = range(0, 0)

if len(splitRanges) == 1:
    print("Printing U+%04X" % splitRanges[0])
    points = range(splitRanges[0], splitRanges[0]+1)
if len(splitRanges) == 2:
    print("Printing range U+%04X - U+%04X" % (splitRanges[0], splitRanges[1]))
    points = range(splitRanges[0], splitRanges[1]+1)

string = ''.join([chr(x) for x in points])
print(string)

p = Popen(['pbcopy'], stdout=PIPE, stdin=PIPE, stderr=PIPE)
stdout_data = p.communicate(input=bytes(string, 'utf-8'))[0]
