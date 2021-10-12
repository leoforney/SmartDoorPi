cmake_minimum_required(VERSION 3.16)
project(smartdoorpi)

set(CMAKE_CXX_STANDARD 14)

add_executable(smartdoorpi main.cpp door.cpp)

target_link_libraries(smartdoorpi wiringPi curl)

#[[
find_package ( cppnetlib 0.11.0 REQUIRED )
include_directories ( ${CPPNETLIB_INCLUDE_DIRS} )
target_link_libraries ( smartdoorpi ${CPPNETLIB_LIBRARIES} )]]
