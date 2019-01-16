package com.nyp.fypj.smartbackpackapp.logon;

import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthenticationInterceptor implements Interceptor {

    String credentials = null;

    public AuthenticationInterceptor(String user,String password){
        credentials = Credentials.basic(user, password);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
            Request authenticatedRequest = chain.request().newBuilder().header("Authorization", credentials).build();
            return chain.proceed(authenticatedRequest);
    }
}
