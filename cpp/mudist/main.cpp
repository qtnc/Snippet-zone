#include "global.h"
#include<richedit.h>
#include<prsht.h>
#include "win32api++.hpp"
#include "dialogs.h"
#include "Socket.hpp"
#include "UniversalSpeech.h"
#include "Resource.hpp"
#include "IniFile.hpp"
#include "bass.h"
#include<functional>
#include<cstdio>
#include<cstdlib>
#include<dirent.h>
#include<cmath>
#include<ctime>
#include<csignal>
#include<clocale>
#include<cctype>
#include<iostream>
#include<fstream>
#include<sstream>
#include<string>
#include<vector>
#include<array>
#include<list>
#include<deque>
#include<map>
#include<unordered_map>
#include "strings.hpp"
using namespace std;

struct HistoryEntry {
int channel;
tstring value;
HistoryEntry (const tstring& v, int c) : value(v), channel(c) {}
};

struct HistoryView {
int channels;
tstring name;
HistoryView (int c, const tstring& n) : channels(c), name(n) {}
};

HINSTANCE hInst;
HWND win = 0, edit = 0, chat=0;
HFONT font = 0;
RECT winRect;
HANDLE workerReady=NULL, workerBusy=NULL;
bool shiftDown=false, altDown=false, ctrlDown=false;
bool active = false, inDialogBox = false, speakAll = false, historySpeakOff = false, usingJfw=false;
unordered_map<string,tstring>langmap;
unordered_map<string,DWORD> sounds;
unordered_map<string,DWORD> streams;
vector<HistoryEntry> history;
vector<HistoryView> views;
vector<tstring> chatHistory;
//function<void(void)>* workerFunc = NULL;
int histView, histPos, chatpos;
//char keymap[256] = {0};
//unsigned long long clientId = 0, clientId2=0; char clientId3[20]= "noname";
//unsigned long long screenReaderId = 0;
string APPDIR, USERDIR;


//extern "C" {
LRESULT CALLBACK SendMessageInEDT (HWND hwnd, UINT msg, WPARAM wp, LPARAM lp) ;
LRESULT CALLBACK winproc (HWND,UINT,WPARAM,LPARAM);
LRESULT CALLBACK histproc (HWND,UINT,WPARAM,LPARAM,INT_PTR,DWORD) ;
LRESULT CALLBACK chatproc (HWND,UINT,WPARAM,LPARAM,INT_PTR,DWORD) ;
bool EnsureDirsExist (const string& startdir, const string& file);
int GetErrorText (int errcode, LPTSTR buf, DWORD maxlen) ;
void FillLogFont (LOGFONT& lf, HWND hwnd, const tstring& name, int size); 
unsigned long long fileTimesMs (const char* fn, int r) ;
unsigned long long getLastModified (LPCTSTR);
void historyChangeView (int n, bool absolute) ;
void historyMove (int n);
void historyCopyView (void) ;
void historyClear (void) ;
void historySave (void);
void historySave (const tstring& file);
bool chatHistoryMove (int) ;
bool loadLang (bool reload = false) ;
const tstring& getLang (const string&) ;
void historyAdd (const tstring&, int) ;
int setSoundVolume (int, bool) ;
int setStreamsVolume (int, bool) ;
void stopAllStreams (void) ;
void stopAllSounds (void) ;
void ResizeAllControls () ;
unsigned long long getLastModified (LPCTSTR fn) ;
void initLua (void);
void luaExecFile (const string&);
void luaRunMacro (int key);
void luaMudSend (const tstring&);

void SingBeep (int note, int octave, int duration) {
Beep(440*pow(2, (3+note + octave*12)/12.0), duration);
}

void crash (int n) {
SingBeep(7, 1, 400);
SingBeep(4, 1, 200);
SingBeep(9, 1, 200);
SingBeep(7, 1, 400);
SingBeep(4, 1, 400);
SingBeep(2, 1, 150);
Sleep(50);
SingBeep(2, 1, 150);
Sleep(50);
SingBeep(7, 0, 150);
Sleep(50);
SingBeep(7, 0, 150);
Sleep(50);
SingBeep(0, 1, 600);
MessageBox(win, getLang("crash").c_str(), getLang("crash2").c_str(), MB_OK | MB_ICONERROR);
exit(1);
}

void terminate2 (void) {
crash(0);
}

int initBass (void) {
BASS_SetConfig(BASS_CONFIG_DEV_DEFAULT, TRUE);
if (!BASS_Init(-1, 44100, 0x0, win, NULL)) return BASS_ErrorGetCode();
BASS_SetConfig(BASS_CONFIG_UPDATEPERIOD, 50);
BASS_SetConfig(BASS_CONFIG_BUFFER, 150);
BASS_SetConfig(BASS_CONFIG_NET_PLAYLIST, 1);
BASS_SetConfig(BASS_CONFIG_NET_BUFFER, 3000);
BASS_SetConfig(BASS_CONFIG_NET_READTIMEOUT, 5000);
BASS_SetConfig(BASS_CONFIG_NET_TIMEOUT, 2000);
BASS_SetConfig(BASS_CONFIG_FLOATDSP, TRUE);
BASS_SetConfig(BASS_CONFIG_MUSIC_VIRTUAL, 256);
return 0;
}

