local aliases = {
}

local inputs = {
}

local macros = {
}

local keys = {
ctrl=512, control=512,
shift=1024, alt=2048, 
a=65, b=66, c=67, d=68, e=69, f=70, g=71, h=72, i=73,
j=74, k=75, l=76, m=77, n=78, o=79, p=80, q=81,
r=82, s=83, t=84, u=85, v=86, w=87, x=88, y=89, z=90,
num0=48, num1=49, num2=50, num3=51, num4=52, num5=53, num6=54, num7=55, num8=56, num9=57,
numpad0=96, numpad1=97, numpad2=98, numpad3=99, numpad4=100, numpad5=101, numpad6=102, numpad7=103, numpad8=104, numpad9=105,
numpadstar=106, numpadmultiply=106, numpadadd=107, numpadplus=107, numpadminus=109, numpaddot=110, numpaddecimal=110, numpadslash=111, numpaddivide=111,
f1=112, f2=113, f3=114, f4=115, f5=116, f6=117, f7=118, f8=119, f9=120, f10=121, f11=122, f12=123,
f13=124, f14=125, f15=126, f16=127, f17=128, f18=129, f19=130, f20=131, f21=132, f22=133, f23=134, f24=135
}

local soundVolume = 0.4
local streamVolume = 0.3

local function match2 (a, ...)
if a then return {a,...}
else return nil 
end end

local function addInputFilter (t)
return table.insert(inputs, t)
end

local function processTrigger (self, input, match)
send(input.line:gsub(self.trigger, self.send))
end

local function processSound (self)
if type(self.sound)=='table' then play(self.sound[1+math.floor(math.random()*#self.sound)]) 
else play(self.sound)
end end

local function processChannel (self, input)
input.channel = self.channel
end

local function processFilter (self, input)
input.line = input.line:gsub(self.trigger, self.filter)
end

local function processGag (self, input) 
input.line=''
return false
end

function eval (s, ...)
return assert(loadstring(s, 'eval'))(...)
end

do
local oldcon = connect
function connect (host, port, encoding)
if type(host)=='table' then
encoding = host.encoding
port = host.port
host = host.host
end
return oldcon(host, port, encoding)
end end

function play (name, vol, pitch, pan)
if type(name)=='table' then
pitch = name.pitch
pan = name.pan
vol = name.volume
name = name.name or name[1]
end
return playSound(name, (vol or 1) * soundVolume, pitch or 0, pan or 0)
end

function stream (name, vol, pitch, pan)
if type(name)=='table' then
pitch = name.pitch
pan = name.pan
vol = name.volume
name = name.name or name[1]
end
return playStream(name, (vol or 1) * streamVolume, pitch or 0, pan or 0)
end

function view (name, mask)
if mask then addView(lang(name), tonumber(mask))
else return function(m) view(name,m) end
end end

function alias (_alias, replacement)
if replacement then
if type(replacement)=='string' and replacement:match('[;\r\n]') then table.insert(aliases, {'^'.._alias, function() send(replacement) return '', false end }) 
else table.insert(aliases, {'^'.._alias, replacement}) end
else return function(repl) alias(_alias, repl) end
end end

function trigger (trig, action)
if action==nil then  return function(act) trigger(trig, act) end 
elseif type(action)=='string' or type(action)=='table' then return addInputFilter{trigger=trig, func=processTrigger, send=action}
elseif type(action)=='number' then return addInputFilter{trigger=trig, func=processChannel, channel=action}
elseif type(action)=='boolean' and not action then return addInputFilter{trigger=trig, func=processGag}
elseif type(action)=='function' then return addInputFilter{trigger=trig, func=action}
else return error('Invalid argument: ' .. action)
end end

function sound (trig, action)
if action then  return addInputFilter{trigger=trig, func=processSound, sound=action}
else return function(act) sound(trig,act) end 
end end

function filter (trig, action)
if action then  return addInputFilter{trigger=trig, func=processFilter, filter=action}
else return function(act) filter(trig,act) end 
end end

function channel (chan, trig)
if not trig then return function(x) channel(chan,x) end
elseif type(trig)=='table' then for _,item in ipairs(trig) do channel(chan, item) end
else return addInputFilter{trigger=trig, func=processChannel, channel=chan+0}
end end

function gag (trig)
if type(trig)=='table' then for _,item in ipairs(trig) do gag(item) end
else return addInputFilter{trigger=trig, func=processGag}
end end

function macro (key, action)
if type(key)=='table' then  for k,v in pairs(key) do macro(k,v) end
elseif not action then return function(act) macro(key,act) end
else
local code = 0
for k in key:gmatch('%w+') do
k = k:lower()
if keys[k] then code = code + keys[k]
end end
macros[code]=action
end end

function sendLastLine () return lastLine end

_G.macros = macro
_G.gags = gag

function processAlias (line)
for _,item in ipairs(aliases) do
local cont = true
local alias, replacement = unpack(item)
local m = match2(line:match(alias))
if m then 
if type(replacement)=='string' or type(replacement)=='table' then 
line = line:gsub(alias, replacement)
elseif type(replacement)=='function' then 
local newLine, cont  = replacement(alias, m, line)
line = newLine or line
if newLine==false then line=false break end
if cont==false then break end
end end end
return line
end

function processMudInputs  (t)
for _,item in ipairs(inputs) do
local m = match2(t.line:match(item.trigger))
if m then 
if false==item:func(t,m) then break end
end end 
return t.line, t.channel
end

function onreceive (line, channel)
return processMudInputs{line=line, channel=channel}
end

function onsend (line)
return processAlias(line)
end

function onkeypress (key)
local action = macros[key]
if action then
if type(action)=='string' then send(action)
elseif type(action)=='function' then action(key)
end end end



