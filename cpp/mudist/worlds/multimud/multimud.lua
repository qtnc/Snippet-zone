title "Multimud"

connect {
host= 'multimud.info', 
port=6022 
}

macros {
Alt_S = listeSorties,
Alt_F = listeObjets,
Alt_C = listeCorps,
Alt_D = listePersos,
Alt_T = listeConteneurs,
F1 = 'cast soin',
F5 = 'drink rouge',
Ctrl_A = "get * piece from corps; get * piece from 2.corps; get * piece from 3.corps; get * piece from 4.corps; get * potion from corps; get * potion from 2.corps; get * potion from 3.corps; get * potion from 4.corps",
numpad5 = 'look',
numpad8 = 'nord',
numpad2 = 'sud',
numpad4 = 'ouest',
numpad6 = 'est',
numpad1 = 'sud-ouest',
numpad3 = 'sud-est',
numpad7 = 'nord-ouest',
numpad9 = 'nord-est',
ctrl_numpad8 = 'haut',
ctrl_numpad2 = 'bas',
ctrl_numpad5 = 'porte'
}

channel '1' {
"^.+ crie.*:"
}

channel '2' {
"^.+ dit.*:",
}

channel '3' {
"^.+ vous dit.*:",
"^.+ vous répond.*:"
}

channel '4' {
'^%*Groupe%*'
}

channel '5' {
'^%*Clan%*'
}

channel '6' {
'^~Navire~'
}

view 'Discussions' '254'
view 'shout' '2'
view 'say' '4'
view 'tell' '8'
view 'groupe' '16'
view 'clan' '32'
view 'navire' '64'

trigger "^.+ parvient a désarmer Adrael" "wear naginata"

gag "vous dit: tout de suite, mon maître"

sound "^.+ vous dit.*:" "chat"
sound "^.+ vous répond.*:" "chat"
sound "^Vous gagnez un NIVEAU" "login"
sound "^Le soleil se couche" "hulotte"
sound "^Le soleil se lève" "ambbird3"
sound "^Vous recevez %d+ xp" "agony"
sound "^Vous parvenez a désarmer" "click"
sound "^Commande inconnue" "rebond"