bool initSpeech (void) {
speechSetValue(SP_ENABLE_NATIVE_SPEECH, false);
return true;
}

static inline string ProtectNull (const char* str) {
if (str) return str;
else return "";
}

void initViews () {
views.clear();
views.push_back(HistoryView(-1, getLang("General")));
}

int addView (const tstring& name, int mask) {
views.push_back(HistoryView(mask, name));
return views.size() -1;
}

int initApp (void) {
if (!IsDebuggerPresent()) {
signal(SIGSEGV, crash);
set_terminate(terminate2 );
set_unexpected(terminate2);
}
if (!Socket::initialize()) return -1;
{
char buf[300]={0};
GetModuleFileNameA(NULL, buf, 300);
*(1+strrchr(buf,'\\'))=0;
APPDIR = buf;
}
string APPDATA = ProtectNull(getenv("APPDATA"));
if (APPDATA.empty()) return -2;
USERDIR = APPDATA + "\\" + string(CONFIGDIR) + "\\";
if (INVALID_FILE_ATTRIBUTES==GetFileAttributesA(USERDIR.c_str()) && !CreateDirectoryA(USERDIR.c_str(),NULL)) return -3;
string SNDDIR = USERDIR+"sounds\\";
if (INVALID_FILE_ATTRIBUTES==GetFileAttributesA(SNDDIR.c_str()) && !CreateDirectoryA(SNDDIR.c_str(),NULL)) return -4;

/*clientId = GetMACAddress();
if (!clientId || !GetVolumeInformationA(APPDATA.substr(0,3).c_str(), NULL, 0, (unsigned long*)(&clientId2), NULL, NULL, NULL, 0)) return -4;
clientId ^= 0xCAFEBABEFACEFULL;
clientId2 ^= 0xFACADE5BULL;
const char* username = getenv("USERNAME");
if (username) {
/ *SHA1Context ctx;
SHA1Reset(&ctx);
SHA1Input(&ctx, "\xB1\x7C\xD3\x8A", 4);
SHA1Input(&ctx, username, strlen(username));
SHA1Input(&ctx, "\xE4\xC5\xD6\x9F", 4);
SHA1Result(&ctx);
clientId3 = ctx.Message_Digest[0] ^ (ctx.Message_Digest[1]<<31ULL) ^(ctx.Message_Digest[2]<<25ULL) ^(ctx.Message_Digest[3]<<17ULL) ^(ctx.Message_Digest[4]<<9ULL);* /
int unl = strlen(username);
memcpy(clientId3, username, 1+unl);
for (int i=0; i<unl; i++) clientId3[i] ^= 0x5C;
}*/

if (!loadLang(true)) return -5;
/*workerReady = CreateSemaphore(NULL, 0, 1, NULL);
workerBusy = CreateSemaphore(NULL, 1, 1, NULL);
if (!workerReady || !workerBusy) return -6;
NewThread(WorkerThread, NULL);*/
/*keymap[32] = keymap[10] = keymap[13] = 1;
for (char c = 'A'; c<='Z'; c++) keymap[c]=1;
for (char c='0'; c<='9'; c++) keymap[c]=1;
for (int i=0x60; i<=0x87; i++) keymap[i]=1;
keymap[VK_APPS] = 0;
keymap[VK_DELETE] = 1;
keymap[VK_F5] = keymap[VK_F6] = keymap[VK_F7] = keymap[VK_F8] = keymap[VK_F10] = keymap[VK_F12] = 0;*/
histPos = -1;
chatpos = 0;
histView=0;
return 0;
}

inline bool IsKeyDown (int key) {
return GetAsyncKeyState(key)<0;
}

void I18NMenus (HMENU menu) {
int count = GetMenuItemCount(menu);
if (count<=0) return;
for (int i=0; i<count; i++) {
int len = GetMenuString(menu, i, NULL, 0, MF_BYPOSITION);
wchar_t buf[len+1];
GetMenuString(menu, i, buf, len+1, MF_BYPOSITION);
string oldLabel = toString(buf);
tstring newLabel = getLang(oldLabel.c_str());
MENUITEMINFO mii;
mii.cbSize = sizeof(MENUITEMINFO);
mii.fMask = MIIM_ID | MIIM_SUBMENU;
if (!GetMenuItemInfo(menu, i, TRUE, &mii)) continue;
int flg2 = MF_BYPOSITION | (mii.hSubMenu? MF_POPUP : MF_STRING);
ModifyMenu(menu, i, flg2, mii.wID, newLabel.c_str() );
if (mii.hSubMenu) I18NMenus(mii.hSubMenu);
}}

