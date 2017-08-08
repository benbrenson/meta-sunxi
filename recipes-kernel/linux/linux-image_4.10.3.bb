DESCRIPTION = "Mainline linux kernel support for the nanopi-neo. \
Customized for running with CactusPot board extension."

# We will cross compile the kernel, because qemu has poor performance.
inherit kernel debianize
DEPENDS_class-cross += "dtc-native"

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-${PV}:"

URL = "git://git.pixel-group.de/siemens-ct/Siemens_CT_REE-kernel.git"
SRCREV = "${BRANCH}"
BRANCH = "linux-4.10.y"


SRC_DIR = "git"
SRC_URI += " \
        ${URL};protocol=https;branch=${BRANCH} \
        file://defconfig \
        file://0001-Added-support-for-ixxat-usb-to-can-v2-compact.patch \
        file://dts \
        file://debian \
        "

DTBO_SRC_DIR  ?= "arch/${TARGET_ARCH}/boot/dts/overlays"
DTBOS         ?= ""
DTBO_DEST_DIR ?= "boot/dts/overlays"

# Overwrite the standart dtc with the overlay capable one.
do_update_dtc() {
    cd ${PPS}

    # We need to compile the standart dtc first, since copying the modified dtc
    # is not enough. The dtc otherwise will be recompiled again.
    ${MAKE} scripts
    cp /opt/bin/overlay-dtc ${PPS}/scripts/dtc/dtc
}
do_update_dtc[depends] = "dtc:do_install"
do_update_dtc[chroot] = "1"
do_update_dtc[id] = "${CROSS_BUILDCHROOT_ID}"
addtask do_update_dtc after do_copy_device_tree before do_compile_overlays


do_compile_overlays() {
    cd ${PPS}
    for dtbo in ${DTBOS}; do
        ${MAKE} ${dtbo}
    done
}
do_compile_overlays[chroot] = "1"
do_compile_overlays[id] = "${CROSS_BUILDCHROOT_ID}"
do_compile_overlays[stamp-extra-info] = "${MACHINE}.chroot"
addtask do_compile_overlays after do_update_dtc before do_install_overlays

# for now only install overlays.txt file
do_install_overlays() {
    echo "overlays=${DTBOS}" | xargs > ${S}/debian/${BPN}/boot/overlays.txt
}
do_install_overlays[dirs] += "${S}/debian/${BPN}/boot"
addtask do_install_overlays after do_compile_overlays before do_build


BBCLASSEXTEND = "cross"