package org.jasig.cas.authentication;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * CasServer添加验证码校验
 * Created by fengjing on 2016/4/27.
 */
public class UsernamePasswordCaptchaCredential extends RememberMeUsernamePasswordCredential {

    private static final long serialVersionUID = 8317889802836113837L;

    @NotNull
    @Size(min = 1, message = "required.captcha")
    private String captcha;

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((captcha == null) ? 0 : captcha.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        UsernamePasswordCaptchaCredential other = (UsernamePasswordCaptchaCredential) obj;
        if (captcha == null) {
            if (other.captcha != null)
                return false;
        } else if (!captcha.equals(other.captcha))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "UsernamePasswordCaptchaCredentials [captcha=" + captcha + ", isRememberMe()=" + isRememberMe()
                + ", getPassword()=" + getPassword() + ", getUsername()=" + getUsername() + ", toString()="
                + super.toString() + ", getClass()=" + getClass() + "]";
    }
}