string chooseScriptToLoad (void) {
vector<tstring> list;
WIN32_FIND_DATA fd;
ZeroMemory(&fd, sizeof(fd));
HANDLE h = FindFirstFile(TEXT("worlds\\*"), &fd);
if (h) do {
tstring fn = fd.cFileName;
if (!starts_with(fn, TEXT("."))) list.push_back(fn);
} while(FindNextFile(h,&fd));
if (h) FindClose(h);
int sel = ChoiceDialog (win, getLang("Connection"), getLang("Choose the world to connect to"), list, 0);
if (sel<0 || sel>=list.size()) return "";
else return toString(list[sel]);
}

int WINAPI WinMain (HINSTANCE hThisInstance,                      HINSTANCE hPrevInstance,                      LPSTR lpszArgument,                      int nWindowStile) {

{ int re=0;
if (re=initApp()) {
TCHAR cbuf[512]={0};
snwprintf(cbuf, 511, TEXT("Unable to initialize application environment.\r\nError code: %d"), re);
Beep(1397, 200);
Beep(988, 200);
MessageBox(NULL, cbuf, TEXT("Fatal error"), MB_OK | MB_ICONERROR);
return 1;
}}

if (!initSpeech()) {
Beep(1397, 200);
Beep(988, 200);
Beep(1760, 200);
MessageBox(NULL, TEXT("Unable to initialize UniversalSpeech"), TEXT("Fatal error"), MB_OK | MB_ICONERROR);
return 1;
}
initViews();

{ int re=0;
if (re=initBass()) {
Beep(1397, 200);
Beep(988, 200);
Beep(1175, 200);
TCHAR cbuf[512]={0};
snwprintf(cbuf, 511, TEXT("Unable to initialize BASS audio library.\r\nError code: %d"), re);
speechSay(cbuf, true);
MessageBox(NULL, cbuf, TEXT("Fatal error"), MB_OK | MB_ICONERROR);
return 1;
}}

hInst = hThisInstance;
    WNDCLASSEX wincl;
wincl.hInstance = hThisInstance;
wincl.lpszClassName = TEXT(CLASSNAME);
wincl.lpfnWndProc = winproc;
wincl.style = CS_DBLCLKS;
wincl.cbSize = sizeof (WNDCLASSEX);
wincl.hIcon = LoadIcon (NULL, IDI_APPLICATION);
wincl.hIconSm = LoadIcon (NULL, IDI_APPLICATION);
wincl.hCursor = LoadCursor (NULL, IDC_ARROW);
wincl.lpszMenuName = TEXT("menu");
wincl.cbClsExtra = 0;
wincl.cbWndExtra = 0;
wincl.hbrBackground = (HBRUSH) COLOR_BACKGROUND;
if (!RegisterClassEx(&wincl)) {
MessageBox(NULL, TEXT("Couldn't register window class"), TEXT("Fatal error"), MB_OK|MB_ICONERROR);
return 1;
}
INITCOMMONCONTROLSEX ccex = { sizeof(INITCOMMONCONTROLSEX), ICC_BAR_CLASSES |  ICC_HOTKEY_CLASS | ICC_PROGRESS_CLASS | ICC_UPDOWN_CLASS | ICC_TAB_CLASSES  };
if (!InitCommonControlsEx(&ccex)) return 1;
LoadLibrary(TEXT("riched20.dll"));
win = CreateWindowEx(
WS_EX_CONTROLPARENT,
TEXT(CLASSNAME), tsnprintf(512, getLang("winTitle1"), (CLIENT_VERSION>>16)&0xFF, (CLIENT_VERSION>>8)&0xFF, CLIENT_VERSION&0xFF).c_str(), 
WS_VISIBLE | WS_OVERLAPPEDWINDOW | WS_CAPTION | WS_BORDER | WS_MAXIMIZEBOX | WS_MINIMIZEBOX | WS_SYSMENU,
CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT,
HWND_DESKTOP, NULL, hInst, NULL);
CreateWindowEx(
0, TEXT("STATIC"), getLang("History").c_str(),
WS_CHILD | WS_VISIBLE,
-200, -200, 200, 100, 
win, NULL, hInst, NULL);
edit = CreateWindowEx(
0, RICHEDIT_CLASS, TEXT(""),
WS_CHILD | WS_VISIBLE | WS_TABSTOP | WS_VSCROLL | ES_LEFT | ES_AUTOVSCROLL | ES_DISABLENOSCROLL | ES_MULTILINE | ES_READONLY | ES_WANTRETURN | ES_NOOLEDRAGDROP | ES_SAVESEL,
CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, 
win, NULL, hInst, NULL);
CreateWindowEx(
0, TEXT("STATIC"), getLang("Input").c_str(),
WS_CHILD | WS_VISIBLE | SS_NOTIFY,
CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, 
win, NULL, hInst, NULL);
chat = CreateWindowEx(
0, TEXT("EDIT"), TEXT(""),
WS_CHILD | WS_VISIBLE | ES_LEFT | ES_AUTOHSCROLL | WS_TABSTOP,
CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, 
win, NULL, hInst, NULL);
HMENU sysmenu = GetSystemMenu(win, false);
//InsertMenu(sysmenu, 5, MF_BYPOSITION | MF_STRING, IDC_ABOUT, getLang("about").c_str());
SendMessage(edit, EM_SETLIMITTEXT, 16777215, 0);
SendMessage(edit, EM_SETEVENTMASK, 0, ENM_LINK);
SendMessage(edit, WM_USER+91, TRUE, NULL);
//SendMessage(edit, EM_SETBKGNDCOLOR, conf.rtfBgColor==CLR_INVALID, conf.rtfBgColor);
SendMessage(chat, EM_SETLIMITTEXT, 4095, 0);
SetWindowSubclass(edit, histproc, 0, 0);
SetWindowSubclass(chat, chatproc, 0, 0);
//changeFont(conf.fontName, conf.fontSize);
I18NMenus(GetMenu(win));
SendMessage(win, WM_SIZE, 0, 0);
ShowWindow(win, SW_SHOW);
SetFocus(chat);
initLua();

string world = lpszArgument? lpszArgument : "";
if (world.size()<=0) world = chooseScriptToLoad();
if (world.size()>0) world = "worlds/" + world + "/init.lua";
if (world.size()<=0 || INVALID_FILE_ATTRIBUTES==GetFileAttributesA(world.c_str())) return 1;
luaExecFile(world);

MSG msg;
HACCEL hAccel = LoadAccelerators(hInst, TEXT("accel"));
while (GetMessage(&msg,NULL,0,0)) {
if (TranslateAccelerator(win, hAccel, &msg)) continue;
TranslateMessage(&msg);
DispatchMessage(&msg);
}
BASS_Free();
Socket::deinitialize();
return msg.wParam;
}

