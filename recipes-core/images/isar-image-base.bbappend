ADMIN_PACKAGES_append_nanopi-neo-air = "wpasupplicant"
DEV_PACKAGES_append_nanopi-neo-air = "wireless-tools"

BOOTPART = "${base_devdir}/${BOOT_DEVICE_LINUX}${BOOTP_PRIM_NUM}"

# Post Rootfs: Add mmc to fstab
do_post_fstab() {
    echo "${BOOTPART}   /boot       ${BOOTDEVICE_FSTYPE}        defaults        1   1" >> /etc/fstab
}
addtask do_post_fstab
do_post_fstab[stamp-extra-info] = "${MACHINE}.chroot"
do_post_fstab[chroot] = "1"
do_post_fstab[id] = "${ROOTFS_ID}"

POST_ROOTFS_TASKS_append = "do_post_fstab;"