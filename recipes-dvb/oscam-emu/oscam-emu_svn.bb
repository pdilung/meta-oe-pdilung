SUMMARY = "OSCam: Open Source Conditional Access Module with oscam-emu patch"
HOMEPAGE = "http://www.streamboard.tv/oscam/ \
	https://github.com/oscam-emu/oscam-emu"



LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"

DEPENDS = "libusb1 openssl pcsc-lite"

SRCREV = "11425"
EMUREV = "770"
PV = "1.20+r${SRCPV}+r${EMUREV}"
PR = "r0"

CAMFILE="Ncam_${PN}.sh"
CAMNAME="Oscam ${PV} EMU r${EMUREV}"

SRC_URI = "svn://www.streamboard.tv/svn/oscam;module=trunk;protocol=http;rev=${SRCREV};scmdata=keep \
	file://add-caid-0653-to-irdeto-reader.patch;patch=1 \
	file://oscam-emu-${EMUREV}.patch;patch=1"


S = "${WORKDIR}/trunk"

FILES_${PN} += "/usr/camscript*"

inherit cmake

do_configure_prepend() {
    ${S}/config.sh --restore --enable WITH_SSL
}

EXTRA_OECMAKE = "-DDEFAULT_CS_CONFDIR=${sysconfdir} -DCMAKE_BUILD_TYPE=Debug"

do_install_append() {
    cat > ${CAMFILE} <<-EOF
	#!/bin/sh

	CAMNAME="${CAMNAME}"

	remove_tmp () {
		rm -rf /tmp/*.info* /tmp/*.tmp*
	}

	case "\$1" in
		start)
			echo "[SCRIPT] \$1: \$CAMNAME"
			remove_tmp
			/usr/bin/oscam -b &
			sleep 2
			;;
		stop)
			echo "[SCRIPT] \$1: \$CAMNAME"
			killall -9 oscam 2>/dev/null
			sleep 2
			remove_tmp
			;;
		*)
			\$0 stop
			exit 0
			;;
	esac

	exit 0
	EOF

	install -d -m 0755 ${D}/usr/camscript
	install -m 0755 ${CAMFILE} ${D}/usr/camscript/${CAMFILE}
}

pkg_prerm_${PN}_prepend() {
	if [ -x /usr/camscript/${CAMFILE} ]; then
		/usr/camscript/${CAMFILE} stop
	fi
}

pkg_postinst_${PN}_append() {
    if [ -x /usr/camscript/${CAMFILE} ]; then
        /usr/camscript/${CAMFILE} start
    fi
}
