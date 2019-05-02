#ifndef _____SSL0SOCKET_HPP_8_____
#define _____SSL0SOCKET_HPP_8_____
#include "tcpstream.hpp"

class export sslstream: public tcpstream {

public:
static export bool initialize ();
static export void deinitialize ();

int export open (const char* host, uint16_t port);

inline sslstream (): tcpstream()  { initialize(); }
inline sslstream (const char* host, uint16_t port): sslstream() { open(host, port); }
inline sslstream (const std::string& host, uint16_t port): sslstream(host.c_str(), port) {}
};

#endif
