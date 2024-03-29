package com.base.maker;

import com.xpert.faces.bootstrap.BootstrapVersion;
import com.xpert.faces.primefaces.PrimeFacesVersion;
import com.xpert.maker.MakerSwingFrame;

/**
 *
 * @author ayslan
 */
public class Maker extends MakerSwingFrame {

    @Override
    public String getDefaultPackage() {
        return "com.base.modelo";
    }

    @Override
    public String getDefaultTemplatePath() {
        return "/template/mainTemplate.xhtml";
    }

    @Override
    public String getDefaultResourceBundle() {
        return "msg";
    }

    @Override
    public String getDefaultBaseDAOImpl() {
        return "com.base.application.BaseDAOImpl";
    }

    public static void main(String[] args) {
        run(new Maker());
    }

    @Override
    public String getManagedBeanSuffix() {
        return "MB";
    }

    @Override
    public String getBusinessObjectSuffix() {
        return "BO";
    }

    @Override
    public PrimeFacesVersion getPrimeFacesVersion() {
        return PrimeFacesVersion.VERSION_5;
    }

    @Override
    public BootstrapVersion getBootstrapVersion() {
        return BootstrapVersion.VERSION_3;
    }
    
    
    
}
