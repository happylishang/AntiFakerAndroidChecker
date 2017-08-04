/*
 *  Collin's Binary Instrumentation Tool/Framework for Android
 *  Collin Mulliner <collin[at]mulliner.org>
 *
 *  (c) 2012,2013
 *
 *  License: LGPL v2.1
 *
 */

void (*log_function)(char *logmsg);

#define log(...) \
        {char __msg[1024] = {0};\
        snprintf(__msg, sizeof(__msg)-1, __VA_ARGS__);\
        log_function(__msg); }

struct hook_t {
	unsigned int jump[3];
	unsigned int store[3];
	unsigned char jumpt[20];
	unsigned char storet[20];
	unsigned int orig;
	unsigned int patch;
	unsigned char thumb;
	unsigned char name[128];
	void *data;
};

int start_coms(int *coms, char *ptsn);

void hook_cacheflush(unsigned int begin, unsigned int end);	
void hook_precall(struct hook_t *h);
void hook_postcall(struct hook_t *h);
int hook(struct hook_t *h, int pid, char *libname, char *funcname, void *hook_arm, void *hook_thumb);
void unhook(struct hook_t *h);
