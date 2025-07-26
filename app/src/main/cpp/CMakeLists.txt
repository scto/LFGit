# Legt die minimal erforderliche Version von CMake fest.
cmake_minimum_required(VERSION 3.22.1)

# Definiert das Projekt und aktiviert die Unterstützung für die Sprachen C und Assembler (ASM).
# Dies ist notwendig, da Sie eine .S-Datei (bootstrap-zip.S) und eine .c-Datei (bootstrap.c) verwenden.
project(termux_bootstrap LANGUAGES C ASM)

# Fügt die Quelldateien hinzu und erstellt eine geteilte Bibliothek (SHARED library).
# CMake fügt automatisch das Präfix "lib" hinzu, sodass der resultierende Dateiname "libtermux-bootstrap.so" lautet.
add_library(
        # Name des Moduls
        termux-bootstrap

        # Art der Bibliothek
        SHARED

        # Quelldateien 
        bootstrap-zip.S
        bootstrap.c
)

# Optional: Wenn Sie spezielle Linker-Flags oder andere Bibliotheken verknüpfen müssen,
# können Sie dies mit target_link_libraries() tun. Für dieses Beispiel ist es nicht notwendig.
#
# target_link_libraries(termux-bootstrap
#                       android
#                       log)