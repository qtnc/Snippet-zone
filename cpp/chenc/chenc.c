#include<windows.h>
#include<stdio.h>

const struct encoding {
int cp;
const char* name;
}
encodings[] = {
{ 0, "ANSI" },
{ 65001, "UTF-8" },
{ 65002, "UTF-8+BOM" },
{ 65010, "UTF-16Le" },
{ 65011, "UTF-16Le+BOM" },
{ 65020, "UTF-16Be" },
{ 65021, "UTF-16Be+BOM" },
{ 28591, "ISO-8859-1" },
{ 28592, "ISO-8859-2" },
{ 28593, "ISO-8859-3" },
{ 28594, "ISO-8859-4" },
{ 28595, "ISO-8859-5" },
{ 28596, "ISO-8859-6" },
{ 28597, "ISO-8859-7" },
{ 28598, "ISO-8859-8" },
{ 28599, "ISO-8859-9" },
{ 28603, "ISO-8859-13" },
{ 28605, "ISO-8859-15" },
{ 500, "EBCDIC" },

{ 38598, "ISO-8859-8" },
{ 65001, "UTF8" },
{ 65002, "UTF8+BOM" },
{ 65002, "UTF8BOM" },
{ 65010, "UTF16Le" },
{ 65011, "UTF16Le+BOM" },
{ 65020, "UTF16Be" },
{ 65021, "UTF16Be+BOM" },
{ 65000, "UTF7" },
{ 28591, "Latin-1" },
{ 28591, "Latin1" },
{ 28605, "Latin-9" },
{ 28605, "Latin9" },
{ 28592, "Latin-2" },
{ 28592, "Latin2" },
{ 65000, "UTF-7" },
{ 10000, "Macintosh" },
{ 0, "US-ASCII" },
{ 0, "ASCII" },
{ 20273, "IBM273" },
{ 20277, "IBM277" },
{ 20278, "IBM278" },
{ 20280, "IBM280" },
{ 20284, "IBM284" },
{ 20285, "IBM285" },
{ 20290, "IBM290" },
{ 20297, "IBM297" },
{ 20420, "IBM420" },
{ 20423, "IBM423" },
{ 20424, "IBM424" },
{ 20871, "IBM871" },
{ 20880, "IBM880" },
{ 20905, "IBM905" },
{ 20924, "IBM924" },
{ 20924, "IBM00924" },
{ 20866, "Koi8-R" },
{ 21866, "Koi8-U" },
{ 21025, "IBM1025" },
{ 50220, "ISO-2022-JP" },
{ 50222, "ISO-2022-JP" },
{ 50225, "ISO-2022-KR" },
{ 51932, "EUC-JP" },
{ 51949, "EUC-KR" },
{ 54936, "GB18030" },
{ 932, "Shift-JIS" },
{ 936, "GB2312" },
{ 950, "BIG5" },
{ 1361, "JOHAB" },
{ -1, NULL }
};


static const wchar_t* mb2wc (const char* s, int csz, unsigned int cp, int* sz) {
if (!s) return NULL;
if (csz<=0) csz = strlen(s);
int nSize = MultiByteToWideChar(cp, 0, s, csz, NULL, 0);
if (nSize<=0) return NULL;
wchar_t* wbuf = malloc((1+nSize)*sizeof(wchar_t));
nSize = MultiByteToWideChar(cp, 0, s, csz, wbuf, nSize);
if (nSize<=0) { free(wbuf); return NULL; }
wbuf[nSize]=0;
if (sz) *sz = nSize;
wbuf[nSize]=0;
return wbuf;
}

static const char* wc2mb (const wchar_t* ws, int wssz, unsigned int cp, int* sz, int flags) {
if (!ws) return NULL;
if (wssz<=0) wssz = wcslen(ws);
int nSize = WideCharToMultiByte(cp, 0, ws, wssz, NULL, 0, NULL, NULL);
if (nSize<=0) return NULL;
char* buf = NULL;
if (flags==1) {
buf = malloc(nSize+4);
buf[0] = 0xEF;
buf[1] = 0xBB;
buf[2] = 0xBF;
buf += 3;
}
else buf = malloc(nSize+1);
nSize = WideCharToMultiByte(cp, 0, ws, wssz, buf, nSize, NULL, NULL);
if (flags==1) {
if (nSize>0) nSize+=3;
buf-=3;
}
if (nSize<=0) { free(buf); return NULL; }
if (sz) *sz=nSize;
buf[nSize]=0;
return buf;
}

