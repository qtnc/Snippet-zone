#include "global.h"
#include "win32api++.hpp"
extern "C" {
#include<lua/lua.h>
#include<lua/lauxlib.h>
#include<lua/lualib.h>
}
#include "UniversalSpeech.h"
#include<string>
#include "strings.hpp"
using namespace std;

#define luaL_checktable(L,n) luaL_checktype(L,n,LUA_TTABLE)
#define luaL_optboolean(L,n,b) (lua_isboolean(L,n)? lua_toboolean(L,n) : b)

extern bool reconnect;

void mudSend (const string&);
void luaMudSend (const string& line);
int mudConnect (const string&, int);
void mudDisconnect ();
void historyAdd (const tstring&, int);
bool playSound (const string& name, double vol, double pitch, double pan);
bool preloadSound (const string& name);
const tstring& getLang (const string&);
int addView (const tstring& name, int mask);
void luaMudSend (const string& line);

extern HWND win;
CRITICAL_SECTION cs;
lua_State* L = NULL;
int encoding = DEFAULT_CP;

static inline void lua_push (lua_State *L) {}
static inline void lua_push (lua_State* L, int n) { lua_pushinteger(L,n); }
static inline void lua_push (lua_State* L, bool n) { lua_pushboolean(L,n); }
static inline void lua_push (lua_State* L, nullptr_t n) { lua_pushnil(L); }
static inline void lua_push (lua_State* L, const string& s) { lua_pushlstring(L, s.data(), s.size()); }
static inline void lua_push (lua_State* L, const char* s) { lua_pushstring(L, s); }

template<class Arg, class... Tail>  inline void lua_push (lua_State* L, Arg arg, Tail... tailArgs) {
lua_push(L, arg);
lua_push(L, tailArgs...);
}

template<class... A> inline bool lua_callfunc (lua_State* L, const char* name, A... args) {
lua_settop(L,0);
lua_getfield(L, LUA_REGISTRYINDEX, "getbacktrace");
lua_getfield(L, LUA_GLOBALSINDEX, name);
if (!lua_isfunction(L,-1)) {
lua_settop(L,0);
return false;
}
lua_push(L, args...);
if (lua_pcall(L, sizeof...(args), LUA_MULTRET, -2-sizeof...(args))) {
historyAdd(snwprintf(4096, TEXT("LUA ERROR: %hs"), lua_tostring(L,-1)), 0);
return false;
}
return true;
}

template<class T> T lua_get (lua_State* L, int n) { throw 0; }
template<> int lua_get (lua_State* L, int n) { return lua_tointeger(L,n); }
template<> bool lua_get (lua_State* L, int n) { return lua_toboolean(L,n); }
template<> double lua_get (lua_State* L, int n) { return lua_tonumber(L,n); }
template<> string lua_get (lua_State* L, int n) { return lua_tostring(L,n); }


template<class T> T luaL_optfield (lua_State* L, int index, const char* name, const T& def) {
T re;
lua_getfield(L, index, name);
if (lua_isnoneornil(L,-1)) re=def;
else re = lua_get<T>(L,-1);
lua_pop(L,1);
return re;
}

static inline string luaL_optfield (lua_State* L, int index, const char* name, const char* def) { return luaL_optfield<string>(L, index, name, def); }

static int lua_getbacktrace (lua_State* l) {
lua_getglobal(l, "debug");
lua_getfield(l, -1, "traceback");
lua_pushvalue(l,1);
lua_pushinteger(l,2);
if (lua_pcall(l, 2, 1,0)) {
historyAdd(TEXT("LUA ERROR: Error in error handling !"), 0);
}
return 1;
}

void luaExecFile (const string& fn) {
EnterCriticalSection(&cs);
lua_getfield(L, LUA_REGISTRYINDEX, "getbacktrace");
if (luaL_loadfile(L, fn.c_str()) ||  lua_pcall(L, 0, 0, -2)) {
historyAdd(snwprintf(4096, TEXT("Lua error: %hs"), lua_tostring(L,-1)), 0);
}
LeaveCriticalSection(&cs);
}

static void luaEval (const string& str) {
EnterCriticalSection(&cs);
lua_getfield(L, LUA_REGISTRYINDEX, "getbacktrace");
if (luaL_loadstring(L, str.c_str()) ||  lua_pcall(L, 0, 0, -2)) {
historyAdd(snwprintf(4096, TEXT("Lua error: %hs"), lua_tostring(L,-1)), 0);
}
LeaveCriticalSection(&cs);
}

static int luaDisconnect (lua_State *L) {
mudDisconnect();
return 0;
}

static int luaConnect (lua_State* L) {
string host = luaL_checkstring(L,1);
int port = luaL_checkint(L,2);
int newEncoding = luaL_optint(L, 3, DEFAULT_CP);
if (host.empty()) return 0;
encoding=newEncoding;
int code = mudConnect(host, port);
lua_push(L,code==0, code);
return 2;
}

static int luaHistoryAdd (lua_State* L) {
string msg = luaL_checkstring(L,1);
int channel = luaL_optint(L,2,0);
historyAdd(toTString(msg, encoding), channel);
return 0;
}

