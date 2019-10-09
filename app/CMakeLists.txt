cmake_minimum_required(VERSION 3.4.1)

add_library(
    airnav-lib
    SHARED
    src/main/cpp/airnav-lib.cpp
)

include_directories(src/main/cpp/include/)

