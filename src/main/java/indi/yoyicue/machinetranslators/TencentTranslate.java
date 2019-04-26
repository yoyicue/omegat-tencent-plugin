package indi.yoyicue.machinetranslators;

import java.awt.Window;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.tmt.v20180321.TmtClient;
import com.tencentcloudapi.tmt.v20180321.models.TextTranslateRequest;
import com.tencentcloudapi.tmt.v20180321.models.TextTranslateResponse;
import org.omegat.core.Core;
import org.omegat.core.machinetranslators.BaseTranslate;
import org.omegat.gui.exttrans.MTConfigDialog;
import org.omegat.util.Language;

/**
 * Support for Tencent Cloud Translator API machine translation.
 * @author Li Da (yoyciue@gmail.com)
 * @see <a href="https://cloud.tencent.com/document/product/551/7377">Translator API</a>
 */

public class TencentTranslate extends BaseTranslate {

    protected static final String PROPERTY_API_SECRET_ID = "tencent.api.secret.Id";
    protected static final String PROPERTY_API_SECRET_KEY = "tencent.api.secret.Key";

    public static void loadPlugins() {
        Core.registerMachineTranslationClass(TencentTranslate.class);
    }

    public static void unloadPlugins() {
    }

    @Override
    protected String getPreferenceName() {
        return "allow_tencent_translate";
    }

    public String getName() {
        return "Tencent Translate";
    }

    @Override
    protected String translate(Language sLang, Language tLang, String text) throws Exception {

        String SecretId = getCredential(PROPERTY_API_SECRET_ID);
        String SecretKey = getCredential(PROPERTY_API_SECRET_KEY);

        Credential cred = new Credential(SecretId, SecretKey);
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("tmt.tencentcloudapi.com");

        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);

        TmtClient client = new TmtClient(cred, "ap-guangzhou", clientProfile);

        String sourcelang = tmtLang(sLang);
        String targetlang = tmtLang(tLang);

        TreeMap<String, Object> params = new TreeMap<String, Object>();
        params.put("SourceText", text);
        params.put("Source", sourcelang);
        params.put("Target", targetlang);
        params.put("ProjectId", "0");

        Gson gson = new Gson();
        TextTranslateRequest req = TextTranslateRequest.fromJsonString(gson.toJson(params), TextTranslateRequest.class);

        String translation;
        try{
            TextTranslateResponse resp = client.TextTranslate(req);
            translation = resp.getTargetText();
        } catch (TencentCloudSDKException e) {
            translation = e.toString();
        }
        return translation;
    }

    private String tmtLang(Language language) {
        String lang = language.getLanguage();
        if (lang.equalsIgnoreCase("zh-cn") || lang.equalsIgnoreCase("zh-hk") || lang.equalsIgnoreCase("zh-tw")) {
            return "zh";
        } else if (lang.equalsIgnoreCase("ja")) {
            return "jp";
        } else if (lang.equalsIgnoreCase("ko")) {
            return "kr";
        } else {
            return language.getLanguageCode().toLowerCase();
        }

    }
    
    @Override
    public boolean isConfigurable() {
        return true;
    }
    
    @Override
    public void showConfigurationUI(Window parent) {
        MTConfigDialog dialog = new MTConfigDialog(parent, getName()) {
            @Override
            protected void onConfirm() {
                String id = panel.valueField1.getText().trim();
                String key = panel.valueField2.getText().trim();
                boolean temporary = panel.temporaryCheckBox.isSelected();
                setCredential(PROPERTY_API_SECRET_ID, id, temporary);
                setCredential(PROPERTY_API_SECRET_KEY, key, temporary);
            }
        };
        dialog.panel.valueLabel1.setText("secretId");
        dialog.panel.valueField1.setText(getCredential(PROPERTY_API_SECRET_ID));
        dialog.panel.valueLabel2.setText("secretKey");
        dialog.panel.valueField2.setText(getCredential(PROPERTY_API_SECRET_KEY));
        dialog.panel.temporaryCheckBox.setSelected(isCredentialStoredTemporarily(PROPERTY_API_SECRET_ID));
        dialog.panel.temporaryCheckBox.setSelected(isCredentialStoredTemporarily(PROPERTY_API_SECRET_KEY));
        dialog.show();
    }
}
