DESCRIPTION ?= "Mainline linux kernel support for the nanopi."

DESCRIPTION_nanopi-neo-air = "Mainline linux kernel support for the nanopi-neo-air."


# We will cross compile the kernel, because qemu has poor performance.
inherit debianize kernel
DEPENDS_class-cross = "dtc-native"

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-${PV}:"

URL = "git://git.kernel.org/pub/scm/linux/kernel/git/stable/linux-stable.git"
BRANCH="master"
SRCREV = "ef954844c7ace62f773f4f23e28d2d915adc419f"

SRC_DIR = "git"
SRC_URI += " \
        ${URL};branch=${BRANCH} \
        file://${MACHINE}_defconfig \
        file://dts \
        file://debian \
        file://0001-Added-support-for-compiling-device-tree-overlays.patch \
        file://0002-can-mcp251x-Fixed-delay-after-hw-reset.patch \
        file://0003-spi-sun6i-Added-support-for-gpio-chipselect.patch \
        file://0004-spi-sun6i-Fixed-maximum-transfer-size-of-64bit.patch \
        file://0005-net-Added-device-tree-support-for-w5100-driver.patch \
        "

SRC_URI_append_nanopi-neo-air = "file://firmware"

DTBO_SRC_DIR  ?= "arch/${TARGET_ARCH}/boot/dts/overlays"
DTBOS         ?= ""
DTBOS_LOAD    ?= ""
DTBO_DEST_DIR ?= "boot/dts/overlays"


generate_postinst() {
    sed -i 's|##KVERSION##|${FIX_KVERSION}|' ${EXTRACTDIR}/debian/postinst
}
do_unpack[postfuncs] += "generate_postinst"


# Overwrite the standart dtc with the overlay capable one.
debianize_build_prepend() {
	${MAKE} scripts
	cp /opt/bin/overlay-dtc ${PPS}/scripts/dtc/dtc
}

# for now only install overlays.txt file
debianize_install_append() {
	echo "overlays=${DTBOS_LOAD}" | xargs > debian/${BPN}/boot/overlays.txt
}

# Install required firmware binary and nvram config file for
# ap6212 wireless chipset
debianize_install_append_nanopi-neo-air() {
	install -m 644 -d debian/${BPN}/lib/firmware/brcm
	install -m 644 ${PP}/firmware/brcmfmac43430-sdio.bin debian/${BPN}/lib/firmware/brcm
	install -m 644 ${PP}/firmware/brcmfmac43430-sdio.txt debian/${BPN}/lib/firmware/brcm
}


BBCLASSEXTEND = "cross"
