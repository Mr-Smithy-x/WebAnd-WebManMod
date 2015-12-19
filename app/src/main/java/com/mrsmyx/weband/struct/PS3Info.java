package com.mrsmyx.weband.struct;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Charlton on 9/10/2015.
 */
public class PS3Info implements Serializable {
    private String CPU;
    private String RSX;
    private String MEM;
    private String FAN;
    private String PSID;
    private String IDPSEID0;
    private String IDPSLV2;
    private String MACAddr;

    public ArrayList<String> toConfidArrayList(){
        ArrayList<String> a = new ArrayList<>();
        a.add(FIRM);
        a.add(HDD);
        a.add(PSID);
        a.add(IDPSLV2);
        a.add(IDPSEID0);
        a.add(MACAddr);
        return a;
    }

    public String getCPU_C() {
        return CPU_C;
    }

    public PS3Info setCPU_C(String CPU_C) {
        this.CPU_C = CPU_C;
        return this;
    }

    public String getRSX_C() {
        return RSX_C;
    }

    public PS3Info setRSX_C(String RSX_C) {
        this.RSX_C = RSX_C;
        return this;
    }

    public String getUP_T() {
        return UP_T;
    }

    public PS3Info setUP_T(String UP_T) {
        this.UP_T = UP_T;
        return this;
    }

    private String CPU_C;
    private String RSX_C;
    private String UP_T;

    public static PS3Info Build() {
        return new PS3Info();
    }

    public String getCPU() {
        return CPU;
    }

    public PS3Info setCPU(String CPU) {
        this.CPU = CPU;
        return this;
    }

    public String getRSX() {
        return RSX;
    }

    public PS3Info setRSX(String RSX) {
        this.RSX = RSX;
        return this;
    }

    public String getMEM() {
        return MEM;
    }

    public PS3Info setMEM(String MEM) {
        this.MEM = MEM;
        return this;
    }

    public String getHDD() {
        return HDD;
    }

    public PS3Info setHDD(String HDD) {
        this.HDD = HDD;
        return this;
    }

    public String getFIRM() {
        return FIRM;
    }

    public PS3Info setFIRM(String FIRM) {
        this.FIRM = FIRM;
        return this;
    }

    private String HDD;
    private String FIRM;

    public PS3Info setFAN(String FAN) {
        this.FAN = FAN;
        return this;
    }

    public String getFAN() {
        return FAN;
    }

    public void setPSID(String PSID) {
        this.PSID = PSID;
    }

    public String getPSID() {
        return PSID;
    }

    public void setIDPSEID0(String IDPSEID0) {
        this.IDPSEID0 = IDPSEID0;
    }

    public String getIDPSEID0() {
        return IDPSEID0;
    }

    public void setIDPSLV2(String IDPSLV2) {
        this.IDPSLV2 = IDPSLV2;
    }

    public String getIDPSLV2() {
        return IDPSLV2;
    }

    public void setMACAddr(String MACAddr) {
        this.MACAddr = MACAddr;
    }

    public String getMACAddr() {
        return MACAddr;
    }
}