bool loadLang (bool reload) {
if (!reload && langmap.size()>0) return true;
string s = setlocale(LC_ALL,"");
setlocale(LC_ALL,"C");
int i = s.find('_');
string language = string(s.begin(), s.begin()+i);
langmap.clear();
IniFileRef<unordered_map, string, tstring> ini(langmap);
ini.load(USERDIR + language + ".lng");
if (langmap.size()<=0) ini.load(APPDIR + language + ".lng");
if (langmap.size()<=0) ini.load(USERDIR + "english.lng");
if (langmap.size()<=0) ini.load(APPDIR + "english.lng");
return langmap.size()>0;
}

const tstring& getLang (const string& key) {
const tstring& s = langmap[key];
if (!s.empty()) return s;
langmap[key] = toTString(key);
return langmap[key];
}

bool CopyToClipboard (const tstring& str) {
if (!OpenClipboard(win)) return false;
EmptyClipboard();
HGLOBAL hMem = GlobalAlloc(GMEM_MOVEABLE, sizeof(TCHAR) * (1+str.size()));
memcpy(GlobalLock(hMem), str.c_str(), sizeof(TCHAR) * (1+str.size()));
GlobalUnlock(hMem);
SetClipboardData(CF_UNICODETEXT, hMem);
CloseClipboard();
return true;
}

void ResizeAllControls (void) {
const RECT& r = winRect;
MoveWindow(chat, 10, r.bottom -45, r.right -10, 40, TRUE);
MoveWindow(edit, 10, 10, r.right -10, r.bottom -60, TRUE);
}

DWORD getSound (const string& name) {
DWORD sample = sounds[name];
if (!sample) {
string file = USERDIR + "sounds\\" + name + ".ogg";
string altFile = APPDIR + "sounds\\" + name + ".ogg";
sample = BASS_SampleLoad(FALSE, file.c_str(), 0, 0, 128, BASS_SAMPLE_SOFTWARE | BASS_SAMPLE_OVER_POS);
if (!sample) sample = BASS_SampleLoad(FALSE, altFile.c_str(), 0, 0, 128, BASS_SAMPLE_SOFTWARE | BASS_SAMPLE_OVER_POS);
sounds[name] = sample;
}
return sample;
}

bool preloadSound (const string& name) {
return getSound(name);
}

bool playSound (const string& name, double vol, double pitch, double pan) {
DWORD sample = getSound(name);
if (!sample) return false;
DWORD chan = BASS_SampleGetChannel(sample, false);
if (vol>0 && vol<1) BASS_ChannelSetAttribute(chan, BASS_ATTRIB_VOL, vol);
if (pan>=-1 && pan<=1 && pan!=0) BASS_ChannelSetAttribute(chan, BASS_ATTRIB_PAN, pan);
if (pitch>=-60 && pitch<=60 && pitch!=0) {
float f = 44100;
BASS_ChannelGetAttribute(chan, BASS_ATTRIB_FREQ, &f);
f *= pow(2, pitch/12.0);
BASS_ChannelSetAttribute(chan, BASS_ATTRIB_FREQ, f);
}
BASS_ChannelPlay(chan, false);
return true;
}


