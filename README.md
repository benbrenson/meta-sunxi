# Layer for running linux on sunxi boards
## Host package dependencies
* make-kpkg
* devscripts
* debhelper
* dh-make
* git-buildpackage
* schroot

## Debianize packages
The isar build system will create a full debian compatible distribution.
Since lot of source repositories are not prepared for getting compiled as debian compatible package,
these have to be debianized.
We should follow a clean debian like workflow to create, compile and install debian packages.

In order to debianize a package, we have to do the following steps:

1. Write a recipe for the source code package:
* This step is usuall for bitbake and will simply define the required steps to be performed for compiling and installing
  a simple software component.
2. Add a debianize folder into the respective recipe file folder:
* The debianize folder contains files required for creating a debian source package. Basically this folder can be created with helper
  scripts(e.g. dh_make).
* After creating the debianize folder, it is mandatory to modify all required parts of its contents in order to
  to let the debianize.bb class be capable of preparing the source package.
  This will maybe include modification of following files:
  * control
  * copyright
  * rules (only define build targets)
  * debian.install (define files for installation)



## debianize.bbclass
The debianize class is required for recipes/packages which refering to non-debian compatible source code.
Including this class will trigger specific steps for modifying the source to a debian source package.
The debianize class will require a folder called **debianize** which has to be provided in the recipes folder.
** Note: If the recipe refers to a source code repository, which is already debian compatible, the debianize folder and including the debiaize.bbclass is not mandatory.**