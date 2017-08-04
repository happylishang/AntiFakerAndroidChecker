/*
 *  Collin's Binary Instrumentation Tool/Framework for Android
 *  Collin Mulliner <collin[at]mulliner.org>
 *  http://www.mulliner.org/android/
 *
 *  (c) 2012,2013
 *
 *  License: LGPL v2.1
 *
 */

#define _GNU_SOURCE
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <dlfcn.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <sys/select.h>
#include <string.h>
#include <termios.h>
#include <pthread.h>
#include <sys/epoll.h>

#include <jni.h>

#include "util.h"
#include "hook.h"


//static void __attribute__ ((constructor)) base_my_init(void);

static char logfile[] = "/data/local/tmp/adbi.log";

static void logmsgtofile(char *msg)
{
	int fp = open(logfile, O_WRONLY|O_APPEND);
    if (fp != -1) {
	    write(fp, msg, strlen(msg));
	    close(fp);
    }
}

static void logmsgtostdout(char *msg)
{
	write(1, msg, strlen(msg));
}

void* set_logfunction(void *func)
{
	void *old = log_function;
	log_function = func;
	return old;
}

static void base_my_init(void)
{
	// set the log_function
	log_function = logmsgtofile;
}