static int luaSpeak (lua_State* L) {
string msg = luaL_checkstring(L,1);
bool interrupt = luaL_optboolean(L,2,false);
speechSay(toTString(msg,encoding).c_str(), interrupt);
return 0;
}

static int luaTypeCmd (lua_State* L) {
luaMudSend(luaL_checkstring(L,1));
return 0;
}

static int luaWait (lua_State* L) {
int ms = luaL_checkint(L,1);
LeaveCriticalSection(&cs);
Sleep(ms);
EnterCriticalSection(&cs);
return 0;
}

static int luaPlaySound (lua_State* L) {
string name = luaL_checkstring(L,1);
double vol = luaL_optnumber(L, 2, 1.0);
double pitch = luaL_optnumber(L, 3, 0.0);
double pan = luaL_optnumber(L, 4, 0.0);
if (!name.empty()) playSound(name, vol, pitch, pan);
return 0;
}

static int luaPreloadSound (lua_State* L) {
preloadSound(luaL_checkstring(L,1));
return 0;
}

static int luaGetLang (lua_State* L) {
lua_push(L, toString(getLang(luaL_checkstring(L,1)), encoding));
return 1;
}

static int luaSetWinTitle (lua_State* L) {
tstring str = toTString(luaL_checkstring(L,1), encoding) + TEXT(" - ") + getLang("winTitle0");
SetWindowText(win, str);
return 0;
}

static int luaRegView (lua_State* L) {
string name = luaL_checkstring(L,1);
int mask = luaL_checkint(L,2);
lua_push(addView(toTString(name, encoding), mask));
return 1;
}

static DWORD luaMudSendProxy (string* strptr) {
luaMudSend(strptr->substr(1));
delete strptr;
return 0;
}

static int luaSetConfig (lua_State* L) {
string key = lua_tostring(L,1);
if (key=="reconnect") reconnect = lua_isboolean(L,2)&&lua_toboolean(L,2);
return 0;
}

void initLua (void) {
InitializeCriticalSection(&cs);
EnterCriticalSection(&cs);
L = lua_open();
luaL_openlibs(L);

lua_pushcfunction(L, lua_getbacktrace);
lua_setfield(L, LUA_REGISTRYINDEX, "getbacktrace");
lua_register(L, "connect", luaConnect);
lua_register(L, "disconnect", luaDisconnect);
lua_register(L, "log", luaHistoryAdd);
lua_register(L, "say", luaSpeak);
lua_register(L, "send", luaTypeCmd);
lua_register(L, "wait", luaWait);
lua_register(L, "playSound", luaPlaySound);
lua_register(L, "preloadSound", luaPreloadSound);
lua_register(L, "title", luaSetWinTitle);
lua_register(L, "lang", luaGetLang);
lua_register(L, "addView", luaRegView);
lua_register(L, "setConfig", luaSetConfig);

luaExecFile("default.lua");
LeaveCriticalSection(&cs);
}

void mudRecvLine (const string& line) {
if (line.empty()) return;
int channel = 0;
EnterCriticalSection(&cs);
if (lua_callfunc(L, "onreceive", line, channel)) {
if (lua_type(L,-1)==LUA_TNUMBER) { channel=lua_tointeger(L,-1); line=lua_tostring(L,-2); }
else if (lua_type(L,-1)==LUA_TSTRING) line=lua_tostring(L,-1);
else if (lua_type(L,-1)==LUA_TBOOLEAN && !lua_toboolean(L,-1)) line="";
}
LeaveCriticalSection(&cs);
if (!line.empty()) {
tstring sEnc = toTString(line, encoding);
historyAdd(sEnc, channel);
}}

void luaMudSend (const string& line0) {
string line = trim_copy(line0);
if (line.empty()) return;
if (starts_with(line, "&")) { NewThread(luaMudSendProxy, new string(line)); }
else for (auto str: split(line, ";\n\r")) {
trim(str);
if (starts_with(str,"$")) { luaEval(str.substr(1)); continue; }
EnterCriticalSection(&cs);
if (lua_callfunc(L, "onsend", str)) {
if (lua_isboolean(L,-1) && !lua_toboolean(L,-1)) str="";
else if (lua_isstring(L,-1)) str = lua_tostring(L,-1);
}
LeaveCriticalSection(&cs);
if (!str.empty()) mudSend(str);
}}

void luaMudSend (const tstring& sEnc) {
luaMudSend(toString(sEnc, encoding));
}

void luaRunMacro (int key) {
EnterCriticalSection(&cs);
lua_callfunc(L, "onkeypress", key);
LeaveCriticalSection(&cs);
}

void luaMudConnected() {
EnterCriticalSection(&cs);
lua_callfunc(L, "onconnect");
LeaveCriticalSection(&cs);
}

void luaMudDisconnected() {
EnterCriticalSection(&cs);
lua_callfunc(L, "ondisconnect");
LeaveCriticalSection(&cs);
}

void luaConnectError (int errnum) {
EnterCriticalSection(&cs);
lua_callfunc(L, "onconnecterror", errnum);
LeaveCriticalSection(&cs);
}
