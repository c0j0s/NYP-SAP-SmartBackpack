package com.nyp.fypj.smartbackpackapp.data.glide;

import android.content.Context;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.sap.cloud.mobile.foundation.common.ClientProvider;
import java.io.InputStream;
import okhttp3.OkHttpClient;

@GlideModule
public class CustomGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        // Set up Glide to use application OkHttpClient
        OkHttpClient client = ClientProvider.get();
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);
        glide.getRegistry().replace(GlideUrl.class, InputStream.class, factory);
    }
}