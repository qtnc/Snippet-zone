#include "global.h"
#include "strings.hpp"
#include<string>
#include<cwchar>
#include<cstdarg>
#include<boost/algorithm/string.hpp>
#include<unordered_map>
using namespace std;

string snsprintf (int max, const string& fmt, ...) {
string out(max+1, '\0');
va_list ap;
va_start(ap,fmt);
stringSize(out) = vsnprintf((char*)out.data(), max, fmt.c_str(), ap);
va_end(ap);
return out;
}

wstring snwprintf (int max, const wstring& fmt, ...) {
wstring out(max+1, L'\0');
out.reserve(max);
va_list ap;
va_start(ap,fmt);
stringSize(out) = vsnwprintf((wchar_t*)out.data(), max, fmt.c_str(), ap);
va_end(ap);
return out;
}

