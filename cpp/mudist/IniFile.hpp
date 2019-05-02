#ifndef _____INI_FILE_____
#define _____INI_FILE_____
#include<string>
#include<fstream>
#include "strings.hpp"

template <template <class, class, class...> class M, class K, class V, class... UNUSED>
class IniFileRef {
private :
M<K,V,UNUSED...>& m;

public :
IniFileRef (M<K,V,UNUSED...>& x) : m(x) {}

bool load (const std::string& filename) {
std::ifstream in(filename);
if (!in) return false;
return load(in);
}

bool load (std::istream& in) {
bool longMode=false;
std::string line, lastKey;
while (getline(in,line)) {
if (line.empty() || line[0]==';' || line[0]=='#' || line[0]=='-') continue;
if (!longMode) {
if (line[0]=='[') goto longmode;
int eqp = line.find('=');
if (eqp<0 || eqp>=line.size()) continue;
std::string key(line.begin(), line.begin()+eqp);
std::string value(line.begin()+eqp+1, line.end());
replace_all(value, "\\r", "\r");
replace_all(value, "\\n", "\n");
replace_all(value, "\\t", "\t");
m[key] = fromString<V>(value);
}
else { longmode: 
if (line[0]=='[') {
longMode=true;
int ecp = line.find(']');
lastKey = std::string(line.begin()+1, line.begin()+ecp);
m[lastKey] = fromString<V>("");
continue;
}
m[lastKey] = fromString<V>(toString(m[lastKey]) + "\n" + line);
}}
return true;
}

void save (const std::string& filename) {
std::ofstream out(filename);
if (!out) return;
for (auto e: m) {
out << e.first << "=" << toString(e.second) << std::endl;
}
out.close();
}

inline bool empty () const { return m.empty(); }
inline size_t size () const { return m.size(); }
inline bool contains (const K& key) const { return m.find(key)!=m.end(); }
inline const V& operator[] (const K& k) const { return m[k]; }
inline V& operator[] (const K& k) { return m[k]; }
inline decltype(m.begin()) begin () { return m.begin(); }
inline decltype(m.end()) end () { return m.end(); }

template <class T> T get (const K& key, const T& def) {
auto it = m.find(key);
return it==m.end()? def : fromString<T>(toString(it->second));
}

std::string get (const K& key, const char* value) {
return get<std::string>(key, std::string(value));
}

template <class T> void put (const K& key, const T& value) {
m[key] = toString(value);
}


};

template <template <class, class, class...> class M, class K, class V, class... UNUSED>
class IniFile: public IniFileRef<M,K,V,UNUSED...> {
private : 
M<K,V,UNUSED...> map;
public :
IniFile () : IniFileRef<M,K,V,UNUSED...>(map) {}
};

#endif
