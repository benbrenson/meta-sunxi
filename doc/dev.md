# Development notes
## Creating debian package from uboot
* dh_make is used
* useful cmdline options: --copyright gpl2
  Creates a copyright file template for GPL-2


* Thing to be done for debianize uboot:
```bash
root@pc $ DEBEMAIL="email@address"
root@pc $ DEBFULLNAME="Max Mustermann"
root@pc $ export DEBEMAIL DEBFULLNAME

root@pc $ mv <uboot-folder> u-boot-2017.03
root@pc $ cd u-boot-2017.03
root@pc $ dh_make --copyright gpl2 --createorig -y --single
```
** Note: For creating a template based debian directory use -t directory option. **
Now modify the debian folder contents. The folder is created in uboot-folder.




Then compile/build the package:

```bash
root@pc $ dpkg-buildpackage -us -uc -i --host-arch=armhf
```