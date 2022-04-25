# CMakeLists.txt
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.
#CMakeLists.txt
cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
add_library(
      # 设置so文件名称.
       emulator_check
       # 设置这个so文件为共享.
       SHARED
       # Provides a relative path to your source file(s).
       src/main/jni/emulator/emcheck64.c )
#
#add_library(
#        # 设置so文件名称.
#        emulator_check_32
#        # 设置这个so文件为共享.
#        SHARED
#        # Provides a relative path to your source file(s).
#        src/main/jni/emulator/emcheck32.c)

add_library(
      # 设置so文件名称.
       property_get
       # 设置这个so文件为共享.
       SHARED
       # Provides a relative path to your source file(s).
       src/main/jni/property/proget.c)
# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
find_library( # Sets the name of the path variable.
       log-lib
       # Specifies the name of the NDK library that
       # you want CMake to locate.
       log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.
target_link_libraries( # Specifies the target library.
            # 制定目标库.
            emulator_check
            # Links the target library to the log library
            # included in the NDK.
            ${log-lib} )

target_link_libraries( # Specifies the target library.
        # 制定目标库.
        property_get
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib} )
#
#target_compile_options ( <lib> PUBLIC -m32 )
#target_link_libraries ( <target> PRIVATE <lib> )