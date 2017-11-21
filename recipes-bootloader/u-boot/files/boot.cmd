setenv envfile_max_size "0x100000"
setenv dtbo_addr "0x44000000"


if printenv ustate; then
    if test ${ustate} = 0; then
        # Normal operation... do nothing special.
        echo "Normal boot. No update was performed."

    elif test ${ustate} = 1; then
        echo "Update was performed. Switching rootfs partitions."

        # Switch rootfs partitions
        setenv cmdline_rootdev ${cmdline_rootdev_sec}
        setenv cmdline_rootdev_sec ${cmdline_rootdev_prim}
        setenv cmdline_rootdev_prim ${cmdline_rootdev}

        # Switch boot partitions
        setenv bootpart ${bootpart_sec_num}
        setenv bootpart_sec_num ${bootpart_prim_num}
        setenv bootpart_prim_num ${bootpart}

        setenv bootargs ${bootargs_base} root=${cmdline_rootdev}

        # Update was performed set ustate 2 so only rootfs will set it
        # to 0 again.
        setenv ustate 2
        saveenv

    elif test ${ustate} = 2; then
        # Oh, oh... something went wrong.
        # Rootfs service did not reset the ustate.
        # So switch rootfs and boot partitions back and set ustate to 3, so
        # userspace will handle this case.

        echo "Something went wrong while performing the update. Switching rootfs partitions back."
        setenv cmdline_rootdev ${cmdline_rootdev_sec}
        setenv cmdline_rootdev_sec ${cmdline_rootdev_prim}
        setenv cmdline_rootdev_prim ${cmdline_rootdev}

        setenv bootpart ${bootpart_sec_num}
        setenv bootpart_sec_num ${bootpart_prim_num}
        setenv bootpart_prim_num ${bootpart}

        setenv bootargs ${bootargs_base} root=${cmdline_rootdev}
        setenv ustate 3
        saveenv

    else
        echo "ustate has an abnormal value. Please check this!"
    fi

else
    # First boot of device!
    echo "First Boot of device. Initialize environment."
    setenv ustate 0

    setenv bootdev ##BOOT_DEVICE_NAME##
    setenv bootdev_num ##BOOT_DEVICE_NUM##

    setenv bootpart ##BOOTP_PRIM_NUM##
    setenv bootpart_prim_num ##BOOTP_PRIM_NUM##
    setenv bootpart_sec_num ##BOOTP_SEC_NUM##

    setenv cmdline_rootdev ##CMDLINE_ROOTDEV_PRIM##
    setenv cmdline_rootdev_prim ##CMDLINE_ROOTDEV_PRIM##
    setenv cmdline_rootdev_sec ##CMDLINE_ROOTDEV_SEC##

    setenv bootargs_base ##KERNEL_CMDLINE##
    setenv bootargs ${bootargs_base} root=${cmdline_rootdev}
    setenv fdtfile ##DTBS##
    saveenv
fi


load ${bootdev} ${bootdev_num}:${bootpart} ${ramdisk_addr_r} /boot/overlays.txt
env import -t ${ramdisk_addr_r} ${envfile_max_size}

echo Kernel commandline ${bootargs}

load ${bootdev} ${bootdev_num}:${bootpart} ${kernel_addr_r} /boot/uImage

load ${bootdev} ${bootdev_num}:${bootpart} ${fdt_addr_r} /boot/dts/${fdtfile}
fdt addr ${fdt_addr_r}

# Add 100K size for possible overlay extension
fdt resize 100000

for overlay_file in ${overlays}; do
	if load ${bootdev} ${bootdev_num}:${bootpart} ${dtbo_addr} /boot/dts/overlays/${overlay_file}; then
		echo "Applying device tree overlay ${overlay_file}"
		fdt apply ${dtbo_addr} || setenv overlay_error "true"
	fi
done

# Activate watchdog (16 sec) for boot failure case
echo Activating Watchdog, kernel must deactivate!
wdt 16

bootz ${kernel_addr_r} - ${fdt_addr_r} || bootm ${kernel_addr_r} - ${fdt_addr_r}

