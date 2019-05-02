#include "bass++.hpp"
#include "bass.h"
#include "bass_fx.h"
#include "bass-addon.h"
#include<cstdio>
#include<iostream>
#include<cmath>
using namespace std;

#define Check(X) CheckError(X, __FILE__, __LINE__)

#ifdef __WIN32
#undef CreateFile
#define CurrentWindow GetForegroundWindow()
#else
#define CurrentWindow 0
#endif

#ifndef bassfunc
const BASS_FUNCTIONS *bassfunc=NULL;
#endif

static void __attribute__((constructor)) _____bassAddonInit  () {
GetBassFunc();
}

const char* ERROR_MESSAGES[] = {
"Mystery problem!",
"OK",
"Insufficient memory",
"Couldn't open file",
"Couldn't find an usable driver",
"Sample buffer lost",
"Invalid handle",
"Unsupported format",
"Invalid seeking position",
"Device hasn't been initialized",
"Audio output hasn't started",
"SSL support is unavailable",
nullptr, nullptr, nullptr,
"Already in use / initialized / paused",
nullptr, nullptr, nullptr,
"No available free channel",
"Illegal type specified",
"Illegal parameters specified",
"No available 3D audio support",
"No available EAX support",
"Invalid device specified",
"Currently not playing",
"Illegal sample rate",
"Handle isn't a valid file stream",
"No available hardware channel",
"No available module sequence data",
"No available Internet connection",
"Couldn't create file",
"Effects are unavailable",

nullptr, nullptr,
"Requested data/action is unavailable",
"The handle isn't a valid decoding channel",
"A newer version of DirectX is required",
"Connection timed out",
"Unsupported file format",
"Unavailable speaker",
"Invalid BASS version",
"Unavailable or unsupported codec",
"The channel has ended",
"The device is busy"
};