static inline BOOL testUtf8rule (unsigned char** x, int n) {
int i = 0;
while (i<n && (*++(*x)&0xC0)==0x80) i++;
return i==n;
}

int guessEncoding (const unsigned char* ch) {
if (ch[0]==0xEF && ch[1]==0xBB && ch[2]==0xBF) return 65002; // UTF8-BOM
if (ch[0]==255 && ch[1]==254) return 65011; // Unicode little-endian with BOM
if (ch[0]==254 && ch[1]==255) return 65021; // Unicode big-endian with BOM
if (ch[1]==0 && ch[3]==0 && ch[5]==0) return 65010; // Probably unicode little-endian without BOM
if (ch[0]==0 && ch[2]==0 && ch[4]==0) return 65020; // Probably unicode big-endian without BOM
BOOL encutf = FALSE;
for (unsigned char* x = ch; *x; ++x) {
if (*x<0x80) continue;
if (*x>=0x81 && *x<=0x90) return 850; 
else if (*x==164) return 28605;
else if ((*x>=0x80 && *x<0xC0) || *x>=248) return CP_ACP;
else if (*x>=0xF0 && !testUtf8rule(&x, 3)) return CP_ACP;
else if (*x>=0xE0 && !testUtf8rule(&x, 2)) return CP_ACP;
else if (*x>=0xC0 && !testUtf8rule(&x, 1)) return CP_ACP;
encutf = TRUE;
}
return encutf? CP_UTF8 : CP_ACP;
}

wchar_t* unicodeSwitchEndianess (wchar_t* s, int l) {
union { struct { unsigned char a, b; }; short s; } u;
int i; for(i=0; i<l; i++) {
u.s = s[i];
unsigned char z = u.a;
u.a = u.b;
u.b = z;
s[i] = u.s;
}
return s;
}



static int str2enc (const char* str) {
for (const struct encoding* e = encodings; e->name; e++) {
if (!stricmp(e->name, str)) return e->cp;
}
if (!strnicmp(str, "CP", 2)) return strtol(str+2, NULL, 10);
else if (!strnicmp(str, "IBM", 3)) return strtol(str+3, NULL, 10);
else if (!strnicmp(str, "windows-", 8)) return strtol(str+8, NULL, 10);
else if (!strnicmp(str, "windows", 7)) return strtol(str+7, NULL, 10);
else return strtol(str, NULL, 10);
}

static int enc2str (int enc, char* buf, int buflen) {
for (const struct encoding* e = encodings; e->name; e++) {
if (enc==e->cp) return snprintf(buf, buflen, e->name, e->cp);
}
if (enc>=1250 && enc<=1258) return snprintf(buf, buflen, "Windows-%d", enc);
else if (enc<1200) return snprintf(buf, buflen, "IBM%d", enc);
else return snprintf(buf, buflen, "CP%d", enc);
}

void enumCP  (void) {
int count = 5, max = 8;
int* encs = malloc(max * sizeof(int));
encs[0] = 65002;
encs[1] = 65010;
encs[2] = 65011;
encs[3] = 65020;
encs[4] = 65021;
BOOL CALLBACK _cb (LPCTSTR cpNum) {
int enc = strtol(cpNum, NULL, 10);
encs[count++] = enc;
if (count>=max) {
max = max*3/2+1;
encs = realloc(encs, max*sizeof(int));
}
return TRUE;
}
int intcmp (int* a, int* b) {
return *a-*b;
}
EnumSystemCodePages(_cb, CP_INSTALLED);
qsort(encs, count, sizeof(int), intcmp);
printf("List of supported code pages\r\n%-6s %s\r\n", "Code", "Name");
for (int i=0; i<count; i++) {
char buf[64]={0};
enc2str(encs[i], buf, 63);
printf("%6d %s\r\n", encs[i], buf);
}}

void printHelp (void) {
printf(
"chenc - change encoding\r\n"
"chenc [infile]\r\n"
"chenc [infile] [outenc] {outfile}\r\n"
"chenc {-i [infile]|stdin} {-o [outfile]|stdout} {-s [sourceEncoding]} {-t {targetEncoding]}\r\n"
"chenc -l\r\n\r\n"
"Options :\r\n"
"-i\tInput file\r\n"
"-l\tList supported encodings\r\n"
"-s\tSource encoding (encoding of input file)\r\n"
"-t\tTarget encoding (desired encoding in output file)\r\n"
"-o\tOutput file\r\n\r\n"
"If no output file and encodings are given, chenc just try to guess the encoding of the input file and returns its guess without further action.\r\n"
"If no source encoding is given, chenc try to guess the encoding of the input file automatically\r\n"
"If no output file is given, chenc overwrites the input file."
);//
}

