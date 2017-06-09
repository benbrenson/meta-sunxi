DESCRIPTION = "Mainline linux kernel support for the nanopi-neo. \
Customized for running with CactusPot board extension."

# We will cross compile the kernel, because qemu has poor performance.
inherit dpkg-cross kernel debianize
DEPENDS += "dtc"

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-${PV}:"

URL = "https://${GITUSER}:${KERNEL_PASSWD}@git.pixel-group.de/siemens-ct/Siemens_CT_REE-kernel.git"
KERNEL_SRC = "git://${URL};protocol=file"
SRCREV = "40e9411858aecb066c414efd617b8e2d8434ab5d"
BRANCH = "linux-4.10.y"

SRC_URI += " \
        ${KERNEL_SRC};branch=${BRANCH} \
        file://defconfig \
        file://0001-Added-support-for-ixxat-usb-to-can-v2-compact.patch \
        file://dts \
        file://debian \
        "

export DTBO_SRC_DIR ?= "arch/${CCARCH}/boot/dts/overlays"
export DTBOS        ?= ""

export DTBO_INSTALL_DIR_BASE ?= "boot"
export DTBO_INSTALL_DIR ?= "${DTB_INSTALL_DIR_BASE}/dts/overlays"

# Overwrite the standart dtc with the overlay capable one.
do_update_dtc() {
    cd ${S}

    # We need to compile the standart dtc first, since copying the modified dtc
    # is not enough. The dtc otherwise will be recompiled again.
    ${MAKE} scripts
    cp ${TOOLSDIR_NATIVE}/dtc/dtc ${S}/scripts/dtc
}
do_update_dtc[depends] = "dtc:do_install"
addtask do_update_dtc after do_copy_device_tree before do_compile_overlays

do_compile_overlays() {
    cd ${S}
    for dtbo in ${DTBOS}; do
        ${MAKE} ${dtbo}
    done
}
addtask do_compile_overlays after do_install_dtb before do_build
