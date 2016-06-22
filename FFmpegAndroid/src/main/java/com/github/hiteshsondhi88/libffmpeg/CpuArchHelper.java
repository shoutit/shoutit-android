package com.github.hiteshsondhi88.libffmpeg;

import android.annotation.TargetApi;
import android.os.Build;

import java.util.Arrays;
import java.util.HashSet;

class CpuArchHelper {

    static CpuArch getCpuArch() {
        // check if device is x86
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return lollipopArch();
        } else {
            return preLollipopArch();
        }
    }

    private static CpuArch preLollipopArch() {
        if (Build.CPU_ABI.equals(getx86CpuAbi())) {
            return CpuArch.x86;
        } else {
            // check if device is armeabi
            if (Build.CPU_ABI.equals(getArmeabiv7CpuAbi())) {
                ArmArchHelper cpuNativeArchHelper = new ArmArchHelper();
                String archInfo = cpuNativeArchHelper.cpuArchFromJNI();
                // check if device is arm v7
                if (cpuNativeArchHelper.isARM_v7_CPU(archInfo)) {
                    // check if device is neon
                    if (cpuNativeArchHelper.isNeonSupported(archInfo)) {
                        return CpuArch.ARMv7_NEON;
                    }
                    return CpuArch.ARMv7;
                }
            }
            return CpuArch.NONE;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static CpuArch lollipopArch() {
        final HashSet<String> abisSet = new HashSet<>(Arrays.asList(Build.SUPPORTED_ABIS));
        if (abisSet.contains(getx86CpuAbi())) {
            return CpuArch.x86;
        } else if (abisSet.contains(getArmeabiv7CpuAbi())) {
            return CpuArch.ARMv7;
        } else {
            return CpuArch.NONE;
        }
    }

    static String getx86CpuAbi() {
        return "x86";
    }

    static String getArmeabiv7CpuAbi() {
        return "armeabi-v7a";
    }
}
