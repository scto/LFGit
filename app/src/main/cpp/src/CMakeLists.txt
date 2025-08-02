#set(BINARY ${CMAKE_PROJECT_NAME}_src)

file(GLOB_RECURSE SOURCES LIST_DIRECTORIES false *-zip.S *.c)

set(SOURCES ${SOURCES})

#add_executable(Termux_bootstrap_src ${SOURCES})

add_library(${BINARY}_lib SHARED ${SOURCES})
target_link_libraries(Termux_bootstrap_src android log)
