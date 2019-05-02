#ifndef _____RESOURCE_HPP_3
#define _____RESOURCE_HPP_3
#include "global.h"

struct Resource {
HANDLE hf, hr;

Resource (const TCHAR* name) ;
~Resource () ;
Resource (const Resource&) = delete;
operator= (const Resource&) = delete;
size_t size () ;
const void* data () ;
void* copy () ;
};

#endif

