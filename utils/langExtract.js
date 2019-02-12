// https://r12a.github.io/app-charuse/
function rawLanguage (lang) {
    console.log(lang)
    out = ''
    var charList
    var stats = 'Non-ASCII character count: &nbsp; '
    var total = 0
    var rawOut = "lang:"+langs[lang].name+':'+lang+'\n'
    out += '<tr><th>Name</th><td class="large">'+langs[lang].name+' ['+lang+'] <a href="https://en.wikipedia.org/w/index.php?search='+langs[lang].name+'%20language" target="_blank"><img src="wikipedia.png" alt="Search Wikipedia for this language" title="Search Wikipedia for this language" style="float:right;" /></a></td></tr>'
    if (langs[lang].letter) {
        charList = [...langs[lang].letter]
        charList.sort()
        stats += charList.length + ' letters'
        total += charList.length
        rawOut += "  letters: "
        out += '<tr><th>Letters</th><td class="large">'
        for (j=0;j<charList.length;j++) { 
            cp = charList[j].codePointAt(0).toString(16).toUpperCase()
            while (cp.length<4) cp = '0'+cp
            name = 'U+'+cp+' '+charData[charList[j]]
            rawOut += cp + ' '
            out += '<span title="'+name+'">'+charList[j]+'</span> ' 
            }
        rawOut += '\n'
        out += '<a href="/uniview?charlist='+langs[lang].letter+'" target="_blank"><img src="univ.png" alt="Show characters in UniView." title="Show characters in UniView." class="ulink"/></a></td></tr>'
        }
    if (langs[lang].mark) {
        charList = [...langs[lang].mark]
        if (total > 0) stats += ', '
        stats += charList.length + ' marks'
        total += charList.length
        out += '<tr><th>Marks</th><td class="large">'
        rawOut += "  marks: "
        for (j=0;j<charList.length;j++) { 
            cp = charList[j].codePointAt(0).toString(16).toUpperCase()
            while (cp.length<4) cp = '0'+cp
            name = 'U+'+cp+' '+charData[charList[j]]
            rawOut += cp + ' '
            out += '<span title="'+name+'">'+'\u00A0'+charList[j]+'</span> ' 
            }
        rawOut += '\n'
        out += '<a href="/uniview?charlist='+langs[lang].mark+'" target="_blank"><img src="univ.png" alt="Show characters in UniView."  title="Show characters in UniView." class="ulink"/></a></td></tr>'
        }
    if (langs[lang].punctuation) {
        charList = [...langs[lang].punctuation]
        if (total > 0) stats += ', '
        stats += charList.length + ' punctuation'
        total += charList.length
        out += '<tr><th>Punctuation</th><td class="large">'
        rawOut += "  punct: "
        for (j=0;j<charList.length;j++)  { 
            cp = charList[j].codePointAt(0).toString(16).toUpperCase()
            while (cp.length<4) cp = '0'+cp
            name = 'U+'+cp+' '+charData[charList[j]]
            rawOut += cp + ' '
            out += '<span title="'+name+'">'+'\u00A0'+charList[j]+'</span> ' 
            }
        rawOut += '\n'
        out += '<a href="/uniview?charlist='+langs[lang].punctuation+'" target="_blank"><img src="univ.png" alt="Show characters in UniView." title="Show characters in UniView." class="ulink"/></a></td></tr>'
        }
    if (langs[lang].number) {
        charList = [...langs[lang].number]
        if (total > 0) stats += ', '
        stats += charList.length + ' numbers'
        total += charList.length
        out += '<tr><th>Numbers</th><td class="large">'
        rawOut += "  nums: "
        for (j=0;j<charList.length;j++)  { 
            cp = charList[j].codePointAt(0).toString(16).toUpperCase()
            while (cp.length<4) cp = '0'+cp
            name = 'U+'+cp+' '+charData[charList[j]]
            rawOut += cp + ' '
            out += '<span title="'+name+'">'+'\u00A0'+charList[j]+'</span> ' 
            }
        rawOut += '\n'
        out += '<a href="/uniview?charlist='+langs[lang].number+'" target="_blank"><img src="univ.png" alt="Show characters in UniView." title="Show characters in UniView." class="ulink"/></a></td></tr>'
        }
    if (langs[lang].symbol) {
        charList = [...langs[lang].symbol]
        if (total > 0) stats += ', '
        stats += charList.length + ' symbols'
        total += charList.length
        out += '<tr><th>Symbols</th><td class="large">'
        rawOut += "  symbols: "
        for (j=0;j<charList.length;j++)  { 
            cp = charList[j].codePointAt(0).toString(16).toUpperCase()
            while (cp.length<4) cp = '0'+cp
            name = 'U+'+cp+' '+charData[charList[j]]
            rawOut += cp + ' '
            out += '<span title="'+name+'">'+'\u00A0'+charList[j]+'</span> ' 
            }
        rawOut += '\n'
        out += '<a href="/uniview?charlist='+langs[lang].symbol+'" target="_blank"><img src="univ.png" alt="Show characters in UniView." title="Show characters in UniView." class="ulink"/></a></td></tr>'
        }
    if (langs[lang].other) {
        charList = [...langs[lang].other]
        if (total > 0) stats += ', '
        stats += charList.length + ' other'
        total += charList.length
        out += '<tr><th>Other</th><td>'
        //out += langs[lang].other
        rawOut += "  other: "
        for (j=0;j<charList.length;j++)  { 
            cp = charList[j].codePointAt(0).toString(16).toUpperCase()
            while (cp.length<4) cp = '0'+cp
            name = 'U+'+cp+' '+charData[charList[j]]
            rawOut += cp + ' '
            out += '<span title="'+name+'">U+'+cp+'</span> ' 
            }
        rawOut += '\n'
        //for (j=0;j<charList.length;j++) out += charList[j].codepointAt(0)+' '
        out += '<a href="/uniview?charlist='+langs[lang].other+'" target="_blank"><img src="univ.png" alt="Show characters in UniView." title="Show characters in UniView." class="ulink"/></a></td></tr>'
        }
    if (langs[lang].aux) {
        charList = [...langs[lang].aux]
        if (total > 0) stats += ', '
        stats += charList.length + ' infrequent'
        total += charList.length
        out += '<tr><th>Infrequent</th><td class="large">'
        rawOut += "  infreq: "
        for (j=0;j<charList.length;j++) { 
            cp = charList[j].codePointAt(0).toString(16).toUpperCase()
            while (cp.length<4) cp = '0'+cp
            name = 'U+'+cp+' '+charData[charList[j]]
            rawOut += cp + ' '
            out += '<span title="'+name+'">'+'\u00A0'+charList[j]+'</span> ' 
            }
        rawOut += '\n'
        out += '<a href="/uniview?charlist='+langs[lang].aux+'" target="_blank"><img src="univ.png" alt="Show characters in UniView."  title="Show characters in UniView." class="ulink"/></a></td></tr>'
        }
    stats += ' : &nbsp; total ' + total
    if (!langs[lang].letter && !langs[lang].mark && !langs[lang].punctuation && !langs[lang].number && !langs[lang].symbol && !langs[lang].other) {
        out += '<tr><th></th><td class="large">ASCII only</td></tr>'
        }
    else out += '<tr><th></th><td style="border:0; font-size: 70%; font-style: italic; line-height: 1; color:gray;">'+stats+'</td></tr>'
    out += '<tr><th>Source</th><td>'+langs[lang].source+'</td></tr>'
    switch (langs[lang].region) {
        case 'afr': region = 'Africa';break
        case 'oce': region = 'Oceania';break
        case 'eur': region = 'Europe';break
        case 'nam': region = 'Northern America';break
        case 'cam': region = 'Central America';break
        case 'sam': region = 'South America';break
        case 'wasia': region = 'Western Asia';break
        case 'casia': region = 'Central Asia';break
        case 'nasia': region = 'Northern Asia';break
        case 'sasia': region = 'South Asia';break
        case 'seasia': region = 'South East Asia';break
        case 'easia': region = 'East Asia';break
        case 'carib': region = 'Caribbean';break
        }
    out += '<tr><th>Region</th><td>'+region+'</td></tr>'
    if (langs[lang].speakers === '?') var speakers = 'Not known'
    else { 
        speakers = langs[lang].speakers.replace(/~/,'')
        speakers = parseInt(speakers).toLocaleString('en')
        }
    rawOut += "  speakers: "+speakers+"\n"
    //speakers = speakers.replace(/\?/,'0')
    out += '<tr><th>Native speakers</th><td>'+speakers+'</td></tr>'
    if (langs[lang].notes) out += '<tr><th>Notes</th><td style="font-size:90%">'+langs[lang].notes+'</td></tr>'
    return rawOut
    }
 
var langKeys = Object.keys(langs)
var allLangs = ""

for(i = 0; i < langKeys.length; i++) {
    allLangs += rawLanguage(langKeys[i])
}

allLangs
