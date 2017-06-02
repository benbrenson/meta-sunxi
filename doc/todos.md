# Todos

## kernel.bbclass:
Install the device tree overlays with an debian.install file.
For now we have modified the image.mk file.


## debianize.bbclass

* need global debian folder
* global debian folder has some template files:
    * control.in (setting version, packagename=recipe-name_version)

* First create konkrete debian folder from template folder
* Then run dh_make with overlay option (-t)

* each recipe has to set the LICENSE tag !!


## Use repository urls instead of refering to local ones (bootloader & kernel)


## Generic way to refer to extracted sources for chroot and cross compilation
* do_unpack:
    * chroot uses BUILDROOT
    * cross uses WORKDIR
* do_build
    * chroot uses relative path from BUILDROOT
    * cross uses absolute path to {S}

## Clean up mounted filesystems for chroot after error occurs

## Add following base functions for each recipe
* do_clean
* do_cleanstamp


# Bugs

## Unsupported Syscall: 384 from qemu while dpkg installation
The qemu: Unsupported syscall: 384 is a warning that the getrandom(2) system call is not implemented by our emulation layer, qemu. It can be safely ignored. Since it's a fairly new system call (introduced in kernel 3.17), apt and almost all programs automatically fall back to reading from /dev/urandom when this syscall fails.

* Source: https://docs.resin.io/troubleshooting/troubleshooting/