namespace BASS {

thread_local int errorReportMode = 0;
function<bool(const Exception&)> errorCallback = nullptr;

template <class T> inline T CheckError (T result, const char* filename, int line) {
if (!result) {
int err = BASS_ErrorGetCode();
#ifdef DEBUG
fprintf(stderr, "Error %d at %s:%d: %s\n", err, filename, line, ERROR_MESSAGES[err+1]);
#endif
if (!errorCallback || errorCallback(Exception(err))) {
switch(errorReportMode) {
case ERRORS_LOG: fprintf(stderr, "BASS Error %d: %s\r\n", err, ERROR_MESSAGES[err+1]); break;
case ERRORS_THROW: throw Exception(err);
default: break;
}}
}
return result;
}

int export GetErrorReportMode () { return errorReportMode; }
void export SetErrorReportMode (int mode) { errorReportMode=mode; }
void export SetErrorReportCallback (const function<bool(const Exception&)>& cb) { errorCallback=cb; }

const char* export Exception::GetErrorMessage () const { return ERROR_MESSAGES[code+1]; }

bool export Init (int device, DWORD freq, DWORD flags) {
return Check(BASS_Init(device, freq, flags, CurrentWindow, nullptr));
}

bool export Free () {
return Check(BASS_Free());
}

bool export SetConfig (DWORD option, DWORD value) {
return Check(BASS_SetConfig(option, value));
}

DWORD export GetConfig (DWORD option) {
return BASS_GetConfig(option);
}

bool export SetDevice (int device) {
return Check(BASS_SetDevice(device));
}

int export GetDevice () {
return BASS_GetDevice();
}

Stream export Stream::CreateFile (const string& filename, DWORD flags) {
return Check(BASS_StreamCreateFile(false, filename.c_str(), 0, 0, flags));
}

Stream export Stream::CreateFile (const wstring& filename, DWORD flags) {
return Check(BASS_StreamCreateFile(false, filename.c_str(), 0, 0, flags | BASS_UNICODE));
}

Stream export Stream::CreateURL (const std::string& url, DWORD flags) {
return Check(BASS_StreamCreateURL(url.c_str(), 0, flags, nullptr, nullptr));
}

Stream export Stream::CreateURL (const std::wstring& url, DWORD flags) {
return Check(BASS_StreamCreateURL(url.c_str(), 0, flags | BASS_UNICODE, nullptr, nullptr));
}

template <class T> inline T* make_copy (const T& x) {
return new T(x);
}

template <class T> void CALLBACK deleteSyncProc (HSYNC sync, ::DWORD channel, ::DWORD data, void* ptr) {
delete reinterpret_cast<T*>(ptr);
}

template <class T> inline void addDeleteSync (DWORD handle, T* ptr) {
Check(BASS_ChannelSetSync(handle, SYNC_FREE, 0, &deleteSyncProc<T>, ptr));
}

static void CALLBACK forwardingDownloadProc (const void* buffer, ::DWORD length, void* udata) {
auto& downloadProc = *reinterpret_cast<function<void(const void*, uint32_t)>*>(udata);
downloadProc(buffer, length);
}

static ::DWORD CALLBACK forwardingStreamProc (::DWORD handle, void* buffer, ::DWORD length, void* udata) {
auto& callback = *reinterpret_cast<function<DWORD(Stream, void*, DWORD)>*>(udata);
int re = callback(handle, buffer, length);
if (re<0) return BASS_STREAMPROC_END;
return re;
}

Stream export Stream::CreateURL (const string& url, DWORD flags, const function<void(const void*, uint32_t)>& downloadProc) {
auto dlproc = make_copy(downloadProc);
auto stream = Check(BASS_StreamCreateURL(url.c_str(), 0, flags, forwardingDownloadProc, dlproc));
addDeleteSync(stream, dlproc);
return stream;
}

Stream export Stream::CreateURL (const wstring& url, DWORD flags, const function<void(const void*, DWORD)>& downloadProc) {
auto dlproc = make_copy(downloadProc);
auto stream = Check(BASS_StreamCreateURL(url.c_str(), 0, flags | BASS_UNICODE, forwardingDownloadProc, dlproc));
addDeleteSync(stream, dlproc);
return stream;
}

Stream export Stream::CreateMem (const void* data, QWORD length, DWORD flags) {
return Check(BASS_StreamCreateFile(true, data, 0, length, flags));
}

Stream export Stream::CreateCallback (DWORD freq, DWORD channels, DWORD flags, const std::function<DWORD(Stream, void* DWORD)>& callback) {
auto cbcopy = make_copy(callback);
auto stream = Check(BASS_StreamCreate(freq, channels, flags, forwardingStreamProc, cbcopy));
addDeleteSync(stream, cbcopy);
return stream;
}

Stream export Stream::CreateDummy (DWORD freq, DWORD channels, DWORD flags) {
return Check(BASS_StreamCreate(freq, channels, flags, STREAMPROC_DUMMY, nullptr));
}

Stream export Stream::GetDeviceOutputStream () {
return Check(BASS_StreamCreate(48000, 2, 0, STREAMPROC_DEVICE, nullptr));
}

Stream export Stream::GetDevice3DOutputStream () {
return Check(BASS_StreamCreate(48000, 2, 0, STREAMPROC_DEVICE_3D, nullptr));
}

MusicStream export MusicStream::CreateFile (const string& filename, DWORD flags, DWORD freq) {
return Check(BASS_MusicLoad(false, filename.c_str(), 0, 0, flags, freq));
}

MusicStream export MusicStream::CreateFile (const wstring& filename, DWORD flags, DWORD freq) {
return Check(BASS_MusicLoad(false, filename.c_str(), 0, 0, flags | BASS_UNICODE, freq));
}

MusicStream export MusicStream::CreateMem (const void* data, DWORD length, DWORD flags, DWORD freq) {
return Check(BASS_MusicLoad(true, data, 0, length, flags, freq));
}

bool export Channel::Play (bool restart) {
return Check(BASS_ChannelPlay(handle, restart));
}

bool export Channel::Pause () {
return Check(BASS_ChannelPause(handle));
}

bool export Channel::Stop () {
return Check(BASS_ChannelStop(handle));
}

double export Channel::GetAttribute (int attr) {
float f;
int attr0 = attr;
if (attr==ATTR_RATE) attr0=ATTR_FREQ;
else if (attr==ATTR_RAW_PITCH) attr0=ATTR_PITCH;
Check(BASS_ChannelGetAttribute(handle, attr0, &f));
switch(attr) {
case ATTR_RATE: {
BASS_CHANNELINFO info;
Check(BASS_ChannelGetInfo(handle, &info));
return f / info.freq;
}
case ATTR_SPEED: return (100.0 + f) / 100.0;
case ATTR_RAW_PITCH: return pow(2, f/12.0);
default:  return f;
}}

static void transformAttrVal (DWORD handle, int& attr, double& value) {
switch(attr){
case ATTR_RATE: {
attr = ATTR_FREQ;
BASS_CHANNELINFO info;
Check(BASS_ChannelGetInfo(handle, &info));
value *= info.freq;
}break;
case ATTR_SPEED: value = 100 * (value -1); break;
case ATTR_RAW_PITCH: attr=ATTR_PITCH; value = 12 * log(value)/log(2); break;
default: break;
}}

bool export Channel::SetAttribute (int attr, double value) {
transformAttrVal(handle, attr, value);
return Check(BASS_ChannelSetAttribute(handle, attr, value));
}

bool export Channel::SlideAttribute (int attr, double time, double value) {
transformAttrVal(handle, attr, value);
return Check(BASS_ChannelSlideAttribute(handle, attr, value, 1000*time));
}

bool export Channel::IsSliding (int attr) {
double value = 1;
transformAttrVal(handle, attr, value);
return BASS_ChannelIsSliding(handle, attr);
}

double export Channel::GetPosition () {
return BASS_ChannelBytes2Seconds(handle, BASS_ChannelGetPosition(handle, BASS_POS_BYTE));
}

bool export Channel::SetPosition (double pos) {
return Check(BASS_ChannelSetPosition(handle, BASS_ChannelSeconds2Bytes(handle, pos), BASS_POS_BYTE));
}

double export Channel::GetLength () {
return BASS_ChannelBytes2Seconds(handle, BASS_ChannelGetLength(handle, BASS_POS_BYTE));
}

int export Channel::GetState () const {
return BASS_ChannelIsActive(handle);
}

DWORD export Channel::Flags (DWORD flags, DWORD mask) {
return BASS_ChannelFlags(handle, flags, mask);
}

bool export Channel::GetInfo (uint32_t* ctype , uint32_t* freq , uint32_t* channels , uint32_t* resolution ) {
BASS_CHANNELINFO info;
if (!Check(BASS_ChannelGetInfo(handle, &info))) return false;
if (ctype) *ctype = info.ctype;
if (freq) *freq = info.freq;
if (channels) *channels = info.chans;
if (resolution) *resolution = info.origres;
return true;
}



bool export Stream::Free () {
return Check(BASS_StreamFree(handle));
}

bool export Stream::SetDevice (int device) {
return Check(BASS_ChannelSetDevice(handle, device));
}

int export Stream::GetDevice () {
return BASS_ChannelGetDevice(handle);
}

bool export GetDeviceInfo (int device, DeviceInfo& info) {
BASS_DEVICEINFO di;
if (!Check(BASS_GetDeviceInfo(device, &di))) return false;
info.name = di.name;
info.flags = di.flags;
return true;
}

bool export IsDeviceInitialized (int device) {
BASS_DEVICEINFO info;
if (!Check(BASS_GetDeviceInfo(device, &info))) return false;
return info.flags & BASS_DEVICE_INIT;
}

static void CALLBACK VFileCloseProc (void* ptr) {
reinterpret_cast<VFile*>(ptr) ->close();
}

::QWORD CALLBACK VFileLenProc (void* ptr) {
auto len = reinterpret_cast<VFile*>(ptr) ->getLength();
if (len<0) return 0;
else return len;
}

static ::DWORD CALLBACK VFileReadProc (void* buffer, ::DWORD length, void* ptr) {
return reinterpret_cast<VFile*>(ptr) ->read(buffer, length);
}

static BOOL CALLBACK VFileSeekProc (::QWORD pos, void* ptr) {
return reinterpret_cast<VFile*>(ptr) ->seek(pos);
}

Stream export Stream::CreateFile (VFile& file, bool buffered, DWORD flags) {
BASS_FILEPROCS procs = { VFileCloseProc, VFileLenProc, VFileReadProc, VFileSeekProc };
return Check(BASS_StreamCreateFileUser(buffered? STREAMFILE_BUFFER : STREAMFILE_NOBUFFER, flags, &procs, &file));
}

Sample export Sample::CreateFile (const string& filename, DWORD max, DWORD flags) {
return Check(BASS_SampleLoad(false, filename.c_str(), 0, 0, max, flags));
}

Sample export Sample::CreateFile (const wstring& filename, DWORD max, DWORD flags) {
return Check(BASS_SampleLoad(false, filename.c_str(), 0, 0, max, flags | BASS_UNICODE));
}

Sample export Sample::CreateMem (const void* data, QWORD length, DWORD max, DWORD flags) {
return Check(BASS_SampleLoad(true, data, 0, length, max, flags));
}

SampleChannel export Sample::CreateChannel (bool onlyNew) {
return Check(BASS_SampleGetChannel(handle, onlyNew));
}

vector<SampleChannel> export Sample::GetAllChannels () {
vector<SampleChannel> channels;
int count = BASS_SampleGetChannels(handle, nullptr);
if (count>0) {
::DWORD ch[count];
Check(BASS_SampleGetChannels(handle, ch));
for (int i=0; i<count; i++) channels.push_back(ch[i]);
}
return channels;
}

bool export Sample::StopAll () {
return Check(BASS_SampleStop(handle));
}

bool export Sample::Free () {
return Check(BASS_SampleFree(handle));
}

SampleInfo export Sample::GetInfo () {
SampleInfo info;
Check(BASS_SampleGetInfo(handle, reinterpret_cast<BASS_SAMPLE*>(&info)));
return info;
}

bool export Sample::SetInfo (const SampleInfo& info) {
return Check(BASS_SampleSetInfo(handle, reinterpret_cast<const BASS_SAMPLE*>(&info)));
}

DWORD export Sample::GetData (void* buffer) {
if (!buffer) return GetInfo().length;
return Check(BASS_SampleGetData(handle, buffer));
}

bool export Sample::SetData (const void* buffer) {
return Check(BASS_SampleSetData(handle, buffer));
}

Sample export SampleChannel::GetSample () {
BASS_CHANNELINFO info;
Check(BASS_ChannelGetInfo(handle, &info));
return info.sample;
}

Sample export Sample::CreateRaw (const void* data, DWORD length, DWORD freq, DWORD channels, DWORD max, DWORD flags) {
auto handle = Check(BASS_SampleCreate(length, freq, channels, max, flags));
if (handle) Check(BASS_SampleSetData(handle, data));
return handle;
}

Plugin export Plugin::Load (const std::string& filename) {
return Check(BASS_PluginLoad(filename.c_str(), 0));
}

Plugin export Plugin::Load (const std::wstring& filename) {
return Check(BASS_PluginLoad(filename.c_str(), BASS_UNICODE));
}

bool export Plugin::Free () {
return Check(BASS_PluginFree(handle));
}

DWORD export Plugin::GetType (int format) {
auto& pi = *Check(BASS_PluginGetInfo(handle));
return format>=0 && format<pi.formatc? pi.formats[format].ctype : -1;
}

std::vector<std::pair<std::string, std::string>> export Plugin::GetFormats () {
auto& pi = *Check(BASS_PluginGetInfo(handle));
vector<pair<string,string>> formats;
for (int i=0; i<pi.formatc; i++) {
formats.emplace_back( pi.formats[i].name, pi.formats[i].exts );
}
return formats;
}

void CALLBACK forwardSyncCallback (HSYNC sync, ::DWORD channel, ::DWORD data, void* udata) {
auto& func = *reinterpret_cast<function<void(Sync, Channel, int)>*>(udata);
func(sync, channel, data);
}

void CALLBACK forwardDSPCallback (HDSP dsp, ::DWORD channel, void* data, ::DWORD length, void* udata) {
auto& func = *reinterpret_cast<function<void(DSP, Channel, const void*, DWORD)>*>(udata);
func(DSP(dsp, channel), channel, data, length);
}

Sync export Channel::SetSync (DWORD type, QWORD param, const function<void(Sync, Channel, int)>& callback) {
auto proc = make_copy(callback);
auto result = Check(BASS_ChannelSetSync(handle, type, param, forwardSyncCallback, proc));
addDeleteSync(handle, proc);
return result;
}

bool export Channel::RemoveSync (const Sync& sync) { 
return Check(BASS_ChannelRemoveSync(handle, sync.GetHandle()));
}

DSP export Channel::SetDSP (const std::function<void(DSP, Channel, void*, DWORD)>& callback, int priority) {
auto proc = make_copy(callback);
auto result = Check(BASS_ChannelSetDSP(handle, forwardDSPCallback, proc, priority));
addDeleteSync(handle, proc);
return DSP( result, *this );
}

bool export Channel::RemoveDSP (const DSP& dsp) {
return Check(BASS_ChannelRemoveDSP(handle, dsp.GetHandle()));
}

bool export DSP::SetPriority (int priority) {
return Check(BASS_FXSetPriority(handle, priority));
}

DWORD export Channel::GetData (void* buffer, DWORD length) {
return BASS_ChannelGetData(handle, buffer, length);
}

const char* export Channel::GetTags (DWORD type) {
return BASS_ChannelGetTags(handle, type);
}

void export AppendDoubleNullTerminatedList (std::vector<std::string>& list, const char* dntstr) {
if (dntstr) while(*dntstr) {
string s = dntstr;
list.push_back(s);
dntstr += s.size() +1;
}}

export TempoStream::TempoStream (const Stream& source, DWORD flags):
Stream(Check(BASS_FX_TempoCreate(source.handle, flags)))
{}

Stream export TempoStream::GetSource () {
return Check(BASS_FX_TempoGetSource(handle));
}

static void CALLBACK DLPCatchHTTPStatus (const void* vbuffer, ::DWORD length, void* status) {
const char* buffer = reinterpret_cast<const char*>(vbuffer);
if (!length && buffer && !strnicmp(buffer,"HTTP", 4)) {
sscanf(strchr(buffer, ' '), "%d", reinterpret_cast<int*>(status));
}}

export File::File (const std::string& url): file(nullptr), status(-1) {
file = bassfunc->file.OpenURL(url.c_str(), 0, BASS_STREAM_STATUS, DLPCatchHTTPStatus, &status, 0);
if (status<200 || status>=300) Close();
else if (!file) status = BASS_ErrorGetCode();
}

void export File::Close () {
if (file) bassfunc->file.Close(file);
file = nullptr;
}


int64_t export File::GetLength () {
return bassfunc->file.GetPos(file, BASS_FILEPOS_END);
}

int64_t export File::GetDownloadedLength () {
return bassfunc->file.GetPos(file, BASS_FILEPOS_DOWNLOAD);
}

int export File::Read (void* buf, int len) {
return bassfunc->file.Read(file, buf, len);
}

bool export FXPushParams (DWORD fx, const void* ptr) {
return Check(BASS_FXSetParameters(fx, ptr));
}

bool export FXPullParams (DWORD fx, void* ptr) {
return Check(BASS_FXGetParameters(fx, ptr));
}

bool export FXReset (DWORD fx) {
return Check(BASS_FXReset(fx));
}

DWORD export FXInstall (DWORD handle, int type, int priority) {
return Check(BASS_ChannelSetFX(handle, type, priority));
}

bool export FXDeinstall (DWORD handle, DWORD fx) {
return Check(BASS_ChannelRemoveFX(handle, fx));
}

#define V(X) reinterpret_cast<BASS_3DVECTOR*>(X)

bool export Update3D () {
BASS_Apply3D();
return true;
}

static inline BASS_3DVECTOR* dtobv (double* d, BASS_3DVECTOR& v) {
if (!d) return nullptr;
v.x = d[0];
v.y = d[1];
v.z = d[2];
return &v;
}

static inline void bvtod (const BASS_3DVECTOR& v, double* d) {
if (!d) return;
d[0] = v.x;
d[1] = v.y;
d[2] = v.z;
}

bool export Set3DPosition (float* pos, float* vel, float* front, float* top) {
return Check(BASS_Set3DPosition(V(pos), V(vel), V(front), V(top)));
}

bool export Get3DPosition (float* pos, float* vel, float* front, float* top) {
return Check(BASS_Get3DPosition(V(pos), V(vel), V(front), V(top)));
}

bool export Set3DPosition (double* pos, double* vel, double* front, double* top) {
BASS_3DVECTOR vpos, vvel, vfront, vtop,
*fpos = dtobv(pos, vpos), *fvel = dtobv(vel, vvel), *ffront = dtobv(front, vfront), *ftop = dtobv(top, vtop);
return Check(BASS_Set3DPosition(fpos, fvel, ffront, ftop));
}
		bool export Get3DPosition (double* pos, double* vel, double* front, double* top) {
BASS_3DVECTOR vpos, vvel, vfront, vtop;
bool re = Check(BASS_Get3DPosition(&vpos, &vvel, &vfront, &vtop));
bvtod(vpos, pos); bvtod(vvel, vel); bvtod(vfront, front); bvtod(vtop, top);
return re;
}

bool export Set3DFactors (double distance, double rolloff, double doppler) {
return Check(BASS_Set3DFactors(distance, rolloff, doppler));
}

bool export Get3DFactors (float* distance, float* rolloff, float* doppler) {
return Check(BASS_Get3DFactors(distance, rolloff, doppler));
}

bool export Get3DFactors (double* distance, double* rolloff, double* doppler) {
float fd=-1, fr=-1, fp=-1;
bool re = Check(BASS_Get3DFactors(&fd, &fr, &fp));
if (distance) *distance=fd;
if (rolloff) *rolloff=fr;
if (doppler) *doppler=fp;
return re;
}

bool export Channel::Set3DPosition (float* pos, float* ori, float* vel) {
return Check(BASS_ChannelSet3DPosition(handle, V(pos), V(ori), V(vel)));
}

bool export Channel::Get3DPosition (float* pos, float* ori, float* vel) {
return Check(BASS_ChannelGet3DPosition(handle, V(pos), V(ori), V(vel)));
}

bool export Channel::Set3DPosition (double* pos, double* ori, double* vel) {
BASS_3DVECTOR vpos, vori, vvel,
*fpos = dtobv(pos, vpos), *fori = dtobv(ori, vori), *fvel = dtobv(vel, vvel);
return Check(BASS_ChannelSet3DPosition(handle, fpos, fori, fvel));
}

bool export Channel::Get3DPosition (double* pos, double* ori, double* vel) {
BASS_3DVECTOR vpos, vori, vvel;
bool re = Check(BASS_ChannelGet3DPosition(handle, &vpos, &vori, &vvel));
bvtod(vpos, pos); bvtod(vori, ori); bvtod(vvel, vel);
return re;
}

bool export Channel::Set3DAttributes (int mode, double minDist, double maxDist, int innerAngle, int outerAngle, double outerVol) {
return Check(BASS_ChannelSet3DAttributes(handle, mode, minDist, maxDist, innerAngle, outerAngle, outerVol));
}

bool export Channel::Get3DAttributes (int* mode, float* minDist, float* maxDist, int* innerAngle, int* outerAngle, float* outerVol) {
return Check(BASS_ChannelGet3DAttributes(handle, reinterpret_cast<::DWORD*>(mode), minDist, maxDist, reinterpret_cast<::DWORD*>(innerAngle), reinterpret_cast<::DWORD*>(outerAngle), outerVol));
}

bool export Channel::Get3DAttributes (int* mode, double* minDist, double* maxDist, int* innerAngle, int* outerAngle, double* outerVol) {
float fminDist=-1, fmaxDist=-1, fouterVol=-1;
bool re = Check(BASS_ChannelGet3DAttributes(handle, reinterpret_cast<::DWORD*>(mode), &fminDist, &fmaxDist, reinterpret_cast<::DWORD*>(innerAngle), reinterpret_cast<::DWORD*>(outerAngle), &fouterVol));
if (minDist) *minDist = fminDist;
if (maxDist) *maxDist = fmaxDist;
if (outerVol) *outerVol=fouterVol;
return re;
}

#undef V

} // namespace BASS
