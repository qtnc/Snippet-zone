#ifndef _____SOCKET_HPP_8_____
#define _____SOCKET_HPP_8_____
#include<stdexcept>
#include<cstdint>
#include<iostream>
#include<string>

#ifdef __WIN32
#define export __declspec(dllexport)
#else
#define export 
#endif

class export tcpstream: public std::iostream {
protected:
int errCode = 0;

public:
static export bool initialize ();
static export void deinitialize ();

int export open (const char* host, uint16_t port);
int errcode ();
inline bool is_open () { return rdbuf(); }

inline tcpstream () { initialize(); }
inline tcpstream (const char* host, uint16_t port): tcpstream() { open(host, port); }
inline tcpstream (const std::string& host, uint16_t port): tcpstream(host.c_str(), port) {}

virtual void close ();
virtual ~tcpstream ();
};

#endif
