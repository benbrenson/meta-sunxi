ifeq ($(CONFIG_OF_CONFIGFS),y)

dtbo-$(CONFIG_MACH_SUN8I) += \
			sun8i-h3-i2c1.dts \
			sun8i-h3-spi-mcp2515.dts \
			sun8i-h3-i2c0.dts \
			sun8i-h3-i2c2.dts \
			sun8i-h3-spi-spidev.dts

targets += $(dtbo-y)

endif

always		:= $(dtbo-y)
clean-files	:= *.dtbo
