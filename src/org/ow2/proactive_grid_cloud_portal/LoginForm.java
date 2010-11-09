package org.ow2.proactive_grid_cloud_portal;

import java.io.InputStream;

import javax.ws.rs.FormParam;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.resteasy.annotations.providers.multipart.PartType;


/**
 * a class that represent a mean to submit credential through the rest api.
 * A credential can be either 
 *   - a username/password couple
 *   - a file submitted through an HTTP form and whose name is 'credential'
 */
@XmlRootElement
public class LoginForm {
    /**
     * a representation of a serialized Credentials class
     */
    private InputStream credential;
    
    /**
     * username
     */
    private String username;
    
    /**
     * password
     */
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
