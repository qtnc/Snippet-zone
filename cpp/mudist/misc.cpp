#include "global.h"
#include<string>
#include<cwchar>
#include<cstdarg>
#include<algorithm>
#include<shlobj.h>
#include <Iphlpapi.h>
#include "strings.hpp"
#include "UniversalSpeech.h"
using namespace std;

const tstring& getLang (const string& key) ;

void FillLogFont (LOGFONT& lf, HWND h, const tstring& name, int size) {
lf.lfHeight = MulDiv(size, GetDeviceCaps(GetDC(h), LOGPIXELSY), 72);
lf.lfWidth = 0;
lf.lfEscapement = 0;
lf.lfOrientation = 0;
lf.lfWeight = 0;
lf.lfItalic = 0;
lf.lfUnderline = 0;
lf.lfStrikeOut = 0;
lf.lfCharSet = DEFAULT_CHARSET;
lf.lfOutPrecision = OUT_DEFAULT_PRECIS, 
lf.lfClipPrecision = CLIP_DEFAULT_PRECIS;
lf.lfQuality = DEFAULT_QUALITY;
lf.lfPitchAndFamily = VARIABLE_PITCH | FF_SWISS;
memcpy(lf.lfFaceName, name.c_str(), name.size()+1);
}

int GetErrorText (int errcode, LPTSTR buf, DWORD maxlen) {
return FormatMessage(FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS, NULL, errcode, MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), buf, maxlen, NULL);
}

static inline bool rgb2hsl1 (double r, double g, double b, double& h, double& s, double& l) {
double max = std::max(std::max(r, g), b);
double min = std::min(std::min(r, g), b);
l = (max+min)/2.0;
if (max==min) {
h=0;
s=0;
return true;
}
if (l<=0.5) s = (max-min)/(max+min);
else s = (max-min)/(2-max-min);
double rc = (max-r)/(max-min), gc = (max-g)/(max-min), bc = (max-b)/(max-min);
if (r==max) h = gc-bc;
else if (g==max) h = 2+rc-bc;
else h = 4+gc-rc;
h /= 6.0;
if (h>1) h--;
else if (h<0) h++;
return true;
}

COLORREF rgb2hsl (COLORREF c) {
double r = GetRValue(c) / 255.0;
double g = GetGValue(c) / 255.0;
double b = GetBValue(c) / 255.0;
double h, s, l;
rgb2hsl1(r,g,b,h,s,l);
return RGB(h*240.0, s*240.0, l*240.0);
}

unsigned long long getLastModified (LPCTSTR fn) {
static unsigned long long rep = 0;
if (!rep) {
SYSTEMTIME st1 = { 1970, 1, 0, 1, 0, 0, 0, 0 };
FILETIME ft1;
SystemTimeToFileTime(&st1, &ft1);
rep = (((unsigned long long)ft1.dwHighDateTime)<<32) | ft1.dwLowDateTime;
}
unsigned long long l = 0;
FILETIME ft;
HANDLE h = CreateFile(fn, GENERIC_READ, 0, 0, OPEN_EXISTING, 0, 0);
if (h==INVALID_HANDLE_VALUE) return 0;
if (GetFileTime(h, 0, 0, &ft)) l = (((unsigned long long)ft.dwHighDateTime)<<32) | ft.dwLowDateTime;
CloseHandle(h);
return ((l-rep));
}

