ADMIN_PACKAGES_append_nanopi-neo-air = " wpasupplicant "
DEV_PACKAGES_append_nanopi-neo-air = " wireless-tools "

IMAGE_PREINSTALL_append_nanopi-neo-air += " isc-dhcp-client "

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