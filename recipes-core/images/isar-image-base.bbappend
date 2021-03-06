FILESPATH_prepend := "${THISDIR}/${PN}-${PV}:${THISDIR}/files:"

IMAGE_PREINSTALL_append_nanopi-neo-air += "isc-dhcp-client wireless-tools wpasupplicant"

BOOTPART ?= "${base_devdir}/${BOOT_DEVICE_LINUX}${RECOVERY_BOOTPART_NUM}"
BOOTSCIPT_DIR ?= "/boot/recovery"

# Post Rootfs: Add mmc to fstab
do_post_fstab() {
    mkdir -p ${BOOTSCIPT_DIR}
    echo "${BOOTPART}   ${BOOTSCIPT_DIR}       ${BOOTDEVICE_FSTYPE}        defaults        1   1" >> /etc/fstab
}
addtask do_post_fstab
do_post_fstab[stamp-extra-info] = "${MACHINE}.chroot"
do_post_fstab[chroot] = "1"
do_post_fstab[id] = "${ROOTFS_ID}"

POST_ROOTFS_TASKS_append = "do_post_fstab;"