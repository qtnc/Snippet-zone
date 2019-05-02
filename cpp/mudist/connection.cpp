#include "global.h"
#include "strings.hpp"
#include "Socket.hpp"
#include "strings.hpp"
#include<string>
using namespace std;

Socket sock;
string lastHost = "";
int lastPort = 0;
int recoDelay = 5;
bool reconnect = true;


int GetErrorText (int errcode, LPTSTR buf, DWORD maxlen);
const tstring& getLang (const string&) ;
void mudRecvLine (const string&);
void historyAdd (const tstring&, int);
int mudConnect (const string&, int);
void luaMudConnected();
void luaMudDisconnected();
void luaConnectError (int);


void mudDisconnect () {
if (sock) {
historyAdd(getLang("disconnected"), 0);
sock.close();
luaMudDisconnected();
}}

DWORD CALLBACK mudReadLoop (LPVOID lp) {
char buf[4096] = {0};
char* ch = buf -1;
int n, telnetCmd=0;
while((n=sock.recv(++ch,1))>0 || n==-2) {
if (*ch=='\n' || n==-2) {
*ch=0;
if (ch!=buf) mudRecvLine(trim_copy(string(buf)));
ch = buf -1;
}
else if (*ch>0 && *ch<32 && *ch!='\t' && *ch!='\x1B') --ch;
else if (telnetCmd) {
telnetCmd--;
if (*ch!='\xFF') --ch;
}
else if (*ch=='\xFF') { --ch; telnetCmd=1; }
}
if (sock) mudDisconnect();
if (reconnect && lastPort && lastHost.size()>0) while(mudConnect(lastHost, lastPort)) {
Sleep(recoDelay);
recoDelay*=2;
}}

int mudConnect (const string& host, int port) {
mudDisconnect();
historyAdd(snwprintf(512, getLang("connecting2"), host.c_str(), port), 0);
int re = sock.open(host.c_str(), port);
if (re==0) {
lastHost=host; lastPort=port;
recoDelay=5;
historyAdd(snwprintf(512, getLang("connecting3"), host.c_str(), port), 0);
luaMudConnected();
NewThread(mudReadLoop, NULL);
}
else {
TCHAR msg[4096]={0};
GetErrorText(re, msg, 4095);
historyAdd(snwprintf(512, getLang("connecting4"), host.c_str(), port, re, msg), 0);
luaConnectError(re);
}
return re;
}

void mudSend (const string& s) {
if (sock) sock.send((s + "\r\n").c_str(), s.size()+2);
}