int main (int argc, char** argv) {
FILE *in=NULL, *out=NULL;
const char *infn = NULL, *outfn = NULL, *incps = NULL, *outcps = NULL;

if (argc<2) {
printHelp();
return 1;
}

for (int i=1; i<argc; i++) {
if (!strcmp(argv[i], "-i")) infn = argv[++i];
else if (!strcmp(argv[i], "-o")) outfn = argv[++i];
else if (!strcmp(argv[i], "-s")) incps = argv[++i];
else if (!strcmp(argv[i], "-t")) outcps = argv[++i];
else if (!strcmp(argv[i], "-l")) { enumCP(); return 0; }
else if (i==1) infn = argv[i];
else if (i==2) outcps = argv[i];
else if (i==3) outfn = argv[i];
else fprintf(stderr, "Warning: unrecognized parameter: %s\r\n", argv[i]);
}
if (!infn) infn = "stdin";
if (!strcmp(infn, "stdin")) in = (FILE*)stdin;
else in = fopen(infn, "rb");
if (!in) {
fprintf(stderr, "Couldn't open input file: %s\r\n", infn);
return 1;
}

int n, pos=0, buflen=32;
char* buf = malloc(buflen);
while ((n=fread(buf+pos, 1, buflen-pos, in))>0) {
pos+=n;
if (pos>=buflen -8) {
int nblen = buflen *3/2+1;
buf = realloc(buf, nblen);
buflen = nblen;
}}
fclose(in);
buf[pos+3] = buf[pos+2] = buf[pos+1] = buf[pos]=0;

int incp=0;
if (!incps) incp = guessEncoding(buf);
else incp = str2enc(incps);
if (incp<0) {
fprintf(stderr, "Unknown or unsupported input encoding: %s\r\n", incps);
return -1;
}

if (!incps && !outcps && !outfn) {
if (incp==0) incp = GetACP();
char cps[64]={0};
enc2str(incp, cps, 63);
printf("Guessed encoding for %s: %s (%d)\r\n", infn, cps, incp);
return 0;
}

wchar_t* wcs = NULL;
buflen = pos;
if (incp<-1 || incp==65020 || incp==65021 || incp==65010 || incp==65011) buflen/=2;
if (incp==65020 || incp==65021) unicodeSwitchEndianess(buf, buflen);
if (incp==65010 || incp==65020) wcs = buf;
else if (incp==65011 || incp==65021) wcs = buf+2;
else if (incp==65002) wcs = mb2wc(buf+3, buflen -3, CP_UTF8, &buflen);
else wcs = mb2wc(buf, buflen, incp, &buflen);

int outcp = -1;
const char* mbs = NULL;
if (!outcps) outcps = "missing";
if (outcps) outcp = str2enc(outcps);
if (outcp<0) {
fprintf(stderr, "Unknown, unspecified or unsupported output encoding: %s\r\n", outcps);
return 1;
}
if (incp==outcp) return 0;
if (outcp==65011 || outcp==65021) {
wcs = realloc(wcs, sizeof(wchar_t)*(buflen+2));
memmove(wcs+1, wcs, sizeof(wchar_t)*buflen);
wcs[++buflen]=0;
wcs[0] = 0xFEFF;
}
if (outcp==65020 || outcp==65021) unicodeSwitchEndianess(wcs, buflen);
if (outcp==65010 || outcp==65011 || outcp==65020 || outcp==65021) { mbs = wcs; buflen*=2; }
else if (outcp==65002) mbs = wc2mb(wcs, buflen, CP_UTF8, &buflen, 1);
else mbs = wc2mb(wcs, buflen, outcp, &buflen, 0);

if (!outfn) outfn = infn;
if (!strcmp(outfn, "stdout") || !strcmp(outfn, "stdin")) out = (FILE*)stdout;
else out = fopen(outfn, "wb");
if (!out) {
fprintf(stderr, "Couldn't open output file: %s\r\n", outfn);
return 1;
}
fwrite(mbs, buflen, 1, out);
fclose(out);
return 0;
}
