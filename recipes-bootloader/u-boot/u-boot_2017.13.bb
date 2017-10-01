DESCRIPTION = "Official bootloader for many SoC devices developed by denx company. This package installs the bootloader \
but does not flash it to the bootmedia. This has to be done separately."
LICENSE = "gpl2"

include u-boot.inc
inherit dpkg debianize u-boot

# Add build dependencies installed via apt-get
# to debian control file
DEB_DEPENDS += " device-tree-compiler "

SRCREV = "5877d8f398de26617be6f1f57bc30c49e9f90ebb"
BRANCH = "master"

SECTION  = "admin"
PRIORITY = "extra"

BOOTSCRIPT_SRC ?= "boot.cmd"
BOOTSCRIPT ?= "boot.scr"


SRC_DIR="git"
SRC_URI += " \
         ${URL};branch=${BRANCH} \
         file://defconfig \
         file://debian \
         file://overlays.txt \
         file://${BOOTSCRIPT_SRC} \
         file://0001-Fixed-silent-return-after-broken-fdt-apply-command.patch \
         "


BOOT="${EXTRACTDIR}/${BOOTSCRIPT_SRC}"
do_configure() {
    [ -z ${KERNEL_CMDLINE} ] && bbfatal "\nNo Kernel cmdline specified. Please set KERNEL_CMDLINE variable."
    #echo "${KERNEL_CMDLINE}" > ${S}/cmdline.txt
    set -x
    sed -i -e 's|##BOOT_DEVICE_NUM##|${BOOT_DEVICE_NUM}|g'   ${BOOT}
    sed -i -e 's|##BOOT_DEVICE_NAME##|${BOOT_DEVICE_NAME}|g' ${BOOT}
    sed -i -e 's|##BOOTP_PRIM_NUM##|${BOOTP_PRIM_NUM}|g'     ${BOOT}
    sed -i -e 's|##BOOTP_SEC_NUM##|${BOOTP_SEC_NUM}|g'       ${BOOT}
    sed -i -e 's|##ROOTDEV_PRIM##|${ROOTDEV_PRIM}|g'         ${BOOT}
    sed -i -e 's|##ROOTDEV_SEC##|${ROOTDEV_SEC}|g'           ${BOOT}
    sed -i -e 's|##KERNEL_CMDLINE##|${KERNEL_CMDLINE}|g'     ${BOOT}
    sed -i -e 's|##DTBS##|${DTBS}|g'                         ${BOOT}

}
addtask do_configure after do_unpack before do_build

do_pre_install_append() {
    install -m 0755 ${S}/${BOOT_IMG} ${DEPLOY_DIR_IMAGE}
    install -m 0755 ${S}/${BOOTSCRIPT} ${DEPLOY_DIR_IMAGE}
}


BBCLASSEXTEND = "cross"