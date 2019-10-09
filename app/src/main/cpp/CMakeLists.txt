cmake_minimum_required(VERSION 3.4.1)

add_library(
    airnav-lib
    SHARED
    airnav-lib.c
)

target_link_libraries(
    airnav-lib
    android
    log
)

include_directories(src/main/cpp/include/)
