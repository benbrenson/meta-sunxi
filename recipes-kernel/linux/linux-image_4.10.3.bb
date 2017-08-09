DESCRIPTION = "Mainline linux kernel support for the nanopi-neo. \
Customized for running with CactusPot board extension."

# We will cross compile the kernel, because qemu has poor performance.
inherit kernel debianize
DEPENDS_class-cross = "dtc-native"

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
debianize_build_prepend() {
	${MAKE} scripts
	cp /opt/bin/overlay-dtc ${PPS}/scripts/dtc/dtc
}

# for now only install overlays.txt file
debianize_install_append() {
	echo "overlays=${DTBOS}" | xargs > debian/${BPN}/boot/overlays.txt
}


BBCLASSEXTEND = "cross"
