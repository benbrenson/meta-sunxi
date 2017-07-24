DESCRIPTION = "Mainline linux kernel support for the nanopi-neo. \
Customized for running with CactusPot board extension."

# We will cross compile the kernel, because qemu has poor performance.
inherit dpkg-cross kernel debianize
DEPENDS += "dtc"

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-${PV}:"

URL = "git://git.pixel-group.de/siemens-ct/Siemens_CT_REE-kernel.git"
SRCREV = "${BRANCH}"
BRANCH = "linux-4.10.y"

# Activate generating debian rules file
GENERATE_RULES = "true"

SRC_DIR = "git"
SRC_URI += " \
        ${URL};protocol=https;branch=${BRANCH} \
        file://defconfig \
        file://0001-Added-support-for-ixxat-usb-to-can-v2-compact.patch \
        file://dts \
        file://debian \
        "

export DTBO_SRC_DIR ?= "arch/${TARGET_ARCH}/boot/dts/overlays"
export DTBOS        ?= ""
export DTBO_DEST_DIR ?= "boot/dts/overlays"

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
addtask do_compile_overlays after do_update_dtc before do_install_overlays

# for now only install overlays.txt file
do_install_overlays() {
    echo "overlays=${DTBOS}" | xargs > ${S}/debian/${PN}/boot/overlays.txt
}
do_install_overlays[dirs] += "${S}/debian/${PN}/boot"
addtask do_install_overlays after do_compile_overlays before do_build
