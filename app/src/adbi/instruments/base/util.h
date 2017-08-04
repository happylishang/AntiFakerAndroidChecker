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

#include <termios.h>

int find_name(pid_t pid, char *name, char *libn, unsigned long *addr);
int find_libbase(pid_t pid, char *libn, unsigned long *addr);


