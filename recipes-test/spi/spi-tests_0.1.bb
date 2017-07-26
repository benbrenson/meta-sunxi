DESCRIPTION = "Application for running basic spi latency tests."
LICENSE = "gpl2"

inherit dpkg debianize

URL="local"
SECTION  = "admin"
PRIORITY = "standard"

SRC_DIR="src"

SRC_URI += " \
        file://src \
        file://debian \
        "
