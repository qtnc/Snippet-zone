#ifndef ___TCPBUF_H_8
#define ___TCPBUF_H_8
#include<iostream>
#include<memory>

#ifdef __WIN32
#include<winsock2.h>
#include<windows.h>
#include <ws2tcpip.h>
#else
#include<sys/socket.h>
#endif

class tcpbuf: public std::streambuf {
protected:
SOCKET sock = 0;
std::unique_ptr<char[]> in = nullptr, out = nullptr;
size_t inSize=0, outSize=0;
int errCode = 0;
friend class tcpstream;

public:
tcpbuf () = default;
virtual int open (const char* host, uint16_t port);
virtual int recv (char* buf, int len);
virtual int send (const char* buf, int len);
virtual void close ();
~tcpbuf ();

protected:
int underflow () override;
int overflow (int c) override;
int sync () override;
};

#endif
