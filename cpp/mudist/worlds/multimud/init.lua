for i = 6000, 6010 do preloadSound(tostring(i)) end
for i = 6100, 6110 do preloadSound(tostring(i)) end
for _,i in ipairs{'object', 'body', 'somebody', 'container'} do preloadSound(i) end

local gvie, gvie10, gviemax, gmag, gmagmax, gmou, gmoumax
local ignoreItem, elems = false, {}

trigger('^Vie: (%d+)/(%d+)%s+Mag: (%d+)/(%d+)%s+Mou: (%d+)/(%d+)%s+>', function(self, input, match)
ignoreItem=true
local vie, viemax, mag, magmax, mou, moumax = unpack(match)
if vie or viemax or mag or magmax or mou or moumax then
local vie10 = math.floor(10 * vie/viemax)
if vie10~=gvie10 then play(6100+vie10) end
if vie~=gvie then log(string.format('Vie: %d/%d', vie, viemax)) end
if mag~=gmag then log(string.format('Mag: %d/%d', mag, magmax)) end
if mou~=gmou and mou/moumax<0.5 then log(string.format('Mou: %d/%d', mou, moumax)) end
gvie, gvie10, gviemax, gmag, gmagmax, gmou, gmoumax  = vie, vie10, viemax, mag, magmax, mou, moumax 
end 
input.line = input.line:sub(input.line:match('>%s*()'))
end)--trigger

trigger('Adv%-%[(%-?%d+)%%%]', function(self, input, match)
ignoreItem=true
local adv = unpack(match)
if adv then
adv = math.max(0, math.ceil(adv/10))
play(6000+adv)
end
input.line = input.line:sub(input.line:match('>%s*()'))
end)--trigger

trigger('^(%d+)/(%d+)$', function(self, input, match)
local val, max = unpack(match)
local val10 = math.floor(10*val/max)
play(6100+val10)
end)--trigger

trigger('^([%-%+%*%~%>%%])%s+(.+)$', function(self, input, match)
local kind, str = unpack(match)
if not ignoreItem and kind=='*' then 
table.insert(elems.persos, str)
play('somebody')
elseif not ignoreItem and kind=='+' then 
table.insert(elems.corps, str)
play('body')
elseif not ignoreItem and kind=='-' then 
table.insert(elems.objets, str) 
play('object')
elseif not ignoreItem and kind=='%' then 
table.insert(elems.conteneurs, str)
play('container')
elseif kind=='>' then 
ignoreItem=false
elems.sorties, elems.objets, elems.corps, elems.persos, elems.conteneurs  = {}, {}, {}, {}, {}
table.insert(elems.sorties, str)
end
end)--trigger

local function listing (x) return function() 
if not elems[x] or #elems[x]<=0 then say('Aucun '..x,true) 
else log(table.concat(elems[x], '\r\n')) 
end end end
listeSorties, listeObjets, listeCorps, listePersos, listeConteneurs = listing('sorties'), listing('objets'), listing('corps'), listing('persos'), listing('conteneurs')

local accents = {}
for line in io.lines('accents.txt') do
local old, new = line:match('^%s*(%S+)%s+(%S+)[%s%z]*$')
if old and new then  accents[old]=new end
end

local function processAccentWord (word)
return accents[word:lower()]
end

local function processAccents (line)
return line:gsub('[%w%-]+', processAccentWord)
end

function onreceive (line, channel)
return processMudInputs{line=processAccents(line), channel=channel}
end

alias('quit$', function() setConfig('reconnect', false) return 'quit' end)

dofile('worlds/multimud/multimud.lua')