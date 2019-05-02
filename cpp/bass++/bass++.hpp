#ifndef _____BASSPLUSPLUS_HPP_1
#define _____BASSPLUSPLUS_HPP_1
#include<string>
#include<vector>
#include<cstdint>
#include<functional>

#ifdef __WIN32
#define export __declspec(dllexport)
#else
#define export 
#endif

#ifdef CreateFile
#undef CreateFile
#endif

#pragma GCC diagnostic ignored "-Wsign-compare"

namespace BASS {
typedef uint16_t WORD;
typedef uint32_t DWORD;
typedef uint64_t QWORD;

enum {
// Channel activte states
STOPPED = 0,
PLAYING = 1,
STALLED = 2,
PAUSED = 3,

// Shortened forms for most used flags
MONO = 2,
LOOP = 4,
OLD_FX = 128,
FLOAT = 256,
FREESOURCE = 0x10000,
PRESCAN = 0x20000,
AUTOFREE = 0x40000,
DECODE = 0x200000,

// Channel attributes
ATTR_FREQ = 1,
ATTR_VOL = 2,
ATTR_PAN = 3,
ATTR_NOBUFFER = 5,
ATTR_VBR = 6,
ATTR_CPU = 7,
ATTR_SRC = 8,
ATTR_NET_RESUME = 9,
ATTR_NORAMP = 11,
ATTR_BITRATE = 12,
ATTR_MUSIC_AMPLIFY = 0x100,
ATTR_MUSIC_PANSEP = 0x101,
ATTR_MUSIC_PSCALER = 0x102,
ATTR_MUSIC_BPM = 0x103,
ATTR_MUSIC_SPEED = 0x104,
ATTR_MUSIC_VOL_GLOBAL = 0x105,
ATTR_MUSIC_ACTIVE = 0x106,
ATTR_MUSIC_VOL_CHAN = 0x200,
ATTR_MUSIC_VOL_INST = 0x300,
ATTR_SPEED = 0x10000,
ATTR_PITCH = 0x10001,
ATTR_RAW_PITCH = 0xFF,
ATTR_RATE = 0xFE,

// 3D modes
MODE_3D_NORMAL = 0,
MODE_3D_RELATIVE = 1,
MODE_3D_OFF = 2,

// Sync types
SYNC_POS = 0,
SYNC_END = 2,
SYNC_META = 4,
SYNC_SLIDE = 5,
SYNC_STALL = 6,
SYNC_DOWNLOAD = 7,
SYNC_FREE = 8,
SYNC_SETPOS = 11,
SYNC_MUSICPOS = 10,
SYNC_MUSICINST = 1,
SYNC_MUSICFX = 3,
SYNC_OGG_CHANGE = 12,
SYNC_MIXTIME = 0x40000000,
SYNC_ONETIME = 0x80000000,

// Sample flags
SAMPLE_8BITS = 1,
SAMPLE_MONO = 2,
SAMPLE_LOOP = 4,
SAMPLE_3D = 8,
SAMPLE_SOFTWARE = 16,
SAMPLE_MUTEMAX = 32,
SAMPLE_OLD_FX = 128,
SAMPLE_FLOAT = 256,
SAMPLE_OVER_VOL = 0x10000,
SAMPLE_OVER_POS = 0x20000,
SAMPLE_OVER_DIST = 0x30000,

// Stream flags
STREAM_MONO = 2,
STREAM_LOOP = 4,
STREAM_3D = 8,
STREAM_OLD_FX = 128,
STREAM_FLOAT = 256,
STREAM_FREESOURCE = 0x10000,
STREAM_PRESCAN = 0x20000,
STREAM_AUTOFREE = 0x40000,
STREAM_RESTRATE = 0x80000,
STREAM_BLOCK = 0x100000,
STREAM_DECODE = 0x200000,
STREAM_STATUS = 0x800000,
STREAM_ASYNC_FILE = 0x40000000,

// Record flags
RECORD_PAUSED = 0x8000,
RECORD_ECHOCANCEL = 0x2000,
RECORD_AGC = 0x4000,

// Music flags
MUSIC_RAMP = 0x200,
MUSIC_RAMPS = 0x400,
MUSIC_SURROUND = 0x800,
MUSIC_SURROUND2 = 0x1000,
MUSIC_FT2PAN = 0x2000,
MUSIC_FT2MOD = 0x2000,
MUSIC_PT1MOD = 0x4000,
MUSIC_NONINTER = 0x10000,
MUSIC_SYNCINTER = 0x800000,
MUSIC_POSRESET = 0x8000,
MUSIC_POSRESETEX = 0x400000,
MUSIC_STOPBACK = 0x80000,
MUSIC_NOSAMPLE = 0x100000,
MUSIC_PRESCAN = 0x20000,
MUSIC_AUTOFREE = 0x40000,

// Channel types
TYPE_SAMPLE = 1,
TYPE_RECORD = 2,
TYPE_STREAM = 0x10000,
TYPE_OGG = 0x10002,
TYPE_MP1 = 0x10003,
TYPE_MP2 = 0x10004,
TYPE_MP3 = 0x10005,
TYPE_AIFF = 0x10006,
TYPE_CA = 0x10007,
TYPE_MF = 0x10008,
TYPE_WAV = 0x40000, 
TYPE_WAV_PCM = 0x50001,
TYPE_WAV_FLOAT = 0x50003,
TYPE_MOD = 0x20000,
TYPE_MTM = 0x20001,
TYPE_S3M = 0x20002,
TYPE_XM = 0x20003,
TYPE_IT = 0x20004,
TYPE_MO3 = 0x00100,

// Tag types
TAGS_ID3 = 0, // TAG_ID3 structure
TAGS_ID3V2 = 1, // variable length structure
TAG_OGG = 2, // OGG comments: double-null-terminated  string list
TAG_HTTP = 3, // HTTP headers: double-null-terminated  string list
TAG_ICY = 4, // ICY headers: double-null-terminated  string list
TAG_META = 5, // ICY meta-data: double-null-terminated  string list
TAG_APE = 6, // APE: double-null-terminated  string list
TAG_MP4 = 7, // MP4/iTune: double-null-terminated  string list
TAG_WMA = 8, // WMA: double-null-terminated  string list
TAG_VENDOR = 9, // OGG vendor: null-terminated string
TAG_LYRIC3 = 10, // Lyric3: null-terminated string
TAG_CA = 11, // CoreAudio 
TAG_MF = 13, // MediaFoundation: double-null-terminated string list
TAG_WAVEFORMAT = 14, // WAVEFORMATEX structure
TAG_RIFF = 0x100, // RIFF INFO: double-null-terminated string list
TAG_RIFF_BEXT = 0x101, // RIFF BEXT: TAG_BEXT structure
TAG_RIFF_CART = 0x102, // RIFF CART: TAG_CART structure
TAG_RIFF_DISP = 0x103, // RIF DISP: null-terminated string
TAG_RIFF_CUE = 0x104, // RIFF CUE: TAG_CUE structure
TAG_RIFF_SMPL = 0x105, // RIFF SMPL: TAG_SMPL structure
TAG_APE_BINARY = 0x1000, // APE Binary: TAG_APE_BINARY structure
TAG_MOD_NAME = 0x10000, // Module name
TAG_MOD_MESSAGE = 0x10001, // Module message
TAG_MOD_ORDER = 0x10002, // Module order list: uint8_t list of pattern numbers
TAG_MOD_AUTHOR = 0x10003, // Module author
TAG_MOD_INSTRUMENT = 0x10100, // Module instrument name #
TAG_MOD_SAMPLE = 0x10300, // Module sample name #

// Init flags
DEVICE_8BITS = 1,
DEVICE_MONO = 2,
DEVICE_3D = 4,
DEVICE_16BITS = 8,
DEVICE_LATENCY = 0x100,
DEVICE_FREQ = 0x4000,
DEVICE_STEREO = 0x8000,

// GetDeviceInfo flags
DEVICE_ENABLED = 1,
DEVICE_DEFAULT = 2,
DEVICE_INIT = 8,

// Configuration parameters
CONFIG_BUFFER = 0,
CONFIG_UPDATEPERIOD = 1,
CONFIG_GVOL_SAMPLE = 4,
CONFIG_GVOL_STREAM = 5,
CONFIG_GVOL_MUSIC = 6,
CONFIG_CURVE_VOL = 7,
CONFIG_CURVE_PAN = 8,
CONFIG_FLOATDSP = 9,
CONFIG_3DALGORITHM = 10,
CONFIG_NET_TIMEOUT = 11,
CONFIG_NET_BUFFER = 12,
CONFIG_PAUSE_NOPLAY = 13,
CONFIG_NET_PREBUF = 15,
CONFIG_NET_PASSIVE = 18,
CONFIG_REC_BUFFER = 19,
CONFIG_NET_PLAYLIST = 21,
CONFIG_MUSIC_VIRTUAL = 22,
CONFIG_VERIFY = 	23,
CONFIG_UPDATETHREADS = 24,
CONFIG_DEV_BUFFER = 27,
CONFIG_VISTA_TRUEPOS = 30,
CONFIG_IOS_MIXAUDIO = 34,
CONFIG_DEV_DEFAULT = 36,
CONFIG_NET_READTIMEOUT = 37,
CONFIG_VISTA_SPEAKERS = 38,
CONFIG_IOS_SPEAKER = 39,
CONFIG_MF_DISABLE = 40,
CONFIG_HANDLES = 	41,
CONFIG_UNICODE = 	42,
CONFIG_SRC = 43,
CONFIG_SRC_SAMPLE = 44,
CONFIG_ASYNCFILE_BUFFER = 45,
CONFIG_OGG_PRESCAN = 47,
CONFIG_MF_VIDEO = 48,
CONFIG_AIRPLAY = 	49,
CONFIG_DEV_NONSTOP = 50,
CONFIG_IOS_NOCATEGORY = 51,
CONFIG_VERIFY_NET = 52,
CONFIG_DEV_PERIOD = 53,
CONFIG_FLOAT = 	54,
CONFIG_NET_SEEK = 56,
// error report modes
ERRORS_IGNORE = 0,
ERRORS_LOG = 1,
ERRORS_THROW = 2
};

void export AppendDoubleNullTerminatedList (std::vector<std::string>& list, const char* dlst);

void export SetErrorReportMode (int);
int export GetErrorReportMode ();
void export SetErrorReportCallback (const std::function<bool(const struct Exception&)>& callback);

bool export Init (int device = -1, DWORD freq = 48000, DWORD flags = 0);
bool export Free ();
bool export SetConfig (DWORD option, DWORD value);
DWORD export GetConfig (DWORD option);

int export GetDevice ();
bool export SetDevice (int device);
bool export GetDeviceInfo (int device, struct DeviceInfo& info);
bool export IsDeviceInitialized (int device);

bool export Update3D ();
bool export Set3DPosition (float* pos, float* vel=nullptr, float* front=nullptr, float* top=nullptr);
bool export Get3DPosition (float* pos, float* vel=nullptr, float* front=nullptr, float* top=nullptr);
bool export Set3DPosition (double* pos, double* vel=nullptr, double* front=nullptr, double* top=nullptr);
bool export Get3DPosition (double* pos, double* vel=nullptr, double* front=nullptr, double* top=nullptr);
bool export Set3DFactors (double distance, double rolloff, double doppler);
bool export Get3DFactors (float* distance, float* rolloff, float* doppler);
bool export Get3DFactors (double* distance, double* rolloff, double* doppler);
inline bool Set3DDistanceFactor (double d) { return Set3DFactors(d, 0, 0); }
inline bool Set3DRollOffFactor (double rolloff) { return Set3DFactors(0, rolloff, 0); }
inline bool Set3DDopplerFactor (double doppler) { return Set3DFactors(0, 0, doppler); }
inline double Get3DDistanceFactor () { double d = -1; Get3DFactors(&d, nullptr, nullptr); return d; }
inline double Get3DRollOffFactor () { double d = -1; Get3DFactors(nullptr, &d, nullptr); return d; }
inline double Get3DDopplerFactor () { double d = -1; Get3DFactors(nullptr, nullptr, &d); return d; }

bool export FXPushParams (DWORD fx, const void* ptr);
bool export FXPullParams (DWORD fx, void* ptr);
bool export FXReset (DWORD fx);
DWORD export FXInstall (DWORD handle, int type, int priority);
bool export FXDeinstall (DWORD handle, DWORD fx);

class Exception: std::exception {
private: 
int code;
public:
inline Exception (int c): code(c) {}
inline int GetCode () const { return code; }
const char* export GetErrorMessage () const;
virtual const char* what () const noexcept final override { return GetErrorMessage(); }
};

struct DeviceInfo {
std::string name;
DWORD flags;
};

struct VFile {
virtual int read (void* buffer, int length) = 0;
virtual bool seek (int64_t pos) = 0;
virtual int64_t getLength () = 0;
virtual void close () = 0;
virtual ~VFile () = default;
};

class Handle {
protected:
DWORD handle;

public:
Handle (DWORD handle=0): handle(handle) {}
inline DWORD GetHandle () const { return handle; }
inline bool IsValid () const { return handle; }
inline operator bool () const { return IsValid(); }
};

template<class E>
class FX: public E, public Handle  {
protected:
E* edata () { return this; }
public:
FX (DWORD handle=0): Handle(handle) { memset(edata(), 0, sizeof(E)); }
FX (const FX&) = default;
~FX () = default;
bool Reset () { return FXReset(handle); }
bool Push () { return FXPushParams(handle, edata()); }
bool Pull () { return FXPullParams(handle, edata()); }
};

class Sync;
class DSP;
class Channel: public Handle {
protected:
friend class Sample;

public:
inline Channel (DWORD handle=0): Handle(handle) {}
Channel (const Channel&) = default;
~Channel () = default;

bool export Play (bool restart = false);
bool export Pause ();
bool export Stop ();
int export GetState () const;
inline bool IsActive () const { return handle && GetState(); }
inline bool IsValid () const { return handle && GetState(); }
inline bool IsPlaying () const { return GetState()==PLAYING; }
inline bool IsStopped () const { return GetState()==STOPPED; }
inline bool IsPaused () const { return GetState()==PAUSED; }
inline bool IsStalled () const { return GetState()==STALLED; }
inline operator bool () const { return IsValid(); }

double export GetAttribute (int attr);
bool export SetAttribute (int attr, double value);
bool export SlideAttribute (int attr, double time, double targetValue);
bool export IsSliding (int attr = 0);
inline double GetVolume () { return GetAttribute(ATTR_VOL); }
inline double GetPan () { return GetAttribute(ATTR_PAN); }
inline double GetRate () { return GetAttribute(ATTR_RATE); }
inline bool SetVolume (double value) { return SetAttribute(ATTR_VOL, value); }
inline bool SetPan (double value) { return SetAttribute(ATTR_PAN, value); }
inline bool SetRate (double value) { return SetAttribute(ATTR_RATE, value); }

bool export Set3DPosition (float* pos, float* ori=nullptr, float* vel=nullptr);
bool export Get3DPosition (float* pos, float* ori=nullptr, float* vel=nullptr);
bool export Set3DPosition (double* pos, double* ori=nullptr, double* vel=nullptr);
bool export Get3DPosition (double* pos, double* ori=nullptr, double* vel=nullptr);
bool export Set3DAttributes (int mode, double minDist, double maxDist, int innerAngle, int outerAngle, double outerVol);
bool export Get3DAttributes (int* mode, float* minDist, float* maxDist, int* innerAngle, int* outerAngle, float* outerVol);
bool export Get3DAttributes (int* mode, double* minDist, double* maxDist, int* innerAngle, int* outerAngle, double* outerVol);

double export GetPosition ();
bool export SetPosition (double position);
double export GetLength ();

DWORD export Flags (DWORD flags=0, DWORD mask=0);
inline DWORD GetFlags () { return Flags(0, 0); }
inline bool HasFlag (DWORD flag) { return GetFlags()&flag; }
inline DWORD SetFlags (DWORD flags) { return Flags(flags, flags); }
inline DWORD ClearFlags (DWORD flags) { return Flags(0, flags); }

const char* export GetTags (DWORD type);
bool export GetInfo (uint32_t* ctype = nullptr, uint32_t* freq = nullptr, uint32_t* channels = nullptr, uint32_t* resolution = nullptr);
inline uint32_t GetType () { uint32_t x; GetInfo(&x); return x; }
inline uint32_t GetSampleRate () { uint32_t x; GetInfo(nullptr, &x); return x; }
inline uint32_t GetChannels () { uint32_t x; GetInfo(nullptr, nullptr, &x); return x; }
inline uint32_t GetResolution () { uint32_t x; GetInfo(nullptr, nullptr, nullptr, &x); return x; }

Sync export SetSync (DWORD type, QWORD param, const std::function<void(Sync, Channel, int)>& callback);
bool export RemoveSync (const Sync& sync);
DSP export SetDSP (const std::function<void(DSP, Channel, void*, DWORD)>& callback, int priority = 0);
bool export RemoveDSP (const DSP& dsp);

template<class E> FX<E>  SetFX (int type, int priority=0) { 
FX<E> re = FXInstall(handle, type, priority);
re.Pull();
return re;
}

DWORD export GetData (void* buffer, DWORD length);
};

class Stream: public Channel {
protected:
friend class TempoStream;

public:
static Stream export CreateFile (const std::string& filename, DWORD flags=0);
static Stream export CreateFile (const std::wstring& filename, DWORD flags=0);
static Stream export CreateFile (VFile& file, bool buffered=false, DWORD flags=0);
static Stream export CreateURL (const std::string& url, DWORD flags = 0);
static Stream export CreateURL (const std::wstring& url, DWORD flags = 0);
static Stream export CreateURL (const std::string& url, DWORD flags, const std::function<void(const void*, uint32_t)>& downloadProc);
static Stream export CreateURL (const std::wstring& url, DWORD flags, const std::function<void(const void*, uint32_t)>& downloadProc);
static Stream export CreateMem (const void* data, QWORD length, DWORD flags = 0);
static Stream export CreateCallback (DWORD freq, DWORD channels, DWORD flags, const std::function<DWORD(Stream, void* DWORD)>& callback);
static Stream export CreateDummy (DWORD freq, DWORD channels, DWORD flags=0);
static Stream export GetDeviceOutputStream ();
static Stream export GetDevice3DOutputStream ();

inline Stream (DWORD handle = 0): Channel(handle) {}
Stream (const Stream&) = default;
~Stream () = default;

int export GetDevice ();
bool export SetDevice (int device);
bool export Free ();
};

class Sample;
class SampleChannel: public Channel {
public:
inline SampleChannel (DWORD handle): Channel(handle) {}
SampleChannel (const SampleChannel&) = default;
~SampleChannel () = default;

Sample GetSample () ;
};

class MusicStream: public Stream {
public:
inline MusicStream (DWORD handle): Stream(handle) {}
MusicStream (const MusicStream&) = default;
~MusicStream () = default;

static MusicStream CreateFile (const std::string& filename, DWORD flags = 0, DWORD freq = 1);
static MusicStream CreateFile (const std::wstring& filename, DWORD flags = 0, DWORD freq = 1);
static MusicStream CreateMem (const void* data, DWORD length, DWORD flags=0, DWORD freq=1);
};

struct SampleInfo {
    DWORD freq;
    float volume;
    float pan;
    DWORD flags;
    DWORD length;
    DWORD max;
    DWORD resolution;
    DWORD channels;
    DWORD minGap;
    DWORD mode3d;
    float minDist;
    float maxDist;
    DWORD innerAngle;
    DWORD outerAngle;
    float outerVolume;
    DWORD vam;
    DWORD priority;
};

class Sample: public Handle {
protected:
friend class SampleChannel;

public:
static Sample export CreateFile (const std::string& filename, DWORD max = 16, DWORD flags = 0);
static Sample export CreateFile (const std::wstring& filename, DWORD max = 16, DWORD flags = 0);
static Sample export CreateMem (const void* data, QWORD length, DWORD max = 16, DWORD flags = 0);
static Sample export CreateRaw (const void* data, DWORD length, DWORD freq=44100, DWORD channels=1, DWORD max=16, DWORD flags=0);

Sample (DWORD handle = 0): Handle(handle) {}
Sample (const Sample&) = default;
~Sample () = default;
inline DWORD GetHandle () { return handle; }

SampleChannel export CreateChannel (bool onlyNew=false);
std::vector<SampleChannel> export GetAllChannels ();
bool export StopAll ();
bool export Free ();
SampleInfo export GetInfo ();
bool export SetInfo (const SampleInfo& info);
DWORD export GetData (void* buffer = nullptr);
bool export SetData (const void* buffer);

inline DWORD GetSampleRate () { return GetInfo().freq; }
inline int GetChannels () { return GetInfo().channels; }
inline DWORD GetLength () { return GetInfo().length; }
};

class DSP: public Handle {
private:
DWORD channel;

public:
DSP (DWORD handle=0, DWORD ch=0): Handle(handle), channel(ch) {}
DSP (const DSP&) = default;
~DSP () = default;
bool export SetPriority (int priority);
inline Channel GetChannel () { return channel; }
};

class Sync: public Handle {
public:
Sync (DWORD handle=0): Handle(handle) {}
Sync (const Sync&) = default;
~Sync () = default;
};

class Plugin: public Handle  {
public:
inline Plugin (DWORD handle = 0): Handle(handle) {}
Plugin (const Plugin&) = default;
~Plugin() = default;

static Plugin export Load (const std::string& filename);
static Plugin export Load (const std::wstring& filename);

DWORD export GetType (int format = 0);
std::vector<std::pair<std::string, std::string>> export GetFormats ();
bool export Free ();
};

class TempoStream: public Stream {
public:
inline TempoStream (): Stream(0) {}
export TempoStream (const Stream& source, DWORD flags = 0);
TempoStream (const TempoStream&) = default;
~TempoStream () = default;

Stream export GetSource ();
inline double GetSpeed () { return GetAttribute(ATTR_SPEED); }
inline double GetPitch () { return GetAttribute(ATTR_RAW_PITCH); }
inline double GetSemitones () { return GetAttribute(ATTR_PITCH); }
inline bool SetSpeed (double value) { return SetAttribute(ATTR_SPEED, value); }
inline bool SetPitch (double value) { return SetAttribute(ATTR_RAW_PITCH, value); }
inline bool SetSemitones (double value) { return SetAttribute(ATTR_PITCH, value); }
};

class File {
private:
void* file;
int status;

public:
export File (const std::string& url);
inline bool isValid () { return file && status>=200 && status < 300; }
inline operator bool () { return isValid(); }
inline bool operator! () { return !isValid(); }
inline int GetStatus () { return status; }
int64_t export GetLength();
int64_t export GetDownloadedLength();
int export Read (void* buffer, int length);
void export Close();
inline ~File () { Close(); }
};


} // namespace BASS

#endif