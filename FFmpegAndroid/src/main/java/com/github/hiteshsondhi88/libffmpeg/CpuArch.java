package com.github.hiteshsondhi88.libffmpeg;

import android.text.TextUtils;

enum CpuArch {
    x86("7b418aa3039a639ad2dd4f9972415c530e014d9c"),
    ARMv7("ff6150ee31903a0f834ad793c57794fc1a5cd0ba"),
    ARMv7_NEON("e73ae729faed2f6b1b1e1fadea6e2170d6a10f49"),
    NONE(null);

    private String sha1;

    CpuArch(String sha1) {
        this.sha1 = sha1;
    }

    String getSha1(){
        return sha1;
    }

    static CpuArch fromString(String sha1) {
        if (!TextUtils.isEmpty(sha1)) {
            for (CpuArch cpuArch : CpuArch.values()) {
                if (sha1.equalsIgnoreCase(cpuArch.sha1)) {
                    return cpuArch;
                }
            }
        }
        return NONE;
    }
}
