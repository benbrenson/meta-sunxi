DESCRIPTION_nanopi-neo = "Mainline linux kernel support for the nanopi."

DESCRIPTION_nanopi-neo-air = "Mainline linux kernel support for the nanopi-neo-air."


# We will cross compile the kernel, because qemu has poor performance.
inherit debianize kernel
DEPENDS = "dtc-native"

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-${PV}:"

URL = "git://git.kernel.org/pub/scm/linux/kernel/git/stable/linux-stable.git"
BRANCH="master"
SRCREV = "ef954844c7ace62f773f4f23e28d2d915adc419f"

SRC_DIR = "git"
SRC_URI += " \
        ${URL};branch=${BRANCH};protocol=https \
        file://${MACHINE}_defconfig \
        file://dts/sun8i-h3-nanopi.dtsi \
        file://dts/sun8i-h3-nanopi-neo.dts \
        file://dts/sun8i-h3-nanopi-neo-air.dts \
        file://debian \
        file://0001-Added-support-for-compiling-device-tree-overlays.patch \
        file://0002-can-mcp251x-Fixed-delay-after-hw-reset.patch \
        file://0003-spi-sun6i-Added-support-for-gpio-chipselect.patch \
        file://0004-spi-sun6i-Fixed-maximum-transfer-size-of-64bit.patch \
        file://0005-net-Added-device-tree-support-for-w5100-driver.patch \
        file://0006-can-mcp251x-Fixed-deadlock-for-free_irq-while-irq-wa.patch \
        "
SRC_URI_append_nanopi-neo-air = "file://firmware"

DTBO_SRC_DIR  = "arch/${TARGET_ARCH}/boot/dts/overlays"
DTBO_DEST_DIR = "boot/dts/overlays"

do_copy_device_tree() {
    cp  ${EXTRACTDIR}/dts/sun8i-h3-nanopi.dtsi \
        ${EXTRACTDIR}/dts/sun8i-h3-nanopi-neo.dts \
        ${EXTRACTDIR}/dts/sun8i-h3-nanopi-neo-air.dts \
        ${S}/arch/${TARGET_ARCH}/boot/dts
}
do_copy_defconfig[postfuncs] += "do_copy_device_tree"

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
# ap6212 (BCM43430) wireless chipset
debianize_install_append_nanopi-neo-air() {
	install -m 644 -d debian/${BPN}/lib/firmware/brcm
	install -m 644 ${PP}/firmware/brcmfmac43430-sdio.bin.7.45.77.0.ucode1043.2054 debian/${BPN}/lib/firmware/brcm/brcmfmac43430-sdio.bin
	install -m 644 ${PP}/firmware/brcmfmac43430-sdio.txt debian/${BPN}/lib/firmware/brcm
}


BBCLASSEXTEND = "cross"
