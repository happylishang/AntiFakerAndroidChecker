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
#include <stdlib.h>

#include "../base/hook.h"
#include "../base/base.h"

#include <android/log.h>
#include <string.h>


#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"lishang",__VA_ARGS__)


#undef log

#define log(...) \
        {FILE *fp = fopen("/data/local/tmp/adbi_example.log", "a+"); if (fp) {\
        fprintf(fp, __VA_ARGS__);\
        fclose(fp);}}


// this file is going to be compiled into a thumb mode binary

void __attribute__ ((constructor)) my_init(void);

static struct hook_t eph;

// for demo code only
static int counter;

// // arm version of hook

/*  
 *  log function to pass to the hooking library to implement central loggin
 *
 *  see: set_logfunction() in base.h
 */
static void my_log(char *msg)
{
	log("%s", msg)
}


// extern int my_epoll_wait_arm(int epfd, struct epoll_event *events, int maxevents, int timeout);


// int my_epoll_wait(int epfd, struct epoll_event *events, int maxevents, int timeout)
// {
// 	int (*orig_epoll_wait)(int epfd, struct epoll_event *events, int maxevents, int timeout);
// 	orig_epoll_wait = (void*)eph.orig;

// 	hook_precall(&eph);
// 	int res = orig_epoll_wait(epfd, events, maxevents, timeout);
 
// 		hook_postcall(&eph);
// 		log("epoll_wait() called\n");
   
       
//        LOGI("my_epoll_wait"); 
// 	return res;
// }



extern int my_system_property_get_arm(const char *name, char *value);
 
int my_system_property_get(const char *name, char *value)
{

 
   LOGI(" property_get  %s \n",name);

	int (*system_property_get)(const char *name, char *value);
	system_property_get = (void*)eph.orig;
  	hook_precall(&eph);
  	system_property_get(name,value);
   
    char ro[100] ="ro.serialno";
    int ret=strcmp(name, "ro.serialno");
    counter++;
    if(ret == 0){
		 char * a;
		 if(counter %4 == 0){
		 		a="123345678";
		 }else{
		 	   a="87654321";
		 }	 
   	 	strcpy(value, a );
    	LOGI("property_get a  %s ",a); 
	}
 	 LOGI("property_get value  %s ",value); 
 	 hook_postcall(&eph);
	return 6;
}
 

void my_init(void)
{
	counter = 3;

	log("%s started\n", __FILE__)
 
	set_logfunction(my_log);

	// hook(&eph, getpid(), "libc.", "epoll_wait", my_epoll_wait_arm, my_epoll_wait);
	hook(&eph, getpid(), "libc.", "__system_property_get", my_system_property_get_arm, my_system_property_get);


}

