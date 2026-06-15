package pro.sketchware.ai.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import pro.sketchware.ai.model.AiProviderType;

public final class AiSecretStore {
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "sketchware_complete_ai_api_keys";
    private static final String PREFS = "ai_agent_secrets";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    private AiSecretStore() {
    }

    public static void put(Context context, AiProviderType provider, String secret) {
        if (secret == null || secret.isBlank()) {
            remove(context, provider);
            return;
        }
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
            byte[] encrypted = cipher.doFinal(secret.getBytes(StandardCharsets.UTF_8));
            String value = Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP)
                    + ":"
                    + Base64.encodeToString(encrypted, Base64.NO_WRAP);
            preferences(context).edit().putString(provider.name(), value).apply();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to encrypt AI API key", exception);
        }
    }

    public static String get(Context context, AiProviderType provider) {
        String value = preferences(context).getString(provider.name(), "");
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            String[] parts = value.split(":", 2);
            if (parts.length != 2) {
                remove(context, provider);
                return "";
            }
            byte[] iv = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] encrypted = Base64.decode(parts[1], Base64.NO_WRAP);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            remove(context, provider);
            return "";
        }
    }

    public static void remove(Context context, AiProviderType provider) {
        preferences(context).edit().remove(provider.name()).apply();
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static synchronized SecretKey getOrCreateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        if (keyStore.containsAlias(KEY_ALIAS)) {
            return ((KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null)).getSecretKey();
        }
        KeyGenerator generator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
        generator.init(new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build());
        return generator.generateKey();
    }
}
