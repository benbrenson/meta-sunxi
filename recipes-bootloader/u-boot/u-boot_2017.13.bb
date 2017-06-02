DESCRIPTION = "Official bootloader for many SoC devices developed by denx company. This package installs the bootloader \
but does not flash it to the bootmedia. This has to be done separately."
LICENSE = "gpl2"

include u-boot.inc
inherit dpkg-cross debianize

BOOTLOADER_SRC = "${URL};protocol=file"
SRCREV = "08933c6b2fbdc8e07f3f158dba4a2644bbae0b5e"
BRANCH = "h3_siemens"

SECTION  = "admin"
PRIORITY = "extra"

BOOTSCRIPT ?= "boot.scr"

SRC_DIR="git"
SRC_URI= " \
        ${BOOTLOADER_SRC};branch=${BRANCH} \
        file://defconfig \
        file://debian \
        file://overlays.txt \
        file://boot.cmd \
        "

do_install_append() {
    install -m 0755 ${S}/${BOOT_IMG} ${DEPLOY_DIR_BIN}/bootloader
}
do_install[dirs] += "${DEPLOY_DIR_BIN}/bootloader"


