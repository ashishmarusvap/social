package com.social;

public interface AuthenticationInterface {
     void okLogin(SocialLoginModel user);
     void failedLogin(String st);
}

