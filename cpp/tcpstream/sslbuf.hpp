#ifndef _____SSLSOCKET_HPP_8_____
#define _____SSLSOCKET_HPP_8_____
#include "sslstream.hpp"
#include "tcpbuf.hpp"

#ifdef USE_SCHANNEL
#include "ssl.h"
#else
struct tls;
typedef tls SSL_SOCKET;
#endif

class sslbuf: public tcpbuf {
private:
SSL_SOCKET* ssl = nullptr;
friend class sslstream;

public:
sslbuf () = default;
virtual int open (const char* host, uint16_t port) override;
virtual int recv (char* buf, int len) override;
virtual int send (const char* buf, int len) override;
virtual void close () override;
~sslbuf ();
};

#endif
