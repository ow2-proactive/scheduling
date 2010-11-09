package org.ow2.proactive_grid_cloud_portal;

import java.io.InputStream;

import javax.ws.rs.FormParam;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

public class LoginForm {
    private InputStream credential;
    private String username;
    private String password;

    public LoginForm() {}

    public InputStream getCredential() {
        return credential;
    }

    @FormParam("credential")
    @PartType("application/octet-stream")
    public void setFileData(final InputStream filedata) {
        this.credential = filedata;
    }

    public String getUsername() {
        return username;
    }

    @FormParam("username")
    public void setLogin(String login) {
        this.username = login;
    }

    public String getPassword() {
        return password;
    }

    @FormParam("password")
    public void setPassword(String password) {
        this.password = password;
    }

}
