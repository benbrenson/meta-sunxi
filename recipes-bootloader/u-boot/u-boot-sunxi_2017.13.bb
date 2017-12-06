DESCRIPTION = "Official bootloader for many SoC devices developed by denx company. This package installs the bootloader \
but does not flash it to the bootmedia. This has to be done separately."
LICENSE = "gpl2"

include u-boot.inc
inherit debianize u-boot dpkg

# Special case: Since uboot recipe in turn is selected by PREFERRED_PRODIVERS,
# all debian binary packages of uboot have to set one common provider (which is usually ${PN} in other cases) and also
# an unique provider.
PROVIDES_append = " libubootenv-sunxi libubootenv "

DEB_DEPENDS += " device-tree-compiler "

SRCREV = "5877d8f398de26617be6f1f57bc30c49e9f90ebb"
BRANCH = "master"

SECTION  = "admin"
PRIORITY = "extra"

BOOTSCRIPT_SRC ?= "boot.cmd"
BOOTSCRIPT ?= "boot.scr"
GENERATE_BOOTSCRIPT = "true"

SRC_DIR="git"
SRC_URI += " \
         ${URL};branch=${BRANCH};protocol=http \
         file://defconfig \
         file://debian \
         file://overlays.txt \
         file://${BOOTSCRIPT_SRC} \
         file://0001-Fixed-silent-return-after-broken-fdt-apply-command.patch \
         "

do_pre_install_append() {
    install -m 0755 ${S}/${BOOT_IMG} ${DEPLOY_DIR_IMAGE}
    install -m 0755 ${S}/${BOOTSCRIPT} ${DEPLOY_DIR_IMAGE}
}


BBCLASSEXTEND = "cross"