#include "tcpbuf.hpp"
#include "tcpstream.hpp"
#include<iostream>
using namespace std;

#ifdef __WIN32
static bool initialized = false;

bool tcpstream::initialize (void) {
if (initialized) return true;
        WSADATA WSAData;
return initialized = !WSAStartup(MAKEWORD(2,0), &WSAData);
}

void tcpstream::deinitialize (void) {
if (!initialized) return;
WSACleanup();
initialized=false;
}

#else
//todo
#endif

int tcpstream::errcode () {
if (errCode) return errCode;
auto buf = dynamic_cast<tcpbuf*>(rdbuf());
return buf? buf->errCode : 0;
}

void tcpstream::close () {
rdbuf(nullptr);
}

tcpstream::~tcpstream () {
close();
}

int tcpstream::open (const char* host, uint16_t port) {
auto buf = new tcpbuf();
if (!(errCode = buf->open(host, port))) {
rdbuf(buf);
clear();
}
else {
delete buf;
setstate(badbit);
}
return errCode;
}

tcpbuf::~tcpbuf () {
close();
}

int tcpbuf::open (const char* host, uint16_t port) {
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

void tcpbuf::close (void) {
if (sock) closesocket(sock);
sock=0;
}

int tcpbuf::send (const char* s, int len) {
if (!sock) return -1;
int n=0, m=1;
while (sock && n<len && m>0 && m!=SOCKET_ERROR) {
m = ::send(sock, s+n, len-n, 0);
if (m==SOCKET_ERROR) { 
close(); 
errCode = WSAGetLastError();
return n; 
}
n+=m;
}
return n;
}

int tcpbuf::recv (char* buf, int len) {
if (!sock || len<=0) return -1;
int n = ::recv(sock, buf, len, 0);
if (n<=0 || n==SOCKET_ERROR) {
errCode = WSAGetLastError();
return -1;
}
return n;
}

int tcpbuf::underflow () {
if (!in || !inSize) {
inSize = 4096;
in = make_unique<char[]>(inSize);
}
int read = recv(&in[0], inSize);
if (read<0) return EOF;
setg(&in[0], &in[0], &in[read]);
return *eback();
}

int tcpbuf::overflow (int c) {
if (c==EOF) return c;
if (out && outSize) {
int pos = pptr()-pbase();
auto newOut = make_unique<char[]>(outSize*=2);
memcpy(&newOut[0], &out[0], pos);
out = std::move(newOut);
setp(&out[pos], &out[outSize]);
out[pos] = c;
}
else {
out = make_unique<char[]>(4096);
setp(&out[0], &out[4096]);
out[0] = c;
}
pbump(1);
return c;
}

int tcpbuf::sync () {
int len = pptr()-pbase();
int n = send(&out[0], len);
setp(&out[0], &out[outSize]);
return n!=len;
}

