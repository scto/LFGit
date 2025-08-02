cmake_minimum_required(VERSION 3.22.1)
project(termux_bootstrap LANGUAGES C ASM)
set(CMAKE_CXX_STANDARD 17)
add_compile_options(-Wall -Wextra -Wpedantic -Werror)

add_subdirectory("src")