/*int setStreamsVolume (int n, bool rel) {
float old = conf.streamsVol;
if (rel) conf.streamsVol += n / 100.0f;
else conf.streamsVol = n / 100.0f;
if (conf.streamsVol>1) conf.streamsVol=1;
else if (conf.streamsVol<0.001f) conf.streamsVol=0.001f;
for (auto e: streams) {
DWORD stream = e.second;
if (stream && BASS_ChannelIsActive(stream)) {
float vol = 0;
BASS_ChannelGetAttribute(stream, BASS_ATTRIB_VOL, &vol);
if (old>0) vol = vol * conf.streamsVol / old;
BASS_ChannelSetAttribute(stream, BASS_ATTRIB_VOL, vol);
}}
return round(100*conf.streamsVol);
}

int setSoundVolume (int n, bool rel) {
if (rel) conf.fxVol += n / 100.0f;
else conf.fxVol = n / 100.0f;
if (conf.fxVol>1) conf.fxVol=1;
else if (conf.fxVol<0) conf.fxVol=0;
playSound("menubounds",-1);
return round(100*conf.fxVol);
}

void stopAllStreams (void) {
for (auto strm: streams) BASS_ChannelStop(strm.second);
streams.clear();
}

void stopAllSounds (void) {
for (auto smp: sounds) BASS_SampleStop(smp.second);
}

void changeSoundCard (int devIndex) {
int oldDev = BASS_GetDevice();
if (oldDev==devIndex) return;
if (devIndex<1) return;
else if (BASS_Init(devIndex, 44100, 0, win, NULL) || BASS_ErrorGetCode()==BASS_ERROR_ALREADY) {
BASS_DEVICEINFO dev;
BASS_GetDeviceInfo(devIndex,&dev);
conf.device = devIndex;
conf.deviceName = toString(dev.name);
for (auto e: streams) if (e.second && BASS_ChannelIsActive(e.second)) BASS_ChannelSetDevice(e.second, conf.device);
for (auto e: sounds) BASS_ChannelSetDevice(e.second, conf.device);
BASS_SetDevice(oldDev);
BASS_Free();
BASS_SetDevice(conf.device);
}}

void changeFont (const tstring& name, int size) {
LOGFONT lf;
FillLogFont(lf, win, name, size);
HFONT hf = CreateFontIndirect(&lf);
if (!hf) return;
if (font) DeleteObject(font);
font = hf;
conf.fontName = name;
conf.fontSize = size;
SendMessage(win, WM_SETFONT, font, TRUE);
SendMessage(edit, WM_SETFONT, font, TRUE);
SendMessage(chat, WM_SETFONT, font, TRUE);
SendMessage(chatlbl, WM_SETFONT, font, TRUE);
SendMessage(lstlbl, WM_SETFONT, font, TRUE);
recreateListControl();
CHARFORMAT2 cf;
cf.cbSize = sizeof(cf);
cf.dwMask = CFM_FACE | CFM_SIZE;
cf.yHeight = size*20;
memcpy(cf.szFaceName, name.c_str(), (name.size()+1)*sizeof(TCHAR));
SendMessage(edit, EM_SETCHARFORMAT, SCF_ALL, &cf);
}*/

void historyChangeView (int n, bool absolute) {
if (!absolute) n+=histView;
if (n<0 || n>=views.size()) {
MessageBeep(MB_OK);
speechSay(views[histView].name.c_str(), true);
return;
}
histView=n;
speechSay(views[n].name.c_str(), true);
}

void historyMove (int n) {
if (history.empty()) return;
int sens = n>0? 1: -1, i=histPos;
HistoryView& v = views[histView];
while (n!=0) {
i+=sens;
if (i<0 || i>=history.size()) break;
if (!(v.channels & (1<<history[i].channel))) continue;
n-=sens;
histPos=i;
}
if (histPos>=0 && histPos<history.size() && (v.channels & (1<<history[histPos].channel))) {
tstring s2 = history[histPos].value + TEXT("");
*remove_if(s2.begin(), s2.end(), [&](const TCHAR& c)mutable{  return (c<32 && c!=10 && c!=13);  }) =0;
speechSay(s2.c_str(), TRUE);
brailleDisplay(s2.c_str());
}}

/*void FlashWindow2 (int channel, int num) {
HistoryChannel& ch = channels[channel&31];
int flags = ch.flags;
if (active || !(flags&CHF_PLAY_OUTWIN)) return;
FLASHWINFO flw;
flw.cbSize = sizeof(flw);
flw.hwnd = win;
flw.dwFlags = 3;
flw.uCount = num;
flw.dwTimeout = 0;
FlashWindowEx(&flw);
}*/

