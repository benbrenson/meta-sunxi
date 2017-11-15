FILESPATH_prepend := "${THISDIR}/files:"

FW_ENV = "fw_env.config.sunxi"

SRC_URI += "\
           file://${FW_ENV} \
           "

debianize_install_append_update() {
	mkdir -p debian/${BPN}/sbin
	mkdir -p debian/${BPN}/etc
	install -m 0755 ${PPS}/tools/env/fw_printenv debian/${BPN}/sbin/fw_printenv
	install -m 0755 ${PPS}/tools/env/fw_printenv debian/${BPN}/sbin/fw_setenv
	install -m 0644 ${PP}/${FW_ENV} debian/${BPN}/etc/fw_env.config
}