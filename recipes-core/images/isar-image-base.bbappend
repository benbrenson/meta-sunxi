ADMIN_PACKAGES_append_nanopi-neo-air = "wpasupplicant"
DEV_PACKAGES_append_nanopi-neo-air = "wireless-tools"

BOOTPART = "mmcblk0p1"
BOOTPART_FSTYPE = "vfat"

# Post Rootfs: Add mmc to fstab
do_post_fstab() {
    echo "/dev/${BOOTPART}   /       ${BOOTPART_FSTYPE}        defaults        1   1" >> /etc/fstab
}
addtask do_post_fstab
do_post_fstab[stamp-extra-info] = "${MACHINE}.chroot"
do_post_fstab[chroot] = "1"
do_post_fstab[id] = "${ROOTFS_ID}"

POST_ROOTFS_TASKS_append = "do_post_fstab;"