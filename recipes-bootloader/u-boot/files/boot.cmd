setenv bootdev "mmc"
setenv envfile_max_size "0x100000"
setenv dtbo_addr "0x44000000"


load ${bootdev} 0:1 ${ramdisk_addr_r} cmdline.txt
env import -t ${ramdisk_addr_r} ${envfile_max_size}

load ${bootdev} 0:1 ${ramdisk_addr_r} overlays.txt
env import -t ${ramdisk_addr_r} ${envfile_max_size}

echo Setting cmdline:
echo ${bootargs}

load ${bootdev} 0:1 ${kernel_addr_r} uImage

load ${bootdev} 0:1 ${fdt_addr_r} dts/${fdtfile}
fdt addr ${fdt_addr_r}

# Add 100K size for possible overlay extension
fdt resize 100000

for overlay_file in ${overlays}; do
	if load ${bootdev} 0:1 ${dtbo_addr} dts/overlays/${overlay_file}; then
		echo "Applying device tree overlay ${overlay_file}"
		fdt apply ${dtbo_addr} || setenv overlay_error "true"
	fi
done

bootz ${kernel_addr_r} - ${fdt_addr_r} || bootm ${kernel_addr_r} - ${fdt_addr_r}

