#ifndef _CONSTS_H
#define _CONSTS_H
#define CINTERFACE
#define _WIN32_IE 0x0400
//#define _WIN32_WINNT 0x501
#define UNICODE
#include<windows.h>
#include<commctrl.h>

#define null nullptr

#define min(a,b) ((a)<(b)?(a):(b))
#define max(a,b) ((a)>(b)?(a):(b))

#define ___WIN___ win
#define RunInEDT(b) { std::function<void(void)> func = [&]() mutable b; SendMessage(___WIN___, WM_RUNPROC, 0, &func); }
#define NewThread(x,v) CreateThread( NULL, 0, x, v, 0, NULL)

#ifdef UNICODE
#define tstring std::wstring
#define toTString toWString
#define tsnprintf snwprintf
#define tostringstream wostringstream
#define tofstream wofstream
#define tstrdup wcsdup
#else
#define tstring std::string
#define toTString toString
#define tsnprintf snsprintf
#define tostringstream ostringstream
#define tofstream ofstream
#define tstrdup strdup
#endif

#define CLASSNAME "QCMudist01"
#define CONFIGDIR "Mudist"
#define CLIENT_VERSION 0x00001
#define CLIENT_BUILD 1

#define CHF_SPEAK_NEVER 0
#define CHF_SPEAK_INWIN 1
#define CHF_SPEAK_OUTWIN 2
#define CHF_SPEAK_ALWAYS 3
#define CHF_SPEAK_INTERRUPT 4
#define CHF_PLAY_NEVER 0
#define CHF_PLAY_INWIN 8
#define CHF_PLAY_OUTWIN 16
#define CHF_PLAY_ALWAYS 24
#define CHF_NOLOG 32


#define FONTNAME "Arial"
#define FONTSIZE 14

#define WM_RUNPROC WM_USER +100

#define IDC_CHAT 1001
#define IDC_EXIT 1034
#define IDC_SAVE 1035
#define IDC_ABOUT 1036
#define IDC_NEXT_VIEW 2023
#define IDC_PREV_VIEW 2024
#define IDC_COPYCUR 2025
#define IDC_COPYVIEW 2027
#define IDC_CLEARHISTORY 2030
#define IDC_SETVIEWN 2900
#define IDC_DEBUG 3000

#define IDD_CHOICE "choicedlg"
#define IDD_INPUT "inputdlb"

#define CFM_HIDDEN 0x100
#define CFE_HIDDEN CFM_HIDDEN
#define CFE_NONE 0

#endif
