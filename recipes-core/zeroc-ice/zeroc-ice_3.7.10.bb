SUMMARY  = "ZeroC Ice for Yocto"
DESCRIPTION = "Comprehensive RPC framework"
HOMEPAGE = "https://zeroc.com"
SECTION  = "libs"

LICENSE  = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://ICE_LICENSE;md5=51cbbfae4849a92975efff73e4de3a0c \
                    file://LICENSE;md5=1b65bb9598f16820aab2ae1dd2a51f9f"

SRCREV  = "0d8c494068b694d42e7d21a1e07d4436619e8ecb"

PV = "3.7.10"
PR = "r0"

SRC_URI = "git://github.com/zeroc-ice/ice;branch=3.7;protocol=https"

S = "${WORKDIR}/git"
B = "${WORKDIR}/git"

inherit pkgconfig python3native python3-dir

DEPENDS  = "openssl bzip2 mcpp lmdb expat python3 libedit"
BLUEZ = "bluez5"

DEPENDS:append:class-target = " ${@bb.utils.contains('DISTRO_FEATURES', 'bluetooth', d.expand('${BLUEZ} dbus-glib'), '', d)}"
DEPENDS:append:class-nativesdk = " ${@bb.utils.contains('DISTRO_FEATURES', 'bluetooth', d.expand('${BLUEZ} dbus-glib'), '', d)}"

DEPENDS:append:class-target = " zeroc-ice-native"
DEPENDS:append:class-nativesdk = " zeroc-ice-native"

RDEPENDS_${BPN} = "openssl bzip2 expat"

#
# OECORE_SDK_VERSION is always set in an SDK. To get the Ice build system to
# detect a Yocto/OE build just need to to be set here.
#

EXTRA_OEMAKE = "OECORE_SDK_VERSION=yes V=1"
EXTRA_OEMAKE:append:class-target = " CONFIGS=all ICE_HOME=${STAGING_DIR_NATIVE}/usr ICE_BIN_DIST=compilers"
EXTRA_OEMAKE:append:class-nativesdk = " CONFIGS=all ICE_HOME=${STAGING_DIR_NATIVE}/usr ICE_BIN_DIST=compilers"

do_compile () {
    oe_runmake LANGUAGES="cpp python" srcs
}

do_configure () {
    oe_runmake LANGUAGES="cpp python" distclean
}

do_install () {
    oe_runmake LANGUAGES="cpp python" \
    DESTDIR=${D} prefix=${prefix} USR_DIR_INSTALL=yes \
    PYTHON_INSTALLDIR=${PYTHON_SITEPACKAGES_DIR} install
}

#
# Use bellow targets for native
#
do_compile:class-native () {
    oe_runmake -C cpp slice2cpp slice2py
}

do_configure:class-native () {
    oe_runmake distclean -C cpp
}

do_install:class-native () {
    oe_runmake DESTDIR=${D} prefix=${prefix} USR_DIR_INSTALL=yes install-slice
    oe_runmake DESTDIR=${D} prefix=${prefix} USR_DIR_INSTALL=yes -C cpp slice2cpp_install slice2py_install
}

FILES:${PN} += "${base_prefix}/usr/share/ice/templates.xml \
                ${base_prefix}/usr/share/ice/ICE_LICENSE \
                ${base_prefix}/usr/share/ice/LICENSE"

# Add slice compilers and -slice dependency to -dev
FILES:${PN}-dev += "${bindir}/slice2*"
DEPENDS:${PN}-dev= "${PN}-slice"
RDEPENDS:${PN}-dev= "${PN}-slice"

# Glacier2
PACKAGES =+ "zeroc-glacier2"
FILES:zeroc-glacier2 += "${bindir}/glacier2router"

# IceGrid
PACKAGES =+ "zeroc-icegrid"
FILES:zeroc-icegrid += "${bindir}/icegrid*"

# IcePatch2
PACKAGES =+ "zeroc-icepatch2"
FILES:zeroc-icepatch2 += "${bindir}/icepatch2*"

# IceBox
PACKAGES =+ "zeroc-icebox"
FILES:zeroc-icebox += "${bindir}/icebox*"

# IceStorm
PACKAGES =+ "zeroc-icestorm"
FILES:zeroc-icestorm += "${bindir}/icestorm*"

# IceBridge
PACKAGES =+ "zeroc-icebridge"
FILES:zeroc-icebridge += "${bindir}/icebridge*"

# Python
PACKAGES += "${PN}-python3"
FILES:${PN}-python3 += "${PYTHON_SITEPACKAGES_DIR}"
RDEPENDS:${PN}-python3 = "${PN}-slice python3-core"

# Slice
PACKAGES =+ "${PN}-slice"
FILES:${PN}-slice += "${base_prefix}/usr/share/ice/slice ${base_prefix}/usr/share/slice"

# Utils
PACKAGES =+ "${PN}-utils"
FILES:${PN}-utils += "${bindir}/*admin"

# Add native and nativesdk support
BBCLASSEXTEND += "native nativesdk"
