
package com.example.azat.weathergdg.entity.weatherElements;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Sys_ {

    @SerializedName("pod")
    @Expose
    private String pod;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Sys_() {
    }

    /**
     * 
     * @param pod
     */
    public Sys_(String pod) {
        this.pod = pod;
    }

    /**
     * 
     * @return
     *     The pod
     */
    public String getPod() {
        return pod;
    }

    /**
     * 
     * @param pod
     *     The pod
     */
    public void setPod(String pod) {
        this.pod = pod;
    }

}
