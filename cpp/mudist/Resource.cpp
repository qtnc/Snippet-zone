#include "global.h"
#include "Resource.hpp"
#include<cstring>

Resource::Resource (const TCHAR* name) : hf(0), hr(0) {
hf = FindResource(NULL, name, MAKEINTRESOURCE(RT_RCDATA));
if (hf) hr = LoadResource(NULL, hf);
}

Resource::~Resource () {
if (hr) FreeResource(hr);
}

size_t Resource::size () {
return SizeofResource(NULL, hf);
}

const void* Resource::data () {
return LockResource(hr);
}

void* Resource::copy () {
size_t sz = size();
char* ch = new char[sz];
memcpy(ch, data(), sz);
return ch;
}
