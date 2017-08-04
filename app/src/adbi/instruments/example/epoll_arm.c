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

#include <sys/types.h>
#include <sys/epoll.h>


// extern int my_epoll_wait(int epfd, struct epoll_event *events, int maxevents, int timeout);

// int my_epoll_wait_arm(int epfd, struct epoll_event *events, int maxevents, int timeout)
// {
// 	return my_epoll_wait(epfd, events, maxevents, timeout);
// }

extern int my_system_property_get(const char *name, char *value);

int my_system_property_get_arm(const char *name, char *value)
{
	return my_system_property_get(name, value);
}