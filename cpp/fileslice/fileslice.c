#include<stdio.h>
#include<io.h>
#include<stdlib.h>

int main (int argc, char** argv) {
if (argc<5) {
fprintf(stderr, "Usage: fileslice [dest] [source] [offset_from] [offset_to]\n");
return 1;
}
const char* dst = argv[1];
const char* src = argv[2];
int from = atoi(argv[3]);
int to = atoi(argv[4]);
FILE* in = fopen(src, "rb");
if (!in) {
fprintf(stderr, "Couldn't open %s for reading.\n", src);
return 1;
}
int fsize = filelength(fileno(in));
if (to<0) to+=fsize;
if (from<0) from+=fsize;
if (from>to) {
fprintf(stderr, "from_offset is greater than to_offset: %d>%d\n", from, to);
return 1;
}
if (fsize<to) {
fprintf(stderr, "%s has a size of %d bytes; offset %d is invalid.\n", src, fsize, to);
return 1;
}
FILE* out = fopen(dst, "wb");
if (!out) {
fprintf(stderr, "Couldn't open %s for writing.\n", dst);
return 1;
}
fseek(in, from, SEEK_SET);
int length = to-from;
char* buf = malloc(length);
if (!buf) {
fprintf(stderr, "Unsufficient memory to store %d bytes.\n", length);
return 1;
}
if (
fread(buf, length, 1, in)
&& fwrite(buf, length, 1, out)
)
printf("Successfully extracted %d bytes from %s from offset %d to %d and written to %s.\n", length, src, from, to, dst);
else {
fprintf(stderr, "I/O Error\n");
return 1;
}
fclose(in);
fclose(out);
return 0;
}