void historyAdd (const tstring& s1, int channel) {
channel&=31;
int flags = 0;
tstring s = s1;


std::function<void(void)> f = [&]()mutable{
/*bool active = ::active || inDialogBox;
if (!historySpeakOff && (
(active && (flags&CHF_SPEAK_INWIN)) 
|| (!active && (flags&CHF_SPEAK_OUTWIN))
)) */
{
tstring s2 = s + TEXT("");
*remove_if(s2.begin(), s2.end(), [&](const TCHAR& c)mutable{  return (c<32 && c!=10 && c!=13);  }) =0;
speechSay(s2.c_str(), false);
}
history.push_back(HistoryEntry(s,channel));

int cp1, cp2, tlen = GetWindowTextLength(edit);
SendMessage(edit, EM_GETSEL, &cp1, &cp2);
SendMessage(edit, EM_SETSEL, tlen, tlen);
SendMessage(edit, EM_REPLACESEL, 0, TEXT("\r\n"));
SendMessage(edit, EM_SETSEL, tlen+2, tlen+2);
for (auto str: split(s,TEXT("\x1B"))) {
CHARFORMAT2 cf;
SETTEXTEX stx;
stx.flags = 6;
stx.codepage = 1200;
cf.cbSize = sizeof(cf);
cf.dwMask = CFM_BOLD | CFM_ITALIC | CFM_UNDERLINE |  CFM_STRIKEOUT | CFM_LINK | CFM_PROTECTED | CFM_SUBSCRIPT | CFM_SUPERSCRIPT | CFM_COLOR;
cf.dwEffects = 0; //(CFE_BOLD | CFE_ITALIC | CFE_UNDERLINE | CFE_STRIKEOUT | CFE_AUTOCOLOR);
cf.crTextColor = RGB(0,0,0); //ch.color;
/*int decl= -1;
while (++decl<str.size()&&str[decl]<32) {
switch(str[decl]) {
case 14 : case 15:  {
int cid = (str[++decl] -14);
if (cid>=0 && cid<custColors.size()) {
cf.crTextColor = custColors[cid];
cf.dwEffects&=(~CFE_AUTOCOLOR);
}}break;
case 16 : cf.dwEffects |= CFM_LINK | CFE_PROTECTED; break;
case 17: cf.dwEffects |= CFE_BOLD; break;
case 18: cf.dwEffects |= CFE_ITALIC; break;
case 19: cf.dwEffects |= CFE_UNDERLINE; break;
case 20: cf.dwEffects |= CFE_STRIKEOUT; break;
case 21: cf.dwEffects |= CFE_SUPERSCRIPT; break;
case 22: cf.dwEffects |= CFE_SUBSCRIPT; break;
default: break;
}}*/
SendMessage(edit, EM_SETCHARFORMAT, SCF_SELECTION, &cf);
SendMessage(edit, EM_SETTEXTEX, &stx, str.c_str()+0);
}
if (GetFocus()==edit) SendMessage(edit, EM_SETSEL, cp1, cp2); 
else SendMessage(edit, WM_VSCROLL, SB_BOTTOM, 0);
}; // EDT
SendMessage(win, WM_RUNPROC, 0, &f);
}

tstring historyReturnView (void) {
tostringstream out;
for (HistoryEntry& e: history) {
if (!(views[histView].channels&(1<<e.channel))) continue;
out << e.value << "\r\n";
}
return out.str();
}

void historyCopyView (void) {
CopyToClipboard(historyReturnView());
speechSay(getLang("copied").c_str(),true);
}

void historySave (const tstring& file) {
tofstream out(toString(file));
out << historyReturnView();
speechSay(getLang("saved").c_str(),true);
}

void historySave () {
tstring file = FileDialog (win, FD_SAVE, TEXT(""),  getLang("Save view"), getLang("Text files") + TEXT("|*.txt") );
if (file.size()>0) historySave(file);
}

void historyClear (void) {
SetWindowText(edit, NULL);
history.clear();
}

void handleChatEnter () {
tstring str = GetWindowText(chat);
if (str.size()<1) return false;
if (str==TEXT("!") && chatHistory.size()>0) str = chatHistory.back();
luaMudSend(str);
for (auto it = chatHistory.begin(); it!=chatHistory.end(); ++it) {
if (*it==str) {
chatHistory.erase(it);
break;
}}
chatHistory.push_back(str);
if (chatHistory.size()>100) chatHistory.erase(chatHistory.begin());
chatpos = chatHistory.size();
SetWindowText(chat,NULL);
}

bool chatHistoryMove (int n) {
chatpos += n;
if (chatpos<0) chatpos=0;
else if (chatpos>chatHistory.size()) chatpos=chatHistory.size() ;
if (chatpos<0 || chatpos>=chatHistory.size()) {
SetWindowText(chat,NULL);
return true;
}
const tstring& s = chatHistory[chatpos];
SetWindowText(chat, s.c_str());
//SendMessage(chat, EM_SETSEL, selStrt s.size());
return true;
}

