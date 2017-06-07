DESCRIPTION = "Mainline linux kernel support for the nanopi-neo. \
Customized for running with CactusPot board extension."

# We will cross compile the kernel, because qemu has poor performance.
inherit cross-compile kernel
DEPENDS += "dtc"

PROVIDES="linux-image"

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-${PV}:"

KERNEL_SRC = "git://https://${GITUSER}:${KERNEL_PASSWD}@git.pixel-group.de/siemens-ct/Siemens_CT_REE-kernel.git;protocol=file"
SRCREV="40e9411858aecb066c414efd617b8e2d8434ab5d"
BRANCH="linux-4.10.y"

SRC_URI += " \
        ${KERNEL_SRC};branch=${BRANCH} \
        file://defconfig \
        file://0001-Added-support-for-ixxat-usb-to-can-v2-compact.patch \
        file://dts \
        file://debianize \
        "

DTB_DIR  = "dts"
DTBO_DIR = "dts/overlays"
TARGET   = "kernel_image"
