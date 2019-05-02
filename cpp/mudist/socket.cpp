#include<winsock2.h>
#include<windows.h>
#include <ws2tcpip.h>
#include<cstring>
#include "socket.hpp"
#include<cstdio>

extern "C" int WSAAPI getaddrinfo (PCSTR pNodeName, PCSTR pServiceName, const addrinfo *pHint,addrinfo** ppResult);
extern "C" void WSAAPI freeaddrinfo (addrinfo*);

struct scope_lock {
CRITICAL_SECTION& cs;
scope_lock (CRITICAL_SECTION& cs1) : cs(cs1) { EnterCriticalSection(&cs); }
~scope_lock () { LeaveCriticalSection(&cs); }
};
#define SCOPE_LOCK(x) scope_lock ___CS_SCOPE_LOCK_##__LINE__##_ (x)


bool Socket::initialize (void) {
        WSADATA WSAData;
return !WSAStartup(MAKEWORD(2,0), &WSAData);
}

void Socket::deinitialize (void) {
WSACleanup();
}

Socket::Socket (void) : sock(0) {
InitializeCriticalSection(&cs);
}

Socket::~Socket () {
close();
DeleteCriticalSection(&cs);
}

int Socket::open (const char* host, int port) {
//https://msdn.microsoft.com/en-us/library/windows/desktop/ms738520(v=vs.85).aspx
addrinfo *addrs=NULL, hint = { 0, AF_UNSPEC, SOCK_STREAM, IPPROTO_TCP, 0, NULL, NULL, NULL };
int result=0;
char strPort[7]={0};
snprintf(strPort, 6, "%d", port);
if ((result=getaddrinfo(host, strPort, &hint, &addrs))) return result;
if (addrs) for (addrinfo *p = addrs; p; p=p->ai_next) {
if (
(sock = socket(p->ai_family, p->ai_socktype, 0))
&& SOCKET_ERROR!=connect(sock, (SOCKADDR*)(p->ai_addr), p->ai_addrlen))
{ break; }
result = WSAGetLastError();
if (socket) closesocket(sock);
}
if (addrs) freeaddrinfo(addrs);
return result;
}

void Socket::close (void) {
SCOPE_LOCK(cs);
if (sock) closesocket(sock);
sock=0;
}

int Socket::send (const void* s, int len) {
if (!sock) return -1;
if (len<0) len=strlen(s);
SCOPE_LOCK(cs);
int n=0, m=1;
while (sock && n<len && m>0 && m!=SOCKET_ERROR) {
m = ::send(sock, s+n, len-n, 0);
if (m==SOCKET_ERROR) { close(); return n; }
n+=m;
}
return n;
}

int Socket::recv (void* buf, int len) {
if (!sock || len<=0) return -1;
fd_set fdSet;
fdSet.fd_count=1;
fdSet.fd_array[0] = sock;
timeval tv = { 0, 300000 };
int sel = select(0, &fdSet, NULL, &fdSet, &tv);
if (sel==0) return -2;
else if (sel==SOCKET_ERROR) return -1;
int n, pos = 0;
while(pos<len) {
n = ::recv(sock, buf+pos, len-pos, 0);
if (n<=0 || n==SOCKET_ERROR) return 0;
pos+=n;
}
return pos;
}


/*#include<iostream>
using namespace std;
int main (int argc, char** argv) {
char c, buf[1024] = {0};
Socket::initialize();
Socket sock;
sock.open("localhost", 80);
sock.send("GET / HTTP/1.0\r\n\r\n", -1);

int n=0;
while (sock.recv(&c,1)==1) {
if (c=='\n' || c=='\r' || n>=1023) {
buf[n]=0;
cout << n << ": " << buf << endl;
n=0;
continue;
}
buf[n++] = c;
}

}*/

