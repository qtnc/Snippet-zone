#include<string.h>
#include<stdio.h>
#include<stdlib.h>

typedef struct {
char SFPv[4];
unsigned int version;
unsigned int fileCount;
unsigned int reserved;
} PackFileHeader;

typedef struct {
char SFPv[4];
unsigned int filenameLength;
unsigned int reserved;
unsigned int dataLength;
} PackItemHeader;

int main (int argc, char** argv) {
if (argc<2) {
fprintf(stderr, "Usage: bgtunpack <pack_file>\n");
return 1;
}
FILE* fp = fopen(argv[1], "rb");
if (!fp) {
fprintf(stderr, "Couldn't open %s\n", argv[1]);
return 1;
}
PackFileHeader pack;
PackItemHeader item;
if (!fread(&pack, 1, sizeof(pack), fp)) {
fprintf(stderr, "Unable to open %s: read error\n", argv[1]);
return 1;
}
if (strncmp(pack.SFPv, "SFPv", 4) || pack.version!=1) {
fseek(fp, 0, SEEK_SET);
int len = filelength(fileno(fp));
char* buf = malloc(len);
if (!fread(buf, 1, len, fp)) return 1;
for (int i=0; i<len-sizeof(pack); i++) {
PackFileHeader* p = (PackFileHeader*)(buf+i);
if (p->version==1 && !strncmp(p->SFPv, "SFPv", 4)) {
printf("Pack data found at offset %d\n", i);
pack = *p;
fseek(fp, i+sizeof(pack), SEEK_SET);
free(buf);
break;
}}}
if (strncmp(pack.SFPv, "SFPv", 4) || pack.version!=1) {
fprintf(stderr, "%s isn't a pack file\n", argv[1]);
return 1;
}
printf("%s contains %d files.\n", argv[1], pack.fileCount);
for (int i=0; i<pack.fileCount; i++) {
if (!fread(&item, 1, sizeof(item), fp) || strncmp(item.SFPv, "SFPv", 4)) {
fprintf(stderr, "Problem extracting file #%d: not a pack file item\n", i+1);
return 1;
}
char filename[item.filenameLength+1];
if (!fread(filename, 1, item.filenameLength, fp)) {
fprintf(stderr, "Problem extracting file #%d: unable to extract file name\n", i+1);
return 1;
}
filename[item.filenameLength]=0;
printf("Extracting %s (%d bytes)\n", filename, item.dataLength);
char* buf = malloc(item.dataLength);
if (!buf) {
fprintf(stderr, "Unable to extract %s: out of memory (%d bytes)\n", filename, item.dataLength);
return 1;
}
if (!fread(buf, 1, item.dataLength, fp)) {
fprintf(stderr, "Unable to extract %s: read error\n", filename);
return 1;
}
FILE* out = fopen(filename, "wb");
if (!out) {
fprintf(stderr, "Unable to extract and create %s.\n", filename);
return 1;
}
fwrite(buf, 1, item.dataLength, out);
fclose(out);
free(buf);
}
printf("Successfully unpacked %s\n", argv[1]);
return 0;
}