void aboutDialog (HWND hwnd) {
tstring txt = tsnprintf(1023, TEXT("%s %d.%d.%d\r\nCopyright \xA9 2016 QuentinC\r\nhttp://quentinc.net/"), getLang("about3").c_str(), (CLIENT_VERSION>>16)&0xFF, (CLIENT_VERSION>>8)&0xFF, (CLIENT_VERSION)&0xFF);
MessageBox(hwnd, txt.c_str(), getLang("about2").c_str(), MB_OK | MB_ICONINFORMATION);
}

/*DWORD CALLBACK WorkerThread (LPVOID unused) {
while(true){
WaitForSingleObject(workerReady, INFINITE);
if (workerFunc && *workerFunc) {
(*workerFunc)();
delete workerFunc;
workerFunc = NULL;
}
ReleaseSemaphore(workerBusy, 1, NULL);
}
return 0;
}

void SendToWorker (function<void(void)>* func) {
WaitForSingleObject(workerBusy, INFINITE);
workerFunc = func;
ReleaseSemaphore(workerReady, 1, NULL);
}*/

LRESULT CALLBACK chatproc (HWND hwnd, UINT msg, WPARAM wp, LPARAM lp, INT_PTR scId, DWORD scData) {
switch(msg) {
case WM_KEYDOWN : 
case WM_SYSKEYDOWN :
shiftDown = IsKeyDown(VK_SHIFT);
ctrlDown = IsKeyDown(VK_CONTROL); 
altDown = IsKeyDown(VK_MENU);
switch(LOWORD(wp)) {
case VK_RETURN: 
if (!ctrlDown&&!shiftDown&&!altDown) handleChatEnter(); 
return true;
case VK_PRIOR: historyMove(ctrlDown||shiftDown? -1000000000 : -1); return true;
case VK_NEXT: historyMove(ctrlDown||shiftDown? 1000000000 : 1); return true;
case VK_UP : chatHistoryMove(-1); return true;
case VK_DOWN :
if (altDown) return true; 
chatHistoryMove(1); return true;
case VK_TAB : return true; //todo
case VK_ESCAPE: SetWindowText(chat, NULL); return TRUE;
case VK_LEFT: case VK_RIGHT: case VK_CONTROL: case VK_MENU: case VK_SHIFT: break;
default : {
int k1 = LOWORD(wp), k = k1 | (shiftDown?VKM_SHIFT:0) | (ctrlDown?VKM_CTRL:0) | (altDown?VKM_ALT:0);
if (k==2163) break; // Alt+F4
if (altDown || ctrlDown || (k1>=0x60 && k1<0x90)) {
luaRunMacro(k);
return FALSE;
}}break;
}break;
case WM_KEYUP : 
shiftDown = IsKeyDown(VK_SHIFT);
ctrlDown = IsKeyDown(VK_CONTROL); 
altDown = IsKeyDown(VK_MENU);
switch(LOWORD(wp)) {
case VK_TAB: 
if (shiftDown) SetFocus(edit);
return true;
case VK_RETURN: return true;
}break;
case WM_CHAR :  
ctrlDown = IsKeyDown(VK_CONTROL) ;
altDown = IsKeyDown(VK_MENU);
if (ctrlDown && (LOWORD(wp)==22 || LOWORD(wp)==3 || LOWORD(wp)==24)) break;
if (LOWORD(wp)==VK_RETURN || LOWORD(wp)==VK_TAB || (ctrlDown^altDown))  return true;
{int scan = (lp>>16)&0xFF; if (scan>=70 && scan<=84) return true; }
break;
case WM_SYSCOMMAND: if (wp==SC_KEYMENU) return TRUE; break;
case WM_SETFOCUS:  
SendMessage(chat, EM_SETSEL, 0, -1);
break;
}
return DefSubclassProc(hwnd,msg,wp,lp);
}

