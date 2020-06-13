package su.dreamtime.dtrefsystem;

import su.dreamtime.dtrefsystem.data.Database;

public class Referrer {
    private String name;
    private String code;
    private int referalsCount;

    private Referrer(String name, String code) {
        this(name, code, 0);
    }

    public Referrer(String name, String code, int referalsCount) {
        this.name = name;
        this.code = code;
        this.referalsCount = referalsCount;
    }

    public static Referrer createReferrer(String name) {

        Referrer r = new Referrer(name, Database.rndString(10));
        return r;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public int getReferalsCount() {
        return referalsCount;
    }
}