LRESULT CALLBACK histproc (HWND hwnd, UINT msg, WPARAM wp, LPARAM lp, INT_PTR scId, DWORD scData) {
switch(msg) {
case WM_KEYDOWN : 
case WM_SYSKEYDOWN :
shiftDown = IsKeyDown(VK_SHIFT);
ctrlDown = IsKeyDown(VK_CONTROL); 
altDown = IsKeyDown(VK_MENU);
switch(LOWORD(wp)) {
case VK_RETURN : {
int sel=0;
SendMessage(edit, EM_GETSEL, &sel, 0);
LPARAM pt = SendMessage(edit, EM_POSFROMCHAR, sel+1, 0);
SendMessage(edit, WM_LBUTTONDOWN, 0, pt);
SendMessage(edit, WM_LBUTTONUP, 0, pt);
return TRUE;
}break;
case VK_TAB : return TRUE;
case VK_PRIOR: historyMove(ctrlDown||shiftDown? -1000000000 : -1); return true;
case VK_NEXT: historyMove(ctrlDown||shiftDown? 1000000000 : 1); return true;
default: 
if (ctrlDown && !shiftDown && !altDown && LOWORD(wp)=='C') break;
if ( (LOWORD(wp)>='A'&&LOWORD(wp)<='Z') || (LOWORD(wp)>='0'&&LOWORD(wp)<='9') ) {
//sendKeyDown(LOWORD(wp), shiftDown, ctrlDown, altDown);
return FALSE; 
}break;
}break;
case WM_KEYUP : 
shiftDown = IsKeyDown(VK_SHIFT);
ctrlDown = IsKeyDown(VK_CONTROL); 
altDown = IsKeyDown(VK_MENU);
switch(LOWORD(wp)) {
case VK_TAB: 
SetFocus(chat); 
return TRUE;
}break;
case WM_CHAR : return TRUE;
case WM_CONTEXTMENU : {
static HMENU hPop = NULL;
if (!hPop) {
hPop = CreatePopupMenu();
AppendMenu(hPop, MF_STRING, IDC_SAVE, getLang("Sa&ve view...").c_str());
AppendMenu(hPop, MF_STRING, IDC_COPYVIEW, getLang("copyView").c_str());
AppendMenu(hPop, MF_STRING, IDC_CLEARHISTORY, getLang("Clear history").c_str());
}
POINT pt;
GetCursorPos(&pt);
TrackPopupMenu(hPop, TPM_LEFTBUTTON, pt.x, pt.y, 0, win, NULL);
}return TRUE;
//case WM_SYSCOMMAND: if (wp==SC_KEYMENU) return TRUE; break;
case WM_SETFOCUS: break;
}
return DefSubclassProc(hwnd,msg,wp,lp);
}

LRESULT CALLBACK SendMessageInEDT (HWND hwnd, UINT msg, WPARAM wp, LPARAM lp) {
LRESULT re = 0;
RunInEDT({
re = SendMessage(hwnd, msg, wp, lp);
}) // EDT
return re;
}

LRESULT CALLBACK winproc (HWND hwnd, UINT msg, WPARAM wp, LPARAM lp) {
switch (msg) {
case WM_COMMAND : 
switch(LOWORD(wp)) {
case IDC_NEXT_VIEW: historyChangeView(1,false); break;
case IDC_PREV_VIEW: historyChangeView(-1,false); break;
case IDC_COPYCUR : 
if (histPos>=0 && histPos<=history.size()) {
tstring s2 = history[histPos].value;
CopyToClipboard(s2); 
speechSay(getLang("copied").c_str(),true);
}
else MessageBeep(MB_OK);
break;
case IDC_COPYVIEW : historyCopyView(); break;
case IDC_CLEARHISTORY : historyClear(); break;
case IDC_SAVE: historySave(); break;
//case IDC_SPEECHONOFF : historySpeakOff=!historySpeakOff; speechSay(getLang(historySpeakOff?"disabled":"enabled").c_str(), true); break;
//case IDC_STOPSOUNDS : stopAllSounds(); break;
//case IDC_STOPSTREAMS: stopAllStreams(); break;
case IDC_ABOUT: aboutDialog(win); break;
case IDC_EXIT: SendMessage(hwnd, WM_CLOSE, 0, 0); break;
// other commands
}
if (LOWORD(wp)>=IDC_SETVIEWN && LOWORD(wp)<=IDC_SETVIEWN+9) historyChangeView(LOWORD(wp)-IDC_SETVIEWN, true);
break;
case WM_RUNPROC : {
std::function<void(void)>* f = lp; (*f)();
}break;
case WM_NOTIFY : switch( ((NMHDR*)(lp)) ->code) {
/*case EN_LINK : {
ENLINK* l = (ENLINK*)lp;
if (l->msg==WM_LBUTTONDOWN) openLink(l->chrg.cpMin, l->chrg.cpMax);
}break;*/
// ohter notifications
}break;
case WM_SETFOCUS : SetFocus(chat);  break;
case WM_ACTIVATE : active = !!LOWORD(wp); break;
case WM_SIZE : {
GetClientRect(win,&winRect);
ResizeAllControls();
}break;
case WM_SYSCOMMAND: 
//if (wp==SC_KEYMENU && lp!=' ') return TRUE;
if (LOWORD(wp)==IDC_ABOUT) aboutDialog(win);
break;
case WM_CLOSE :
//if (conf.confirmOnExit && IDYES!=MessageBox(win, getLang("confirmquit").c_str(), tsnprintf(512, getLang("winTitle1"), (CLIENT_VERSION>>16)&0xFF, (CLIENT_VERSION>>8)&0xFF, CLIENT_VERSION&0xFF).c_str(), MB_ICONEXCLAMATION | MB_YESNO)) return TRUE; 
break;
case WM_DESTROY :
PostQuitMessage(0); 
break;
}
return DefWindowProc(hwnd,msg,wp,lp);
}

